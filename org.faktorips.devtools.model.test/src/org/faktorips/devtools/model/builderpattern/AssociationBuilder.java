/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builderpattern;

import static java.util.Objects.requireNonNull;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;

public class AssociationBuilder {

    @SuppressWarnings("unused")
    private final IIpsProject project;
    private final AssociationType type;

    private IPolicyCmptType from;
    private IPolicyCmptType to;
    private int min = 0;
    private int max = IAssociation.CARDINALITY_MANY;

    public AssociationBuilder(IIpsProject project, AssociationType type) {
        this.project = project;
        this.type = type;
    }

    /**
     * Sets the association's origin, i.e. the type on which the association is created.
     */
    public AssociationBuilder from(IPolicyCmptType from) {
        this.from = requireNonNull(from);
        return this;
    }

    /**
     * Sets the association's target.
     * 
     * @see IAssociation#setTarget(String)
     */
    public AssociationBuilder to(IPolicyCmptType to) {
        this.to = requireNonNull(to);
        return this;
    }

    /**
     * Sets the association's minimum cardinality.
     * 
     * @see IAssociation#setMinCardinality(int)
     */
    public AssociationBuilder withMinCardinality(int min) {
        this.min = min;
        return this;
    }

    /**
     * Sets the association's maximum cardinality.
     * 
     * @see IAssociation#setMaxCardinality(int)
     */
    public AssociationBuilder withMaxCardinality(int max) {
        this.max = max;
        return this;
    }

    public AssociationBuilder withCardinality(int min, int max) {
        this.min = min;
        this.max = max;
        return this;
    }

    /**
     * Creates the association.
     */
    public IAssociation build() {
        IAssociation association = from.newAssociation();
        association.setAssociationType(type);
        association.setTarget(to.getQualifiedName());
        association.setTargetRoleSingular(to.getQualifiedName());
        association.setTargetRolePlural(to.getQualifiedName());
        association.setMinCardinality(min);
        association.setMaxCardinality(max);
        return association;
    }

    public static class Constrain extends AssociationBuilder {

        @SuppressWarnings("unused")
        private final IAssociation constrainedAssociation;

        public Constrain(IIpsProject project, IAssociation constrainedAssociation) {
            super(project, constrainedAssociation.getAssociationType());
            this.constrainedAssociation = constrainedAssociation;

            to(project.findPolicyCmptType(constrainedAssociation.getTarget()));
            withMinCardinality(constrainedAssociation.getMinCardinality());
            withMaxCardinality(constrainedAssociation.getMaxCardinality());
        }

        @Override
        public IAssociation build() {
            IAssociation association = super.build();
            association.setConstrain(true);
            return association;
        }
    }
}
