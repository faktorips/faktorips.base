/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.pctype.associationwizard;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IType;

public class NewPcTypeAssociationWizardTest  extends AbstractIpsPluginTest {
    private IIpsProject project;
    
    public void setUp() throws Exception{
        super.setUp();
        project = newIpsProject("TestProject");
    }

    /**
     * Test: get corresponding target associations. Used in the wizard to find all available
     * associations which could be used as inverse association.<br>
     * Note: this test is also covered by the test for the method IType.findAssociationsForTargetAndAssociationType()
     * @see {@link IType#findAssociationsForTargetAndAssociationType(String, AssociationType, IIpsProject)}
     * 
     * @throws Exception
     */
    public void testGetCorrespondingTargetAssociations() throws Exception{
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
        
        // don't find any relations because no relation on policyCmptType1 have policyCmptType2 as target
        List result = NewPcTypeAssociationWizard.getCorrespondingTargetAssociations(relation21, policyCmptType1);
        assertEquals(0, result.size());
        
        superRelation.setTarget(policyCmptType2.getName());
        result = NewPcTypeAssociationWizard.getCorrespondingTargetAssociations(relation21, policyCmptType1);
        assertEquals(1, result.size());
        assertTrue(result.contains(superRelation));
        
        relation12.setTarget(policyCmptType2.getName());
        result = NewPcTypeAssociationWizard.getCorrespondingTargetAssociations(relation21, policyCmptType1);
        assertEquals(2, result.size());
        assertTrue(result.contains(superRelation));
        assertTrue(result.contains(relation12));
        
        superRelation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        relation12.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        result = NewPcTypeAssociationWizard.getCorrespondingTargetAssociations(relation21, policyCmptType1);
        assertEquals(0, result.size());
        
        relation21.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        result = NewPcTypeAssociationWizard.getCorrespondingTargetAssociations(relation21, policyCmptType1);
        assertEquals(1, result.size());
        assertTrue(result.contains(relation12));
        
        relation21.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        result = NewPcTypeAssociationWizard.getCorrespondingTargetAssociations(relation21, policyCmptType1);
        assertEquals(1, result.size());
        assertTrue(result.contains(superRelation));
    }
    
    /**
     * Test if the last pages for creating the product cmpt type association are available or not.
     *
     */
    public void testIsProductCmptTypeAvailable() throws CoreException{
        PolicyCmptType sourcePolicyCmptType = newPolicyAndProductCmptType(project, "Policy", "PolicyType");
        PolicyCmptType targetPolicyCmptType = newPolicyAndProductCmptType(project, "Coverage", "CoverageType");
        
        sourcePolicyCmptType.setConfigurableByProductCmptType(false);
        targetPolicyCmptType.setConfigurableByProductCmptType(false);
        assertFalse(NewPcTypeAssociationWizard.isProductCmptTypeAvailable(project, sourcePolicyCmptType, targetPolicyCmptType));

        sourcePolicyCmptType.setConfigurableByProductCmptType(true);
        sourcePolicyCmptType.setProductCmptType("PolicyType");
        targetPolicyCmptType.setConfigurableByProductCmptType(false);
        assertFalse(NewPcTypeAssociationWizard.isProductCmptTypeAvailable(project, sourcePolicyCmptType, targetPolicyCmptType));

        sourcePolicyCmptType.setConfigurableByProductCmptType(false);
        targetPolicyCmptType.setConfigurableByProductCmptType(true);
        targetPolicyCmptType.setProductCmptType("CoverageType");
        assertFalse(NewPcTypeAssociationWizard.isProductCmptTypeAvailable(project, sourcePolicyCmptType, targetPolicyCmptType));

        sourcePolicyCmptType.setConfigurableByProductCmptType(true);
        targetPolicyCmptType.setConfigurableByProductCmptType(true);
        sourcePolicyCmptType.setProductCmptType("PolicyType");
        targetPolicyCmptType.setProductCmptType("CoverageType");        
        assertTrue(NewPcTypeAssociationWizard.isProductCmptTypeAvailable(project, sourcePolicyCmptType, targetPolicyCmptType));
 
        String productCmptType = sourcePolicyCmptType.getProductCmptType();
        sourcePolicyCmptType.setProductCmptType("NONE");
        assertFalse(NewPcTypeAssociationWizard.isProductCmptTypeAvailable(project, sourcePolicyCmptType, targetPolicyCmptType));

        sourcePolicyCmptType.setProductCmptType(productCmptType);
        assertTrue(NewPcTypeAssociationWizard.isProductCmptTypeAvailable(project, sourcePolicyCmptType, targetPolicyCmptType));
        
        targetPolicyCmptType.setProductCmptType("NONE");
        assertFalse(NewPcTypeAssociationWizard.isProductCmptTypeAvailable(project, sourcePolicyCmptType, targetPolicyCmptType));
    }
}
