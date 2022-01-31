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

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;

public class AssociationComponentNode extends ComponentNode {

    private final int minCardinality;
    private final int maxCardinality;
    private final String targetRoleSingular;
    private IType targetingType;
    private boolean isInherited;
    private final boolean isDerivedUnion;
    private final boolean isSubsetOfADerivedUnion;
    private final String subsettedDerivedUnion;

    /**
     * Creates a new AssociationComponentNode with designated parent node and value.
     * 
     * @param targetType the corresponding IType element to this node
     * @param rootProject the {@link IIpsProject} which should be used to compute project references
     * @param targetingAssociation the association which points to the provided {@IType}
     * 
     * @see ComponentNode
     */
    private AssociationComponentNode(IType targetType, ComponentNode parent, IIpsProject rootProject,
            IAssociation targetingAssociation) {
        super(targetType, rootProject);
        this.targetingType = targetingAssociation.getType();
        this.minCardinality = targetingAssociation.getMinCardinality();
        this.maxCardinality = targetingAssociation.getMaxCardinality();
        this.targetRoleSingular = targetingAssociation.getTargetRoleSingular();
        this.isInherited = false;
        this.isDerivedUnion = targetingAssociation.isDerivedUnion();
        this.isSubsetOfADerivedUnion = targetingAssociation.isSubsetOfADerivedUnion();
        this.subsettedDerivedUnion = targetingAssociation.getSubsettedDerivedUnion();
        this.setParent(parent);
    }

    /**
     * Factory method for the creation of new {@link AssociationComponentNode
     * AssociationComponentNodes}. It extracts all mandatory attributes, except the targetType, for
     * the underlying {@link ComponentNode}, from the provided {@link IAssociation}.
     * 
     * @param targetType the corresponding IType element to this node
     * @param targetingAssociation the {@link IAssociation} which contains the association target
     * @param rootProject the {@link IIpsProject} which should be used to compute project references
     */
    public static AssociationComponentNode newAssociationComponentNode(IType targetType,
            IAssociation targetingAssociation,
            ComponentNode parent,
            IIpsProject rootProject) {
        return new AssociationComponentNode(targetType, parent, rootProject, targetingAssociation);
    }

    /**
     * Factory method for the creation of new {@link AssociationComponentNode
     * AssociationComponentNodes}. It extracts the {@link IType} value, mandatory for the underlying
     * {@link ComponentNode}, from the provided {@link IAssociation}.
     * 
     * @param targetingAssociation the {@link IAssociation} which contains the association target
     * @param rootProject the {@link IIpsProject} which should be used to compute project references
     */
    public static AssociationComponentNode newAssociationComponentNode(IAssociation targetingAssociation,
            ComponentNode parent,
            IIpsProject rootProject) {
        return new AssociationComponentNode(targetingAssociation.findTarget(rootProject), parent, rootProject,
                targetingAssociation);
    }

    public void setTargetingType(IType type) {
        this.targetingType = type;
    }

    public IType getTargetingType() {
        return this.targetingType;
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

    /**
     * Indicates if the underlying association is inherited.
     */
    public boolean isInherited() {
        return this.isInherited;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (isInherited ? 1231 : 1237);
        result = prime * result + maxCardinality;
        result = prime * result + minCardinality;
        result = prime * result + ((targetRoleSingular == null) ? 0 : targetRoleSingular.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AssociationComponentNode other = (AssociationComponentNode)obj;
        if (isInherited != other.isInherited) {
            return false;
        }
        if (maxCardinality != other.maxCardinality) {
            return false;
        }
        if (minCardinality != other.minCardinality) {
            return false;
        }
        if (targetRoleSingular == null) {
            if (other.targetRoleSingular != null) {
                return false;
            }
        } else if (!targetRoleSingular.equals(other.targetRoleSingular)) {
            return false;
        }
        return true;
    }

    public boolean isDerivedUnion() {
        return isDerivedUnion;
    }

    public boolean isSubsetOfADerivedUnion() {
        return isSubsetOfADerivedUnion;
    }

    public String getSubsettedDerivedUnion() {
        return subsettedDerivedUnion;
    }

    public void setInherited(boolean inherited) {
        this.isInherited = inherited;
    }
}
