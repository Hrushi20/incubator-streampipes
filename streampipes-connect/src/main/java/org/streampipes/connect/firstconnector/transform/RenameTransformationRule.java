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

package org.streampipes.connect.firstconnector.transform;

import java.util.List;
import java.util.Map;

public class RenameTransformationRule implements TransformationRule {
    private List<String> oldKey;
    private  String newKey;

    public RenameTransformationRule(List<String> oldKey, String newKey) {
        this.oldKey = oldKey;
        this.newKey = newKey;
    }

    @Override
    public Map<String, Object> transform(Map<String, Object> event) {
        Map<String, Object> nestedEvent = event;

        return transform(event, oldKey);
    }

    private Map<String, Object> transform(Map<String, Object> event, List<String> keys) {
        if (keys.size() == 1) {
            Object o = event.get(keys.get(0));
            event.remove(keys.get(0));
            event.put(newKey, o);

        } else {
            String key = keys.get(0);
            List<String> newKeysTmpList = keys.subList(1, keys.size());
            Map<String, Object> newSubEvent =
                    transform((Map<String, Object>) event.get(key), newKeysTmpList);

            event.remove(key);
            event.put(key, newSubEvent);
        }

        return event;
    }
}

