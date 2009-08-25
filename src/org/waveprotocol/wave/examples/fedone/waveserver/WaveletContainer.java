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

import org.waveprotocol.wave.examples.fedone.common.HashedVersion;
import org.waveprotocol.wave.model.wave.ParticipantId;
import org.waveprotocol.wave.model.wave.data.WaveletData;
import org.waveprotocol.wave.protocol.common.ProtocolAppliedWaveletDelta;
import org.waveprotocol.wave.protocol.common.ProtocolHashedVersion;
import org.waveprotocol.wave.protocol.common.ProtocolWaveletDelta;

import java.util.List;
import java.util.NavigableSet;

/**
 * Interface for a container class for a Wavelet's current state as well as its
 * delta history. Local and remote wavelet interfaces inherit from this one.
 *
 *
 */
abstract interface WaveletContainer {
  enum State {
    /** Everything is working fine. */
    OK,
    /** Wavelet's history is not yet available. */
    LOADING,
    /** Wavelet has been deleted, the instance will not contain any data. */
    DELETED,
    /** For some reason this instance is broken, e.g. a remote wavelet update signature failed. */
    CORRUPTED
  }

  State getState();

  void setState(State state);

  WaveletData getWaveletData();

  // TODO: decide which to go for depending on common.proto modification. ###
  /**
   * Retrieve the wavlet history of deltas applied to the wavelet.
   *
   * @param versionStart start version (inclusive), minimum 0.
   * @param versionEnd end version (exclusive).
   * @throws WaveletStateException if the wavelet is in a state unsuitable for retrieving history.
   * @return canonical {@code ProtocolAppliedWaveletDelta}s in the range as requested. Note that
   *         if a delta straddles one of the requested version boundaries, it will be included.
   */
  NavigableSet<ByteStringMessage<ProtocolAppliedWaveletDelta>> requestHistory(
      ProtocolHashedVersion versionStart, ProtocolHashedVersion versionEnd)
      throws WaveletStateException;

  /**
   * Retrieve the wavlet history of deltas applied to the wavelet, with additional
   * safety check that
   *
   * @param versionStart start version (inclusive), minimum 0.
   * @param versionEnd end version (exclusive).
   * @throws AccessControlException if the hashedVersion does not match that of the wavelet history.
   * @throws WaveletStateException if the wavelet is in a state unsuitable for retrieving history.
   * @return deltas in the range as requested, or null if there was an error. Note that
   *         if a delta straddles one of the requested version boundaries, it will be included.
   */
  NavigableSet<ProtocolWaveletDelta> requestTransformedHistory(ProtocolHashedVersion versionStart,
      ProtocolHashedVersion versionEnd) throws AccessControlException, WaveletStateException;

  /**
   * @param participantId id of participant attempting to gain access to wavelet.
   * @throws WaveletStateException if the wavelet is in a state unsuitable for checking permissions.
   * @return true if the participant is a participant on the wavelet.
   */
  boolean checkAccessPermission(ParticipantId participantId) throws WaveletStateException;

  /**
   * The Last Committed Version returns when the local or remote wave server committed the wavelet.
   * @throws WaveletStateException if the wavelet is in a state unsuitable for getting LCV.
   */
  ProtocolHashedVersion getLastCommittedVersion() throws WaveletStateException;

  /** A list of participants currently on the wave */
  List<ParticipantId> getParticipants();

  /** @return the current version of the wavelet. */
  HashedVersion getCurrentVersion();
}