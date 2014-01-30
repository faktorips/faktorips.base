/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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