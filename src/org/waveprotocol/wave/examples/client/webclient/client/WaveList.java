/**
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package org.waveprotocol.wave.examples.client.webclient.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

import org.waveprotocol.wave.examples.client.common.IndexEntry;
import org.waveprotocol.wave.examples.client.webclient.client.events.UserLoginEvent;
import org.waveprotocol.wave.examples.client.webclient.client.events.UserLoginEventHandler;
import org.waveprotocol.wave.examples.client.webclient.client.events.WaveCreationEvent;
import org.waveprotocol.wave.examples.client.webclient.client.events.WaveIndexUpdatedEvent;
import org.waveprotocol.wave.examples.client.webclient.client.events.WaveIndexUpdatedEventHandler;
import org.waveprotocol.wave.examples.client.webclient.client.events.WaveSelectionEvent;
import org.waveprotocol.wave.examples.client.webclient.client.events.WaveSelectionEventHandler;
import org.waveprotocol.wave.examples.client.webclient.util.Log;
import org.waveprotocol.wave.model.id.WaveId;

import java.util.List;

public class WaveList extends Composite {
  interface Binder extends UiBinder<Widget, WaveList> {
  }

  interface Style extends CssResource {
    String entry();
    String selected();
    String updated();
  }

  private static final Binder BINDER = GWT.create(Binder.class);
  private static final Log LOG = Log.get(WaveList.class);
  private static final int MAX_SNIPPET_LENGTH = 64;
  private static final String WAVE_ID_PREFIX = "Wave_";

  private Element currentSelection;
  private WaveId pendingSelectionId;

  @UiField HTMLPanel panel;
  @UiField Style style;
  @UiField Button newWaveButton;

  public WaveList() {
    initWidget(BINDER.createAndBindUi(this));

    addDomHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Element elt = event.getNativeEvent().getEventTarget().cast();
        String id = elt.getId();
        while (!id.startsWith(WAVE_ID_PREFIX)) {
          elt = elt.getParentElement();
          if (elt == null || elt == panel.getElement()) {
            return;
          }
          id = elt.getId();
        }
        event.stopPropagation();
        String serializedWaveId = elt.getId().substring(WAVE_ID_PREFIX.length());
        WaveId waveId = WaveId.deserialise(serializedWaveId);
        ClientEvents.get().fireEvent(new WaveSelectionEvent(waveId));
      }
    }, ClickEvent.getType());

    ClientEvents.get().addWaveSelectionEventHandler(
        new WaveSelectionEventHandler() {
          @Override
          public void onSelection(WaveId id) {
            LOG.info("WaveList changing selection to " + id.toString());
            select(id);
          }
        });
    ClientEvents.get().addWaveIndexUpdatedEventHandler(
        new WaveIndexUpdatedEventHandler() {
          @Override
          public void onWaveIndexUpdate(WaveIndexUpdatedEvent event) {
            LOG.info("WaveList refreshing due to index update");
            update(event.getEntries());
          }
        });
    ClientEvents.get().addUserLoginEventHandler(new UserLoginEventHandler() {
      @Override
      public void onUserLoginSuccess(UserLoginEvent event) {
        newWaveButton.setEnabled(true);
      }
    });
  }

  @UiHandler("newWaveButton")
  void newWave(ClickEvent event) {
    ClientEvents.get().fireEvent(WaveCreationEvent.CREATE_NEW_WAVE);
  }

  private void select(WaveId id) {
    if (currentSelection != null) {
      currentSelection.removeClassName(style.selected());
    }
    String serializedId = id.serialise();
    String eltId = WAVE_ID_PREFIX + serializedId;
    Element elt = panel.getElementById(eltId);
    currentSelection = elt;
    if (elt != null) {
      elt.removeClassName(style.updated());
      elt.addClassName(style.selected());
      pendingSelectionId = null;
    } else {
      pendingSelectionId = id;
    }
  }

  private void update(List<IndexEntry> entries) {
    for (IndexEntry entry : entries) {
      String serializedId = entry.getWaveId().serialise();
      String eltId = WAVE_ID_PREFIX + serializedId;
      Element elt = panel.getElementById(eltId);

      if (elt != null) {
        elt.addClassName(style.updated());
      } else {
        elt = Document.get().createDivElement();
        elt.setClassName(style.entry() + " " + style.updated());
        elt.setId(eltId);
        panel.getElement().appendChild(elt);
      }

      String digest = entry.getDigest();
      if (digest.trim().isEmpty()) {
        digest = "(empty)";
      }

      elt.setInnerText(digest.substring(0, Math.min(digest.length(),
          MAX_SNIPPET_LENGTH)));

      if (entry.getWaveId().equals(pendingSelectionId)) {
        select(entry.getWaveId());
      }
    }
  }
}
