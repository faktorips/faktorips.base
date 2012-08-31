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

class CompositeNode extends AbstractStructureNode {

    /**
     * Creates a {@link CompositeNode} with a mandatory parent {@link ComponentNode} and a
     * {@link List} of mandatory children.
     * 
     * @param parent the parent, this parameter must not be {@code null}.
     * @throws NullPointerException if one of the parameters is {@code null} or children is empty.
     */
    public CompositeNode(ComponentNode parent, List<ComponentNode> children) {
        super(parent, children);
    }
}
