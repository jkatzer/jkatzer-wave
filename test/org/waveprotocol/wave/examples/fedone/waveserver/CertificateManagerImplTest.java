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

import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.easymock.classextension.EasyMock.createStrictMock;
import static org.waveprotocol.wave.examples.fedone.waveserver.Ticker.EASY_TICKS;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import junit.framework.TestCase;

import org.apache.commons.codec.binary.Base64;
import org.waveprotocol.wave.crypto.CachedCertPathValidator;
import org.waveprotocol.wave.crypto.CertPathStore;
import org.waveprotocol.wave.crypto.DefaultCacheImpl;
import org.waveprotocol.wave.crypto.DefaultCertPathStore;
import org.waveprotocol.wave.crypto.DefaultTrustRootsProvider;
import org.waveprotocol.wave.crypto.DisabledCertPathValidator;
import org.waveprotocol.wave.crypto.SignatureException;
import org.waveprotocol.wave.crypto.SignerInfo;
import org.waveprotocol.wave.crypto.TimeSource;
import org.waveprotocol.wave.crypto.TrustRootsProvider;
import org.waveprotocol.wave.crypto.UnknownSignerException;
import org.waveprotocol.wave.crypto.VerifiedCertChainCache;
import org.waveprotocol.wave.crypto.WaveCertPathValidator;
import org.waveprotocol.wave.crypto.WaveSignatureVerifier;
import org.waveprotocol.wave.crypto.WaveSigner;
import org.waveprotocol.wave.crypto.WaveSignerFactory;
import org.waveprotocol.wave.examples.fedone.common.HashedVersion;
import org.waveprotocol.wave.examples.fedone.common.WaveletOperationSerializer;
import org.waveprotocol.wave.examples.fedone.waveserver.CertificateManager.SignerInfoPrefetchResultListener;
import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.id.WaveletId;
import org.waveprotocol.wave.model.id.WaveletName;
import org.waveprotocol.wave.protocol.common.ProtocolHashedVersion;
import org.waveprotocol.wave.protocol.common.ProtocolSignature;
import org.waveprotocol.wave.protocol.common.ProtocolSignedDelta;
import org.waveprotocol.wave.protocol.common.ProtocolSignerInfo;
import org.waveprotocol.wave.protocol.common.ProtocolWaveletDelta;
import org.waveprotocol.wave.protocol.common.ProtocolSignature.SignatureAlgorithm;
import org.waveprotocol.wave.protocol.common.ProtocolSignerInfo.HashAlgorithm;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CertificateManagerImplTest extends TestCase {

  private static final String PRIVATE_KEY =
    "-----BEGIN PRIVATE KEY-----\n" +
    "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKueRG+YuGX6Fifk\n" +
    "JpYR+Gh/qF+PpGLSYVR7CzhGNh5a8RayKwPM8YNqsfKAT8VqLdAk19x//cf03Cgc\n" +
    "UwLQsuUo3zxK4E110L96lVX6oF12FiIpSCVN+E93qin2W7VXw2JtfvQ4BllwdNMj\n" +
    "/yNPl+bHuhtOjFAPpWEhCkSJP6NlAgMBAAECgYAaRocP1wAUjO+rd+D4hRPVXAY5\n" +
    "a1Kt1qwUNSqImSdcCmxzHyA62rv3dPR9vmt4PEN7ZMiv9+CxJqo2ce+7tJxO/Xq1\n" +
    "lPTh8IVX+NUPI8LWtek9VZlXZ16nY5qXZ0i32vrwOz+GaZMfchAK05eTaiUJTN4P\n" +
    "T2Wskp6jnlDGZYeNmQJBANXMPa70jf2M6zHq0dKBg+4I3XZ1x59G0fUnho1Ck+Q5\n" +
    "ixo5GpFbbx2YgQmbFNUHhMNAJvLTduV5S3+CopqB3FMCQQDNfpUYQrmrAOvAZiQ0\n" +
    "uX/BtorjvSoTkj4g2JegaGWUVAc8As9d3VrBf8l2ovJRuzVSGqHpzke7T8wGwaGr\n" +
    "cEpnAkBFz+N0dbbHzHQgYKUTL+d8mrh2Lg95Gw8EFlwBVHQmWgPqFCtwu4KVD29T\n" +
    "S6iJx2K6vv/42sRAOlNE18tw2GaxAkBAKakGBTeR5Fy4G2xspgr1AjlFuLfdmokZ\n" +
    "mmdlp5MoCECmBT6YUVhYGL1f9KryyCBy/WvW5BjTrKvI5EbFj+87AkAobTHhq+D7\n" +
    "TOQBpaA5v45z6HNsFdCovQkQokJbirQ0KDIopo5IT7Qtz7+Gi3S0uYl3xooAsCRc\n" +
    "Zj50nIvr3txX\n" +
    "-----END PRIVATE KEY-----\n";

  private static final String CERTIFICATE =
    "-----BEGIN CERTIFICATE-----\n" +
    "MIIC9TCCAl6gAwIBAgIJALQVfb0zIz6bMA0GCSqGSIb3DQEBBQUAMFsxCzAJBgNV\n" +
    "BAYTAlVTMRMwEQYDVQQIEwpTb21lLVN0YXRlMSEwHwYDVQQKExhJbnRlcm5ldCBX\n" +
    "aWRnaXRzIFB0eSBMdGQxFDASBgNVBAMTC2V4YW1wbGUuY29tMB4XDTA5MDcxODA2\n" +
    "MjIyNloXDTEwMDcxODA2MjIyNlowWzELMAkGA1UEBhMCVVMxEzARBgNVBAgTClNv\n" +
    "bWUtU3RhdGUxITAfBgNVBAoTGEludGVybmV0IFdpZGdpdHMgUHR5IEx0ZDEUMBIG\n" +
    "A1UEAxMLZXhhbXBsZS5jb20wgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAKue\n" +
    "RG+YuGX6FifkJpYR+Gh/qF+PpGLSYVR7CzhGNh5a8RayKwPM8YNqsfKAT8VqLdAk\n" +
    "19x//cf03CgcUwLQsuUo3zxK4E110L96lVX6oF12FiIpSCVN+E93qin2W7VXw2Jt\n" +
    "fvQ4BllwdNMj/yNPl+bHuhtOjFAPpWEhCkSJP6NlAgMBAAGjgcAwgb0wHQYDVR0O\n" +
    "BBYEFD2DmpOW+OiFr6U3Nu7NuDGuBSJgMIGNBgNVHSMEgYUwgYKAFD2DmpOW+OiF\n" +
    "r6U3Nu7NuDGuBSJgoV+kXTBbMQswCQYDVQQGEwJVUzETMBEGA1UECBMKU29tZS1T\n" +
    "dGF0ZTEhMB8GA1UEChMYSW50ZXJuZXQgV2lkZ2l0cyBQdHkgTHRkMRQwEgYDVQQD\n" +
    "EwtleGFtcGxlLmNvbYIJALQVfb0zIz6bMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcN\n" +
    "AQEFBQADgYEAS7H+mB7lmEihX5lOWp9ZtyI7ua7MYVK05bbuBZJLAhO1mApu5Okg\n" +
    "DqcybVV8ijPLJkII75dn+q7olpwMmgyjjsozEKY1N0It9nRsb9fW2tKGp2qlCMA4\n" +
    "zP29U9091ZRH/xL1RPVzhkRHqfNJ/x+iTC4laSLBtwlsjjkd8Us6xrg=\n" +
    "-----END CERTIFICATE-----\n";

  private static final String DOMAIN = "example.com";
  private static final String OTHER_DOMAIN = "other.org";

  private static final byte[] REAL_SIGNATURE = Base64.decodeBase64((
    "aYfzuohSPaqbwn/Ro0bgklyoTwKAmsYl7efRlC684yGOXdbAm+bPm9KHVVYIeLjSHTR" +
    "M4ZB5rTkHIzh1B+/QHM8eO61AOp9WIP6kF7Vqnjm4KhcDbuUYPdV8qLPkjEjoDl1vCd" +
    "p4NMnfLyHS7MMsN4MGTaLNtFeLNK6AyAZrM8c=").getBytes());

  private static final String REAL_DOMAIN = "initech-corp.com";

  private static final String REAL_CERTIFICATE =
    "-----BEGIN CERTIFICATE-----\n" +
    "MIIHWzCCBkOgAwIBAgICCn0wDQYJKoZIhvcNAQEFBQAwgdgxCzAJBgNVBAYTAlVT\n" +
    "MREwDwYDVQQIDAhDb2xvcmFkbzEjMCEGA1UECgwaSmFiYmVyIFNvZnR3YXJlIEZv\n" +
    "dW5kYXRpb24xIzAhBgNVBAsMGlNlY3VyZSBDZXJ0aWZpY2F0ZSBTaWduaW5nMUYw\n" +
    "RAYDVQQDDD1TdGFydENvbSBDbGFzcyAxIEludGVybWVkaWF0ZSBDQSAtIEphYmJl\n" +
    "ciBTb2Z0d2FyZSBGb3VuZGF0aW9uMSQwIgYJKoZIhvcNAQkBFhVjZXJ0bWFzdGVy\n" +
    "QGphYmJlci5vcmcwHhcNMDkwODI4MTM0MDUyWhcNMTAwODI4MTM0MDUyWjCBoDEL\n" +
    "MAkGA1UEBhMCQVUxEzARBgNVBAgTClNvbWUtU3RhdGUxGTAXBgNVBAoTEGluaXRl\n" +
    "Y2gtY29ycC5jb20xHjAcBgNVBAsTFURvbWFpbiB2YWxpZGF0ZWQgb25seTEeMBwG\n" +
    "A1UEAxMVd2F2ZS5pbml0ZWNoLWNvcnAuY29tMSEwHwYJKoZIhvcNAQkBFhJiYWxm\n" +
    "YW56QGdvb2dsZS5jb20wgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAMsM6ZEW\n" +
    "hPCMVM8ji3jp/+bbUEFp4/A+8X/Ow3FUSIbOymE3buhS4uP4RGgMkc19ORfG5kLI\n" +
    "bX1O5AAXNFi9N3jTGJb7ahbacjpFqZUdmz/XvnlxA0u3gf0zEceQ8tpuYZ/8r0FS\n" +
    "5/w0/ZglRNknuE2eyuupClaFLPYW2h7HYBwhAgMBAAGjggPnMIID4zAMBgNVHRME\n" +
    "BTADAgEAMAsGA1UdDwQEAwIDqDATBgNVHSUEDDAKBggrBgEFBQcDATAdBgNVHQ4E\n" +
    "FgQUNzB8oOjW0uOI3VCOkHVXbwFNIU4wgd0GA1UdIwSB1TCB0oAUe47EZ9BGIRcR\n" +
    "/6F6QnWf6sSrcuShgbakgbMwgbAxCzAJBgNVBAYTAklMMQ8wDQYDVQQIEwZJc3Jh\n" +
    "ZWwxDjAMBgNVBAcTBUVpbGF0MRYwFAYDVQQKEw1TdGFydENvbSBMdGQuMRowGAYD\n" +
    "VQQLExFDQSBBdXRob3JpdHkgRGVwLjEpMCcGA1UEAxMgRnJlZSBTU0wgQ2VydGlm\n" +
    "aWNhdGlvbiBBdXRob3JpdHkxITAfBgkqhkiG9w0BCQEWEmFkbWluQHN0YXJ0Y29t\n" +
    "Lm9yZ4IBFDBXBgNVHREEUDBOoCMGCCsGAQUFBwgFoBcMFXdhdmUuaW5pdGVjaC1j\n" +
    "b3JwLmNvbYIQaW5pdGVjaC1jb3JwLmNvbYIVd2F2ZS5pbml0ZWNoLWNvcnAuY29t\n" +
    "MCAGA1UdEgQZMBeBFWNlcnRtYXN0ZXJAamFiYmVyLm9yZzBiBgNVHR8EWzBZMCug\n" +
    "KaAnhiVodHRwOi8vY2VydC5zdGFydGNvbS5vcmcveG1wcC1jcmwuY3JsMCqgKKAm\n" +
    "hiRodHRwOi8vY3JsLnN0YXJ0Y29tLm9yZy94bXBwLWNybC5jcmwwgYQGCCsGAQUF\n" +
    "BwEBBHgwdjA3BggrBgEFBQcwAYYraHR0cDovL29jc3Auc3RhcnRjb20ub3JnL3N1\n" +
    "Yi9jbGFzczEveG1wcC9jYTA7BggrBgEFBQcwAoYvaHR0cDovL2NlcnQuc3RhcnRj\n" +
    "b20ub3JnL3N1Yi5jbGFzczEueG1wcC5jYS5jcnQwggFKBgNVHSAEggFBMIIBPTCC\n" +
    "ATkGCysGAQQBgbU3AQEFMIIBKDA1BggrBgEFBQcCARYpaHR0cDovL2NlcnQuc3Rh\n" +
    "cnRjb20ub3JnL2ludGVybWVkaWF0ZS5wZGYwLwYIKwYBBQUHAgEWI2h0dHA6Ly9j\n" +
    "ZXJ0LnN0YXJ0Y29tLm9yZy9wb2xpY3kucGRmMIG9BggrBgEFBQcCAjCBsDAUFg1T\n" +
    "dGFydENvbSBMdGQuMAMCAQEagZdMaW1pdGVkIExpYWJpbGl0eSwgcmVhZCB0aGUg\n" +
    "c2VjdGlvbiAqTGVnYWwgTGltaXRhdGlvbnMqIG9mIHRoZSBTdGFydENvbSBDZXJ0\n" +
    "aWZpY2F0aW9uIEF1dGhvcml0eSBQb2xpY3kgYXZhaWxhYmxlIGF0IGh0dHA6Ly9j\n" +
    "ZXJ0LnN0YXJ0Y29tLm9yZy9wb2xpY3kucGRmMA0GCSqGSIb3DQEBBQUAA4IBAQB/\n" +
    "Xe2be9pVU1DMd407qiujql4b253kLOEEugkNjoV3epCZxT/44N2FJwwSrFhPpWdb\n" +
    "AYYxJY53cbB1yLvA4u3xvc2y1jh8uZMbP7sVsJWSzDTTIxCirtNqYXnOAa+tb1m6\n" +
    "wWveczrVWS3b8t/Tz2ozxd45n3T8yfUeI2PEPe4BcMUNNYvW7ROAxXTkxYnfE0Gf\n" +
    "9nL76KJVwM+RzHJirlzefJNNNDHkzegy53/kzsq/IzhS6ovsSEQdR2ue7a1sYmvZ\n" +
    "Hj8K5F7+S93u/P9iHsoGjU2j4IgAq6iCxEqDEsVBr1IMMZaLbbNZiSboq1ZYSLhV\n" +
    "jU2YSURXFt+84p/k5juk\n" +
    "-----END CERTIFICATE-----\n";

  private static final String STARTCOM_CERT =
    "-----BEGIN CERTIFICATE-----\n" +
    "MIIHADCCBmmgAwIBAgIBFDANBgkqhkiG9w0BAQUFADCBsDELMAkGA1UEBhMCSUwx\n" +
    "DzANBgNVBAgTBklzcmFlbDEOMAwGA1UEBxMFRWlsYXQxFjAUBgNVBAoTDVN0YXJ0\n" +
    "Q29tIEx0ZC4xGjAYBgNVBAsTEUNBIEF1dGhvcml0eSBEZXAuMSkwJwYDVQQDEyBG\n" +
    "cmVlIFNTTCBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTEhMB8GCSqGSIb3DQEJARYS\n" +
    "YWRtaW5Ac3RhcnRjb20ub3JnMB4XDTA2MTIwMjIzNTUyMVoXDTExMTIwMjIzNTUy\n" +
    "MVowgdgxCzAJBgNVBAYTAlVTMREwDwYDVQQIDAhDb2xvcmFkbzEjMCEGA1UECgwa\n" +
    "SmFiYmVyIFNvZnR3YXJlIEZvdW5kYXRpb24xIzAhBgNVBAsMGlNlY3VyZSBDZXJ0\n" +
    "aWZpY2F0ZSBTaWduaW5nMUYwRAYDVQQDDD1TdGFydENvbSBDbGFzcyAxIEludGVy\n" +
    "bWVkaWF0ZSBDQSAtIEphYmJlciBTb2Z0d2FyZSBGb3VuZGF0aW9uMSQwIgYJKoZI\n" +
    "hvcNAQkBFhVjZXJ0bWFzdGVyQGphYmJlci5vcmcwggEiMA0GCSqGSIb3DQEBAQUA\n" +
    "A4IBDwAwggEKAoIBAQCeju/E54r6cwRmEzkGwBIq5anE2IHM10iYIeqOjTnN2WMM\n" +
    "XERxgmuSpwJays/BaMATh1/QFnMHjXiTICmeyXbJ2fKrxTHPCJ+DUeLbFvVX3bOO\n" +
    "SxAffkCLwZuUw9RyZ9zDLBNpR1FsdiSD9mV9DEH4T3sNU79Mjy+o83jFojTg39R7\n" +
    "nH8B6z7VLmlC+ENxsMqjdwRv7HtY595VBLwK/gejblT8kCVFFA/WjmiOVoZ4aMGd\n" +
    "OOvsSgEZ9LaejB4xZdq+PP40DjxqhMQw89uzhWnCxxh0h+4PNfxhbPqJxZ9UMUWg\n" +
    "uPLYPAoj9U5p3YgmRvEaKdrijOkhODeNVkV/a57jAgMBAAGjggN6MIIDdjAMBgNV\n" +
    "HRMEBTADAQH/MAsGA1UdDwQEAwIBJjAdBgNVHQ4EFgQUe47EZ9BGIRcR/6F6QnWf\n" +
    "6sSrcuQwgd0GA1UdIwSB1TCB0oAUHInDlsy9/jLVDYyBMbaYnY0oZI2hgbakgbMw\n" +
    "gbAxCzAJBgNVBAYTAklMMQ8wDQYDVQQIEwZJc3JhZWwxDjAMBgNVBAcTBUVpbGF0\n" +
    "MRYwFAYDVQQKEw1TdGFydENvbSBMdGQuMRowGAYDVQQLExFDQSBBdXRob3JpdHkg\n" +
    "RGVwLjEpMCcGA1UEAxMgRnJlZSBTU0wgQ2VydGlmaWNhdGlvbiBBdXRob3JpdHkx\n" +
    "ITAfBgkqhkiG9w0BCQEWEmFkbWluQHN0YXJ0Y29tLm9yZ4IBADAgBgNVHREEGTAX\n" +
    "gRVjZXJ0bWFzdGVyQGphYmJlci5vcmcwHQYDVR0SBBYwFIESYWRtaW5Ac3RhcnRj\n" +
    "b20ub3JnMBEGCWCGSAGG+EIBAQQEAwIABzBUBglghkgBhvhCAQ0ERxZFU3RhcnRD\n" +
    "b20gQ2xhc3MgMSBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eSAtIEphYmJlciBTb2Z0\n" +
    "d2FyZSBGb3VuZGF0aW9uMGIGA1UdHwRbMFkwKaAnoCWGI2h0dHA6Ly9jZXJ0LnN0\n" +
    "YXJ0Y29tLm9yZy9jYS1jcmwuY3JsMCygKqAohiZodHRwOi8vY3JsLnN0YXJ0Y29t\n" +
    "Lm9yZy9jcmwvY2EtY3JsLmNybDCCAUoGA1UdIASCAUEwggE9MIIBOQYLKwYBBAGB\n" +
    "tTcBAQEwggEoMC8GCCsGAQUFBwIBFiNodHRwOi8vY2VydC5zdGFydGNvbS5vcmcv\n" +
    "cG9saWN5LnBkZjA1BggrBgEFBQcCARYpaHR0cDovL2NlcnQuc3RhcnRjb20ub3Jn\n" +
    "L2ludGVybWVkaWF0ZS5wZGYwgb0GCCsGAQUFBwICMIGwMBQWDVN0YXJ0Q29tIEx0\n" +
    "ZC4wAwIBARqBl0xpbWl0ZWQgTGlhYmlsaXR5LCByZWFkIHRoZSBzZWN0aW9uICpM\n" +
    "ZWdhbCBMaW1pdGF0aW9ucyogb2YgdGhlIFN0YXJ0Q29tIENlcnRpZmljYXRpb24g\n" +
    "QXV0aG9yaXR5IFBvbGljeSBhdmFpbGFibGUgYXQgaHR0cDovL2NlcnQuc3RhcnRj\n" +
    "b20ub3JnL3BvbGljeS5wZGYwDQYJKoZIhvcNAQEFBQADgYEAtOq85Q1lf8PjsJCg\n" +
    "uQ6TL3TJ1rSadfOwEyHJqIjR5LYpxdcJ5WxSEM3DxdrFnTaPBC6RQ7v836i9DdW3\n" +
    "FS5/y1Et5gKksLNPQqaYEVFuvB4AGTp2HkdUGo8Oz9Dd4zTcvTSTeo/9mVxqdxKa\n" +
    "lhMZMHD/ivqg8faZSQNYMg6xq7I=\n" +
    "-----END CERTIFICATE-----\n";

  private static final String GENERIC_ERROR = "It's not my fault!";

  private CertPathStore store;
  private CertificateManager manager;
  private Ticker ticker;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    store = new DefaultCertPathStore();
    manager = new CertificateManagerImpl(false, getSigner(), getVerifier(store, true), store);
    ticker = new Ticker();
  }

  /*
   * TESTS
   */
  public void testSignature() throws Exception {
    ProtocolWaveletDelta delta = ProtocolWaveletDelta.newBuilder()
        .setHashedVersion(getHashedVersion())
        .setAuthor("bob@example.com")
        .build();
    ByteStringMessage<ProtocolWaveletDelta> canonicalDelta = toCanonicalDelta(delta);

    ProtocolSignedDelta signedDelta = manager.signDelta(canonicalDelta);

    manager.storeSignerInfo(getSignerInfo().toProtoBuf());
    ByteStringMessage<ProtocolWaveletDelta> compare = manager.verifyDelta(signedDelta);

    assertEquals(canonicalDelta, compare);
  }

  public void testSignature_missingSignerInfo() throws Exception {
    ProtocolWaveletDelta delta = ProtocolWaveletDelta.newBuilder()
        .setHashedVersion(getHashedVersion())
        .setAuthor("bob@example.com")
        .build();
    ByteStringMessage<ProtocolWaveletDelta> canonicalDelta = toCanonicalDelta(delta);
    manager = new CertificateManagerImpl(false, getSigner(), getVerifier(store, false), store);
    ProtocolSignedDelta signedDelta = manager.signDelta(canonicalDelta);

    try {
      manager.verifyDelta(signedDelta);
      fail("expected UnknownSignerException, but didn't get it");
    } catch (UnknownSignerException e) {
      // expected
    } catch (Exception e) {
      fail("expected UnknownSignerExeception, but got " + e);
    }
  }

  public void testSignature_authorNotMatching() throws Exception {
    ProtocolWaveletDelta delta = ProtocolWaveletDelta.newBuilder()
        .setHashedVersion(getHashedVersion())
        .setAuthor("bob@someotherdomain.com")
        .build();
    ByteStringMessage<ProtocolWaveletDelta> canonicalDelta = toCanonicalDelta(delta);

    ProtocolSignedDelta signedDelta = manager.signDelta(canonicalDelta);

    manager.storeSignerInfo(getSignerInfo().toProtoBuf());

    try {
      manager.verifyDelta(signedDelta);
      fail("expected exception, but didn't get it");
    } catch (SignatureException e) {
      // expected
    }
  }

  public void testRealSignature() throws Exception {
    manager = new CertificateManagerImpl(false, getSigner(), getRealVerifier(store), store);
    manager.storeSignerInfo(getRealSignerInfo().toProtoBuf());
    ByteStringMessage<ProtocolWaveletDelta> compare = manager.verifyDelta(getFakeSignedDelta());
    assertEquals(compare, getFakeDelta());
  }

  /**
   * Test prefetchDeltaSignerInfo for a single request on a single domain, and that subsequent
   * requests on the same domain return instantly.
   */
  public void test_prefetchDeltaSignerInfo1() throws Exception {
    SignerInfoPrefetchResultListener mockListener =
        createStrictMock(SignerInfoPrefetchResultListener.class);
    mockListener.onSuccess(getRealSignerInfo().toProtoBuf());

    replay(mockListener);
    manager.prefetchDeltaSignerInfo(getSuccessfulProvider(), getRealSignerId(),
        getFakeWaveletName(DOMAIN), null, mockListener);
    verify(mockListener);

    // Shouldn't get a NPE from the null provider because the callback should not be used
    reset(mockListener);
    mockListener.onSuccess(getRealSignerInfo().toProtoBuf());

    replay(mockListener);
    manager.prefetchDeltaSignerInfo(null, getRealSignerId(), getFakeWaveletName(DOMAIN), null,
        mockListener);
    verify(mockListener);
  }

  /**
   * Test prefetchDeltaSignerInfo for multiple requests on a single domain where the first one
   * does not terminate.  The entire request should fail.
   */
  public void test_prefetchDeltaSignerInfo2() throws Exception {
    // The dead listener won't return
    SignerInfoPrefetchResultListener deadListener =
      createStrictMock(SignerInfoPrefetchResultListener.class);
    replay(deadListener);
    manager.prefetchDeltaSignerInfo(getDeadProvider(), getRealSignerId(),
        getFakeWaveletName(DOMAIN), null, deadListener);

    // But this will.  However, it shouldn't be called since the other was added first, and only
    // 1 request is started per domain
    SignerInfoPrefetchResultListener aliveListener =
      createStrictMock(SignerInfoPrefetchResultListener.class);
    replay(aliveListener);
    manager.prefetchDeltaSignerInfo(getSuccessfulProvider(), getRealSignerId(),
        getFakeWaveletName(DOMAIN), null, aliveListener);

    verify(deadListener, aliveListener);
  }

  /**
   * Test prefetchDeltaSignerInfo for multiple requests on different domains where the first one
   * does not terminate.  However the second should terminate, and both callbacks called.
   */
  public void test_prefetchDeltaSignerInfo3() throws Exception {
    // This will never return, but the callback will run later
    SignerInfoPrefetchResultListener deadListener =
      createStrictMock(SignerInfoPrefetchResultListener.class);

    replay(deadListener);
    manager.prefetchDeltaSignerInfo(getDeadProvider(), getRealSignerId(),
        getFakeWaveletName(DOMAIN), null, deadListener);

    // This should succeed later, after some number of ticks
    SignerInfoPrefetchResultListener slowSuccessListener =
      createStrictMock(SignerInfoPrefetchResultListener.class);

    replay(slowSuccessListener);
    manager.prefetchDeltaSignerInfo(getSlowSuccessfulProvider(ticker, EASY_TICKS),
        getRealSignerId(), getFakeWaveletName(OTHER_DOMAIN), null, slowSuccessListener);

    // This would succeed right now if it didn't have to wait for the slow success
    SignerInfoPrefetchResultListener successListener =
      createStrictMock(SignerInfoPrefetchResultListener.class);

    replay(successListener);
    manager.prefetchDeltaSignerInfo(getSuccessfulProvider(), getRealSignerId(),
        getFakeWaveletName(OTHER_DOMAIN), null, successListener);

    // After ticking, each callback should run
    verify(deadListener, slowSuccessListener, successListener);
    reset(deadListener, slowSuccessListener, successListener);

    deadListener.onSuccess(getRealSignerInfo().toProtoBuf());
    slowSuccessListener.onSuccess(getRealSignerInfo().toProtoBuf());
    successListener.onSuccess(getRealSignerInfo().toProtoBuf());

    replay(deadListener, slowSuccessListener, successListener);
    ticker.tick(EASY_TICKS);
    verify(deadListener, slowSuccessListener, successListener);

    // Subsequent calls should also succeed immediately without calling the callback
    SignerInfoPrefetchResultListener nullListener =
      createStrictMock(SignerInfoPrefetchResultListener.class);

    nullListener.onSuccess(getRealSignerInfo().toProtoBuf());
    replay(nullListener);
    manager.prefetchDeltaSignerInfo(null, getRealSignerId(), getFakeWaveletName(DOMAIN), null,
        nullListener);
    verify(nullListener);
  }

  /**
   * Test prefetchDeltaSignerInfo for failing requests -- the failure should be propagated to
   * the prefetch listener, and requests on the same domain should fail.
   */
  public void test_prefetchDeltaSignerInfo4() throws Exception {
    // This will fail later
    SignerInfoPrefetchResultListener failListener =
        createStrictMock(SignerInfoPrefetchResultListener.class);

    replay(failListener);
    manager.prefetchDeltaSignerInfo(getSlowFailingProvider(ticker, EASY_TICKS), getRealSignerId(),
        getFakeWaveletName(DOMAIN), null, failListener);

    // This would succeed later if it weren't for the previous one failing
    SignerInfoPrefetchResultListener successListener =
      createStrictMock(SignerInfoPrefetchResultListener.class);

    replay(successListener);
    manager.prefetchDeltaSignerInfo(getSuccessfulProvider(), getRealSignerId(),
        getFakeWaveletName(DOMAIN), null, successListener);

    // Both callbacks should fail after ticking
    verify(failListener, successListener);
    reset(failListener, successListener);

    failListener.onFailure(GENERIC_ERROR);
    successListener.onFailure(GENERIC_ERROR);

    replay(failListener, successListener);
    ticker.tick(EASY_TICKS);
    verify(failListener, successListener);
  }

  /**
   * Test prefetchDeltaSignerInfo for failing requests where a previous request on a different
   * domain has already succeeded.  The failing request should also appear to succeed.
   */
  public void test_prefetchDeltaSignerInfo5() throws Exception {
    // This would fail if the next (immediate) request didn't succeed
    SignerInfoPrefetchResultListener failListener =
      createStrictMock(SignerInfoPrefetchResultListener.class);

    replay(failListener);
    manager.prefetchDeltaSignerInfo(getSlowFailingProvider(ticker, EASY_TICKS), getRealSignerId(),
        getFakeWaveletName(DOMAIN), getHashedVersion(), failListener);
    verify(failListener);
    reset(failListener);

    // This will succeed immediately
    SignerInfoPrefetchResultListener successListener =
      createStrictMock(SignerInfoPrefetchResultListener.class);

    successListener.onSuccess(getRealSignerInfo().toProtoBuf());
    failListener.onSuccess(getRealSignerInfo().toProtoBuf());

    replay(failListener, successListener);
    manager.prefetchDeltaSignerInfo(getSuccessfulProvider(), getRealSignerId(),
        getFakeWaveletName(OTHER_DOMAIN), getHashedVersion(), successListener);
    verify(failListener, successListener);

    // The failing listener shouldn't do anything, even after the ticks
    reset(failListener, successListener);
    replay(failListener, successListener);
    ticker.tick(EASY_TICKS);
    verify(failListener, successListener);
  }

  /**
   * Test prefetchDeltaSignerInfo for failing requests -- even though the first request fails,
   * the second request on a different domain should succeed.
   */
  public void test_prefetchDeltaSignerInfo6() throws Exception {
    // This will fail later
    SignerInfoPrefetchResultListener failListener =
      createStrictMock(SignerInfoPrefetchResultListener.class);

    replay(failListener);
    manager.prefetchDeltaSignerInfo(getSlowFailingProvider(ticker, EASY_TICKS), getRealSignerId(),
        getFakeWaveletName(DOMAIN), getHashedVersion(), failListener);
    verify(failListener);

    // This will succeed later, after the failing one fails
    SignerInfoPrefetchResultListener successListener =
      createStrictMock(SignerInfoPrefetchResultListener.class);

    replay(successListener);
    manager.prefetchDeltaSignerInfo(getSlowSuccessfulProvider(ticker, EASY_TICKS * 2),
        getRealSignerId(), getFakeWaveletName(OTHER_DOMAIN), getHashedVersion(), successListener);
    verify(successListener);

    // The failing request should fail, but successful request left alone
    reset(failListener, successListener);
    failListener.onFailure(GENERIC_ERROR);
    replay(failListener, successListener);
    ticker.tick(EASY_TICKS);
    verify(failListener, successListener);

    // The successful request should now succeed
    reset(failListener, successListener);
    successListener.onSuccess(getRealSignerInfo().toProtoBuf());
    replay(failListener, successListener);
    ticker.tick(EASY_TICKS);
    verify(failListener, successListener);
  }


  /*
   * UTILITIES
   */

  private ByteStringMessage<ProtocolWaveletDelta> toCanonicalDelta(ProtocolWaveletDelta d) {
    try {
      ByteStringMessage<ProtocolWaveletDelta> canonicalDelta = ByteStringMessage.from(
          ProtocolWaveletDelta.getDefaultInstance(), d.toByteString());
      return canonicalDelta;
    } catch (InvalidProtocolBufferException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private ProtocolHashedVersion getHashedVersion() {
    return WaveletOperationSerializer.serialize(HashedVersion.unsigned(3L));
  }

  private WaveSignatureVerifier getRealVerifier(CertPathStore store) throws Exception {
    TrustRootsProvider trustRoots = new DefaultTrustRootsProvider();
    VerifiedCertChainCache cache = new DefaultCacheImpl(getFakeTimeSource());
    WaveCertPathValidator validator = new CachedCertPathValidator(
      cache, getFakeTimeSource(), trustRoots);

    return new WaveSignatureVerifier(validator, store);
  }

  private WaveSignatureVerifier getVerifier(CertPathStore store,
      boolean disableSignerVerification) {
    VerifiedCertChainCache cache = new DefaultCacheImpl(getFakeTimeSource());
    WaveCertPathValidator validator;
    if (disableSignerVerification) {
      validator = new DisabledCertPathValidator();
    } else {
      validator = new CachedCertPathValidator(
          cache, getFakeTimeSource(), getTrustRootsProvider());
    }
    return new WaveSignatureVerifier(validator, store);
  }

  private TrustRootsProvider getTrustRootsProvider() {
    return new TrustRootsProvider() {
      @Override
      public Collection<X509Certificate> getTrustRoots() {
        try {
          return getSigner().getSignerInfo().getCertificates();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  private WaveSigner getSigner() throws Exception {
    InputStream keyStream = new ByteArrayInputStream(PRIVATE_KEY.getBytes());
    InputStream certStream = new ByteArrayInputStream(CERTIFICATE.getBytes());
    List<InputStream> certStreams = ImmutableList.of(certStream);

    WaveSignerFactory factory = new WaveSignerFactory();
    return factory.getSigner(keyStream, certStreams, DOMAIN);
  }

  private SignerInfo getRealSignerInfo() throws Exception {
    CertificateFactory factory = CertificateFactory.getInstance("X.509");
    X509Certificate realCert = (X509Certificate) factory.generateCertificate(
        new ByteArrayInputStream(REAL_CERTIFICATE.getBytes()));
    X509Certificate startCom = (X509Certificate) factory.generateCertificate(
        new ByteArrayInputStream(STARTCOM_CERT.getBytes()));

    return new SignerInfo(HashAlgorithm.SHA256,
        ImmutableList.of(realCert, startCom), REAL_DOMAIN);
  }

  private SignerInfo getSignerInfo() throws Exception {
    return getSigner().getSignerInfo();
  }

  private TimeSource getFakeTimeSource() {
    return new TimeSource() {
      @Override
      public Date now() {
        return new Date(currentTimeMillis());
      }

      @Override
      public long currentTimeMillis() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(2009, 11, 1);
        return cal.getTimeInMillis();
      }
    };
  }

  private ProtocolSignedDelta getFakeSignedDelta() throws Exception {
    return ProtocolSignedDelta.newBuilder()
        .setDelta(getFakeDelta().getByteString())
        .addSignature(getRealSignature())
        .build();
  }

  private ByteStringMessage<ProtocolWaveletDelta> getFakeDelta() throws Exception {
    ProtocolWaveletDelta delta = ProtocolWaveletDelta.newBuilder()
        .setHashedVersion(getHashedVersion())
        .setAuthor("bob@initech-corp.com")
        .build();
    return toCanonicalDelta(delta);
  }

  private ProtocolSignature getRealSignature() throws Exception {
    return ProtocolSignature.newBuilder()
        .setSignerId(ByteString.copyFrom(getRealSignerInfo().getSignerId()))
        .setSignatureAlgorithm(SignatureAlgorithm.SHA1_RSA)
        .setSignatureBytes(ByteString.copyFrom(REAL_SIGNATURE))
        .build();
  }

  private WaveletName getFakeWaveletName(String domain) {
    return WaveletName.of(new WaveId(domain, "wave"), new WaveletId(domain, "wavelet"));
  }

  private ByteString getRealSignerId() throws Exception {
    return ByteString.copyFrom(getRealSignerInfo().getSignerId());
  }


  /*
   * Fake WaveletFederationProviders.
   */

  private abstract class WaveletSignerInfoProvider implements WaveletFederationProvider {
    @Override public void postSignerInfo(String destinationDomain, ProtocolSignerInfo signerInfo,
        PostSignerInfoResponseListener listener) {
      throw new UnsupportedOperationException();
    }

    @Override  public void requestHistory(WaveletName waveletName, String domain,
        ProtocolHashedVersion startVersion, ProtocolHashedVersion endVersion, long lengthLimit,
        HistoryResponseListener listener) {
      throw new UnsupportedOperationException();
    }

    @Override public void submitRequest(WaveletName waveletName, ProtocolSignedDelta delta,
        SubmitResultListener listener) {
      throw new UnsupportedOperationException();
    }
  }

  private WaveletFederationProvider getSuccessfulProvider() {
    return new WaveletSignerInfoProvider() {
      @Override public void getDeltaSignerInfo(ByteString signerId, WaveletName waveletName,
          ProtocolHashedVersion deltaEndVersion, DeltaSignerInfoResponseListener listener) {
        try {
          listener.onSuccess(getRealSignerInfo().toProtoBuf());
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  private WaveletFederationProvider getSlowSuccessfulProvider(final Ticker ticker,
      final int ticks) {
    return new WaveletSignerInfoProvider() {
      @Override public void getDeltaSignerInfo(ByteString signerId, WaveletName waveletName,
          ProtocolHashedVersion deltaEndVersion, final DeltaSignerInfoResponseListener listener) {
        ticker.runAt(ticks, new Runnable() {
          @Override public void run() {
            try {
              listener.onSuccess(getRealSignerInfo().toProtoBuf());
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }
        });
      }
    };
  }

  private WaveletFederationProvider getSlowFailingProvider(final Ticker ticker, final int ticks) {
    return new WaveletSignerInfoProvider() {
      @Override public void getDeltaSignerInfo(ByteString signerId, WaveletName waveletName,
          ProtocolHashedVersion deltaEndVersion, final DeltaSignerInfoResponseListener listener) {
        ticker.runAt(ticks, new Runnable() {
          @Override public void run() {
            listener.onFailure(GENERIC_ERROR);
          }
        });
      }
    };
  }

  private WaveletFederationProvider getDeadProvider() {
    return new WaveletSignerInfoProvider() {
      @Override public void getDeltaSignerInfo(ByteString signerId, WaveletName waveletName,
          ProtocolHashedVersion deltaEndVersion, DeltaSignerInfoResponseListener listener) {
        // Never calls the callback
      }
    };
  }
}
