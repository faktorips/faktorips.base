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

import org.faktorips.util.ArgumentCheck;

public abstract class AbstractStrucureNode implements IModelOverviewNode {

    private final ComponentNode parent;
    private final List<ComponentNode> children;

    /**
     * Creates a node with a mandatory parent {@link ComponentNode} and a List of mandatory
     * {@link #children}.
     * 
     * @param parent the parent, this parameter must not be <tt>null</tt>.
     * @param children a list of {@link ComponentNode component nodes}, this list must not be
     *            <tt>null</tt> or empty.
     * @throws NullPointerException if one of the parameters is <tt>null</tt> or {@link #children}
     *             is empty.
     */
    public AbstractStrucureNode(ComponentNode parent, List<ComponentNode> children) {

        ArgumentCheck.notNull(parent, "'parent' must not be null."); //$NON-NLS-1$
        ArgumentCheck.notNull(children, "'children' must not be null."); //$NON-NLS-1$
        ArgumentCheck.isTrue(!children.isEmpty(), "'children', must not be empty."); //$NON-NLS-1$

        this.parent = parent;
        this.children = children;
    }

    @Override
    public List<IModelOverviewNode> getChildren() {
        List<IModelOverviewNode> nodes = new ArrayList<IModelOverviewNode>();
        nodes.addAll(children);
        return nodes;
    }

    /**
     * Returns the parent node of this node.
     * 
     * @return the {@link ComponentNode} parent node, which should never be <tt>null</tt>
     */
    @Override
    public IModelOverviewNode getParent() {
        return parent;
    }
}
