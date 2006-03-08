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

package org.faktorips.devtools.core.ui.views;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IpsObjectType;

public class ProductCmptDropListener implements DropTargetListener {

    StructuredViewer dropTarget;
    
    public ProductCmptDropListener(StructuredViewer dropTarget) {
        this.dropTarget = dropTarget;
    }
    
    public void dragEnter(DropTargetEvent event) {
        event.detail = DND.DROP_LINK;
    }

    public void dragLeave(DropTargetEvent event) {
        // nothing to do
    }

    public void dragOperationChanged(DropTargetEvent event) {
        // nothing to do
    }

    public void dragOver(DropTargetEvent event) {
        // nothing to do
    }

    public void drop(DropTargetEvent event) {
        try {
            dropTarget.setInput(IpsPlugin.getDefault().getIpsModel().getIpsProjects()[0].findIpsObject(IpsObjectType.PRODUCT_CMPT, (String)event.data));
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

    public void dropAccept(DropTargetEvent event) {
        event.detail = DND.DROP_LINK;
        
    }
}
