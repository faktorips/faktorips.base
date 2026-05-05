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

import static org.faktorips.abstracttest.matcher.Matchers.absent;
import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.faktorips.testsupport.IpsMatchers.hasSize;
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
import org.faktorips.devtools.model.productcmpt.IPolicyCmptLinkCardinality;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class PolicyCmptLinkCardinalityTest extends AbstractIpsPluginTest {

    private IIpsSrcFile ipsSrcFile;
    private ProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IPolicyCmptLinkCardinality link;
    private ProductCmpt template;
    private IProductCmptGeneration templateGeneration;
    private IPolicyCmptLinkCardinality templateLink;
    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IIpsProject ipsProject;
    private IPolicyCmptTypeAssociation policyCmptTypeAssociation;
    private IProductCmptTypeAssociation productCmptTypeAssociation;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "TestPolicy", "TestProduct");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        policyCmptTypeAssociation = policyCmptType.newPolicyCmptTypeAssociation();
        policyCmptTypeAssociation.setTargetRoleSingular("Coverage");
        productCmptTypeAssociation = productCmptType.newProductCmptTypeAssociation();
        productCmptTypeAssociation.setMatchingAssociationName("Coverage");
        productCmptTypeAssociation.setTargetRoleSingular("CoverageType");
        policyCmptTypeAssociation.setMatchingAssociationName("CoverageType");
        productCmpt = newProductCmpt(productCmptType, "TestProduct");
        generation = productCmpt.getProductCmptGeneration(0);
        link = generation.newPolicyCmptLinkCardinality("Coverage");

        template = newProductTemplate(productCmptType, "TestTemplate");
        templateGeneration = template.getProductCmptGeneration(0);
        templateLink = templateGeneration.newPolicyCmptLinkCardinality("Coverage");

        ipsSrcFile = productCmpt.getIpsSrcFile();
    }

    @Test
    public void testGetAssociation() {
        assertEquals("Coverage", link.getAssociation());
    }

    @Test
    public void testFindAssociation() {
        assertEquals(policyCmptTypeAssociation, link.findAssociation(ipsProject));

        policyCmptTypeAssociation.setTargetRoleSingular("blabla");
        assertNull(link.findAssociation(ipsProject));
    }

    @Test
    public void testRemove() {
        link.delete();
        assertThat(generation.getPolicyCmptLinkCardinality("Coverage"), is(absent()));
        assertTrue(ipsSrcFile.isDirty());
    }

    @Test
    public void testToXml() {
        setUpAssociation(true);
        productCmpt.setTemplate("TestTemplate");
        templateLink.setMinCardinality(2);
        templateLink.setMaxCardinality(3);
        templateLink.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        link.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        Element element = link.toXml(newDocument());

        IPolicyCmptLinkCardinality copy = new PolicyCmptLinkCardinality(generation, "asd");
        copy.initFromXml(element);
        assertEquals(link.getId(), copy.getId());
        assertEquals("Coverage", copy.getAssociation());
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
        productCmpt.setTemplate("TestTemplate");
        templateLink.setMinCardinality(2);
        templateLink.setMaxCardinality(3);
        templateLink.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        link.initFromXml((Element)getTestDocument().getDocumentElement()
                .getElementsByTagName(IPolicyCmptLinkCardinality.TAG_NAME)
                .item(0));
        assertEquals("42", link.getId());
        assertEquals("Coverage", link.getAssociation());
        assertEquals(2, link.getMinCardinality());
        assertEquals(3, link.getMaxCardinality());
        assertEquals(TemplateValueStatus.INHERITED, link.getTemplateValueStatus());

        link.initFromXml((Element)getTestDocument().getDocumentElement()
                .getElementsByTagName(IPolicyCmptLinkCardinality.TAG_NAME)
                .item(1));
        assertEquals("43", link.getId());
        assertEquals(1, link.getMinCardinality());
        assertEquals(Integer.MAX_VALUE, link.getMaxCardinality());

        link.initFromXml((Element)getTestDocument().getDocumentElement()
                .getElementsByTagName(IPolicyCmptLinkCardinality.TAG_NAME)
                .item(2));
        assertEquals("44", link.getId());
        assertEquals(Cardinality.UNDEFINED, link.getCardinality());
    }

    @Test
    public void testValidateUnknownAssociation() {
        link.setAssociation("foo");
        MessageList ml = link.validate(ipsProject);
        assertThat(ml, hasMessageCode(IPolicyCmptLinkCardinality.MSGCODE_UNKNOWN_ASSOCIATION));
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
        IPolicyCmptType Coverage = newPolicyAndProductCmptType(ipsProject, "TestCoverage", "TestCoverage");
        IProductCmptType CoverageType = Coverage.findProductCmptType(ipsProject);
        IProductCmptTypeAssociation productAssociation = policyCmptTypeAssociation
                .findMatchingProductCmptTypeAssociation(ipsProject);
        productAssociation.setTarget(CoverageType.getQualifiedName());
        productAssociation.setTargetRoleSingular("CoverageType");

        productAssociation.setChangingOverTime(changingOverTime);
    }

    @Test
    public void testGetCaption() {
        ILabel label = policyCmptTypeAssociation.getLabel(Locale.US);
        label.setValue("foo");
        label.setPluralValue("foos");
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
        ILabel label = policyCmptTypeAssociation.getLabel(Locale.US);
        label.setValue("foo");
        label.setPluralValue("foos");
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
        policyCmptTypeAssociation.setTargetRoleSingular("notCapitalized");
        productCmptTypeAssociation.setMatchingAssociationName("notCapitalized");
        link.setAssociation("notCapitalized");
        assertEquals(StringUtils.capitalize(link.getAssociation()), link.getLastResortCaption());
    }

    @Test
    public void testGetLastResortPluralCaption() {
        policyCmptTypeAssociation.setTargetRoleSingular("notCapitalized");
        productCmptTypeAssociation.setMatchingAssociationName("notCapitalized");
        link.setAssociation("notCapitalized");
        assertEquals(StringUtils.capitalize(link.getAssociation()), link.getLastResortPluralCaption());
    }

    @Test
    public void testGetProductCmptLinkContainer_Product() {
        IPolicyCmptLinkCardinality newPolicyCmptLinkCardinality = createLinkWithContainer(productCmpt, "id1",
                "Coverage");
        assertNotNull(newPolicyCmptLinkCardinality.getProductCmptLinkContainer());
        assertEquals(productCmpt, newPolicyCmptLinkCardinality.getProductCmptLinkContainer());
    }

    @Test
    public void testGetProductCmptLinkContainer_Generation() {
        IPolicyCmptLinkCardinality newPolicyCmptLinkCardinality = createLinkWithContainer(generation, "id1",
                "Coverage");
        assertNotNull(newPolicyCmptLinkCardinality.getProductCmptLinkContainer());
        assertEquals(generation, newPolicyCmptLinkCardinality.getProductCmptLinkContainer());
    }

    private IPolicyCmptLinkCardinality createLinkWithContainer(IProductCmptLinkContainer container,
            String partId,
            String associationName) {
        IPolicyCmptLinkCardinality newPolicyCmptLinkCardinality = new PolicyCmptLinkCardinality(container, partId);
        newPolicyCmptLinkCardinality.setAssociation(associationName);
        return newPolicyCmptLinkCardinality;
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
        IPolicyCmptLinkCardinality templateLink = createTemplateLink();
        templateLink.setCardinality(templateCardinality);
        link.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        link.setCardinality(linkCardinality);

        assertThat(link.getCardinality(), is(linkCardinality));
    }

    @Test
    public void testGetCardinality_Undefined() throws Exception {
        Cardinality cardinality = new Cardinality(123, 321, 221);
        IPolicyCmptLinkCardinality templateLink = createTemplateLink();
        templateLink.setCardinality(cardinality);
        link.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        assertThat(link.getCardinality(), is(Cardinality.UNDEFINED));
    }

    @Test
    public void testGetCardinality_Inherited() throws Exception {
        Cardinality cardinality = new Cardinality(123, 321, 221);
        IPolicyCmptLinkCardinality templateLink = createTemplateLink();
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
        IPolicyCmptLinkCardinality templateLink = createTemplateLink();
        templateLink.setCardinality(templateCardinality);
        link.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        assertThat(link.getCardinality(), is(templateCardinality));
    }

    @Test
    public void testSetTemplateValueStatus_FormerlyUndefinedCardinalityIsCopiedFromTemplate() throws Exception {
        Cardinality cardinality = new Cardinality(123, 321, 221);
        IPolicyCmptLinkCardinality templateLink = createTemplateLink();
        templateLink.setCardinality(cardinality);
        link.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        assertThat(link.getCardinality(), is(Cardinality.UNDEFINED));

        link.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        assertThat(link.getCardinality(), is(cardinality));
    }

    @Test
    public void testDelete_InheritedLinkIsSetToUndefined() {
        IPolicyCmptLinkCardinality templateLink = createTemplateLink();
        link.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        // sanity check
        assertThat(link.findTemplateProperty(ipsProject), is(templateLink));

        link.delete();
        assertThat(link.isDeleted(), is(false));
        assertThat(link.getTemplateValueStatus(), is(TemplateValueStatus.UNDEFINED));
        assertThat(generation.getPolicyCmptLinkCardinalities(), hasItem(link));
    }

    @Test
    public void testDelete_DefinedLinkIsSetToUndefinedWhenTemplateIsPresent() {
        IPolicyCmptLinkCardinality templateLink = createTemplateLink();
        link.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        // sanity check
        assertThat(link.findTemplateProperty(ipsProject), is(templateLink));

        link.delete();

        assertThat(link.isDeleted(), is(false));
        assertThat(link.getTemplateValueStatus(), is(TemplateValueStatus.UNDEFINED));
        assertThat(generation.getPolicyCmptLinkCardinalities(), hasItem(link));
    }

    @Test
    public void testDelete_LinkIsDeletedIfNoTemplateLinkExists() {
        // There is a template but no matching link
        ProductCmpt template = newProductTemplate(productCmptType, "Template");
        productCmpt.setTemplate(template.getQualifiedName());

        // sanity check
        assertThat(productCmpt.isUsingTemplate(), is(true));
        assertThat(link.findTemplateProperty(ipsProject), is(nullValue()));
        assertThat(generation.getPolicyCmptLinkCardinalities(), hasItem(link));

        link.delete();
        assertThat(link.isDeleted(), is(true));
        assertThat(generation.getPolicyCmptLinkCardinalities().size(), is(0));
    }

    @Test
    public void testDelete_LinkIsDeletedIfTemplateDoesNotExist() {
        // The referenced template does not exist
        productCmpt.setTemplate("There is no such template");

        // sanity check
        assertThat(productCmpt.isUsingTemplate(), is(true));
        assertThat(link.findTemplateProperty(ipsProject), is(nullValue()));
        assertThat(generation.getPolicyCmptLinkCardinalities(), hasItem(link));

        link.delete();
        assertThat(link.isDeleted(), is(true));
        assertThat(generation.getPolicyCmptLinkCardinalities().size(), is(0));
    }

    @Test
    public void testDelete_LinkIsDeletedIfNoTemplateExists() {
        // sanity check
        assertThat(productCmpt.isUsingTemplate(), is(false));
        assertThat(link.findTemplateProperty(ipsProject), is(nullValue()));
        assertThat(generation.getPolicyCmptLinkCardinalities(), hasItem(link));

        link.delete();
        assertThat(link.isDeleted(), is(true));
        assertThat(generation.getLinksAsList().size(), is(0));
    }

    protected IPolicyCmptLinkCardinality createTemplateLink() {
        setUpAssociation(true);
        ProductCmpt template = newProductTemplate(productCmptType, "Template");
        productCmpt.setTemplate(template.getQualifiedName());
        return template.getProductCmptGeneration(0).newPolicyCmptLinkCardinality("Coverage");
    }

    @Test
    public void testIsAssociationConfiguredInTemplate_ReturnsFalse_WhenProductCmptTypeIsNull() {
        setUpAssociation(true);
        productCmpt.setTemplate("TestTemplate");
        template.setProductCmptType("NonExistentType");

        final PolicyCmptLinkCardinality policyCmptLinkCardinality = (PolicyCmptLinkCardinality)link;
        assertThat(policyCmptLinkCardinality.isAssociationConfiguredInTemplate(), is(false));
    }

    @Test
    public void testIsAssociationConfiguredInTemplate_ReturnsFalse_WhenPolicyCmptTypeIsNull() {
        setUpAssociation(true);
        productCmpt.setTemplate("TestTemplate");
        // template resolves the same productCmptType; disabling its policy configuration
        // makes findPolicyCmptType() return null
        productCmptType.setConfigurationForPolicyCmptType(false);

        final PolicyCmptLinkCardinality policyCmptLinkCardinality = (PolicyCmptLinkCardinality)link;
        assertThat(policyCmptLinkCardinality.isAssociationConfiguredInTemplate(), is(false));
    }

    @Test
    public void testIsAssociationConfiguredInTemplate_ReturnsFalse_WhenAssociationNotFound() {
        setUpAssociation(true);
        productCmpt.setTemplate("TestTemplate");
        policyCmptTypeAssociation.setTargetRoleSingular("SomethingElse");

        final PolicyCmptLinkCardinality policyCmptLinkCardinality = (PolicyCmptLinkCardinality)link;
        assertThat(policyCmptLinkCardinality.isAssociationConfiguredInTemplate(), is(false));
    }

    @Test
    public void testIsAssociationConfiguredInTemplate_ReturnsTrue_WhenAssociationExists() {
        setUpAssociation(true);
        productCmpt.setTemplate("TestTemplate");

        final PolicyCmptLinkCardinality policyCmptLinkCardinality = (PolicyCmptLinkCardinality)link;
        assertThat(policyCmptLinkCardinality.isAssociationConfiguredInTemplate(), is(true));
    }

    @Test
    public void testIsAssociationConfiguredInTemplate_ReturnsFalse_WhenNotUsingTemplate() {
        setUpAssociation(true);

        final PolicyCmptLinkCardinality policyCmptLinkCardinality = (PolicyCmptLinkCardinality)link;
        assertThat(policyCmptLinkCardinality.isAssociationConfiguredInTemplate(), is(false));
    }

    @Test
    public void testIsConcreteValue() {
        // make product cmpt part of template hierarchy
        setUpAssociation(true);
        productCmpt.setTemplate("TestTemplate");
        templateLink.setMinCardinality(2);
        templateLink.setMaxCardinality(3);
        templateLink.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        templateLink.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        assertTrue(templateLink.isConcreteValue());

        templateLink.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        assertTrue(templateLink.isConcreteValue());

        link.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        assertTrue(link.isConcreteValue());

        link.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        assertFalse(link.isConcreteValue());
    }

}