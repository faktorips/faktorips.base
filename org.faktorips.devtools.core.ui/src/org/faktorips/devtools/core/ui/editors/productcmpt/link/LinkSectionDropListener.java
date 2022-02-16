/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Item;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsFileTransferViewerDropAdapter;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.LinkDropListener;
import org.faktorips.devtools.core.ui.util.LinkCreatorUtil;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.SingleEventModification;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * Drop Listener for the link section. This drop listener is able to move elements within the link
 * section and could create new links for objects that are dragged from outside the link section.
 * <p>
 * This listener is very similar to the {@link LinkDropListener} which is responsible for
 * drag&amp;drop in the product structure view. Because of the different items provided by the
 * different content providers, these two listeners are separated.
 * 
 * @author dirmeier
 */
public class LinkSectionDropListener extends IpsFileTransferViewerDropAdapter {

    private List<IProductCmptLink> movedCmptLinks;
    private final IProductCmptGeneration generation;

    private final LinkCreatorUtil linkCreatorUtil = new LinkCreatorUtil(false);
    private final LinksSection linksSection;

    public LinkSectionDropListener(LinksSection linksSection, IProductCmptGeneration generation) {
        super(linksSection.getViewer());
        this.linksSection = linksSection;
        this.generation = generation;
    }

    @Override
    public void dragEnter(DropTargetEvent event) {
        if (event.detail == DND.DROP_NONE) {
            if (movedCmptLinks == null) {
                event.detail = DND.DROP_LINK;
            } else {
                event.detail = DND.DROP_MOVE;
            }
        }
        if (event.detail == DND.DROP_MOVE && movedCmptLinks == null) {
            event.detail = DND.DROP_LINK;
        }
        super.dragEnter(event);
    }

    // Overridden to make it accessible in test cases
    @Override
    protected List<IProductCmpt> getTransferElements(TransferData transferType) {
        return super.getTransferElements(transferType);
    }

    @Override
    public boolean validateDrop(Object target, int operation, TransferData transferType) {
        if (target == null | !linksSection.isDataChangeable()) {
            return false;
        }
        if (movedCmptLinks != null) {
            if ((target instanceof IProductCmptLink || target instanceof ILinkSectionViewItem)
                    && (getCurrentLocation() == LOCATION_BEFORE || getCurrentLocation() == LOCATION_AFTER)) {
                boolean result = canMove(target);
                return result;
            } else {
                return false;
            }
        } else {
            List<IProductCmpt> draggedCmpts = getTransferElements(transferType);
            if (draggedCmpts == null) {
                return false;
            }
            // Linux bug - see comment of getTransferElements(..)
            if (draggedCmpts.isEmpty()) {
                return true;
            }
            try {
                boolean result = canCreateLinks(draggedCmpts, target);
                return result;
            } catch (IpsException e) {
                IpsPlugin.log(e);
                return false;
            }
        }
    }

    @Override
    public boolean performDrop(final Object data) {
        SingleEventModification<List<IProductCmptLink>> modification = new LinkCreator(generation.getIpsSrcFile(),
                data);
        try {
            List<IProductCmptLink> result = ((IpsModel)IIpsModel.get())
                    .executeModificationsWithSingleEvent(modification);
            linksSection.setSelection(result);
            return !result.isEmpty();
        } catch (IpsException e) {
            IpsPlugin.log(e);
            return false;
        }

    }

