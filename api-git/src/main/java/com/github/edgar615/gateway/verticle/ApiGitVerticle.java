package com.github.edgar615.gateway.verticle;

import com.google.common.base.Strings;

import com.github.edgar615.util.base.EncryptUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.VertxException;
import io.vertx.core.json.JsonObject;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * 从GIT处加载API的定义文件到本地.
 * 这个组件只负责接收消息从git更新文件，真正的API加载交由FileApiDiscoveryVerticle处理，
 * 由于git是一个耗时操作，所以应该交由工作线程来处理
 * <p>
 * 这部分核心代码是从vertx-config-git中复制而来
 *
 * @author Edgar  Date 2018/2/1
 */
public class ApiGitVerticle extends AbstractVerticle {
    private final static Logger LOGGER
            = LoggerFactory.getLogger(ApiGitVerticle.class);

    private static final String RELOAD_ADDR_PREFIX =
            "__com.github.edgar615.gateway.api.discovery.reload.file";

    private static final String GIT_ADDR_PREFIX =
            "__com.github.edgar615.gateway.api.discovery.git";

    private static final String WEBHOOK_ADDR_PREFIX =
            "__com.github.edgar615.gateway.api.discovery.git.webhook";

    private File path;

    private String url;

    private String branch;

    private String remote;

    private String secret;

    private static boolean verifySignature(String payload, String signature, String secret) {
        boolean isValid = false;

        try {
            String actual = EncryptUtils.encryptHmacSha1(payload, secret);
            String excepted = signature.substring(5);
            isValid = excepted.equalsIgnoreCase(actual);

        } catch (IOException ex) {

            ex.printStackTrace();

        }

        return isValid;
    }

    @Override
    public void start() throws Exception {
        LOGGER.info("[Verticle] [start] start {}",
                    ApiGitVerticle.class.getSimpleName());
        String path = Objects.requireNonNull(config().getString("path"),
                                             "The `path` configuration is required.");
        this.path = new File(path);
        if (this.path.isFile()) {
            logError("path must be a file");
            throw new IllegalArgumentException("The `path` must be a file");
        }

        // Git repository
        url = Objects.requireNonNull(config().getString("url"),
                                     "The `url` configuration (Git repository location) is "
                                     + "required.");
        branch = config().getString("branch", "master");
        remote = config().getString("remote", "origin");
        pullApi();

        vertx.eventBus().<JsonObject>consumer(GIT_ADDR_PREFIX, msg -> {
            pullApi();
        });

        String secret = config().getString("secret");
        vertx.eventBus().<JsonObject>consumer(WEBHOOK_ADDR_PREFIX, msg -> {
            if (Strings.isNullOrEmpty(secret)) {
                pullApi();
                return;
            }
            //校验
            String signature = msg.body().getString("signature");
            String payload = msg.body().getString("payload");
            if (verifySignature(payload, signature, secret)) {
                pullApi();
            }
        });
    }

    public void pullApi() {
        try {
            initializeGit();
        } catch (Exception e) {
            logError("Unable to initialize the Git repository");
            throw new VertxException("Unable to initialize the Git repository", e);
        }
        vertx.eventBus().send(RELOAD_ADDR_PREFIX, new JsonObject());
    }

    private void logError(String message) {
        LOGGER.error("[Verticle] [start] start {} failed, {}",
                     ApiGitVerticle.class.getSimpleName(), message);
    }

    private Git initializeGit() throws IOException, GitAPIException {
        if (path.isDirectory()) {
            Git git = Git.open(path);
            String current = git.getRepository().getBranch();
            if (branch.equalsIgnoreCase(current)) {
                PullResult pull = git.pull().setRemote(remote).call();
                if (!pull.isSuccessful()) {
                    logError("Unable to pull the branch + '" + branch +
                             "' from the remote repository '" + remote + "'");
                }
                return git;
            } else {
                git.checkout().
                        setName(branch).
                        setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK).
                        setStartPoint(remote + "/" + branch).
                        call();
                return git;
            }
        } else {
            return Git.cloneRepository()
                    .setURI(url)
                    .setBranch(branch)
                    .setRemote(remote)
                    .setDirectory(path)
                    .call();
        }
    }
}
