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

package org.faktorips.devtools.core.internal.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;

public class Migration_2_5_0_rc2Test extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private PolicyCmptType pcType;
    private IPolicyCmptTypeAssociation association;
    private PolicyCmptType targetType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();

        pcType = newPolicyCmptType(ipsProject, "Policy");
        targetType = newPolicyCmptType(ipsProject, "Coverage");

        association = pcType.newPolicyCmptTypeAssociation();
        association.setTarget(targetType.getQualifiedName());
        association.setTargetRoleSingular(targetType.getUnqualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
    }

    /**
     * Test migration. The inverse of a detail to master must be set to the corresponding master to
     * detail and the inverse of the master to detail must be set to the corresponding detail to
     * master.
     */
    @Test
    public void testFixInverseOfDetailToMasterAssociation() throws CoreException {
        MessageList ml = new MessageList();
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        assertFalse(Migration_2_5_0_rc2.fixInverseOfDetailToMasterAssociation(ipsProject, ml, association));

        association.setAssociationType(AssociationType.ASSOCIATION);
        ml = new MessageList();
        assertFalse(Migration_2_5_0_rc2.fixInverseOfDetailToMasterAssociation(ipsProject, ml, association));

        // test if inverse already set
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        association.setInverseAssociation("dummy");
        ml = new MessageList();
        assertFalse(Migration_2_5_0_rc2.fixInverseOfDetailToMasterAssociation(ipsProject, ml, association));
        // assert that the associations are not changed (must be done manually)
        assertEquals("dummy", association.getInverseAssociation());

        // migrate association if type is detail to master and inverse is not set
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        association.setInverseAssociation("");
        ml = new MessageList();
        assertTrue(Migration_2_5_0_rc2.fixInverseOfDetailToMasterAssociation(ipsProject, ml, association));
        assertNotNull(ml.getMessageByCode(Migration_2_5_0_rc2.MSGCODE_NO_MASTER_TO_DETAIL_CANDIDATE_NOT_EXISTS));
        // assert that the associations are not changed (must be done manually)
        assertEquals("", association.getInverseAssociation());

        association.setTarget("dummy");
        ml = new MessageList();
        assertTrue(Migration_2_5_0_rc2.fixInverseOfDetailToMasterAssociation(ipsProject, ml, association));
        assertNotNull(ml.getMessageByCode(Migration_2_5_0_rc2.MSGCODE_TARGET_POLICY_CMPT_NOT_EXISTS));
        // assert that the associations are not changed (must be done manually)
        assertEquals("", association.getInverseAssociation());

        association.setTarget(targetType.getQualifiedName());

        // test with master to detail candidates not unique
        IPolicyCmptTypeAssociation masterToDetail1 = targetType.newPolicyCmptTypeAssociation();
        IPolicyCmptTypeAssociation masterToDetail2 = targetType.newPolicyCmptTypeAssociation();
        masterToDetail1.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        masterToDetail2.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        masterToDetail1.setTargetRoleSingular("A1toB");
        masterToDetail2.setTargetRoleSingular("A2toB");
        masterToDetail1.setTarget(pcType.getQualifiedName());
        masterToDetail2.setTarget(pcType.getQualifiedName());
        ml = new MessageList();
        assertTrue(Migration_2_5_0_rc2.fixInverseOfDetailToMasterAssociation(ipsProject, ml, association));
        assertNotNull(ml.getMessageByCode(Migration_2_5_0_rc2.MSGCODE_MASTER_TO_DETAIL_CANDIDATES_NOT_UNIQUE));
        // assert that the associations are not changed (must be done manually)
        assertEquals("", association.getInverseAssociation());
        assertEquals("", masterToDetail1.getInverseAssociation());
        assertEquals("", masterToDetail2.getInverseAssociation());

        masterToDetail2.setTarget("unknown");
        ml = new MessageList();
        assertTrue(Migration_2_5_0_rc2.fixInverseOfDetailToMasterAssociation(ipsProject, ml, association));
        assertEquals(0, ml.getNoOfMessages());

        // test fixed associations detail to master and master to detail
        assertEquals(association.getInverseAssociation(), masterToDetail1.getName());
    }
}
