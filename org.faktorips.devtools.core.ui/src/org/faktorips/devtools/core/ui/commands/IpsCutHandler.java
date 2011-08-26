/***************************************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.ui.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartState;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * A handler to cut IpsObjectPartContainer-objects out of the model into the clipboard.
 */
public class IpsCutHandler extends IpsAbstractHandler {

    @Override
    public void execute(ExecutionEvent event, IWorkbenchPage activePage, IIpsSrcFile ipsSrcFile)
            throws ExecutionException {
        IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getCurrentSelectionChecked(event);
        Clipboard clipboard = new Clipboard(HandlerUtil.getActiveShellChecked(event).getDisplay());

        cutToClipboard(selection, clipboard);
    }

    public void cutToClipboard(IStructuredSelection selection, Clipboard clipboard) {
        List<String> removedObjects = new ArrayList<String>();
        IIpsObjectPart part;
        for (Iterator<Object> iter = getSelectionIterator(selection); iter.hasNext();) {
            Object selected = iter.next();
            if (selected instanceof IIpsObjectPart) {
                part = (IIpsObjectPart)selected;
                removedObjects.add(new IpsObjectPartState(part).toString());
                part.delete();
            }
        }

        if (removedObjects.size() > 0) {
            ArrayList<IResource> emptyList = new ArrayList<IResource>(0);
            clipboard.setContents(getDataArray(removedObjects, emptyList, null),
                    getTypeArray(removedObjects, emptyList, null));
        }
    }

}
