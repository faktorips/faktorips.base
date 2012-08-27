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
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.util.ArgumentCheck;

public class ModelOverviewContentProvider implements ITreeContentProvider {

    @Override
    public Object[] getElements(Object inputElement) {
        // check input arguments
        ArgumentCheck
                .isInstanceOf(inputElement, IIpsProject.class,
                        "The input element for the ModelOverviewContentProvider.getElements(Oject) must be an instance of IIpsProject."); //$NON-NLS-1$

        List<IType> rootComponents;
        IIpsProject iIpsProject;
        try {
            // get all components from the input project
            IpsObjectType[] filter = { IpsObjectType.PRODUCT_CMPT_TYPE, IpsObjectType.POLICY_CMPT_TYPE };
            List<IIpsSrcFile> srcFiles = new ArrayList<IIpsSrcFile>();

            iIpsProject = (IIpsProject)inputElement;
            iIpsProject.findAllIpsSrcFiles(srcFiles, filter);

            // get the root elements
            rootComponents = getRootComponentTypes(getComponentsFromSrcFiles(srcFiles));
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

        return encapsulateComponentTypes(rootComponents, iIpsProject).toArray();
    }

    private List<IType> getRootComponentTypes(List<IType> components) {
        List<IType> rootComponents = new ArrayList<IType>();
        for (IType iType : components) {
            if (!iType.hasSupertype() && !isAssociationTarget(iType, components)) {
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
     * @param srcFiles a {@link List} of {@link IIpsSrcFile} which represents {@link IType} elements
     * @return a {@link List} of the same size with corresponding {@link IType}, or an empty
     *         {@link List} if the input {@link List} was empty
     */
    private List<IType> getComponentsFromSrcFiles(List<IIpsSrcFile> srcFiles) {
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
    public void dispose() {
        // Nothing to do
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // Nothing to do
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        return ((IModelOverviewNode)parentElement).getChildren().toArray();
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
     * Encapsulates a {@link List} of {@link IType ITypes} into a {@link List} of
     * {@link ComponentNode ComponentNodes}.
     * 
     * @param components the elements which should be encapsulated
     * @return a {@link List} of {@link ComponentNode ComponenteNodes}
     */
    protected static List<ComponentNode> encapsulateComponentTypes(List<IType> components, IIpsProject rootProject) {
        List<ComponentNode> componentNodes = new ArrayList<ComponentNode>();

        for (IType component : components) {
            componentNodes.add(new ComponentNode(component, null, rootProject));
        }

        return componentNodes;
    }

    /**
     * This method checks if an {@link IType} is targeted by an {@link IAssociation}.
     * 
     * @param target the {@link IType} which should be checked on incoming associations
     * @param components a {@link List} of {@link IType} which define the scope of associations that
     *            will be checked
     * @return {@code true}, if any association is directed towards the provided target, otherwise
     *         {@code false}
     */
    private boolean isAssociationTarget(IType target, List<IType> components) {
        for (IType component : components) {
            List<IType> associations = getAssociations(component);
            for (IType association : associations) {
                if (association.getQualifiedName().equals(target.getQualifiedName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a {@link List} of {@link IType component types} which are associated to this
     * {@link IType} component type. The only {@link AssociationType association types} which will
     * be returned are {@link AssociationType}.COMPOSITION_MASTER_TO_DETAIL an
     * {@link AssociationType}.AGGREGATION, that means only associations which are directed away
     * from the argument <tt>object</tt>.
     * 
     * @param rootElement for which the outgoing associations should be returned
     * @return a {@link List} of associated {@link IType}s
     */
    protected static List<IType> getAssociations(IType rootElement) {
        List<IType> associations = new ArrayList<IType>();

        try {
            List<IAssociation> findAssociations = rootElement.getAssociations();
            for (IAssociation association : findAssociations) {
                if (association.getAssociationType().equals(AssociationType.COMPOSITION_MASTER_TO_DETAIL)
                        || association.getAssociationType().equals(AssociationType.AGGREGATION)) {
                    associations.add(association.findTarget(association.getIpsProject()));
                }
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return associations;
    }
}
