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

import java.util.List;

import org.faktorips.util.ArgumentCheck;

abstract class AbstractStructureNode implements IModelOverviewNode {

    private final ComponentNode parent;

    /*
     * TODO CODE-REVIEW FIPS-1194: Children mit leerer Liste initialisieren, spart NullCheck in
     * addChildren
     */
    private List<ComponentNode> children;

    /**
     * Creates a node with a mandatory parent {@link ComponentNode} and a {@link List} of mandatory
     * {@link #children}.
     * 
     * @param parent the parent, this parameter must not be {@code null}.
     * @param children a {@link List} of {@link ComponentNode component nodes}, this {@link List}
     *            must not be {@code null} or empty.
     * @throws NullPointerException if one of the parameters is {@code null} or the provided list of
     *             children is empty.
     */
    public AbstractStructureNode(ComponentNode parent, List<ComponentNode> children) {
        ArgumentCheck.notNull(parent, "'parent' must not be null."); //$NON-NLS-1$
        ArgumentCheck.notNull(children, "'children' must not be null."); //$NON-NLS-1$
        ArgumentCheck.isTrue(!children.isEmpty(), "'children', must not be empty."); //$NON-NLS-1$

        this.parent = parent;
        addChildren(children);
    }

    @Override
    public List<ComponentNode> getChildren() {
        return children;
    }

    /**
     * Returns the parent node of this node, which should never be {@code null}.
     */
    @Override
    public ComponentNode getParent() {
        return parent;
    }

    private void addChildren(List<ComponentNode> children) {
        /*
         * TODO CODE-REVIEW FIPS-1194: Methode ist private und diese Checks wurden bereits im
         * Konstruktor durchgeführt
         */
        ArgumentCheck.notNull(children, "'children' must not be null."); //$NON-NLS-1$
        ArgumentCheck.isTrue(!children.isEmpty(), "'children', must not be empty."); //$NON-NLS-1$

        if (this.children == null) {
            this.children = children;
        } else {
            this.children.addAll(children);
        }

        for (ComponentNode componentNode : children) {
            componentNode.setParent(this);
        }
    }
}
