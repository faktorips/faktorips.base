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

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IType;

public class SubtypeComponentNode extends ComponentNode {

    public SubtypeComponentNode(IType value, ComponentNode parent, IIpsProject sourceProject) {
        super(value, sourceProject);
        this.setParent(parent);
    }

    /**
     * Encapsulates a {@link List} of {@link IType ITypes} into a {@link List} of
     * {@link SubtypeComponentNode SubtypeComponentNodes}.
     * 
     * @param components the elements which should be encapsulated
     * @param parent the parent {@link SubtypeComponentNode} for this set of {@link IType ITypes}
     * @param sourceProject the project which is used to compute project references
     * @return a {@link List} of {@link SubtypeComponentNode SubtypeComponenteNodes}
     */
    static List<SubtypeComponentNode> encapsulateSubtypeComponentTypes(Collection<IType> components,
            ComponentNode parent,
            IIpsProject sourceProject) {
        List<SubtypeComponentNode> componentNodes = new ArrayList<SubtypeComponentNode>();
        for (IType component : components) {
            SubtypeComponentNode componentNode = new SubtypeComponentNode(component, parent, sourceProject);
            componentNode.setParent(parent);
            componentNodes.add(componentNode);
        }
        return componentNodes;
    }

}
