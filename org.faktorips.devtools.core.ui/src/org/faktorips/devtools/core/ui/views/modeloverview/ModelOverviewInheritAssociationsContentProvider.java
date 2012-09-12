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

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IType;

public class ModelOverviewInheritAssociationsContentProvider extends AbstractModelOverviewContentProvider {

    private final AssociationType[] associationTypes = { AssociationType.AGGREGATION,
            AssociationType.COMPOSITION_MASTER_TO_DETAIL };
    private final IpsObjectType[] ipsObjectTypes = { IpsObjectType.POLICY_CMPT_TYPE, IpsObjectType.PRODUCT_CMPT_TYPE };

    @Override
    public Object[] getChildren(Object parentElement) {

        return null;
    }

    @Override
    public Object getParent(Object element) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected String getWaitingLabel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Object[] collectElements(Object inputElement, IProgressMonitor monitor) {
        // only accept project input
        if (inputElement instanceof IIpsProject) {
            IIpsProject project = (IIpsProject)inputElement;

            List<IType> projectSpecificITypes = getProjectSpecificITypes(getProjectITypes(project, ipsObjectTypes),
                    project);
            List<IType> rootElements = getProjectRootElementsFromComponentList(projectSpecificITypes, associationTypes);
            return ComponentNode.encapsulateComponentTypes(rootElements, project).toArray();
        } else {
            return null;
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

    @Override
    List<AbstractStructureNode> getComponentNodeChildren(ComponentNode parent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    SubtypeNode getComponentNodeSubtypeChild(ComponentNode parent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    CompositeNode getComponentNodeCompositeChild(ComponentNode parent) {
        // TODO Auto-generated method stub
        return null;
    }

}
