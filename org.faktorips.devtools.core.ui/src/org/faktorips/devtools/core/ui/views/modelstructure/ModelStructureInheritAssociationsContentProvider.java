/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views.modelstructure;

import static org.faktorips.devtools.core.ui.views.modelstructure.AssociationComponentNode.newAssociationComponentNode;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;

public final class ModelStructureInheritAssociationsContentProvider extends AbstractModelStructureContentProvider {

    private List<ComponentNode> storedRootElements;
    private final AssociationType[] ASSOCIATION_TYPES = { AssociationType.AGGREGATION,
            AssociationType.COMPOSITION_MASTER_TO_DETAIL };

    @Override
    public Object getParent(Object element) {
        return ((ComponentNode)element).getParent();
    }

    @Override
    public boolean hasChildren(Object element) {
        return this.getChildren(element) != null && this.getChildren(element).length > 0;
    }

    @Override
    protected String getWaitingLabel() {
        return Messages.ModelStructure_waitingLabel;
    }

    @Override
    protected Object[] collectElements(Object inputElement, IProgressMonitor monitor) {
        // if input is an IType, alter it to the corresponding IIpsProject
        IIpsProject inputProject = null;
        if (inputElement instanceof IType) {
            inputProject = ((IType)inputElement).getIpsProject();
        } else {
            inputProject = (IIpsProject)inputElement;
        }

        // only accept project input
        if (inputProject != null) {
            IIpsProject project = inputProject;

            monitor.beginTask(getWaitingLabel(), 3);
            List<IType> projectComponents;
            if (getShowTypeState() == ShowTypeState.SHOW_POLICIES) {
                projectComponents = getProjectITypes(project, IpsObjectType.POLICY_CMPT_TYPE);
            } else {
                projectComponents = getProjectITypes(project, IpsObjectType.PRODUCT_CMPT_TYPE);
            }
            monitor.worked(1);

            List<IType> derivedRootElements = computeDerivedRootElements(
                    getProjectSpecificITypes(projectComponents, project), projectComponents, project);
            monitor.worked(1);
            storedRootElements = ComponentNode.encapsulateComponentTypes(derivedRootElements, null, project);
            Object[] rootElements = storedRootElements.toArray();
            monitor.worked(1);
            monitor.done();
            return rootElements;
        } else {
            return null;
        }
    }

