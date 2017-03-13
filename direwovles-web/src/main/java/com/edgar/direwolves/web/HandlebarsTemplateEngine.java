package com.edgar.direwolves.web;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.ValueResolver;
import io.vertx.ext.web.templ.TemplateEngine;

public interface HandlebarsTemplateEngine extends TemplateEngine {

  /**
   * Default max number of templates to cache
   */
  int DEFAULT_MAX_CACHE_SIZE = 10000;

  /**
   * Default template extension
   */
  String DEFAULT_TEMPLATE_EXTENSION = "hbs";

  /**
   * Create a template engine using defaults
   *
   * @return  the engine
   */
  static HandlebarsTemplateEngine create() {
    return new HandlebarsTemplateEngineImpl();
  }

  /**
   * Set the extension for the engine
   *
   * @param extension  the extension
   * @return a reference to this for fluency
   */
  HandlebarsTemplateEngine setExtension(String extension);

  /**
   * Set the max cache size for the engine
   *
   * @param maxCacheSize  the maxCacheSize
   * @return a reference to this for fluency
   */
  HandlebarsTemplateEngine setMaxCacheSize(int maxCacheSize);

  /**
   * Get a reference to the internal Handlebars object so it
   * can be configured.
   *
   * @return a reference to the internal Handlebars instance.
   */
  Handlebars getHandlebars();

  /**
   * Return the array of configured handlebars context value resolvers.
   * @return array of configured resolvers
   */
  ValueResolver[] getResolvers();

  /**
   * Set the array of handlebars context value resolvers.
   * 
   * @param resolvers the value resolvers to be used
   * @return a reference to the internal Handlebars instance.
   */
  HandlebarsTemplateEngine setResolvers(ValueResolver... resolvers);

  void clearCache();

}