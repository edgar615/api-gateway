package com.edgar.direwolves.plugin.response;

/**
 * Created by edgar on 16-11-5.
 */
class ExtractResultPluginImpl implements ExtractResultPlugin {

  /**
   * 如果只有一个endpoint，直接将endpoint的响应提取为返回值.
   */
  private final boolean extractValueFromSingleKeyModel = true;

  /**
   * 如果有多个endpoint，将endpoint的响应通过name合并到一个JsonObject中
   */
  private final boolean zipValueFromMultiKeyModel = true;

//  AtLeastOneSuccessfulStrategy firstSuccessfulStrategy

  /**
   * 只有所有的都成功才算成功，如果有一个失败就失败了，返回第一个失败的结果
   */
  private final boolean allSuccessfulStrategy = true;

  ExtractResultPluginImpl() {
  }

  public boolean isExtractValueFromSingleKeyModel() {
    return extractValueFromSingleKeyModel;
  }

  public boolean isZipValueFromMultiKeyModel() {
    return zipValueFromMultiKeyModel;
  }

  public boolean isAllSuccessfulStrategy() {
    return allSuccessfulStrategy;
  }
}
