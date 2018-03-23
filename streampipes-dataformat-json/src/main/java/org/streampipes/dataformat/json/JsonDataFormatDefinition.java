/*
 * Copyright 2018 FZI Forschungszentrum Informatik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.streampipes.dataformat.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.streampipes.commons.exceptions.SpRuntimeException;
import org.streampipes.dataformat.SpDataFormatDefinition;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JsonDataFormatDefinition implements SpDataFormatDefinition {

  private ObjectMapper objectMapper;

  public JsonDataFormatDefinition() {
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public Map<String, Object> toMap(byte[] event) throws SpRuntimeException {
    try {
      return objectMapper.readValue(new String(event), HashMap.class);
    } catch (IOException e) {
      throw new SpRuntimeException("Could not convert event to map data structure");
    }
  }

  @Override
  public byte[] fromMap(Map<String, Object> event) throws SpRuntimeException {
    try {
      return objectMapper.writeValueAsBytes(event);
    } catch (JsonProcessingException e) {
      throw new SpRuntimeException("Could not convert map data structure to JSON string");
    }
  }
}
