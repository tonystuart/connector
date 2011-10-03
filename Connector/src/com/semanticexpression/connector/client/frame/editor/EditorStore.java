// Copyright 2011 Semantic Expression, Inc. All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the GNU General Public License, either version 3 or (at your option)
// any later version. The terms of this license may be found at
// http://www.gnu.org/copyleft/gpl.html
//
// This program is made available on an "as is" basis, without warranties or
// conditions of any kind, either express or implied.
//
// Please contact us for other licensing options.
//
// Contributors:
//
// Anthony F. Stuart - Initial implementation
//
//
//

package com.semanticexpression.connector.client.frame.editor;

import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.frame.editor.EditorFrame.ModificationContext;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Sequence;

public class EditorStore extends ListStore<Association>
{
  private ContentReference contentReference;
  private EditorStoreListener editorStoreListener;
  private boolean isSuppressModifyEvent;
  private String keyName;
  private ModificationContext modificationContext;
  private Sequence<Association> sequence;

  public EditorStore(ModificationContext modificationContext)
  {
    this.modificationContext = modificationContext;

    setMonitorChanges(true);
    addStoreListener(getEditorStoreListener());
  }

  private EditorStoreListener getEditorStoreListener()
  {
    if (editorStoreListener == null)
    {
      editorStoreListener = new EditorStoreListener();
    }
    return editorStoreListener;
  }

  public void initialize(ContentReference contentReference, String keyName)
  {
    this.contentReference = contentReference;
    this.keyName = keyName;

    isSuppressModifyEvent = true;

    removeAll();

    sequence = contentReference.get(keyName);
    if (sequence != null)
    {
      add(sequence);
    }

    isSuppressModifyEvent = false;
  }

  public boolean isSuppressModifyEvent()
  {
    return isSuppressModifyEvent;
  }

  public void setSuppressModifyEvent(boolean isSuppressModifyEvent)
  {
    this.isSuppressModifyEvent = isSuppressModifyEvent;
  }

  private final class EditorStoreListener extends StoreListener<Association>
  {
    @Override
    public void handleEvent(StoreEvent<Association> e)
    {
      if (!isSuppressModifyEvent)
      {
        super.handleEvent(e);
      }
    }

    @Override
    public void storeAdd(StoreEvent<Association> storeEvent)
    {
      if (sequence == null)
      {
        sequence = new Sequence<Association>();
        sequence.setTrackChanges(true);
        contentReference.set(keyName, sequence);
      }

      List<Association> associations = Utility.getStoreEventModels(storeEvent);
      int offset = storeEvent.getIndex();
      for (Association association : associations)
      {
        sequence.add(offset++, association);
      }

      modificationContext.onModify();
    }

    @Override
    public void storeClear(StoreEvent<Association> se)
    {
      if (sequence != null)
      {
        sequence.clear();
      }
    }

    @Override
    public void storeRemove(StoreEvent<Association> storeEvent)
    {
      List<Association> associations = Utility.getStoreEventModels(storeEvent);
      for (Association association : associations)
      {
        sequence.remove(association);
      }

      modificationContext.onModify();
    }

    @Override
    public void storeUpdate(StoreEvent<Association> se)
    {
      modificationContext.onModify();
    }
  }
}