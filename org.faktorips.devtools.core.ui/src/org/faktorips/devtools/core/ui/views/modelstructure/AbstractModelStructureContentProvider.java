/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelstructure;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.faktorips.devtools.core.ui.internal.DeferredStructuredContentProvider;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;

public abstract class AbstractModelStructureContentProvider extends DeferredStructuredContentProvider implements
        ITreeContentProvider {

    /**
     * Provides information about the currently shown {@link IType}s. It will be set by the
     * {@link ModelStructure}, but the content provider is responsible to act on this state.
     */
    private ShowTypeState showState = ShowTypeState.SHOW_POLICIES;

    /**
     * Computes the root elements of a complete {@link IIpsProject}. An element is considered as
     * root if it is no association target of any other {@link IType} and if it has no supertype
     * {@link IType}.
     * 
     * @param components all components of the concerned {@link IIpsProject}s
     * @param sourceProject the selected {@link IIpsProject}
     * @param monitor an {@link IProgressMonitor} which consumes three units of work
     * @param types the {@link AssociationType}s which should be considered for the root element
     *            computation
     * @return a {@link List} of {@link IType} with the root elements, or an empty list if there are
     *         no elements.
     */
    protected static List<IType> getProjectRootElementsFromComponentList(List<IType> components,
            IIpsProject sourceProject,
            IProgressMonitor monitor,
            AssociationType... types) {
        List<IType> rootComponents = new ArrayList<>();
        List<IType> rootCandidates = new ArrayList<>();

        SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

        SubMonitor loopProgress = subMonitor.newChild(30).setWorkRemaining(components.size());

        // compute the set of Supertype-root-candidates and real root-elements (no (existing)
        // Supertype and no incoming Association)
        for (IType iType : components) {
            if (!hasExistingSupertype(iType, components)) {
                rootCandidates.add(iType);
                if (!isAssociationTarget(iType, components, types)) {
                    rootComponents.add(iType);
                }
            }
            loopProgress.worked(1);
        }

        if (rootComponents.size() != rootCandidates.size()) {
            removeDescendants(rootCandidates, rootComponents, components, types);
            SubMonitor whileProgress = subMonitor.newChild(40).setWorkRemaining(rootCandidates.size());
            // Remove an arbitrary element from the candidates list and add it to the root elements
            while (!rootCandidates.isEmpty()) {
                IType newRoot = rootCandidates.remove(0);
                rootComponents.add(newRoot);
                removeDescendants(rootCandidates, rootComponents, components, types);
                whileProgress.worked(1);
            }
        }
        subMonitor.setWorkRemaining(30);

        subMonitor.newChild(30);
        removeSuperfluousRootElements(rootComponents, sourceProject, types);
        subMonitor.worked(30);
        return rootComponents;
    }

    /**
     * Removes all root-elements whose descendants does not include any element from the
     * source-project.
     */
    private static void removeSuperfluousRootElements(List<IType> rootCandidates,
            IIpsProject sourceProject,
            AssociationType... types) {
        List<IType> elementsToRemove = new ArrayList<>();
        for (IType rootCandidate : rootCandidates) {
            if (!isContainingSourceProjectElement(rootCandidate, sourceProject, new ArrayList<IType>(), types)) {
                elementsToRemove.add(rootCandidate);
            }
        }
        rootCandidates.removeAll(elementsToRemove);
    }

    /**
     * Checks if the tree of descendants of an {@IType} element contains an element of a specific
     * {@link IIpsProject}.
     * 
     * @param element the {@link IType} root element of the tree
     * @param sourceProject the {@link IIpsProject} for which the existence of an element is
     *            evaluated
     * @param callHierarchy a {@link List} of {@link IType} which contains the elements which
     *            already have been checked
     * @param types the {@link AssociationType}s which should be considered for the computation of
     *            element associations
     * @return {@code true} if the indicated tree contains an element of the provided project,
     *         otherwise {@code false}
     */
    private static boolean isContainingSourceProjectElement(IType element,
            IIpsProject sourceProject,
            List<IType> callHierarchy,
            AssociationType[] types) {

        if (callHierarchy.contains(element)) {
            return false;
        } else {
            callHierarchy.add(element);
        }

        if (element.getIpsProject().equals(sourceProject)) {
            return true;
        }

        // check associations and subtypes
        List<IType> descendants = new ArrayList<>();
        descendants.addAll(getAssociationsForAssociationTypes(element, types));
        descendants.addAll(element.findSubtypes(false, false, sourceProject));

        for (IType descendingType : descendants) {
            if (descendingType.getIpsProject().equals(sourceProject)) {
                return true;
            } else {
                if (isContainingSourceProjectElement(descendingType, sourceProject,
                        new ArrayList<>(callHierarchy), types)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Convenience function for {@link #getExistingSupertypeFromList(IType, List)} {@code != null}.
     */
    protected static boolean hasExistingSupertype(IType type, List<IType> components) {
        return getExistingSupertypeFromList(type, components) != null;
    }

    /**
     * Returns the supertype of the provided {@link IType} element, if it is contained in the
     * provided components list, otherwise {@code null}.
     * 
     * @param type the base {@link IType}
     * @param components a {@link List} of {@link IType ITypes} containing all types from the
     *            relevant project scope
     */
    protected static IType getExistingSupertypeFromList(IType type, List<IType> components) {
        String supertype = type.getSupertype();
        for (IType supertypeCandidate : components) {
            if (supertypeCandidate.getQualifiedName().equals(supertype)) {
                return supertypeCandidate;
            }
        }
        return null;
    }

    /**
     * Removes all elements from the {@link List} of root candidates, which are root elements or
     * somehow are associated to them. Afterwards rootCandidates consist of a {@link List} of root
     * candidates which are not associated to the provided rootComponents.
     * 
     * @param rootComponents list of assured root elements
     * @param rootCandidates list of potential root elements (for example all elements without a
     *            supertype)
     * @param components list of all components to be observed
     * @param types an array of {@link AssociationType} which should be recognized. Make sure it is
     *            not empty, otherwise nothing will be removed!
     */
    static void removeDescendants(List<IType> rootCandidates,
            List<IType> rootComponents,
            List<IType> components,
            AssociationType... types) {

        List<IType> potentialDescendants = new ArrayList<>(components);
        potentialDescendants.removeAll(rootComponents);
        List<IType> descendants = new ArrayList<>(rootComponents);

        // remove all descending elements from the candidates list
        while (!descendants.isEmpty()) {
            List<IType> newDescendants = new ArrayList<>();
            for (IType potentialDescendant : potentialDescendants) {
                if (isAssociationTarget(potentialDescendant, descendants, types)) {
                    newDescendants.add(potentialDescendant);
                }
                if (hasExistingSupertype(potentialDescendant, descendants)) {
                    newDescendants.add(potentialDescendant);
                }
            }
            potentialDescendants.removeAll(newDescendants);
            rootCandidates.removeAll(descendants);
            descendants = newDescendants;
        }
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
    protected static boolean isAssociationTarget(IType target, List<IType> components, AssociationType... types) {
        return !getAssociatingTypes(target, components, types).isEmpty();
    }

    /**
     * This method computes a {@link List} of {@link IType ITypes} which target the indicated target
     * with an {@link IAssociation}, or an empty {@link List} if there are no such associations.
     * 
     * @param target the {@link IType} which should be checked on incoming associations
     * @param components a {@link List} of {@link IType} which define the scope of associations that
     *            will be checked
     */
    protected static List<IType> getAssociatingTypes(IType target, List<IType> components, AssociationType... types) {
        List<IType> associatingComponents = new ArrayList<>();
        for (IType component : components) {
            List<IType> targets = getAssociationsForAssociationTypes(component, types);
            if (targets.contains(target)) {
                associatingComponents.add(component);
            }
        }
        return associatingComponents;
    }

    /**
     * Returns a {@link List} of all {@link IAssociation}s which are associated by this
     * {@link IType} and match one of the provided types.
     * 
     * @return a {@link List} of associated {@link IAssociation}s
     */
    protected static List<IType> getAssociationsForAssociationTypes(IType sourceElement, AssociationType... types) {
        List<IAssociation> associations = sourceElement.getAssociations(types);
        List<IType> associatingTypes = new ArrayList<>(associations.size());
        for (IAssociation association : associations) {
            associatingTypes.add(association.findTarget(sourceElement.getIpsProject()));
        }
        return associatingTypes;
    }

    /**
     * Retrieves all {@link IType ITypes} which are contained in the indicated project and the
     * projects it depends.
     * 
     * @param ipsProject the {@link IIpsProject} for which the objects should be retrieved
     * @param types an array of {@link IpsObjectType} which should be retrieved
     * 
     * @return a {@link List} of {@link IType ITypes}, or an empty {@link List} if no {@link IType
     *         ITypes} exist.
     */
    protected static List<IType> getProjectITypes(IIpsProject ipsProject, IpsObjectType... types) {
        List<IIpsSrcFile> srcFiles = ipsProject.findAllIpsSrcFiles(types);

        List<IType> components = new ArrayList<>(srcFiles.size());
        for (IIpsSrcFile file : srcFiles) {
            components.add((IType)file.getIpsObject());
        }
        return components;
    }

    /**
     * Toggles the show type state.<br>
     * <p>
     * <strong>Example:</strong> <br>
     * <ol>
     * <li>{@link #getShowTypeState()} returns {@link ShowTypeState
     * ShowTypeState.SHOW_POLICIES}</li>
     * <li>{@link #toggleShowTypeState()}</li>
     * <li>{@link #getShowTypeState()} return {@link ShowTypeState ShowTypeState.SHOW_PRODUCTS}</li>
     * </ol>
     * and the other way round
     */
    public final void toggleShowTypeState() {
        if (this.showState == ShowTypeState.SHOW_POLICIES) {
            this.showState = ShowTypeState.SHOW_PRODUCTS;
        } else {
            this.showState = ShowTypeState.SHOW_POLICIES;
        }
    }

    public final ShowTypeState getShowTypeState() {
        return showState;
    }

    /**
     * Sets the show type state of the content provider.
     */
    public final void setShowTypeState(ShowTypeState showState) {
        this.showState = showState;
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

        private ShowTypeState(int value) {
            this.state = value;
        }

        public int getState() {
            return state;
        }
    }

    @Override
    public final Object[] getChildren(Object parentElement) {
        if (parentElement instanceof ComponentNode) {
            ComponentNode node = (ComponentNode)parentElement;
            if (node.isRepetition()) {
                return new Object[0];
            }
            return getComponentNodeChildren(node).toArray();
        }
        return new Object[0];
    }

    /**
     * Returns a {@link List} consisting of {@link SubtypeComponentNode}s and
     * {@link AssociationComponentNode}s. The {@link SubtypeComponentNode}s will be before the
     * {@link AssociationComponentNode}s in the returned {@link List}.
     * 
     * @return a list with the computed children, or an empty list if there are no children
     */
    final List<ComponentNode> getComponentNodeChildren(ComponentNode parent) {
        List<ComponentNode> children = new ArrayList<>();

        List<SubtypeComponentNode> subtypeChildren = getComponentNodeSubtypeChildren(parent);
        if (subtypeChildren != null) {
            children.addAll(subtypeChildren);
        }

        List<AssociationComponentNode> associationChildren = getComponentNodeAssociationChildren(parent);
        if (associationChildren != null) {
            children.addAll(associationChildren);
        }

        return children;
    }

    /**
     * Computes the {@link SubtypeComponentNode} children of this node, if there are any
     * {@link IType}s in the project scope which are subclassing the enclosed {@link IType} of this
     * node.
     * 
     * @param parent the node for which the children should be computed
     * @return a {@link SubtypeComponentNode}, or {@code null} if there are no subtypes.
     */
    protected abstract List<SubtypeComponentNode> getComponentNodeSubtypeChildren(ComponentNode parent);

    /**
     * Computes the {@link AssociationComponentNode} children of this node, if there are any
     * {@link IType}s in the project scope which are associated by the enclosed {@link IType} of
     * this node.
     * 
     * @param parent the node for which the children should be computed
     * @return a {@link AssociationComponentNode}, or {@code null} if there are no associated types.
     */
    protected abstract List<AssociationComponentNode> getComponentNodeAssociationChildren(ComponentNode parent);

    /**
     * Checks if this type is directly or indirectly associated by another {@link IType} of the
     * same. {@link IIpsProject}
     * 
     * @param type the {@link IType} for which the associations should be checked
     * @param projectSpecificITypes a {@link List} of {@link IType}s, containing all ITypes of the
     *            provided {@code type} {@link IIpsProject}
     * @param allComponentITypes a {@link List} of {@link IType}s, containing all ITypes of the
     *            project, including those from referenced projects
     * @param project the project scope for this operation
     * @return {@code true} if there is a direct or inherited association, otherwise {@code false}
     */
    protected static boolean isAssociated(IType type,
            List<IType> projectSpecificITypes,
            List<IType> allComponentITypes,
            IIpsProject project,
            AssociationType... types) {

        if (type == null) {
            return false;
        }
        if (project == null) {
            project = type.getIpsProject();
        }
        if (isAssociationTarget(type, projectSpecificITypes, types)) {
            return true;
        } else {
            List<IType> associatingTypes = getAssociatingTypes(type, allComponentITypes, types);
            for (IType associatingType : associatingTypes) {
                List<IType> associationSubtypes = associatingType.findSubtypes(true, false, project);
                for (IType associationSubtype : associationSubtypes) {
                    if (associationSubtype.getIpsProject().equals(project)) {
                        return true;
                    }
                }
            }
            return isAssociated(type.findSupertype(project), projectSpecificITypes, allComponentITypes, project, types);
        }
    }

    /**
     * Takes a {@link List} of {@link IType} and extracts all elements which are contained in the
     * provided {@link IIpsProject} into a new list.
     * 
     * @param components a list of {@link IType}
     * @param project the project for which the {@link IType}s should be retrieved
     * @return a {@link List} of {@link IType}, or an empty list if no provided elements are
     *         contained in this project
     */
    protected static List<IType> getProjectSpecificITypes(List<IType> components, IIpsProject project) {
        List<IType> projectComponents = new ArrayList<>();
        for (IType iType : components) {
            if (iType.getIpsProject().getName().equals(project.getName())) {
                projectComponents.add(iType);
            }
        }
        return projectComponents;
    }

    protected abstract List<ComponentNode> getStoredRootElements();
}
