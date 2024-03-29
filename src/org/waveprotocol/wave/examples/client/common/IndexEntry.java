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

package org.waveprotocol.wave.examples.client.common;

import com.google.common.annotations.GwtCompatible;

import org.waveprotocol.wave.model.id.WaveId;

/**
 * Information about a wave derived from the index wave's reference to it.
 *
 *
 */
@GwtCompatible
public class IndexEntry {
  private final WaveId waveId;
  private final String digest;

  /**
   * Create a new index entry for a wave, containing its wave id and digest.
   *
   * @param waveId
   * @param digest
   */
  public IndexEntry(WaveId waveId, String digest) {
    this.waveId = waveId;
    this.digest = digest;
  }

  /**
   * @return the wave id referenced by this index entry
   */
  public WaveId getWaveId() {
    return waveId;
  }

  /**
   * @return the digest of the wave referenced by this index entry
   */
  public String getDigest() {
    return digest;
  }
}