    private List<IType> computeDerivedRootElements(List<IType> projectSpecificITypes,
            List<IType> allComponentITypes,
            IIpsProject project) {
        List<IType> rootCandidates = new ArrayList<IType>();

        for (IType projectType : projectSpecificITypes) {
            try {
                IType supertype = projectType.findSupertype(project);

                if ((supertype == null || !supertype.getIpsProject().equals(project))
                        && !isAssociated(projectType, projectSpecificITypes, allComponentITypes, project,
                                ASSOCIATION_TYPES)) {
                    rootCandidates.add(projectType);
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }

        // remove superfluous root elements
        List<IType> notRootElements = new ArrayList<IType>();
        for (IType rootCandidate : rootCandidates) {
            // remove element if it is associated by another project specific element
            if (isAssociated(rootCandidate, projectSpecificITypes, allComponentITypes, project)) {
                notRootElements.add(rootCandidate);
            }
        }
        rootCandidates.removeAll(notRootElements);
        return rootCandidates;
    }

    private List<IType> findProjectSpecificSubtypes(IType parent, IIpsProject project) {
        List<IType> rootElements = new ArrayList<IType>();
        List<IType> subtypes = parent.findSubtypes(false, false, project);
        boolean foundNothing = true;
        for (IType subtype : subtypes) {
            if (subtype.getIpsProject().equals(project)) { // root-element found
                rootElements.add(subtype);
                foundNothing = false;
            }
        }

        // perform a breadth-first-search on the subtype-hierarchy
        List<IType> sameLevelSubtypes = new ArrayList<IType>(subtypes);
        while (foundNothing) {
            List<IType> newSameLevelSubtypes = new ArrayList<IType>();
            for (IType subtype : sameLevelSubtypes) {
                newSameLevelSubtypes.addAll(subtype.findSubtypes(false, false, project));
            }
            for (IType sameLevelSubtype : sameLevelSubtypes) {
                if (sameLevelSubtype.getIpsProject().equals(project)) {
                    rootElements.add(sameLevelSubtype);
                    foundNothing = false;
                }
            }
            if (foundNothing && newSameLevelSubtypes.isEmpty()) {
                return newSameLevelSubtypes;
            } else {
                sameLevelSubtypes = newSameLevelSubtypes;
            }
        }
        return rootElements;
    }

    @Override
    List<SubtypeComponentNode> getComponentNodeSubtypeChildren(ComponentNode parent) {
        List<IType> subtypes = findProjectSpecificSubtypes(parent.getValue(), parent.getValue().getIpsProject());
        return SubtypeComponentNode.encapsulateSubtypeComponentTypes(subtypes, parent, parent.getValue()
                .getIpsProject());
    }

    @Override
    List<AssociationComponentNode> getComponentNodeAssociationChildren(ComponentNode parent) {

        List<AssociationComponentNode> associationNodes = new ArrayList<AssociationComponentNode>();

        IType parentValue = parent.getValue();
        // IIpsProject project = parentValue.getIpsProject();
        IIpsProject project = parent.getSourceIpsProject();

        // add direct associations (from the same project)
        associationNodes.addAll(getDirectAssociationComponentNodes(parent, project));

        try {
            /*
             * general root element or supertype is from the same project, therefore we have no
             * derived associations
             */
            if (parentValue.findSupertype(project) == null
                    || parentValue.findSupertype(project).getIpsProject().equals(project)) {
                if (!associationNodes.isEmpty()) {
                    return associationNodes;
                } else {
                    return null;
                }
            } else {
                // compute the derived associations -> go up in the inheritance hierarchy
                IType supertype = parentValue.findSupertype(project);
                List<IAssociation> supertypeAssociations = new ArrayList<IAssociation>();
                // collect all relevant supertype-associations
                while (supertype != null && !supertype.getIpsProject().equals(project)) {
                    supertypeAssociations.addAll(0, supertype.getAssociations(ASSOCIATION_TYPES));
                    supertype = supertype.findSupertype(project);
                }

                ArrayList<AssociationComponentNode> superAssociationNodes = new ArrayList<AssociationComponentNode>();
                for (IAssociation supertypeAssociation : supertypeAssociations) {
                    if (!supertypeAssociation.isDerivedUnion()) {
                        IType target = supertypeAssociation.findTarget(project);
                        AssociationComponentNode associationComponentNode = newAssociationComponentNode(target,
                                supertypeAssociation, parent, project);
                        associationComponentNode.setInherited(true);
                        superAssociationNodes.add(associationComponentNode);
                    }
                }
                associationNodes.addAll(0, superAssociationNodes);
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

        if (!associationNodes.isEmpty()) {
            return associationNodes;
        }
        return null;
    }

    private List<AssociationComponentNode> getDirectAssociationComponentNodes(ComponentNode parent, IIpsProject project) {
        IType parentValue = parent.getValue();
        List<IAssociation> directAssociations = new ArrayList<IAssociation>();
        for (IAssociation directAssociation : parentValue.getAssociations(ASSOCIATION_TYPES)) {
            try {
                if (directAssociation.findTarget(project).getIpsProject().equals(project)) {
                    directAssociations.add(directAssociation);
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }

        List<AssociationComponentNode> componentNodes = new ArrayList<AssociationComponentNode>();
        for (IAssociation association : directAssociations) {
            componentNodes.add(AssociationComponentNode.newAssociationComponentNode(association, parent, project));
        }
        return componentNodes;
    }

    @Override
    List<ComponentNode> getStoredRootElements() {
        return storedRootElements;
    }
}
