/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.Messages;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.IAssociation;

/**
 * This class provides several ways to create a links in a product component.
 * 
 * @author dirmeier
 */
public class LinkCreatorUtil {

    private final boolean autoSave;

    public LinkCreatorUtil(boolean autoSave) {
        this.autoSave = autoSave;
    }

    public boolean createLinks(List<IProductCmpt> droppedCmpts, IProductCmptStructureReference target) {
        boolean haveToSave = autoSave;
        try {
            boolean result;
            IIpsSrcFile ipsSrcFile;
            if (target instanceof IProductCmptReference) {
                IProductCmptReference cmptReference = (IProductCmptReference)target;
                ipsSrcFile = cmptReference.getWrappedIpsSrcFile();
                haveToSave &= !ipsSrcFile.isDirty();
                result = processProductCmptReference(droppedCmpts, cmptReference, true);
            } else if (target instanceof IProductCmptTypeAssociationReference) {
                IProductCmptTypeAssociationReference relationReference = (IProductCmptTypeAssociationReference)target;
                ipsSrcFile = relationReference.getParent().getWrappedIpsSrcFile();
                haveToSave &= !ipsSrcFile.isDirty();
                result = processAssociationReference(droppedCmpts, relationReference, true);
            } else {
                return false;
            }
            if (result && haveToSave) {
                ipsSrcFile.save(null);
            }
            return result;
        } catch (IpsException e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    protected boolean processProductCmptReference(List<IProductCmpt> draggedCmpts,
            IProductCmptReference target,
            boolean createLinks) {
        IpsUIPlugin.getDefault();
        if (!IpsUIPlugin.isEditable(target.getProductCmpt().getIpsSrcFile())) {
            return false;
        }

        IIpsProject ipsProject = target.getProductCmpt().getIpsProject();
        IProductCmptGeneration generation = target.getProductCmpt().getGenerationEffectiveOn(
                target.getStructure().getValidAt());
        IProductCmptType cmptType = target.getProductCmpt().findProductCmptType(ipsProject);
        if (generation == null || cmptType == null) {
            return false;
        }
        List<IProductCmptTypeAssociation> associations = cmptType.findAllNotDerivedAssociations(ipsProject);
        // should only return true if all dragged cmpts are valid
        boolean result = false;
        for (IProductCmpt draggedCmpt : draggedCmpts) {
            List<IProductCmptTypeAssociation> possibleAssos = new ArrayList<>();
            for (IProductCmptTypeAssociation aAssoziation : associations) {
                if (canCreateValidLink(generation, draggedCmpt, aAssoziation)) {
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
                    IProductCmptTypeAssociation association = possibleAssos.get(0);
                    createLink(association, generation, draggedCmpt.getQualifiedName(), draggedCmpts.size() == 1);
                } else if (possibleAssos.size() > 1) {
                    Object[] selectedAssociations = selectAssociation(draggedCmpt.getQualifiedName(), possibleAssos);
                    if (selectedAssociations != null) {
                        for (Object object : selectedAssociations) {
                            if (object instanceof IProductCmptTypeAssociation) {
                                IProductCmptTypeAssociation association = (IProductCmptTypeAssociation)object;
                                createLink(association, generation, draggedCmpt.getQualifiedName(),
                                        draggedCmpts.size() == 1);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private boolean canCreateValidLink(IProductCmptGeneration generation,
            IProductCmpt draggedCmpt,
            IProductCmptTypeAssociation aAssoziation) {
        if (generation == null) {
            return false;
        }
        IProductCmptLinkContainer container;
        if (generation.isContainerFor(aAssoziation)) {
            container = generation;
        } else {
            container = generation.getProductCmpt();
        }
        return container.canCreateValidLink(draggedCmpt, aAssoziation, container.getIpsProject());
    }

    /**
     * Set to protected to override in test class
     */
    protected Object[] selectAssociation(String droppedCmptName, List<IProductCmptTypeAssociation> possibleAssos) {
        Shell shell = Display.getDefault().getActiveShell();
        if (shell == null) {
            shell = new Shell(Display.getDefault());
        }
        //
        ArrayList<IAssociation> copyOfPossibleAssos = new ArrayList<>(possibleAssos);
        SelectionDialog dialog = ListSelectionDialog.of(copyOfPossibleAssos)
                .contentProvider(new AssociationSelectionDialogContentProvider(copyOfPossibleAssos))
                .labelProvider(new WorkbenchLabelProvider())
                .message(NLS.bind(Messages.LinkDropListener_selectAssociation, droppedCmptName))
                .create(shell);

        dialog.setBlockOnOpen(true);
        dialog.setHelpAvailable(false);
        if (dialog.open() == Window.OK) {
            if (dialog.getResult().length > 0) {
                return dialog.getResult();
            }
        }
        return null;
    }

    protected boolean processAssociationReference(List<IProductCmpt> draggedCmpts,
            IProductCmptTypeAssociationReference target,
            boolean createLink) {
        IProductCmptTypeAssociation association;
        IProductCmptGeneration generation;
        IProductCmpt parentCmpt = ((IProductCmptReference)target.getParent()).getProductCmpt();
        IpsUIPlugin.getDefault();
        if (!IpsUIPlugin.isEditable(parentCmpt.getIpsSrcFile())) {
            return false;
        }
        generation = parentCmpt.getGenerationEffectiveOn(target.getStructure().getValidAt());
        association = target.getAssociation();
        // should only return true if all dragged cmpts are valid
        boolean result = false;
        for (IProductCmpt draggedCmpt : draggedCmpts) {
            if (canCreateValidLink(generation, draggedCmpt, association)) {
                result = true;
                if (createLink) {
                    createLink(association, generation, draggedCmpt.getQualifiedName(),
                            draggedCmpts.size() == 1);
                }
            } else {
                return false;
            }
        }
        return result;
    }

    /**
     * Creates a new link instance for the given association. If the association is defined as
     * changing over time, the link instance will be added to the product component generation.
     * Otherwise it will be added to the product component itself.
     * 
     * @param association the association the new link is an instance of.
     * @param generation the generation currently active in the editor. The new link is not
     *            necessarily added to this generation!
     * @param targetQualifiedName the qualified name of the target product component
     * @return the newly created link instance
     */
    public IProductCmptLink createLink(IProductCmptTypeAssociation association,
            IProductCmptGeneration generation,
            String targetQualifiedName,
            boolean singleTarget) {
        if (generation != null && association != null && IpsUIPlugin.getDefault().isGenerationEditable(generation)) {
            IProductCmptLinkContainer container = getLinkContainerFor(generation, association);
            return createLinkForContainer(targetQualifiedName, container, association, singleTarget);
        }
        return null;
    }

    /**
     * Returns the generation if it is a container for the given association. Returns the
     * generation's product component otherwise.
     * 
     * @param generation the possible link container
     * @param association the association to retrieve a {@link IProductCmptLinkContainer container}
     *            for.
     * @see IProductCmptLinkContainer#isContainerFor(IProductCmptTypeAssociation)
     */
    public static IProductCmptLinkContainer getLinkContainerFor(IProductCmptGeneration generation,
            IProductCmptTypeAssociation association) {
        if (generation.isContainerFor(association)) {
            return generation;
        } else {
            return generation.getProductCmpt();
        }
    }

    /**
     * Returns a new {@link IProductCmptLinkContainer container} link. Sets cardinalities of the
     * link to the corresponding policyAssociation values, if the {@link IProductCmptLink} is the
     * first in container, has a single target and the association contains a matching
     * policyAssociation.
     * 
     * @param droppedCmptQName the name of the dropped component
     * @param container contains the newly created link
     * @param association the association to retrieve a {@link IProductCmptLinkContainer container}
     *            for
     * @param singleTarget boolean which is true, if the link contains one target, false if multiple
     * @return a new {@link IProductCmptLink} container link.
     */
    private IProductCmptLink createLinkForContainer(String droppedCmptQName,
            IProductCmptLinkContainer container,
            IProductCmptTypeAssociation association,
            boolean singleTarget) {
        IProductCmptLink newLink = container.newLink(association.getName());
        IAssociation policyAssociation = association.findMatchingAssociation();
        newLink.setTarget(droppedCmptQName);
        if (singleTarget
                && policyAssociation != null
                && isFirstLink(container, newLink)) {
            newLink.setMaxCardinality(policyAssociation.getMaxCardinality());
            newLink.setMinCardinality(policyAssociation.getMinCardinality());
            newLink.setDefaultCardinality(policyAssociation.getMinCardinality());
        } else {
            newLink.setMaxCardinality(1);
            newLink.setMinCardinality(0);
        }
        return newLink;
    }

    private boolean isFirstLink(IProductCmptLinkContainer container, IProductCmptLink newLink) {
        String newLinkAssociation = newLink.getAssociation();
        return container.getLinksAsList().stream()
                .map(IProductCmptLink::getAssociation)
                .filter(Predicate.isEqual(newLinkAssociation))
                .count() == 1;
    }

    private static class AssociationSelectionDialogContentProvider implements IStructuredContentProvider {

        private final Object[] associations;

        public AssociationSelectionDialogContentProvider(List<IAssociation> associations) {
            this.associations = associations.toArray();
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return associations;
        }

        @Override
        public void dispose() {
            // Nothing to do
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // Nothing to do
        }

    }

}
