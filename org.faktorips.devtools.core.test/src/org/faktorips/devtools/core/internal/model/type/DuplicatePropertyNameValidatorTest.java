/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.type;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.util.message.ObjectProperty;
import org.junit.Before;
import org.junit.Test;

public class DuplicatePropertyNameValidatorTest extends AbstractIpsPluginTest {

    private DuplicatePropertyNameValidator validatorTest;
    private IPolicyCmptTypeAssociation toVA;
    private IPolicyCmptTypeAssociation toA;
    private IPolicyCmptTypeAssociation toB;
    private IPolicyCmptTypeAssociation toVB;
    private IPolicyCmptTypeAssociation toC;
    private IPolicyCmptTypeAssociation toVC;
    private ObjectProperty opToVA;
    private ObjectProperty opToVB;
    private ObjectProperty opToVC;
    private IPolicyCmptTypeAssociation toO;
    private IPolicyCmptTypeAssociation toAO;
    private ObjectProperty opToVO;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject ipsProject = newIpsProject();
        validatorTest = new DuplicatePropertyNameValidator(ipsProject);
        PolicyCmptType policyCmptTypeA = newPolicyCmptType(ipsProject, "pctA");
        PolicyCmptType policyCmptTypeB = newPolicyCmptType(ipsProject, "pctB");
        PolicyCmptType policyCmptTypeB1 = newPolicyCmptType(ipsProject, "pctB1");
        PolicyCmptType policyCmptTypeC = newPolicyCmptType(ipsProject, "pctC");
        PolicyCmptType policyCmptTypeV = newPolicyCmptType(ipsProject, "pctV");
        PolicyCmptType policyCmptTypeOther = newPolicyCmptType(ipsProject, "pctO");

        policyCmptTypeB.setSupertype(policyCmptTypeA.getName());
        policyCmptTypeC.setSupertype(policyCmptTypeB.getName());

        policyCmptTypeB1.setSupertype(policyCmptTypeA.getName());

        toA = (IPolicyCmptTypeAssociation)policyCmptTypeV.newAssociation();
        toA.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        toA.setTarget(policyCmptTypeA.getName());
        toA.setTargetRoleSingular("toA");
        toA.setTargetRolePlural("toAs");

        toVA = (IPolicyCmptTypeAssociation)policyCmptTypeA.newAssociation();
        toVA.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        toVA.setTarget(policyCmptTypeV.getName());
        toVA.setTargetRoleSingular("toV");
        toVA.setTargetRolePlural("toV");
        toVA.setInverseAssociation(toA.getName());

        toB = (IPolicyCmptTypeAssociation)policyCmptTypeV.newAssociation();
        toB.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        toB.setTarget(policyCmptTypeA.getName());
        toB.setTargetRoleSingular("toB");
        toB.setTargetRolePlural("toBs");

        toVB = (IPolicyCmptTypeAssociation)policyCmptTypeB.newAssociation();
        toVB.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        toVB.setTarget(policyCmptTypeV.getName());
        toVB.setTargetRoleSingular("toV");
        toVB.setTargetRolePlural("toV");
        toVB.setInverseAssociation(toB.getName());

        toC = (IPolicyCmptTypeAssociation)policyCmptTypeV.newAssociation();
        toC.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        toC.setTarget(policyCmptTypeA.getName());
        toC.setTargetRoleSingular("toC");
        toC.setTargetRolePlural("toCs");

        toVC = (IPolicyCmptTypeAssociation)policyCmptTypeC.newAssociation();
        toVC.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        toVC.setTarget(policyCmptTypeV.getName());
        toVC.setTargetRoleSingular("toV");
        toVC.setTargetRolePlural("toV");
        toVC.setInverseAssociation(toC.getName());

        toO = (IPolicyCmptTypeAssociation)policyCmptTypeV.newAssociation();
        toO.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        toO.setTarget(policyCmptTypeA.getName());
        toO.setTargetRoleSingular("toO");
        toO.setTargetRolePlural("toOs");

        toAO = (IPolicyCmptTypeAssociation)policyCmptTypeOther.newAssociation();
        toAO.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        toAO.setTarget(policyCmptTypeA.getName());
        toAO.setTargetRoleSingular("toV");
        toAO.setTargetRolePlural("toV");
        toAO.setInverseAssociation(toO.getName());

        opToVA = new ObjectProperty(toVA, "toV");
        opToVB = new ObjectProperty(toVB, "toV");
        opToVC = new ObjectProperty(toVC, "toV");
        opToVO = new ObjectProperty(toAO, "toV");

    }

    @Test
    public void testIgnore() {
        ObjectProperty[] objectProperties = new ObjectProperty[] { opToVB, opToVA };
        // both are not inverse of derived union - only one is valid
        assertFalse(validatorTest.ignore(objectProperties));
        toA.setDerivedUnion(true);
        assertTrue(validatorTest.ignore(objectProperties));

        objectProperties = new ObjectProperty[] { opToVC, opToVB, opToVA };
        // both toVC and toVB are no inverse of derived unions!
        assertFalse(validatorTest.ignore(objectProperties));
        toB.setDerivedUnion(true);
        assertTrue(validatorTest.ignore(objectProperties));

        objectProperties = new ObjectProperty[] { opToVO, opToVB, opToVA };
        assertFalse(validatorTest.ignore(objectProperties));

        objectProperties = new ObjectProperty[] { opToVA, opToVO };
        assertFalse(validatorTest.ignore(objectProperties));

        objectProperties = new ObjectProperty[] { opToVO, opToVA };
        assertFalse(validatorTest.ignore(objectProperties));

        objectProperties = new ObjectProperty[] { opToVO, opToVO };
        assertFalse(validatorTest.ignore(objectProperties));
    }

    @Test
    public void testIgnore_notIgnored() {
        ObjectProperty[] objectProperties = new ObjectProperty[] { opToVB, opToVA };
        // both are not inverse of derived union - only one is valid
        assertFalse(validatorTest.ignore(objectProperties));
    }

    /**
     * The constrain is set correct but both associations are in the same type.
     */
    @Test
    public void testIgnore_constrainSameType() {
        ObjectProperty[] objectProperties = new ObjectProperty[] { new ObjectProperty(toB, "toB"),
                new ObjectProperty(toA, "toA") };
        toB.setConstrain(true);
        toB.setTargetRoleSingular("toA");
        toB.setTargetRolePlural("toAs");

        assertFalse(validatorTest.ignore(objectProperties));
    }

    /**
     * The constrain is set correct but both associations are in the same type.
     */
    @Test
    public void testIgnore_constrain() {
        ObjectProperty[] objectProperties = new ObjectProperty[] { opToVB, opToVA };
        toVB.setConstrain(true);
        toVB.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        assertTrue(validatorTest.ignore(objectProperties));
    }

    @Test
    public void testIgnore_wrongConstrain() {
        ObjectProperty[] objectProperties = new ObjectProperty[] { opToVB, opToVA };
        toVA.setConstrain(true);

        assertFalse(validatorTest.ignore(objectProperties));
    }

}
