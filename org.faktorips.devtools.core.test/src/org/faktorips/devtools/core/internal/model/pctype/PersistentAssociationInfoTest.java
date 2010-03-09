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

package org.faktorips.devtools.core.internal.model.pctype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPersistentAssociationInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.util.message.MessageList;

public class PersistentAssociationInfoTest extends PersistenceIpsTest {

    private IPolicyCmptTypeAssociation pcAssociation;
    private IPolicyCmptTypeAssociation targetPcAssociation;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        PolicyCmptType targetPolicyCmptType = newPolicyCmptType(ipsProject, "Policy2");

        pcAssociation = policyCmptType.newPolicyCmptTypeAssociation();
        targetPcAssociation = targetPolicyCmptType.newPolicyCmptTypeAssociation();

        pcAssociation.setTarget(targetPolicyCmptType.getQualifiedName());
        pcAssociation.setTargetRoleSingular(targetPolicyCmptType.getUnqualifiedName());
        targetPcAssociation.setTarget(policyCmptType.getQualifiedName());
        targetPcAssociation.setTargetRoleSingular(policyCmptType.getUnqualifiedName());

        pcAssociation.setInverseAssociation(targetPcAssociation.getName());
        targetPcAssociation.setInverseAssociation(pcAssociation.getName());
    }

    public void testValidate() throws CoreException {
        MessageList ml = null;

        IPersistentAssociationInfo persistenceAssociatonInfo = pcAssociation.getPersistenceAssociatonInfo();

        pcAssociation.setAssociationType(AssociationType.ASSOCIATION);
        pcAssociation.setMinCardinality(0);
        pcAssociation.setMaxCardinality(2);
        targetPcAssociation.setAssociationType(AssociationType.ASSOCIATION);
        targetPcAssociation.setMinCardinality(0);
        targetPcAssociation.setMaxCardinality(2);

        persistenceAssociatonInfo.setJoinTableName("");
        persistenceAssociatonInfo.setTargetColumnName("");
        persistenceAssociatonInfo.setSourceColumnName("");

        ml = persistenceAssociatonInfo.validate(ipsProject);
        // TODO Joerg Merge Persistence Branch keine Validierung der Target/Source Column Names wenn
        // leer? StringUtils.isBlank korrekt, d.h. lassen wir namen die nur leerzeichen enthalten
        // zu?
        assertEquals(1, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_JOIN_TABLE_NAME_EMPTY));

        persistenceAssociatonInfo.setJoinTableName("INVALID JOIN_TABLE");
        ml = persistenceAssociatonInfo.validate(ipsProject);
        assertEquals(1, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_JOIN_TABLE_NAME_INVALID));

        persistenceAssociatonInfo.setJoinTableName("JOIN_TABLE");
        ml = persistenceAssociatonInfo.validate(ipsProject);
        assertEquals(0, ml.getNoOfMessages());

        persistenceAssociatonInfo.setTargetColumnName("INVALID SOURCE_COLUMN");
        persistenceAssociatonInfo.setSourceColumnName("INVALID TARGET_COLUMN");
        ml = persistenceAssociatonInfo.validate(ipsProject);
        assertEquals(2, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_SOURCE_COLUMN_NAME_INVALID));
        assertNotNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_TARGET_COLUMN_NAME_INVALID));

        persistenceAssociatonInfo.setSourceColumnName("SOURCE_COLUMN");
        ml = persistenceAssociatonInfo.validate(ipsProject);
        assertEquals(1, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_TARGET_COLUMN_NAME_INVALID));

        persistenceAssociatonInfo.setTargetColumnName("TARGET_COLUMN");
        ml = persistenceAssociatonInfo.validate(ipsProject);
        assertEquals(0, ml.getNoOfMessages());
    }

    public void testJoinTableRequiredAssociation() throws CoreException {
        IPersistentAssociationInfo persistenceAssociatonInfo = pcAssociation.getPersistenceAssociatonInfo();

        // 1:1 Association, join table not required
        setAssociationTypeAndCardinality(AssociationType.ASSOCIATION, AssociationType.ASSOCIATION, new int[] { 0, 1 },
                new int[] { 0, 1 });
        assertFalse(persistenceAssociatonInfo.isJoinTableRequired());

        // n:n Association, join table required
        setAssociationTypeAndCardinality(AssociationType.ASSOCIATION, AssociationType.ASSOCIATION, new int[] { 0, 2 },
                new int[] { 0, 2 });
        assertTrue(persistenceAssociatonInfo.isJoinTableRequired());

        // 1:n Association, join table not required
        setAssociationTypeAndCardinality(AssociationType.ASSOCIATION, AssociationType.ASSOCIATION, new int[] { 0, 1 },
                new int[] { 0, 2 });
        assertFalse(persistenceAssociatonInfo.isJoinTableRequired());
    }

    public void testJoinTableRequiredMaster2Detail() throws CoreException {
        IPersistentAssociationInfo persistenceAssociatonInfo = pcAssociation.getPersistenceAssociatonInfo();

        // 1:n Composition master to detail, join table not required
        setAssociationTypeAndCardinality(AssociationType.COMPOSITION_MASTER_TO_DETAIL,
                AssociationType.COMPOSITION_DETAIL_TO_MASTER, new int[] { 0, 2 }, new int[] { 0, 1 });
        assertFalse(persistenceAssociatonInfo.isJoinTableRequired());

        // n:n Composition master to detail, join table required
        setAssociationTypeAndCardinality(AssociationType.COMPOSITION_MASTER_TO_DETAIL,
                AssociationType.COMPOSITION_DETAIL_TO_MASTER, new int[] { 0, 2 }, new int[] { 0, 2 });
        assertTrue(persistenceAssociatonInfo.isJoinTableRequired());

        // n:1 Composition master to detail, join table not required
        setAssociationTypeAndCardinality(AssociationType.COMPOSITION_MASTER_TO_DETAIL,
                AssociationType.COMPOSITION_DETAIL_TO_MASTER, new int[] { 0, 1 }, new int[] { 0, 2 });
        assertFalse(persistenceAssociatonInfo.isJoinTableRequired());

    }

    public void testJoinTableRequiredDetail2Master() throws CoreException {
        IPersistentAssociationInfo persistenceAssociatonInfo = pcAssociation.getPersistenceAssociatonInfo();

        // n:1 Association, join table not required
        setAssociationTypeAndCardinality(AssociationType.ASSOCIATION, AssociationType.ASSOCIATION, new int[] { 0, 2 },
                new int[] { 0, 1 });
        assertFalse(persistenceAssociatonInfo.isJoinTableRequired());

        // 1:n Composition detail to master, join table not required
        setAssociationTypeAndCardinality(AssociationType.COMPOSITION_DETAIL_TO_MASTER,
                AssociationType.COMPOSITION_MASTER_TO_DETAIL, new int[] { 0, 2 }, new int[] { 0, 1 });
        assertFalse(persistenceAssociatonInfo.isJoinTableRequired());

        // n:n Composition master to detail, join table required
        setAssociationTypeAndCardinality(AssociationType.COMPOSITION_DETAIL_TO_MASTER,
                AssociationType.COMPOSITION_MASTER_TO_DETAIL, new int[] { 0, 2 }, new int[] { 0, 2 });
        assertTrue(persistenceAssociatonInfo.isJoinTableRequired());

        // n:1 Composition master to detail, join table not required
        setAssociationTypeAndCardinality(AssociationType.COMPOSITION_DETAIL_TO_MASTER,
                AssociationType.COMPOSITION_MASTER_TO_DETAIL, new int[] { 0, 1 }, new int[] { 0, 2 });
        assertFalse(persistenceAssociatonInfo.isJoinTableRequired());
    }

    private void setAssociationTypeAndCardinality(AssociationType associationTypeSource,
            AssociationType associationTypeTarget,
            int[] cardinalitiesSource,
            int[] cardinalitiesTarget) {
        pcAssociation.setAssociationType(associationTypeSource);
        pcAssociation.setMinCardinality(cardinalitiesSource[0]);
        pcAssociation.setMaxCardinality(cardinalitiesSource[1]);
        targetPcAssociation.setAssociationType(associationTypeTarget);
        targetPcAssociation.setMinCardinality(cardinalitiesTarget[0]);
        targetPcAssociation.setMaxCardinality(cardinalitiesTarget[1]);
    }
}
