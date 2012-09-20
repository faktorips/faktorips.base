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
import java.util.List;

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

public abstract class AbstractModelOverviewContentProvider extends DeferredStructuredContentProvider implements
        ITreeContentProvider {

    protected ShowTypeState showState = ShowTypeState.SHOW_POLICIES;

    /**
     * Computes the root elements of a complete {@link IIpsProject}. An element is considered as
     * root if it is no association target of any other {@link IType} and if it has no supertype
     * {@link IType}.
     * 
     * @param components all components of the concerned {@link IIpsProject}s
     * @return a {@link List} of {@link IType} with the root elements, or an empty list if there are
     *         no elements.
     */
    protected static List<IType> getProjectRootElementsFromComponentList(List<IType> components,
            IIpsProject sourceProject,
            IProgressMonitor monitor,
            AssociationType... types) {
        List<IType> rootComponents = new ArrayList<IType>();
        List<IType> rootCandidates = new ArrayList<IType>();

        // compute the set of Supertype-root-candidates and real root-elements (no (existing)
        // Supertype and no incoming Association)
        for (IType iType : components) {
            if (!hasExistingSupertype(iType, components)) {
                rootCandidates.add(iType);
                if (!isAssociationTarget(iType, components, types)) {
                    rootComponents.add(iType);
                }
            }
        }
        monitor.worked(1);

        // the lists are the same we have found an unambiguous set of root-elements
        if (rootComponents.size() != rootCandidates.size()) {
            removeDescendants(rootCandidates, rootComponents, components, types);
            // Remove an arbitrary element from the candidates list and add it to the root elements
            while (!rootCandidates.isEmpty()) {
                IType newRoot = rootCandidates.remove(0);
                rootComponents.add(newRoot);
                removeDescendants(rootCandidates, rootComponents, components, types);
            }
        }
        monitor.worked(1);

        removeSuperfluousRootElements(rootComponents, sourceProject, types);
        return rootComponents;
    }

    /**
     * Removes all root-elements whose descendants does not include any element from the
     * source-project
     */
    private static void removeSuperfluousRootElements(List<IType> rootCandidates,
            IIpsProject sourceProject,
            AssociationType... types) {
        List<IType> elementsToRemove = new ArrayList<IType>();
        for (IType rootCandidate : rootCandidates) {
            if (!isContainingSourceProjectElement(rootCandidate, sourceProject, new ArrayList<IType>(), types)) {
                elementsToRemove.add(rootCandidate);
            }
        }
        rootCandidates.removeAll(elementsToRemove);
    }

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
        List<IType> descendants = new ArrayList<IType>();
        descendants.addAll(getAssociationsForAssociationTypes(element, types));
        descendants.addAll(element.findSubtypes(false, false, sourceProject));

        for (IType descendingType : descendants) {
            if (descendingType.getIpsProject().equals(sourceProject)) {
                return true;
            } else {
                if (isContainingSourceProjectElement(descendingType, sourceProject,
                        new ArrayList<IType>(callHierarchy), types)) {
                    return true;
                }
            }
        }

        return false;
    }

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
    // FIXME does not remove all descendants -> testcase
    static void removeDescendants(List<IType> rootCandidates,
            List<IType> rootComponents,
            List<IType> components,
            AssociationType... types) {

        List<IType> potentialDescendants = new ArrayList<IType>(components);
        potentialDescendants.removeAll(rootComponents);
        List<IType> descendants = new ArrayList<IType>(rootComponents);

        // remove all descending elements from the candidates list
        while (!descendants.isEmpty()) {
            List<IType> newDescendants = new ArrayList<IType>();
            for (IType potentialDescendant : potentialDescendants) {
                if (isAssociationTarget(potentialDescendant, descendants, types)) {
                    newDescendants.add(potentialDescendant);
                }
                if (hasExistingSupertype(potentialDescendant, descendants)) {
                    newDescendants.add(potentialDescendant);
                }
            }
            potentialDescendants.removeAll(newDescendants);
            rootCandidates.removeAll(descendants); // newDescendants will be removed in the next
                                                   // iteration, except they are empty
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
        List<IType> associatingComponents = new ArrayList<IType>();
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
        List<IType> associatingTypes = new ArrayList<IType>(associations.size());
        for (IAssociation association : associations) {
            try {
                associatingTypes.add(association.findTarget(sourceElement.getIpsProject()));
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
        return associatingTypes;
    }

    /**
     * Retrieves all {@link IType ITypes} which are contained in the indicated project and the
     * projects it depends.
     * 
     * @param ipsProject the {@link IIpsProject} for which the objects should be retrieved
     * @param types an array of {@list IpsObjectType} which should be retrieved
     * 
     * @return a {@link List} of {@link IType ITypes}, or an empty {@link List} if no {@link IType
     *         ITypes} exist.
     */
    protected static List<IType> getProjectITypes(IIpsProject ipsProject, IpsObjectType... types) {

        List<IIpsSrcFile> srcFiles = new ArrayList<IIpsSrcFile>();
        try {
            ipsProject.findAllIpsSrcFiles(srcFiles, types);
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

    /**
     * Returns a {@link List} which may contain at most one {@link CompositeNode} and one
     * {@link SubtypeNode}. The {@link SubtypeNode} will be before the {@link CompositeNode} in the
     * returned {@link List}. This method has to compute the grandchildren of the node, therefore
     * these will be stored in the direct children.
     * 
     * @return a {@link List} with a {@link SubtypeNode} and a {@link CompositeNode}, in this
     *         indicated order when both children exist, otherwise a list with only one element, or
     *         an empty list if no child exists.
     */
    abstract List<AbstractStructureNode> getComponentNodeChildren(ComponentNode parent);

    /**
     * Computes the child {@link SubtypeNode} of this node, if there are any {@link IType}s in the
     * project scope which are subclassing the enclosed {@link IType} of this node.
     * 
     * @param parent the node for which the {@link SubtypeNode} should be computed
     * @return a {@link SubtypeNode}, or {@code null} if there are no subtypes.
     */
    abstract SubtypeNode getComponentNodeSubtypeChild(ComponentNode parent);

    /**
     * Computes the child {@link CompositeNode} of this node, if there are any {@link IType}s in the
     * project scope which are associated by the enclosed {@link IType} of this node.
     * 
     * @param parent the node for which the {@link CompositeNode} should be computed
     * @return a {@link CompositeNode}, or {@code null} if there are no associated types.
     */
    abstract CompositeNode getComponentNodeCompositeChild(ComponentNode parent);

    /**
     * Checks if this type is directly or indirectly associated by another {@link IType} of the same
     * {@link IIpsProject}
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
            try {
                return isAssociated(type.findSupertype(project), projectSpecificITypes, allComponentITypes, project,
                        types);
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
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
        List<IType> projectComponents = new ArrayList<IType>();
        for (IType iType : components) {
            if (iType.getIpsProject().getName().equals(project.getName())) {
                projectComponents.add(iType);
            }
        }
        return projectComponents;
    }
}
