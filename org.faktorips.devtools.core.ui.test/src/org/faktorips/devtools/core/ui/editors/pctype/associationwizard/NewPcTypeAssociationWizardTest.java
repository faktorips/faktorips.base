/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype.associationwizard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.type.AssociationType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.junit.Before;
import org.junit.Test;

public class NewPcTypeAssociationWizardTest extends AbstractIpsPluginTest {
    private IIpsProject project;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
    }

    /**
     * Test: get corresponding target associations. Used in the wizard to find all available
     * associations which could be used as inverse association.<br>
     * Note: this test is also covered by the test for the method
     * IType.findAssociationsForTargetAndAssociationType()
     * 
     * @see IType#findAssociationsForTargetAndAssociationType(String, AssociationType, IIpsProject,
     *      boolean)
     */
    @Test
    public void testGetCorrespondingTargetAssociations() throws Exception {
        IPolicyCmptType policyCmptTypeSuper1 = newPolicyCmptType(project, "policyCmptSuper1");
        IPolicyCmptType policyCmptType1 = newPolicyCmptType(project, "policyCmpt1");
        policyCmptType1.setSupertype(policyCmptTypeSuper1.getQualifiedName());

        IPolicyCmptType policyCmptType2 = newPolicyCmptType(project, "policyCmpt2");

        policyCmptTypeSuper1.newPolicyCmptTypeAssociation().setTargetRoleSingular("dummy");
        IPolicyCmptTypeAssociation superRelation = policyCmptTypeSuper1.newPolicyCmptTypeAssociation();
        superRelation.setTargetRoleSingular("realtionSuper");
        superRelation.setAssociationType(AssociationType.ASSOCIATION);
        IPolicyCmptTypeAssociation relation12 = policyCmptType1.newPolicyCmptTypeAssociation();
        relation12.setTargetRoleSingular("realtion12");

        IPolicyCmptTypeAssociation relation21 = policyCmptType2.newPolicyCmptTypeAssociation();
        relation21.setAssociationType(AssociationType.ASSOCIATION);

        NewPcTypeAssociationWizard wizard = new NewPcTypeAssociationWizard(relation21);
        wizard.storeTargetPolicyCmptType(policyCmptType1);

        // don't find any association because no association on policyCmptType1 have policyCmptType2
        // as target
        List<IAssociation> result = wizard.getExistingInverseAssociationCandidates();
        assertEquals(0, result.size());

        // don't find associations from supertype
        superRelation.setTarget(policyCmptType2.getName());
        result = wizard.getExistingInverseAssociationCandidates();
        assertEquals(0, result.size());

        relation12.setTarget(policyCmptType2.getName());
        result = wizard.getExistingInverseAssociationCandidates();
        assertEquals(1, result.size());
        assertTrue(result.contains(relation12));

        superRelation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        relation12.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        result = wizard.getExistingInverseAssociationCandidates();
        assertEquals(0, result.size());

        relation21.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        result = wizard.getExistingInverseAssociationCandidates();
        assertEquals(1, result.size());
        assertTrue(result.contains(relation12));

        relation21.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        result = wizard.getExistingInverseAssociationCandidates();
        assertEquals(0, result.size());
    }

    /**
     * Don't find association from subtypes
     */
    @Test
    public void testGetCorrespondingTargetAssociationsFromSubType() throws Exception {
        // create supertypes
        IPolicyCmptType police = newPolicyCmptType(project, "policy");
        IPolicyCmptType coverage = newPolicyCmptType(project, "coverage");

        // create subtypes
        IPolicyCmptType motorPolice = newPolicyCmptType(project, "motorPolicy");
        IPolicyCmptType motorCoverage = newPolicyCmptType(project, "motorCoverage");
        motorPolice.setSupertype(police.getQualifiedName());
        motorCoverage.setSupertype(coverage.getQualifiedName());

        // create association on supertypes
        IPolicyCmptTypeAssociation assCoverage = police.newPolicyCmptTypeAssociation();
        assCoverage.setTarget(coverage.getQualifiedName());
        assCoverage.setTargetRolePlural("coverage");
        assCoverage.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IPolicyCmptTypeAssociation assPolice = coverage.newPolicyCmptTypeAssociation();
        assPolice.setTarget(police.getQualifiedName());
        assPolice.setTargetRolePlural("policy");
        assPolice.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);

        assCoverage.setInverseAssociation(assPolice.getName());
        assPolice.setInverseAssociation(assCoverage.getName());

        // create association on subtypes
        IPolicyCmptTypeAssociation assMotorCoverage = motorPolice.newPolicyCmptTypeAssociation();
        assMotorCoverage.setTarget(motorCoverage.getQualifiedName());
        assMotorCoverage.setTargetRolePlural("motorCoverage");
        assMotorCoverage.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IPolicyCmptTypeAssociation assMotorPolice = motorCoverage.newPolicyCmptTypeAssociation();
        assMotorPolice.setTarget(motorPolice.getQualifiedName());
        assMotorPolice.setTargetRolePlural("motorPolicy");
        assMotorPolice.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);

        assMotorCoverage.setInverseAssociation(assMotorPolice.getName());
        assMotorPolice.setInverseAssociation(assMotorPolice.getName());

        IPolicyCmptTypeAssociation assMotorCoverage2 = police.newPolicyCmptTypeAssociation();
        assMotorCoverage2.setTargetRoleSingular("motorCoverage");
        assMotorCoverage2.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        NewPcTypeAssociationWizard wizard = new NewPcTypeAssociationWizard(assMotorCoverage2);
        wizard.storeTargetPolicyCmptType(motorCoverage);
        List<IAssociation> correspondingTargetAssociations = wizard.getExistingInverseAssociationCandidates();
        assertEquals(0, correspondingTargetAssociations.size());
    }

    /**
     * Test if the last pages for creating the product cmpt type association are available or not.
     */
    @Test
    public void testIsProductCmptTypeAvailable() throws CoreException {
        PolicyCmptType sourcePolicyCmptType = newPolicyAndProductCmptType(project, "Policy", "PolicyType");
        PolicyCmptType targetPolicyCmptType = newPolicyAndProductCmptType(project, "Coverage", "CoverageType");

        sourcePolicyCmptType.setConfigurableByProductCmptType(false);
        targetPolicyCmptType.setConfigurableByProductCmptType(false);
        assertFalse(NewPcTypeAssociationWizard.isProductCmptTypeAvailable(project, sourcePolicyCmptType,
                targetPolicyCmptType));

        sourcePolicyCmptType.setConfigurableByProductCmptType(true);
        sourcePolicyCmptType.setProductCmptType("PolicyType");
        targetPolicyCmptType.setConfigurableByProductCmptType(false);
        assertFalse(NewPcTypeAssociationWizard.isProductCmptTypeAvailable(project, sourcePolicyCmptType,
                targetPolicyCmptType));

        sourcePolicyCmptType.setConfigurableByProductCmptType(false);
        targetPolicyCmptType.setConfigurableByProductCmptType(true);
        targetPolicyCmptType.setProductCmptType("CoverageType");
        assertFalse(NewPcTypeAssociationWizard.isProductCmptTypeAvailable(project, sourcePolicyCmptType,
                targetPolicyCmptType));

        sourcePolicyCmptType.setConfigurableByProductCmptType(true);
        targetPolicyCmptType.setConfigurableByProductCmptType(true);
        sourcePolicyCmptType.setProductCmptType("PolicyType");
        targetPolicyCmptType.setProductCmptType("CoverageType");
        assertTrue(NewPcTypeAssociationWizard.isProductCmptTypeAvailable(project, sourcePolicyCmptType,
                targetPolicyCmptType));

        String productCmptType = sourcePolicyCmptType.getProductCmptType();
        sourcePolicyCmptType.setProductCmptType("NONE");
        assertFalse(NewPcTypeAssociationWizard.isProductCmptTypeAvailable(project, sourcePolicyCmptType,
                targetPolicyCmptType));

        sourcePolicyCmptType.setProductCmptType(productCmptType);
        assertTrue(NewPcTypeAssociationWizard.isProductCmptTypeAvailable(project, sourcePolicyCmptType,
                targetPolicyCmptType));

        targetPolicyCmptType.setProductCmptType("NONE");
        assertFalse(NewPcTypeAssociationWizard.isProductCmptTypeAvailable(project, sourcePolicyCmptType,
                targetPolicyCmptType));
    }
}
