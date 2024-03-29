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

package org.waveprotocol.wave.examples.fedone.util.testing;

import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.id.WaveletId;
import org.waveprotocol.wave.model.id.WaveletName;
import org.waveprotocol.wave.model.wave.ParticipantId;

/**
 * Commonly used constants for unit testing. Some constants taken from
 * previously existing test cases.
 *
 * @author mk.mateng@gmail.com (Michael Kuntzman)
 */
// TODO(Michael): Maybe move this class to the libraries repository/branch.
public interface TestingConstants {
  public static final String BLIP_ID = "b+blip";

  public static final String MESSAGE = "The quick brown fox jumps over the lazy dog";

  public static final String MESSAGE2 = "Why's the rum gone?";

  public static final String MESSAGE3 = "There is no spoon";

  public static final String DOMAIN = "host.com";

  public static final String OTHER_USER = "other@" + DOMAIN;

  public static final ParticipantId OTHER_PARTICIPANT = new ParticipantId(OTHER_USER);

  public static final int PORT = 9876;

  /**
   * Timeout, in milliseconds, for tests that may fail through abnormal
   * behaviors such as deadocks or infinite loops. Usually 1000-2000 ms should
   * be enough. We give a little more to be safe.
   */
  public static final long TEST_TIMEOUT = 5000;

  public static final String USER = "user@" + DOMAIN;

  public static final ParticipantId PARTICIPANT = new ParticipantId(USER);

  public static final WaveId WAVE_ID = new WaveId(DOMAIN, "w+wave");

  public static final WaveletId WAVELET_ID = new WaveletId(DOMAIN, "wavelet");

  public static final WaveletName WAVELET_NAME = WaveletName.of(WAVE_ID, WAVELET_ID);
}