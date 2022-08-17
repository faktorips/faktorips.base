/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.faktorips.devtools.core.ui.LinkDropListener;
import org.faktorips.devtools.core.ui.views.IpsElementDropListener;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

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
            productStructureExplorer.showStructure((IIpsSrcFile)transferred[0]);
        }
    }

    @Override
    public void dropAccept(DropTargetEvent event) {
        Object[] transferred = super.getTransferedElements(event.currentDataType);
        // in linux transferred is always null while drag action
        if (transferred == null || transferred.length > 0 && transferred[0] instanceof IIpsSrcFile
                && productStructureExplorer.isSupported((IIpsSrcFile)transferred[0])) {
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
