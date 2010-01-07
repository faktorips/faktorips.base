package org.faktorips.devtools.core.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeRelationReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;

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

/**
 * Listener for Drop-Actions to create new relations.
 * 
 * @author Thorsten Guenther
 * @author Cornelius Dirmeier
 */
public class ReferenceDropListener extends DropTargetAdapter {

    private CopyOnWriteArrayList<IDropDoneListener> listeners;

    private int oldDetail = DND.DROP_NONE;

    private boolean enabled = true;

    private IProductCmptLink toMove;

    @Override
    public void dragEnter(DropTargetEvent event) {
        if (!enabled) {
            event.detail = DND.DROP_NONE;
            return;
        }

        if (event.detail == 0) {
            event.detail = DND.DROP_LINK;
        }

        oldDetail = event.detail;

        event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SELECT | DND.FEEDBACK_INSERT_AFTER | DND.FEEDBACK_SCROLL;

    }

    @Override
    public void dragOver(DropTargetEvent event) {
        Object insertAt = getInsertAt(event);
        if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
            // we have a file transfer
            String[] filenames = (String[])FileTransfer.getInstance().nativeToJava(event.currentDataType);

            // Under some platforms, the data is not available during dragOver.
            if (filenames == null) {
                return;
            }

            boolean accept = false;

            for (int i = 0; i < filenames.length; i++) {
                IFile file = getFile(filenames[i]);
                try {
                    IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(file);

                    if (element == null || !element.exists()) {
                        event.detail = DND.DROP_NONE;
                        return;
                    }

                    IProductCmpt draggedCmpt = getProductCmpt(file);

                    IAssociation association = null;
                    IProductCmptGeneration generation = null;
                    if (insertAt instanceof IProductCmptTypeRelationReference) {
                        IProductCmptTypeRelationReference reference = (IProductCmptTypeRelationReference)insertAt;
                        IProductCmpt parentCmpt = ((IProductCmptReference)reference.getParent()).getProductCmpt();
                        generation = (IProductCmptGeneration)parentCmpt.getGenerationByEffectiveDate(reference
                                .getStructure().getValidAt());
                        association = reference.getRelation();
                    } else if (insertAt instanceof IProductCmptLink) {
                        IProductCmptLink link = ((IProductCmptLink)insertAt);
                        generation = link.getProductCmptGeneration();
                        association = link.findAssociation(generation.getIpsProject());
                    }

                    if (generation != null
                            && generation.canCreateValidLink(draggedCmpt, association, generation.getIpsProject())) {
                        accept = true;
                    }
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }

            if (accept == true) {
                // we can create at least on of the requested Relations - so we accept the drop
                event.detail = oldDetail;
            } else {
                // event.detail = DND.DROP_NONE;
            }

        } else if (toMove != null && insertAt instanceof IProductCmptLink) {
            event.detail = DND.DROP_MOVE;
        } else {
            event.detail = DND.DROP_NONE;
        }
    }

    @Override
    public void drop(DropTargetEvent event) {
        IIpsSrcFile srcFile = getTargetSrcFile(event);
        boolean srcFileDirty = false;
        if (srcFile != null) {
            srcFileDirty = srcFile.isDirty();
        }
        List<IProductCmptLink> result = handleDrop(event);
        notifyDropDoneListener(event, result, srcFileDirty);
    }

