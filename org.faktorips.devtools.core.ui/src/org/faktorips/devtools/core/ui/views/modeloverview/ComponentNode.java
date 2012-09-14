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
import java.util.List;

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.IIpsSrcFileViewItem;
import org.faktorips.util.ArgumentCheck;

class ComponentNode implements IModelOverviewNode, IIpsSrcFileViewItem {

    private IType value;
    private IIpsProject sourceProject;
    private AbstractStructureNode parent;

    /**
     * Creates a new ComponentNode with designated parent node and value.
     * 
     * @param value the corresponding IType element to this node
     * @param sourceProject the {@link IIpsProject} which should be used to compute project
     *            references
     * @throws NullPointerException if value or rootProject is null
     */
    public ComponentNode(IType value, IIpsProject sourceProject) {

        ArgumentCheck.notNull(value, "The value of this node must not be null!"); //$NON-NLS-1$
        ArgumentCheck.notNull(sourceProject, "The rootProject parameter is mandatory"); //$NON-NLS-1$

        this.value = value;
        this.sourceProject = sourceProject;
    }

    /**
     * Checks if any of the parent nodes contains the same value. Two ComponentNodes with the same
     * value are considered equal in this method.
     * 
     * @return {@code true} if any parent node contains the same value, otherwise {@code false}
     */
    public boolean isRepetition() {
        if (getParent() == null) {
            return false;
        }
        return getParent().getParent().isRepetitionInternal(this.getValue());
    }

    private boolean isRepetitionInternal(IType value) {
        if (this.value.equals(value)) {
            return true;
        } else if (getParent() == null) {
            return false;
        }
        return getParent().getParent().isRepetitionInternal(value);
    }

    @Override
    public AbstractStructureNode getParent() {
        return parent;
    }

    /**
     * @see #getParent()
     * 
     */
    public void setParent(AbstractStructureNode parent) {
        this.parent = parent;
    }

    /**
     * Returns the stored {@link IType} element of this value. This method should never return
     * {@code null}.
     */
    public IType getValue() {
        return value;
    }

    /**
     * Returns the {@link IIpsProject} for which this node has been created.
     */
    IIpsProject getSourceIpsProject() {
        return this.sourceProject;
    }

    /**
     * Encapsulates a {@link List} of {@link IType ITypes} into a {@link List} of
     * {@link ComponentNode ComponentNodes}.
     * 
     * @param components the elements which should be encapsulated
     * @param sourceProject the project which is used to compute project references
     * @return a {@link List} of {@link ComponentNode ComponenteNodes}
     */
    protected static List<ComponentNode> encapsulateComponentTypes(Collection<IType> components,
            IIpsProject sourceProject) {
        List<ComponentNode> componentNodes = new ArrayList<ComponentNode>();
        for (IType component : components) {
            componentNodes.add(new ComponentNode(component, sourceProject));
        }
        return componentNodes;
    }

    @Override
    public IIpsSrcFile getWrappedIpsSrcFile() {
        return this.getIpsSrcFile();
    }

    @SuppressWarnings("rawtypes")
    // method defined in supertype, cannot remove the warning here!
    @Override
    public Object getAdapter(Class adapter) {
        return this.getValue().getAdapter(adapter);
    }

    @Override
    public IIpsSrcFile getIpsSrcFile() {
        return this.getValue().getIpsSrcFile();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result + ((sourceProject == null) ? 0 : sourceProject.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ComponentNode other = (ComponentNode)obj;
        if (parent == null) {
            if (other.parent != null) {
                return false;
            }
        } else if (!parent.equals(other.parent)) {
            return false;
        }
        if (sourceProject == null) {
            if (other.sourceProject != null) {
                return false;
            }
        } else if (!sourceProject.equals(other.sourceProject)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

}
