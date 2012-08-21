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

/* TODO CODE-REVIEW FIPS-1194: package-private sollte reichen */
public class CompositeNode extends AbstractStrucureNode {

    /**
     * Creates a {@link CompositeNode} with a mandatory parent {@link ComponentNode} and a List of
     * mandatory <tt>children</tt>.
     * 
     * @param parent the parent, this parameter must not be <tt>null</tt>.
     * @param children a list of {@link ComponentNode component nodes}, this list must not be
     *            <tt>null</tt> or empty.
     * @throws NullPointerException if one of the parameters is <tt>null</tt> or <tt>children</tt>
     *             is empty.
     */
    public CompositeNode(ComponentNode parent, List<ComponentNode> children) {
        super(parent, children);
    }

}
