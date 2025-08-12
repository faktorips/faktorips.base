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

import static org.faktorips.testsupport.IpsMatchers.containsText;
import static org.faktorips.testsupport.IpsMatchers.hasInvalidObject;
import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.faktorips.testsupport.IpsMatchers.isEmpty;
import static org.faktorips.testsupport.IpsMatchers.lacksMessageCode;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestIpsModelExtensions;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartContainer;
import org.faktorips.devtools.model.internal.ipsproject.properties.IpsProjectProperties;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptTypeAttribute;
import org.faktorips.devtools.model.internal.pctype.ValidationRule;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptTypeAttribute;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptTypeMethod;
import org.faktorips.devtools.model.internal.productcmpttype.TableStructureUsage;
import org.faktorips.devtools.model.internal.valueset.RangeValueSet;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.pctype.AttributeType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.productcmpt.DateBasedProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.DeltaType;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.model.productcmpt.IDeltaEntryForProperty;
import org.faktorips.devtools.model.productcmpt.IExpressionDependencyDetail;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptKind;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainerToTypeDelta;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.model.valueset.IRangeValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.values.DateUtil;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProductCmptTest extends AbstractIpsPluginTest {

    private static final String POLICY_ATTRIBUTE_NAME = "sumInsured";
    private ProductCmpt productCmpt;
    private IIpsPackageFragmentRoot root;
    private IIpsPackageFragment pack;
    private IIpsSrcFile srcFile;
    private IIpsProject ipsProject;
    private IPolicyCmptType policyCmptType;
    private IPolicyCmptTypeAttribute policyAttr;
    private IProductCmptTypeAttribute attr1;
    private IProductCmptTypeAttribute attr2;
    private IProductCmptType productCmptType;
    private IProductCmptLink link;
    private ProductCmpt target;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject(new ArrayList<>());
        root = ipsProject.getIpsPackageFragmentRoots()[0];
        pack = root.createPackageFragment("products.folder", true, null);
        srcFile = pack.createIpsFile(IpsObjectType.PRODUCT_CMPT, "TestProduct", true, null);
        productCmpt = (ProductCmpt)srcFile.getIpsObject();

        policyCmptType = newPolicyAndProductCmptType(ipsProject, "PolType", "ProdType");
        policyAttr = policyCmptType.newPolicyCmptTypeAttribute("policyAttr");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        attr1 = new ProductCmptTypeAttribute(productCmptType, "IDAttr1");
        attr1.setName("TypeAttr1");
        attr2 = new ProductCmptTypeAttribute(productCmptType, "IDAttr2");
        attr2.setName("TypeAttr2");
    }

    @Test
    public void testGetChildrenThis_generationsAreAllowed() {
        productCmpt.setProductCmptType(productCmptType.getQualifiedName());

        IPropertyValue property1 = productCmpt.newPropertyValue(attr1, IAttributeValue.class);
        IPropertyValue property2 = productCmpt.newPropertyValue(attr2, IAttributeValue.class);

        IProductCmptTypeAssociation association = productCmptType.newProductCmptTypeAssociation();
        association.setTarget(productCmptType.getQualifiedName());
        association.setTargetRoleSingular("association");

        IProductCmptLink link = productCmpt.newLink("association");
        link.setTarget(productCmpt.getQualifiedName());

        IIpsObjectGeneration generation = productCmpt.newGeneration();

        // Verify
        List<IIpsElement> children = Arrays.asList(productCmpt.getChildrenThis());
        assertTrue(children.contains(property1));
        assertTrue(children.contains(property2));
        assertTrue(children.contains(link));
        assertTrue(children.contains(generation));
    }

    @Test
    public void testGetChildrenThis_generationsAreNotAllowed() {
        productCmptType.setChangingOverTime(false);
        productCmpt.setProductCmptType(productCmptType.getQualifiedName());

        IIpsObjectGeneration generation = productCmpt.newGeneration();

        List<IIpsElement> children = Arrays.asList(productCmpt.getChildrenThis());
        assertFalse(children.contains(generation));
    }

    @Test
    public void testDependsOn() {
        IProductCmptTypeAssociation association = productCmptType.newProductCmptTypeAssociation();
        association.setChangingOverTime(false);
        association.setTargetRoleSingular("testAsso");
        association.setTarget(productCmptType.getQualifiedName());
        ProductCmpt targetProductCmpt = newProductCmpt(productCmptType, "referenced");
        IProductCmptLink link = productCmpt.newLink(association);
        link.setTarget(targetProductCmpt.getQualifiedName());

        IDependency[] dependsOn = productCmpt.dependsOn();

        assertEquals(1, dependsOn.length);
        assertEquals(targetProductCmpt.getQualifiedNameType(), dependsOn[0].getTarget());
    }

    @Test
    public void testValidate_ProductCmptTypeIsMissing() {
        IProductCmptType type = newProductCmptType(ipsProject, "Product");
        productCmpt.setProductCmptType(type.getQualifiedName());

        MessageList list = productCmpt.validate(ipsProject);
        assertNull(list.getMessageByCode(IProductCmpt.MSGCODE_MISSINGG_PRODUCT_CMPT_TYPE));

        productCmpt.setProductCmptType("UnknownType");
        list = productCmpt.validate(ipsProject);
        assertNotNull(list.getMessageByCode(IProductCmpt.MSGCODE_MISSINGG_PRODUCT_CMPT_TYPE));

        productCmpt.setProductCmptType("");
        list = productCmpt.validate(ipsProject);
        assertNotNull(list.getMessageByCode(IProductCmpt.MSGCODE_MISSINGG_PRODUCT_CMPT_TYPE));

        // this has once been a bug (NPE in validation of the generation!)
        IFormula ce = ((IProductCmptGeneration)productCmpt.newGeneration()).newFormula();
        ce.setFormulaSignature("SomeFormula");
        ce.setExpression("42");
        list = productCmpt.validate(ipsProject);
        assertNotNull(list.getMessageByCode(IProductCmpt.MSGCODE_MISSINGG_PRODUCT_CMPT_TYPE));
    }

    @Test
    public void testValidate_ProductCmptTypeIsNotAbstract() {
        IProductCmptType type = newProductCmptType(ipsProject, "Product");
        productCmpt.setProductCmptType(type.getQualifiedName());

        MessageList list = productCmpt.validate(ipsProject);
        assertNull(list.getMessageByCode(IProductCmpt.MSGCODE_ABSTRACT_PRODUCT_CMPT_TYPE));

        type.setAbstract(true);
        list = productCmpt.validate(ipsProject);
        assertNotNull(list.getMessageByCode(IProductCmpt.MSGCODE_ABSTRACT_PRODUCT_CMPT_TYPE));
    }

    @Test
    public void testValidate_ProductTemplate_TypeMayBeAbstract() {
        IProductCmptType type = newProductCmptType(ipsProject, "Product");
        ProductCmpt productTemplate = newProductTemplate(ipsProject, "MyTemplate");
        productTemplate.setProductCmptType(type.getQualifiedName());

        MessageList list = productTemplate.validate(ipsProject);
        assertNull(list.getMessageByCode(IProductCmpt.MSGCODE_ABSTRACT_PRODUCT_CMPT_TYPE));

        type.setAbstract(true);
        list = productTemplate.validate(ipsProject);
        assertNull(list.getMessageByCode(IProductCmpt.MSGCODE_ABSTRACT_PRODUCT_CMPT_TYPE));
    }

    @Test
    public void testValidate_ProductCmptTypeIsNotDeprecated() {
        IProductCmptType type = newProductCmptType(ipsProject, "DeprecatedProduct");
        productCmpt.setProductCmptType(type.getQualifiedName());

        var messageList = productCmpt.validate(ipsProject);
        assertThat(messageList, not(hasMessageCode(IProductCmpt.MSGCODE_DEPRECATED_PRODUCT_CMPT_TYPE)));
    }

    @Test
    public void testValidate_ProductCmptTypeIsDeprecated() {
        IProductCmptType type = newProductCmptType(ipsProject, "DeprecatedProduct");
        productCmpt.setProductCmptType(type.getQualifiedName());
        ((IpsObjectPartContainer)type).setDeprecated(true);
        var deprecation = type.getDeprecation();
        deprecation.setSinceVersionString("1.2.3");
        deprecation.setForRemoval(true);
        var locale = IIpsModel.get().getMultiLanguageSupport().getUsedLanguagePackLocale();
        IDescription description = deprecation.newDescription();
        description.setLocale(locale);
        deprecation.setDescriptionText(locale, "Use Foo instead");

        var messageList = productCmpt.validate(ipsProject);

        assertThat(messageList, hasMessageCode(IProductCmpt.MSGCODE_DEPRECATED_PRODUCT_CMPT_TYPE));
        Message message = messageList.getMessageByCode(IProductCmpt.MSGCODE_DEPRECATED_PRODUCT_CMPT_TYPE);
        assertThat(message, hasInvalidObject(productCmpt, IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE));
        assertThat(message, containsText("1.2.3"));
        assertThat(message, containsText("Use Foo instead"));
    }

    @Test
    public void testValidate_ProductCmptTypeIsDeprecated_UseExistingDeprecationDescription() {
        var properties = ipsProject.getProperties();
        var usedLanguagePackLocale = IIpsModel.get().getMultiLanguageSupport().getUsedLanguagePackLocale();
        var defaultLocale = usedLanguagePackLocale.equals(Locale.ITALIAN) ? Locale.CHINESE : Locale.ITALIAN;
        properties.addSupportedLanguage(defaultLocale);
        var otherLocale = usedLanguagePackLocale.equals(Locale.FRENCH) ? Locale.KOREAN : Locale.FRENCH;
        properties.addSupportedLanguage(otherLocale);
        properties.setDefaultLanguage(defaultLocale);
        ipsProject.setProperties(properties);
        IProductCmptType type = newProductCmptType(ipsProject, "DeprecatedProduct");
        productCmpt.setProductCmptType(type.getQualifiedName());
        ((IpsObjectPartContainer)type).setDeprecated(true);
        var deprecation = type.getDeprecation();
        deprecation.setSinceVersionString("1.2.3");
        deprecation.setForRemoval(true);
        deprecation.setDescriptionText(defaultLocale, "Default Description");
        deprecation.setDescriptionText(otherLocale, "Other Description");

        var messageList = productCmpt.validate(ipsProject);

        assertThat(messageList, hasMessageCode(IProductCmpt.MSGCODE_DEPRECATED_PRODUCT_CMPT_TYPE));
        Message message = messageList.getMessageByCode(IProductCmpt.MSGCODE_DEPRECATED_PRODUCT_CMPT_TYPE);
        assertThat(message, hasInvalidObject(productCmpt, IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE));
        assertThat(message, containsText("1.2.3"));
        assertThat(message, containsText("Default Description"));
    }

    @Test
    public void testValidate_TemplateType() {
        IProductCmptType baseType = newProductCmptType(ipsProject, "baseType");
        IProductCmptType subType = newProductCmptType(baseType, "subType");

        IProductCmpt baseComp = newProductCmpt(baseType, "baseComp");
        IProductCmpt subComp = newProductCmpt(subType, "subComp");

        IProductCmpt baseTemplate = newProductTemplate(baseType, "baseTemplate");
        IProductCmpt subTemplate = newProductTemplate(subType, "subTemplate");

        // No template
        MessageList list = baseComp.validate(ipsProject);
        assertThat(list, lacksMessageCode(IProductCmpt.MSGCODE_INVALID_TEMPLATE));
        assertThat(list, lacksMessageCode(IProductCmpt.MSGCODE_INCONSISTENT_TEMPLATE_TYPE));

        // Non existing template
        baseComp.setTemplate("noSuchTemplate");
        list = baseComp.validate(ipsProject);
        assertThat(list, hasMessageCode(IProductCmpt.MSGCODE_INVALID_TEMPLATE));
        assertThat(list, lacksMessageCode(IProductCmpt.MSGCODE_INCONSISTENT_TEMPLATE_TYPE));

        // Consistent type hierarchy
        baseComp.setTemplate(baseTemplate.getQualifiedName());
        list = baseComp.validate(ipsProject);
        assertThat(list, lacksMessageCode(IProductCmpt.MSGCODE_INVALID_TEMPLATE));
        assertThat(list, lacksMessageCode(IProductCmpt.MSGCODE_INCONSISTENT_TEMPLATE_TYPE));

        subComp.setTemplate(baseTemplate.getQualifiedName());
        list = subComp.validate(ipsProject);
        assertThat(list, lacksMessageCode(IProductCmpt.MSGCODE_INVALID_TEMPLATE));
        assertThat(list, lacksMessageCode(IProductCmpt.MSGCODE_INCONSISTENT_TEMPLATE_TYPE));

        subComp.setTemplate(subTemplate.getQualifiedName());
        list = subComp.validate(ipsProject);
        assertThat(list, lacksMessageCode(IProductCmpt.MSGCODE_INVALID_TEMPLATE));
        assertThat(list, lacksMessageCode(IProductCmpt.MSGCODE_INCONSISTENT_TEMPLATE_TYPE));

        // Inconsistent type hierarchy
        baseComp.setTemplate(subTemplate.getQualifiedName());
        list = baseComp.validate(ipsProject);
        assertThat(list, lacksMessageCode(IProductCmpt.MSGCODE_INVALID_TEMPLATE));
        assertThat(list, hasMessageCode(IProductCmpt.MSGCODE_INCONSISTENT_TEMPLATE_TYPE));
    }

    @Test
    public void testValidate_ProductTemplate_TemplateCycle() {
        IProductCmptType type = newProductCmptType(ipsProject, "type");
        ProductCmpt template1 = newProductTemplate(type, "template1");
        ProductCmpt template2 = newProductTemplate(type, "template2");
        ProductCmpt template3 = newProductTemplate(type, "template3");

        // Template has no template hierarchy
        MessageList list = template1.validate(ipsProject);
        assertNull(list.getMessageByCode(IProductCmpt.MSGCODE_TEMPLATE_CYCLE));

        // Template hierarchy without a cycle
        template1.setTemplate(template2.getQualifiedName());
        template2.setTemplate(template3.getQualifiedName());
        list = template1.validate(ipsProject);
        assertNull(list.getMessageByCode(IProductCmpt.MSGCODE_TEMPLATE_CYCLE));

        // Template hierarchy with a cycle
        template3.setTemplate(template1.getQualifiedName());
        list = template1.validate(ipsProject);
        assertNotNull(list.getMessageByCode(IProductCmpt.MSGCODE_TEMPLATE_CYCLE));
    }

    @Test
    public void testValidate_InconsitencyInTypeHierarch() {
        IProductCmptType type = newProductCmptType(ipsProject, "Product");
        ProductCmpt product = newProductCmpt(type, "products.Testproduct");

        MessageList ml = product.validate(type.getIpsProject());
        assertThat(ml, lacksMessageCode(IProductCmpt.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));

        IProductCmptType supertype = newProductCmptType(ipsProject, "SuperProduct");
        IProductCmptType supersupertype = newProductCmptType(ipsProject, "SuperSuperProduct");

        type.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());
        supersupertype.setSupertype("abc");

        ml = type.validate(type.getIpsProject());
        assertThat(ml, hasMessageCode(IType.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));

        ml = product.validate(type.getIpsProject());
        assertThat(ml, hasMessageCode(IProductCmpt.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));

        supersupertype.setSupertype("");
        ml = type.validate(type.getIpsProject());
        assertThat(ml, lacksMessageCode(IType.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));
        ml = product.validate(type.getIpsProject());
        assertThat(ml, lacksMessageCode(IProductCmpt.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));

        supersupertype.setSupertype(type.getQualifiedName());
        ml = type.validate(type.getIpsProject());
        assertThat(ml, hasMessageCode(IType.MSGCODE_CYCLE_IN_TYPE_HIERARCHY));
        ml = product.validate(type.getIpsProject());
        assertThat(ml, hasMessageCode(IProductCmpt.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));

        type.setSupertype("Unkown");
        ml = type.validate(type.getIpsProject());
        assertThat(ml, hasMessageCode(IType.MSGCODE_SUPERTYPE_NOT_FOUND));
        ml = product.validate(type.getIpsProject());
        assertThat(ml, hasMessageCode(IProductCmpt.MSGCODE_INCONSISTENT_TYPE_HIERARCHY));
    }

    @Test
    public void testValidate_NameDoesNotComplyToNamingStrategy() {
        IProductCmptType type = newProductCmptType(ipsProject, "ProductType");
        ProductCmpt product = newProductCmpt(type, "Product");
        IIpsProjectProperties projectProperties = ipsProject.getProperties();
        projectProperties.setProductCmptNamingStrategy(new DateBasedProductCmptNamingStrategy());
        ipsProject.setProperties(projectProperties);

        MessageList validationMessages = product.validate(ipsProject);
        assertNotNull(validationMessages.getMessageByCode(IProductCmptNamingStrategy.MSGCODE_ILLEGAL_VERSION_ID));
    }

    @Test
    public void testValidate_RuntimeIdDoesNotComplyToNamingStrategy() {
        IProductCmptType type = newProductCmptType(ipsProject, "ProductType");
        ProductCmpt product = newProductCmpt(type, "Product");
        product.setRuntimeId("");

        MessageList validationMessages = product.validate(ipsProject);
        assertNotNull(
                validationMessages.getMessageByCode(IProductCmptNamingStrategy.MSGCODE_INVALID_RUNTIME_ID_FORMAT));
    }

    @Test
    public void testValidate_DuplicateRuntimeIds() {
        IProductCmptType type = newProductCmptType(ipsProject, "ProductType");
        ProductCmpt product1 = newProductCmpt(type, "Product1");
        ProductCmpt product2 = newProductCmpt(type, "Product2");
        product1.setRuntimeId("Product");
        product2.setRuntimeId("Product");

        MessageList validationMessages = product1.validate(ipsProject);
        assertNotNull(validationMessages.getMessageByCode(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION));
    }

    @Test
    public void testValidate_DuplicateRuntimeIdIgnoresTemplate() {
        IProductCmptType type = newProductCmptType(ipsProject, "ProductType");
        ProductCmpt product = newProductCmpt(type, "Product1");
        ProductCmpt template = newProductTemplate(type, "Product2");
        product.setRuntimeId("Product");
        template.setRuntimeId("Product");

        MessageList validationMessages = product.validate(ipsProject);
        MessageList validationMessages2 = template.validate(ipsProject);

        assertNull(validationMessages.getMessageByCode(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION));
        assertNull(validationMessages2.getMessageByCode(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION));

    }

    @Test
    public void testValidate_ReferencedProductComponentsNotValidOnValidFromDate_illegalDates()
            throws IpsException, Exception {
        setUpLinkForDateValidityCheck();
        productCmpt.setValidFrom(DateUtil.parseIsoDateStringToGregorianCalendar("2007-01-01"));
        target.setValidFrom(DateUtil.parseIsoDateStringToGregorianCalendar("2008-01-01"));

        assertThat(productCmpt.validate(ipsProject),
                hasMessageCode(IProductCmptLinkContainer.MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE));
    }

    private void setUpLinkForDateValidityCheck() {
        IProductCmptType type = newProductCmptType(ipsProject, "Product");
        productCmpt = newProductCmpt(type, "Product1");
        target = newProductCmpt(type, "TargetProduct");

        IProductCmptTypeAssociation association = type.newProductCmptTypeAssociation();
        association.setAssociationType(AssociationType.AGGREGATION);
        association.setTarget(type.getQualifiedName());
        association.setChangingOverTime(false);

        link = productCmpt.newLink(association);

        link.setTarget(target.getQualifiedName());
        link.setMinCardinality(0);
        link.setMaxCardinality(1);
    }

    @Test
    public void testValidate_ReferencedProductComponentsNotValidOnValidFromDate_projectSetting()
            throws IpsException, Exception {
        setUpLinkForDateValidityCheck();
        productCmpt.setValidFrom(DateUtil.parseIsoDateStringToGregorianCalendar("2007-01-01"));
        target.setValidFrom(DateUtil.parseIsoDateStringToGregorianCalendar("2008-01-01"));

        assertThat(productCmpt.validate(ipsProject),
                hasMessageCode(IProductCmptLinkContainer.MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE));

        // assert that there is no validation error if the optional constraint
        // "referencedProductComponentsAreValidOnThisGenerationsValidFromDate" is turned off
        IIpsProjectProperties oldProps = ipsProject.getProperties();
        IIpsProjectProperties newProps = new IpsProjectProperties(ipsProject, (IpsProjectProperties)oldProps);
        newProps.setReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled(false);
        ipsProject.setProperties(newProps);

        assertThat(productCmpt.validate(ipsProject),
                lacksMessageCode(IProductCmptLinkContainer.MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE));

        // cleanup
        ipsProject.setProperties(oldProps);
    }

    @Test
    public void testValidate_ReferencedProductComponentsNotValidOnValidFromDate_legalDates()
            throws IpsException, Exception {
        setUpLinkForDateValidityCheck();
        productCmpt.setValidFrom(DateUtil.parseIsoDateStringToGregorianCalendar("2007-01-01"));
        target.setValidFrom(DateUtil.parseIsoDateStringToGregorianCalendar("2007-01-01"));

        assertThat(productCmpt.validate(ipsProject),
                lacksMessageCode(IProductCmptLinkContainer.MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE));

        target.setValidFrom(DateUtil.parseIsoDateStringToGregorianCalendar("2006-01-01"));

        assertThat(productCmpt.validate(ipsProject),
                lacksMessageCode(IProductCmptLinkContainer.MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE));
    }

    @Test
    public void testValidate_FixDifferences() {
        IProductCmptType type = newProductCmptType(ipsProject, "Product");
        IProductCmptTypeAttribute attribute = type.newProductCmptTypeAttribute("attribtue");
        attribute.setChangingOverTime(true);
        ProductCmpt product = newProductCmpt(type, "products.Testproduct");
        assertThat(product.validate(type.getIpsProject()), hasMessageCode(IProductCmpt.MSGCODE_DIFFERENCES_TO_MODEL));

        product.fixAllDifferencesToModel(ipsProject);
        assertNull(product.validate(type.getIpsProject()).getMessageByCode(IProductCmpt.MSGCODE_DIFFERENCES_TO_MODEL));

        attribute.setChangingOverTime(false);
        assertThat(product.validate(type.getIpsProject()), hasMessageCode(IProductCmpt.MSGCODE_DIFFERENCES_TO_MODEL));
    }

    @Test
    public void testValidate_FixDifferences_InvalidGenerations() {
        IProductCmptType type = newProductCmptType(ipsProject, "Product");
        type.setChangingOverTime(true);
        ProductCmpt product = newProductCmpt(type, "products.Testproduct");
        product.newGeneration(new GregorianCalendar(2015, 0, 1));
        product.newGeneration(new GregorianCalendar(2015, 1, 1));
        product.newGeneration(new GregorianCalendar(2016, 7, 28));

        MessageList ml = product.validate(type.getIpsProject());
        assertThat(ml, lacksMessageCode(IProductCmpt.MSGCODE_DIFFERENCES_TO_MODEL));

        type.setChangingOverTime(false);
        ml = product.validate(type.getIpsProject());
        assertThat(ml, hasMessageCode(IProductCmpt.MSGCODE_DIFFERENCES_TO_MODEL));
    }

    @Test
    public void testValidate_FixDifferences_AttributeWithMissingConfigElement() throws Exception {
        IProductCmpt product = newProductCmpt(productCmptType, "EmptyTestProduct");
        assertThat(product.validate(ipsProject), isEmpty());

        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setValueSetConfiguredByProduct(true);
        attribute.setName("test");
        assertThat(product.validate(ipsProject), hasMessageCode(IProductCmpt.MSGCODE_DIFFERENCES_TO_MODEL));
    }

    @Test
    public void testValidate_DuplicateKindIDVersionID_duplicateInDifferentPackage() throws Exception {
        newProductCmpt(ipsProject, "somepkg.Duplicate 2020");
        IProductCmpt product = newProductCmpt(productCmptType, "otherpkg.Duplicate 2020");

        assertThat(product.validate(ipsProject), hasMessageCode(IProductCmpt.MSGCODE_DUPLICATE_KINDID_VERSIONID));
    }

    @Test
    public void testValidate_DuplicateKindIDVersionID_duplicateInDifferentPackage_SeverityNone() throws Exception {
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setDuplicateProductComponentSeverity(Severity.NONE);
        ipsProject.setProperties(properties);
        newProductCmpt(ipsProject, "somepkg.Duplicate 2020");
        IProductCmpt product = newProductCmpt(productCmptType, "otherpkg.Duplicate 2020");

        assertThat(product.validate(ipsProject), lacksMessageCode(IProductCmpt.MSGCODE_DUPLICATE_KINDID_VERSIONID));
    }

    @Test
    public void testValidate_DuplicateKindIDVersionID_uniqueVersionID() throws Exception {
        newProductCmpt(ipsProject, "Duplicate 2020");
        IProductCmpt product = newProductCmpt(productCmptType, "Duplicate 2021");

        assertThat(product.validate(ipsProject), lacksMessageCode(IProductCmpt.MSGCODE_DUPLICATE_KINDID_VERSIONID));
    }

    @Test
    public void testValidate_DuplicateKindIDVersionID_uniqueKindID() throws Exception {
        newProductCmpt(ipsProject, "Duplicate 2020");
        IProductCmpt product = newProductCmpt(productCmptType, "NoDuplicate 2020");

        assertThat(product.validate(ipsProject), lacksMessageCode(IProductCmpt.MSGCODE_DUPLICATE_KINDID_VERSIONID));
    }

    @Test
    // Suppressed "unused" warning for improved readability
    @SuppressWarnings("unused")
    public void testFindPropertyValues() {
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "MyProduct");

        // Create some properties on the product component
        IProductCmptTypeAttribute productAttribute1 = productCmptType.newProductCmptTypeAttribute("productAttribute1");
        IProductCmptTypeAttribute productAttribute2 = productCmptType.newProductCmptTypeAttribute("productAttribute2");
        IProductCmptTypeAttribute productAttribute3 = productCmptType.newProductCmptTypeAttribute("productAttribute3");

        // Create some properties on the generation
        IProductCmptTypeAttribute genAttribute1 = productCmptType.newProductCmptTypeAttribute("g1");
        IProductCmptTypeAttribute genAttribute2 = productCmptType.newProductCmptTypeAttribute("g2");

        // Create a category and assign some properties
        IProductCmptCategory category = productCmptType.newCategory("myCategory");
        productAttribute1.setCategory(category.getName());
        productAttribute3.setCategory(category.getName());
        genAttribute2.setCategory(category.getName());

        // Create two generations
        GregorianCalendar validFrom1 = createValidFromDate(1);
        GregorianCalendar validFrom2 = createValidFromDate(2);
        IProductCmptGeneration generation1 = (IProductCmptGeneration)productCmpt.newGeneration(validFrom1);
        IProductCmptGeneration generation2 = (IProductCmptGeneration)productCmpt.newGeneration(validFrom2);

        // Create the corresponding property values
        IPropertyValue productValue1 = productCmpt.newPropertyValue(productAttribute1, IAttributeValue.class);
        productCmpt.newPropertyValue(productAttribute2, IAttributeValue.class);
        IPropertyValue productValue3 = productCmpt.newPropertyValue(productAttribute3, IAttributeValue.class);
        generation1.newAttributeValue(genAttribute1);
        IPropertyValue gen1Value2 = generation1.newAttributeValue(genAttribute2);
        generation2.newAttributeValue(genAttribute1);
        IPropertyValue gen2Value2 = generation2.newAttributeValue(genAttribute2);

        // Check for generation 1
        List<IPropertyValue> propertyValuesGen1 = productCmpt.findPropertyValues(category, validFrom1, ipsProject);
        assertEquals(productValue1, propertyValuesGen1.get(0));
        assertEquals(productValue3, propertyValuesGen1.get(1));
        assertEquals(gen1Value2, propertyValuesGen1.get(2));
        assertEquals(3, propertyValuesGen1.size());

        // Check for generation 2
        List<IPropertyValue> propertyValuesGen2 = productCmpt.findPropertyValues(category, validFrom2, ipsProject);
        assertEquals(productValue1, propertyValuesGen2.get(0));
        assertEquals(productValue3, propertyValuesGen2.get(1));
        assertEquals(gen2Value2, propertyValuesGen2.get(2));
        assertEquals(3, propertyValuesGen2.size());
    }

    /**
     * <strong>Scenario:</strong><br>
     * The {@link IPropertyValue property values} are requested but the {@link IProductCmptType} the
     * {@link IProductCmpt} is an instance of cannot be found.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * An empty list should be returned but no exception may be thrown.
     */
    @Test
    public void testFindPropertyValues_ProductCmptTypeCannotBeFound() {
        GregorianCalendar validFrom = new GregorianCalendar(2011, 12, 12);
        productCmpt.newGeneration(validFrom);

        assertTrue(productCmpt.findPropertyValues(null, validFrom, ipsProject).isEmpty());
    }

    /**
     *
     * <strong>Scenario:</strong><br>
     * The {@link IPropertyValue property values} are requested but no
     * {@link IProductCmptGeneration} exists for the indicated effective date.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Only the {@link IPropertyValue property values} belonging to the {@link IProductCmpt} itself
     * should be returned.
     */
    @Test
    public void testFindPropertyValues_NoGenerationWithTheIndicatedEffectiveDate_ForProductAttribute() {
        IProductCmptCategory category = productCmptType.newCategory("myCategory");
        IProductCmptTypeAttribute productAttribute = productCmptType.newProductCmptTypeAttribute("productAttribute");
        productAttribute.setCategory(category.getName());

        IProductCmpt productCmpt = newProductCmpt(productCmptType, "MyProduct");
        IPropertyValue productValue = productCmpt.newPropertyValue(productAttribute, IAttributeValue.class);

        List<IPropertyValue> propertyValues = productCmpt.findPropertyValues(category,
                new GregorianCalendar(2070, 1, 1), ipsProject);
        assertEquals(productValue, propertyValues.get(0));
        assertEquals(1, propertyValues.size());
    }

    /**
     *
     * <strong>Scenario:</strong><br>
     * The {@link IPropertyValue property values} are requested but no
     * {@link IProductCmptGeneration} exists for the indicated effective date.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Only the {@link IPropertyValue property values} belonging to the {@link IProductCmpt} itself
     * should be returned.
     */
    @Test
    public void testFindPropertyValues_NoGenerationWithTheIndicatedEffectiveDateForPolicyAttribute() {
        IProductCmptCategory category = productCmptType.newCategory("myCategory");
        IPolicyCmptTypeAttribute policyAttribute = policyCmptType.newPolicyCmptTypeAttribute("policyAttribute");
        policyAttribute.setValueSetConfiguredByProduct(true);
        policyAttribute.setAttributeType(AttributeType.CHANGEABLE);
        policyAttribute.setCategory(category.getName());
        productCmptType.setPolicyCmptType("PolType");

        IProductCmpt productCmpt = newProductCmpt(productCmptType, "MyProduct");
        IPropertyValue productValue = productCmpt.newPropertyValue(policyAttribute, IConfiguredDefault.class);

        List<IPropertyValue> propertyValues = productCmpt.findPropertyValues(category,
                new GregorianCalendar(2070, 1, 1), ipsProject);
        assertEquals(productValue, propertyValues.get(0));
        assertEquals(1, propertyValues.size());
    }

    /**
     * <strong>Scenario:</strong><br>
     * The {@link IPropertyValue property values} are requested but no specific
     * {@link IProductCmptCategory} is given.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The {@link IPropertyValue property values} for all {@link IProductCmptCategory categories}
     * should be returned.
     */
    @Test
    public void testFindPropertyValues_NoCategoryGiven() {
        IProductCmptCategory category1 = productCmptType.newCategory("category1");
        IProductCmptCategory category2 = productCmptType.newCategory("category2");

        IProductCmptTypeAttribute productAttribute1 = productCmptType.newProductCmptTypeAttribute("productAttribute1");
        productAttribute1.setCategory(category1.getName());
        IProductCmptTypeAttribute productAttribute2 = productCmptType.newProductCmptTypeAttribute("productAttribute2");
        productAttribute2.setCategory(category2.getName());

        IProductCmpt productCmpt = newProductCmpt(productCmptType, "MyProduct");
        GregorianCalendar validFrom = createValidFromDate(1);
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration(validFrom);
        IPropertyValue productValue1 = generation.newPropertyValue(productAttribute1, IAttributeValue.class);
        IPropertyValue productValue2 = generation.newPropertyValue(productAttribute2, IAttributeValue.class);

        List<IPropertyValue> propertyValues = productCmpt.findPropertyValues(null, validFrom, ipsProject);
        assertEquals(productValue1, propertyValues.get(0));
        assertEquals(productValue2, propertyValues.get(1));
        assertEquals(2, propertyValues.size());
    }

    @Test
    public void testGetKindId() {
        IProductCmptKind kind = productCmpt.getKindId();
        assertEquals("TestProduct", kind.getName());
        assertEquals("TestProduct", kind.getRuntimeId());

        IProductCmptNamingStrategy strategy = new DateBasedProductCmptNamingStrategy(" ", "yyyy-MM", false);
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setProductCmptNamingStrategy(strategy);
        ipsProject.setProperties(props);
        productCmpt = newProductCmpt(ipsProject, "motor.MotorProduct 2005-10");
        kind = productCmpt.getKindId();
        assertEquals("MotorProduct", kind.getName());
        assertEquals("MotorProduct", kind.getRuntimeId());
    }

    @Test
    public void testGetKindIdWithIllegalName() {
        IProductCmptNamingStrategy strategy = new DateBasedProductCmptNamingStrategy(" ", "yyyy-MM", false);
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setProductCmptNamingStrategy(strategy);
        ipsProject.setProperties(props);

        productCmpt = newProductCmpt(ipsProject, "motor.MotorProduct 2011-11");
        productCmpt.setName("motor.MotorProduct");
        assertNull(productCmpt.getKindId());
    }

    @Test
    public void testSetProductCmptType() {
        productCmpt.setProductCmptType("newType");
        assertEquals("newType", productCmpt.getProductCmptType());
        assertTrue(srcFile.isDirty());
    }

    @Test
    public void testInitFromXml_LegazyConfigElement() {
        productCmpt.initFromXml(getTestDocument("_LegazyConfigElement").getDocumentElement());
        IConfiguredValueSet configuredValueSet = productCmpt.getPropertyValue(POLICY_ATTRIBUTE_NAME,
                IConfiguredValueSet.class);
        IConfiguredDefault configuredDefault = productCmpt.getPropertyValue(POLICY_ATTRIBUTE_NAME,
                IConfiguredDefault.class);

        // the ID should NOT be the old ID of the valueSet to avoid ID collision
        assertThat(configuredValueSet.getId(), is(not("1")));
        assertThat(configuredValueSet.getValueSet(), instanceOf(RangeValueSet.class));
        assertThat(configuredValueSet.getPolicyCmptTypeAttribute(), is(POLICY_ATTRIBUTE_NAME));
        assertThat(configuredDefault.getValue(), is("10"));
        assertThat(configuredDefault.getPolicyCmptTypeAttribute(), is(POLICY_ATTRIBUTE_NAME));
    }

    @Test
    public void testInitFromXml() {
        productCmpt.initFromXml(getTestDocument().getDocumentElement());
        assertEquals("MotorProduct", productCmpt.getProductCmptType());
        assertEquals("MotorProductId", productCmpt.getRuntimeId());
        assertEquals("MyLittleTemplate", productCmpt.getTemplate());
        assertEquals(2, productCmpt.getNumOfGenerations());
        IProductCmptGeneration gen = (IProductCmptGeneration)productCmpt.getGenerationsOrderedByValidDate()[0];
        assertEquals(1, gen.getNumOfConfigElements());
        IConfiguredDefault ce = gen.getConfiguredDefaults()[0];
        assertEquals("1.5", ce.getValue());
        IConfiguredValueSet configuredValueSet = productCmpt.getPropertyValue("myAttribute", IConfiguredValueSet.class);
        IValueSet valueSet = configuredValueSet.getValueSet();
        assertThat(valueSet, is(instanceOf(IRangeValueSet.class)));
        assertThat(((IRangeValueSet)valueSet).getLowerBound(), is("22"));
        assertThat(((IRangeValueSet)valueSet).getUpperBound(), is("33"));
        assertThat(((IRangeValueSet)valueSet).getStep(), is("4"));

        assertEquals(2, productCmpt.getNumOfLinks());
        assertEquals("staticCoverage", productCmpt.getLinksAsList().get(0).getAssociation());
        assertEquals("staticIDontKnow", productCmpt.getLinksAsList().get(1).getAssociation());
    }

    @Test
    public void testReInitFromXml() {
        productCmpt.initFromXml(getTestDocument().getDocumentElement());
        assertEquals("MotorProduct", productCmpt.getProductCmptType());
        assertEquals("MotorProductId", productCmpt.getRuntimeId());
        assertEquals("MyLittleTemplate", productCmpt.getTemplate());
        assertEquals(2, productCmpt.getNumOfGenerations());
        IProductCmptGeneration gen = (IProductCmptGeneration)productCmpt.getGenerationsOrderedByValidDate()[0];
        assertEquals(1, gen.getNumOfConfigElements());
        IConfiguredDefault ce = gen.getConfiguredDefaults()[0];
        assertEquals("1.5", ce.getValue());
        IConfiguredValueSet configuredValueSet = productCmpt.getPropertyValue("myAttribute", IConfiguredValueSet.class);
        IValueSet valueSet = configuredValueSet.getValueSet();
        assertThat(valueSet, is(instanceOf(IRangeValueSet.class)));
        assertThat(((IRangeValueSet)valueSet).getLowerBound(), is("22"));
        assertThat(((IRangeValueSet)valueSet).getUpperBound(), is("33"));
        assertThat(((IRangeValueSet)valueSet).getStep(), is("4"));

        assertEquals(2, productCmpt.getNumOfLinks());
        assertEquals("staticCoverage", productCmpt.getLinksAsList().get(0).getAssociation());
        assertEquals("staticIDontKnow", productCmpt.getLinksAsList().get(1).getAssociation());

        productCmpt.initFromXml(getTestDocument().getDocumentElement());
        assertThat(productCmpt.getPropertyValue("myAttribute", IConfiguredValueSet.class),
                is(sameInstance(configuredValueSet)));
        var newGen = (IProductCmptGeneration)productCmpt.getGenerationsOrderedByValidDate()[0];
        assertThat(newGen, is(sameInstance(gen)));
        assertThat(newGen.getConfiguredDefaults()[0], is(sameInstance(ce)));
    }

    @Test
    public void testToXml() {
        productCmpt.setProductCmptType("MotorProduct");
        productCmpt.setRuntimeId("MotorProductId");
        productCmpt.setTemplate("MeinTemplate");
        IProductCmptGeneration gen1 = (IProductCmptGeneration)productCmpt.newGeneration();
        gen1.setValidFrom(new GregorianCalendar(1999, 0, 1));
        IConfiguredDefault ce1 = gen1.newPropertyValue(policyAttr, IConfiguredDefault.class);
        ce1.setValue("0.15");
        IIpsObjectGeneration gen2 = productCmpt.newGeneration();
        gen2.setValidFrom(new GregorianCalendar(2000, 0, 1));

        Element element = productCmpt.toXml(newDocument());
        ProductCmpt copy = newProductCmpt(ipsProject, "TestProductCopy");
        copy.initFromXml(element);
        assertEquals("MotorProduct", copy.getProductCmptType());
        assertEquals("MotorProductId", copy.getRuntimeId());
        assertEquals("MeinTemplate", productCmpt.getTemplate());
        assertEquals(2, copy.getNumOfGenerations());
        IProductCmptGeneration genCopy = (IProductCmptGeneration)copy.getGenerationsOrderedByValidDate()[0];
        assertEquals(1, genCopy.getConfiguredDefaults().length);
        assertEquals("0.15", genCopy.getConfiguredDefaults()[0].getValue());
    }

    @Test
    public void testInitFromXml_AttributeValues() {
        productCmpt.initFromXml(getTestDocument().getDocumentElement());
        IAttributeValue attributeValue = productCmpt.getAttributeValue("bezeichnung");
        assertNotNull(attributeValue);
        assertEquals("testtesttest", attributeValue.getPropertyValue());
    }

    @Test
    public void testToXml_AttributeValues() {
        attr2.setChangingOverTime(false);
        IPropertyValue propertyValue = productCmpt.newPropertyValue(attr2, IAttributeValue.class);
        Element xml = productCmpt.toXml(newDocument());

        ProductCmpt copy = newProductCmpt(ipsProject, "TestProductCopy");
        copy.initFromXml(xml);
        IAttributeValue copyAttributeValue = copy.getAttributeValue(attr2.getName());
        assertNotNull(copyAttributeValue);
        assertEquals(propertyValue.getName(), copyAttributeValue.getName());
    }

    @Test
    public void testInitFromXml_TableContentUsage() {
        productCmpt.initFromXml(getTestDocument().getDocumentElement());
        assertEquals(1, productCmpt.getTableContentUsages().length);
        assertNotNull(productCmpt.getTableContentUsage("staticTable"));
    }

    @Test
    public void testToXml_TableContentUsage() {
        productCmpt.newPropertyValues(new TableStructureUsage(mock(IProductCmptType.class), "tc"));
        Element xml = productCmpt.toXml(newDocument());

        ProductCmpt copy = newProductCmpt(ipsProject, "TestProductCopy");
        copy.initFromXml(xml);
        assertEquals(1, productCmpt.getTableContentUsages().length);
    }

    @Test
    public void testInitFromXml_Formula() {
        productCmpt.initFromXml(getTestDocument().getDocumentElement());
        assertEquals(1, productCmpt.getFormulas().length);
        assertNotNull(productCmpt.getFormula("PremiumCalculation"));
    }

    @Test
    public void testToXml_Formula() {
        try (TestIpsModelExtensions testIpsModelExtensions = TestIpsModelExtensions.get()) {
            IFormulaCompiler formulaCompiler = mock(IFormulaCompiler.class);
            testIpsModelExtensions.setFormulaCompiler(formulaCompiler);
            IFormula newFormula = productCmpt
                    .newPropertyValue(new ProductCmptTypeMethod(mock(IProductCmptType.class), "Id"), IFormula.class);
            newFormula.setExpression("anyExpression");
            Document document = newDocument();
            Element xml = productCmpt.toXml(document);
            verify(formulaCompiler).compileFormulas(productCmpt, document, xml);

            ProductCmpt copy = newProductCmpt(ipsProject, "TestProductCopy");
            copy.initFromXml(xml);
            assertEquals(1, productCmpt.getFormulas().length);
        }
    }

    @Test
    public void testToXml_ImplClass() {
        try (TestIpsModelExtensions testIpsModelExtensions = TestIpsModelExtensions.get()) {
            testIpsModelExtensions.setImplementationClassProvider(p -> "my.Impl");
            Document document = newDocument();
            productCmpt.setProductCmptType("foo.Bar");
            Element xml = productCmpt.toXml(document);

            assertThat(xml.getAttribute(ProductComponent.PROPERTY_IMPLEMENTATION_CLASS), is("my.Impl"));
        }
    }

    @Test
    public void testToXml_Links() {
        attr2.setChangingOverTime(false);
        IProductCmptLink newLink = productCmpt.newLink("newLink");
        newLink.setTarget("target");
        newLink.setTargetRuntimeId("targetID");
        Element xml = productCmpt.toXml(newDocument());

        ProductCmpt copy = newProductCmpt(ipsProject, "TestProductCopy");
        copy.initFromXml(xml);
        List<IProductCmptLink> linksCopy = copy.getLinksAsList("newLink");
        assertNotNull(linksCopy);
        assertEquals(1, linksCopy.size());
        assertEquals(newLink.getTarget(), linksCopy.get(0).getTarget());
        assertEquals(newLink.getTargetRuntimeId(), linksCopy.get(0).getTargetRuntimeId());
    }

    @Test
    public void testInitFromXml_ValidationRuleConfig() {
        productCmpt.initFromXml(getTestDocument().getDocumentElement());
        assertEquals(2, productCmpt.getNumOfValidationRules());
        IValidationRuleConfig activeValidationRuleConfig = productCmpt.getValidationRuleConfig("activeRule");
        assertNotNull(activeValidationRuleConfig);
        assertTrue(activeValidationRuleConfig.isActive());
        IValidationRuleConfig inactiveValidationRuleConfig = productCmpt.getValidationRuleConfig("inactiveRule");
        assertNotNull(inactiveValidationRuleConfig);
        assertFalse(inactiveValidationRuleConfig.isActive());
    }

    @Test
    public void testToXml_ValidationRuleConfig() {
        IValidationRule validationRule = policyCmptType.newRule();
        validationRule.setName("MyRule");
        IValidationRuleConfig validationRuleConfig = productCmpt.newValidationRuleConfig(validationRule);
        validationRuleConfig.setValidationRuleName("MyRule");
        Element xml = productCmpt.toXml(newDocument());

        ProductCmpt copy = newProductCmpt(ipsProject, "TestProductCopy");
        copy.initFromXml(xml);
        assertEquals(1, productCmpt.getNumOfValidationRules());
    }

    @Test
    public void testContainsGenerationFormula() {
        assertFalse(productCmpt.containsGenerationFormula());
        IProductCmptGeneration gen1 = (IProductCmptGeneration)productCmpt
                .newGeneration(new GregorianCalendar(2000, 1, 1));
        IProductCmptGeneration gen2 = (IProductCmptGeneration)productCmpt
                .newGeneration(new GregorianCalendar(2010, 1, 1));
        gen1.newFormula();
        assertTrue(productCmpt.containsGenerationFormula());

        for (IFormula formula : gen1.getFormulas()) {
            formula.delete();
        }
        assertFalse(productCmpt.containsGenerationFormula());

        gen2.newFormula();
        assertTrue(productCmpt.containsGenerationFormula());
    }

    @Test
    public void testContainsDifferenceToModel() {
        PolicyCmptType testType = newPolicyAndProductCmptType(ipsProject, "TestPolicyType", "TestProductType");
        IPolicyCmptTypeAttribute a1 = testType.newPolicyCmptTypeAttribute();
        a1.setName("A1");
        a1.setValueSetConfiguredByProduct(true);

        IProductCmptType productCmptType = testType.findProductCmptType(ipsProject);
        IProductCmpt product = newProductCmpt(productCmptType, "TestProduct");
        IProductCmptGeneration gen = product.getProductCmptGeneration(0);
        gen.newPropertyValue(a1, IConfiguredDefault.class);

        IPolicyCmptTypeAttribute a2 = testType.newPolicyCmptTypeAttribute();
        a2.setName("A2");
        a2.setValueSetConfiguredByProduct(true);

        IProductCmpt product2 = newProductCmpt(productCmptType, "TestProduct2");
        gen = product2.getProductCmptGeneration(0);
        gen.newPropertyValue(a1, IConfiguredDefault.class);
        gen.newPropertyValue(a2, IConfiguredDefault.class);

        assertEquals(true, product.containsDifferenceToModel(ipsProject));
        assertEquals(false, product2.containsDifferenceToModel(ipsProject));
        testType.getPolicyCmptTypeAttribute("A2").delete();
        assertEquals(false, product.containsDifferenceToModel(ipsProject));
        assertEquals(true, product2.containsDifferenceToModel(ipsProject));
    }

    @Test
    public void testContainsDifferencesToModel_productCmptTypeAttribute() {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        IProductCmptTypeAttribute newAttribute = newProductCmptType.newProductCmptTypeAttribute("testAttr");
        newAttribute.setChangingOverTime(false);

        ProductCmpt newProductCmpt = newProductCmpt(newProductCmptType, "Cmpt1");
        assertTrue(newProductCmpt.containsDifferenceToModel(ipsProject));

        IPropertyValue wrongAttributeValue = newProductCmpt.getProductCmptGeneration(0).newPropertyValue(newAttribute,
                IAttributeValue.class);
        assertTrue(newProductCmpt.containsDifferenceToModel(ipsProject));

        IPropertyValue correctAttributeValue = newProductCmpt.newPropertyValue(newAttribute, IAttributeValue.class);
        assertTrue(newProductCmpt.containsDifferenceToModel(ipsProject));

        wrongAttributeValue.delete();
        assertFalse(newProductCmpt.containsDifferenceToModel(ipsProject));

        newAttribute.setChangingOverTime(true);
        assertTrue(newProductCmpt.containsDifferenceToModel(ipsProject));

        newAttribute.setChangingOverTime(false);
        assertFalse(newProductCmpt.containsDifferenceToModel(ipsProject));

        newAttribute.delete();
        assertTrue(newProductCmpt.containsDifferenceToModel(ipsProject));

        correctAttributeValue.delete();
        assertFalse(newProductCmpt.containsDifferenceToModel(ipsProject));
    }

    @Test
    public void testFixDifferencesToModel_productCmptTypeAttribute() {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        IProductCmptTypeAttribute newAttribute = newProductCmptType.newProductCmptTypeAttribute("testAttr");
        newAttribute.setChangingOverTime(false);

        ProductCmpt newProductCmpt = newProductCmpt(newProductCmptType, "Cmpt1");

        assertTrue(newProductCmpt.containsDifferenceToModel(ipsProject));
        IAttributeValue attributeValue = newProductCmpt.getAttributeValue(newAttribute.getName());
        assertNull(attributeValue);

        newProductCmpt.fixAllDifferencesToModel(ipsProject);
        assertFalse(newProductCmpt.containsDifferenceToModel(ipsProject));
        attributeValue = newProductCmpt.getAttributeValue(newAttribute.getName());
        assertNotNull(attributeValue);

        newAttribute.setChangingOverTime(true);
        newProductCmpt.fixAllDifferencesToModel(ipsProject);
        assertTrue(attributeValue.isDeleted());
        IAttributeValue attributeValue1 = newProductCmpt.getAttributeValue(newAttribute.getName());
        assertNull(attributeValue1);

        IAttributeValue attributeValue2 = newProductCmpt.getFirstGeneration().getAttributeValue(newAttribute.getName());
        assertNotNull(attributeValue2);

        assertNotSame(attributeValue.getId().intern(), attributeValue2.getId().intern());

        newAttribute.setChangingOverTime(false);
        newProductCmpt.fixAllDifferencesToModel(ipsProject);
        IAttributeValue attributeValue3 = newProductCmpt.getAttributeValue(newAttribute.getName());
        assertNotNull(attributeValue3);

        assertNotSame(attributeValue.getId().intern(), attributeValue2.getId().intern());
        assertNotSame(attributeValue.getId().intern(), attributeValue3.getId().intern());

        newAttribute.delete();
        newProductCmpt.fixAllDifferencesToModel(ipsProject);
        assertTrue(attributeValue3.isDeleted());
        IAttributeValue attributeValue4 = newProductCmpt.getAttributeValue(newAttribute.getName());
        assertNull(attributeValue4);

        assertNotSame(attributeValue.getId().intern(), attributeValue2.getId().intern());
        assertNotSame(attributeValue.getId().intern(), attributeValue3.getId().intern());
    }

    @Test
    public void testComputeDeltaToModel_AttributeValues() {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        IProductCmptTypeAttribute newAttribute = newProductCmptType.newProductCmptTypeAttribute("testAttr");
        newAttribute.setChangingOverTime(false);

        ProductCmpt newProductCmpt = newProductCmpt(newProductCmptType, "Cmpt1");

        IPropertyValueContainerToTypeDelta computeDeltaToModel = newProductCmpt.computeDeltaToModel(ipsProject);

        assertEquals(newProductCmpt, computeDeltaToModel.getPropertyValueContainer());
        IDeltaEntry[] entries = computeDeltaToModel.getEntries();
        assertEquals(1, entries.length);
        assertEquals(DeltaType.MISSING_PROPERTY_VALUE, entries[0].getDeltaType());
        assertEquals(newAttribute.getName(), ((IDeltaEntryForProperty)entries[0]).getPropertyName());

        newAttribute.setChangingOverTime(true);
        computeDeltaToModel = newProductCmpt.computeDeltaToModel(ipsProject);
        entries = computeDeltaToModel.getEntries();
        assertEquals(0, entries.length);

        newAttribute.setChangingOverTime(false);
        newProductCmpt.fixAllDifferencesToModel(ipsProject);

        newAttribute.setChangingOverTime(true);
        computeDeltaToModel = newProductCmpt.computeDeltaToModel(ipsProject);
        entries = computeDeltaToModel.getEntries();
        assertEquals(1, entries.length);
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, entries[0].getDeltaType());
        assertEquals(newAttribute.getName(), ((IDeltaEntryForProperty)entries[0]).getPropertyName());
    }

    @Test
    public void testContainsDifferencesToModel_productCmptTypeTableContent() {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        ITableStructureUsage tableStructureUsage = newProductCmptType.newTableStructureUsage();
        tableStructureUsage.setRoleName("TSU");
        tableStructureUsage.setChangingOverTime(false);

        ProductCmpt newProductCmpt = newProductCmpt(newProductCmptType, "Cmpt1");
        assertTrue(newProductCmpt.containsDifferenceToModel(ipsProject));

        newProductCmpt.newPropertyValues(tableStructureUsage);
        assertFalse(newProductCmpt.containsDifferenceToModel(ipsProject));
    }

    @Test
    public void testFixDifferencesToModel_productCmptTypeTableContent() {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        ITableStructureUsage tableStructureUsage = newProductCmptType.newTableStructureUsage();
        tableStructureUsage.setRoleName("TSU");
        tableStructureUsage.setChangingOverTime(false);

        ProductCmpt newProductCmpt = newProductCmpt(newProductCmptType, "Cmpt1");

        assertTrue(newProductCmpt.containsDifferenceToModel(ipsProject));

        newProductCmpt.fixAllDifferencesToModel(ipsProject);
        assertFalse(newProductCmpt.containsDifferenceToModel(ipsProject));
    }

    @Test
    public void testComputeDeltaToModel_productCmptTypeTableContent() {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        ITableStructureUsage tableStructureUsage = newProductCmptType.newTableStructureUsage();
        tableStructureUsage.setRoleName("TSU");
        tableStructureUsage.setChangingOverTime(false);

        ProductCmpt newProductCmpt = newProductCmpt(newProductCmptType, "Cmpt1");

        IPropertyValueContainerToTypeDelta computeDeltaToModel = newProductCmpt.computeDeltaToModel(ipsProject);

        assertEquals(newProductCmpt, computeDeltaToModel.getPropertyValueContainer());
        IDeltaEntry[] entries = computeDeltaToModel.getEntries();
        assertEquals(1, entries.length);
        assertEquals(DeltaType.MISSING_PROPERTY_VALUE, entries[0].getDeltaType());
        assertEquals(tableStructureUsage.getRoleName(), ((IDeltaEntryForProperty)entries[0]).getPropertyName());

        tableStructureUsage.setChangingOverTime(true);
        computeDeltaToModel = newProductCmpt.computeDeltaToModel(ipsProject);
        entries = computeDeltaToModel.getEntries();
        assertEquals(0, entries.length);

        tableStructureUsage.setChangingOverTime(false);
        newProductCmpt.fixAllDifferencesToModel(ipsProject);

        tableStructureUsage.setChangingOverTime(true);
        computeDeltaToModel = newProductCmpt.computeDeltaToModel(ipsProject);
        entries = computeDeltaToModel.getEntries();
        assertEquals(1, entries.length);
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, entries[0].getDeltaType());
        assertEquals(tableStructureUsage.getRoleName(), ((IDeltaEntryForProperty)entries[0]).getPropertyName());
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.model.ipsobject.IFixDifferencesToModelSupport#fixAllDifferencesToModel(IIpsProject)}
     * .
     */
    @Test
    public void testFixAllDifferencesToModel() {
        IPolicyCmptType testType = newPolicyAndProductCmptType(ipsProject, "TestPolicyType", "TestProductType");
        IProductCmptType productCmptType = testType.findProductCmptType(ipsProject);
        IPolicyCmptTypeAttribute a1 = testType.newPolicyCmptTypeAttribute();
        a1.setName("A1");
        a1.setValueSetConfiguredByProduct(true);

        IProductCmpt product = newProductCmpt(productCmptType, "TestProduct");
        IProductCmptGeneration gen = product.getProductCmptGeneration(0);
        gen.newPropertyValues(a1);

        IPolicyCmptTypeAttribute a2 = testType.newPolicyCmptTypeAttribute();
        a2.setName("A2");
        a2.setValueSetConfiguredByProduct(true);

        IProductCmpt product2 = newProductCmpt(productCmptType, "TestProduct2");
        gen = product2.getProductCmptGeneration(0);
        gen.newPropertyValues(a1);
        gen.newPropertyValues(a2);

        assertEquals(true, product.containsDifferenceToModel(ipsProject));
        product.fixAllDifferencesToModel(ipsProject);
        assertEquals(false, product.containsDifferenceToModel(ipsProject));

        assertEquals(false, product2.containsDifferenceToModel(ipsProject));
        product2.fixAllDifferencesToModel(ipsProject);
        assertEquals(false, product2.containsDifferenceToModel(ipsProject));
    }

    @Test
    public void testSetValidTo() {
        productCmpt.setValidTo(new GregorianCalendar(2000, 1, 1));
        assertEquals(new GregorianCalendar(2000, 1, 1), productCmpt.getValidTo());
    }

    @Test
    public void testFindMetaClass() {
        IProductCmptType type = newProductCmptType(ipsProject, "Product");
        productCmpt.setProductCmptType(type.getQualifiedName());

        IIpsSrcFile typeSrcFile = productCmpt.findMetaClassSrcFile(ipsProject);
        assertEquals(type.getIpsSrcFile(), typeSrcFile);
    }

    @Test
    public void testNewPropertyValues() throws Exception {
        assertEquals(0, productCmpt.getPropertyValues(PropertyValueType.ATTRIBUTE_VALUE.getInterfaceClass()).size());
        productCmpt.newPropertyValues(attr1);
        assertEquals(1, productCmpt.getPropertyValues(PropertyValueType.ATTRIBUTE_VALUE.getInterfaceClass()).size());
        productCmpt.newPropertyValues(attr2);
        assertEquals(2, productCmpt.getPropertyValues(PropertyValueType.ATTRIBUTE_VALUE.getInterfaceClass()).size());

        productCmpt.newPropertyValues(policyCmptType.newRule());
        assertEquals(2, productCmpt.getPropertyValues(PropertyValueType.ATTRIBUTE_VALUE.getInterfaceClass()).size());
        productCmpt.newPropertyValues(new PolicyCmptTypeAttribute(policyCmptType, "pcTypeAttribute"));
        assertEquals(2, productCmpt.getPropertyValues(PropertyValueType.ATTRIBUTE_VALUE.getInterfaceClass()).size());
        productCmpt.newPropertyValues(new TableStructureUsage(productCmptType, ""));
        assertEquals(1,
                productCmpt.getPropertyValues(PropertyValueType.TABLE_CONTENT_USAGE.getInterfaceClass()).size());
        assertEquals(2, productCmpt.getPropertyValues(PropertyValueType.ATTRIBUTE_VALUE.getInterfaceClass()).size());
        productCmpt.newPropertyValues(new ProductCmptTypeMethod(productCmptType, "BaseMethod"));
        assertEquals(2, productCmpt.getPropertyValues(PropertyValueType.ATTRIBUTE_VALUE.getInterfaceClass()).size());
        assertEquals(1, productCmpt.getPropertyValues(PropertyValueType.FORMULA.getInterfaceClass()).size());
    }

    @Test
    public void testGetAttributeValue() {
        productCmpt.newPropertyValues(attr1);
        assertNotNull(productCmpt.getAttributeValue("TypeAttr1"));
        assertNull(productCmpt.getAttributeValue("NonExistentAttr"));
    }

    @Test
    public void testHasPropertyValue() {
        assertFalse(productCmpt.hasPropertyValue(attr1, PropertyValueType.ATTRIBUTE_VALUE));

        productCmpt.newPropertyValues(attr1);
        assertTrue(productCmpt.hasPropertyValue(attr1, PropertyValueType.ATTRIBUTE_VALUE));
    }

    @Test
    public void testNewPartThis() {
        Element element = mock(Element.class);
        when(element.getNodeName()).thenReturn(IProductCmptGeneration.TAG_NAME);
        IIpsObjectPart part = productCmpt.newPartThis(element, "genID");
        assertNotNull(part);

        when(element.getNodeName()).thenReturn(IAttributeValue.TAG_NAME);
        part = productCmpt.newPartThis(element, "attrID");
        assertNotNull(part);

        when(element.getNodeName()).thenReturn(ValidationRule.TAG_NAME);
        part = productCmpt.newPartThis(element, "vRuleID");
        assertNull(part);

        when(element.getNodeName()).thenReturn(TableContentUsage.TAG_NAME);
        part = productCmpt.newPartThis(element, "TCUID");
        assertNotNull(part);

    }

    @Test
    public void testGetLatestProductCmptGeneration() {
        GregorianCalendar today = new GregorianCalendar();
        today = new GregorianCalendar(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1,
                today.get(Calendar.DAY_OF_MONTH));
        GregorianCalendar tomorrow = (GregorianCalendar)today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        GregorianCalendar yesterday = (GregorianCalendar)today.clone();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);

        IIpsObjectGeneration firstGeneration = productCmpt.newGeneration(yesterday);
        IIpsObjectGeneration secondGeneration = productCmpt.newGeneration(tomorrow);
        IIpsObjectGeneration thirdGeneration = productCmpt.newGeneration(today);

        assertSame(firstGeneration, productCmpt.getProductCmptGeneration(0));
        assertSame(secondGeneration, productCmpt.getProductCmptGeneration(1));
        assertSame(thirdGeneration, productCmpt.getProductCmptGeneration(2));
        assertSame(firstGeneration, productCmpt.getFirstGeneration());
        assertSame(secondGeneration, productCmpt.getLatestProductCmptGeneration());
    }

    private GregorianCalendar createValidFromDate(int offsetToCurrentDay) {
        int currentYear = GregorianCalendar.getInstance().get(Calendar.YEAR);
        int currentMonth = GregorianCalendar.getInstance().get(Calendar.MONTH);
        int currentDay = GregorianCalendar.getInstance().get(Calendar.DAY_OF_MONTH);
        return new GregorianCalendar(currentYear, currentMonth, currentDay + offsetToCurrentDay);
    }

    @Test
    public void testIsContainerForChangingAssociation() {
        IProductCmptTypeAssociation changingAssoc = productCmptType.newProductCmptTypeAssociation();
        changingAssoc.setChangingOverTime(true);

        assertFalse(productCmpt.isContainerFor(changingAssoc));
    }

    @Test
    public void testIsContainerForStaticAssociation() {
        IProductCmptTypeAssociation staticAssoc = productCmptType.newProductCmptTypeAssociation();
        staticAssoc.setChangingOverTime(false);

        assertTrue(productCmpt.isContainerFor(staticAssoc));
    }

    @Test
    public void testGetLinksIncludingGenerations() {
        IProductCmptGeneration generation1 = (IProductCmptGeneration)productCmpt
                .newGeneration(new GregorianCalendar(2010, 0, 1));
        IProductCmptGeneration generation2 = (IProductCmptGeneration)productCmpt
                .newGeneration(new GregorianCalendar(2011, 0, 1));
        ArrayList<IProductCmptLink> links = new ArrayList<>();
        links.add(productCmpt.newLink("asdff"));
        links.add(productCmpt.newLink("asdff2"));
        links.add(generation1.newLink("asd1"));
        links.add(generation1.newLink("asd2"));
        links.add(generation2.newLink("asd3"));
        links.add(generation2.newLink("asd4"));

        List<IProductCmptLink> linksIncludingGenerations = productCmpt.getLinksIncludingGenerations();
        assertEquals(links, linksIncludingGenerations);
    }

    @Test
    public void testGetTableContentUsages() {
        ITableContentUsage contentUsagePC = productCmpt
                .newPropertyValue(new TableStructureUsage(mock(IProductCmptType.class), ""), ITableContentUsage.class);
        assertNotNull(contentUsagePC);

        assertEquals(1, productCmpt.getTableContentUsages().length);
        assertEquals(contentUsagePC, productCmpt.getTableContentUsages()[0]);
    }

    @Test
    public void testGetFormulas() {
        IFormula formula = productCmpt.newPropertyValue(new ProductCmptTypeMethod(mock(IProductCmptType.class), "Id"),
                IFormula.class);
        assertNotNull(formula);

        assertEquals(1, productCmpt.getFormulas().length);
        assertEquals(formula, productCmpt.getFormulas()[0]);
    }

    @Test
    public void testAddDependenciesFromFormulaExpressions() {
        ProductCmpt productCmptSpy = spy(productCmpt);
        IDependency dependency = mock(IDependency.class);
        ExpressionDependencyDetail dependencyDetail1 = mock(ExpressionDependencyDetail.class);
        ExpressionDependencyDetail dependencyDetail2 = mock(ExpressionDependencyDetail.class);
        IFormula formula1 = mock(IFormula.class);
        IFormula formula2 = mock(IFormula.class);
        when(productCmptSpy.getFormulas()).thenReturn(new IFormula[] { formula1, formula2 });
        Map<IDependency, IExpressionDependencyDetail> dependencyMap1 = new HashMap<>();
        dependencyMap1.put(dependency, dependencyDetail1);
        Map<IDependency, IExpressionDependencyDetail> dependencyMap2 = new HashMap<>();
        dependencyMap2.put(dependency, dependencyDetail2);
        when(formula1.dependsOn()).thenReturn(dependencyMap1);
        when(formula2.dependsOn()).thenReturn(dependencyMap2);

        Map<IDependency, List<IDependencyDetail>> detailsResult = new HashMap<>();
        productCmptSpy.dependsOn(detailsResult);

        assertEquals(1, detailsResult.size());
        assertThat(detailsResult.keySet(), hasItem(dependency));
        List<? extends IDependencyDetail> detailList = detailsResult.get(dependency);
        assertEquals(2, detailList.size());
        assertEquals(dependencyDetail1, detailList.get(0));
        assertEquals(dependencyDetail2, detailList.get(1));
    }

    @Test
    public void testContainsDifferencesToModel_productCmptTypeFormula() {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        IProductCmptTypeMethod newFormulaSignature = newProductCmptType.newFormulaSignature("newFormula");
        newFormulaSignature.setChangingOverTime(false);

        ProductCmpt newProductCmpt = newProductCmpt(newProductCmptType, "Cmpt1");
        assertTrue(newProductCmpt.containsDifferenceToModel(ipsProject));

        newProductCmpt.newPropertyValues(newFormulaSignature);
        assertFalse(newProductCmpt.containsDifferenceToModel(ipsProject));
    }

    @Test
    public void testFixDifferencesToModel_productCmptTypeFormula() {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        IProductCmptTypeMethod newFormulaSignature = newProductCmptType.newFormulaSignature("newFormula");
        newFormulaSignature.setChangingOverTime(false);

        ProductCmpt newProductCmpt = newProductCmpt(newProductCmptType, "Cmpt1");

        assertTrue(newProductCmpt.containsDifferenceToModel(ipsProject));

        newProductCmpt.fixAllDifferencesToModel(ipsProject);
        assertFalse(newProductCmpt.containsDifferenceToModel(ipsProject));
    }

    @Test
    public void testComputeDeltaToModel_productCmptTypeFormula() {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        IProductCmptTypeMethod newFormulaSignature = newProductCmptType.newFormulaSignature("newFormula");
        newFormulaSignature.setChangingOverTime(false);

        ProductCmpt newProductCmpt = newProductCmpt(newProductCmptType, "Cmpt1");

        IPropertyValueContainerToTypeDelta computeDeltaToModel = newProductCmpt.computeDeltaToModel(ipsProject);

        assertEquals(newProductCmpt, computeDeltaToModel.getPropertyValueContainer());
        IDeltaEntry[] entries = computeDeltaToModel.getEntries();
        assertEquals(1, entries.length);
        assertEquals(DeltaType.MISSING_PROPERTY_VALUE, entries[0].getDeltaType());
        assertEquals(newFormulaSignature.getFormulaName(), ((IDeltaEntryForProperty)entries[0]).getPropertyName());

        newFormulaSignature.setChangingOverTime(true);
        computeDeltaToModel = newProductCmpt.computeDeltaToModel(ipsProject);
        entries = computeDeltaToModel.getEntries();
        assertEquals(0, entries.length);

        newFormulaSignature.setChangingOverTime(false);
        newProductCmpt.fixAllDifferencesToModel(ipsProject);

        newFormulaSignature.setChangingOverTime(true);
        computeDeltaToModel = newProductCmpt.computeDeltaToModel(ipsProject);
        entries = computeDeltaToModel.getEntries();
        assertEquals(1, entries.length);
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, entries[0].getDeltaType());
        assertEquals(newFormulaSignature.getFormulaName(), ((IDeltaEntryForProperty)entries[0]).getPropertyName());
    }

    @Test
    public void testNewFormula() {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        IProductCmptTypeMethod formulaSignature = newProductCmptType.newFormulaSignature("newFormula");

        ProductCmpt productCmpt = newProductCmpt(newProductCmptType, "Cmpt1");
        IFormula formula = productCmpt.newPropertyValue(formulaSignature, IFormula.class);

        assertNotNull(formula);
        assertEquals(formulaSignature.getFormulaName(), formula.getFormulaSignature());
    }

    @Test
    public void testNewFormula2() {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        ProductCmpt productCmpt = newProductCmpt(newProductCmptType, "Cmpt1");
        IFormula formula = productCmpt.newPart(IFormula.class);

        assertNotNull(formula);
        assertEquals("", formula.getFormulaSignature());
    }

    @Test
    public void testAllowGenerations_changingOverTimeEnabled() {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        newProductCmptType.setChangingOverTime(true);
        ProductCmpt productCmpt = newProductCmpt(newProductCmptType, "Cmpt1");

        assertTrue(productCmpt.allowGenerations());
    }

    @Test
    public void testAllowGenerations_changingOverTimeDisabled() {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        newProductCmptType.setChangingOverTime(false);
        ProductCmpt productCmpt = newProductCmpt(newProductCmptType, "Cmpt1");

        assertFalse(productCmpt.allowGenerations());
    }

    @Test
    public void testAllowGenerations_productCmptTypeCanNotBeFound() {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        ProductCmpt productCmpt = newProductCmpt(newProductCmptType, "Cmpt1");
        productCmpt = spy(productCmpt);
        when(productCmpt.findProductCmptType(ipsProject)).thenReturn(null);

        assertTrue(productCmpt.allowGenerations());
    }

    @Test
    public void testIsProductTemplate_noTemplate() {
        ProductCmpt product = newProductCmpt(ipsProject, "AnyProdCmpt");

        assertFalse(product.isProductTemplate());
    }

    @Test
    public void testIsProductTemplate_isTemplate() {
        IIpsObject template = newIpsObject(ipsProject, IpsObjectType.PRODUCT_TEMPLATE, "AnyProdCmpt");

        assertThat(template, instanceOf(IProductCmpt.class));
        assertTrue(((IProductCmpt)template).isProductTemplate());
    }

    @Test
    public void testPropertyTemplate() throws Exception {
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(IProductCmpt.PROPERTY_TEMPLATE,
                ProductCmpt.class);

        assertThat(propertyDescriptor.getReadMethod(), is(not(nullValue())));
        assertThat(propertyDescriptor.getWriteMethod(), is(not(nullValue())));
    }

    @Test
    public void testFindTemplate() {
        IProductCmpt product = newProductCmpt(ipsProject, "product");
        product.setTemplate(null);
        assertThat(product.findTemplate(ipsProject), is(nullValue()));

        product.setTemplate("");
        assertThat(product.findTemplate(ipsProject), is(nullValue()));

        product.setTemplate("noSuchTemplateExists");
        assertThat(product.findTemplate(ipsProject), is(nullValue()));

        IProductCmpt template = newProductTemplate(ipsProject, "template");
        product.setTemplate(template.getQualifiedName());
        assertThat(product.findTemplate(ipsProject), is(template));
    }

    @Test
    public void testIsPartOfTemplateHierarchy_prodCmpt() {
        IProductCmpt product = newProductCmpt(ipsProject, "product");
        product.setTemplate(null);

        assertThat(product.isPartOfTemplateHierarchy(), is(false));

        product.setTemplate("someTemplate");
        assertThat(product.isPartOfTemplateHierarchy(), is(true));
    }

    @Test
    public void testIsPartOfTemplateHierarchy_template() {
        IProductCmpt product = newProductTemplate(ipsProject, "product");
        product.setTemplate(null);

        assertThat(product.isPartOfTemplateHierarchy(), is(true));

        product.setTemplate("parentTemplate");
        assertThat(product.isPartOfTemplateHierarchy(), is(true));
    }

    @Test
    public void testAddPartThis_ConfigElement() {
        ProductCmpt product = newProductCmpt(productCmptType, "product");
        IConfiguredDefault configDefault = product.newPropertyValue(policyAttr, IConfiguredDefault.class);

        assertThat(product.addPartThis(configDefault), is(true));
    }

    @Test
    public void testRemovePartThis_ConfigElement() {
        ProductCmpt product = newProductCmpt(productCmptType, "product");
        IConfiguredDefault configDefault = product.newPropertyValue(policyAttr, IConfiguredDefault.class);

        assertThat(product.removePartThis(configDefault), is(true));
        assertNull(product.getPropertyValue(policyAttr, IConfiguredDefault.class));
    }

    @Test
    public void testGetValidationRules() {
        List<IValidationRuleConfig> rules = productCmpt.getValidationRuleConfigs();
        assertEquals(0, rules.size());

        newValidationRuleConfig("rule1");
        rules = productCmpt.getValidationRuleConfigs();
        assertEquals(1, rules.size());

        newValidationRuleConfig("rule2");
        rules = productCmpt.getValidationRuleConfigs();
        assertEquals(2, rules.size());
    }

    private IValidationRuleConfig newValidationRuleConfig(String ruleName) {
        IValidationRule rule = mock(IValidationRule.class);
        when(rule.getPropertyName()).thenReturn(ruleName);
        when(rule.isActivatedByDefault()).thenReturn(false);
        when(rule.getProductCmptPropertyType()).thenReturn(ProductCmptPropertyType.VALIDATION_RULE);
        return productCmpt.newValidationRuleConfig(rule);
    }

    @Test
    public void testGetNumValidationRules() {
        assertEquals(0, productCmpt.getNumOfValidationRules());

        newValidationRuleConfig("rule1");
        assertEquals(1, productCmpt.getNumOfValidationRules());

        newValidationRuleConfig("rule2");
        assertEquals(2, productCmpt.getNumOfValidationRules());
    }

    @Test
    public void testNewValidationRule() {
        assertEquals(0, productCmpt.getNumOfValidationRules());

        newValidationRuleConfig("rule1");
        assertEquals(1, productCmpt.getChildren().length);

        newValidationRuleConfig("rule2");
        assertEquals(2, productCmpt.getChildren().length);
    }

    @Test
    public void testGetValidationRuleByName() {
        IValidationRule rule = policyCmptType.newRule();
        rule.setName("rule1");
        productCmpt.newValidationRuleConfig(rule);

        rule = policyCmptType.newRule();
        rule.setName("ruleTwo");
        productCmpt.newValidationRuleConfig(rule);

        rule = policyCmptType.newRule();
        rule.setName("ruleThree");
        productCmpt.newValidationRuleConfig(rule);

        assertEquals(3, productCmpt.getValidationRuleConfigs().size());

        assertNotNull(productCmpt.getValidationRuleConfig("rule1"));
        assertNotNull(productCmpt.getValidationRuleConfig("ruleTwo"));
        assertNotNull(productCmpt.getValidationRuleConfig("ruleThree"));
        assertNull(productCmpt.getValidationRuleConfig("nonExistingRule"));
        assertNull(productCmpt.getValidationRuleConfig(null));
    }
}
