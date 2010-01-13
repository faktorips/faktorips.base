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
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeRelationReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.AssociationSelectionDialog;

public class LinkDropListener extends ViewerDropAdapter {

    private TransferData actualTransferType;

    private List<IProductCmpt> actualTransferElements;

    private boolean autoSave = false;

    public LinkDropListener(Viewer viewer) {
        super(viewer);
        setFeedbackEnabled(false);
    }

    /**
     * @param autoSave The autoSave to set.
     */
    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
    }

    /**
     * @return Returns the autoSave.
     */
    public boolean isAutoSave() {
        return autoSave;
    }

    @Override
    public boolean validateDrop(Object target, int operation, TransferData transferType) {
        if (operation != DND.DROP_LINK) {
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
            return validateDrop(target, operation, draggedCmpts);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    private boolean validateDrop(Object target, int operation, List<IProductCmpt> draggedCmpts) throws CoreException {
        if (target instanceof IProductCmptReference) {
            // product cmpt reference in product structure view
            IProductCmptReference reference = (IProductCmptReference)target;
            return processProductCmptReference(draggedCmpts, reference, false);
        } else if (target instanceof IProductCmptTypeRelationReference) {
            IProductCmptTypeRelationReference reference = (IProductCmptTypeRelationReference)target;
            return processAssociationReference(draggedCmpts, reference, false);
        } else if (target instanceof IProductCmptLink) {
            IProductCmptLink link = ((IProductCmptLink)target);
            return processProductCmptLink(draggedCmpts, link, false);
        } else {
            return false;
        }
    }

    @Override
    public boolean performDrop(Object data) {
        if (!(data instanceof String[])) {
            return false;
        }
        List<IProductCmpt> droppedCmpts = getProductCmpts((String[])data);
        Object target = getCurrentTarget();
        boolean haveToSave = autoSave;
        try {
            boolean result;
            IIpsSrcFile ipsSrcFile;
            if (target instanceof IProductCmptReference) {
                IProductCmptReference cmptReference = (IProductCmptReference)target;
                ipsSrcFile = cmptReference.getWrappedIpsObject().getIpsSrcFile();
                haveToSave &= !ipsSrcFile.isDirty();
                result = processProductCmptReference(droppedCmpts, cmptReference, true);
            } else if (target instanceof IProductCmptTypeRelationReference) {
                IProductCmptTypeRelationReference relationReference = (IProductCmptTypeRelationReference)target;
                ipsSrcFile = relationReference.getParent().getWrappedIpsObject().getIpsSrcFile();
                haveToSave &= !ipsSrcFile.isDirty();
                result = processAssociationReference(droppedCmpts, relationReference, true);
            } else if (target instanceof IProductCmptLink) {
                IProductCmptLink cmptLink = (IProductCmptLink)target;
                ipsSrcFile = cmptLink.getIpsObject().getIpsSrcFile();
                haveToSave &= !ipsSrcFile.isDirty();
                result = processProductCmptLink(droppedCmpts, cmptLink, true);
            } else {
                return false;
            }
            if (result && haveToSave) {
                ipsSrcFile.save(false, null);
            }
            return result;
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    private boolean processProductCmptReference(List<IProductCmpt> draggedCmpts,
            IProductCmptReference reference,
            boolean createLinks) throws CoreException {

        IIpsProject ipsProject = reference.getProductCmpt().getIpsProject();
        IProductCmptGeneration generation = (IProductCmptGeneration)reference.getProductCmpt()
                .findGenerationEffectiveOn(reference.getStructure().getValidAt());
        IProductCmptType cmptType = reference.getProductCmpt().findProductCmptType(ipsProject);
        if (generation == null || cmptType == null) {
            return false;
        }
        List<IAssociation> associations = cmptType.findAllNotDerivedAssociations();
        // should only return true if all dragged cmpts are valid
        boolean result = false;
        for (IProductCmpt draggedCmpt : draggedCmpts) {
            List<IAssociation> possibleAssos = new ArrayList<IAssociation>();
            for (IAssociation aAssoziation : associations) {
                if (generation.canCreateValidLink(draggedCmpt, aAssoziation, generation.getIpsProject())) {
                    possibleAssos.add(aAssoziation);
                }
            }
            if (possibleAssos.size() > 0) {
                result = true;
            } else if (!createLinks) {
                return false;
            }
            if (createLinks) {
                if (possibleAssos.size() == 1) {
                    IAssociation association = possibleAssos.get(0);
                    createLink(draggedCmpt.getName(), generation, association);
                } else if (possibleAssos.size() > 1) {
                    Object[] selectedAssociations = selectAssociation(draggedCmpt.getName(), possibleAssos);
                    if (selectedAssociations != null) {
                        for (Object object : selectedAssociations) {
                            if (object instanceof IAssociation) {
                                IAssociation association = (IAssociation)object;
                                createLink(draggedCmpt.getName(), generation, association);
                                result = true;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Set to protected to test without clicking dialogs
     * 
     * @param droppedCmptName
     * @param possibleAssos
     * @return
     */
    protected Object[] selectAssociation(String droppedCmptName, List<IAssociation> possibleAssos) {
        Shell shell = Display.getDefault().getActiveShell();
        if (shell == null) {
            shell = new Shell(Display.getDefault());
        }
        SelectionDialog dialog = new AssociationSelectionDialog(shell, possibleAssos, NLS.bind(
                Messages.LinkDropListener_selectAssociation, droppedCmptName));
        dialog.setBlockOnOpen(true);
        dialog.setHelpAvailable(false);
        if (dialog.open() == Window.OK) {
            if (dialog.getResult().length > 0) {
                return dialog.getResult();
            }
        }
        // TODO Auto-generated method stub
        return null;
    }

    private boolean processAssociationReference(List<IProductCmpt> draggedCmpts,
            IProductCmptTypeRelationReference reference,
            boolean createLink) throws CoreException {
        IAssociation association;
        IProductCmptGeneration generation;
        IProductCmpt parentCmpt = ((IProductCmptReference)reference.getParent()).getProductCmpt();
        generation = (IProductCmptGeneration)parentCmpt
                .findGenerationEffectiveOn(reference.getStructure().getValidAt());
        association = reference.getRelation();
        // should only return true if all dragged cmpts are valid
        boolean result = false;
        for (IProductCmpt draggedCmpt : draggedCmpts) {
            if (generation != null
                    && generation.canCreateValidLink(draggedCmpt, association, generation.getIpsProject())) {
                result = true;
                if (createLink) {
                    createLink(draggedCmpt.getName(), generation, association);
                }
            } else {
                return false;
            }
        }
        return result;
    }

    private boolean processProductCmptLink(List<IProductCmpt> draggedCmpts, IProductCmptLink link, boolean createLink)
            throws CoreException {
        IAssociation association;
        IProductCmptGeneration generation = link.getProductCmptGeneration();
        association = link.findAssociation(generation.getIpsProject());
        // should only return true if all dragged cmpts are valid
        boolean result = false;
        for (IProductCmpt draggedCmpt : draggedCmpts) {
            if (generation != null
                    && generation.canCreateValidLink(draggedCmpt, association, generation.getIpsProject())) {
                result = true;
                if (createLink) {
                    createLink(draggedCmpt.getName(), generation, association);
                }
            } else {
                return false;
            }
        }
        return result;
    }

    private IProductCmptLink createLink(String droppedCmptName,
            IProductCmptGeneration generation,
            IAssociation association) {
        if (generation != null && association != null && generation.getIpsSrcFile().isMutable()) {
            IProductCmptLink newLink = null;

            // TODO handle location

            // if (insertBefore != null) {
            // newLink = generation.newLink(association.getName(), insertBefore);
            // } else {
            newLink = generation.newLink(association.getName());
            // }
            newLink.setTarget(droppedCmptName);
            newLink.setMaxCardinality(1);
            newLink.setMinCardinality(0);
            return newLink;
        } else {
            return null;
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
        // TODO debug for linux!
        // Under some platforms, the data is not available during dragOver.
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

}
