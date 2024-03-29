/**
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.waveprotocol.wave.examples.fedone.rpc;

import com.google.common.collect.Lists;
import com.google.protobuf.Descriptors;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

import junit.framework.TestCase;

import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc;
import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.ProtocolOpenRequest;
import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.ProtocolSubmitRequest;
import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.ProtocolSubmitResponse;
import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.ProtocolWaveletUpdate;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test case for ClientRpcChannelImpl and ServerRpcProvider.
 * 
 *
 */
public class RpcTest extends TestCase {

  protected ServerRpcProvider server = null;
  protected ClientRpcChannel client = null;

  protected ClientRpcChannel newClient() throws IOException {
     return new ClientRpcChannelImpl(server.getBoundAddress());
  }

  protected void startServer() throws IOException {
    server = new ServerRpcProvider(null, null, null, null);
    server.startRpcServer();
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    startServer();
  }

  @Override
  public void tearDown() throws Exception {
    server.stopServer();
    server = null;
    client = null;
    super.tearDown();
  }

  /**
   * Asserts that the streaming RPC option is being parsed correctly.
   */
  public void testIsStreamingRpc() throws Exception {    
    Descriptors.ServiceDescriptor serviceDescriptor =
        WaveClientRpc.ProtocolWaveClientRpc.getDescriptor();
    assertTrue(serviceDescriptor.findMethodByName("Open").getOptions()
        .getExtension(Rpc.isStreamingRpc));
    assertFalse(serviceDescriptor.findMethodByName("Submit").getOptions()
        .getExtension(Rpc.isStreamingRpc));
  }
  
  /**
   * Tests a complete, simple end-to-end RPC.
   */
  public void testSimpleRpc() throws Exception {
    final int TIMEOUT_SECONDS = 5;
    final String USER = "thorogood@google.com";
    final String WAVE = "foowave";
    final AtomicBoolean receivedOpenRequest = new AtomicBoolean(false);
    final CountDownLatch responseLatch = new CountDownLatch(2);
    final List<ProtocolWaveletUpdate> responses = Lists.newArrayList();
    final ProtocolWaveletUpdate cannedResponse =
        ProtocolWaveletUpdate.newBuilder().setWaveletName(WAVE).build();

    // Generate fairly dummy RPC implementation.
    WaveClientRpc.ProtocolWaveClientRpc.Interface rpcImpl =
        new WaveClientRpc.ProtocolWaveClientRpc.Interface() {
          @Override
          public void open(RpcController controller, ProtocolOpenRequest request,
              RpcCallback<ProtocolWaveletUpdate> callback) {
            assertTrue(receivedOpenRequest.compareAndSet(false, true));
            assertEquals(USER, request.getParticipantId());
            assertEquals(WAVE, request.getWaveId());

            // Return a valid response.
            callback.run(cannedResponse);

            // Falling out of this method will automatically finish this RPC.
            callback.run(null);
            // TODO: terrible idea?
          }

          @Override
          public void submit(RpcController controller, ProtocolSubmitRequest request,
              RpcCallback<ProtocolSubmitResponse> callback) {
            throw new UnsupportedOperationException();
          }
        };

    // Register the RPC implementation with the ServerRpcProvider.
    server.registerService(WaveClientRpc.ProtocolWaveClientRpc.newReflectiveService(rpcImpl));

    // Create a client connection to the server, *after* it has registered services.
    client = newClient();

    // Create a client-side stub for talking to the server.
    WaveClientRpc.ProtocolWaveClientRpc.Stub stub =
        WaveClientRpc.ProtocolWaveClientRpc.newStub(client);

    // Create a controller, set up request, wait for responses.
    RpcController controller = client.newRpcController();
    ProtocolOpenRequest request =
        ProtocolOpenRequest.newBuilder().setParticipantId(USER).setWaveId(WAVE).build();
    stub.open(controller, request, new RpcCallback<ProtocolWaveletUpdate>() {
      @Override
      public void run(ProtocolWaveletUpdate response) {
        responses.add(response);
        responseLatch.countDown();
      }
    });
    
    // Wait for both responses to be received and assert their equality.
    responseLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    assertEquals(Arrays.asList(cannedResponse, null), responses);
    assertEquals(0, responseLatch.getCount());
  }

