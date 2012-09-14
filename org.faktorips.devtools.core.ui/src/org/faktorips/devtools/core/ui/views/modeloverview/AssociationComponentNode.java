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

    // private IAssociation association;
    private int minCardinality;
    private int maxCardinality;
    private String targetRoleSingular;
    private boolean isInherited;

    /**
     * Creates a new AssociationComponentNode with designated parent node and value.
     * 
     * @param value the corresponding IType element to this node
     * @param rootProject the {@link IIpsProject} which should be used to compute project references
     * @param targetingAssociation the association which points to the provided {@IType}
     * 
     * @see ComponentNode
     */
    private AssociationComponentNode(IType value, IIpsProject rootProject, IAssociation targetingAssociation) {
        super(value, rootProject);
        this.minCardinality = targetingAssociation.getMinCardinality();
        this.maxCardinality = targetingAssociation.getMaxCardinality();
        this.targetRoleSingular = targetingAssociation.getTargetRoleSingular();
        this.isInherited = false;
    }

    /**
     * Creates a new AssociationComponentNode with designated parent node and value.
     * 
     * @param targetType the {@link IType} value of this node
     * @param minCardinality the minimal cardinality of the association
     * @param maxCardinality the maximal cardinality of the association
     * @param targetRoleSingular the singular role name
     * @param rootProject the project which defines the project-dependency scope
     * @param isInherited indicates if the association is inherited
     */
    private AssociationComponentNode(IType targetType, int minCardinality, int maxCardinality,
            String targetRoleSingular, IIpsProject rootProject, boolean isInherited) {
        super(targetType, rootProject);
        this.minCardinality = minCardinality;
        this.maxCardinality = maxCardinality;
        this.targetRoleSingular = targetRoleSingular;
        this.isInherited = isInherited;
    }

    /**
     * Factory method for the creation of new {@link AssociationComponentNode
     * AssociationComponentNodes}. It extracts the {@link IType} value, mandatory for the underlying
     * {@link ComponentNode}, from the provided {@link IAssociation}.
     * 
     * @param rootProject the {@link IIpsProject} which should be used to compute project references
     * @param targetingAssociation the association which points to the provided {@IType}
     */
    public static AssociationComponentNode newAssociationComponentNode(IAssociation targetingAssociation,
            IIpsProject rootProject) {
        try {
            return new AssociationComponentNode(targetingAssociation.findTarget(rootProject), rootProject,
                    targetingAssociation);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Factory method for the creation of new {@link AssociationComponentNode
     * AssociationComponentNodes}.
     * 
     * @param targetType the {@link IType} value of this node
     * @param minCardinality the minimal cardinality of the association
     * @param maxCardinality the maximal cardinality of the association
     * @param targetRoleSingular the singular role name
     * @param rootProject the project which defines the project-dependency scope
     * @param isInherited indicates if the association is inherited
     */
    public static AssociationComponentNode newAssociationComponentNode(IType targetType,
            int minCardinality,
            int maxCardinality,
            String targetRoleSingular,
            IIpsProject rootProject,
            boolean isInherited) {
        return new AssociationComponentNode(targetType, minCardinality, maxCardinality, targetRoleSingular,
                rootProject, isInherited);
    }

    public int getMinCardinality() {
        return this.minCardinality;
    }

    public int getMaxCardinality() {
        return this.maxCardinality;
    }

    public String getTargetRoleSingular() {
        return this.targetRoleSingular;
    }

    public boolean isInherited() {
        return this.isInherited;
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
            componentNodes.add(AssociationComponentNode.newAssociationComponentNode(association, rootProject));
        }
        return componentNodes;
    }
}
