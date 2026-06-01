/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.xmodel.productcmpt;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.model.builder.naming.BuilderAspect;
import org.faktorips.devtools.model.builder.xmodel.GeneratorConfig;
import org.faktorips.devtools.model.builder.xmodel.GeneratorModelCaches;
import org.faktorips.devtools.model.builder.xmodel.GeneratorModelContext;
import org.faktorips.devtools.model.builder.xmodel.ModelService;
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyAssociation;
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyAttribute;
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.runtime.IConfigurableModelObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class XProductClassTest {

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IProductCmptType type;

    @Mock
    private IPolicyCmptType policyType;

    @Mock
    private XPolicyCmptClass xPolicyCmpt;

    @Mock
    private IProductCmptType superType;

    @Mock
    private IProductCmptType superSuperType;

    @Mock
    private GeneratorModelContext modelContext;

    @Mock
    private GeneratorConfig generatorConfig;

    @Mock
    private ModelService modelService;

    @Mock
    private XProductAttribute attrNode1;

    @Mock
    private XProductAttribute attrNode2;

    @Mock
    private XProductAttribute attrNode3;

    @Mock
    private XPolicyAttribute polAttrNode1;

    @Mock
    private XPolicyAttribute polAttrNode2;

    @Mock
    private XPolicyAttribute polAttrNode3;

    @Mock
    private XProductAssociation assocNode1;

    @Mock
    private XProductAssociation assocNode2;

    @Mock
    private XProductAssociation assocNode3;

    private XProductClass xProductClass;

    @Before
    public void initModelContext() {
        when(modelContext.getBaseGeneratorConfig()).thenReturn(generatorConfig);
        when(modelContext.getGeneratorModelCache()).thenReturn(new GeneratorModelCaches());
    }

    @Before
    public void createProductClass() {
        // need this because XProductClass is abstract
        xProductClass = mock(XProductClass.class, CALLS_REAL_METHODS);
        when(xProductClass.getModelService()).thenReturn(modelService);
        when(xProductClass.getContext()).thenReturn(modelContext);
        when(xProductClass.getIpsObjectPartContainer()).thenReturn(type);
    }

    @Before
    public void createTypes() {
        when(type.getIpsProject()).thenReturn(ipsProject);
    }

    @Before
    public void setUpAssociations() {
        List<IProductCmptTypeAssociation> assocList = new ArrayList<>();
        IProductCmptTypeAssociation assoc1 = mock(IProductCmptTypeAssociation.class);
        IProductCmptTypeAssociation assoc2 = mock(IProductCmptTypeAssociation.class);
        IProductCmptTypeAssociation assoc3 = mock(IProductCmptTypeAssociation.class);
        when(assoc1.isChangingOverTime()).thenReturn(false);
        when(assoc2.isChangingOverTime()).thenReturn(true);
        when(assoc3.isChangingOverTime()).thenReturn(false);

        assocList.add(assoc1);
        assocList.add(assoc2);
        assocList.add(assoc3);

        when(type.getProductCmptTypeAssociations()).thenReturn(assocList);
        when(modelService.getModelNode(assoc1, XProductAssociation.class, modelContext)).thenReturn(assocNode1);
        when(modelService.getModelNode(assoc2, XProductAssociation.class, modelContext)).thenReturn(assocNode2);
        when(modelService.getModelNode(assoc3, XProductAssociation.class, modelContext)).thenReturn(assocNode3);
    }

    @Before
    public void setUpAttributes() {
        List<IProductCmptTypeAttribute> attrList = new ArrayList<>();
        IProductCmptTypeAttribute attr1 = mock(IProductCmptTypeAttribute.class);
        IProductCmptTypeAttribute attr2 = mock(IProductCmptTypeAttribute.class);
        IProductCmptTypeAttribute attr3 = mock(IProductCmptTypeAttribute.class);
        attrList.add(attr1);
        attrList.add(attr2);
        attrList.add(attr3);

        when(attr1.isChangingOverTime()).thenReturn(true);
        when(attr2.isChangingOverTime()).thenReturn(false);
        when(attr3.isChangingOverTime()).thenReturn(true);

        when(type.getProductCmptTypeAttributes()).thenReturn(attrList);
        when(modelService.getModelNode(attr1, XProductAttribute.class, modelContext)).thenReturn(attrNode1);
        when(modelService.getModelNode(attr2, XProductAttribute.class, modelContext)).thenReturn(attrNode2);
        when(modelService.getModelNode(attr3, XProductAttribute.class, modelContext)).thenReturn(attrNode3);

        when(attrNode1.isGenerateContentCode()).thenReturn(true);
        when(attrNode2.isGenerateContentCode()).thenReturn(true);
        when(attrNode3.isGenerateContentCode()).thenReturn(true);
    }

    @Test
    public void getChangableProductAttributes() {
        XProductClass productClass = new XProductCmptGenerationClass(type, modelContext, modelService);
        Set<XProductAttribute> attributes = productClass.getAttributes();
        assertThat(attributes.size(), is(2));
        assertThat(attributes, hasItems(attrNode1, attrNode3));
    }

    @Test
    public void getStaticProductAttributes() {
        XProductCmptClass productClass = new XProductCmptClass(type, modelContext, modelService);
        Set<XProductAttribute> attributes = productClass.getAttributes();
        assertThat(attributes.size(), is(1));
        assertThat(attributes, hasItem(attrNode2));
    }

    @Test
    public void getAllAttributes() {
        XProductCmptClass productClass = new XProductCmptClass(type, modelContext, modelService);
        Set<XProductAttribute> attributes = productClass.getAllDeclaredAttributes();
        assertThat(attributes.size(), is(3));
        assertThat(attributes, hasItems(attrNode1, attrNode2, attrNode3));
    }

    @Test
    public void getChangingProductAssociations() {
        XProductClass productClass = new XProductCmptGenerationClass(type, modelContext, modelService);
        Set<XProductAssociation> associations = productClass.getAssociations();
        assertThat(associations.size(), is(1));
        assertThat(associations, hasItems(assocNode2));
    }

    @Test
    public void getStaticProductAssociations() {
        XProductClass productClass = new XProductCmptClass(type, modelContext, modelService);
        Set<XProductAssociation> associations = productClass.getAssociations();
        assertThat(associations.size(), is(2));
        assertThat(associations, hasItems(assocNode1, assocNode3));
    }

    @Test
    public void getAllAssociations() {
        XProductClass productClass = new XProductCmptClass(type, modelContext, modelService);
        Set<XProductAssociation> associations = productClass.getAllDeclaredAssociations();
        assertThat(associations.size(), is(3));
        assertThat(associations, hasItems(assocNode1, assocNode2, assocNode3));
    }

    @Test
    public void testGetPolicyClassName() throws Exception {
        when(type.findPolicyCmptType(ipsProject)).thenReturn(policyType);
        when(modelService.getModelNode(policyType, XPolicyCmptClass.class, modelContext)).thenReturn(xPolicyCmpt);
        when(xProductClass.isConfigurationForPolicyCmptType()).thenReturn(true);

        when(xPolicyCmpt.getSimpleName(BuilderAspect.INTERFACE)).thenReturn("IPolicyCmpt");
        when(xPolicyCmpt.getSimpleName(BuilderAspect.IMPLEMENTATION)).thenReturn("PolicyCmpt");

        assertThat(xProductClass.getPolicyClassName(BuilderAspect.INTERFACE), is("IPolicyCmpt"));
        assertThat(xProductClass.getPolicyClassName(BuilderAspect.IMPLEMENTATION), is("PolicyCmpt"));

        assertThat(xProductClass.getPolicyInterfaceName(), is("IPolicyCmpt"));
        assertThat(xProductClass.getPolicyImplClassName(), is("PolicyCmpt"));
    }

    @Test
    public void testGetPolicyClassName_notConfigured() throws Exception {
        when(xProductClass.isConfigurationForPolicyCmptType()).thenReturn(false);

        assertThat(xProductClass.getPolicyClassName(BuilderAspect.INTERFACE),
                is(IConfigurableModelObject.class.getSimpleName()));
        assertThat(xProductClass.getPolicyClassName(BuilderAspect.IMPLEMENTATION),
                is(IConfigurableModelObject.class.getSimpleName()));
        assertThat(xProductClass.getPolicyInterfaceName(), is(IConfigurableModelObject.class.getSimpleName()));
        assertThat(xProductClass.getPolicyImplClassName(), is(IConfigurableModelObject.class.getSimpleName()));
    }

    @Test
    public void testIsContainsNotDerivedOrConstrainingAssociations_noAssociation() throws Exception {
        doReturn(new HashSet<>()).when(xProductClass).getAssociations();
        assertThat(xProductClass.isContainsNotDerivedOrConstrainingAssociations(), is(false));
    }

    @Test
    public void testIsContainsNotDerivedOrConstrainingAssociations_someAssociationDerived() throws Exception {

        HashSet<XProductAssociation> associations = new HashSet<>();
        associations.add(assocNode1);
        associations.add(assocNode2);
        doReturn(associations).when(xProductClass).getAssociations();
        assertThat(xProductClass.isContainsNotDerivedOrConstrainingAssociations(), is(true));
    }

    @Test
    public void testIsContainsNotDerivedOrConstrainingAssociations_someAssociationConstrains() throws Exception {

        HashSet<XProductAssociation> associations = new HashSet<>();
        associations.add(assocNode1);
        associations.add(assocNode2);
        doReturn(associations).when(xProductClass).getAssociations();
        assertThat(xProductClass.isContainsNotDerivedOrConstrainingAssociations(), is(true));
    }

    @Test
    public void testGetConfiguredAttributesInternal() throws Exception {
        when(xProductClass.getType()).thenReturn(type);
        doReturn(xPolicyCmpt).when(xProductClass).getPolicyCmptClass();

        assertThat(xProductClass.getConfiguredAttributesInternal(), is(empty()));

        when(xProductClass.isConfigurationForPolicyCmptType()).thenReturn(true);

        assertThat(xProductClass.getConfiguredAttributesInternal(), is(empty()));

        doReturn(true).when(xProductClass).isConfigurationForPolicyCmptType();

        when(polAttrNode1.isProductRelevant()).thenReturn(true);
        when(polAttrNode1.isGenerateGetAllowedValuesForAndGetDefaultValue()).thenReturn(true);
        when(polAttrNode2.isProductRelevant()).thenReturn(true);

        Set<XPolicyAttribute> attributes = new LinkedHashSet<>();
        attributes.add(polAttrNode1);
        attributes.add(polAttrNode2);
        attributes.add(polAttrNode3);

        when(xPolicyCmpt.getAttributes()).thenReturn(attributes);

        assertThat(xProductClass.getConfiguredAttributesInternal(), is(empty()));

        when(type.getQualifiedName()).thenReturn("myProduct");
        when(xPolicyCmpt.isConfiguredBy("myProduct")).thenReturn(true);

        assertThat(xProductClass.getConfiguredAttributesInternal().size(), is(1));
        assertThat(xProductClass.getConfiguredAttributesInternal(), hasItem(polAttrNode1));
    }

    @Test
    public void testGetAssociationsInternal_changing() {
        Set<IProductCmptTypeAssociation> associations = xProductClass.getAssociationsInternal(true);

        assertThat(associations.size(), is(1));
    }

    @Test
    public void testGetAssociationsInternal_static() {
        Set<IProductCmptTypeAssociation> associations = xProductClass.getAssociationsInternal(false);

        assertThat(associations.size(), is(2));
    }

    @Test
    public void testGetAttributes_nonGenerated() throws Exception {
        XProductCmptGenerationClass productClass = new XProductCmptGenerationClass(type, modelContext, modelService);
        when(attrNode1.isGenerateContentCode()).thenReturn(false);
        when(attrNode3.isGenerateContentCode()).thenReturn(false);

        Set<XProductAttribute> result = productClass.getAttributes();

        assertThat(result, is(empty()));
    }

    @Test
    public void testGetAttributes_someGenerated() throws Exception {
        XProductCmptGenerationClass productClass = new XProductCmptGenerationClass(type, modelContext, modelService);
        when(attrNode1.isGenerateContentCode()).thenReturn(false);
        when(attrNode3.isGenerateContentCode()).thenReturn(true);

        Set<XProductAttribute> result = productClass.getAttributes();

        assertThat(result.size(), is(1));
        assertThat(result, hasItem(attrNode3));
    }

    @Test
    public void testGetCardinalityConfigurableAssociations() {
        when(assocNode1.isCardinalityConfigurable()).thenReturn(true);
        when(assocNode2.isCardinalityConfigurable()).thenReturn(false);
        when(assocNode3.isCardinalityConfigurable()).thenReturn(true);

        LinkedHashSet<XProductAssociation> associations = new LinkedHashSet<>();
        associations.add(assocNode1);
        associations.add(assocNode2);
        associations.add(assocNode3);
        doReturn(associations).when(xProductClass).getAssociations();

        Set<XProductAssociation> result = xProductClass.getCardinalityConfigurableAssociations();

        assertThat(result.size(), is(2));
        assertThat(result, hasItems(assocNode1, assocNode3));
    }

    @Test
    public void testGetCardinalityConfigurableAssociations_none() {
        when(assocNode1.isCardinalityConfigurable()).thenReturn(false);
        when(assocNode2.isCardinalityConfigurable()).thenReturn(false);
        when(assocNode3.isCardinalityConfigurable()).thenReturn(false);

        LinkedHashSet<XProductAssociation> associations = new LinkedHashSet<>();
        associations.add(assocNode1);
        associations.add(assocNode2);
        associations.add(assocNode3);
        doReturn(associations).when(xProductClass).getAssociations();

        Set<XProductAssociation> result = xProductClass.getCardinalityConfigurableAssociations();

        assertThat(result, is(empty()));
    }

    @Test
    public void testIsValidForCodeGeneration() {
        when(xProductClass.getType()).thenReturn(type);
        when(type.isValid(ipsProject)).thenReturn(true);
        when(type.findPolicyCmptType(ipsProject)).thenReturn(policyType);
        when(modelService.getModelNode(policyType, XPolicyCmptClass.class, modelContext)).thenReturn(xPolicyCmpt);
        when(xProductClass.isConfigurationForPolicyCmptType()).thenReturn(true);
        when(policyType.isValid(ipsProject)).thenReturn(true);
        when(xPolicyCmpt.getType()).thenReturn(policyType);
        when(xPolicyCmpt.getIpsProject()).thenReturn(ipsProject);

        assertThat(xProductClass.isValidForCodeGeneration(), is(true));
    }

    @Test
    public void testIsValidForCodeGeneration_typeInvalid() {
        when(xProductClass.getType()).thenReturn(type);
        when(type.isValid(ipsProject)).thenReturn(false);

        assertThat(xProductClass.isValidForCodeGeneration(), is(false));
    }

    @Test
    public void testIsValidForCodeGeneration_notConfigured() {
        when(xProductClass.getType()).thenReturn(type);
        when(type.isValid(ipsProject)).thenReturn(true);
        when(xProductClass.isConfigurationForPolicyCmptType()).thenReturn(false);

        assertThat(xProductClass.isValidForCodeGeneration(), is(true));
    }

    @Test
    public void testIsValidForCodeGeneration_policyTypeInvalid() {
        when(xProductClass.getType()).thenReturn(type);
        when(type.isValid(ipsProject)).thenReturn(true);
        when(type.findPolicyCmptType(ipsProject)).thenReturn(policyType);
        when(modelService.getModelNode(policyType, XPolicyCmptClass.class, modelContext)).thenReturn(xPolicyCmpt);
        when(xProductClass.isConfigurationForPolicyCmptType()).thenReturn(true);
        when(policyType.isValid(ipsProject)).thenReturn(false);
        when(xPolicyCmpt.getType()).thenReturn(policyType);
        when(xPolicyCmpt.getIpsProject()).thenReturn(ipsProject);

        assertThat(xProductClass.isValidForCodeGeneration(), is(false));
    }

    @Test
    public void testIsValidForCodeGeneration_policyTypeInvalidForProductProject() {
        when(xProductClass.getType()).thenReturn(type);
        when(type.isValid(ipsProject)).thenReturn(true);
        when(type.findPolicyCmptType(ipsProject)).thenReturn(policyType);
        when(modelService.getModelNode(policyType, XPolicyCmptClass.class, modelContext)).thenReturn(xPolicyCmpt);
        when(xProductClass.isConfigurationForPolicyCmptType()).thenReturn(true);
        IIpsProject ipsProject2 = mock(IIpsProject.class);
        when(policyType.isValid(ipsProject2)).thenReturn(true);
        when(xPolicyCmpt.getType()).thenReturn(policyType);
        when(xPolicyCmpt.getIpsProject()).thenReturn(ipsProject2);

        assertThat(xProductClass.isValidForCodeGeneration(), is(true));
    }

    @Test
    public void testGetPureCardinalityConfigurablePolicyAssociations_notConfigured() {
        when(xProductClass.isConfigurationForPolicyCmptType()).thenReturn(false);

        Set<XPolicyAssociation> result = xProductClass.getPureCardinalityConfigurablePolicyAssociations();

        assertThat(result, is(empty()));
    }

    @Test
    public void testGetPureCardinalityConfigurablePolicyAssociations_componentWithoutGenerations() {
        when(xProductClass.isConfigurationForPolicyCmptType()).thenReturn(true);
        when(xProductClass.isChangeOverTimeClass()).thenReturn(false);
        when(type.isChangingOverTime()).thenReturn(false);
        when(type.findPolicyCmptType(ipsProject)).thenReturn(policyType);
        when(modelService.getModelNode(policyType, XPolicyCmptClass.class, modelContext)).thenReturn(xPolicyCmpt);
        when(type.getQualifiedName()).thenReturn("myProduct");
        when(xPolicyCmpt.isConfiguredBy("myProduct")).thenReturn(true);

        XPolicyAssociation polAssoc1 = mock(XPolicyAssociation.class);
        XPolicyAssociation polAssoc2 = mock(XPolicyAssociation.class);
        when(polAssoc1.isCardinalityConfigurable()).thenReturn(true);
        when(polAssoc1.getName(false)).thenReturn("coverage");
        when(polAssoc2.isCardinalityConfigurable()).thenReturn(false);

        LinkedHashSet<XPolicyAssociation> polAssociations = new LinkedHashSet<>();
        polAssociations.add(polAssoc1);
        polAssociations.add(polAssoc2);
        when(xPolicyCmpt.getAssociations()).thenReturn(polAssociations);

        doReturn(new LinkedHashSet<>()).when(xProductClass).getAllDeclaredAssociations();

        Set<XPolicyAssociation> result = xProductClass.getPureCardinalityConfigurablePolicyAssociations();

        assertThat(result.size(), is(1));
        assertThat(result, hasItem(polAssoc1));
    }

    @Test
    public void testGetPureCardinalityConfigurablePolicyAssociations_excludesCoveredByProductAssociation() {
        when(xProductClass.isConfigurationForPolicyCmptType()).thenReturn(true);
        when(xProductClass.isChangeOverTimeClass()).thenReturn(false);
        when(type.isChangingOverTime()).thenReturn(false);
        when(type.findPolicyCmptType(ipsProject)).thenReturn(policyType);
        when(modelService.getModelNode(policyType, XPolicyCmptClass.class, modelContext)).thenReturn(xPolicyCmpt);
        when(type.getQualifiedName()).thenReturn("myProduct");
        when(xPolicyCmpt.isConfiguredBy("myProduct")).thenReturn(true);

        XPolicyAssociation polAssoc1 = mock(XPolicyAssociation.class);
        when(polAssoc1.isCardinalityConfigurable()).thenReturn(true);
        when(polAssoc1.getName(false)).thenReturn("coverage");

        LinkedHashSet<XPolicyAssociation> polAssociations = new LinkedHashSet<>();
        polAssociations.add(polAssoc1);
        when(xPolicyCmpt.getAssociations()).thenReturn(polAssociations);

        IProductCmptTypeAssociation prodAssoc = mock(IProductCmptTypeAssociation.class);
        IPolicyCmptTypeAssociation matchingPolAssoc = mock(IPolicyCmptTypeAssociation.class);
        when(prodAssoc.findMatchingPolicyCmptTypeAssociation(ipsProject)).thenReturn(matchingPolAssoc);
        when(matchingPolAssoc.getTargetRoleSingular()).thenReturn("coverage");

        XProductAssociation xProdAssoc = mock(XProductAssociation.class);
        when(xProdAssoc.isCardinalityConfigurable()).thenReturn(true);
        when(xProdAssoc.getAssociation()).thenReturn(prodAssoc);

        LinkedHashSet<XProductAssociation> prodAssociations = new LinkedHashSet<>();
        prodAssociations.add(xProdAssoc);
        doReturn(prodAssociations).when(xProductClass).getAllDeclaredAssociations();

        Set<XPolicyAssociation> result = xProductClass.getPureCardinalityConfigurablePolicyAssociations();

        assertThat(result, is(empty()));
    }

    @Test
    public void testGetPureCardinalityConfigurablePolicyAssociations_generationClassForTypeWithGenerations() {
        when(xProductClass.isConfigurationForPolicyCmptType()).thenReturn(true);
        when(xProductClass.isChangeOverTimeClass()).thenReturn(true);
        when(type.isChangingOverTime()).thenReturn(true);
        when(type.findPolicyCmptType(ipsProject)).thenReturn(policyType);
        when(modelService.getModelNode(policyType, XPolicyCmptClass.class, modelContext)).thenReturn(xPolicyCmpt);
        when(type.getQualifiedName()).thenReturn("myProduct");
        when(xPolicyCmpt.isConfiguredBy("myProduct")).thenReturn(true);

        XPolicyAssociation polAssoc1 = mock(XPolicyAssociation.class);
        when(polAssoc1.isCardinalityConfigurable()).thenReturn(true);
        when(polAssoc1.getName(false)).thenReturn("coverage");

        LinkedHashSet<XPolicyAssociation> polAssociations = new LinkedHashSet<>();
        polAssociations.add(polAssoc1);
        when(xPolicyCmpt.getAssociations()).thenReturn(polAssociations);

        doReturn(new LinkedHashSet<>()).when(xProductClass).getAllDeclaredAssociations();

        Set<XPolicyAssociation> result = xProductClass.getPureCardinalityConfigurablePolicyAssociations();

        assertThat(result.size(), is(1));
        assertThat(result, hasItem(polAssoc1));
    }

    @Test
    public void testGetPureCardinalityConfigurablePolicyAssociations_componentClassForTypeWithGenerations() {
        when(xProductClass.isConfigurationForPolicyCmptType()).thenReturn(true);
        when(xProductClass.isChangeOverTimeClass()).thenReturn(false);
        when(type.isChangingOverTime()).thenReturn(true);

        Set<XPolicyAssociation> result = xProductClass.getPureCardinalityConfigurablePolicyAssociations();

        assertThat(result, is(empty()));
    }

    @Test
    public void testGetPureCardinalityConfigurablePolicyAssociations_notConfiguredByThisType() {
        when(xProductClass.isConfigurationForPolicyCmptType()).thenReturn(true);
        when(xProductClass.isChangeOverTimeClass()).thenReturn(false);
        when(type.isChangingOverTime()).thenReturn(false);
        when(type.findPolicyCmptType(ipsProject)).thenReturn(policyType);
        when(modelService.getModelNode(policyType, XPolicyCmptClass.class, modelContext)).thenReturn(xPolicyCmpt);
        when(type.getQualifiedName()).thenReturn("myProduct");
        when(xPolicyCmpt.isConfiguredBy("myProduct")).thenReturn(false);

        Set<XPolicyAssociation> result = xProductClass.getPureCardinalityConfigurablePolicyAssociations();

        assertThat(result, is(empty()));
    }

}
