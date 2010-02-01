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

package org.faktorips.devtools.core.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Item;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.core.ui.util.LinkCreatorUtil;

public class LinkDropListener extends ViewerDropAdapter {

    private TransferData actualTransferType;

    private List<IProductCmpt> actualTransferElements;

    private LinkCreatorUtil linkCreator;

    private IProductCmptLink movedCmptLink;

    public LinkDropListener(Viewer viewer) {
        super(viewer);
        setFeedbackEnabled(false);
        setLinkCreator(new LinkCreatorUtil(true));
    }

    /**
     * @param autoSave The autoSave to set.
     */
    public void setAutoSave(boolean autoSave) {
        linkCreator.setAutoSave(autoSave);
    }

    /**
     * @return Returns the autoSave.
     */
    public boolean isAutoSave() {
        return linkCreator.isAutoSave();
    }

    @Override
    public void dragEnter(DropTargetEvent event) {
        if (event.detail == DND.DROP_NONE) {
            if (movedCmptLink == null) {
                event.detail = DND.DROP_LINK;
            } else {
                event.detail = DND.DROP_MOVE;
            }
        }
        if (event.detail == DND.DROP_MOVE && movedCmptLink == null) {
            event.detail = DND.DROP_LINK;
        }
        super.dragEnter(event);
    }

    @Override
    public boolean validateDrop(Object target, int operation, TransferData transferType) {
        if (target == null) {
            return false;
        }
        if (movedCmptLink != null) {
            if ((target instanceof IProductCmptLink || target instanceof IProductCmptTypeAssociationReference)
                    && (getCurrentLocation() == LOCATION_BEFORE || getCurrentLocation() == LOCATION_AFTER)) {
                boolean result = canMove(target);
                setFeedbackEnabled(result);
                return result;
            } else {
                return false;
            }
        } else {
            setFeedbackEnabled(false);
            List<IProductCmpt> draggedCmpts = getTransferElements(transferType);
            if (draggedCmpts == null) {
                return false;
            }
            // Linux bug - @see comment of getTransferElements(..)
            if (draggedCmpts.isEmpty()) {
                return true;
            }
            try {
                boolean result = getLinkCreator().canCreateLinks(target, draggedCmpts);
                return result;
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return false;
            }
        }
    }

    @Override
    public boolean performDrop(Object data) {
        if (getCurrentOperation() == DND.DROP_MOVE && movedCmptLink != null) {
            Object target = getCurrentTarget();
            return moveLink(target);
        } else if (getCurrentOperation() == DND.DROP_LINK && data instanceof String[]) {
            List<IProductCmpt> droppedCmpts = getProductCmpts((String[])data);
            Object target = getCurrentTarget();
            return getLinkCreator().createLinks(droppedCmpts, target);
        } else {
            return false;
        }
    }

    private boolean canMove(Object target) {
        if (target instanceof IProductCmptLink) {
            IProductCmptLink targetCmptLink = (IProductCmptLink)target;
            if (targetCmptLink.getAssociation().equals(movedCmptLink.getAssociation())) {
                return true;
            }
        }
        try {
            List<IProductCmpt> draggedCmpts = new ArrayList<IProductCmpt>();
            draggedCmpts.add(movedCmptLink.findTarget(movedCmptLink.getIpsProject()));
            return getLinkCreator().canCreateLinks(target, draggedCmpts);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    private boolean moveLink(Object target) {
        if (target instanceof IProductCmptTypeAssociationReference) {
            IProductCmptTypeAssociationReference associationReference = (IProductCmptTypeAssociationReference)target;
            movedCmptLink.setAssociation(associationReference.getAssociation().getName());
            return true;
        } else if (target instanceof IProductCmptLink) {
            IProductCmptLink targetLink = (IProductCmptLink)target;
            IProductCmptGeneration generation = targetLink.getProductCmptGeneration();
            boolean before = getCurrentLocation() == LOCATION_BEFORE;
            generation.moveLink(movedCmptLink, targetLink, before);
            return true;
        } else {
            return false;
        }
    }

    private IFile getFile(String filename) {
        return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(filename));
    }

    private IProductCmpt getProductCmpt(IIpsElement element) throws CoreException {
        if (element instanceof IIpsSrcFile
                && ((IIpsSrcFile)element).getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT)) {
            return (IProductCmpt)((IIpsSrcFile)element).getIpsObject();
        } else {
            return null;
        }
    }

