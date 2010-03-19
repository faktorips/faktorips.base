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
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPersistentAssociationInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPersistentAssociationInfo.FetchType;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PersistentAssociationInfoTest extends PersistenceIpsTest {

    private IPolicyCmptTypeAssociation pcAssociation;
    private IPolicyCmptTypeAssociation targetPcAssociation;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        PolicyCmptType targetPolicyCmptType = newPolicyCmptType(ipsProject, "Policy2");

        targetPolicyCmptType.getPersistenceTypeInfo().setEnabled(true);
        policyCmptType.getPersistenceTypeInfo().setEnabled(true);

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
        IPersistentAssociationInfo inversePersistenceAssociatonInfo = targetPcAssociation
                .getPersistenceAssociatonInfo();

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
        assertEquals(1, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_OWNER_OF_ASSOCIATION_MISMATCH));

        persistenceAssociatonInfo.setOwnerOfManyToManyAssociation(true);
        inversePersistenceAssociatonInfo.setOwnerOfManyToManyAssociation(true);
        persistenceAssociatonInfo.setJoinTableName("INVALID JOIN_TABLE");
        ml = persistenceAssociatonInfo.validate(ipsProject);
        assertEquals(4, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_OWNER_OF_ASSOCIATION_MISMATCH));
        assertNotNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_JOIN_TABLE_NAME_INVALID));
        assertNotNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_SOURCE_COLUMN_NAME_EMPTY));
        assertNotNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_TARGET_COLUMN_NAME_EMPTY));

        inversePersistenceAssociatonInfo.setOwnerOfManyToManyAssociation(false);

        persistenceAssociatonInfo.setJoinTableName("JOIN_TABLE");
        ml = persistenceAssociatonInfo.validate(ipsProject);
        assertEquals(2, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_SOURCE_COLUMN_NAME_EMPTY));
        assertNotNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_TARGET_COLUMN_NAME_EMPTY));

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

        persistenceAssociatonInfo.setTransient(true);
        inversePersistenceAssociatonInfo.setTransient(true);
        ml = persistenceAssociatonInfo.validate(ipsProject);
        assertEquals(0, ml.getNoOfMessages());

        persistenceAssociatonInfo.setTransient(true);
        inversePersistenceAssociatonInfo.setTransient(false);
        ml = persistenceAssociatonInfo.validate(ipsProject);
        assertEquals(1, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_TRANSIENT_MISMATCH));

        persistenceAssociatonInfo.setTransient(false);
        inversePersistenceAssociatonInfo.setTransient(false);
        persistenceAssociatonInfo.setTargetColumnName("TARGET_COLUMN");
        ml = persistenceAssociatonInfo.validate(ipsProject);
        assertEquals(0, ml.getNoOfMessages());
    }

    public void testValidateTargetSideNotTransient() throws CoreException {
        MessageList ml = null;

        pcAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        targetPcAssociation.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        IPersistentAssociationInfo sourcePersistenceAssociatonInfo = pcAssociation.getPersistenceAssociatonInfo();
        IPersistentAssociationInfo targetPersistenceAssociatonInfo = targetPcAssociation.getPersistenceAssociatonInfo();

        targetPersistenceAssociatonInfo.setJoinColumnName("JoinColumn");

        ml = sourcePersistenceAssociatonInfo.validate(ipsProject);
        assertEquals(0, ml.getNoOfMessages());
        ml = targetPersistenceAssociatonInfo.validate(ipsProject);
        assertEquals(0, ml.getNoOfMessages());

        // test target association must be transient or target policy component persist disabled

        sourcePersistenceAssociatonInfo.setTransient(true);
        ml = sourcePersistenceAssociatonInfo.validate(ipsProject);
        assertEquals(1, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_TRANSIENT_MISMATCH));
        ml = targetPersistenceAssociatonInfo.validate(ipsProject);
        assertEquals(1, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_TRANSIENT_MISMATCH));

        targetPersistenceAssociatonInfo.setTransient(true);
        ml = sourcePersistenceAssociatonInfo.validate(ipsProject);
        assertEquals(0, ml.getNoOfMessages());
        ml = targetPersistenceAssociatonInfo.validate(ipsProject);
        assertEquals(0, ml.getNoOfMessages());

        sourcePersistenceAssociatonInfo.setTransient(true);
        targetPersistenceAssociatonInfo.setTransient(false);
        targetPersistenceAssociatonInfo.getPolicyComponentTypeAssociation().getPolicyCmptType()
                .getPersistenceTypeInfo().setEnabled(false);
        ml = sourcePersistenceAssociatonInfo.validate(ipsProject);
        assertEquals(0, ml.getNoOfMessages());
        ml = targetPersistenceAssociatonInfo.validate(ipsProject);
        assertEquals(0, ml.getNoOfMessages());

        targetPersistenceAssociatonInfo.getPolicyComponentTypeAssociation().getPolicyCmptType()
                .getPersistenceTypeInfo().setEnabled(true);

        // no transient mismatch in case of unidirectional association

        pcAssociation.setInverseAssociation("");
        pcAssociation.setMaxCardinality(1); // set to 1 we don't want a join table
        sourcePersistenceAssociatonInfo.setJoinColumnName("JoinColumn");
        ml = sourcePersistenceAssociatonInfo.validate(ipsProject);
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

        // unidirectional :n Composition master to detail, join table not required
        // because we can add in JPA an attribute joinTable (column name of the target side forein
        // key column) instead
        pcAssociation.setInverseAssociation("");
        setAssociationTypeAndCardinality(AssociationType.COMPOSITION_MASTER_TO_DETAIL, null, new int[] { 0, 2 }, null);
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
        if (associationTypeTarget != null) {
            targetPcAssociation.setAssociationType(associationTypeTarget);
        }
        if (cardinalitiesTarget != null) {
            targetPcAssociation.setMinCardinality(cardinalitiesTarget[0]);
            targetPcAssociation.setMaxCardinality(cardinalitiesTarget[1]);
        }
    }

    public void testInitFromXml() throws CoreException {
        NodeList nodeList = getTestDocument().getElementsByTagName(IPersistentAssociationInfo.XML_TAG);
        assertEquals(1, nodeList.getLength());

        Element element = (Element)nodeList.item(0);
        IPersistentAssociationInfo persistenceAssociatonInfo = pcAssociation.getPersistenceAssociatonInfo();
        persistenceAssociatonInfo.initFromXml(element);

        assertFalse(persistenceAssociatonInfo.isTransient());
        assertFalse(persistenceAssociatonInfo.isOwnerOfManyToManyAssociation());
        assertEquals("joinTable1", persistenceAssociatonInfo.getJoinTableName());
        assertEquals("targetColumn1", persistenceAssociatonInfo.getTargetColumnName());
        assertEquals("sourceColumn1", persistenceAssociatonInfo.getSourceColumnName());
        assertEquals(FetchType.LAZY, persistenceAssociatonInfo.getFetchType());
        assertEquals("joinColumn1", persistenceAssociatonInfo.getJoinColumnName());
    }

    public void testToXml() throws CoreException {
        IPersistentAssociationInfo persistenceAssociatonInfo = pcAssociation.getPersistenceAssociatonInfo();
        persistenceAssociatonInfo.setTransient(true);
        persistenceAssociatonInfo.setOwnerOfManyToManyAssociation(true);
        persistenceAssociatonInfo.setJoinTableName("joinTable0");
        persistenceAssociatonInfo.setTargetColumnName("targetColumn0");
        persistenceAssociatonInfo.setSourceColumnName("sourceColumn0");
        persistenceAssociatonInfo.setFetchType(FetchType.EAGER);
        persistenceAssociatonInfo.setJoinColumnName("joinColumn0");
        Element element = persistenceAssociatonInfo.toXml(newDocument());

        PolicyCmptType copyOfPcType = (PolicyCmptType)newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "Copy");
        copyOfPcType.newPolicyCmptTypeAssociation();
        IPersistentAssociationInfo copyOfPersistenceAssociatonInfo = copyOfPcType.getPolicyCmptTypeAssociations()[0]
                .getPersistenceAssociatonInfo();
        copyOfPersistenceAssociatonInfo.initFromXml(element);

        assertTrue(copyOfPersistenceAssociatonInfo.isTransient());
        assertTrue(copyOfPersistenceAssociatonInfo.isOwnerOfManyToManyAssociation());
        assertEquals("joinTable0", copyOfPersistenceAssociatonInfo.getJoinTableName());
        assertEquals("targetColumn0", copyOfPersistenceAssociatonInfo.getTargetColumnName());
        assertEquals("sourceColumn0", copyOfPersistenceAssociatonInfo.getSourceColumnName());
        assertEquals(FetchType.EAGER, copyOfPersistenceAssociatonInfo.getFetchType());
        assertEquals("joinColumn0", copyOfPersistenceAssociatonInfo.getJoinColumnName());
    }

    public void testIsJoinColumnRequired() throws CoreException {

        // due to performance reason do the cast to PersistentAssociationInfo
        // thus we can use the public method isJoinTableRequired(inverseAss)
        PersistentAssociationInfo persistenceAssociatonInfo = (PersistentAssociationInfo)pcAssociation
                .getPersistenceAssociatonInfo();
        PersistentAssociationInfo inversePersistenceAssociatonInfo = (PersistentAssociationInfo)targetPcAssociation
                .getPersistenceAssociatonInfo();

        // bidirectional:

        // one-to-one | master-to-detail => false
        // one-to-one | detail-to-master => true
        pcAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        pcAssociation.setMaxCardinality(1);
        targetPcAssociation.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        targetPcAssociation.setMaxCardinality(1);
        assertFalse(persistenceAssociatonInfo.isJoinColumnRequired(targetPcAssociation));
        assertTrue(inversePersistenceAssociatonInfo.isJoinColumnRequired(pcAssociation));

        // one-to-one | association => true (on one side)
        pcAssociation.setAssociationType(AssociationType.ASSOCIATION);
        pcAssociation.setMaxCardinality(1);
        targetPcAssociation.setAssociationType(AssociationType.ASSOCIATION);
        targetPcAssociation.setMaxCardinality(1);
        assertTrue(persistenceAssociatonInfo.isJoinColumnRequired(targetPcAssociation));
        assertTrue(inversePersistenceAssociatonInfo.isJoinColumnRequired(pcAssociation));

        pcAssociation.setAssociationType(AssociationType.ASSOCIATION);
        pcAssociation.setMaxCardinality(1);
        persistenceAssociatonInfo.setJoinColumnName("JoinColumn");
        targetPcAssociation.setAssociationType(AssociationType.ASSOCIATION);
        targetPcAssociation.setMaxCardinality(1);

        assertTrue(persistenceAssociatonInfo.isJoinColumnRequired(targetPcAssociation));
        assertFalse(inversePersistenceAssociatonInfo.isJoinColumnRequired(pcAssociation));

        // one-to-many | master-to-detail => false
        // many-to-one | detail-to-master => true
        // one-to-many | detail-to-master => false (but not supported)
        // many-to-one | master-to-detail => false (but not supported)
        pcAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        pcAssociation.setMaxCardinality(2);
        targetPcAssociation.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        targetPcAssociation.setMaxCardinality(1);
        assertFalse(persistenceAssociatonInfo.isJoinColumnRequired(targetPcAssociation));
        assertTrue(inversePersistenceAssociatonInfo.isJoinColumnRequired(pcAssociation));

        // one-to-many | association => false
        // many-to-one | association => true
        pcAssociation.setAssociationType(AssociationType.ASSOCIATION);
        pcAssociation.setMaxCardinality(2);
        targetPcAssociation.setAssociationType(AssociationType.ASSOCIATION);
        targetPcAssociation.setMaxCardinality(1);
        assertFalse(persistenceAssociatonInfo.isJoinColumnRequired(targetPcAssociation));
        assertTrue(inversePersistenceAssociatonInfo.isJoinColumnRequired(pcAssociation));

        // many-to-many | all => false
        pcAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        pcAssociation.setMaxCardinality(2);
        targetPcAssociation.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        targetPcAssociation.setMaxCardinality(2);
        assertFalse(persistenceAssociatonInfo.isJoinColumnRequired(targetPcAssociation));
        assertFalse(inversePersistenceAssociatonInfo.isJoinColumnRequired(pcAssociation));

        // unidirectional:

        // all | all => true
        pcAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        pcAssociation.setMaxCardinality(1);
        pcAssociation.setInverseAssociation("");
        targetPcAssociation.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        targetPcAssociation.setMaxCardinality(2);
        targetPcAssociation.setInverseAssociation("");
        assertTrue(persistenceAssociatonInfo.isJoinColumnRequired(targetPcAssociation));
        assertTrue(inversePersistenceAssociatonInfo.isJoinColumnRequired(pcAssociation));
    }

    public void testColumnNamesUnique() {
        // TODO Joerg Testfall column names der JoinColumns muessen unique sein!
    }
}