    private boolean canMove(Object target) {
        // allow moving within the same association
        String associationName = getAssociationName(target);
        boolean result = false;
        List<IProductCmpt> draggedCmpts = new ArrayList<>();
        try {
            for (IProductCmptLink movedCmptLink : movedCmptLinks) {
                // move is valid if cmpt is moved within the same association or if a link in the
                // new association could be created
                if (associationName != null && associationName.equals(movedCmptLink.getAssociation())) {
                    result = true;
                } else {
                    draggedCmpts.add(movedCmptLink.findTarget(movedCmptLink.getIpsProject()));
                }
            }
            if (!draggedCmpts.isEmpty()) {
                result = canCreateLinks(draggedCmpts, target);
            }
            return result;
        } catch (IpsException e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    private boolean canCreateLinks(List<IProductCmpt> draggedCmpts, Object target) {
        // should only return true if all dragged cmpts are valid
        IProductCmptTypeAssociation association = getAssociation(target);
        boolean result = false;
        if (generation == null) {
            return false;
        }
        IProductCmptLinkContainer linkContainer = LinkCreatorUtil.getLinkContainerFor(generation, association);
        for (IProductCmpt draggedCmpt : draggedCmpts) {
            if (linkContainer.canCreateValidLink(draggedCmpt, association, generation.getIpsProject())) {
                result = true;
            } else {
                return false;
            }
        }
        return result;
    }

    /**
     * Override the determineLocation method because we have only location after or location before
     * when moving an element. When drag&amp;drop is not in moving mode, we do not have location
     * feedback. In that case we return the normal determined location instead.
     */
    @Override
    protected int determineLocation(DropTargetEvent event) {
        if (!(event.item instanceof Item)) {
            return LOCATION_NONE;
        }
        // dropping on an associationReference means moving on first position of this node
        if (getCurrentTarget() instanceof ILinkSectionViewItem) {
            return LOCATION_AFTER;
        }
        Item item = (Item)event.item;
        Point coordinates = new Point(event.x, event.y);
        coordinates = getViewer().getControl().toControl(coordinates);
        if (item != null) {
            Rectangle bounds = getBounds(item);
            int offset = bounds.height / 2;
            if ((coordinates.y - bounds.y) < offset) {
                return LOCATION_BEFORE;
            }
            if ((bounds.y + bounds.height - coordinates.y) < offset) {
                return LOCATION_AFTER;
            }
        }
        return LOCATION_ON;
    }

    protected void setToMove(List<IProductCmptLink> selectedLinks) {
        movedCmptLinks = selectedLinks;
    }

    private String getAssociationName(Object target) {
        String associationName = null;
        if (target instanceof IProductCmptLink) {
            IProductCmptLink targetCmptLink = (IProductCmptLink)target;
            associationName = targetCmptLink.getAssociation();
        } else if (target instanceof ILinkSectionViewItem) {
            associationName = ((ILinkSectionViewItem)target).getAssociationName();
        }
        return associationName;
    }

    private IProductCmptTypeAssociation getAssociation(Object target) {
        String associationName = getAssociationName(target);
        IProductCmptType type = generation.findProductCmptType(generation.getIpsProject());
        return (IProductCmptTypeAssociation)type.findAssociation(associationName, generation.getIpsProject());
    }

    @Override
    public boolean validateDropSingle(Object target, int operation, TransferData data) {
        return false;
    }

    @Override
    public boolean performDropSingle(Object data) {
        return false;
    }

    @Override
    protected TreeViewer getViewer() {
        return (TreeViewer)super.getViewer();
    }

    /**
     * This class helps creating or moving {@link IProductCmptLink}s at the link section.
     * <p>
     * Creating means dragging new {@link IProductCmptLink}s to the links section.
     * <p>
     * Moving means dragging existing {@link IProductCmptLink}s from an {@link AssociationViewItem}
     * to another.
     */
    private final class LinkCreator extends SingleEventModification<List<IProductCmptLink>> {

        private final Object data;
        private List<IProductCmptLink> result;

        private LinkCreator(IIpsSrcFile ipsSrcFile, Object data) {
            super(ipsSrcFile);
            this.data = data;
        }

        @Override
        protected boolean execute() {
            if (getCurrentOperation() == DND.DROP_MOVE && movedCmptLinks != null) {
                return moveLinks();
            } else if (getCurrentOperation() == DND.DROP_LINK && data instanceof String[]) {
                return createLinks();
            } else {
                return false;
            }
        }

        private boolean moveLinks() {
            Object target = getCurrentTarget();
            List<IProductCmptLink> listCopy = new ArrayList<>(movedCmptLinks);
            /*
             * If you drop a set of components you expect them in the same order as they were
             * selected. To achieve this, we need to inverse the list, if insertion is after a
             * component.
             */
            if (getCurrentLocation() == LOCATION_AFTER) {
                Collections.reverse(listCopy);
            }
            boolean moveResult = moveLinks(listCopy, target);
            if (moveResult) {
                result = movedCmptLinks;
            } else {
                result = Collections.emptyList();
            }
            return moveResult;
        }

        private boolean moveLinks(List<IProductCmptLink> links, Object target) {
            if (!canMove(target)) {
                return false;
            } else {
                for (IProductCmptLink movedLink : links) {
                    moveLink(movedLink, target);
                }
                return true;
            }
        }

        private boolean moveLink(IProductCmptLink link, Object target) {
            if (target instanceof AbstractAssociationViewItem) {
                String associationName = ((AbstractAssociationViewItem)target).getAssociationName();
                // move to first position of this association
                boolean moveResult = false;
                IProductCmptLinkContainer linkContainer = link.getProductCmptLinkContainer();
                for (IProductCmptLink firstTarget : linkContainer.getLinksAsList()) {
                    if (firstTarget.getAssociation().equals(associationName)) {
                        // first link of correct association type, move after this and break
                        moveResult = linkContainer.moveLink(link, firstTarget, true);
                        break;
                    }
                }
                // no link of this association type found, move to first position (if there is any)
                if (linkContainer.getLinksAsList().size() > 0) {
                    moveResult = linkContainer.moveLink(link, linkContainer.getLinksAsList().get(0), true);
                } else {
                    // if there is no element yet, move is ok
                    return true;
                }
                if (moveResult) {
                    // setting correct asscociation
                    link.setAssociation(associationName);
                }
                return moveResult;
            } else if (target instanceof LinkViewItem) {
                return moveLink(link, ((LinkViewItem)target).getLink());
            } else if (target instanceof IProductCmptLink) {
                IProductCmptLink targetLink = (IProductCmptLink)target;
                IProductCmptLinkContainer linkContainer = targetLink.getProductCmptLinkContainer();
                boolean before = getCurrentLocation() == LOCATION_BEFORE;
                return linkContainer.moveLink(link, targetLink, before);
            } else {
                return false;
            }
        }

        private boolean createLinks() {
            List<IProductCmpt> droppedCmpts = getProductCmpts((String[])data);
            /*
             * If you drop a set of components you expect them in the same order as they were
             * selected. To achieve this, we need to inverse the list, if insertion is after a
             * component.
             */
            if (getCurrentLocation() == LOCATION_AFTER) {
                Collections.reverse(droppedCmpts);
            }
            List<IProductCmptLink> createdCmptLinks = createLinks(droppedCmpts, getCurrentTarget());
            result = createdCmptLinks;
            return !createdCmptLinks.isEmpty();
        }

        private List<IProductCmptLink> createLinks(List<IProductCmpt> draggedCmpts, Object target)
                {
            if (!canCreateLinks(draggedCmpts, target)) {
                return Collections.emptyList();
            }

            ArrayList<IProductCmptLink> createdCmptLinks = new ArrayList<>();
            for (IProductCmpt draggedCmpt : draggedCmpts) {
                createdCmptLinks.add(createLink(draggedCmpt.getQualifiedName(), generation, target));
            }
            return createdCmptLinks;
        }

        private IProductCmptLink createLink(String droppedCmptQName, IProductCmptGeneration generation, Object target) {
            IProductCmptTypeAssociation association = getAssociation(target);
            if (generation != null && association != null && IpsUIPlugin.isEditable(generation.getIpsSrcFile())) {
                IProductCmptLink newLink = linkCreatorUtil.createLink(association, generation, droppedCmptQName);
                moveLink(newLink, target);
                return newLink;
            } else {
                return null;
            }
        }

        @Override
        protected List<IProductCmptLink> getResult() {
            return result;
        }

    }

    /**
     * Listener to handle the move of relations.
     * 
     * @author Cornelius Dirmeier
     */
    public class MoveLinkDragListener implements DragSourceListener {

        private ISelectionProvider selectionProvider;

        public MoveLinkDragListener(ISelectionProvider selectionProvider) {
            this.selectionProvider = selectionProvider;
        }

        @Override
        public void dragStart(DragSourceEvent event) {
            // we provide the event data yet so we can decide if we will
            // accept a drop at drag-over time.
            List<IProductCmptLink> selectedLinks = getSelectedLinks();
            setToMove(selectedLinks);
            event.doit = selectedLinks != null;
            event.data = "local"; //$NON-NLS-1$
        }

        @Override
        public void dragSetData(DragSourceEvent event) {
            setToMove(getSelectedLinks());
            event.data = "local"; //$NON-NLS-1$
        }

        private List<IProductCmptLink> getSelectedLinks() {
            if (selectionProvider.getSelection() instanceof IStructuredSelection) {
                IStructuredSelection structuredSelection = (IStructuredSelection)selectionProvider.getSelection();
                List<IProductCmptLink> result = new ArrayList<>();
                for (Object obj : structuredSelection.toArray()) {
                    if (obj instanceof LinkViewItem) {
                        IProductCmptLink link = ((LinkViewItem)obj).getLink();
                        result.add(link);
                    } else {
                        return null;
                    }
                }
                return result;
            }
            return null;
        }

        @Override
        public void dragFinished(DragSourceEvent event) {
            setToMove(null);
        }

    }

}
