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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.DummyRoot;

public class ProductCmptDragListener implements DragSourceListener {

    StructuredViewer dragSource;
    
    public ProductCmptDragListener(StructuredViewer dragSource) {
        this.dragSource = dragSource;
    }
    
    public void dragStart(DragSourceEvent event) {
        Object selected = ((IStructuredSelection)dragSource.getSelection()).getFirstElement();
        if (selected instanceof IProductCmpt || selected instanceof DummyRoot) {
            event.doit = true;
        }
        else {
            event.doit = false;
        }
        
    }

    public void dragSetData(DragSourceEvent event) {
        Object selected = ((IStructuredSelection)dragSource.getSelection()).getFirstElement();
        if (selected instanceof IProductCmpt) {
            event.data = ((IProductCmpt)selected).getQualifiedName();
        }
        else if (selected instanceof DummyRoot) {
            event.data = ((DummyRoot)selected).data.getQualifiedName();
        }
    }

    public void dragFinished(DragSourceEvent event) {
        // nothing to do
    }
    
}
