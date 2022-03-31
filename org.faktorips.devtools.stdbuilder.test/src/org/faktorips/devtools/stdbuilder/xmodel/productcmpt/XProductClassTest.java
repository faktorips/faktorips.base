/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel.productcmpt;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
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
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.xmodel.GeneratorConfig;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelCaches;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.runtime.IConfigurableModelObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
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
        when(superType.getIpsProject()).thenReturn(ipsProject);
        when(superSuperType.getIpsProject()).thenReturn(ipsProject);
        when(type.findSupertype(any(IIpsProject.class))).thenReturn(superType);
        when(superType.findSupertype(any(IIpsProject.class))).thenReturn(superSuperType);
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
        assertEquals(2, attributes.size());
        assertThat(attributes, hasItems(attrNode1, attrNode3));
    }

    @Test
    public void getStaticProductAttributes() {
        XProductCmptClass productClass = new XProductCmptClass(type, modelContext, modelService);
        Set<XProductAttribute> attributes = productClass.getAttributes();
        assertEquals(1, attributes.size());
        assertThat(attributes, hasItem(attrNode2));
    }

    @Test
    public void getAllAttributes() {
        XProductCmptClass productClass = new XProductCmptClass(type, modelContext, modelService);
        Set<XProductAttribute> attributes = productClass.getAllDeclaredAttributes();
        assertEquals(3, attributes.size());
        assertThat(attributes, hasItems(attrNode1, attrNode2, attrNode3));
    }

    @Test
    public void getChangingProductAssociations() {
        XProductClass productClass = new XProductCmptGenerationClass(type, modelContext, modelService);
        Set<XProductAssociation> associations = productClass.getAssociations();
        assertEquals(1, associations.size());
        assertThat(associations, hasItems(assocNode2));
    }

    @Test
    public void getStaticProductAssociations() {
        XProductClass productClass = new XProductCmptClass(type, modelContext, modelService);
        Set<XProductAssociation> associations = productClass.getAssociations();
        assertEquals(2, associations.size());
        assertThat(associations, hasItems(assocNode1, assocNode3));
    }

    @Test
    public void getAllAssociations() {
        XProductClass productClass = new XProductCmptClass(type, modelContext, modelService);
        Set<XProductAssociation> associations = productClass.getAllDeclaredAssociations();
        assertEquals(3, associations.size());
        assertThat(associations, hasItems(assocNode1, assocNode2, assocNode3));
    }

    @Test
    public void testGetPolicyClassName() throws Exception {
        when(type.findPolicyCmptType(ipsProject)).thenReturn(policyType);
        when(modelService.getModelNode(policyType, XPolicyCmptClass.class, modelContext)).thenReturn(xPolicyCmpt);
        when(xProductClass.isConfigurationForPolicyCmptType()).thenReturn(true);

        when(xPolicyCmpt.getSimpleName(BuilderAspect.INTERFACE)).thenReturn("IPolicyCmpt");
        when(xPolicyCmpt.getSimpleName(BuilderAspect.IMPLEMENTATION)).thenReturn("PolicyCmpt");

        assertEquals("IPolicyCmpt", xProductClass.getPolicyClassName(BuilderAspect.INTERFACE));
        assertEquals("PolicyCmpt", xProductClass.getPolicyClassName(BuilderAspect.IMPLEMENTATION));

        assertEquals("IPolicyCmpt", xProductClass.getPolicyInterfaceName());
        assertEquals("PolicyCmpt", xProductClass.getPolicyImplClassName());
    }

    @Test
    public void testGetPolicyClassName_notConfigured() throws Exception {
        when(type.findPolicyCmptType(ipsProject)).thenReturn(policyType);
        when(modelService.getModelNode(policyType, XPolicyCmptClass.class, modelContext)).thenReturn(xPolicyCmpt);
        when(xProductClass.isConfigurationForPolicyCmptType()).thenReturn(false);

        assertEquals(IConfigurableModelObject.class.getSimpleName(),
                xProductClass.getPolicyClassName(BuilderAspect.INTERFACE));
        assertEquals(IConfigurableModelObject.class.getSimpleName(),
                xProductClass.getPolicyClassName(BuilderAspect.IMPLEMENTATION));
        assertEquals(IConfigurableModelObject.class.getSimpleName(), xProductClass.getPolicyInterfaceName());
        assertEquals(IConfigurableModelObject.class.getSimpleName(), xProductClass.getPolicyImplClassName());
    }

    @Test
    public void testIsContainsNotDerivedOrConstrainingAssociations_noAssociation() throws Exception {
        doReturn(new HashSet<XProductAssociation>()).when(xProductClass).getAssociations();
        assertFalse(xProductClass.isContainsNotDerivedOrConstrainingAssociations());
    }

    @Test
    public void testIsContainsNotDerivedOrConstrainingAssociations_someAssociationDerived() throws Exception {
        when(assocNode1.isDerived()).thenReturn(true);
        when(assocNode2.isOneToMany()).thenReturn(true);

        HashSet<XProductAssociation> associations = new HashSet<>();
        associations.add(assocNode1);
        associations.add(assocNode2);
        doReturn(associations).when(xProductClass).getAssociations();
        assertTrue(xProductClass.isContainsNotDerivedOrConstrainingAssociations());
    }

    @Test
    public void testIsContainsNotDerivedOrConstrainingAssociations_someAssociationConstrains() throws Exception {
        when(assocNode1.isConstrain()).thenReturn(true);
        when(assocNode2.isOneToMany()).thenReturn(true);

        HashSet<XProductAssociation> associations = new HashSet<>();
        associations.add(assocNode1);
        associations.add(assocNode2);
        doReturn(associations).when(xProductClass).getAssociations();
        assertTrue(xProductClass.isContainsNotDerivedOrConstrainingAssociations());
    }

    @Test
    public void testGetConfiguredAttributesInternal() throws Exception {
        when(xProductClass.getType()).thenReturn(type);
        doReturn(true).when(xProductClass).isChangeOverTimeClass();
        doReturn(xPolicyCmpt).when(xProductClass).getPolicyCmptClass();

        assertTrue(xProductClass.getConfiguredAttributesInternal().isEmpty());

        when(xProductClass.isConfigurationForPolicyCmptType()).thenReturn(true);

        assertTrue(xProductClass.getConfiguredAttributesInternal().isEmpty());

        doReturn(true).when(xProductClass).isConfigurationForPolicyCmptType();

        when(polAttrNode1.isProductRelevant()).thenReturn(true);
        when(polAttrNode1.isGenerateGetAllowedValuesForAndGetDefaultValue()).thenReturn(true);
        when(polAttrNode2.isProductRelevant()).thenReturn(true);

        Set<XPolicyAttribute> attributes = new LinkedHashSet<>();
        attributes.add(polAttrNode1);
        attributes.add(polAttrNode2);
        attributes.add(polAttrNode3);

        when(xPolicyCmpt.getAttributes()).thenReturn(attributes);

        assertTrue(xProductClass.getConfiguredAttributesInternal().isEmpty());

        when(type.getQualifiedName()).thenReturn("myProduct");
        when(xPolicyCmpt.isConfiguredBy("myProduct")).thenReturn(true);

        assertEquals(1, xProductClass.getConfiguredAttributesInternal().size());
        assertThat(xProductClass.getConfiguredAttributesInternal(), hasItem(polAttrNode1));
    }

    @Test
    public void testGetAssociationsInternal_changing() {
        Set<IProductCmptTypeAssociation> associations = xProductClass.getAssociationsInternal(true);

        assertEquals(1, associations.size());
    }

    @Test
    public void testGetAssociationsInternal_static() {
        Set<IProductCmptTypeAssociation> associations = xProductClass.getAssociationsInternal(false);

        assertEquals(2, associations.size());
    }

    @Test
    public void testGetAttributes_nonGenerated() throws Exception {
        XProductCmptGenerationClass productClass = new XProductCmptGenerationClass(type, modelContext, modelService);
        when(attrNode1.isGenerateContentCode()).thenReturn(false);
        when(attrNode3.isGenerateContentCode()).thenReturn(false);

        Set<XProductAttribute> result = productClass.getAttributes();

        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAttributes_someGenerated() throws Exception {
        XProductCmptGenerationClass productClass = new XProductCmptGenerationClass(type, modelContext, modelService);
        when(attrNode1.isGenerateContentCode()).thenReturn(false);
        when(attrNode3.isGenerateContentCode()).thenReturn(true);

        Set<XProductAttribute> result = productClass.getAttributes();

        assertEquals(1, result.size());
        assertThat(result, hasItem(attrNode3));
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

        assertTrue(xProductClass.isValidForCodeGeneration());
    }

    @Test
    public void testIsValidForCodeGeneration_typeInvalid() {
        when(xProductClass.getType()).thenReturn(type);
        when(type.isValid(ipsProject)).thenReturn(false);

        assertFalse(xProductClass.isValidForCodeGeneration());
    }

    @Test
    public void testIsValidForCodeGeneration_notConfigured() {
        when(xProductClass.getType()).thenReturn(type);
        when(type.isValid(ipsProject)).thenReturn(true);
        when(xProductClass.isConfigurationForPolicyCmptType()).thenReturn(false);

        assertTrue(xProductClass.isValidForCodeGeneration());
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

        assertFalse(xProductClass.isValidForCodeGeneration());
    }

    @Test
    public void testIsValidForCodeGeneration_policyTypeInvalidForProductProject() {
        when(xProductClass.getType()).thenReturn(type);
        when(type.isValid(ipsProject)).thenReturn(true);
        when(type.findPolicyCmptType(ipsProject)).thenReturn(policyType);
        when(modelService.getModelNode(policyType, XPolicyCmptClass.class, modelContext)).thenReturn(xPolicyCmpt);
        when(xProductClass.isConfigurationForPolicyCmptType()).thenReturn(true);
        IIpsProject ipsProject2 = mock(IIpsProject.class);
        when(policyType.isValid(ipsProject)).thenReturn(false);
        when(policyType.isValid(ipsProject2)).thenReturn(true);
        when(xPolicyCmpt.getType()).thenReturn(policyType);
        when(xPolicyCmpt.getIpsProject()).thenReturn(ipsProject2);

        assertTrue(xProductClass.isValidForCodeGeneration());
    }

}
