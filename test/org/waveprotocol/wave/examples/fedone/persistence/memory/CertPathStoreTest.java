/**
 * Copyright 2010 Google Inc.
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

package org.waveprotocol.wave.examples.fedone.persistence.memory;

import org.waveprotocol.wave.crypto.CertPathStore;
import org.waveprotocol.wave.examples.fedone.persistence.CertPathStoreTestBase;

/**
 * Unittest for the {@link CertPathStore} implementation in {@link MemoryStore}.
 *
 * @author ljvderijk@google.com (Lennard de Rijk)
 *
 */
public class CertPathStoreTest extends CertPathStoreTestBase {

  private MemoryStore memoryStore;

  public CertPathStoreTest() throws Exception {
    super();
  }

  @Override
  protected CertPathStore newCertPathStore() {
    this.memoryStore = new MemoryStore();
    return memoryStore;
  }
}
