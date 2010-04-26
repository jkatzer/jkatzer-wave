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

package org.waveprotocol.wave.crypto;

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * A cert path (aka cert chain) validator interface.  Not specifically for wave, but the class name
 * {@code CertPathValidator} was already taken.
 */
public interface WaveCertPathValidator {
  /**
   * Validates a certificate chain. The first certificate in the chain is the certificate for the
   * key used for signing. The last certificate in the chain is either a trusted CA certificate, or
   * a certificate issued by a trusted CA. Certificate N in the chain must have been issued by
   * certificate N+1 in the chain.
   *
   * @param certs list of certificates
   * @throws SignatureException if the certificate chain doesn't validate.
   */
  void validate(List<? extends X509Certificate> certs) throws SignatureException;
}
