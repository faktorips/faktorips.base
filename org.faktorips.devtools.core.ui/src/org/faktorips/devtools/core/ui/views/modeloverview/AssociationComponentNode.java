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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;

public class AssociationComponentNode extends ComponentNode {

    private IAssociation association;

    /**
     * Creates a new AssociationComponentNode with designated parent node and value.
     * 
     * @param value the corresponding IType element to this node
     * @param parent the parent node
     * @param rootProject the {@link IIpsProject} which should be used to compute project references
     * @param targetingAssociation the association which points to the provided {@IType}
     * 
     * @see ComponentNode
     */
    private AssociationComponentNode(IType value, CompositeNode parent, IIpsProject rootProject,
            IAssociation targetingAssociation) {
        super(value, parent, rootProject);
        this.association = targetingAssociation;
    }

    /**
     * Factory method for the creation of new {@link AssociationComponentNode
     * AssociationComponentNodes}. It extracts the {@link IType} value, mandatory for the underlying
     * {@link ComponentNode}, from the provided {@link IAssociation}.
     * 
     * @param parent the parent node
     * @param rootProject the {@link IIpsProject} which should be used to compute project references
     * @param targetingAssociation the association which points to the provided {@IType}
     */
    public static AssociationComponentNode newAssociationComponentNode(IAssociation targetingAssociation,
            CompositeNode parent,
            IIpsProject rootProject) {
        try {
            return new AssociationComponentNode(targetingAssociation.findTarget(rootProject), parent, rootProject,
                    targetingAssociation);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * 
     * @return the stored {@link IAssociation} which is targeting this node.
     */
    public IAssociation getAssociation() {
        return this.association;
    }

    /**
     * Encapsulates a {@link List} of {@link IAssociation IAssociations} into a {@link List} of
     * {@link AssociationComponentNode AssociationComponentNodes}.
     * 
     * @param associations the elements which should be encapsulated
     */
    protected static List<AssociationComponentNode> encapsulateAssociationComponentTypes(Collection<IAssociation> associations,
            IIpsProject rootProject) {
        List<AssociationComponentNode> componentNodes = new ArrayList<AssociationComponentNode>();
        for (IAssociation association : associations) {
            componentNodes.add(AssociationComponentNode.newAssociationComponentNode(association, null, rootProject));
        }
        return componentNodes;
    }

}
