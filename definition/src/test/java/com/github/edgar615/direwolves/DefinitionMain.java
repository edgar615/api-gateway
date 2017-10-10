package com.github.edgar615.direwolves;

import com.github.edgar615.direwolves.verticle.ApiDefinitionVerticle;
import io.vertx.core.Launcher;

/**
 * Created by Edgar on 2017/6/20.
 *
 * @author Edgar  Date 2017/6/20
 */
public class DefinitionMain {
  public static void main(String[] args) {
    new Launcher().execute("run", ApiDefinitionVerticle.class.getName(),
                           "--cluster",
                           "--conf=H:\\dev\\workspace\\direwolves\\definition\\src\\main\\conf"
                           + "\\definition.json");
  }
}
