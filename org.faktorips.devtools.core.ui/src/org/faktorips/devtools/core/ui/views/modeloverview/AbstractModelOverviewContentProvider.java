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

        // the lists are the same we have found an unambiguous set of root-elements
        if (rootComponents.size() == rootCandidates.size()) {
            return rootComponents;
        } else {
            removeDescendants(rootCandidates, rootComponents, components, types);
            // Remove an arbitrary element from the candidates list and add it to the root elements
            while (!rootCandidates.isEmpty()) {
                IType newRoot = rootCandidates.remove(0);
                rootComponents.add(newRoot);
                removeDescendants(rootCandidates, rootComponents, components, types);
            }
        }
        return rootComponents;
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
    private static void removeDescendants(List<IType> rootCandidates,
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
     * This method computes a {@link List} of {@link IType ITypes} which targets the indicated
     * target with an {@link IAssociation}, or an empty {@link List} if there are no such
     * associations.
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
     * Returns a {@link List} of all {@link IAssociation}s which are associated to this
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
}
