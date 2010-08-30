// Generated by http://code.google.com/p/protostuff/ ... DO NOT EDIT!
// Generated from waveclient-rpc.proto

package org.waveprotocol.wave.examples.fedone.waveserver;

import com.google.gwt.core.client.*;

public class WaveletSnapshot extends JavaScriptObject  {


    public static class Builder extends WaveletSnapshot {
      protected Builder() { }
      public final WaveletSnapshot build() {
        return (WaveletSnapshot)this;
      }
      public static native Builder create() /*-{
        return {
        };
      }-*/;
    }

    public static final Builder newBuilder() {
      return Builder.create();
    }
    
    /**
     * Creates a new WaveletSnapshot instance 
     *
     * @return new WaveletSnapshot instance
     */
    public static native WaveletSnapshot create() /*-{
        return {
          "_protoMessageName": "WaveletSnapshot",              
        };
    }-*/;

    /**
     * Creates a new JsArray<WaveletSnapshot> instance 
     *
     * @return new JsArray<WaveletSnapshot> instance
     */
    public static native JsArray<WaveletSnapshot> createArray() /*-{
        return [];
    }-*/;

    /**
     * Returns the name of this protocol buffer.
     */
    public static native String getProtocolBufferName(JavaScriptObject instance) /*-{
        return instance._protoMessageName;
    }-*/;

    /**
     * Gets a WaveletSnapshot (casting) from a JavaScriptObject
     *
     * @param JavaScriptObject to cast
     * @return WaveletSnapshot
     */
    public static native WaveletSnapshot get(JavaScriptObject jso) /*-{
        return jso;
    }-*/;

    /**
     * Gets a JsArray<WaveletSnapshot> (casting) from a JavaScriptObject
     *
     * @param JavaScriptObject to cast
     * @return JsArray<WaveletSnapshot> 
     */
    public static native JsArray<WaveletSnapshot> getArray(JavaScriptObject jso) /*-{
        return jso;
    }-*/;

    /**
     * Parses a WaveletSnapshot from a json string
     *
     * @param json string to be parsed/evaluated
     * @return WaveletSnapshot 
     */
    public static native WaveletSnapshot parse(String json) /*-{
        return eval("(" + json + ")");
    }-*/;

    /**
     * Parses a JsArray<WaveletSnapshot> from a json string
     *
     * @param json string to be parsed/evaluated
     * @return JsArray<WaveletSnapshot> 
     */
    public static native JsArray<WaveletSnapshot> parseArray(String json) /*-{
        return eval("(" + json + ")");
    }-*/;
    
    /**
     * Serializes a json object to a json string.
     *
     * @param WaveletSnapshot the object to serialize
     * @return String the serialized json string
     */
    public static native String stringify(WaveletSnapshot obj) /*-{
        var buf = [];
        var _1 = obj["1"];
        if(_1 != null && _1.length != 0) {
            buf.push("\"1\":[\"" + _1.join("\",\"") + "\"]");
        }
        var _2 = obj["2"];
        if(_2 != null && _2.length != 0) {
            var b = [], fn = @org.waveprotocol.wave.examples.fedone.waveserver.DocumentSnapshot::stringify(Lorg/waveprotocol/wave/examples/fedone/waveserver/DocumentSnapshot;);
            for(var i=0,l=_2.length; i<l; i++)
                b.push(fn(_2[i]));
            buf.push("\"2\":[" + b.join(",") + "]");
        }

        return buf.length == 0 ? "{}" : "{" + buf.join(",") + "}";
    }-*/;
    
    public static native boolean isInitialized(WaveletSnapshot obj) /*-{
        return true;
    }-*/;

    protected WaveletSnapshot() {}

    // getters and setters

    // participantId

    public final native JsArrayString getParticipantIdArray() /*-{
        return this["1"];
    }-*/;

    public final java.util.List<String> getParticipantIdList() {
        JsArrayString array = getParticipantIdArray();
        java.util.List<String> list = new java.util.ArrayList<String>();
        
        if (array == null) {
          return null; 
        }
        for (int i=0; i < getParticipantIdCount(); i++) {
          list.add(array.get(i));
        }
        return list;
    }

    public final native WaveletSnapshot setParticipantIdArray(JsArrayString participantId) /*-{
        this["1"] = participantId;
        return this;
    }-*/;

    public final native JsArrayString clearParticipantIdArray() /*-{
        return (this["1"] = []);
    }-*/;

    public final String getParticipantId(int index) {
        JsArrayString array = getParticipantIdArray();
        return array == null ? null : array.get(index);
    }

    public final int getParticipantIdCount() {
        JsArrayString array = getParticipantIdArray();
        return array == null ? 0 : array.length();
    }

    public final void addParticipantId(String participantId) {
        JsArrayString array = getParticipantIdArray();
        if(array == null)
            array = clearParticipantIdArray();
        array.push(participantId);
    }

    // document

    public final native JsArray<DocumentSnapshot> getDocumentArray() /*-{
        return this["2"];
    }-*/;

    public final java.util.List<DocumentSnapshot> getDocumentList() {
        JsArray<DocumentSnapshot> array = getDocumentArray();
        java.util.List<DocumentSnapshot> list = new java.util.ArrayList<DocumentSnapshot>();
        
        if (array == null) {
          return null; 
        }
        for (int i=0; i < getDocumentCount(); i++) {
          list.add(array.get(i));
        }
        return list;
    }

    public final native WaveletSnapshot setDocumentArray(JsArray<DocumentSnapshot> document) /*-{
        this["2"] = document;
        return this;
    }-*/;

    public final native JsArray<DocumentSnapshot> clearDocumentArray() /*-{
        return (this["2"] = []);
    }-*/;

    public final DocumentSnapshot getDocument(int index) {
        JsArray<DocumentSnapshot> array = getDocumentArray();
        return array == null ? null : array.get(index);
    }

    public final int getDocumentCount() {
        JsArray<DocumentSnapshot> array = getDocumentArray();
        return array == null ? 0 : array.length();
    }

    public final void addDocument(DocumentSnapshot document) {
        JsArray<DocumentSnapshot> array = getDocumentArray();
        if(array == null)
            array = clearDocumentArray();
        array.push(document);
    }


}