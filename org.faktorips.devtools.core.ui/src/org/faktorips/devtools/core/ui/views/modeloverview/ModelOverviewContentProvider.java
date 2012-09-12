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

package org.faktorips.devtools.core.ui.views.modeloverview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IType;

public class ModelOverviewContentProvider extends AbstractModelOverviewContentProvider {

    private List<List<PathElement>> paths = new ArrayList<List<PathElement>>();
    // it is important that this list does not contain a set of AssociationTypes which would cause
    // association loops
    private final AssociationType[] associationTypeFilter = { AssociationType.AGGREGATION,
            AssociationType.COMPOSITION_MASTER_TO_DETAIL };

    private ShowTypeState showState = ShowTypeState.SHOW_POLICIES;

    @Override
    protected Object[] collectElements(Object inputElement, IProgressMonitor monitor) {
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
            monitor.beginTask(getWaitingLabel(), 3);
            List<IType> projectTypes = getProjectITypes(ipsProject, getCurrentlyNeededIpsObjectType(input));
            monitor.worked(1);

            Collection<IType> rootCandidates = getRootElementsForIType(input, projectTypes,
                    ToChildAssociationType.SELF, new ArrayList<IType>(), new ArrayList<List<PathElement>>(),
                    new ArrayList<PathElement>());
            monitor.worked(1);

            paths = new ArrayList<List<PathElement>>();
            rootComponents = getRootElementsForIType(input, projectTypes, ToChildAssociationType.SELF, rootCandidates,
                    paths, new ArrayList<PathElement>());
            monitor.worked(1);

        } else { // get the root elements if the input is an IpsProject
            monitor.beginTask(getWaitingLabel(), 2);
            List<IType> projectTypes = getProjectITypes(ipsProject, getCurrentlyNeededIpsObjectType());
            monitor.worked(1);

            rootComponents = getProjectRootElementsFromComponentList(projectTypes, associationTypeFilter);
            monitor.worked(1);
        }
        monitor.done();
        return ComponentNode.encapsulateComponentTypes(rootComponents, ipsProject).toArray();
    }

    /**
     * Determines the needed IpsObjectType on base of the showState, this is needed when the root
     * elements for an IIpsProject have to be calculated.
     */
    private IpsObjectType getCurrentlyNeededIpsObjectType() {
        if (showState == ShowTypeState.SHOW_POLICIES) {
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
     * and may be contain a subtype hierarchy. A run of this method with an empty rootCandidates
     * parameter will return a list of root-candidates. A second run of this method with the
     * previously obtained rootCandidates as parameter, will clean the list of false candidates.
     * 
     * @param element the starting point
     * @param componentList the list of all concerned elements
     * @param association the {@link ToChildAssociationType} of the parent element to this element
     * @param rootCandidates a {@link Collection} of {@link IType}.
     * @param foundPaths a {@link List} of paths from the provided element to the computed root
     *            elements
     * @param callHierarchy a {@link List} which contains the path from the current element to the
     *            source element
     */
    Collection<IType> getRootElementsForIType(IType element,
            List<IType> componentList,
            ToChildAssociationType association,
            Collection<IType> rootCandidates,
            List<List<PathElement>> foundPaths,
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
        List<IType> associatingTypes = getAssociatingTypes(element, componentList, associationTypeFilter);
        // Breaking condition
        if (associatingTypes.isEmpty() && supertype == null
                && (association == ToChildAssociationType.SELF || association == ToChildAssociationType.ASSOCIATION)) {
            rootElements.add(element);
            foundPaths.add(callHierarchyTemp);
        }

        // recursive call for all child elements
        for (IType associations : associatingTypes) {
            Collection<IType> rootElementsForIType = getRootElementsForIType(associations, componentList,
                    ToChildAssociationType.ASSOCIATION, rootCandidates, foundPaths, callHierarchyTemp);
            rootElements.addAll(rootElementsForIType);
        }

        if (supertype != null) {
            rootElements.addAll(getRootElementsForIType(supertype, componentList, ToChildAssociationType.SUPERTYPE,
                    rootCandidates, foundPaths, callHierarchyTemp));
        }

        // If a supertype has been added in the first run, it has to be added now, too
        if (rootElements.isEmpty() && association == ToChildAssociationType.SUPERTYPE
                && rootCandidates.contains(element)) {
            rootElements.add(element);
            foundPaths.add(callHierarchyTemp);
        }

        // None of the child elements is a root element, therefore check the association type of the
        // current element
        if (rootElements.isEmpty() && association == ToChildAssociationType.ASSOCIATION) {
            rootElements.add(element);
            foundPaths.add(callHierarchyTemp);
        }

        // If the hierarchy is solely build with supertypes, we must add the source element itself
        if (rootElements.isEmpty() && association == ToChildAssociationType.SELF) {
            rootElements.add(element);
            foundPaths.add(callHierarchyTemp);
        }

        return rootElements;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof ComponentNode && ((ComponentNode)parentElement).isRepetition()) {
            return new Object[0];
        }
        if (parentElement instanceof IModelOverviewNode) {
            return ((IModelOverviewNode)parentElement).getChildren().toArray();
        }
        return new Object[0];
    }

    @Override
    public Object getParent(Object element) {
        IModelOverviewNode node = (IModelOverviewNode)element;
        return node.getParent();
    }

    @Override
    public boolean hasChildren(Object element) {
        return getChildren(element).length != 0;
    }

    /**
     * Returns a {@link List} of {@link List}s of {@link PathElement}s which has been computed by
     * {@link #getRootElementsForIType(IType, List, ToChildAssociationType, Collection, List, List)}
     * or an empty {@link List} otherwise.
     */
    public List<List<PathElement>> getPaths() {
        return paths;
    }

    public void toggleShowTypeState() {
        if (this.showState == ShowTypeState.SHOW_POLICIES) {
            this.showState = ShowTypeState.SHOW_PRODUCTS;
        } else {
            this.showState = ShowTypeState.SHOW_POLICIES;
        }
    }

    public ShowTypeState getShowTypeState() {
        return showState;
    }

    public void setShowTypeState(ShowTypeState showState) {
        this.showState = showState;
    }

    @Override
    protected String getWaitingLabel() {
        return Messages.IpsModelOverview_waitingLabel;
    }

    static enum ToChildAssociationType {
        SELF,
        ASSOCIATION,
        SUPERTYPE
    }

    static enum ShowTypeState {
        SHOW_POLICIES(1),
        SHOW_PRODUCTS(2);
        private final int state;

        ShowTypeState(int value) {
            this.state = value;
        }

        public int getState() {
            return state;
        }
    }

}