    // returns a list of created ProductCmptLinks (maybe an empty list)
    private List<IProductCmptLink> handleDrop(DropTargetEvent event) {
        Object insertAt = getInsertAt(event);
        List<IProductCmptLink> result = new ArrayList<IProductCmptLink>(1);

        // found no relation or relationtype which gives us the information
        // about the position of the insert, so dont drop.
        if (insertAt == null) {
            return result;
        }

        if (event.operations == DND.DROP_MOVE) {
            IProductCmptLink movedLink = move(insertAt);
            if (movedLink != null) {
                result.add(movedLink);
            }
        } else if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
            // we have a file transfer
            String[] filenames = (String[])FileTransfer.getInstance().nativeToJava(event.currentDataType);
            for (int i = 0; i < filenames.length; i++) {
                IFile file = getFile(filenames[i]);
                // at least one have to succeed
                result.add(insert(file, insertAt));
            }
        }
        return result;
    }

    @Override
    public void dropAccept(DropTargetEvent event) {
        // XXX changeable

        // should be handled in dragOver

        // if (!isChangeable) {
        // event.detail = DND.DROP_NONE;
        // }
    }

    private IFile getFile(String filename) {
        return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(filename));
    }

    private IProductCmpt getProductCmpt(IFile file) throws CoreException {
        if (file == null) {
            return null;
        }

        IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(file);

        if (element == null || !element.exists()) {
            return null;
        }

        if (element instanceof IIpsSrcFile
                && ((IIpsSrcFile)element).getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT)) {
            return (IProductCmpt)((IIpsSrcFile)element).getIpsObject();
        }

        return null;
    }

    private IProductCmptLink move(Object insertBefore) {
        if (insertBefore instanceof IProductCmptLink) {
            IProductCmptLink link = (IProductCmptLink)insertBefore;
            link.getProductCmptGeneration().moveLink(getToMove(), (IProductCmptLink)insertBefore);
            return link;
        }
        return null;
    }

    private Object getInsertAt(DropTargetEvent event) {
        if (event.item != null && event.item.getData() != null) {
            return event.item.getData();
        } else {
            // XXX is it correct
            // event happened on the treeview, but not targeted at an entry
            // TreeItem[] items = treeViewer.getTree().getItems();
            // if (items.length > 0) {
            // return items[items.length - 1].getData();
            // }
        }
        return null;
    }

    private IIpsSrcFile getTargetSrcFile(DropTargetEvent event) {
        Object o = getInsertAt(event);
        if (o instanceof IProductCmptStructureReference) {
            IProductCmptStructureReference reference = (IProductCmptStructureReference)o;
            return reference.getWrappedIpsObject().getIpsSrcFile();
        } else if (o instanceof IProductCmptLink) {
            IProductCmptLink link = (IProductCmptLink)o;
            return link.getIpsSrcFile();
        }
        return null;
    }

    /**
     * Insert a new relation to the product component contained in the given file. If the file is
     * <code>null</code> or does not contain a product component, the insert is aborted.
     * 
     * @param file The file describing a product component (can be null, no insert takes place
     *            then).
     * @param insertAt The relation or relation type to insert at.
     */
    private IProductCmptLink insert(IFile file, Object insertAt) {
        try {
            IProductCmpt cmpt = getProductCmpt(file);
            if (cmpt != null) {
                return insert(cmpt, insertAt);
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    /**
     * Inserts a new relation to the product component identified by the given target name.
     * 
     * @param droppedCmpt The dropped product component
     * @param insertAt The product component relation or product component type relation the new
     *            relations has to be inserted. The type of the new relation is determined from this
     *            object (which means the new relation has the same product component relation type
     *            as the given one or is of the given type).
     */
    private IProductCmptLink insert(IProductCmpt droppedCmpt, Object insertAt) {
        String droppedCmptName = droppedCmpt.getQualifiedName();
        IAssociation association = null;
        IProductCmptLink insertBefore = null;
        // only save if changed in vie (insertAt is a reference type) and src file not already dirty
        try {
            IProductCmptGeneration generation = null;
            if (insertAt instanceof IProductCmptTypeRelationReference) {
                // association node in editor or view
                IProductCmptTypeRelationReference reference = (IProductCmptTypeRelationReference)insertAt;
                IProductCmpt parentCmpt = ((IProductCmptReference)reference.getParent()).getProductCmpt();
                generation = (IProductCmptGeneration)parentCmpt.findGenerationEffectiveOn(reference.getStructure()
                        .getValidAt());

                if (generation.canCreateValidLink(droppedCmpt, reference.getRelation(), generation.getIpsProject())) {
                    association = reference.getRelation();
                }
            } else if (insertAt instanceof IProductCmptReference) {
                // product cmpt reference in product structure view
                IProductCmptReference reference = (IProductCmptReference)insertAt;
                IIpsProject ipsProject = reference.getProductCmpt().getIpsProject();
                generation = (IProductCmptGeneration)reference.getProductCmpt().findGenerationEffectiveOn(
                        reference.getStructure().getValidAt());
                IProductCmptType cmptType = reference.getProductCmpt().findProductCmptType(ipsProject);
                if (generation == null || cmptType == null) {
                    return null;
                }
                List<IAssociation> associations = cmptType.findAllNotDerivedAssociations();
                List<IAssociation> possibleAssos = new ArrayList<IAssociation>();
                for (IAssociation aAssoziation : associations) {
                    if (generation.canCreateValidLink(droppedCmpt, aAssoziation, generation.getIpsProject())) {
                        possibleAssos.add(aAssoziation);
                    }
                }
                if (possibleAssos.size() == 1) {
                    association = possibleAssos.get(0);
                }
                // TODO bei mehreren gültigen Assoziationen soll eine auswählbar sein!

            } else if (insertAt instanceof IProductCmptLink) {
                // Product Cmpt Link in linkSection of product cmpt editor
                IProductCmptLink link = (IProductCmptLink)insertAt;
                generation = link.getProductCmptGeneration();
                IProductCmptTypeAssociation aAssoziation = link.findAssociation(generation.getIpsProject());
                if (generation.canCreateValidLink(droppedCmpt, aAssoziation, generation.getIpsProject())) {
                    association = aAssoziation;
                }
                insertBefore = link;
            }

            if (generation != null && association != null && generation.getIpsSrcFile().isMutable()) {
                IProductCmptLink newLink = newLink(generation, droppedCmptName, association, insertBefore);
                return newLink;
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    /**
     * Creates a new link which connects the currently displayed generation with the given target.
     * The new link is placed before the the given one.
     */
    private IProductCmptLink newLink(IProductCmptGeneration generation,
            String target,
            IAssociation association,
            IProductCmptLink insertBefore) {
        IProductCmptLink newLink = null;
        if (insertBefore != null) {
            newLink = generation.newLink(association.getName(), insertBefore);
        } else {
            newLink = generation.newLink(association.getName());
        }
        newLink.setTarget(target);
        newLink.setMaxCardinality(1);
        newLink.setMinCardinality(0);
        return newLink;
    }

    public void setToMove(IProductCmptLink toMove) {
        this.toMove = toMove;
    }

    public IProductCmptLink getToMove() {
        return toMove;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void addDropDoneListener(IDropDoneListener listener) {
        if (listeners == null) {
            listeners = new CopyOnWriteArrayList<IDropDoneListener>();
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public boolean removeDropDoneListener(IDropDoneListener listener) {
        if (listeners != null) {
            return listeners.remove(listener);
        } else {
            return false;
        }
    }

    private void notifyDropDoneListener(DropTargetEvent event, List<IProductCmptLink> result, boolean srcFileDirty) {
        for (IDropDoneListener listener : listeners) {
            listener.dropDone(event, result, srcFileDirty);
        }
    }

    /**
     * A listener that is notified if a reference drop was done
     * 
     * @author dirmeier
     */
    public interface IDropDoneListener {

        /**
         * notifies the listener that a drop was done.
         * 
         * @param event The drop event
         * @param result the list of created or modified links
         * @param srcFileWasDirty
         */
        public void dropDone(DropTargetEvent event, List<IProductCmptLink> result, boolean srcFileWasDirty);

    }

}
