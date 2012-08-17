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

import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.util.ArgumentCheck;

public class ComponentNode implements IModelOverviewNode {

    private IType value;
    private IModelOverviewNode parent;
    private SubtypeNode subtypeNode;

    private CompositeNode compositeNode;

    /**
     * @param value the corresponding IType element to this node
     * @param parent the parent node
     * @throws NullPointerException if <tt>value</tt> is null
     */
    public ComponentNode(IType value, IModelOverviewNode parent) {

        ArgumentCheck.notNull(value, "The value of this node must not be null!"); //$NON-NLS-1$

        this.parent = parent;
        this.value = value;
    }

    /**
     * Returns a list which may contain at most one {@link CompositeNode} and one
     * {@link SubtypeNode}.
     * 
     */
    @Override
    public List<IModelOverviewNode> getChildren() {
        List<IModelOverviewNode> children = new ArrayList<IModelOverviewNode>();
        if (compositeNode != null) {
            children.add(compositeNode);
        }
        if (subtypeNode != null) {
            children.add(subtypeNode);
        }
        return children;
    }

    @Override
    public IModelOverviewNode getParent() {
        return parent;
    }

    /**
     * Returns the stored IType element of this value. This method should never return <tt>null</tt>
     * .
     * 
     * @return
     */
    public IType getValue() {
        return value;
    }

    /**
     * Sets the {@link SubtypeNode} which will be returned by {@link #getChildren()}.
     * 
     * 
     * @param subtypeNode
     */
    public void setSubtypeNode(SubtypeNode subtypeNode) {
        this.subtypeNode = subtypeNode;
    }

    /**
     * Sets the {@link CompositeNode} which will be returned by {@link #getChildren()}.
     * 
     * @param compositeNode
     */
    public void setCompositeNode(CompositeNode compositeNode) {
        this.compositeNode = compositeNode;
    }
}
