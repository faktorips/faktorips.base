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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;

public final class ModelStructureContentProvider extends AbstractModelStructureContentProvider {

    // it is important that this list does not contain a set of AssociationTypes which would cause
    // association loops
    private final AssociationType[] ASSOCIATION_TYPES = { AssociationType.AGGREGATION,
            AssociationType.COMPOSITION_MASTER_TO_DETAIL };
    private List<ComponentNode> storedRootElements;

    @Override
    protected Object[] collectElements(Object inputElement, IProgressMonitor monitor) {

        SubMonitor progress = SubMonitor.convert(monitor);
        progress.beginTask(getWaitingLabel(), 100);
        IIpsProject ipsProject;
        if (inputElement instanceof IType) {
            ipsProject = ((IType)inputElement).getIpsProject();
        } else {
            ipsProject = (IIpsProject)inputElement;
        }

        // get the root elements
        Collection<IType> rootComponents;
        if (inputElement instanceof IType) { // get the root elements if the input is an IType

            IType input = (IType)inputElement;
            List<IType> projectTypes = getProjectITypes(ipsProject, getCurrentlyNeededIpsObjectType(input));
            progress.worked(50);

            Collection<IType> rootCandidates = getRootElementsForIType(input, projectTypes,
                    ToChildAssociationType.SELF, new ArrayList<IType>(), new ArrayList<PathElement>());
            progress.worked(25);

            rootComponents = getRootElementsForIType(input, projectTypes, ToChildAssociationType.SELF, rootCandidates,
                    new ArrayList<PathElement>());
            progress.worked(25);

        } else { // get the root elements if the input is an IpsProject
            List<IType> projectTypes = getProjectITypes(ipsProject, getCurrentlyNeededIpsObjectType());
            progress.worked(70);

            rootComponents = getProjectRootElementsFromComponentList(projectTypes, ipsProject, progress.newChild(30),
                    ASSOCIATION_TYPES);
        }
        monitor.done();
        storedRootElements = ComponentNode.encapsulateComponentTypes(rootComponents, null, ipsProject);
        return storedRootElements.toArray();
    }

    /**
     * Determines the needed IpsObjectType on base of the showState, this is needed when the root
     * elements for an IIpsProject have to be calculated.
     */
    private IpsObjectType getCurrentlyNeededIpsObjectType() {
        if (getShowTypeState() == ShowTypeState.SHOW_POLICIES) {
            return IpsObjectType.POLICY_CMPT_TYPE;
        } else {
            return IpsObjectType.PRODUCT_CMPT_TYPE;
        }
    }

    /**
     * Determines the needed IpsObjectType on base of the Class of the input IType, this is needed
     * when the root elements for a single IType have to be calculated.
     */
    private IpsObjectType getCurrentlyNeededIpsObjectType(IType input) {
        if (input instanceof PolicyCmptType) {
            return IpsObjectType.POLICY_CMPT_TYPE;
        } else {
            return IpsObjectType.PRODUCT_CMPT_TYPE;
        }
    }

