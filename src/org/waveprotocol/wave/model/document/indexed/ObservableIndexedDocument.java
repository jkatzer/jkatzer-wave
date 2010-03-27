// Copyright 2009 Google Inc. All Rights Reserved.

package org.waveprotocol.wave.model.document.indexed;

import org.waveprotocol.wave.model.document.indexed.DocumentEvent.AnnotationChanged;
import org.waveprotocol.wave.model.document.indexed.DocumentEvent.AttributesModified;
import org.waveprotocol.wave.model.document.indexed.DocumentEvent.ContentDeleted;
import org.waveprotocol.wave.model.document.indexed.DocumentEvent.ContentInserted;
import org.waveprotocol.wave.model.document.indexed.DocumentEvent.TextDeleted;
import org.waveprotocol.wave.model.document.indexed.DocumentEvent.TextInserted;
import org.waveprotocol.wave.model.document.operation.Attributes;
import org.waveprotocol.wave.model.document.operation.AttributesUpdate;
import org.waveprotocol.wave.model.document.operation.automaton.DocumentSchema;
import org.waveprotocol.wave.model.document.raw.RawDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * Indexed document that generates events on mutation.
 *
 * The events generated by the application of a *single* op contain
 * efficiently represented information, sufficient to reverse it.
 * (for the mutation components implemented).
 *
 * TODO(danilatos): split/join & annotations.
 *
 * Events are simple inserts, deletes, and attribute updates
 * (splits/joins are expressed as inserts and deletes).
 *
 * There is much low-hanging fruit here for optimisation...
 *
 * @author danilatos@google.com (Daniel Danilatos)
 */
public class ObservableIndexedDocument<N, E extends N, T extends N, V>
    extends IndexedDocumentImpl<N, E, T, V> {

  private int depth = 0;

  private final List<EventBundleImpl.Builder<N, E, T>> events
      = new ArrayList<EventBundleImpl.Builder<N, E, T>>();

  private final List<E> splitElementStack = new ArrayList<E>();

  private final DocumentHandler<N, E, T> handler;

  /** Deletion-event builder.  Non-null in the scope of an element deletion. */
  private ContentDeleted.Builder<N, E, T> deletion;

  /**
   * Call this constructor when you have your own annotation set to pass in.
   *
   * @param handler handler for document events
   * @param substrate document substrate
   * @param annotations annotation set to use
   * @param schema schema to use for this document
   */
  public ObservableIndexedDocument(
      DocumentHandler<N, E, T> handler,
      RawDocument<N, E, T> substrate,
      RawAnnotationSet<Object> annotations,
      DocumentSchema schema) {
    super(substrate, annotations, schema);

    this.handler = handler;
  }

  private static final Object ONE_OBJECT = new Object();
  private static final Object ANOTHER_OBJECT = new Object();

  /**
   * Calling this constructor (where you don't pass your own annotation set)
   * causes one to built internally.
   *
   * @param handler handler for document events
   * @param substrate document substrate
   * @param schema schema to use for this document
   */
  public ObservableIndexedDocument(
      DocumentHandler<N, E, T> handler,
      RawDocument<N, E, T> substrate,
      DocumentSchema schema) {
    // We have to chain constructors here since we want to access the
    // annotation tree after building it so that we can set its listener
    this(handler, substrate, new AnnotationTree<Object>(ONE_OBJECT, ANOTHER_OBJECT, null), schema);
  }

  private ObservableIndexedDocument(
      DocumentHandler<N, E, T> handler,
      RawDocument<N, E, T> substrate,
      AnnotationTree<Object> annotations,
      DocumentSchema schema) {
    super(substrate, annotations, schema);

    AnnotationSetListener<Object> listener = new AnnotationSetListener<Object>() {
      @Override
      public void onAnnotationChange(int start, int end, String key, Object newValue) {
        event(new AnnotationChanged<N, E, T>(start, end, key, (String) newValue));
      }
    };

    annotations.setListener(listener);
    this.handler = handler;
  }

  @Override
  protected void beforeBegin() {
    assert depth == 0;
    assert events.isEmpty();
    assert splitElementStack.isEmpty();

    push();
  }

  @Override
  protected void afterFinish() {
    assert events.size() == 1;
    assert depth == 0;
    assert splitElementStack.isEmpty();

    if (handler != null) {
      handler.onDocumentEvents(events.get(0).build());
    }

    events.clear();
  }


  @Override
  protected void onElementStart(E element) {

    if (depth == 0) {
      event(new ContentInserted<N, E, T>(element));
    }

    // Remember the element just inserted, to include in event.
    inserted(element);

    depth++;
  }

  @Override
  protected void onElementEnd() {
    depth--;
  }

  @Override
  protected void onDeleteElementStart(int location, E element) {
    assert (deletion == null) == (depth == 0);

    if (depth == 0) {
      deletion = new ContentDeleted.Builder<N, E, T>(location);
    }
    depth++;

    // Save the element about to be deleted.
    deleted(element);
    deletion.addElementStart(getTagName(element), getAttributes(element));
  }

  @Override
  protected void onDeleteElementEnd() {
    assert deletion != null;
    deletion.addElementEnd();

    depth--;

    if (depth == 0) {
      event(deletion.build());
      deletion = null;
    }
  }

  @Override
  protected void onModifyAttributes(E element, AttributesUpdate update) {
    event(new AttributesModified<N, E, T>(element, update));
  }

  @Override
  protected void onModifyAttributes(E element, Attributes oldAttributes, Attributes newAttributes) {
    event(new AttributesModified<N, E, T>(element, oldAttributes, newAttributes));
  }

  @Override
  protected void onCharacters(int location, String characters) {
    if (depth == 0) {
      event(new TextInserted<N, E, T>(location, characters));
    }
  }

  @Override
  protected void onDeleteCharacters(int location, String characters) {
    if (depth == 0) {
      // TODO(danilatos): Drop the TextDeleted event altogether.
      event(new TextDeleted<N, E, T>(location, characters));
    }
    if (deletion != null) {
      deletion.addText(characters);
    }
  }

  private void inserted(E element) {
    currentEvent().addInsertedElement(element);
  }

  private void deleted(E element) {
    currentEvent().addDeletedElement(element);
  }

  private void event(DocumentEvent<N, E, T> event) {
    currentEvent().addComponent(event);
  }

  private EventBundleImpl.Builder<N, E, T> currentEvent() {
    return events.get(events.size() - 1);
  }

  private void push() {
    events.add(new EventBundleImpl.Builder<N, E, T>());
  }
}