  /**
   * Tests a RPC that will fail.
   */
  public void testFailedRpc() throws Exception {
    final int TIMEOUT_SECONDS = 5;
    final String ERROR_TEXT = "This error should flow down over the RPC connection!";
    final CountDownLatch responseLatch = new CountDownLatch(1);
    final List<ProtocolWaveletUpdate> responses = Lists.newArrayList();

    // Generate fairly dummy RPC implementation.
    WaveClientRpc.ProtocolWaveClientRpc.Interface rpcImpl =
        new WaveClientRpc.ProtocolWaveClientRpc.Interface() {
          @Override
          public void open(RpcController controller, ProtocolOpenRequest request,
              RpcCallback<ProtocolWaveletUpdate> callback) {
            controller.setFailed(ERROR_TEXT);
          }

          @Override
          public void submit(RpcController controller, ProtocolSubmitRequest request,
              RpcCallback<ProtocolSubmitResponse> callback) {
            throw new UnsupportedOperationException();
          }
        };

    // Register the RPC implementation with the ServerRpcProvider.
    server.registerService(WaveClientRpc.ProtocolWaveClientRpc.newReflectiveService(rpcImpl));

    // Create a client connection to the server, *after* it has registered services.
    client = newClient();

    // Create a client-side stub for talking to the server.
    WaveClientRpc.ProtocolWaveClientRpc.Stub stub =
        WaveClientRpc.ProtocolWaveClientRpc.newStub(client);

    // Create a controller, set up request, wait for responses.
    RpcController controller = client.newRpcController();
    ProtocolOpenRequest request =
        ProtocolOpenRequest.newBuilder().setParticipantId("").setWaveId("").build();
    stub.open(controller, request, new RpcCallback<ProtocolWaveletUpdate>() {
      @Override
      public void run(ProtocolWaveletUpdate response) {
        responses.add(response);
        responseLatch.countDown();
      }
    });

    // Wait for a response, and assert that is a complete failure. :-)
    responseLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    assertEquals(Arrays.asList((ProtocolWaveletUpdate) null), responses);
    assertTrue(controller.failed());
    assertEquals(ERROR_TEXT, controller.errorText());
  }

  /**
   * Tests cancelling a streaming RPC. This is achieved by waiting for the first
   * streaming message, then cancelling the RPC.
   */
  public void testCancelStreamingRpc() throws Exception {
    final int TIMEOUT_SECONDS = 5;
    final int MESSAGES_BEFORE_CANCEL = 5;
    final ProtocolWaveletUpdate cannedResponse =
      ProtocolWaveletUpdate.newBuilder().setWaveletName("").build();
    final CountDownLatch responseLatch = new CountDownLatch(MESSAGES_BEFORE_CANCEL);
    final CountDownLatch finishedLatch = new CountDownLatch(1);

    // Generate fairly dummy RPC implementation.
    WaveClientRpc.ProtocolWaveClientRpc.Interface rpcImpl =
        new WaveClientRpc.ProtocolWaveClientRpc.Interface() {
          @Override
          public void open(RpcController controller, ProtocolOpenRequest request,
              final RpcCallback<ProtocolWaveletUpdate> callback) {
            // Initially return many responses.
            for (int m = 0; m < MESSAGES_BEFORE_CANCEL; ++m) {
              callback.run(cannedResponse);
            }
              
            // Register a callback to handle cancellation. There is no race
            // condition here with sending responses, since there are no
            // contracts on the timing/response to cancellation requests.
            controller.notifyOnCancel(new RpcCallback<Object>() {
              @Override
              public void run(Object object) {
                // Happily shut down this RPC.
                callback.run(null);
              }
            });

          }

          @Override
          public void submit(RpcController controller, ProtocolSubmitRequest request,
              RpcCallback<ProtocolSubmitResponse> callback) {
            throw new UnsupportedOperationException();
          }
        };
  
    // Register the RPC implementation with the ServerRpcProvider.
    server.registerService(WaveClientRpc.ProtocolWaveClientRpc.newReflectiveService(rpcImpl));

    // Create a client connection to the server, *after* it has registered
    // services.
    client = newClient();

    // Create a client-side stub for talking to the server.
    WaveClientRpc.ProtocolWaveClientRpc.Stub stub =
        WaveClientRpc.ProtocolWaveClientRpc.newStub(client);

    // Create a controller, set up request, wait for responses.
    RpcController controller = client.newRpcController();
    ProtocolOpenRequest request =
        ProtocolOpenRequest.newBuilder().setParticipantId("").setWaveId("").build();
    stub.open(controller, request, new RpcCallback<ProtocolWaveletUpdate>() {
      @Override
      public void run(ProtocolWaveletUpdate response) {
        if (response != null) {
          responseLatch.countDown();
        } else {
          finishedLatch.countDown();
        }
      }
    });

    // Wait for all pending responses.
    responseLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    assertEquals(0, responseLatch.getCount());
    assertEquals(1, finishedLatch.getCount());

    // Cancel the RPC and wait for it to finish.
    controller.startCancel();
    finishedLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    assertEquals(0, finishedLatch.getCount());
    assertFalse(controller.failed());
  }

}