    /**
     * Computes the root-nodes for an {@link IType} element. The root-nodes are exactly those nodes
     * which have an outgoing association to the given element. An association is of Type
     * {@link AssociationType}.COMPOSITION_MASTER_TO_DETAIL or {@link AssociationType}.AGGREGATION
     * and may contain a subtype hierarchy. A run of this method with an empty rootCandidates
     * parameter will return a list of root-candidates. A second run of this method with the
     * previously obtained rootCandidates as parameter, will remove false candidates from the list.
     * 
     * @param element the starting point
     * @param componentList the list of all concerned elements
     * @param association the
     *            {@link org.faktorips.devtools.core.ui.views.modelstructure.AbstractModelStructureContentProvider.ToChildAssociationType
     *            ToChildAssociationType} of the parent element to this element
     * @param rootCandidates a {@link Collection} of {@link IType}. elements
     * @param callHierarchy a {@link List} which contains the path from the current element to the
     *            source element
     */
    Collection<IType> getRootElementsForIType(IType element,
            List<IType> componentList,
            ToChildAssociationType association,
            Collection<IType> rootCandidates,
            List<PathElement> callHierarchy) {

        List<PathElement> callHierarchyTemp = new LinkedList<PathElement>(callHierarchy);

        PathElement pathElement = new PathElement(element, association);
        // If the element is already contained in the callHierarchy we have detected a cycle
        if (callHierarchy.contains(pathElement)) {
            return new HashSet<IType>();
        } else {
            callHierarchyTemp.add(0, pathElement);
        }

        IType supertype = getExistingSupertypeFromList(element, componentList);
        Set<IType> rootElements = new HashSet<IType>();
        List<IType> associatingTypes = getAssociatingTypes(element, componentList, ASSOCIATION_TYPES);
        // Breaking condition
        if (associatingTypes.isEmpty() && supertype == null
                && (association == ToChildAssociationType.SELF || association == ToChildAssociationType.ASSOCIATION)) {
            rootElements.add(element);
        }

        // recursive call for all child elements
        for (IType associations : associatingTypes) {
            Collection<IType> rootElementsForIType = getRootElementsForIType(associations, componentList,
                    ToChildAssociationType.ASSOCIATION, rootCandidates, callHierarchyTemp);
            rootElements.addAll(rootElementsForIType);
        }

        if (supertype != null) {
            rootElements.addAll(getRootElementsForIType(supertype, componentList, ToChildAssociationType.SUPERTYPE,
                    rootCandidates, callHierarchyTemp));
        }

        // Add supertype if it has any aggregation or composition
        if (rootElements.isEmpty()
                && association == ToChildAssociationType.SUPERTYPE
                && !element.getAssociations(AssociationType.AGGREGATION, AssociationType.COMPOSITION_MASTER_TO_DETAIL)
                        .isEmpty()) {
            rootElements.add(element);
        }

        // None of the child elements is a root element, therefore check the association type of the
        // current element
        if (rootElements.isEmpty() && association == ToChildAssociationType.ASSOCIATION) {
            rootElements.add(element);
        }

        // If the hierarchy is solely build with supertypes, we must add the source element itself
        if (rootElements.isEmpty() && association == ToChildAssociationType.SELF) {
            rootElements.add(element);
        }

        return rootElements;
    }

    @Override
    List<SubtypeComponentNode> getComponentNodeSubtypeChildren(ComponentNode parent) {
        IIpsProject project = parent.getSourceIpsProject();
        List<IType> subtypes = parent.getValue().findSubtypes(false, false, project);
        List<SubtypeComponentNode> subtypeNodeChildren = new ArrayList<SubtypeComponentNode>();

        List<IType> projectITypes = getProjectITypes(project, parent.getValue().getIpsObjectType());
        List<IType> projectSpecificTypes = getProjectSpecificTypes(projectITypes, project);

        for (IType subtype : subtypes) {
            SubtypeComponentNode componentNode = new SubtypeComponentNode(subtype, parent, project);
            boolean associated = false;
            if (subtype.getIpsProject().equals(project)) {
                associated = isAssociated(subtype, projectSpecificTypes, projectITypes, project, ASSOCIATION_TYPES);
            }
            componentNode.setHasInheritedAssociation(associated);

            subtypeNodeChildren.add(componentNode);
        }

        return subtypeNodeChildren;
    }

    @Override
    List<AssociationComponentNode> getComponentNodeAssociationChildren(ComponentNode parent) {
        List<IAssociation> associations = parent.getValue().getAssociations(
                AssociationType.COMPOSITION_MASTER_TO_DETAIL, AssociationType.AGGREGATION);
        if (!associations.isEmpty()) {
            List<AssociationComponentNode> compositeNodeChildren = new ArrayList<AssociationComponentNode>();

            for (IAssociation association : associations) {
                compositeNodeChildren.add(AssociationComponentNode.newAssociationComponentNode(association, parent,
                        parent.getSourceIpsProject()));
            }
            return compositeNodeChildren;
        }
        return null;
    }

    private List<IType> getProjectSpecificTypes(List<IType> projectITypes, IIpsProject project) {
        List<IType> types = new ArrayList<IType>();
        for (IType projectType : projectITypes) {
            if (projectType.getIpsProject().equals(project)) {
                types.add(projectType);
            }
        }
        return types;
    }

    @Override
    public Object getParent(Object element) {
        ComponentNode node = (ComponentNode)element;
        return node.getParent();
    }

    @Override
    public boolean hasChildren(Object element) {
        return getChildren(element).length != 0;
    }

    @Override
    protected String getWaitingLabel() {
        return Messages.ModelStructure_waitingLabel;
    }

    @Override
    List<ComponentNode> getStoredRootElements() {
        return storedRootElements;
    }
}
