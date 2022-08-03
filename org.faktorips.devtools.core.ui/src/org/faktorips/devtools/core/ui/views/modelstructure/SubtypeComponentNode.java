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

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IType;

public class SubtypeComponentNode extends ComponentNode {

    public SubtypeComponentNode(IType value, ComponentNode parent, IIpsProject sourceProject) {
        super(value, sourceProject);
        setParent(parent);
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
        List<SubtypeComponentNode> componentNodes = new ArrayList<>();
        for (IType component : components) {
            SubtypeComponentNode componentNode = new SubtypeComponentNode(component, parent, sourceProject);
            componentNode.setParent(parent);
            componentNodes.add(componentNode);
        }
        return componentNodes;
    }

}
