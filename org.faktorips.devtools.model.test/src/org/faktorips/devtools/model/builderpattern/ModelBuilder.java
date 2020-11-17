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

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;

public class ModelBuilder {

    private final IIpsProject project;

    public ModelBuilder(IIpsProject project) {
        this.project = project;
    }

    public AssociationBuilder createComposition() {
        return create(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
    }

    public AssociationBuilder create(AssociationType type) {
        return new AssociationBuilder(project, type);
    }

    public AssociationBuilder constrain(IAssociation constrainedAssociation) {
        return new AssociationBuilder.Constrain(project, constrainedAssociation);
    }

    public PolicyCmptTypeBuilder createPolicyCmptType(String name) {
        return new PolicyCmptTypeBuilder(project, name);
    }
}
