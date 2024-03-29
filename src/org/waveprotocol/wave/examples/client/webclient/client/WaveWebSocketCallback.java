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

package org.waveprotocol.wave.examples.client.webclient.client;

import com.google.gwt.core.client.JavaScriptObject;
import org.waveprotocol.wave.examples.fedone.waveserver.ProtocolSubmitResponse;
import org.waveprotocol.wave.examples.fedone.waveserver.ProtocolWaveletUpdate;

/**
 * Created by IntelliJ IDEA. User: arb Date: May 13, 2010 Time: 12:14:18 PM To change this template
 * use File | Settings | File Templates.
 */
interface WaveWebSocketCallback {

  void connected();

  void disconnected();

  void handleWaveletUpdate(ProtocolWaveletUpdate message);

  void receiveSubmitResponse(ProtocolSubmitResponse message, int sequenceNo);

  void unknown();
}
