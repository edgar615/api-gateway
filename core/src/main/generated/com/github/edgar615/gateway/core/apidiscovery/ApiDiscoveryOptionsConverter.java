/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.github.edgar615.gateway.core.apidiscovery;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

/**
 * Converter for {@link com.github.edgar615.gateway.core.apidiscovery.ApiDiscoveryOptions}.
 *
 * NOTE: This class has been automatically generated from the {@link com.github.edgar615.gateway.core.apidiscovery.ApiDiscoveryOptions} original class using Vert.x codegen.
 */
public class ApiDiscoveryOptionsConverter {

  public static void fromJson(JsonObject json, ApiDiscoveryOptions obj) {
    if (json.getValue("name") instanceof String) {
      obj.setName((String)json.getValue("name"));
    }
    if (json.getValue("publishedAddress") instanceof String) {
      obj.setPublishedAddress((String)json.getValue("publishedAddress"));
    }
    if (json.getValue("unpublishedAddress") instanceof String) {
      obj.setUnpublishedAddress((String)json.getValue("unpublishedAddress"));
    }
  }

  public static void toJson(ApiDiscoveryOptions obj, JsonObject json) {
    if (obj.getName() != null) {
      json.put("name", obj.getName());
    }
    if (obj.getPublishedAddress() != null) {
      json.put("publishedAddress", obj.getPublishedAddress());
    }
    if (obj.getUnpublishedAddress() != null) {
      json.put("unpublishedAddress", obj.getUnpublishedAddress());
    }
  }
}