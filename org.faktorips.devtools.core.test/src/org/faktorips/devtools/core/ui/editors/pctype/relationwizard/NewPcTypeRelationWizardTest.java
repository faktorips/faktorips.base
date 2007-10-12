/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype.relationwizard;

import java.util.List;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.RelationType;

public class NewPcTypeRelationWizardTest  extends AbstractIpsPluginTest {
    private IIpsProject project;
    
    public void setUp() throws Exception{
        super.setUp();
        project = newIpsProject("TestProject");
    }

    public void testGetCorrespondingRelationType() throws Exception {
        IPolicyCmptType policyCmptType = newPolicyCmptType(project, "policyCmpt");
        IPolicyCmptTypeAssociation relation = policyCmptType.newPolicyCmptTypeAssociation();
        relation.setTargetRoleSingular("relation");
        
        // no relation type specified
        relation.setRelationType(null);
        assertNull(NewPcTypeRelationWizard.getCorrespondingRelationType(relation.getRelationType()));
        
        // ASSOZIATION => ASSOZIATION
        relation.setRelationType(RelationType.ASSOCIATION);
        assertEquals(RelationType.ASSOCIATION, NewPcTypeRelationWizard.getCorrespondingRelationType(relation.getRelationType()));
        
        // COMPOSITION => REVERSE_COMPOSITION
        relation.setRelationType(RelationType.COMPOSITION_MASTER_TO_DETAIL);
        assertEquals(RelationType.COMPOSITION_DETAIL_TO_MASTER, NewPcTypeRelationWizard.getCorrespondingRelationType(relation.getRelationType()));
        
        // REVERSE_COMPOSITION => COMPOSITION 
        relation.setRelationType(RelationType.COMPOSITION_DETAIL_TO_MASTER);
        assertEquals(RelationType.COMPOSITION_MASTER_TO_DETAIL, NewPcTypeRelationWizard.getCorrespondingRelationType(relation.getRelationType()));
    }
    
    public void testGetCorrespondingTargetRelations() throws Exception{
        IPolicyCmptType policyCmptTypeSuper1 = newPolicyCmptType(project, "policyCmptSuper1");
        IPolicyCmptType policyCmptType1 = newPolicyCmptType(project, "policyCmpt1");
        policyCmptType1.setSupertype(policyCmptTypeSuper1.getQualifiedName());
        
        IPolicyCmptType policyCmptType2 = newPolicyCmptType(project, "policyCmpt2");
        
        policyCmptTypeSuper1.newPolicyCmptTypeAssociation().setTargetRoleSingular("dummy");
        IPolicyCmptTypeAssociation superRelation = policyCmptTypeSuper1.newPolicyCmptTypeAssociation();
        superRelation.setTargetRoleSingular("realtionSuper");
        superRelation.setRelationType(RelationType.ASSOCIATION);
        IPolicyCmptTypeAssociation relation12 = policyCmptType1.newPolicyCmptTypeAssociation();
        relation12.setTargetRoleSingular("realtion12");
        
        IPolicyCmptTypeAssociation relation21 = policyCmptType2.newPolicyCmptTypeAssociation();
        relation21.setRelationType(RelationType.ASSOCIATION);
        
        // don't find any relations because no relation on policyCmptType1 have policyCmptType2 as target
        List result = ReverseRelationPropertiesPage.getCorrespondingTargetRelations(relation21, policyCmptType1);
        assertEquals(0, result.size());
        
        superRelation.setTarget(policyCmptType2.getName());
        result = ReverseRelationPropertiesPage.getCorrespondingTargetRelations(relation21, policyCmptType1);
        assertEquals(1, result.size());
        assertTrue(result.contains(superRelation));
        
        relation12.setTarget(policyCmptType2.getName());
        result = ReverseRelationPropertiesPage.getCorrespondingTargetRelations(relation21, policyCmptType1);
        assertEquals(2, result.size());
        assertTrue(result.contains(superRelation));
        assertTrue(result.contains(relation12));
        
        superRelation.setRelationType(RelationType.COMPOSITION_MASTER_TO_DETAIL);
        relation12.setRelationType(RelationType.COMPOSITION_DETAIL_TO_MASTER);
        result = ReverseRelationPropertiesPage.getCorrespondingTargetRelations(relation21, policyCmptType1);
        assertEquals(0, result.size());
        
        relation21.setRelationType(RelationType.COMPOSITION_MASTER_TO_DETAIL);
        result = ReverseRelationPropertiesPage.getCorrespondingTargetRelations(relation21, policyCmptType1);
        assertEquals(1, result.size());
        assertTrue(result.contains(relation12));
        
        relation21.setRelationType(RelationType.COMPOSITION_DETAIL_TO_MASTER);
        result = ReverseRelationPropertiesPage.getCorrespondingTargetRelations(relation21, policyCmptType1);
        assertEquals(1, result.size());
        assertTrue(result.contains(superRelation));
    }
}
