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
import java.util.Collection;
import java.util.List;

import org.faktorips.devtools.core.ui.IpsSrcFileViewItem;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.util.ArgumentCheck;

class ComponentNode extends IpsSrcFileViewItem {

    private IType value;
    private IIpsProject sourceProject;
    private ComponentNode parent;
    private boolean hasInheritedAssociation;

    /**
     * Creates a new ComponentNode with designated parent node and value.
     * 
     * @param value the corresponding IType element to this node
     * @param sourceProject the {@link IIpsProject} which should be used to compute project
     *            references
     */
    public ComponentNode(IType value, IIpsProject sourceProject) {
        super(value.getIpsSrcFile());
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
        return getParent().isRepetitionInternal(this.getValue());
    }

    private boolean isRepetitionInternal(IType value) {
        if (this.value.equals(value)) {
            return true;
        } else if (getParent() == null) {
            return false;
        }
        return getParent().isRepetitionInternal(value);
    }

    public ComponentNode getParent() {
        return parent;
    }

    /**
     * @see #getParent()
     * 
     */
    public void setParent(ComponentNode parent) {
        this.parent = parent;
    }

    /**
     * Returns the stored {@link IType} element of this value. This method should never return
     * {@code null}.
     */
    public IType getValue() {
        return value;
    }

    public void setValue(IType value) {
        this.value = value;
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
     * @param parent the parent {@link ComponentNode} for this set of {@link IType ITypes}
     * @param sourceProject the project which is used to compute project references
     * @return a {@link List} of {@link ComponentNode ComponenteNodes}
     */
    static List<ComponentNode> encapsulateComponentTypes(Collection<IType> components,
            ComponentNode parent,
            IIpsProject sourceProject) {
        List<ComponentNode> componentNodes = new ArrayList<ComponentNode>();
        for (IType component : components) {
            ComponentNode componentNode = new ComponentNode(component, sourceProject);
            componentNode.setParent(parent);
            componentNodes.add(componentNode);
        }
        return componentNodes;
    }

    public void setHasInheritedAssociation(boolean hasInheritedAssociation) {
        this.hasInheritedAssociation = hasInheritedAssociation;
    }

    public boolean isTargetOfInheritedAssociation() {
        return this.hasInheritedAssociation;
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
