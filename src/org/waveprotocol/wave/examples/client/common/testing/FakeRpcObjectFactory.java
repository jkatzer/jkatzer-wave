/**
 * Copyright (C) 2010 Google Inc.
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

package org.waveprotocol.wave.examples.client.common.testing;

import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

import org.waveprotocol.wave.examples.client.common.ClientBackend;
import org.waveprotocol.wave.examples.fedone.rpc.ClientRpcChannel;
import org.waveprotocol.wave.examples.fedone.rpc.testing.FakeRpcController;
import org.waveprotocol.wave.examples.fedone.frontend.testing.FakeWaveServer;
import org.waveprotocol.wave.examples.fedone.frontend.WaveClientRpcImpl;
import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.ProtocolWaveClientRpc;

import java.io.IOException;

/**
 * A factory of fake RPC objects for the client backend.
 *
 * @author mk.mateng@gmail.com (Michael Kuntzman)
 */
public class FakeRpcObjectFactory implements ClientBackend.RpcObjectFactory {
  /**
   * A {@code ClientRpcChannel} that only returns fake RPC controllers.
   */
  private static class FakeClientRpcChannel implements ClientRpcChannel {
    @Override
    public RpcController newRpcController() {
      return new FakeRpcController();
    }

    @Override
    public void callMethod(MethodDescriptor method, RpcController genericRpcController,
        Message request, Message responsePrototype, RpcCallback<Message> callback) {
    }
  }

  /**
   * @return a fake {@code ClientRpcChannel} implementation.
   */
  @Override
  public ClientRpcChannel createClientChannel(String server, int port) throws IOException {
    return new FakeClientRpcChannel();
  }

  /**
   * @return a {@code WaveClientRpcImpl} backed by a {@code FakeWaveServer}.
   */
  @Override
  public ProtocolWaveClientRpc.Interface createServerInterface(ClientRpcChannel channel) {
    return new WaveClientRpcImpl(new FakeWaveServer());
  }
}
