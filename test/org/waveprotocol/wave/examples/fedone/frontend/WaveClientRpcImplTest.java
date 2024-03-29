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

package org.waveprotocol.wave.examples.fedone.frontend;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

import junit.framework.TestCase;

import org.waveprotocol.wave.examples.fedone.common.CoreWaveletOperationSerializer;
import org.waveprotocol.wave.examples.fedone.common.HashedVersion;
import org.waveprotocol.wave.examples.fedone.frontend.testing.FakeClientFrontend;
import org.waveprotocol.wave.examples.fedone.rpc.testing.FakeRpcController;
import org.waveprotocol.wave.examples.fedone.util.URLEncoderDecoderBasedPercentEncoderDecoder;
import org.waveprotocol.wave.examples.fedone.util.WaveletDataUtil;
import org.waveprotocol.wave.examples.fedone.util.testing.TestingConstants;
import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.ProtocolOpenRequest;
import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.ProtocolSubmitRequest;
import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.ProtocolSubmitResponse;
import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.ProtocolWaveletUpdate;
import org.waveprotocol.wave.federation.Proto.ProtocolHashedVersion;
import org.waveprotocol.wave.federation.Proto.ProtocolWaveletDelta;
import org.waveprotocol.wave.federation.Proto.ProtocolWaveletOperation;
import org.waveprotocol.wave.model.id.IdURIEncoderDecoder;
import org.waveprotocol.wave.model.id.URIEncoderDecoder.EncodingException;
import org.waveprotocol.wave.model.id.WaveletName;
import org.waveprotocol.wave.model.wave.data.WaveletData;

/**
 * Tests for the {@link WaveClientRpcImpl}.
 */
public class WaveClientRpcImplTest extends TestCase implements TestingConstants {

  private static final String FAIL_MESSAGE = "Failed";

  private static final ProtocolHashedVersion HASHED_VERSION =
    CoreWaveletOperationSerializer.serialize(HashedVersion.unsigned(101L));

  private static final ProtocolWaveletDelta DELTA = ProtocolWaveletDelta.newBuilder()
    .setAuthor(USER)
    .setHashedVersion(HASHED_VERSION)
    .addOperation(ProtocolWaveletOperation.newBuilder().build()).build();

  private static final ImmutableList<ProtocolWaveletDelta> DELTAS = ImmutableList.of(DELTA);

  private static final ProtocolHashedVersion RESULTING_VERSION =
    CoreWaveletOperationSerializer.serialize(HashedVersion.unsigned(102L));

  private RpcController controller;

  private int counter = 0;

  private FakeClientFrontend frontend;

  private WaveClientRpcImpl rpcImpl;

  private final IdURIEncoderDecoder uriCodec = new IdURIEncoderDecoder(
      new URLEncoderDecoderBasedPercentEncoderDecoder());

  private WaveletName getWaveletName(String waveletName) {
    try {
      return uriCodec.uriToWaveletName(waveletName);
    } catch (EncodingException e) {
      return null;
    }
  }

  private String getWaveletUri(WaveletName waveletName) {
    try {
      return uriCodec.waveletNameToURI(waveletName);
    } catch (EncodingException e) {
      return null;
    }
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    counter = 0;
    controller = new FakeRpcController();
    frontend = new FakeClientFrontend();
    rpcImpl = new WaveClientRpcImpl(frontend);
  }

  // TODO(arb): test with channelIds.

  /**
   * Tests that an open results in a proper wavelet commit update.
   */
  public void testOpenCommit() {
    ProtocolOpenRequest request = ProtocolOpenRequest.newBuilder()
        .setParticipantId(USER)
        .setWaveId(WAVE_ID.serialise()).build();
    counter = 0;
    rpcImpl.open(controller, request, new RpcCallback<ProtocolWaveletUpdate>() {
      @Override
      public void run(ProtocolWaveletUpdate update) {
        ++counter;
        assertEquals(WAVELET_NAME, getWaveletName(update.getWaveletName()));
        assertTrue(update.hasCommitNotice());
        assertEquals(HASHED_VERSION, update.getCommitNotice());
      }
    });
    frontend.waveletCommitted(WAVELET_NAME, HASHED_VERSION);
    assertEquals(1, counter);
    assertFalse(controller.failed());
  }

  /**
   * Tests that an open failure results in a proper wavelet failure update.
   */
  public void testOpenFailure() {
    ProtocolOpenRequest request = ProtocolOpenRequest.newBuilder()
        .setParticipantId(USER)
        .setWaveId(WAVE_ID.serialise()).build();
    counter = 0;
    rpcImpl.open(controller, request, new RpcCallback<ProtocolWaveletUpdate>() {
      @Override
      public void run(ProtocolWaveletUpdate update) {
        ++counter;
      }
    });
    frontend.doUpdateFailure(WAVE_ID, FAIL_MESSAGE);
    assertEquals(0, counter);
    assertTrue(controller.failed());
    assertEquals(FAIL_MESSAGE, controller.errorText());
  }

