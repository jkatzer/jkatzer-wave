package org.waveprotocol.wave.examples.fedone.rpc;

import com.google.protobuf.Message;
import com.google.protobuf.UnknownFieldSet;
import junit.framework.TestCase;
import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc;

/**
 * @author arb@google.com
 */
public class WebSocketChannelTest extends TestCase {
  private TestWebSocketChannel channel;
  private TestCallback callback;
  private static final int SEQUENCE_NUMBER = 5;

  class TestWebSocketChannel extends WebSocketChannel {
    String message;

    public TestWebSocketChannel(ProtoCallback callback) {
      super(callback);
      this.message = null;
    }

    @Override
    protected void sendMessageString(final String data) {
      this.message = data;
    }
  }

  class TestCallback implements ProtoCallback {
      Message savedMessage = null;
      long sequenceNumber;

      @Override
      public void message(final long sequenceNo, final Message message) {
        this.sequenceNumber = sequenceNo;
        this.savedMessage = message;
      }

      @Override
      public void unknown(final long sequenceNo, final String messageType,
          final UnknownFieldSet message) {
        fail("unknown");
      }

      @Override
      public void unknown(final long sequenceNo, final String messageType, final String message) {
        fail("unknown");
      }
    }

  @Override
  public void setUp() {
    callback = new TestCallback();
    channel = new TestWebSocketChannel(callback);
  }

  public void testRoundTrippingJson() throws Exception {
    WaveClientRpc.ProtocolOpenRequest.Builder sourceBuilder = buildProtocolOpenRequest();
    checkRoundtripping(sourceBuilder);
  }

  public void testRoundTrippingJsonOptionalField() throws Exception {
    WaveClientRpc.ProtocolOpenRequest.Builder sourceBuilder = buildProtocolOpenRequest();
    sourceBuilder.clearSnapshots();
    checkRoundtripping(sourceBuilder);
  }

  public void testRoundTrippingJsonRepeatedField() throws Exception {
    WaveClientRpc.ProtocolOpenRequest.Builder sourceBuilder = buildProtocolOpenRequest();
    sourceBuilder.addWaveletIdPrefix("aaa");
    sourceBuilder.addWaveletIdPrefix("bbb");
    sourceBuilder.addWaveletIdPrefix("ccc");
    checkRoundtripping(sourceBuilder);
  }

  private void checkRoundtripping(final WaveClientRpc.ProtocolOpenRequest.Builder sourceBuilder) {
    WaveClientRpc.ProtocolOpenRequest sourceRequest = sourceBuilder.build();
    channel.sendMessage(SEQUENCE_NUMBER, sourceRequest);
    String sentRequest = channel.message;
    assertNotNull(sentRequest);
    System.out.println(sentRequest);
    channel.handleMessageString(sentRequest);
    assertNotNull(callback.savedMessage);
    assertEquals(SEQUENCE_NUMBER, callback.sequenceNumber);
    assertEquals(sourceRequest, callback.savedMessage);
  }

  private WaveClientRpc.ProtocolOpenRequest.Builder buildProtocolOpenRequest() {
    WaveClientRpc.ProtocolOpenRequest.Builder sourceBuilder =
        WaveClientRpc.ProtocolOpenRequest.newBuilder();
    sourceBuilder.setMaximumWavelets(5);
    sourceBuilder.setParticipantId("O HAI");
    sourceBuilder.setSnapshots(true);
    sourceBuilder.setWaveId("lol!cat");
    return sourceBuilder;
  }
}
