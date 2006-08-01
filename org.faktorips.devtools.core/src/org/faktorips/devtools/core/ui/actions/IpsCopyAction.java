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

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.internal.model.IpsObjectPartState;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPart;

/**
 * Copy of objects controlled by FaktorIps. 
 * 
 * @author Thorsten Guenther
 */
public class IpsCopyAction extends IpsAction {

    private Clipboard clipboard;
    
    public IpsCopyAction(ISelectionProvider selectionProvider, Shell shell) {
        super(selectionProvider);
        clipboard = new Clipboard(shell.getDisplay());
    }
    

    public void run(IStructuredSelection selection) {
        List selectedObjects = selection.toList();

        List copiedObjects = new ArrayList();
        List copiedResources = new ArrayList();
        IIpsObjectPart part;
        for (Iterator iter = selectedObjects.iterator(); iter.hasNext();) {
            Object selected = iter.next();

            if (selected instanceof IIpsObjectPart) {
                part = (IIpsObjectPart)selected;
                // TODO to be refactored with IpsDeleteAction, when inserting and deleting of attributes is allowed. See FS#330
//                copiedObjects.add(new IpsObjectPartState(part).toString());
                
            }
            else if (selected instanceof IIpsElement) {
            	
            	IResource resource = ((IIpsElement)selected).getEnclosingResource();
            	if (resource != null) {
            		copiedResources.add(resource);
            	}
            }
        }

        if (copiedObjects.size() > 0 || copiedResources.size() > 0) {
            clipboard.setContents(getDataArray(copiedObjects, copiedResources), getTypeArray(copiedObjects, copiedResources));
        }
    }

    public void setEnabled(boolean enabled) {
    	super.setEnabled(enabled);
    }
}
