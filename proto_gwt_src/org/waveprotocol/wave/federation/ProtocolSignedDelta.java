// Generated by http://code.google.com/p/protostuff/ ... DO NOT EDIT!
// Generated from federation.protodevel

package org.waveprotocol.wave.federation;

import com.google.gwt.core.client.*;

public class ProtocolSignedDelta extends JavaScriptObject  {


    public static class Builder extends ProtocolSignedDelta {
      protected Builder() { }
      public final ProtocolSignedDelta build() {
        return (ProtocolSignedDelta)this;
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
     * Creates a new ProtocolSignedDelta instance 
     *
     * @return new ProtocolSignedDelta instance
     */
    public static native ProtocolSignedDelta create() /*-{
        return {
          "_protoMessageName": "ProtocolSignedDelta",              
        };
    }-*/;

    /**
     * Creates a new JsArray<ProtocolSignedDelta> instance 
     *
     * @return new JsArray<ProtocolSignedDelta> instance
     */
    public static native JsArray<ProtocolSignedDelta> createArray() /*-{
        return [];
    }-*/;

    /**
     * Returns the name of this protocol buffer.
     */
    public static native String getProtocolBufferName(JavaScriptObject instance) /*-{
        return instance._protoMessageName;
    }-*/;

    /**
     * Gets a ProtocolSignedDelta (casting) from a JavaScriptObject
     *
     * @param JavaScriptObject to cast
     * @return ProtocolSignedDelta
     */
    public static native ProtocolSignedDelta get(JavaScriptObject jso) /*-{
        return jso;
    }-*/;

    /**
     * Gets a JsArray<ProtocolSignedDelta> (casting) from a JavaScriptObject
     *
     * @param JavaScriptObject to cast
     * @return JsArray<ProtocolSignedDelta> 
     */
    public static native JsArray<ProtocolSignedDelta> getArray(JavaScriptObject jso) /*-{
        return jso;
    }-*/;

    /**
     * Parses a ProtocolSignedDelta from a json string
     *
     * @param json string to be parsed/evaluated
     * @return ProtocolSignedDelta 
     */
    public static native ProtocolSignedDelta parse(String json) /*-{
        return eval("(" + json + ")");
    }-*/;

    /**
     * Parses a JsArray<ProtocolSignedDelta> from a json string
     *
     * @param json string to be parsed/evaluated
     * @return JsArray<ProtocolSignedDelta> 
     */
    public static native JsArray<ProtocolSignedDelta> parseArray(String json) /*-{
        return eval("(" + json + ")");
    }-*/;
    
    /**
     * Serializes a json object to a json string.
     *
     * @param ProtocolSignedDelta the object to serialize
     * @return String the serialized json string
     */
    public static native String stringify(ProtocolSignedDelta obj) /*-{
        var buf = [];
        var _1 = obj["1"];
        if(_1 != null)
            buf.push("\"1\":\"" + _1 + "\"");
        var _2 = obj["2"];
        if(_2 != null && _2.length != 0) {
            var b = [], fn = @org.waveprotocol.wave.federation.ProtocolSignature::stringify(Lorg/waveprotocol/wave/federation/ProtocolSignature;);
            for(var i=0,l=_2.length; i<l; i++)
                b.push(fn(_2[i]));
            buf.push("\"2\":[" + b.join(",") + "]");
        }

        return buf.length == 0 ? "{}" : "{" + buf.join(",") + "}";
    }-*/;
    
    public static native boolean isInitialized(ProtocolSignedDelta obj) /*-{
        return 
            obj["1"] != null;
    }-*/;

    protected ProtocolSignedDelta() {}

    // getters and setters

    // delta

    public final native String getDelta() /*-{
        return this["1"];
    }-*/;

    public final native ProtocolSignedDelta setDelta(String delta) /*-{
        this["1"] = delta;
        return this;
    }-*/;

    public final native void clearDelta() /*-{
        delete this["1"];
    }-*/;

    public final native boolean hasDelta() /*-{
        return this["1"] != null;
    }-*/;

    // signature

    public final native JsArray<ProtocolSignature> getSignatureArray() /*-{
        return this["2"];
    }-*/;

    public final java.util.List<ProtocolSignature> getSignatureList() {
        JsArray<ProtocolSignature> array = getSignatureArray();
        java.util.List<ProtocolSignature> list = new java.util.ArrayList<ProtocolSignature>();
        
        if (array == null) {
          return null; 
        }
        for (int i=0; i < getSignatureCount(); i++) {
          list.add(array.get(i));
        }
        return list;
    }

    public final native ProtocolSignedDelta setSignatureArray(JsArray<ProtocolSignature> signature) /*-{
        this["2"] = signature;
        return this;
    }-*/;

    public final native JsArray<ProtocolSignature> clearSignatureArray() /*-{
        return (this["2"] = []);
    }-*/;

    public final ProtocolSignature getSignature(int index) {
        JsArray<ProtocolSignature> array = getSignatureArray();
        return array == null ? null : array.get(index);
    }

    public final int getSignatureCount() {
        JsArray<ProtocolSignature> array = getSignatureArray();
        return array == null ? 0 : array.length();
    }

    public final void addSignature(ProtocolSignature signature) {
        JsArray<ProtocolSignature> array = getSignatureArray();
        if(array == null)
            array = clearSignatureArray();
        array.push(signature);
    }


}