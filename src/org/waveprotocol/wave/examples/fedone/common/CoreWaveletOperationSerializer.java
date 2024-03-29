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

package org.waveprotocol.wave.examples.fedone.common;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;

import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.DocumentSnapshot;
import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.ProtocolWaveletUpdate;
import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.WaveletSnapshot;
import org.waveprotocol.wave.federation.Proto.ProtocolDocumentOperation;
import org.waveprotocol.wave.federation.Proto.ProtocolHashedVersion;
import org.waveprotocol.wave.federation.Proto.ProtocolWaveletDelta;
import org.waveprotocol.wave.federation.Proto.ProtocolWaveletOperation;
import org.waveprotocol.wave.model.document.operation.AnnotationBoundaryMap;
import org.waveprotocol.wave.model.document.operation.Attributes;
import org.waveprotocol.wave.model.document.operation.AttributesUpdate;
import org.waveprotocol.wave.model.document.operation.BufferedDocOp;
import org.waveprotocol.wave.model.document.operation.DocOp;
import org.waveprotocol.wave.model.document.operation.DocOpCursor;
import org.waveprotocol.wave.model.document.operation.impl.AnnotationBoundaryMapImpl;
import org.waveprotocol.wave.model.document.operation.impl.AttributesImpl;
import org.waveprotocol.wave.model.document.operation.impl.AttributesUpdateImpl;
import org.waveprotocol.wave.model.document.operation.impl.DocOpBuilder;
import org.waveprotocol.wave.model.id.WaveletName;
import org.waveprotocol.wave.model.operation.OperationException;
import org.waveprotocol.wave.model.operation.core.CoreAddParticipant;
import org.waveprotocol.wave.model.operation.core.CoreNoOp;
import org.waveprotocol.wave.model.operation.core.CoreRemoveParticipant;
import org.waveprotocol.wave.model.operation.core.CoreWaveletDelta;
import org.waveprotocol.wave.model.operation.core.CoreWaveletDocumentOperation;
import org.waveprotocol.wave.model.operation.core.CoreWaveletOperation;
import org.waveprotocol.wave.model.operation.wave.WaveletOperation;
import org.waveprotocol.wave.model.schema.SchemaCollection;
import org.waveprotocol.wave.model.version.DistinctVersion;
import org.waveprotocol.wave.model.wave.ParticipantId;
import org.waveprotocol.wave.model.wave.data.DocumentFactory;
import org.waveprotocol.wave.model.wave.data.ObservableWaveletData;
import org.waveprotocol.wave.model.wave.data.core.CoreWaveletData;
import org.waveprotocol.wave.model.wave.data.core.impl.CoreWaveletDataImpl;
import org.waveprotocol.wave.model.wave.data.impl.DataUtil;
import org.waveprotocol.wave.model.wave.data.impl.ObservablePluggableMutableDocument;
import org.waveprotocol.wave.model.wave.data.impl.WaveletDataImpl;

import java.util.List;
import java.util.Map;

/**
 * Utility class for serialising/deserialising wavelet operations (and their components) to/from
 * their protocol buffer representations (and their components).
 *
 *
 */
public class CoreWaveletOperationSerializer {

  private static DocumentFactory<?> DOCUMENT_FACTORY =
      ObservablePluggableMutableDocument.createFactory(SchemaCollection.empty());

  private CoreWaveletOperationSerializer() {
  }

  /**
   * Serialize a {@link CoreWaveletDelta} as a {@link ProtocolWaveletDelta} at a
   * specific version.
   *
   * @param waveletDelta to serialize
   * @param version version at which the delta applies
   * @return serialized protocol buffer wavelet delta
   */
  public static ProtocolWaveletDelta serialize(CoreWaveletDelta waveletDelta,
      HashedVersion version) {
   ProtocolWaveletDelta.Builder protobufDelta = ProtocolWaveletDelta.newBuilder();

   for (CoreWaveletOperation waveletOp : waveletDelta.getOperations()) {
     protobufDelta.addOperation(serialize(waveletOp));
   }
   protobufDelta.setAuthor(waveletDelta.getAuthor().getAddress());
   protobufDelta.setHashedVersion(serialize(version));

   return protobufDelta.build();
  }

