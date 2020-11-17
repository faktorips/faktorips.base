/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.pctype;

import static org.junit.Assert.assertEquals;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;
import org.junit.Before;
import org.junit.Test;

public class AssociationTypeTest extends AbstractIpsPluginTest {

    private IAssociation association;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject();
        PolicyCmptType policyCmptType = newPolicyAndProductCmptType(project, "mycompany.motor.MotorPolicy",
                "mycompany.motor.MotorProduct");
        association = policyCmptType.newAssociation();
    }

    @Test
    public void testGetCorrespondingAssociationType() throws Exception {
        association.setAssociationType(AssociationType.ASSOCIATION);
        assertEquals(AssociationType.ASSOCIATION, association.getAssociationType().getCorrespondingAssociationType());

        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        assertEquals(AssociationType.COMPOSITION_DETAIL_TO_MASTER, association.getAssociationType()
                .getCorrespondingAssociationType());

        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        assertEquals(AssociationType.COMPOSITION_MASTER_TO_DETAIL, association.getAssociationType()
                .getCorrespondingAssociationType());
    }
}
