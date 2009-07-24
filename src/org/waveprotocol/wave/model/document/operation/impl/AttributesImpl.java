/**
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.waveprotocol.wave.model.document.operation.impl;

import java.util.List;
import java.util.Map;

import org.waveprotocol.wave.model.document.operation.Attributes;
import org.waveprotocol.wave.model.document.operation.AttributesUpdate;
import org.waveprotocol.wave.model.document.operation.util.ImmutableStateMap;

public class AttributesImpl
    extends ImmutableStateMap<AttributesImpl, AttributesUpdate>
    implements Attributes {

  public static final AttributesImpl EMPTY_MAP = new AttributesImpl();

  public AttributesImpl() {
    super();
  }
  public AttributesImpl(Map<String, String> map) {
    super(map);
  }

  AttributesImpl(List<Attribute> attributes) {
    super(attributes);
  }

  @Override
  protected AttributesImpl createFromList(List<Attribute> attributes) {
    return new AttributesImpl(attributes);
  }
  public AttributesImpl(String... pairs) {
    super(pairs);
  }

}