  /**
   * Serialize a {@link CoreWaveletOperation} as a {@link ProtocolWaveletOperation}.
   *
   * @param waveletOp wavelet operation to serialize
   * @return serialized protocol buffer wavelet operation
   */
  public static ProtocolWaveletOperation serialize(CoreWaveletOperation waveletOp) {
    ProtocolWaveletOperation.Builder protobufOp = ProtocolWaveletOperation.newBuilder();

    if (waveletOp instanceof CoreNoOp) {
      protobufOp.setNoOp(true);
    } else if (waveletOp instanceof CoreAddParticipant) {
      protobufOp.setAddParticipant(
          ((CoreAddParticipant) waveletOp).getParticipantId().getAddress());
    } else if (waveletOp instanceof CoreRemoveParticipant) {
      protobufOp.setRemoveParticipant(
          ((CoreRemoveParticipant) waveletOp).getParticipantId().getAddress());
    } else if (waveletOp instanceof CoreWaveletDocumentOperation) {
      ProtocolWaveletOperation.MutateDocument.Builder mutation =
        ProtocolWaveletOperation.MutateDocument.newBuilder();
      mutation.setDocumentId(((CoreWaveletDocumentOperation) waveletOp).getDocumentId());
      mutation.setDocumentOperation(
          serialize(((CoreWaveletDocumentOperation) waveletOp).getOperation()));
      protobufOp.setMutateDocument(mutation.build());
    } else {
      throw new IllegalArgumentException("Unsupported operation type: " + waveletOp);
    }

    return protobufOp.build();
  }

  /**
   * Serialize a {@link DocOp} as a {@link ProtocolDocumentOperation}.
   *
   * @param inputOp document operation to serialize
   * @return serialized protocol buffer document operation
   */
  public static ProtocolDocumentOperation serialize(DocOp inputOp) {
    final ProtocolDocumentOperation.Builder output = ProtocolDocumentOperation.newBuilder();

    inputOp.apply(new DocOpCursor() {
      private ProtocolDocumentOperation.Component.Builder newComponentBuilder() {
        return ProtocolDocumentOperation.Component.newBuilder();
      }

      @Override public void retain(int itemCount) {
        output.addComponent(newComponentBuilder().setRetainItemCount(itemCount).build());
      }

      @Override public void characters(String characters) {
        output.addComponent(newComponentBuilder().setCharacters(characters).build());
      }

      @Override public void deleteCharacters(String characters) {
        output.addComponent(newComponentBuilder().setDeleteCharacters(characters).build());
      }

      @Override public void elementStart(String type, Attributes attributes) {
        ProtocolDocumentOperation.Component.ElementStart e = makeElementStart(type, attributes);
        output.addComponent(newComponentBuilder().setElementStart(e).build());
      }

      @Override public void deleteElementStart(String type, Attributes attributes) {
        ProtocolDocumentOperation.Component.ElementStart e = makeElementStart(type, attributes);
        output.addComponent(newComponentBuilder().setDeleteElementStart(e).build());
      }

      private ProtocolDocumentOperation.Component.ElementStart makeElementStart(
          String type, Attributes attributes) {
        ProtocolDocumentOperation.Component.ElementStart.Builder e =
          ProtocolDocumentOperation.Component.ElementStart.newBuilder();

        e.setType(type);

        for (String name : attributes.keySet()) {
          e.addAttribute(ProtocolDocumentOperation.Component.KeyValuePair.newBuilder()
              .setKey(name).setValue(attributes.get(name)).build());
        }

        return e.build();
      }

      @Override public void elementEnd() {
        output.addComponent(newComponentBuilder().setElementEnd(true).build());
      }

      @Override public void deleteElementEnd() {
        output.addComponent(newComponentBuilder().setDeleteElementEnd(true).build());
      }

      @Override public void replaceAttributes(Attributes oldAttributes, Attributes newAttributes) {
        ProtocolDocumentOperation.Component.ReplaceAttributes.Builder r =
          ProtocolDocumentOperation.Component.ReplaceAttributes.newBuilder();

        if (oldAttributes.isEmpty() && newAttributes.isEmpty()) {
          r.setEmpty(true);
        } else {
          for (String name : oldAttributes.keySet()) {
            r.addOldAttribute(ProtocolDocumentOperation.Component.KeyValuePair.newBuilder()
                .setKey(name).setValue(oldAttributes.get(name)).build());
          }

          for (String name : newAttributes.keySet()) {
            r.addNewAttribute(ProtocolDocumentOperation.Component.KeyValuePair.newBuilder()
                .setKey(name).setValue(newAttributes.get(name)).build());
          }
        }

        output.addComponent(newComponentBuilder().setReplaceAttributes(r.build()).build());
      }

      @Override public void updateAttributes(AttributesUpdate attributes) {
        ProtocolDocumentOperation.Component.UpdateAttributes.Builder u =
          ProtocolDocumentOperation.Component.UpdateAttributes.newBuilder();

        if (attributes.changeSize() == 0) {
          u.setEmpty(true);
        } else {
          for (int i = 0; i < attributes.changeSize(); i++) {
            u.addAttributeUpdate(makeKeyValueUpdate(
                attributes.getChangeKey(i), attributes.getOldValue(i), attributes.getNewValue(i)));
          }
        }

        output.addComponent(newComponentBuilder().setUpdateAttributes(u.build()).build());
      }

      @Override public void annotationBoundary(AnnotationBoundaryMap map) {
        ProtocolDocumentOperation.Component.AnnotationBoundary.Builder a =
          ProtocolDocumentOperation.Component.AnnotationBoundary.newBuilder();

        if (map.endSize() == 0 && map.changeSize() == 0) {
          a.setEmpty(true);
        } else {
          for (int i = 0; i < map.endSize(); i++) {
            a.addEnd(map.getEndKey(i));
          }
          for (int i = 0; i < map.changeSize(); i++) {
            a.addChange(makeKeyValueUpdate(
                map.getChangeKey(i), map.getOldValue(i), map.getNewValue(i)));
          }
        }

        output.addComponent(newComponentBuilder().setAnnotationBoundary(a.build()).build());
      }

      private ProtocolDocumentOperation.Component.KeyValueUpdate makeKeyValueUpdate(
          String key, String oldValue, String newValue) {
        ProtocolDocumentOperation.Component.KeyValueUpdate.Builder kvu =
          ProtocolDocumentOperation.Component.KeyValueUpdate.newBuilder();
        kvu.setKey(key);
        if (oldValue != null) {
          kvu.setOldValue(oldValue);
        }
        if (newValue != null) {
          kvu.setNewValue(newValue);
        }

        return kvu.build();
      }
    });

    return output.build();
  }

