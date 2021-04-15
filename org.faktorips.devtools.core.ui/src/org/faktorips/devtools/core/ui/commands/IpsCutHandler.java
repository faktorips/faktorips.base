/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartState;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;

/**
 * A handler to cut IpsObjectPartContainer-objects out of the model into the clipboard.
 */
public class IpsCutHandler extends AbstractCopyPasteHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getCurrentSelectionChecked(event);
        Clipboard clipboard = new Clipboard(HandlerUtil.getActiveShellChecked(event).getDisplay());
        cutToClipboard(selection, clipboard);
        return null;
    }

    public void cutToClipboard(IStructuredSelection selection, Clipboard clipboard) {
        List<IpsObjectPartState> removedObjects = new ArrayList<>();
        IIpsObjectPart part;
        for (Iterator<Object> iter = getSelectionIterator(selection); iter.hasNext();) {
            Object selected = iter.next();
            if (selected instanceof IIpsObjectPart) {
                part = (IIpsObjectPart)selected;
                removedObjects.add(new IpsObjectPartState(part));
                part.delete();
            }
        }

        if (removedObjects.size() > 0) {
            ArrayList<IResource> emptyList = new ArrayList<>(0);
            clipboard.setContents(getDataArray(removedObjects, emptyList, null),
                    getTypeArray(removedObjects, emptyList, null));
        }
    }

}
