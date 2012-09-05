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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.internal.DeferredStructuredContentProvider;

public class ModelOverviewContentProvider extends DeferredStructuredContentProvider implements ITreeContentProvider {

    private List<Deque<PathElement>> paths = new ArrayList<Deque<PathElement>>();
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

            monitor.beginTask(getWaitingLabel(), 3);
            List<IType> projectTypes = getProjectTypes(ipsProject, new IpsObjectType[] {
                    IpsObjectType.POLICY_CMPT_TYPE, IpsObjectType.PRODUCT_CMPT_TYPE });
            monitor.worked(1);

            paths = new ArrayList<Deque<PathElement>>();
            IType input = (IType)inputElement;
            Collection<IType> rootCandidates = getRootElementsForIType(input, projectTypes,
                    ToChildAssociationType.SELF, new ArrayList<IType>(), new ArrayList<Deque<PathElement>>(),
                    new ArrayDeque<PathElement>());
            monitor.worked(1);
            rootComponents = getRootElementsForIType(input, projectTypes, ToChildAssociationType.SELF, rootCandidates,
                    getPaths(), new ArrayDeque<PathElement>());
            monitor.worked(1);

        } else { // get the root elements if the input is an IpsProject
            monitor.beginTask(getWaitingLabel(), 2);
            List<IType> projectTypes;
            if (getShowTypeState() == ShowTypeState.SHOW_POLICIES) {
                projectTypes = getProjectTypes(ipsProject, new IpsObjectType[] { IpsObjectType.POLICY_CMPT_TYPE });
            } else {
                projectTypes = getProjectTypes(ipsProject, new IpsObjectType[] { IpsObjectType.PRODUCT_CMPT_TYPE });
            }
            monitor.worked(1);

            rootComponents = getRootTypes(projectTypes);
            monitor.worked(1);
        }
        monitor.done();
        return ComponentNode.encapsulateComponentTypes(rootComponents, ipsProject).toArray();
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
     * @param callHierarchy a {@link Deque} which contains the path from the current element to the
     *            source element
     */
    Collection<IType> getRootElementsForIType(IType element,
            List<IType> componentList,
            ToChildAssociationType association,
            Collection<IType> rootCandidates,
            List<Deque<PathElement>> foundPaths,
            Deque<PathElement> callHierarchy) {

        Deque<PathElement> callHierarchyTemp = new ArrayDeque<PathElement>(callHierarchy);
        callHierarchyTemp.push(new PathElement(element, association));

        IType supertype;
        try {
            supertype = element.findSupertype(element.getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

        // Breaking condition
        Set<IType> rootElements = new HashSet<IType>();
        List<IType> associatingTypes = getAssociatingTypes(element, componentList, associationTypeFilter);
        if (associatingTypes.isEmpty() && supertype == null
                && (association == ToChildAssociationType.SELF || association == ToChildAssociationType.ASSOCIATION)) {
            rootElements.add(element);
            foundPaths.add(callHierarchyTemp);
        }

        // recursive call for all child elements
        for (IType associations : associatingTypes) {
            rootElements.addAll(getRootElementsForIType(associations, componentList,
                    ToChildAssociationType.ASSOCIATION, rootCandidates, foundPaths, callHierarchyTemp));
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

    /**
     * Computes the root elements of a complete {@link IIpsProject}. An element is considered as
     * root if it is no association target of any other {@link IType} and if it has no supertype
     * {@link IType}.
     * 
     * @param components all components of the concerned {@link IIpsProject}
     * @return a {@link List} of {@link IType} with the root elements, or an empty list if there are
     *         no elements.
     */
    private List<IType> getRootTypes(List<IType> components) {
        List<IType> rootComponents = new ArrayList<IType>();
        for (IType iType : components) {
            if (!iType.hasSupertype() && !isAssociationTarget(iType, components, associationTypeFilter)) {
                rootComponents.add(iType);
            }
        }
        return rootComponents;
    }

    /**
     * Takes a {@link List} of {@link IIpsSrcFile} and extracts the corresponding {@link List} of
     * {@link IType}. It operates only on {@link List Lists} of {@link IIpsSrcFile} which represent
     * {@link IType}
     * 
     * @param ipsProject the {@link IIpsProject} for which the objects should be retrieved
     * @param filter an array of {@list IpsObjectType} to filter the retrieved objects
     * 
     * @return a {@link List} of the same size with corresponding {@link IType}, or an empty
     *         {@link List} if the input {@link List} was empty
     */
    private List<IType> getProjectTypes(IIpsProject ipsProject, IpsObjectType[] filter) {

        List<IIpsSrcFile> srcFiles = new ArrayList<IIpsSrcFile>();
        try {
            ipsProject.findAllIpsSrcFiles(srcFiles, filter);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

        List<IType> components = new ArrayList<IType>(srcFiles.size());
        for (IIpsSrcFile file : srcFiles) {
            try {
                components.add((IType)file.getIpsObject());
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
        return components;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof IModelOverviewNode) {
            return ((IModelOverviewNode)parentElement).getChildren().toArray();
        } else {
            return null;
        }
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
     * Convenience function for {@link #getAssociatingTypes(IType, List, AssociationType...)
     * getAssociatingTypes(IType, List, AssociationType...).isEmpty()}.
     * 
     * @param target the {@link IType} which should be checked on incoming associations
     * @param components a {@link List} of {@link IType} which define the scope of associations that
     *            will be checked
     * @return {@code true}, if any association is directed towards the provided target, otherwise
     *         {@code false}
     */
    private boolean isAssociationTarget(IType target, List<IType> components, AssociationType... filter) {
        return !getAssociatingTypes(target, components, filter).isEmpty();
    }

    /**
     * This method computes a {@link List} of {@link IType ITypes} which targets the indicated
     * target with an {@link IAssociation}, or an empty {@link List} if there are no such
     * associations.
     * 
     * @param target the {@link IType} which should be checked on incoming associations
     * @param components a {@link List} of {@link IType} which define the scope of associations that
     *            will be checked
     */
    private List<IType> getAssociatingTypes(IType target, List<IType> components, AssociationType... filter) {
        List<IType> associatingComponents = new ArrayList<IType>();
        for (IType component : components) {
            List<IType> targets = getAssociationsForAssociationTypes(component, filter);
            if (targets.contains(target)) {
                associatingComponents.add(component);
            }
        }
        return associatingComponents;
    }

    /**
     * Returns a {@link List} of all {@link IAssociation}s which are associated to this
     * {@link IType} and match one of the provided types.
     * 
     * @return a {@link List} of associated {@link IAssociation}s
     */
    protected static List<IType> getAssociationsForAssociationTypes(IType rootElement, AssociationType... types) {
        List<IAssociation> associations = rootElement.getAssociations(types);
        List<IType> associatingTypes = new ArrayList<IType>(associations.size());
        for (IAssociation association : associations) {
            try {
                associatingTypes.add(association.findTarget(rootElement.getIpsProject()));
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
        return associatingTypes;
    }

    /**
     * Returns a {@link List} of {@link Deque}s of {@link PathElement}s which has been computed by
     * {@link #getRootElementsForIType(IType, List, ToChildAssociationType, Collection, List, Deque)}
     * or an empty {@link List} otherwise.
     */
    public List<Deque<PathElement>> getPaths() {
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

    static enum ToChildAssociationType {
        SELF,
        ASSOCIATION,
        SUPERTYPE
    }

    @Override
    protected String getWaitingLabel() {
        return Messages.IpsModelOverview_waitingLabel;
    }

}

enum ShowTypeState {
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
