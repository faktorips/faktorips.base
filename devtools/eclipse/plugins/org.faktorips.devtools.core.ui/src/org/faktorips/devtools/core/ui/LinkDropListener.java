/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.editors.productcmpt.LinkCandidateFilter;
import org.faktorips.devtools.core.ui.util.LinkCreatorUtil;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;

public class LinkDropListener extends IpsFileTransferViewerDropAdapter {

    private LinkCreatorUtil linkCreator;

    public LinkDropListener(Viewer viewer) {
        super(viewer);
        setFeedbackEnabled(false);
        linkCreator = new LinkCreatorUtil(true);
    }

    @Override
    public void dragEnter(DropTargetEvent event) {
        if (event.detail == DND.DROP_NONE || event.detail == DND.DROP_MOVE) {
            event.detail = DND.DROP_LINK;
        }
        super.dragEnter(event);
    }

    /**
     * Changing the link creator for testing
     */
    public void setLinkCreator(LinkCreatorUtil linkCreator) {
        this.linkCreator = linkCreator;
    }

    @Override
    public boolean validateDropSingle(Object target, int operation, TransferData data) {
        if (target == null) {
            return false;
        }
        List<IProductCmpt> draggedCmpts = getTransferElements(data);
        if (draggedCmpts == null) {
            return false;
        }
        // Linux bug - @see comment of getTransferElements(..)
        if (draggedCmpts.isEmpty()) {
            return true;
        }
        if (target instanceof IProductCmptStructureReference structureReference) {
            return canCreateLinks(draggedCmpts, structureReference);
        } else {
            return false;
        }
    }

    private boolean canCreateLinks(List<IProductCmpt> draggedCmpts, IProductCmptStructureReference structureReference) {
        LinkCandidateFilter filter = new LinkCandidateFilter(structureReference, IpsPlugin.getDefault()
                .getIpsPreferences().isWorkingModeBrowse());

        for (IProductCmpt productCmpt : draggedCmpts) {
            if (!filter.filter(productCmpt.getIpsSrcFile())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean performDropSingle(Object data) {
        if (getCurrentOperation() == DND.DROP_LINK && data instanceof String[]) {
            List<IProductCmpt> droppedCmpts = getProductCmpts((String[])data);
            Object target = getCurrentTarget();
            if (target instanceof IProductCmptStructureReference structureReference) {
                return linkCreator.createLinks(droppedCmpts, structureReference);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