  /**
   * Deserializes a {@link ProtocolWaveletDelta} as a {@link VersionedWaveletDelta}.
   *
   * @param delta protocol buffer wavelet delta to deserialize
   * @return deserialized wavelet delta and version
   */
  public static VersionedWaveletDelta deserialize(ProtocolWaveletDelta delta) {
    List<CoreWaveletOperation> ops = Lists.newArrayList();
    for (ProtocolWaveletOperation op : delta.getOperationList()) {
      ops.add(deserialize(op));
    }
    CoreWaveletDelta coreDelta = new CoreWaveletDelta(new ParticipantId(delta.getAuthor()), ops);
    HashedVersion hashedVersion = deserialize(delta.getHashedVersion());
    return new VersionedWaveletDelta(coreDelta, hashedVersion);
  }

  /** Deserializes a protobuf to a HashedVersion POJO. */
  public static HashedVersion deserialize(ProtocolHashedVersion hashedVersion) {
    final ByteString historyHash = hashedVersion.getHistoryHash();
    return new HashedVersion(hashedVersion.getVersion(),
        historyHash.toByteArray());
  }

  /** Serializes a HashedVersion POJO to a protobuf. */
  public static ProtocolHashedVersion serialize(HashedVersion hashedVersion) {
    return ProtocolHashedVersion.newBuilder().setVersion(hashedVersion.getVersion()).
      setHistoryHash(ByteString.copyFrom(hashedVersion.getHistoryHash())).build();
  }

