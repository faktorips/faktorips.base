/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartState;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

/**
 * An action to cut IpsObjectPartContainer-objects out of the model into the clipboard.
 * 
 * @author Thorsten Guenther
 */
public class IpsCutAction extends IpsAction {

    private Clipboard clipboard;

    public IpsCutAction(ISelectionProvider selectionProvider, Shell shell) {
        super(selectionProvider);
        clipboard = new Clipboard(shell.getDisplay());
    }

    @Override
    public void run(IStructuredSelection selection) {
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
            clipboard.setContents(getDataArray(removedObjects, emptyList, null), getTypeArray(removedObjects,
                    emptyList, null));
        }
    }
}
