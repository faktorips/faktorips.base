/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.ui.util.LinkCreatorUtil;

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

    @Override
    public boolean validateDrop(Object target, int operation, TransferData transferType) {
        if (target == null) {
            return false;
        }
        List<IProductCmpt> draggedCmpts = getTransferElements(transferType);
        if (draggedCmpts == null) {
            return false;
        }
        // Linux bug - @see comment of getTransferElements(..)
        if (draggedCmpts.isEmpty()) {
            return true;
        }
        try {
            if (target instanceof IProductCmptStructureReference) {
                IProductCmptStructureReference structureReference = (IProductCmptStructureReference)target;
                boolean result = linkCreator.canCreateLinks(structureReference, draggedCmpts);
                return result;
            } else {
                return false;
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    @Override
    public boolean performDrop(Object data) {
        if (getCurrentOperation() == DND.DROP_LINK && data instanceof String[]) {
            List<IProductCmpt> droppedCmpts = getProductCmpts((String[])data);
            Object target = getCurrentTarget();
            if (target instanceof IProductCmptStructureReference) {
                IProductCmptStructureReference structureReference = (IProductCmptStructureReference)target;
                return linkCreator.createLinks(droppedCmpts, structureReference);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Changing the link creator for testing
     */
    public void setLinkCreator(LinkCreatorUtil linkCreator) {
        this.linkCreator = linkCreator;
    }
}
