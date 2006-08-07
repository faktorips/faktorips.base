/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.internal.model.IpsObjectPartState;
import org.faktorips.devtools.core.model.IIpsObjectPart;

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

    public void run(IStructuredSelection selection) {
        List selectedObjects = selection.toList();

        List removedObjects = new ArrayList();
        IIpsObjectPart part;
        for (Iterator iter = selectedObjects.iterator(); iter.hasNext();) {
            Object selected = iter.next();

            if (selected instanceof IIpsObjectPart) {
                part = (IIpsObjectPart)selected;
                removedObjects.add(new IpsObjectPartState(part).toString());
                part.delete();
            }
        }
        
        if (removedObjects.size() > 0) {
        	ArrayList emptyList = new ArrayList(0);
            clipboard.setContents(getDataArray(removedObjects, emptyList), getTypeArray(removedObjects, emptyList));
        }
    	throw new RuntimeException("Forced exception to test feature requested by #326. 2");
    }
}
