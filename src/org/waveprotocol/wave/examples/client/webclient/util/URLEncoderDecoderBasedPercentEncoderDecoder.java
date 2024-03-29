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

package org.waveprotocol.wave.examples.client.webclient.util;

import org.waveprotocol.wave.model.id.URIEncoderDecoder;
import com.google.gwt.http.client.URL;


/**
 * Uses standard java URLEncoder and URLDecoder to percent encode.
 *
 * GWT version.
 *
 *
 */
public class URLEncoderDecoderBasedPercentEncoderDecoder
    implements URIEncoderDecoder.PercentEncoderDecoder {
  @Override
  public String decode(String encodedValue) throws URIEncoderDecoder.EncodingException {
      // The URL decoder will replace + with space so percent escape it.
      encodedValue = encodedValue.replace("+", "%2B");

      String ret = URL.decodeComponent(encodedValue);
      if (ret.indexOf(0xFFFD) != -1) {
        throw new URIEncoderDecoder.EncodingException(
            "Unable to decode value " + encodedValue + " it contains invalid UTF-8");
      }
      return ret;

  }

  @Override
  public String encode(String decodedValue) throws URIEncoderDecoder.EncodingException {
      return URL.encode(decodedValue);
  }
}