  /**
   * Deserialize a {@link ProtocolWaveletOperation} as a {@link WaveletOperation}.
   *
   * @param protobufOp protocol buffer wavelet operation to deserialize
   * @return deserialized wavelet operation
   */
  public static CoreWaveletOperation deserialize(ProtocolWaveletOperation protobufOp) {
    if (protobufOp.hasNoOp()) {
      return CoreNoOp.INSTANCE;
    } else if (protobufOp.hasAddParticipant()) {
      return new CoreAddParticipant(new ParticipantId(protobufOp.getAddParticipant()));
    } else if (protobufOp.hasRemoveParticipant()) {
      return new CoreRemoveParticipant(new ParticipantId(protobufOp.getRemoveParticipant()));
    } else if (protobufOp.hasMutateDocument()) {
      return new CoreWaveletDocumentOperation(
          protobufOp.getMutateDocument().getDocumentId(),
          deserialize(protobufOp.getMutateDocument().getDocumentOperation()));
    } else {
      throw new IllegalArgumentException("Unsupported operation: " + protobufOp);
    }
  }

  /**
   * Deserialize a {@link WaveletSnapshot} into a list of
   * {@link WaveletOperation}s.
   *
   * @param snapshot snapshot protocol buffer to deserialize
   * @return a list of operations
   */
  public static List<CoreWaveletOperation> deserialize(WaveletSnapshot snapshot) {
    List<CoreWaveletOperation> ops = Lists.newArrayList();
    for (String participant : snapshot.getParticipantIdList()) {
      CoreAddParticipant addOp = new CoreAddParticipant(new ParticipantId(participant));
      ops.add(addOp);
    }
    for (DocumentSnapshot document : snapshot.getDocumentList()) {
      CoreWaveletDocumentOperation docOp = new CoreWaveletDocumentOperation(
          document.getDocumentId(), deserialize(document.getDocumentOperation()));
      ops.add(docOp);
    }
    return ops;
  }

  /**
   * Deserialize a {@link ProtocolDocumentOperation} into a {@link DocOp}.
   *
   * @param op protocol buffer document operation to deserialize
   * @return deserialized DocOp
   */
  public static BufferedDocOp deserialize(ProtocolDocumentOperation op) {
    DocOpBuilder output = new DocOpBuilder();

    for (ProtocolDocumentOperation.Component c : op.getComponentList()) {
      if (c.hasAnnotationBoundary()) {
        if (c.getAnnotationBoundary().getEmpty()) {
          output.annotationBoundary(AnnotationBoundaryMapImpl.EMPTY_MAP);
        } else {
          String[] ends = new String[c.getAnnotationBoundary().getEndCount()];
          String[] changeKeys = new String[c.getAnnotationBoundary().getChangeCount()];
          String[] oldValues = new String[c.getAnnotationBoundary().getChangeCount()];
          String[] newValues = new String[c.getAnnotationBoundary().getChangeCount()];
          if (c.getAnnotationBoundary().getEndCount() > 0) {
            c.getAnnotationBoundary().getEndList().toArray(ends);
          }
          for (int i = 0; i < changeKeys.length; i++) {
            ProtocolDocumentOperation.Component.KeyValueUpdate kvu =
              c.getAnnotationBoundary().getChange(i);
            changeKeys[i] = kvu.getKey();
            oldValues[i] = kvu.hasOldValue() ? kvu.getOldValue() : null;
            newValues[i] = kvu.hasNewValue() ? kvu.getNewValue() : null;
          }
          output.annotationBoundary(
              new AnnotationBoundaryMapImpl(ends, changeKeys, oldValues, newValues));
        }
      } else if (c.hasCharacters()) {
        output.characters(c.getCharacters());
      } else if (c.hasElementStart()) {
        Map<String, String> attributesMap = Maps.newHashMap();
        for (ProtocolDocumentOperation.Component.KeyValuePair pair :
            c.getElementStart().getAttributeList()) {
          attributesMap.put(pair.getKey(), pair.getValue());
        }
        output.elementStart(c.getElementStart().getType(), new AttributesImpl(attributesMap));
      } else if (c.hasElementEnd()) {
        output.elementEnd();
      } else if (c.hasRetainItemCount()) {
        output.retain(c.getRetainItemCount());
      } else if (c.hasDeleteCharacters()) {
        output.deleteCharacters(c.getDeleteCharacters());
      } else if (c.hasDeleteElementStart()) {
        Map<String, String> attributesMap = Maps.newHashMap();
        for (ProtocolDocumentOperation.Component.KeyValuePair pair :
            c.getDeleteElementStart().getAttributeList()) {
          attributesMap.put(pair.getKey(), pair.getValue());
        }
        output.deleteElementStart(c.getDeleteElementStart().getType(),
            new AttributesImpl(attributesMap));
      } else if (c.hasDeleteElementEnd()) {
        output.deleteElementEnd();
      } else if (c.hasReplaceAttributes()) {
        if (c.getReplaceAttributes().getEmpty()) {
          output.replaceAttributes(AttributesImpl.EMPTY_MAP, AttributesImpl.EMPTY_MAP);
        } else {
          Map<String, String> oldAttributesMap = Maps.newHashMap();
          Map<String, String> newAttributesMap = Maps.newHashMap();
          for (ProtocolDocumentOperation.Component.KeyValuePair pair :
              c.getReplaceAttributes().getOldAttributeList()) {
            oldAttributesMap.put(pair.getKey(), pair.getValue());
          }
          for (ProtocolDocumentOperation.Component.KeyValuePair pair :
              c.getReplaceAttributes().getNewAttributeList()) {
            newAttributesMap.put(pair.getKey(), pair.getValue());
          }
          output.replaceAttributes(new AttributesImpl(oldAttributesMap),
              new AttributesImpl(newAttributesMap));
        }
      } else if (c.hasUpdateAttributes()) {
        if (c.getUpdateAttributes().getEmpty()) {
          output.updateAttributes(AttributesUpdateImpl.EMPTY_MAP);
        } else {
          String[] triplets = new String[c.getUpdateAttributes().getAttributeUpdateCount()*3];
          for (int i = 0, j = 0; i < c.getUpdateAttributes().getAttributeUpdateCount(); i++) {
            ProtocolDocumentOperation.Component.KeyValueUpdate kvu =
              c.getUpdateAttributes().getAttributeUpdate(i);
            triplets[j++] = kvu.getKey();
            triplets[j++] = kvu.hasOldValue() ? kvu.getOldValue() : null;
            triplets[j++] = kvu.hasNewValue() ? kvu.getNewValue() : null;
          }
          output.updateAttributes(new AttributesUpdateImpl(triplets));
        }
      } else {
        //throw new IllegalArgumentException("Unsupported operation component: " + c);
      }
    }

    return output.build();
  }

