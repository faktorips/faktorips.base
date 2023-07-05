/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import static org.faktorips.testsupport.IpsMatchers.containsMessages;
import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.faktorips.testsupport.IpsMatchers.hasSize;
import static org.faktorips.testsupport.IpsMatchers.isEmpty;
import static org.faktorips.testsupport.IpsMatchers.lacksMessageCode;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.ILabel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpt.Cardinality;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class ProductCmptLinkTest extends AbstractIpsPluginTest {

    private IIpsSrcFile ipsSrcFile;
    private ProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IProductCmptLink link;
    private ProductCmpt template;
    private IProductCmptGeneration templateGeneration;
    private IProductCmptLink templateLink;
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

        template = newProductTemplate(productCmptType, "TestTemplate");
        templateGeneration = template.getProductCmptGeneration(0);
        templateLink = templateGeneration.newLink("CoverageType");

        ipsSrcFile = productCmpt.getIpsSrcFile();
    }

    @Test
    public void testGetAssociation() {
        assertEquals("CoverageType", link.getAssociation());
    }

    @Test
    public void testFindAssociation() {
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
        newProductCmptType(ipsProject, "my.TargetType");
        ProductCmpt targetProductCmpt = newProductCmpt(productCmptType, "my.Target");
        targetProductCmpt.setRuntimeId("my.target.runtime.id");
        link.setTarget("my.Target");
        assertEquals("my.Target", link.getTarget());
        assertEquals("my.target.runtime.id", link.getTargetRuntimeId());
        assertTrue(ipsSrcFile.isDirty());
    }

    @Test
    public void testSetTarget_DoesNotExist() {
        link.setTarget("newTarget");
        assertEquals("newTarget", link.getTarget());
        assertNull(link.getTargetRuntimeId());
        assertTrue(ipsSrcFile.isDirty());
    }

    @Test
    public void testToXml() {
        setUpAssociation(true);
        productCmpt.setTemplate("TestTemplate");
        templateLink.setTarget("newTarget");
        templateLink.setMinCardinality(2);
        templateLink.setMaxCardinality(3);
        templateLink.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        link.setTarget("newTarget");
        link.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        Element element = link.toXml(newDocument());

        IProductCmptLink copy = new ProductCmptLink(generation, "asd");
        copy.initFromXml(element);
        assertEquals(link.getId(), copy.getId());
        assertEquals("newTarget", copy.getTarget());
        assertEquals("CoverageType", copy.getAssociation());
        assertEquals(2, copy.getMinCardinality());
        assertEquals(3, copy.getMaxCardinality());
        assertEquals(TemplateValueStatus.INHERITED, copy.getTemplateValueStatus());

        templateLink.setMaxCardinality(Integer.MAX_VALUE);
        element = link.toXml(newDocument());
        copy.initFromXml(element);
        assertEquals(Integer.MAX_VALUE, copy.getMaxCardinality());
    }

    @Test
    public void testInitFromXml() {
        setUpAssociation(true);
        IProductCmptTypeAssociation association = productCmptType.newProductCmptTypeAssociation();
        association.setTargetRoleSingular("FullCoverage");
        productCmpt.setTemplate("TestTemplate");
        templateLink.setAssociation("FullCoverage");
        templateLink.setTarget("FullCoveragePlus");
        templateLink.setMinCardinality(2);
        templateLink.setMaxCardinality(3);
        templateLink.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        link.initFromXml((Element)getTestDocument().getDocumentElement().getElementsByTagName(IProductCmptLink.TAG_NAME)
                .item(0));
        assertEquals("42", link.getId());
        assertEquals("FullCoverage", link.getAssociation());
        assertEquals("FullCoveragePlus", link.getTarget());
        assertEquals(2, link.getMinCardinality());
        assertEquals(3, link.getMaxCardinality());
        assertEquals(TemplateValueStatus.INHERITED, link.getTemplateValueStatus());

        link.initFromXml((Element)getTestDocument().getDocumentElement().getElementsByTagName(IProductCmptLink.TAG_NAME)
                .item(1));
        assertEquals("43", link.getId());
        assertEquals(1, link.getMinCardinality());
        assertEquals(Integer.MAX_VALUE, link.getMaxCardinality());

        link.initFromXml((Element)getTestDocument().getDocumentElement().getElementsByTagName(IProductCmptLink.TAG_NAME)
                .item(2));
        assertEquals("44", link.getId());
        assertEquals(Cardinality.UNDEFINED, link.getCardinality());
    }

    @Test
    public void testValidateUnknownAssociate() {
        MessageList ml = link.validate(ipsProject);
        assertThat(ml, hasMessageCode(IProductCmptLink.MSGCODE_UNKNWON_ASSOCIATION));
    }

    @Test
    public void testValidateUnknownTarget() {
        link.setTarget("unknown");
        MessageList ml = link.validate(ipsProject);
        assertThat(ml, hasMessageCode(IProductCmptLink.MSGCODE_UNKNWON_TARGET));

        link.setTarget(productCmpt.getQualifiedName());
        ml = link.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IProductCmptLink.MSGCODE_UNKNWON_TARGET));
    }

    @Test
    public void testValidateCardinalityForQualified() {
        IPolicyCmptType coverageType = newPolicyAndProductCmptType(ipsProject, "TestCoverage", "TestCoverageType");
        IProductCmptType coverageTypeType = coverageType.findProductCmptType(ipsProject);

        ProductCmpt cmpt = newProductCmpt(coverageTypeType, "CoverageType");
        link.setTarget(cmpt.getQualifiedName());

        IProductCmptTypeAssociation productAssociation = productCmptType.newProductCmptTypeAssociation();
        productAssociation.setTarget(coverageTypeType.getQualifiedName());
        productAssociation.setTargetRoleSingular("CoverageType");

        IPolicyCmptTypeAssociation policyAssociation = (IPolicyCmptTypeAssociation)policyCmptType.newAssociation();
        policyAssociation.setTarget(coverageType.getQualifiedName());
        policyAssociation.setTargetRoleSingular("Coverage");
        policyAssociation.setQualified(true);
        policyAssociation.setMinCardinality(1);
        policyAssociation.setMaxCardinality(1);

        MessageList msgList = link.validate(ipsProject);
        assertThat(msgList, isEmpty());

        IProductCmptLink secondLink = generation.newLink("CoverageType");
        secondLink.setTarget(cmpt.getQualifiedName());
        secondLink.setMaxCardinality(1);

        msgList = link.validate(ipsProject);
        assertThat(msgList, isEmpty());
        msgList = secondLink.validate(ipsProject);
        assertThat(msgList, isEmpty());

        secondLink.setMaxCardinality(2);
        msgList = secondLink.validate(ipsProject);
        assertThat(msgList, containsMessages());
        assertThat(msgList, hasMessageCode(IProductCmptLink.MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX));
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
        assertThat(ml.getMessagesFor(link), hasSize(1));
        assertEquals(invalidTargetMessage, ml.getMessagesFor(link).getMessage(0));

        link.setTarget(target.getQualifiedName());

        ml = link.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IProductCmptLink.MSGCODE_INVALID_TARGET));
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
    public void testChangingOverTimeMatchesContainer() {
        setUpAssociation(true);

        MessageList messageList = link.validate(ipsProject);
        assertThat(messageList, hasSize(0));
    }

    @Test
    public void testChangingOverTimeDoesNotMatchContainer() {
        setUpAssociation(false);

        MessageList messageList = link.validate(ipsProject);
        assertThat(messageList, hasSize(1));
    }

    private void setUpAssociation(boolean changingOverTime) {
        IPolicyCmptType coverageType = newPolicyAndProductCmptType(ipsProject, "TestCoverage", "TestCoverageType");
        IProductCmptType coverageTypeType = coverageType.findProductCmptType(ipsProject);
        IProductCmptTypeAssociation productAssociation = productCmptType.newProductCmptTypeAssociation();
        productAssociation.setTarget(coverageTypeType.getQualifiedName());
        productAssociation.setTargetRoleSingular("CoverageType");

        productAssociation.setChangingOverTime(changingOverTime);

        IProductCmpt targetCmpt = newProductCmpt(coverageTypeType, "TestCoverage");
        link.setTarget(targetCmpt.getQualifiedName());
    }

    @Test
    public void testGetCaption() {
        createAssociation();
        assertEquals("foo", link.getCaption(Locale.US));
    }

    @Test
    public void testGetCaptionNullPointer() {
        try {
            link.getCaption(null);
            fail();
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testGetPluralCaption() {
        createAssociation();
        assertEquals("foos", link.getPluralCaption(Locale.US));
    }

    @Test
    public void testGetPluralCaptionNullPointer() {
        try {
            link.getPluralCaption(null);
            fail();
        } catch (NullPointerException e) {
            // expected
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

    @Test
    public void testGetProductCmpt() {
        IProductCmptLink newLink = createLinkWithContainer(generation, "id1", "assoc1");
        assertNotNull(newLink.getProductCmpt());
        assertEquals(productCmpt, newLink.getProductCmpt());
    }

    @Test
    public void testGetProductCmpt2() {
        IProductCmptLink newLink = createLinkWithContainer(productCmpt, "id1", "assoc1");
        assertNotNull(newLink.getProductCmpt());
        assertEquals(productCmpt, newLink.getProductCmpt());
    }

    @Test
    public void testGetProductCmptGeneration() {
        IProductCmptLink newLink = createLinkWithContainer(generation, "id1", "assoc1");

        assertNotNull(newLink.getProductCmptLinkContainer());
        assertEquals(generation, newLink.getProductCmptLinkContainer());
    }

    @Test
    public void testGetProductCmptGeneration2() {
        IProductCmptLink newLink = createLinkWithContainer(productCmpt, "id1", "assoc1");

        assertFalse(newLink.getProductCmptLinkContainer() instanceof IProductCmptGeneration);
    }

    private IProductCmptLink createLinkWithContainer(IProductCmptLinkContainer container,
            String partId,
            String associationName) {
        IProductCmptLink newLink = new ProductCmptLink(container, partId);
        newLink.setAssociation(associationName);
        return newLink;
    }

    @Test
    public void testGetMinCardinality() throws Exception {
        Cardinality cardinality = new Cardinality(123, 321, 221);
        link.setCardinality(cardinality);

        assertThat(link.getMinCardinality(), is(123));
    }

    @Test
    public void testGetMaxCardinality() throws Exception {
        Cardinality cardinality = new Cardinality(123, 321, 221);
        link.setCardinality(cardinality);

        assertThat(link.getMaxCardinality(), is(321));
    }

    @Test
    public void testGetDefaultCardinality() throws Exception {
        Cardinality cardinality = new Cardinality(123, 321, 221);
        link.setCardinality(cardinality);

        assertThat(link.getDefaultCardinality(), is(221));
    }

    @Test
    public void testGetCardinality_Defined_NoTemplate() throws Exception {
        Cardinality cardinality = new Cardinality(123, 321, 221);
        link.setCardinality(cardinality);
        link.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        assertThat(link.getCardinality(), is(cardinality));
    }

    @Test
    public void testGetCardinality_Inherited_WithTemplate() throws Exception {
        Cardinality templateCardinality = new Cardinality(123, 321, 221);
        Cardinality linkCardinality = new Cardinality(2, 5, 3);
        IProductCmptLink templateLink = createTemplateLink();
        templateLink.setCardinality(templateCardinality);
        link.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        link.setCardinality(linkCardinality);

        assertThat(link.getCardinality(), is(linkCardinality));
    }

    @Test
    public void testGetCardinality_Undefined() throws Exception {
        Cardinality cardinality = new Cardinality(123, 321, 221);
        IProductCmptLink templateLink = createTemplateLink();
        templateLink.setCardinality(cardinality);
        link.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        assertThat(link.getCardinality(), is(Cardinality.UNDEFINED));
    }

    @Test
    public void testGetCardinality_Inherited() throws Exception {
        Cardinality cardinality = new Cardinality(123, 321, 221);
        IProductCmptLink templateLink = createTemplateLink();
        templateLink.setCardinality(cardinality);
        link.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        assertThat(link.getCardinality(), is(cardinality));
    }

    @Test
    public void testSetTemplateValueStatus_CopyCardinality() throws Exception {
        Cardinality templateCardinality = new Cardinality(123, 321, 221);
        Cardinality linkCardinality = new Cardinality(2, 5, 3);
        link.setCardinality(linkCardinality);
        link.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        IProductCmptLink templateLink = createTemplateLink();
        templateLink.setCardinality(templateCardinality);
        link.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        assertThat(link.getCardinality(), is(templateCardinality));
    }

    @Test
    public void testSetTemplateValueStatus_FormerlyUndefinedCardinalityIsCopiedFromTemplate() throws Exception {
        Cardinality cardinality = new Cardinality(123, 321, 221);
        IProductCmptLink templateLink = createTemplateLink();
        templateLink.setCardinality(cardinality);
        link.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        assertThat(link.getCardinality(), is(Cardinality.UNDEFINED));

        link.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        assertThat(link.getCardinality(), is(cardinality));
    }

    @Test
    public void testDelete_InheritedLinkIsSetToUndefined() {
        IProductCmptLink templateLink = createTemplateLink();
        link.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        // sanity check
        assertThat(link.findTemplateProperty(ipsProject), is(templateLink));

        link.delete();
        assertThat(link.isDeleted(), is(false));
        assertThat(link.getTemplateValueStatus(), is(TemplateValueStatus.UNDEFINED));
        assertThat(generation.getLinksAsList(), hasItem(link));
    }

    @Test
    public void testDelete_DefinedLinkIsSetToUndefinedWhenTemplateIsPresent() {
        IProductCmptLink templateLink = createTemplateLink();
        link.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        // sanity check
        assertThat(link.findTemplateProperty(ipsProject), is(templateLink));

        link.delete();
        assertThat(link.isDeleted(), is(false));
        assertThat(link.getTemplateValueStatus(), is(TemplateValueStatus.UNDEFINED));
        assertThat(generation.getLinksAsList(), hasItem(link));
    }

    @Test
    public void testDelete_LinkIsDeletedIfNoTemplateLinkExists() {
        // There is a template but no matching link
        ProductCmpt template = newProductTemplate(productCmptType, "Template");
        productCmpt.setTemplate(template.getQualifiedName());

        // sanity check
        assertThat(productCmpt.isUsingTemplate(), is(true));
        assertThat(link.findTemplateProperty(ipsProject), is(nullValue()));
        assertThat(generation.getLinksAsList(), hasItem(link));

        link.delete();
        assertThat(link.isDeleted(), is(true));
        assertThat(generation.getLinksAsList().size(), is(0));
    }

    @Test
    public void testDelete_LinkIsDeletedIfTemplateDoesNotExist() {
        // The referenced template does not exist
        productCmpt.setTemplate("There is no such template");

        // sanity check
        assertThat(productCmpt.isUsingTemplate(), is(true));
        assertThat(link.findTemplateProperty(ipsProject), is(nullValue()));
        assertThat(generation.getLinksAsList(), hasItem(link));

        link.delete();
        assertThat(link.isDeleted(), is(true));
        assertThat(generation.getLinksAsList().size(), is(0));
    }

    @Test
    public void testDelete_LinkIsDeletedIfNoTemplateExists() {
        // sanity check
        assertThat(productCmpt.isUsingTemplate(), is(false));
        assertThat(link.findTemplateProperty(ipsProject), is(nullValue()));
        assertThat(generation.getLinksAsList(), hasItem(link));

        link.delete();
        assertThat(link.isDeleted(), is(true));
        assertThat(generation.getLinksAsList().size(), is(0));
    }

    protected IProductCmptLink createTemplateLink() {
        setUpAssociation(true);
        ProductCmpt template = newProductTemplate(productCmptType, "Template");
        productCmpt.setTemplate(template.getQualifiedName());
        IProductCmptLink templateLink = template.getProductCmptGeneration(0).newLink("CoverageType");
        templateLink.setTarget("newTarget");
        link.setTarget("newTarget");
        return templateLink;
    }

    @Test
    public void testIsConcreteValue() {
        // make product cmpt part of template hierarchy
        setUpAssociation(true);
        productCmpt.setTemplate("TestTemplate");
        templateLink.setTarget("newTarget");
        templateLink.setMinCardinality(2);
        templateLink.setMaxCardinality(3);
        templateLink.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        templateLink.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        assertTrue(templateLink.isConcreteValue());

        templateLink.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        assertTrue(templateLink.isConcreteValue());

        link.setTarget("newTarget");
        link.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        assertTrue(link.isConcreteValue());

        link.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        assertFalse(link.isConcreteValue());
    }

    @Test
    public void testIsConfiguringPolicyAssociation() {
        IProductCmptTypeAssociation productAssociation = productCmptType.newProductCmptTypeAssociation();
        productAssociation.setTargetRoleSingular("CoverageType");
        productAssociation.setMatchingAssociationSource(policyCmptType.getName());
        productAssociation.setMatchingAssociationName("Coverage");
        assertThat(link.isConfiguringPolicyAssociation(), is(false));

        IPolicyCmptTypeAssociation policyAssociation = policyCmptType.newPolicyCmptTypeAssociation();
        policyAssociation.setTargetRoleSingular("Coverage");
        assertThat(link.isConfiguringPolicyAssociation(), is(true));
    }

    @Test
    public void testIsConfiguringPolicyAssociation_ProductCmptWithoutPolicyCmpt() {
        IProductCmptType type = newProductCmptType(ipsProject, "NonConfiguringType");
        IProductCmptTypeAssociation productAssociation = type.newProductCmptTypeAssociation();
        productAssociation.setTargetRoleSingular("CoverageType");

        IProductCmpt cmpt = newProductCmpt(type, "NonConfiguringComponent");
        IProductCmptGeneration gen = cmpt.getProductCmptGeneration(0);
        IProductCmptLink nonConfiguringLink = gen.newLink("CoverageType");

        assertThat(nonConfiguringLink.isConfiguringPolicyAssociation(), is(false));
    }

    @Test
    public void testIsConfiguringPolicyAssociation_ProductCmptTypeNotFound() {
        IProductCmptType type = newProductCmptType(ipsProject, "NonConfiguringType");
        IProductCmptTypeAssociation productAssociation = type.newProductCmptTypeAssociation();
        productAssociation.setTargetRoleSingular("CoverageType");

        IProductCmpt cmpt = newProductCmpt(type, "NonConfiguringComponent");
        IProductCmptGeneration gen = cmpt.getProductCmptGeneration(0);
        IProductCmptLink nonConfiguringLink = gen.newLink("CoverageType");
        cmpt.setProductCmptType("NonExistant");

        assertThat(nonConfiguringLink.isConfiguringPolicyAssociation(), is(false));
    }

}