  /**
   * Tests that an open results in a proper wavelet update.
   */
  public void testOpenUpdate() {
    ProtocolOpenRequest request = ProtocolOpenRequest.newBuilder()
        .setParticipantId(USER)
        .setWaveId(WAVE_ID.serialise()).build();
    counter = 0;
    rpcImpl.open(controller, request, new RpcCallback<ProtocolWaveletUpdate>() {
      @Override
      public void run(ProtocolWaveletUpdate update) {
        ++counter;
        assertEquals(WAVELET_NAME, getWaveletName(update.getWaveletName()));
        assertEquals(DELTAS.size(), update.getAppliedDeltaCount());
        for (int i = 0; i < update.getAppliedDeltaCount(); ++i) {
          assertEquals(DELTAS.get(i), update.getAppliedDelta(i));
        }
        assertFalse(update.hasCommitNotice());
      }
    });
    long dummyCreationTime = System.currentTimeMillis();
    WaveletData wavelet =
        WaveletDataUtil.createEmptyWavelet(WAVELET_NAME, PARTICIPANT, dummyCreationTime);
    frontend.waveletUpdate(wavelet, RESULTING_VERSION, DELTAS);
    assertEquals(1, counter);
    assertFalse(controller.failed());
  }

  /**
   * Tests that a failed submit results in the proper submit failure response.
   */
  public void testSubmitFailed() {
    ProtocolSubmitRequest request = ProtocolSubmitRequest.newBuilder()
      .setDelta(DELTA)
      .setWaveletName(getWaveletUri(WAVELET_NAME)).build();
    counter = 0;
    rpcImpl.submit(controller, request, new RpcCallback<ProtocolSubmitResponse>() {
      @Override
      public void run(ProtocolSubmitResponse response) {
        ++counter;
        assertEquals(0, response.getOperationsApplied());
        assertEquals(FAIL_MESSAGE, response.getErrorMessage());
      }
    });
    frontend.doSubmitFailed(WAVELET_NAME, FAIL_MESSAGE);
    assertEquals(1, counter);
    assertFalse(controller.failed());
  }

  /**
   * Tests that a successful submit results in the proper submit response.
   */
  public void testSubmitSuccess() {
    ProtocolSubmitRequest request = ProtocolSubmitRequest.newBuilder()
      .setDelta(DELTA)
      .setWaveletName(getWaveletUri(WAVELET_NAME)).build();
    counter = 0;
    rpcImpl.submit(controller, request, new RpcCallback<ProtocolSubmitResponse>() {
      @Override
      public void run(ProtocolSubmitResponse response) {
        ++counter;
        assertEquals(1, response.getOperationsApplied());
        assertFalse(response.hasErrorMessage());
      }
    });
    frontend.doSubmitSuccess(WAVELET_NAME);
    assertEquals(1, counter);
    assertFalse(controller.failed());
  }

  /**
   * Tests that a bad wave id request is gracefully handled.
   */
  public void testOpenEncodingError() {
    ProtocolOpenRequest request = ProtocolOpenRequest.newBuilder()
        .setParticipantId(USER)
        .setWaveId("badwaveid").build();
    counter = 0;
    try {
      rpcImpl.open(controller, request, new RpcCallback<ProtocolWaveletUpdate>() {
        @Override
        public void run(ProtocolWaveletUpdate update) {
          ++counter;
        }
      });
    } catch (IllegalArgumentException e) {
      controller.setFailed(FAIL_MESSAGE);
    }
    assertEquals(0, counter);
    assertTrue(controller.failed());
    assertFalse(controller.errorText().isEmpty());
  }

  /**
   * Tests that a bad wavelet name submit is gracefully handled.
   */
  public void testSubmitEncodingError() {
    ProtocolSubmitRequest request = ProtocolSubmitRequest.newBuilder()
      .setDelta(DELTA)
      .setWaveletName("badwaveletname").build();
    counter = 0;
    try {
      rpcImpl.submit(controller, request, new RpcCallback<ProtocolSubmitResponse>() {
        @Override
        public void run(ProtocolSubmitResponse response) {
          ++counter;
          assertTrue(response.hasErrorMessage());
        }
      });
    } catch (IllegalArgumentException e) {
      controller.setFailed(FAIL_MESSAGE);
    }
    assertEquals(0, counter);
    assertTrue(controller.failed());
    assertFalse(controller.errorText().isEmpty());
  }
}
