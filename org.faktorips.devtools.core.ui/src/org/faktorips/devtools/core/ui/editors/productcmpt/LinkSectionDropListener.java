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

package org.faktorips.devtools.core.ui.editors.productcmpt;

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
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.IpsFileTransferViewerDropAdapter;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.LinkDropListener;

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

    public LinkSectionDropListener(Viewer viewer, IProductCmptGeneration generation) {
        super(viewer);
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
        if (target == null) {
            return false;
        }
        if (movedCmptLinks != null) {
            if ((target instanceof IProductCmptLink || target instanceof String)
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
                    // if you drop a set of components you aspect them in the same order as the
                    // selection to get this, we need to inverse the list, if insertion is after
                    // a component
                    if (getCurrentLocation() == LOCATION_AFTER) {
                        Collections.reverse(listCopy);
                    }
                    result = moveLinks(listCopy, target);
                } else if (getCurrentOperation() == DND.DROP_LINK && data instanceof String[]) {
                    List<IProductCmpt> droppedCmpts = getProductCmpts((String[])data);
                    // if you drop a set of components you aspect them in the same order as the
                    // selection to get this, we need to inverse the list, if insertion is after
                    // a component
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
        String associationName = null;
        if (target instanceof IProductCmptLink) {
            IProductCmptLink targetCmptLink = (IProductCmptLink)target;
            associationName = targetCmptLink.getAssociation();
        } else if (target instanceof String) {
            associationName = (String)target;
        }
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
        if (target instanceof String) {
            // move to first position of this association
            boolean result = false;
            for (IProductCmptLink firstTarget : generation.getLinks()) {
                if (firstTarget.getAssociation().equals(target)) {
                    // first link of correct association type, move after this and break
                    result = generation.moveLink(link, firstTarget, true);
                    break;
                }
            }
            // no link of this association type found, move to first position (if there is any)
            if (generation.getLinks().length > 0) {
                result = generation.moveLink(link, generation.getLinks()[0], true);
            } else {
                // if there is no element yet, move is ok
                return true;
            }
            if (result) {
                // setting correct asscociation
                link.setAssociation((String)target);
            }
            return result;
        } else if (target instanceof IProductCmptLink) {
            IProductCmptLink targetLink = (IProductCmptLink)target;
            IProductCmptGeneration generation = targetLink.getProductCmptGeneration();
            boolean before = getCurrentLocation() == LOCATION_BEFORE;
            return generation.moveLink(link, targetLink, before);
        } else {
            return false;
        }
    }

    private boolean canCreateLinks(List<IProductCmpt> draggedCmpts, Object target) throws CoreException {
        IpsUIPlugin.getDefault();
        if (!IpsUIPlugin.isEditable(generation.getIpsSrcFile())) {
            return false;
        }
        // should only return true if all dragged cmpts are valid
        IAssociation association = getAssociation(target);
        boolean result = false;
        for (IProductCmpt draggedCmpt : draggedCmpts) {
            if (generation != null
                    && generation.canCreateValidLink(draggedCmpt, association, generation.getIpsProject())) {
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

        IAssociation association = getAssociation(target);
        if (generation != null && association != null && IpsUIPlugin.isEditable(generation.getIpsSrcFile())) {
            IProductCmptLink newLink = null;
            newLink = generation.newLink(association.getName());
            newLink.setTarget(droppedCmptQName);
            newLink.setMaxCardinality(1);
            newLink.setMinCardinality(0);
            moveLink(newLink, target);
            return newLink;
        } else {
            return null;
        }
    }

    /**
     * Override the determineLocation method because we have only location after or location before
     * when moving an element. When D&N is not in moving mode, we do not have location feedback, but
     * we although return the normal determined location.
     */
    @Override
    protected int determineLocation(DropTargetEvent event) {
        if (!(event.item instanceof Item)) {
            return LOCATION_NONE;
        }
        // dropping on an associationReference means moving on first position of this node
        if (getCurrentTarget() instanceof String) {
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

    private IAssociation getAssociation(Object target) throws CoreException {
        String associationName = null;
        if (target instanceof IProductCmptLink) {
            associationName = ((IProductCmptLink)target).getAssociation();
        } else if (target instanceof String) {
            associationName = (String)target;
        }
        IProductCmptType type = generation.getProductCmpt().findProductCmptType(generation.getIpsProject());
        return type.findAssociation(associationName, generation.getIpsProject());
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
                    if (obj instanceof IProductCmptLink) {
                        IProductCmptLink link = (IProductCmptLink)obj;
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
