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

import com.google.gwt.user.client.Window;

import org.waveprotocol.wave.examples.fedone.common.SessionConstants;

/**
 * Session data for the web client.
 *
 * @author kalman@google.com (Benjamin Kalman)
 */
public abstract class Session implements SessionConstants {

  private static final Session INSTANCE;

  static {
    // In the future we could inject other Session instances for testing.
    if (JsSession.isAvailable()) {
      INSTANCE = new JsSession();
    } else {
      Window.alert("Warning: Session data not available.");
      INSTANCE = new StubSession();
    }
  }

  private Session() {
  }

  /**
   * @return the singleton {@link Session} instance
   */
  public static Session get() {
    return INSTANCE;
  }

  /**
   * @return the domain the wave server serves waves for
   */
  public abstract String getDomain();

  /**
   * A {@link Session} which gets its data from the __session JS variable.
   */
  private static final class JsSession extends Session {
    /**
     * @return whether it is possible to have a JsSession
     */
    public static native boolean isAvailable() /*-{
      return typeof($wnd.__session) !== "undefined";
    }-*/;

    @Override
    public String getDomain() {
      return getFieldAsString(DOMAIN);
    }

    private native String getFieldAsString(String s) /*-{
      return $wnd.__session[s];
    }-*/;
  }

  /**
   * A {@link Session} which returns stub values.
   */
  private static final class StubSession extends Session {
    @Override
    public String getDomain() {
      return DOMAIN;
    }
  }
}
