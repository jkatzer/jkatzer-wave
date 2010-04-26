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
 */

package org.waveprotocol.wave.model.operation.wave;

import org.waveprotocol.wave.model.document.operation.BufferedDocOp;
import org.waveprotocol.wave.model.document.operation.algorithm.DocOpInverter;
import org.waveprotocol.wave.model.document.operation.impl.DocOpBuffer;
import org.waveprotocol.wave.model.operation.OperationException;
import org.waveprotocol.wave.model.util.Preconditions;
import org.waveprotocol.wave.model.wave.data.WaveletData;

/**
 * Operation class for an operation that will modify a document within a given wavelet.
 */
public final class WaveletDocumentOperation extends WaveletOperation {
  /** Identifier of the document within the target wavelet to modify. */
  private final String documentId;

  /** Document operation which modifies the target document. */
  private final BufferedDocOp operation;

  /**
   * Constructor.
   *
   * @param documentId
   * @param operation
   */
  public WaveletDocumentOperation(String documentId, BufferedDocOp operation) {
    Preconditions.checkNotNull(documentId, "Null document id");
    Preconditions.checkNotNull(operation, "Null document operation");
    this.documentId = documentId;
    this.operation = operation;
  }

  public String getDocumentId() {
    return documentId;
  }

  public BufferedDocOp getOperation() {
    return operation;
  }

  @Override
  protected void doApply(WaveletData target) throws OperationException {
    target.modifyDocument(documentId, operation);
  }

  @Override
  public WaveletOperation getInverse() {
    DocOpInverter<BufferedDocOp> inverse = new DocOpInverter<BufferedDocOp>(new DocOpBuffer());
    operation.apply(inverse);
    return new WaveletDocumentOperation(documentId, inverse.finish());
  }

  @Override
  public String toString() {
    return "WaveletDocumentOperation(" + documentId + "," + operation + ")";
  }
}
