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

        // get all components from the input project
        IIpsProject model = (IIpsProject)inputElement;
        List<IIpsSrcFile> srcFiles = new ArrayList<IIpsSrcFile>();
        IpsObjectType[] filter = { IpsObjectType.PRODUCT_CMPT_TYPE, IpsObjectType.POLICY_CMPT_TYPE };
        try {
            List<IType> componentsFromSrcFiles;
            model.findAllIpsSrcFiles(srcFiles, filter);

            // get the root elements
            componentsFromSrcFiles = getComponentsFromSrcFiles(srcFiles);
            rootComponents = getRootComponentTypes(componentsFromSrcFiles);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        Object[] array = encapsulateComponentTypes(rootComponents, null).toArray();
        return array;
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
        List<IModelOverviewNode> childNodes = new ArrayList<IModelOverviewNode>();
        if (parentElement instanceof ComponentNode) {
            // hier die child-elemente auslesen, dann deren unterelemente bauen und hinzufügen

            ComponentNode componentNode = (ComponentNode)parentElement;

            List<IType> subtypes = componentNode.getValue().searchSubtypes(false, false);
            List<IType> associations;
            try {
                associations = getAssociations(componentNode.getValue());
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }

            if (associations.size() > 0) {
                List<ComponentNode> children = encapsulateComponentTypes(associations, componentNode);
                CompositeNode compositeNode = new CompositeNode(componentNode, children);
                // componentNode.setCompositeNode(compositeNode);
                childNodes.add(compositeNode);
            }

            if (subtypes.size() > 0) {
                List<ComponentNode> children = encapsulateComponentTypes(subtypes, componentNode);
                SubtypeNode subtypeNode = new SubtypeNode(componentNode, children);
                // componentNode.setCompositeNode(compositeNode);
                childNodes.add(subtypeNode);
            }

        } else if (parentElement instanceof AbstractStrucureNode) {
            IModelOverviewNode parentNode = (IModelOverviewNode)parentElement;
            return parentNode.getChildren().toArray();
        }
        return childNodes.toArray();
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

    private List<IType> getRootComponentTypes(List<IType> components) throws CoreException {
        List<IType> rootComponents = new ArrayList<IType>();
        for (IType iType : components) {
            if (!iType.hasSupertype() && !isAssociationTarget(iType.getQualifiedName(), components)) {
                rootComponents.add(iType);
            }
        }
        return rootComponents;
    }

    // Encapsulates a list of arbitrary ITypes into a List of ComponentNodes
    private List<ComponentNode> encapsulateComponentTypes(List<IType> components, ComponentNode parent) {
        List<ComponentNode> componentNodes = new ArrayList<ComponentNode>();

        for (IType component : components) {
            componentNodes.add(new ComponentNode(component, parent));
        }

        return componentNodes;
    }

    // Gets the PolicyCmpTypes and ProductCmptTypes from a list of IIpsSrcFiles
    private List<IType> getComponentsFromSrcFiles(List<IIpsSrcFile> srcFiles) throws CoreException {
        List<IType> components = new ArrayList<IType>(srcFiles.size());
        for (IIpsSrcFile file : srcFiles) {
            IType ipsObject = (IType)file.getIpsObject();
            components.add(ipsObject);
        }
        return components;
    }

    // checks if target is contained in the associations from the components list
    private boolean isAssociationTarget(String target, List<IType> components) throws CoreException {
        for (IType component : components) {
            List<IType> associations = getAssociations(component);
            for (IType association : associations) {
                if (association.getQualifiedName().equals(target)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a List of {@link IType component types} which are associated to this {@link IType}
     * component type. The only {@link AssociationType association types} which will be returned are
     * {@link AssociationType}.COMPOSITION_MASTER_TO_DETAIL an {@link AssociationType}.AGGREGATION.
     * 
     * @param object
     * @return a list of associated {@link IType}s
     * @throws CoreException
     */
    private List<IType> getAssociations(IType object) throws CoreException {
        List<IType> associations = new ArrayList<IType>();

        // wie wichtig ist diese Methode? Erfuellt nicht findAllAssociations() den gewuenschten
        // Zweck
        // siehe auch isAssociationTarget!

        List<IAssociation> findAssociations = object.findAllAssociations(object.getIpsProject());
        for (IAssociation association : findAssociations) {
            if (association.getAssociationType().equals(AssociationType.COMPOSITION_MASTER_TO_DETAIL)
                    || association.getAssociationType().equals(AssociationType.AGGREGATION)) {
                associations.add(association.findTarget(association.getIpsProject()));
            }
        }
        return associations;
    }
}
