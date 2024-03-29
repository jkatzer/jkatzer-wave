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

package org.waveprotocol.wave.examples.fedone.waveserver;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import org.waveprotocol.wave.examples.fedone.util.EmptyDeltaException;
import org.waveprotocol.wave.federation.Proto.ProtocolHashedVersion;
import org.waveprotocol.wave.federation.Proto.ProtocolSignedDelta;
import org.waveprotocol.wave.model.id.WaveletName;
import org.waveprotocol.wave.model.operation.OperationException;

/**
 * A local wavelet may be updated by submits. The local wavelet will perform
 * operational transformation on the submitted delta and assign it the latest
 * version of the wavelet.
 *
 *
 */
interface LocalWaveletContainer extends WaveletContainer {

  interface Factory {
    /** @throws IllegalArgumentException if the waveletName is bad */
    LocalWaveletContainer create(WaveletName waveletName);
  }

  /**
   * Request that a given delta is submitted to the wavelet.
   *
   * @param waveletName name of wavelet.
   * @param delta to be submitted to the server.
   * @return result of application to the wavelet, both the applied result and the transformed
   *         result.
   * @throws OperationException
   * @throws InvalidProtocolBufferException
   * @throws InvalidHashException
   */
  public DeltaApplicationResult submitRequest(WaveletName waveletName, ProtocolSignedDelta delta)
      throws OperationException, WaveletStateException, InvalidProtocolBufferException,
      InvalidHashException, EmptyDeltaException;

  /**
   * Check whether a submitted delta (identified by its hashed version after application) was
   * signed by a given signer. This (by design) should additionally validate that the history hash
   * is valid for the delta.
   *
   * @param hashedVersion to check whether in the history of the delta
   * @param signerId of the signer
   */
  boolean isDeltaSigner(ProtocolHashedVersion hashedVersion, ByteString signerId);
}
