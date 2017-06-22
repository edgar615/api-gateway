local rate_limits = cjson.decode(ARGV[1])
local now = tonumber(ARGV[2])
local tokens_to_take       = tonumber(ARGV[3]) or 1 --当前请求需要的令牌数量

--local prefix_key = "token.bucket." --限流KEY
local result = {}
local passed = true;
--计算请求是否满足每个限流规则
for i, rate_limit in ipairs(rate_limits) do
    local subject = "token.bucket." .. rate_limit[1] --桶的标识符
    local burst = math.max(tonumber(rate_limit[2]), 1) --桶中最大令牌数，最小值1，
    local refillTime = tonumber(rate_limit[3]) or 1000-- 向桶中添加令牌的周期，单位毫秒
    local refillAmount = math.max(tonumber(rate_limit[4]), 1) or 1 -- 每次refillTime向桶中添加的令牌数量，默认为1

    local available_tokens = burst --可用的令牌数默认等于桶的容量
    local last_update = now --第一次的last_update等于当前时间
    local current = redis.call('HMGET', subject, 'last_update', 'available_tokens')
    if current.err ~= nil then
        redis.call('DEL', subject)
        current = {}
        redis.log(redis.LOG_NOTICE, 'Cannot get ratelimit ' .. subject)
        return redis.error_reply('Cannot get ratelimit ' .. rate_limit[1])
    end

    --计算从上次的时间戳与当前时间戳计算应该添加的令牌数
    if current[1] then
        --上次请求的时间
        last_update = current[1]
        local content = current[2]
        --计算应该生成的令牌数
        local delta_ms = math.max(now - last_update, 0)
        local refillCount  = math.floor(delta_ms / refillTime) * refillAmount
        --如果桶满，直接使用桶的容量
        available_tokens = math.min(content + refillCount, burst)
    end
    if available_tokens < tokens_to_take then
        passed = false
        table.insert(result, { subject, burst, refillTime, refillAmount, available_tokens, 0, last_update})
    else
        table.insert(result, { subject, burst, refillTime, refillAmount, available_tokens, 1, last_update})
    end
end

-- 如果通过，增加每个限流规则
if passed == true then
    for key,value in ipairs(result) do
        value[5] = math.min( value[5] - tokens_to_take,  value[2])
        value[7]  =  value[7]  + math.floor(tokens_to_take /  value[4]  *  value[3] )

        --重新设置令牌
        redis.call('HMSET', value[1],
            'last_update', value[7],
            'available_tokens', value[5])

        --如果没有新的请求过来，在桶满之后可以直接将该令牌删除。
        redis.call('PEXPIRE', value[1], math.ceil((value[2] /  value[4]) * value[3]))
    end
end

-- 返回结果
local summary = {}
for key,value in ipairs(result) do
    local pass = value[6]
    local reset_time = value[7] - now
    if reset_time < 0 then
        reset_time = now - value[7]
    end
    if not pass or pass == 0 then
        --添加四个值　是否通过0或1　剩余请求数 最大请求数　重置时间
        table.insert(summary, 0)
        table.insert(summary,  math.max(value[5], 0))
        table.insert(summary, value[2])
        table.insert(summary, reset_time)
    else
        table.insert(summary, 1)
        table.insert(summary, math.max(value[5], 0))
        table.insert(summary, value[2])
        table.insert(summary, reset_time)
    end
end

return summary