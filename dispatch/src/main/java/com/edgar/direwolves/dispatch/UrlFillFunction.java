//package com.edgar.direwolves.dispatch;
//
//import com.google.common.collect.Lists;
//import com.google.common.collect.Multimap;
//
//import com.csst.microservice.api.dispatcher.ApiContext;
//import com.csst.util.exception.DefaultErrorCode;
//import com.csst.util.exception.SystemException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.List;
//import java.util.Set;
//import java.util.function.BiFunction;
//
///**
// * Created by Edgar on 2016/5/12.
// *
// * @author Edgar  Date 2016/5/12
// */
//class UrlFillFunction implements BiFunction<String, ApiContext, String> {
//
//  private static final Logger LOGGER = LoggerFactory.getLogger(
//          UrlFillFunction.class);
//
//  private static final UrlFillFunction INSTANCE = new UrlFillFunction();
//
//  private UrlFillFunction() {}
//
//  static UrlFillFunction instance() {
//    return INSTANCE;
//  }
//  /**
//   * 获取Multimap中的第一个参数.
//   *
//   * @param params
//   * @param paramName
//   * @return
//   */
//  String getFirst(Multimap<String, String> params, String paramName) {
//    List<String> values = Lists.newArrayList(params.get(paramName));
//    if (values.isEmpty()) {
//      return null;
//    }
//    return values.get(0);
//  }
//
//  @Override
//  public String apply(String url, ApiContext context) {
//    //路径参数
//    Set<String> names = context.params().asMap().keySet();
//    for (String name : names) {
//      url = url.replaceAll("\\$param." + name,
//                           getFirst(context.params(), name));
//    }
//    if (url.contains("$auth.userId")) {
//      if (context.getUser() == null) {
//        throw SystemException.create(DefaultErrorCode.NO_AUTHORITY);
//      } else {
//        url = url.replaceAll("\\$auth.userId", context.getUser().getUserId() + "");
//      }
//    }
//    if (url.contains("$auth.companyCode")) {
//      if (context.getUser() == null) {
//        throw SystemException.create(DefaultErrorCode.NO_AUTHORITY);
//      } else {
//        url = url.replaceAll("\\$auth.companyCode", context.getUser().getCompanyCode() + "");
//      }
//    }else if (url.contains("$auth.username")) {//从auth填充
//        if (context.getUser() == null) {
//            throw SystemException.create(DefaultErrorCode.NO_AUTHORITY);
//        } else {
//            url = url.replaceAll("\\$auth.username", context.getUser().getUsername() + "");
//        }
//    } else if (url.contains("$auth.tel")) {//从auth填充
//        if (context.getUser() == null) {
//            throw SystemException.create(DefaultErrorCode.NO_AUTHORITY);
//        } else {
//            url = url.replaceAll("\\$auth.tel", context.getUser().getTel() + "");
//        }
//    } else if (url.contains("$auth.mail")) {//从auth填充
//        if (context.getUser() == null) {
//            throw SystemException.create(DefaultErrorCode.NO_AUTHORITY);
//        } else {
//            url = url.replaceAll("\\$auth.mail", context.getUser().getMail() + "");
//        }
//    } else if (url.contains("$auth.fullname")) {//从auth填充
//        if (context.getUser() == null) {
//            throw SystemException.create(DefaultErrorCode.NO_AUTHORITY);
//        } else {
//            url = url.replaceAll("\\$auth.fullname", context.getUser().getFullname() + "");
//        }
//    } else if (url.contains("$auth.type")) {//从auth填充
//        if (context.getUser() == null) {
//            throw SystemException.create(DefaultErrorCode.NO_AUTHORITY);
//        } else {
//            url = url.replaceAll("\\$auth.type", context.getUser().getType() + "");
//        }
//    }
//    return url;
//  }
//}
