/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.LinkDropListener;
import org.faktorips.devtools.core.ui.views.IpsElementDropListener;

/**
 * This drop listener is only responsible to show new objects not to drag into the structure. For
 * creating new links {@link LinkDropListener} is used.
 * 
 * @author dirmeier
 */
class ProductCmptDropListener extends IpsElementDropListener {

    /**
     * Comment for <code>productStructureExplorer</code>
     */
    private final ProductStructureExplorer productStructureExplorer;

    ProductCmptDropListener(ProductStructureExplorer productStructureExplorer) {
        this.productStructureExplorer = productStructureExplorer;
    }

    @Override
    public void dragEnter(DropTargetEvent event) {
        dropAccept(event);
    }

    @Override
    public void drop(DropTargetEvent event) {
        Object[] transferred = super.getTransferedElements(event.currentDataType);
        if (transferred.length > 0 && transferred[0] instanceof IIpsSrcFile) {
            try {
                this.productStructureExplorer.showStructure((IIpsSrcFile)transferred[0]);
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
    }

    @Override
    public void dropAccept(DropTargetEvent event) {
        Object[] transferred = super.getTransferedElements(event.currentDataType);
        // in linux transferred is always null while drag action
        if (transferred == null || transferred.length > 0 && transferred[0] instanceof IIpsSrcFile
                && this.productStructureExplorer.isSupported((IIpsSrcFile)transferred[0])) {
            event.detail = DND.DROP_LINK;
        } else {
            event.detail = DND.DROP_NONE;
        }
    }

    @Override
    public int getSupportedOperations() {
        return DND.DROP_LINK;
    }

}