  /**
   * Deserializes the snapshot contained in the {@link ProtocolWaveletUpdate}
   * into a {@link ObservableWaveletData}.
   *
   * @param snapshot the {@link WaveletSnapshot} to deserialize.
   * @param version the version of the wavelet after all deltas have been
   *        applied.
   * @param waveletName the name of the wavelet contained in the update.
   * @throws OperationException if the ops in the snapshot can not be applied.
   */
  public static ObservableWaveletData deserializeSnapshot(
      WaveletSnapshot snapshot, ProtocolHashedVersion version, WaveletName waveletName)
      throws OperationException {
    // TODO(ljvderijk): This method does too many steps to get to the
    // ObservableWaveletData.
    // We need something simpler when operations and the protocol have been
    // edited.

    // TODO(ljvderijk): should be sent along in the new protocol
    DistinctVersion distinctVersion = DistinctVersion.of(version.getVersion(), 0);

    // Creating a CoreWaveletData because the current protocol lacks the
    // meta-data required to construct an ObservableWaveletData directly.
    // But this results in unnecessary object creation and copies.
    CoreWaveletData coreWavelet =
        new CoreWaveletDataImpl(waveletName.waveId, waveletName.waveletId);

    Preconditions.checkArgument(snapshot.getParticipantIdCount() > 0);
    // Have to add a single participant for the copying to complete without a
    // NPE.
    coreWavelet.addParticipant(ParticipantId.ofUnsafe(snapshot.getParticipantId(0)));

    for (DocumentSnapshot document : snapshot.getDocumentList()) {
      BufferedDocOp op =
          CoreWaveletOperationSerializer.deserialize(document.getDocumentOperation());
      coreWavelet.modifyDocument(document.getDocumentId(), op);
    }

    ObservableWaveletData immutableWaveletData =
        DataUtil.fromCoreWaveletData(coreWavelet, distinctVersion, SchemaCollection.empty());

    ObservableWaveletData wavelet =
        WaveletDataImpl.Factory.create(DOCUMENT_FACTORY).create(immutableWaveletData);

    for (String participant : snapshot.getParticipantIdList()) {
      wavelet.addParticipant(new ParticipantId(participant));
    }

    return wavelet;
  }

}
