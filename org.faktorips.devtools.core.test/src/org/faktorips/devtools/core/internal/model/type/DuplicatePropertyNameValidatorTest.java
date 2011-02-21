/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.type;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.util.message.ObjectProperty;
import org.junit.Before;
import org.junit.Test;

public class DuplicatePropertyNameValidatorTest extends AbstractIpsPluginTest {

    private DuplicatePropertyNameValidator validatorTest;
    private IAssociation toVA;
    private IAssociation toA;
    private IAssociation toB;
    private IAssociation toVB;
    private IAssociation toB1;
    private IAssociation toVB1;
    private IAssociation toC;
    private IAssociation toVC;
    private ObjectProperty opToA;
    private ObjectProperty opToVA;
    private ObjectProperty opToVB;
    private ObjectProperty opToVB1;
    private ObjectProperty opToVC;
    private IAssociation toO;
    private IAssociation toAO;
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

        toA = policyCmptTypeV.newAssociation();
        toA.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        toA.setTarget(policyCmptTypeA.getName());
        toA.setTargetRoleSingular("toA");
        toA.setTargetRolePlural("toAs");

        toVA = policyCmptTypeA.newAssociation();
        toVA.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        toVA.setTarget(policyCmptTypeV.getName());
        toVA.setTargetRoleSingular("toV");
        toVA.setTargetRolePlural("toV");

        toB = policyCmptTypeV.newAssociation();
        toB.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        toB.setTarget(policyCmptTypeA.getName());
        toB.setTargetRoleSingular("toB");
        toB.setTargetRolePlural("toBs");

        toVB = policyCmptTypeB.newAssociation();
        toVB.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        toVB.setTarget(policyCmptTypeV.getName());
        toVB.setTargetRoleSingular("toV");
        toVB.setTargetRolePlural("toV");

        toB1 = policyCmptTypeV.newAssociation();
        toB1.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        toB1.setTarget(policyCmptTypeA.getName());
        toB1.setTargetRoleSingular("toB1");
        toB1.setTargetRolePlural("toB1s");

        toVB1 = policyCmptTypeB1.newAssociation();
        toVB1.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        toVB1.setTarget(policyCmptTypeV.getName());
        toVB1.setTargetRoleSingular("toV");
        toVB1.setTargetRolePlural("toV");

        toC = policyCmptTypeV.newAssociation();
        toC.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        toC.setTarget(policyCmptTypeA.getName());
        toC.setTargetRoleSingular("toC");
        toC.setTargetRolePlural("toCs");

        toVC = policyCmptTypeC.newAssociation();
        toVC.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        toVC.setTarget(policyCmptTypeV.getName());
        toVC.setTargetRoleSingular("toV");
        toVC.setTargetRolePlural("toV");

        toO = policyCmptTypeV.newAssociation();
        toO.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        toO.setTarget(policyCmptTypeA.getName());
        toO.setTargetRoleSingular("toO");
        toO.setTargetRolePlural("toOs");

        toAO = policyCmptTypeOther.newAssociation();
        toAO.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        toAO.setTarget(policyCmptTypeA.getName());
        toAO.setTargetRoleSingular("toV");
        toAO.setTargetRolePlural("toV");

        opToVA = new ObjectProperty(toVA, "toV");
        opToVB = new ObjectProperty(toVB, "toV");
        opToVB1 = new ObjectProperty(toVB1, "toV");
        opToVC = new ObjectProperty(toVC, "toV");
        opToVO = new ObjectProperty(toAO, "toV");

    }

    @Test
    public void testIgnoreDuplicatedInverseAssociationsForDerivedUnions() throws CoreException {
        ObjectProperty[] objectProperties = new ObjectProperty[] { opToVB, opToVA };
        assertTrue(validatorTest.ignoreDuplicatedInverseAssociationsForDerivedUnions(objectProperties));

        objectProperties = new ObjectProperty[] { opToVB, opToVB1, opToVA };
        assertTrue(validatorTest.ignoreDuplicatedInverseAssociationsForDerivedUnions(objectProperties));

        objectProperties = new ObjectProperty[] { opToVC, opToVB, opToVB1, opToVA };
        assertTrue(validatorTest.ignoreDuplicatedInverseAssociationsForDerivedUnions(objectProperties));

        objectProperties = new ObjectProperty[] { opToVO, opToVB, opToVB1, opToVA };
        assertFalse(validatorTest.ignoreDuplicatedInverseAssociationsForDerivedUnions(objectProperties));

        objectProperties = new ObjectProperty[] { opToVO, opToVB, opToVA };
        assertFalse(validatorTest.ignoreDuplicatedInverseAssociationsForDerivedUnions(objectProperties));

        objectProperties = new ObjectProperty[] { opToVA, opToVO };
        assertFalse(validatorTest.ignoreDuplicatedInverseAssociationsForDerivedUnions(objectProperties));

        objectProperties = new ObjectProperty[] { opToVO, opToVA };
        assertFalse(validatorTest.ignoreDuplicatedInverseAssociationsForDerivedUnions(objectProperties));

        objectProperties = new ObjectProperty[] { opToVO, opToVO };
        assertFalse(validatorTest.ignoreDuplicatedInverseAssociationsForDerivedUnions(objectProperties));
    }
}
