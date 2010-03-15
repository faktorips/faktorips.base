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

        persistenceAssociatonInfo.setTransient(true);
        targetPcAssociation.getPersistenceAssociatonInfo().setTransient(true);
        ml = persistenceAssociatonInfo.validate(ipsProject);
        assertEquals(0, ml.getNoOfMessages());

        persistenceAssociatonInfo.setTransient(false);
        targetPcAssociation.getPersistenceAssociatonInfo().setTransient(false);
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

        ml = sourcePersistenceAssociatonInfo.validate(ipsProject);
        assertEquals(0, ml.getNoOfMessages());
        ml = targetPersistenceAssociatonInfo.validate(ipsProject);
        assertEquals(0, ml.getNoOfMessages());

        // test target association must be transient or target policy component persist disabled

        sourcePersistenceAssociatonInfo.setTransient(true);
        ml = sourcePersistenceAssociatonInfo.validate(ipsProject);
        assertEquals(1, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_TARGET_SIDE_NOT_TRANSIENT));
        ml = targetPersistenceAssociatonInfo.validate(ipsProject);
        assertEquals(1, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IPersistentAssociationInfo.MSGCODE_TARGET_SIDE_NOT_TRANSIENT));

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
        ml = sourcePersistenceAssociatonInfo.validate(ipsProject);
        System.out.println(ml);
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

        // unidirectional :n Composition master to detail, join table required
        pcAssociation.setInverseAssociation("");
        setAssociationTypeAndCardinality(AssociationType.COMPOSITION_MASTER_TO_DETAIL, null, new int[] { 0, 2 }, null);
        assertTrue(persistenceAssociatonInfo.isJoinTableRequired());
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
        assertEquals("joinTable1", persistenceAssociatonInfo.getJoinTableName());
        assertEquals("targetColumn1", persistenceAssociatonInfo.getTargetColumnName());
        assertEquals("sourceColumn1", persistenceAssociatonInfo.getSourceColumnName());
        assertEquals(FetchType.LAZY, persistenceAssociatonInfo.getFetchType());
    }

    public void testToXml() throws CoreException {
        IPersistentAssociationInfo persistenceAssociatonInfo = pcAssociation.getPersistenceAssociatonInfo();
        persistenceAssociatonInfo.setTransient(true);
        persistenceAssociatonInfo.setJoinTableName("joinTable0");
        persistenceAssociatonInfo.setTargetColumnName("targetColumn0");
        persistenceAssociatonInfo.setSourceColumnName("sourceColumn0");
        persistenceAssociatonInfo.setFetchType(FetchType.EAGER);
        Element element = persistenceAssociatonInfo.toXml(newDocument());

        PolicyCmptType copyOfPcType = (PolicyCmptType)newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "Copy");
        copyOfPcType.newPolicyCmptTypeAssociation();
        IPersistentAssociationInfo copyOfPersistenceAssociatonInfo = copyOfPcType.getPolicyCmptTypeAssociations()[0]
                .getPersistenceAssociatonInfo();
        copyOfPersistenceAssociatonInfo.initFromXml(element);

        assertTrue(copyOfPersistenceAssociatonInfo.isTransient());
        assertEquals("joinTable0", copyOfPersistenceAssociatonInfo.getJoinTableName());
        assertEquals("targetColumn0", copyOfPersistenceAssociatonInfo.getTargetColumnName());
        assertEquals("sourceColumn0", copyOfPersistenceAssociatonInfo.getSourceColumnName());
        assertEquals(FetchType.EAGER, copyOfPersistenceAssociatonInfo.getFetchType());
    }
}
