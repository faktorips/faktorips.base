/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Item;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.SingleEventModification;
import org.faktorips.devtools.core.internal.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.ui.IpsFileTransferViewerDropAdapter;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.LinkDropListener;
import org.faktorips.devtools.core.ui.editors.productcmpt.ProductCmptEditor;
import org.faktorips.devtools.core.ui.util.LinkCreatorUtil;

/**
 * Drop Listener for the link section. This drop listener is able to move elements within the link
 * section and could create new links for objects that are dragged from outside the link section.
 * <p>
 * This listener is very similar to the {@link LinkDropListener} which is responsible for drag&drop
 * in the product structure view. Because of the different items provided by the different content
 * providers, these two listeners are separated.
 * 
 * @author dirmeier
 */
public class LinkSectionDropListener extends IpsFileTransferViewerDropAdapter {

    private List<IProductCmptLink> movedCmptLinks;
    private final IProductCmptGeneration generation;
    private final ProductCmptEditor editor;

    private final LinkCreatorUtil linkCreatorUtil = new LinkCreatorUtil(false);

    public LinkSectionDropListener(ProductCmptEditor editor, Viewer viewer, IProductCmptGeneration generation) {
        super(viewer);
        this.editor = editor;
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

    @Override
    public boolean validateDrop(Object target, int operation, TransferData transferType) {
        if (target == null | !editor.isDataChangeable()) {
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
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return false;
            }
        }
    }

    @Override
    public boolean performDrop(final Object data) {
        SingleEventModification<Boolean> modification = new SingleEventModification<Boolean>(generation.getIpsSrcFile()) {

            boolean result = false;

            @Override
            protected boolean execute() throws CoreException {
                if (getCurrentOperation() == DND.DROP_MOVE && movedCmptLinks != null) {
                    Object target = getCurrentTarget();
                    List<IProductCmptLink> listCopy = new ArrayList<IProductCmptLink>(movedCmptLinks);
                    /*
                     * If you drop a set of components you expect them in the same order as they
                     * were selected. To achieve this, we need to inverse the list, if insertion is
                     * after a component.
                     */
                    if (getCurrentLocation() == LOCATION_AFTER) {
                        Collections.reverse(listCopy);
                    }
                    result = moveLinks(listCopy, target);
                } else if (getCurrentOperation() == DND.DROP_LINK && data instanceof String[]) {
                    List<IProductCmpt> droppedCmpts = getProductCmpts((String[])data);
                    /*
                     * If you drop a set of components you expect them in the same order as they
                     * were selected. To achieve this, we need to inverse the list, if insertion is
                     * after a component.
                     */
                    if (getCurrentLocation() == LOCATION_AFTER) {
                        Collections.reverse(droppedCmpts);
                    }
                    result = createLinks(droppedCmpts, getCurrentTarget());
                } else {
                    result = false;
                }
                return result;
            }

            @Override
            protected Boolean getResult() {
                return result;
            }

        };
        try {
            return IpsPlugin.getDefault().getIpsModel().executeModificationsWithSingleEvent(modification);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return false;
        }

    }

    private boolean canMove(Object target) {
        // allow moving within the same association
        String associationName = getAssociationName(target);
        boolean result = false;
        List<IProductCmpt> draggedCmpts = new ArrayList<IProductCmpt>();
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
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return false;
        }
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
            boolean result = false;
            IProductCmptLinkContainer linkContainer = link.getProductCmptLinkContainer();
            for (IProductCmptLink firstTarget : linkContainer.getLinksAsList()) {
                if (firstTarget.getAssociation().equals(associationName)) {
                    // first link of correct association type, move after this and break
                    result = linkContainer.moveLink(link, firstTarget, true);
                    break;
                }
            }
            // no link of this association type found, move to first position (if there is any)
            if (linkContainer.getLinksAsList().size() > 0) {
                result = linkContainer.moveLink(link, linkContainer.getLinksAsList().get(0), true);
            } else {
                // if there is no element yet, move is ok
                return true;
            }
            if (result) {
                // setting correct asscociation
                link.setAssociation(associationName);
            }
            return result;
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

    private boolean canCreateLinks(List<IProductCmpt> draggedCmpts, Object target) throws CoreException {
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

    private boolean createLinks(List<IProductCmpt> draggedCmpts, Object target) throws CoreException {
        if (!canCreateLinks(draggedCmpts, target)) {
            return false;
        }

        for (IProductCmpt draggedCmpt : draggedCmpts) {
            createLink(draggedCmpt.getQualifiedName(), generation, target);
        }
        return true;
    }

    private IProductCmptLink createLink(String droppedCmptQName, IProductCmptGeneration generation, Object target)
            throws CoreException {

        IProductCmptTypeAssociation association = getAssociation(target);
        if (generation != null && association != null && IpsUIPlugin.isEditable(generation.getIpsSrcFile())) {
            IProductCmptLink newLink = linkCreatorUtil.createLink(association, generation, droppedCmptQName);
            moveLink(newLink, target);
            return newLink;
        } else {
            return null;
        }
    }

    /**
     * Override the determineLocation method because we have only location after or location before
     * when moving an element. When D&D is not in moving mode, we do not have location feedback. In
     * that case we return the normal determined location instead.
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

    private IProductCmptTypeAssociation getAssociation(Object target) throws CoreException {
        String associationName = getAssociationName(target);
        IProductCmptType type = generation.findProductCmptType(generation.getIpsProject());
        return (IProductCmptTypeAssociation)type.findAssociation(associationName, generation.getIpsProject());
    }

    /**
     * Listener to handle the move of relations.
     * 
     * @author Cornelius Dirmeier
     */
    public class MoveLinkDragListener implements DragSourceListener {
        ISelectionProvider selectionProvider;

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
                List<IProductCmptLink> result = new ArrayList<IProductCmptLink>();
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

    @Override
    public boolean validateDropSingle(Object target, int operation, TransferData data) {
        // nothing to do
        return false;
    }

    @Override
    public boolean performDropSingle(Object data) {
        // nothing to do
        return false;
    }

}
