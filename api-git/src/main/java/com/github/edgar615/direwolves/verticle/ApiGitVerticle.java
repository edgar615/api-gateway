package com.github.edgar615.direwolves.verticle;

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
 * 这个组件只负责接收消息从git更新文件，真正的API加载交由FileApiDiscoveryVerticle处理
 * <p>
 * 这部分核心代码是从vertx-config-git中复制而来
 *
 * @author Edgar  Date 2018/2/1
 */
public class ApiGitVerticle extends AbstractVerticle {
  private final static Logger LOGGER
          = LoggerFactory.getLogger(ApiGitVerticle.class);

  private static final String RELOAD_ADDR_PREFIX = "api.discovery.reload.";

  private static final String GIT_ADDR_PREFIX = "api.discovery.git.";

  private static final String WEBHOOK_ADDR_PREFIX = "api.discovery.webhook.";

  private File path;

  private String url;

  private String branch;

  private String remote;

  private String name;

  private String secret;

  @Override
  public void start() throws Exception {
    String name = Objects.requireNonNull(config().getString("name"),
                                         "The `name` configuration is required.");
    this.name = name;
    String path = Objects.requireNonNull(config().getString("path"),
                                         "The `path` configuration is required.");
    this.path = new File(path);
    if (this.path.isFile()) {
      throw new IllegalArgumentException("The `path` must not be a file");
    }

    // Git repository
    url = Objects.requireNonNull(config().getString("url"),
                                 "The `url` configuration (Git repository location) is required.");
    branch = config().getString("branch", "master");
    remote = config().getString("remote", "origin");
    pullApi();

    vertx.eventBus().<JsonObject>consumer(GIT_ADDR_PREFIX + name, msg -> {
      pullApi();
    });

    String secret = config().getString("secret");
    vertx.eventBus().<JsonObject>consumer(WEBHOOK_ADDR_PREFIX + name, msg -> {
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

  private static boolean verifySignature(String payload, String signature, String secret) {
    boolean isValid = false;

    try {
      String actual = EncryptUtils.encryptHmacSha1(payload, secret);
      String excepted = signature.substring(5);
      System.out.println(actual);
      System.out.println(excepted);
      isValid = excepted.equalsIgnoreCase(actual);

    } catch (IOException ex) {

      ex.printStackTrace();

    }

    return isValid;
  }

  public void pullApi() {
    try {
      initializeGit();
    } catch (Exception e) {
      throw new VertxException("Unable to initialize the Git repository", e);
    }
    vertx.eventBus().send(RELOAD_ADDR_PREFIX + name, new JsonObject());
  }

  private Git initializeGit() throws IOException, GitAPIException {
    if (path.isDirectory()) {
      Git git = Git.open(path);
      String current = git.getRepository().getBranch();
      if (branch.equalsIgnoreCase(current)) {
        PullResult pull = git.pull().setRemote(remote).call();
        if (!pull.isSuccessful()) {
          LOGGER.warn("Unable to pull the branch + '" + branch +
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
