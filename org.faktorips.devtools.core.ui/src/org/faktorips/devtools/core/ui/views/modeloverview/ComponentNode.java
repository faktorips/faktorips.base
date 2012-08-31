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

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.IIpsSrcFileViewItem;
import org.faktorips.util.ArgumentCheck;

class ComponentNode implements IModelOverviewNode, IIpsSrcFileViewItem {

    private IType value;
    private IIpsProject rootProject;
    private List<AbstractStructureNode> children;
    private AbstractStructureNode parent;
    private CompositeNode compositeChild;
    private SubtypeNode subtypeChild;

    /**
     * Creates a new ComponentNode with designated <tt>parent</tt> node and <tt>value</tt>. Use this
     * constructor if the parent node is already known at creation time.
     * 
     * @param value the corresponding IType element to this node
     * @param parent the parent node
     * @throws NullPointerException if <tt>value</tt> is null
     */
    public ComponentNode(IType value, AbstractStructureNode parent, IIpsProject rootProject) {

        ArgumentCheck.notNull(value, "The value of this node must not be null!"); //$NON-NLS-1$
        ArgumentCheck.notNull(rootProject, "The rootProject parameter is mandatory"); //$NON-NLS-1$

        this.parent = parent;
        this.value = value;
        this.rootProject = rootProject;
    }

    /**
     * Returns a {@link List} which may contain at most one {@link CompositeNode} and one
     * {@link SubtypeNode}. The {@link SubtypeNode} will be before the {@link CompositeNode} in the
     * returned {@link List}. This method has to compute the grandchildren of the node, therefore
     * these will be stored in the direct children.
     */
    @Override
    public List<AbstractStructureNode> getChildren() {
        if (children != null) {
            return children;
        }
        List<AbstractStructureNode> children = new ArrayList<AbstractStructureNode>();

        addSubtypeChild(children);
        addCompositeChild(children);

        return children;
    }

    private void addCompositeChild(List<AbstractStructureNode> children) {
        List<IType> associations = ModelOverviewContentProvider.getAssociations(this.getValue());
        List<ComponentNode> compositeNodeChildren = ModelOverviewContentProvider.encapsulateComponentTypes(
                associations, this.rootProject);
        if (!compositeNodeChildren.isEmpty()) {
            CompositeNode compositeNode = new CompositeNode(this, compositeNodeChildren);
            children.add(compositeNode);
            this.compositeChild = compositeNode;
        }
    }

    private void addSubtypeChild(List<AbstractStructureNode> children) {
        List<IType> subtypes = this.getValue().findSubtypes(false, false, this.rootProject);
        List<ComponentNode> subtypeNodeChildren = ModelOverviewContentProvider.encapsulateComponentTypes(subtypes,
                this.rootProject);
        if (!subtypeNodeChildren.isEmpty()) {
            SubtypeNode subtypeNode = new SubtypeNode(this, subtypeNodeChildren);
            children.add(subtypeNode);
            this.subtypeChild = subtypeNode;
        }
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
    IIpsProject getRootIpsProject() {
        return this.rootProject;
    }

    public CompositeNode getCompositeChild() {
        if (children == null) {
            getChildren();
        }
        return compositeChild;
    }

    public SubtypeNode getSubtypeChild() {
        if (children == null) {
            getChildren();
        }
        return subtypeChild;
    }

    @Override
    public IIpsSrcFile getWrappedIpsSrcFile() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getAdapter(Class adapter) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IIpsSrcFile getIpsSrcFile() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result + ((rootProject == null) ? 0 : rootProject.hashCode());
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
        if (rootProject == null) {
            if (other.rootProject != null) {
                return false;
            }
        } else if (!rootProject.equals(other.rootProject)) {
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
