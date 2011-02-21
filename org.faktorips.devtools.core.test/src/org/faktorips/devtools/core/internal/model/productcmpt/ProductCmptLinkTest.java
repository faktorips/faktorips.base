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

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class ProductCmptLinkTest extends AbstractIpsPluginTest {

    private IIpsSrcFile ipsSrcFile;
    private ProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IProductCmptLink link;
    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "TestPolicy", "TestProduct");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        productCmpt = newProductCmpt(productCmptType, "TestProduct");
        generation = productCmpt.getProductCmptGeneration(0);
        link = generation.newLink("CoverageType");
        ipsSrcFile = productCmpt.getIpsSrcFile();
    }

    @Test
    public void testGetAssociation() {
        assertEquals("CoverageType", link.getAssociation());
    }

    @Test
    public void testFindAssociation() throws CoreException {
        IProductCmptTypeAssociation assocation = productCmptType.newProductCmptTypeAssociation();

        assocation.setTargetRoleSingular("CoverageType");
        assertEquals(assocation, link.findAssociation(ipsProject));

        assocation.setTargetRoleSingular("blabla");
        assertNull(link.findAssociation(ipsProject));
    }

    @Test
    public void testRemove() {
        link.delete();
        assertEquals(0, generation.getNumOfLinks());
        assertTrue(ipsSrcFile.isDirty());
    }

    @Test
    public void testSetTarget() {
        link.setTarget("newTarget");
        assertEquals("newTarget", link.getTarget());
        assertTrue(ipsSrcFile.isDirty());
    }

    @Test
    public void testToXml() {
        link = generation.newLink("coverage");
        link.setTarget("newTarget");
        link.setMinCardinality(2);
        link.setMaxCardinality(3);
        Element element = link.toXml(newDocument());

        IProductCmptLink copy = new ProductCmptLink();
        copy.initFromXml(element);
        assertEquals(link.getId(), copy.getId());
        assertEquals("newTarget", copy.getTarget());
        assertEquals("coverage", copy.getAssociation());
        assertEquals(2, copy.getMinCardinality());
        assertEquals(3, copy.getMaxCardinality());

        link.setMaxCardinality(Integer.MAX_VALUE);
        element = link.toXml(newDocument());
        copy.initFromXml(element);
        assertEquals(Integer.MAX_VALUE, copy.getMaxCardinality());
    }

    @Test
    public void testInitFromXml() {
        link.initFromXml((Element)getTestDocument().getDocumentElement()
                .getElementsByTagName(IProductCmptLink.TAG_NAME).item(0));
        assertEquals("42", link.getId());
        assertEquals("FullCoverage", link.getAssociation());
        assertEquals("FullCoveragePlus", link.getTarget());
        assertEquals(2, link.getMinCardinality());
        assertEquals(3, link.getMaxCardinality());

        link.initFromXml((Element)getTestDocument().getDocumentElement()
                .getElementsByTagName(IProductCmptLink.TAG_NAME).item(1));
        assertEquals("43", link.getId());
        assertEquals(1, link.getMinCardinality());
        assertEquals(Integer.MAX_VALUE, link.getMaxCardinality());
    }

    @Test
    public void testValidateUnknownAssociate() throws CoreException {
        MessageList ml = link.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_UNKNWON_ASSOCIATION));
    }

    @Test
    public void testValidateUnknownTarget() throws CoreException {
        link.setTarget("unknown");
        MessageList ml = link.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_UNKNWON_TARGET));

        link.setTarget(productCmpt.getQualifiedName());
        ml = link.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_UNKNWON_TARGET));
    }

    @Test
    public void testValidateCardinality() throws CoreException {
        IPolicyCmptType coverageType = newPolicyAndProductCmptType(ipsProject, "TestCoverage", "TestCoverageType");
        IProductCmptType coverageTypeType = coverageType.findProductCmptType(ipsProject);

        IProductCmptTypeAssociation productAssociation = productCmptType.newProductCmptTypeAssociation();
        productAssociation.setTarget(coverageTypeType.getQualifiedName());
        productAssociation.setTargetRoleSingular("CoverageType");

        IAssociation policyAssociation = policyCmptType.newAssociation();
        policyAssociation.setTarget(coverageType.getQualifiedName());
        policyAssociation.setTargetRoleSingular("Coverage");

        // test setup
        assertEquals(policyAssociation, productAssociation.findMatchingPolicyCmptTypeAssociation(ipsProject));
        assertEquals(productAssociation, link.findAssociation(ipsProject));

        link.setMaxCardinality(0);
        MessageList ml = link.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_MISSING_MAX_CARDINALITY));
        assertNotNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_1));

        link.setMaxCardinality(1);
        ml = link.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_1));

        link.setMinCardinality(2);
        ml = link.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_MIN));

        link.setMaxCardinality(3);
        ml = link.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_MIN));

        // min-max validations according to MTB#515

        policyAssociation.setMinCardinality(1);
        policyAssociation.setMaxCardinality(1);
        link.setMinCardinality(1);
        link.setMaxCardinality(1);
        ml = link.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_MIN_CARDINALITY_FALLS_BELOW_MODEL_MIN));
        assertNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX));

        policyAssociation.setMinCardinality(1);
        policyAssociation.setMaxCardinality(1);
        link.setMinCardinality(1);
        link.setMaxCardinality(2);
        ml = link.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX));

        policyAssociation.setMinCardinality(1);
        policyAssociation.setMaxCardinality(1);
        link.setMinCardinality(0);
        link.setMaxCardinality(1);
        ml = link.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_MIN_CARDINALITY_FALLS_BELOW_MODEL_MIN));

        // with second link
        IProductCmptLink secondLink = generation.newLink("CoverageType");

        policyAssociation.setMinCardinality(1);
        policyAssociation.setMaxCardinality(1);
        link.setMinCardinality(0);
        link.setMaxCardinality(1);
        secondLink.setMinCardinality(0);
        secondLink.setMaxCardinality(1);
        ml = link.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_MIN_CARDINALITY_FALLS_BELOW_MODEL_MIN));
        assertNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX));

        policyAssociation.setMinCardinality(1);
        policyAssociation.setMaxCardinality(1);
        link.setMinCardinality(0);
        link.setMaxCardinality(1);
        secondLink.setMinCardinality(1);
        secondLink.setMaxCardinality(1);
        ml = link.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX));

        policyAssociation.setMinCardinality(2);
        policyAssociation.setMaxCardinality(2);
        link.setMinCardinality(0);
        link.setMaxCardinality(1);
        secondLink.setMinCardinality(1);
        secondLink.setMaxCardinality(1);
        ml = link.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_MIN_CARDINALITY_FALLS_BELOW_MODEL_MIN));

        policyAssociation.setMinCardinality(2);
        policyAssociation.setMaxCardinality(2);
        link.setMinCardinality(0);
        link.setMaxCardinality(1);
        secondLink.setMinCardinality(1);
        secondLink.setMaxCardinality(IProductCmptLink.CARDINALITY_MANY);
        ml = link.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_MIN_CARDINALITY_FALLS_BELOW_MODEL_MIN));
        assertNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX));

    }

    @Test
    public void testValidateInvalidTarget() throws Exception {
        IPolicyCmptType targetType = newPolicyAndProductCmptType(ipsProject, "Coverage", "CoverageType");
        IProductCmptType targetProductType = targetType.findProductCmptType(ipsProject);
        IProductCmptTypeAssociation association = productCmptType.newProductCmptTypeAssociation();
        association.setTarget(targetProductType.getQualifiedName());
        association.setTargetRoleSingular("testRelation");

        IProductCmptLink link = generation.newLink(association.getName());
        IProductCmpt target = newProductCmpt(targetProductType, "target.Target");
        link.setTarget(productCmpt.getQualifiedName());

        MessageList ml = link.validate(ipsProject);
        Message invalidTargetMessage = ml.getMessageByCode(IProductCmptLink.MSGCODE_INVALID_TARGET);
        assertNotNull(invalidTargetMessage);
        assertEquals(1, ml.getMessagesFor(link).size());
        assertEquals(invalidTargetMessage, ml.getMessagesFor(link).getMessage(0));

        link.setTarget(target.getQualifiedName());

        ml = link.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_INVALID_TARGET));
    }

    @Test
    public void testIsMandatory() {
        link.setMinCardinality(0);
        link.setMaxCardinality(1);
        assertFalse(link.isMandatory());

        link.setMinCardinality(1);
        link.setMaxCardinality(1);
        assertTrue(link.isMandatory());

        link.setMinCardinality(2);
        link.setMaxCardinality(3);
        assertFalse(link.isMandatory());

        link.setMinCardinality(3);
        link.setMaxCardinality(2);
        assertFalse(link.isMandatory());
    }

    @Test
    public void testIsOptional() {
        link.setMinCardinality(0);
        link.setMaxCardinality(1);
        assertTrue(link.isOptional());

        link.setMinCardinality(1);
        link.setMaxCardinality(1);
        assertFalse(link.isOptional());

        link.setMinCardinality(2);
        link.setMaxCardinality(3);
        assertFalse(link.isOptional());

        link.setMinCardinality(3);
        link.setMaxCardinality(2);
        assertFalse(link.isOptional());
    }

    @Test
    public void testIs1ToMany() {
        link.setMaxCardinality(10);
        assertTrue(link.is1ToMany());
        link.setMaxCardinality(1);
        assertFalse(link.is1ToMany());
    }

    @Test
    public void testGetCaption() throws CoreException {
        createAssociation();
        assertEquals("foo", link.getCaption(Locale.US));
    }

    @Test
    public void testGetCaptionNullPointer() throws CoreException {
        try {
            link.getCaption(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetPluralCaption() throws CoreException {
        createAssociation();
        assertEquals("foos", link.getPluralCaption(Locale.US));
    }

    @Test
    public void testGetPluralCaptionNullPointer() throws CoreException {
        try {
            link.getPluralCaption(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetLastResortCaption() {
        link.setAssociation("notCapitalized");
        assertEquals(StringUtils.capitalize(link.getAssociation()), link.getLastResortCaption());
    }

    @Test
    public void testGetLastResortPluralCaption() {
        link.setAssociation("notCapitalized");
        assertEquals(StringUtils.capitalize(link.getAssociation()), link.getLastResortPluralCaption());
    }

    private IAssociation createAssociation() {
        IProductCmptTypeAssociation association = productCmptType.newProductCmptTypeAssociation();
        association.setTargetRoleSingular("CoverageType");

        ILabel label = association.getLabel(Locale.US);
        label.setValue("foo");
        label.setPluralValue("foos");

        return association;
    }

}