    /**
     * Get the transfered (dragged) elements. Returns null if there are no elements, the transfer
     * type is wrong or there is at least one invalid element.
     * <p/>
     * On some systems it is not possible to determine the concrete object while dragging. In this
     * case the method returns an empty array. (e.g. linux)
     * 
     * @param transferType
     * @return
     */
    private List<IProductCmpt> getTransferElements(TransferData transferType) {
        if (transferType == null) {
            return null;
        }
        if (transferType.equals(actualTransferType)) {
            return actualTransferElements;
        }
        if (FileTransfer.getInstance().isSupportedType(transferType)) {
            String[] filenames = (String[])FileTransfer.getInstance().nativeToJava(transferType);
            List<IProductCmpt> productCmpts = getProductCmpts(filenames);
            if (productCmpts != null) {
                actualTransferElements = productCmpts;
                actualTransferType = transferType;
                return actualTransferElements;
            }
        }
        actualTransferType = null;
        actualTransferElements = null;
        return null;
    }

    private List<IProductCmpt> getProductCmpts(String[] filenames) {
        // Under some platforms (linux), the data is not available during dragOver.
        if (filenames == null) {
            return new ArrayList<IProductCmpt>();
        }

        List<IProductCmpt> result = new ArrayList<IProductCmpt>();

        for (int i = 0; i < filenames.length; i++) {
            IFile file = getFile(filenames[i]);
            if (file == null) {
                return null;
            }
            try {
                IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(file);
                if (element == null || !element.exists()) {
                    return null;
                }
                IProductCmpt draggedCmpt = getProductCmpt(element);
                if (draggedCmpt == null) {
                    return null;
                }
                result.add(draggedCmpt);
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return null;
            }
        }
        return result;
    }

    /**
     * @param linkCreator The linkCreator to set.
     */
    public void setLinkCreator(LinkCreatorUtil linkCreator) {
        this.linkCreator = linkCreator;
    }

    /**
     * @return Returns the linkCreator.
     */
    public LinkCreatorUtil getLinkCreator() {
        return linkCreator;
    }

    /**
     * Override the determineLocation method because we have only location after or location before
     * when moving an element. When D&N is not in moving mode, we do not have location feedback, but
     * we although return the normal determined location.
     * 
     * {@inheritDoc}
     */
    @Override
    protected int determineLocation(DropTargetEvent event) {
        if (movedCmptLink == null) {
            return super.determineLocation(event);
        }
        if (!(event.item instanceof Item)) {
            return LOCATION_NONE;
        }
        // dropping on an associationReference means moving on first position of this node
        if (getCurrentTarget() instanceof IProductCmptTypeAssociationReference) {
            return LOCATION_AFTER;
        }
        Item item = (Item)event.item;
        Point coordinates = new Point(event.x, event.y);
        coordinates = getViewer().getControl().toControl(coordinates);
        if (item != null) {
            Rectangle bounds = getBounds(item);
            int offset = bounds.height / 2;
            if (bounds == null) {
                return LOCATION_NONE;
            }
            if ((coordinates.y - bounds.y) < offset) {
                return LOCATION_BEFORE;
            }
            if ((bounds.y + bounds.height - coordinates.y) < offset) {
                return LOCATION_AFTER;
            }
        }
        return LOCATION_ON;
    }

    public void setToMove(IProductCmptLink selected) {
        movedCmptLink = selected;
    }

}
