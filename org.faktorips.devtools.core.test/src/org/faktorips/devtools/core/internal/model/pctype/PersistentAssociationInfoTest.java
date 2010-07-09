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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPersistentAssociationInfo;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPersistentAssociationInfo.FetchType;
import org.faktorips.devtools.core.model.pctype.IPersistentAssociationInfo.RelationshipType;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.PersistentType;
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

        targetPolicyCmptType.getPersistenceTypeInfo().setPersistentType(PersistentType.ENTITY);
        policyCmptType.getPersistenceTypeInfo().setPersistentType(PersistentType.ENTITY);

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

        targetPersistenceAssociatonInfo.setFetchType(FetchType.EAGER);
        targetPersistenceAssociatonInfo.setJoinColumnName("JoinColumn");

        ml = sourcePersistenceAssociatonInfo.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_TRANSIENT_MISMATCH));
        assertEquals(0, ml.getNoOfMessages());

        ml = targetPersistenceAssociatonInfo.validate(ipsProject);

        assertNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_TRANSIENT_MISMATCH));
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
                .getPersistenceTypeInfo().setPersistentType(PersistentType.NONE);
        ml = sourcePersistenceAssociatonInfo.validate(ipsProject);
        assertEquals(0, ml.getNoOfMessages());
        ml = targetPersistenceAssociatonInfo.validate(ipsProject);
        assertEquals(0, ml.getNoOfMessages());

        targetPersistenceAssociatonInfo.getPolicyComponentTypeAssociation().getPolicyCmptType()
                .getPersistenceTypeInfo().setPersistentType(PersistentType.ENTITY);

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

    public void testInitFromXml() {
        NodeList nodeList = getTestDocument().getElementsByTagName(IPersistentAssociationInfo.XML_TAG);
        assertEquals(1, nodeList.getLength());

        Element element = (Element)nodeList.item(0);
        IPersistentAssociationInfo persistenceAssociatonInfo = pcAssociation.getPersistenceAssociatonInfo();
        persistenceAssociatonInfo.initFromXml(element);

        assertFalse(persistenceAssociatonInfo.isTransient());
        assertFalse(persistenceAssociatonInfo.isOwnerOfManyToManyAssociation());
        assertTrue(persistenceAssociatonInfo.isCascadeTypeOverwriteDefault());
        assertTrue(persistenceAssociatonInfo.isCascadeTypeRefresh());
        assertTrue(persistenceAssociatonInfo.isCascadeTypeMerge());
        assertTrue(persistenceAssociatonInfo.isCascadeTypePersist());
        assertTrue(persistenceAssociatonInfo.isCascadeTypeRemove());
        assertTrue(persistenceAssociatonInfo.isOrphanRemoval());
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
        persistenceAssociatonInfo.setCascadeTypeOverwriteDefault(true);
        persistenceAssociatonInfo.setCascadeTypeMerge(true);
        persistenceAssociatonInfo.setCascadeTypeRemove(true);
        persistenceAssociatonInfo.setCascadeTypeRefresh(true);
        persistenceAssociatonInfo.setCascadeTypePersist(true);
        persistenceAssociatonInfo.setJoinTableName("joinTable0");
        persistenceAssociatonInfo.setTargetColumnName("targetColumn0");
        persistenceAssociatonInfo.setSourceColumnName("sourceColumn0");
        persistenceAssociatonInfo.setFetchType(FetchType.EAGER);
        persistenceAssociatonInfo.setJoinColumnName("joinColumn0");
        persistenceAssociatonInfo.setOrphanRemoval(true);
        Element element = persistenceAssociatonInfo.toXml(newDocument());

        PolicyCmptType copyOfPcType = (PolicyCmptType)newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "Copy");
        copyOfPcType.newPolicyCmptTypeAssociation();
        IPersistentAssociationInfo copyOfPersistenceAssociatonInfo = copyOfPcType.getPolicyCmptTypeAssociations()[0]
                .getPersistenceAssociatonInfo();
        copyOfPersistenceAssociatonInfo.initFromXml(element);

        assertTrue(copyOfPersistenceAssociatonInfo.isTransient());
        assertTrue(copyOfPersistenceAssociatonInfo.isOwnerOfManyToManyAssociation());
        assertTrue(copyOfPersistenceAssociatonInfo.isCascadeTypeOverwriteDefault());
        assertTrue(copyOfPersistenceAssociatonInfo.isCascadeTypeRefresh());
        assertTrue(copyOfPersistenceAssociatonInfo.isCascadeTypeMerge());
        assertTrue(copyOfPersistenceAssociatonInfo.isCascadeTypePersist());
        assertTrue(copyOfPersistenceAssociatonInfo.isCascadeTypeRemove());
        assertEquals("joinTable0", copyOfPersistenceAssociatonInfo.getJoinTableName());
        assertEquals("targetColumn0", copyOfPersistenceAssociatonInfo.getTargetColumnName());
        assertEquals("sourceColumn0", copyOfPersistenceAssociatonInfo.getSourceColumnName());
        assertEquals(FetchType.EAGER, copyOfPersistenceAssociatonInfo.getFetchType());
        assertEquals("joinColumn0", copyOfPersistenceAssociatonInfo.getJoinColumnName());
        assertTrue(copyOfPersistenceAssociatonInfo.isOrphanRemoval());
    }

    public void testIsJoinColumnRequired() {
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

    public void testInitDefaultCascadeTypes() throws CoreException {
        pcAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        pcAssociation.setMaxCardinality(2);
        targetPcAssociation.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        targetPcAssociation.setMaxCardinality(1);

        PersistentAssociationInfo persistenceAssociatonInfo = (PersistentAssociationInfo)pcAssociation
                .getPersistenceAssociatonInfo();
        PersistentAssociationInfo inversePersistenceAssociatonInfo = (PersistentAssociationInfo)targetPcAssociation
                .getPersistenceAssociatonInfo();

        persistenceAssociatonInfo.initDefaultsCascadeTypes();
        assertTrue(persistenceAssociatonInfo.isCascadeTypeMerge());
        assertTrue(persistenceAssociatonInfo.isCascadeTypeRefresh());
        assertTrue(persistenceAssociatonInfo.isCascadeTypeRemove());
        assertTrue(persistenceAssociatonInfo.isCascadeTypePersist());

        inversePersistenceAssociatonInfo.initDefaultsCascadeTypes();
        assertFalse(inversePersistenceAssociatonInfo.isCascadeTypeMerge());
        assertFalse(inversePersistenceAssociatonInfo.isCascadeTypeRefresh());
        assertFalse(inversePersistenceAssociatonInfo.isCascadeTypeRemove());
        assertFalse(inversePersistenceAssociatonInfo.isCascadeTypePersist());

        pcAssociation.setAssociationType(AssociationType.ASSOCIATION);
        pcAssociation.setMaxCardinality(1);
        targetPcAssociation.setAssociationType(AssociationType.ASSOCIATION);
        targetPcAssociation.setMaxCardinality(1);

        persistenceAssociatonInfo.initDefaultsCascadeTypes();
        assertFalse(persistenceAssociatonInfo.isCascadeTypeMerge());
        assertFalse(persistenceAssociatonInfo.isCascadeTypeRefresh());
        assertFalse(persistenceAssociatonInfo.isCascadeTypeRemove());
        assertFalse(persistenceAssociatonInfo.isCascadeTypePersist());

        inversePersistenceAssociatonInfo.initDefaultsCascadeTypes();
        assertFalse(inversePersistenceAssociatonInfo.isCascadeTypeMerge());
        assertFalse(inversePersistenceAssociatonInfo.isCascadeTypeRefresh());
        assertFalse(inversePersistenceAssociatonInfo.isCascadeTypeRemove());
        assertFalse(inversePersistenceAssociatonInfo.isCascadeTypePersist());
    }

    public void testInitDefaults() throws CoreException {
        pcAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        pcAssociation.setMaxCardinality(2);
        targetPcAssociation.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        targetPcAssociation.setMaxCardinality(1);

        PersistentAssociationInfo persistenceAssociatonInfo = (PersistentAssociationInfo)pcAssociation
                .getPersistenceAssociatonInfo();
        PersistentAssociationInfo inversePersistenceAssociatonInfo = (PersistentAssociationInfo)targetPcAssociation
                .getPersistenceAssociatonInfo();
        persistenceAssociatonInfo.initDefaults();
        inversePersistenceAssociatonInfo.initDefaults();
        assertEquals(FetchType.LAZY, persistenceAssociatonInfo.getFetchType());
        assertTrue(persistenceAssociatonInfo.isOrphanRemoval());
        assertEquals(FetchType.EAGER, inversePersistenceAssociatonInfo.getFetchType());
        assertFalse(inversePersistenceAssociatonInfo.isOrphanRemoval());

        pcAssociation.setAssociationType(AssociationType.ASSOCIATION);
        pcAssociation.setMaxCardinality(1);
        targetPcAssociation.setAssociationType(AssociationType.ASSOCIATION);
        targetPcAssociation.setMaxCardinality(1);
        persistenceAssociatonInfo.initDefaults();
        inversePersistenceAssociatonInfo.initDefaults();
        assertEquals(FetchType.EAGER, persistenceAssociatonInfo.getFetchType());
        assertFalse(persistenceAssociatonInfo.isOrphanRemoval());
        assertEquals(FetchType.EAGER, inversePersistenceAssociatonInfo.getFetchType());
        assertFalse(inversePersistenceAssociatonInfo.isOrphanRemoval());
    }

    public void testValidateLazyFetchForSingleValuedAssociationsAllowed() throws CoreException {
        MessageList msgList = null;

        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.getPersistenceOptions().setAllowLazyFetchForSingleValuedAssociations(false);
        ipsProject.setProperties(properties);

        IPersistentAssociationInfo persistenceAssociatonInfo = pcAssociation.getPersistenceAssociatonInfo();

        pcAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        pcAssociation.setMaxCardinality(1);
        targetPcAssociation.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        targetPcAssociation.setMaxCardinality(1);

        // eager always allowed
        persistenceAssociatonInfo.setFetchType(FetchType.EAGER);
        msgList = persistenceAssociatonInfo.validate(ipsProject);
        assertNull(msgList
                .getMessageByCode(IPersistentAssociationInfo.MSGCODE_LAZY_FETCH_FOR_SINGLE_VALUED_ASSOCIATIONS_NOT_ALLOWED));

        // lazy not allowed in properties

        persistenceAssociatonInfo.setFetchType(FetchType.LAZY);
        msgList = persistenceAssociatonInfo.validate(ipsProject);
        assertNotNull(msgList
                .getMessageByCode(IPersistentAssociationInfo.MSGCODE_LAZY_FETCH_FOR_SINGLE_VALUED_ASSOCIATIONS_NOT_ALLOWED));

        pcAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        pcAssociation.setMaxCardinality(2);
        targetPcAssociation.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        targetPcAssociation.setMaxCardinality(1);
        persistenceAssociatonInfo.setFetchType(FetchType.LAZY);
        msgList = persistenceAssociatonInfo.validate(ipsProject);
        assertNull(msgList
                .getMessageByCode(IPersistentAssociationInfo.MSGCODE_LAZY_FETCH_FOR_SINGLE_VALUED_ASSOCIATIONS_NOT_ALLOWED));

        // lazy allowed in properties
        properties = ipsProject.getProperties();
        properties.getPersistenceOptions().setAllowLazyFetchForSingleValuedAssociations(true);
        ipsProject.setProperties(properties);
        assertTrue(ipsProject.getProperties().getPersistenceOptions().isAllowLazyFetchForSingleValuedAssociations());

        persistenceAssociatonInfo.setFetchType(FetchType.LAZY);
        msgList = persistenceAssociatonInfo.validate(ipsProject);
        assertNull(msgList
                .getMessageByCode(IPersistentAssociationInfo.MSGCODE_LAZY_FETCH_FOR_SINGLE_VALUED_ASSOCIATIONS_NOT_ALLOWED));
    }

    public void testEvalRelationShipType() throws CoreException {
        IPersistentAssociationInfo persistenceAssociatonInfo = pcAssociation.getPersistenceAssociatonInfo();
        IPersistentAssociationInfo inversePersistenceAssociatonInfo = targetPcAssociation
                .getPersistenceAssociatonInfo();

        pcAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        pcAssociation.setMaxCardinality(1);
        targetPcAssociation.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        targetPcAssociation.setMaxCardinality(1);
        assertEquals(RelationshipType.ONE_TO_ONE, persistenceAssociatonInfo
                .evalBidirectionalRelationShipType(targetPcAssociation));
        assertEquals(RelationshipType.ONE_TO_ONE, inversePersistenceAssociatonInfo
                .evalBidirectionalRelationShipType(pcAssociation));

        pcAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        pcAssociation.setMaxCardinality(2);
        targetPcAssociation.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        targetPcAssociation.setMaxCardinality(1);
        assertEquals(RelationshipType.ONE_TO_MANY, persistenceAssociatonInfo
                .evalBidirectionalRelationShipType(targetPcAssociation));
        assertEquals(RelationshipType.MANY_TO_ONE, inversePersistenceAssociatonInfo
                .evalBidirectionalRelationShipType(pcAssociation));

        pcAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        pcAssociation.setMaxCardinality(2);
        targetPcAssociation.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        targetPcAssociation.setMaxCardinality(2);
        assertEquals(RelationshipType.MANY_TO_MANY, persistenceAssociatonInfo
                .evalBidirectionalRelationShipType(targetPcAssociation));
        assertEquals(RelationshipType.MANY_TO_MANY, inversePersistenceAssociatonInfo
                .evalBidirectionalRelationShipType(pcAssociation));

        // now test with qualified asociation (max=1) but is to-many
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "productTyp");
        pcAssociation.getPolicyCmptType().setProductCmptType(productCmptType.getQualifiedName());
        pcAssociation.setQualified(true);
        pcAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        pcAssociation.setMaxCardinality(1);
        targetPcAssociation.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        targetPcAssociation.setMaxCardinality(1);
        assertEquals(RelationshipType.ONE_TO_MANY, persistenceAssociatonInfo
                .evalBidirectionalRelationShipType(targetPcAssociation));
        assertEquals(RelationshipType.MANY_TO_ONE, inversePersistenceAssociatonInfo
                .evalBidirectionalRelationShipType(pcAssociation));

        // unidirectional
        pcAssociation.setInverseAssociation("");
        assertEquals(RelationshipType.UNKNOWN, persistenceAssociatonInfo.evalBidirectionalRelationShipType(null));
    }

    public void testColumnNamesUnique() throws CoreException {
        IPolicyCmptTypeAssociation pcAssociation2 = policyCmptType.newPolicyCmptTypeAssociation();
        IPersistentAssociationInfo persistenceAssociatonInfo1 = pcAssociation.getPersistenceAssociatonInfo();
        IPersistentAssociationInfo persistenceAssociatonInfo2 = pcAssociation2.getPersistenceAssociatonInfo();

        persistenceAssociatonInfo1.setJoinColumnName("a");
        persistenceAssociatonInfo1.setSourceColumnName("");
        persistenceAssociatonInfo1.setTargetColumnName("");

        persistenceAssociatonInfo2.setJoinColumnName("b");
        persistenceAssociatonInfo2.setSourceColumnName("");
        persistenceAssociatonInfo2.setTargetColumnName("");

        MessageList ml = persistenceAssociatonInfo1.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCEATTR_DUPLICATE_COLNAME));

        ml = persistenceAssociatonInfo1.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentTypeInfo.MSGCODE_PERSISTENCEATTR_DUPLICATE_COLNAME));
    }

    public void testValidateMaxColumnAndTableNameLength() throws CoreException {
        IPersistentAssociationInfo persistenceAssociatonInfo = pcAssociation.getPersistenceAssociatonInfo();
        IPersistentAssociationInfo inversePersistenceAssociatonInfo = targetPcAssociation
                .getPersistenceAssociatonInfo();

        pcAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        pcAssociation.setMaxCardinality(1);
        targetPcAssociation.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        targetPcAssociation.setMaxCardinality(1);

        // test max join column name
        // note that the join column is only necessary on the inverse association side and therefore
        // only validated on the inverse side
        int maxColumnNameLenght = ipsProject.getProperties().getPersistenceOptions().getMaxColumnNameLenght();

        String columnName = StringUtils.repeat("a", maxColumnNameLenght);
        inversePersistenceAssociatonInfo.setJoinColumnName(columnName);

        MessageList ml = inversePersistenceAssociatonInfo.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_JOIN_COLUMN_NAME_INVALID));

        ml = inversePersistenceAssociatonInfo.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_JOIN_COLUMN_NAME_INVALID));

        inversePersistenceAssociatonInfo.setJoinColumnName("invalid" + columnName);
        ml = inversePersistenceAssociatonInfo.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_JOIN_COLUMN_NAME_INVALID));

        // test join table max table and source/target column name
        // note that the join table is only required on the owner n:n relationship side
        // and therefore only validated on this side
        inversePersistenceAssociatonInfo.setJoinColumnName("");
        int maxTableNameLenght = ipsProject.getProperties().getPersistenceOptions().getMaxTableNameLength();
        String joinTableName = StringUtils.repeat("a", maxTableNameLenght);
        String sourceColumnName = StringUtils.repeat("c", maxColumnNameLenght);
        String targetColumnName = StringUtils.repeat("d", maxColumnNameLenght);

        pcAssociation.setAssociationType(AssociationType.ASSOCIATION);
        pcAssociation.setMaxCardinality(2);
        targetPcAssociation.setAssociationType(AssociationType.ASSOCIATION);
        targetPcAssociation.setMaxCardinality(2);
        persistenceAssociatonInfo.setOwnerOfManyToManyAssociation(true);

        persistenceAssociatonInfo.setJoinTableName(joinTableName);
        persistenceAssociatonInfo.setSourceColumnName(sourceColumnName);
        persistenceAssociatonInfo.setTargetColumnName(targetColumnName);

        ml = persistenceAssociatonInfo.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_JOIN_TABLE_NAME_INVALID));
        assertNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_TARGET_COLUMN_NAME_INVALID));
        assertNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_SOURCE_COLUMN_NAME_INVALID));

        persistenceAssociatonInfo.setJoinTableName("invalid" + joinTableName);
        persistenceAssociatonInfo.setSourceColumnName("invalid" + sourceColumnName);
        persistenceAssociatonInfo.setTargetColumnName("invalid" + targetColumnName);

        ml = persistenceAssociatonInfo.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_JOIN_TABLE_NAME_INVALID));
        assertNotNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_TARGET_COLUMN_NAME_INVALID));
        assertNotNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_SOURCE_COLUMN_NAME_INVALID));
    }

    public void testValidateDerivedUnionJoinColumnNotNecessary() throws CoreException {
        // join column is only necessary for none derived union associations
        MessageList ml = null;
        PolicyCmptType superPcType = newPolicyCmptType(ipsProject, "super");
        superPcType.getPersistenceTypeInfo().setPersistentType(PersistentType.ENTITY);
        superPcType.setAbstract(true);
        targetPcAssociation.getPolicyCmptType().setSupertype(superPcType.getQualifiedName());

        IPolicyCmptTypeAssociation derivedUnionAss = policyCmptType.newPolicyCmptTypeAssociation();
        derivedUnionAss.setDerivedUnion(true);
        derivedUnionAss.setTarget(superPcType.getQualifiedName());
        derivedUnionAss.setTargetRoleSingular("derivedUnion");
        derivedUnionAss.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        // unidirectional derived union
        ml = derivedUnionAss.getPersistenceAssociatonInfo().validate(ipsProject);
        // assertNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_JOIN_COLUMN_NAME_EMPTY));

        IPolicyCmptTypeAssociation invDerivedUnion = superPcType.newPolicyCmptTypeAssociation();
        invDerivedUnion.setTarget(policyCmptType.getQualifiedName());
        invDerivedUnion.setTargetRoleSingular("invDerivedUnion");
        invDerivedUnion.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        invDerivedUnion.setInverseAssociation("derivedUnion");
        derivedUnionAss.setInverseAssociation("invDerivedUnion");

        // bidirectional derived union
        ml = derivedUnionAss.getPersistenceAssociatonInfo().validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_JOIN_COLUMN_NAME_EMPTY));

        ml = invDerivedUnion.getPersistenceAssociatonInfo().validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_JOIN_COLUMN_NAME_EMPTY));

        // test join column is necessary on derived union implementation
        // a) unidirectional
        IPolicyCmptTypeAssociation implDerivedUnionAss = policyCmptType.newPolicyCmptTypeAssociation();
        implDerivedUnionAss.setSubsettedDerivedUnion("derivedUnion");
        implDerivedUnionAss.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        implDerivedUnionAss.setTarget(targetPcAssociation.getPolicyCmptType().getQualifiedName());
        implDerivedUnionAss.setTargetRoleSingular("implDerivedUnion");
        ml = implDerivedUnionAss.getPersistenceAssociatonInfo().validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_JOIN_COLUMN_NAME_EMPTY));

        // a) bidirectional, join column must be defined on the owner side
        IPolicyCmptTypeAssociation inverseImplDerivedUnionAss = implDerivedUnionAss.newInverseAssociation();
        ml = inverseImplDerivedUnionAss.getPersistenceAssociatonInfo().validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_JOIN_COLUMN_NAME_EMPTY));
        ml = implDerivedUnionAss.getPersistenceAssociatonInfo().validate(ipsProject);
        assertNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_JOIN_COLUMN_NAME_EMPTY));
    }

    public void testIsOrphanRemovalRequired() {
        IPersistentAssociationInfo persistenceAssociatonInfo = pcAssociation.getPersistenceAssociatonInfo();
        pcAssociation.setAssociationType(AssociationType.ASSOCIATION);
        assertFalse(persistenceAssociatonInfo.isOrphanRemovalRequired());

        pcAssociation.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        assertFalse(persistenceAssociatonInfo.isOrphanRemovalRequired());

        pcAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        assertTrue(persistenceAssociatonInfo.isOrphanRemovalRequired());
    }
}
