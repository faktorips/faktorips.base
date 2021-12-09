/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel.policycmpt;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.stdbuilder.xmodel.GeneratorConfig;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductCmptClass;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductCmptGenerationClass;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelCaches;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XPolicyCmptClassTest {

    @Mock
    private GeneratorModelContext modelContext;

    @Mock
    private GeneratorConfig generatorConfig;

    @Mock
    private ModelService modelService;

    @Mock
    private IPolicyCmptType type;

    @Mock
    private IProductCmptType productCmptType;

    @Mock
    private XPolicyAttribute attributeNode1;

    @Mock
    private XPolicyAttribute attributeNode2;

    @Mock
    private XPolicyAssociation associationNode1;

    @Mock
    private XPolicyAssociation associationNode2;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private XPolicyCmptClass superXType;

    @Before
    public void initModelContext() {
        GeneratorModelCaches generatorModelCache = new GeneratorModelCaches();
        when(modelContext.getGeneratorModelCache()).thenReturn(generatorModelCache);
        when(modelContext.getGeneratorConfig(any(IIpsObject.class))).thenReturn(generatorConfig);
        when(modelContext.getBaseGeneratorConfig()).thenReturn(generatorConfig);
    }

    @Test
    public void initAttributes() {
        setupAttributeList();

        XPolicyCmptClass policyCmptClass = new XPolicyCmptClass(type, modelContext, modelService);
        Set<XPolicyAttribute> attributeNodeSet = policyCmptClass.getAttributes();
        assertEquals(2, attributeNodeSet.size());
        assertThat(attributeNodeSet, hasItems(attributeNode1, attributeNode2));
    }

    @Test
    public void initAttributeList() {
        setupAttributeList();

        XPolicyCmptClass policyCmptClass = new XPolicyCmptClass(type, modelContext, modelService);
        Set<XPolicyAttribute> attributeList = policyCmptClass.getAttributes();
        Set<XPolicyAttribute> secondAttributeList = policyCmptClass.getAttributes();
        // returns copies of the same list
        assertNotSame(attributeList, secondAttributeList);
        assertEquals(attributeList, secondAttributeList);
    }

    private void setupAttributeList() {
        IPolicyCmptTypeAttribute attr1 = mock(IPolicyCmptTypeAttribute.class);
        IPolicyCmptTypeAttribute attr2 = mock(IPolicyCmptTypeAttribute.class);
        List<IPolicyCmptTypeAttribute> attrList = new ArrayList<>();
        attrList.add(attr1);
        attrList.add(attr2);

        doReturn(attributeNode1).when(modelService).getModelNode(attr1, XPolicyAttribute.class, modelContext);
        doReturn(attributeNode2).when(modelService).getModelNode(attr2, XPolicyAttribute.class, modelContext);
        when(type.getPolicyCmptTypeAttributes()).thenReturn(attrList);
    }

    @Test
    public void testInitAssociations() {
        setupAssociationList();
        XPolicyCmptClass policyCmptClass = new XPolicyCmptClass(type, modelContext, modelService);
        Set<XPolicyAssociation> associationNodeSet = policyCmptClass.getAssociations();
        assertEquals(2, associationNodeSet.size());
        assertThat(associationNodeSet, hasItems(associationNode1, associationNode2));
    }

    @Test
    public void testInitAssociationList() {
        setupAssociationList();

        XPolicyCmptClass policyCmptClass = new XPolicyCmptClass(type, modelContext, modelService);
        Set<XPolicyAssociation> assocList = policyCmptClass.getAssociations();
        Set<XPolicyAssociation> secondAssocList = policyCmptClass.getAssociations();
        // returns copies of the same list
        assertNotSame(assocList, secondAssocList);
        assertEquals(assocList, secondAssocList);
    }

    private void setupAssociationList() {
        IPolicyCmptTypeAssociation assoc1 = mock(IPolicyCmptTypeAssociation.class);
        IPolicyCmptTypeAssociation assoc2 = mock(IPolicyCmptTypeAssociation.class);
        List<IPolicyCmptTypeAssociation> assocList = new ArrayList<>();
        assocList.add(assoc1);
        assocList.add(assoc2);

        doReturn(associationNode1).when(modelService).getModelNode(assoc1, XPolicyAssociation.class, modelContext);
        doReturn(associationNode2).when(modelService).getModelNode(assoc2, XPolicyAssociation.class, modelContext);
        when(type.getAssociations()).thenReturn(new ArrayList<IAssociation>(assocList));
        when(type.getPolicyCmptTypeAssociations()).thenReturn(assocList);
    }

    @Test
    public void returnProdGenerationClassName() {
        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();
        IProductCmptType prodType = initProdType(policyCmptClass);

        XProductCmptGenerationClass xProdGenClass = mock(XProductCmptGenerationClass.class);
        when(modelService.getModelNode(prodType, XProductCmptGenerationClass.class, modelContext))
                .thenReturn(xProdGenClass);

        policyCmptClass.getProductCmptGenerationClassName();
        verify(xProdGenClass).getInterfaceName();
    }

    private IProductCmptType initProdType(XPolicyCmptClass policyCmptClass) {
        IPolicyCmptType polType = mock(IPolicyCmptType.class);
        when(policyCmptClass.getType()).thenReturn(polType);

        IProductCmptType prodType = mock(IProductCmptType.class);
        when(polType.findProductCmptType(any(IIpsProject.class))).thenReturn(prodType);
        return prodType;
    }

    @Test
    public void returnProdClassName() {
        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();
        IProductCmptType prodType = initProdType(policyCmptClass);

        XProductCmptClass xProdClass = mock(XProductCmptClass.class);
        when(xProdClass.getInterfaceName()).thenReturn("ProductName");
        when(modelService.getModelNode(prodType, XProductCmptClass.class, modelContext)).thenReturn(xProdClass);

        assertEquals("ProductName", policyCmptClass.getProductCmptClassName());
    }

    @Test
    public void testIsGenerateInitPropertiesFromXML() {
        XPolicyCmptClass policyCmptClass;
        policyCmptClass = setUpAttrList(true, true, true);
        assertTrue(policyCmptClass.isGenerateInitPropertiesFromXML());

        policyCmptClass = setUpAttrList(false, true, true);
        assertTrue(policyCmptClass.isGenerateInitPropertiesFromXML());

        policyCmptClass = setUpAttrList(false, true, false);
        assertTrue(policyCmptClass.isGenerateInitPropertiesFromXML());

        policyCmptClass = setUpAttrList(false, false, false);
        assertFalse(policyCmptClass.isGenerateInitPropertiesFromXML());

        policyCmptClass = spy(new XPolicyCmptClass(type, modelContext, modelService));
        Set<XPolicyAttribute> list = new HashSet<>();
        doReturn(list).when(policyCmptClass).getAttributes();
        assertFalse(policyCmptClass.isGenerateInitPropertiesFromXML());
    }

    private XPolicyCmptClass setUpAttrList(boolean init1, boolean init2, boolean init3) {
        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();
        Set<XPolicyAttribute> list = new HashSet<>();
        XPolicyAttribute attr1 = mock(XPolicyAttribute.class);
        XPolicyAttribute attr2 = mock(XPolicyAttribute.class);
        XPolicyAttribute attr3 = mock(XPolicyAttribute.class);
        when(attr1.isGenerateInitPropertiesFromXML()).thenReturn(init1);
        when(attr2.isGenerateInitPropertiesFromXML()).thenReturn(init2);
        when(attr3.isGenerateInitPropertiesFromXML()).thenReturn(init3);
        list.add(attr1);
        list.add(attr2);
        list.add(attr3);
        doReturn(list).when(policyCmptClass).getAttributes();
        return policyCmptClass;
    }

    @Test
    public void testFindDetailToMasterDerivedUnionAssociations() throws Exception {
        mock(XPolicyAssociation.class);
        setupAssociationList();
        when(associationNode2.isDerived()).thenReturn(true);

        XPolicyCmptClass xPolicyCmptClass = new XPolicyCmptClass(type, modelContext, modelService);
        xPolicyCmptClass.findDetailToMasterDerivedUnionAssociations(
                Arrays.asList(associationNode1, associationNode2));

        verify(associationNode1).getSubsettedDetailToMasterAssociations();
        verify(associationNode2, times(0)).getSubsettedDetailToMasterAssociations();
    }

    @Test
    public void testGetAttributesToInit_ChangingOverTimeWithProductData() {
        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();

        XPolicyAttribute a1 = mock(XPolicyAttribute.class);
        when(a1.isGenerateInitWithProductData()).thenReturn(true);
        when(a1.isOverwrite()).thenReturn(true);
        when(a1.isChangingOverTime()).thenReturn(true);

        XPolicyAttribute a2 = mock(XPolicyAttribute.class);

        XPolicyAttribute a3 = mock(XPolicyAttribute.class);
        when(a3.isChangingOverTime()).thenReturn(true);
        when(a3.isGenerateInitWithProductData()).thenReturn(true);

        doReturn(new HashSet<>(Arrays.asList(a1, a2, a3))).when(policyCmptClass).getAttributes();

        // Execute
        Set<XPolicyAttribute> result = policyCmptClass.getAttributesToInit(true, true);

        // Verify
        assertTrue(result.contains(a1));
        assertTrue(result.contains(a3));
        assertEquals(2, result.size());
    }

    public void testGetAttributesToInit_NotChangingOverTimeWithProductData() {
        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();

        XPolicyAttribute a1 = mock(XPolicyAttribute.class);
        when(a1.isGenerateInitWithProductData()).thenReturn(true);
        when(a1.isOverwrite()).thenReturn(true);
        when(a1.isChangingOverTime()).thenReturn(false);

        XPolicyAttribute a2 = mock(XPolicyAttribute.class);

        XPolicyAttribute a3 = mock(XPolicyAttribute.class);
        when(a3.isChangingOverTime()).thenReturn(false);
        when(a3.isGenerateInitWithProductData()).thenReturn(true);

        doReturn(new HashSet<>(Arrays.asList(a1, a2, a3))).when(policyCmptClass).getAttributes();

        // Execute
        Set<XPolicyAttribute> result = policyCmptClass.getAttributesToInit(true, false);

        // Verify
        assertTrue(result.contains(a1));
        assertTrue(result.contains(a3));
        assertEquals(2, result.size());
    }

    @Test
    public void testGetAttributesToInit_ChangingOverTimeWithoutProductDataAndOverwritten() {
        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();

        XPolicyAttribute a1 = mock(XPolicyAttribute.class);
        when(a1.isOverwrite()).thenReturn(true);

        XPolicyAttribute a2 = mock(XPolicyAttribute.class);
        when(a2.isOverwrite()).thenReturn(true);
        when(a2.isChangingOverTime()).thenReturn(true);
        when(a2.isGenerateInitWithProductData()).thenReturn(true);

        XPolicyAttribute a3 = mock(XPolicyAttribute.class);
        when(a3.isOverwrite()).thenReturn(true);
        when(a3.isChangingOverTime()).thenReturn(true);
        when(a3.isGenerateInitWithoutProductData()).thenReturn(true);

        doReturn(new HashSet<>(Arrays.asList(a1, a2, a3))).when(policyCmptClass).getAttributes();

        // Execute
        Set<XPolicyAttribute> result = policyCmptClass.getAttributesToInit(false, true);

        // Verify
        assertTrue(result.contains(a3));
        assertEquals(1, result.size());
    }

    @Test
    public void testGetAttributesToInit_NotChangingOverTimeWithoutProductDataAndOverwritten() {
        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();

        XPolicyAttribute a1 = mock(XPolicyAttribute.class);
        when(a1.isOverwrite()).thenReturn(true);

        XPolicyAttribute a2 = mock(XPolicyAttribute.class);
        when(a2.isOverwrite()).thenReturn(true);
        when(a2.isChangingOverTime()).thenReturn(false);
        when(a2.isGenerateInitWithProductData()).thenReturn(true);

        XPolicyAttribute a3 = mock(XPolicyAttribute.class);
        when(a3.isOverwrite()).thenReturn(true);
        when(a3.isChangingOverTime()).thenReturn(false);
        when(a3.isGenerateInitWithoutProductData()).thenReturn(true);

        doReturn(new HashSet<>(Arrays.asList(a1, a2, a3))).when(policyCmptClass).getAttributes();

        // Execute
        Set<XPolicyAttribute> result = policyCmptClass.getAttributesToInit(false, false);

        // Verify
        assertTrue(result.contains(a3));
        assertEquals(1, result.size());
    }

    @Test
    public void testIsGenerateAttributeInitCode_changingOverTime() {
        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();

        XPolicyAttribute a1 = mock(XPolicyAttribute.class);
        doReturn(new HashSet<>(Arrays.asList(a1))).when(policyCmptClass).getAttributesToInit(true,
                true);
        doReturn(Collections.emptySet()).when(policyCmptClass).getAttributesToInit(false, true);

        assertTrue(policyCmptClass.isGenerateAttributeInitCode(true));
    }

    @Test
    public void testIsGenerateAttributeInitCode_changingOverTime2() {
        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();

        doReturn(Collections.emptySet()).when(policyCmptClass).getAttributesToInit(true, true);
        doReturn(Collections.emptySet()).when(policyCmptClass).getAttributesToInit(false, true);

        assertFalse(policyCmptClass.isGenerateAttributeInitCode(true));
    }

    @Test
    public void testIsGenerateAttributeInitCode_static() {
        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();

        XPolicyAttribute a1 = mock(XPolicyAttribute.class);
        doReturn(Collections.emptySet()).when(policyCmptClass).getAttributesToInit(true, false);
        doReturn(new HashSet<>(Arrays.asList(a1))).when(policyCmptClass).getAttributesToInit(false,
                false);

        assertTrue(policyCmptClass.isGenerateAttributeInitCode(false));
    }

    @Test
    public void testIsGenerateAttributeInitCode_static2() {
        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();

        doReturn(Collections.emptySet()).when(policyCmptClass).getAttributesToInit(true, false);
        doReturn(Collections.emptySet()).when(policyCmptClass).getAttributesToInit(false, false);

        assertFalse(policyCmptClass.isGenerateAttributeInitCode(false));
    }

    @Test
    public void testGetBaseSuperclassName() {
        when(type.hasSupertype()).thenReturn(false);
        XPolicyCmptClass policyCmptClass = new XPolicyCmptClass(type, modelContext, modelService);
        when(generatorConfig.getBaseClassPolicyCmptType()).thenReturn("pack.MyBaseClass");
        when(modelContext.addImport("pack.MyBaseClass")).thenReturn("MyBaseClass");

        String baseSuperclassName = policyCmptClass.getBaseSuperclassName();

        assertEquals("MyBaseClass", baseSuperclassName);
        verify(modelContext).addImport("pack.MyBaseClass");
    }

    @Test
    public void testGetBaseSuperclassName_configuredPolicyCmptType() {
        when(type.hasSupertype()).thenReturn(false);
        when(type.isConfigurableByProductCmptType()).thenReturn(true);
        XPolicyCmptClass policyCmptClass = new XPolicyCmptClass(type, modelContext, modelService);
        when(generatorConfig.getBaseClassPolicyCmptType()).thenReturn("pack.MyBaseClass");
        when(modelContext.addImport("pack.MyBaseClass")).thenReturn("MyBaseClass");

        String baseSuperclassName = policyCmptClass.getBaseSuperclassName();

        assertEquals("MyBaseClass", baseSuperclassName);
        verify(modelContext).addImport("pack.MyBaseClass");
    }

    @Test
    public void testGetExtendedInterfaces() {
        when(type.hasSupertype()).thenReturn(false);
        XPolicyCmptClass policyCmptClass = new XPolicyCmptClass(type, modelContext, modelService);

        LinkedHashSet<String> extendedInterfaces = policyCmptClass.getExtendedInterfaces();

        assertEquals(1, extendedInterfaces.size());
        assertThat(extendedInterfaces, hasItem("IModelObject"));
    }

    @Test
    public void testGetExtendedInterfaces_configuredPolicyCmptType_changingOverTime() {
        when(productCmptType.isChangingOverTime()).thenReturn(true);

        when(type.hasSupertype()).thenReturn(false);
        when(type.isConfigurableByProductCmptType()).thenReturn(true);
        when(type.findProductCmptType(any(IIpsProject.class))).thenReturn(productCmptType);

        XPolicyCmptClass policyCmptClass = new XPolicyCmptClass(type, modelContext, modelService);
        LinkedHashSet<String> extendedInterfaces = policyCmptClass.getExtendedInterfaces();

        assertEquals(1, extendedInterfaces.size());
        assertThat(extendedInterfaces, hasItem("ITimedConfigurableModelObject"));
    }

    @Test
    public void testGetExtendedInterfaces_configuredPolicyCmptType_notChangingOverTime() {
        when(productCmptType.isChangingOverTime()).thenReturn(false);

        when(type.hasSupertype()).thenReturn(false);
        when(type.isConfigurableByProductCmptType()).thenReturn(true);
        when(type.findProductCmptType(any(IIpsProject.class))).thenReturn(productCmptType);

        XPolicyCmptClass policyCmptClass = new XPolicyCmptClass(type, modelContext, modelService);
        LinkedHashSet<String> extendedInterfaces = policyCmptClass.getExtendedInterfaces();

        assertEquals(1, extendedInterfaces.size());
        assertThat(extendedInterfaces, hasItem("IConfigurableModelObject"));
    }

    @Test
    public void testIsConfigured() {
        when(type.isConfigurableByProductCmptType()).thenReturn(true);
        XPolicyCmptClass policyCmptClass = new XPolicyCmptClass(type, modelContext, modelService);

        assertTrue(policyCmptClass.isConfigured());
    }

    @Test
    public void testHasConfiguredSupertype_NoSupertype() {
        when(type.hasSupertype()).thenReturn(false);
        XPolicyCmptClass policyCmptClass = new XPolicyCmptClass(type, modelContext, modelService);

        assertFalse(policyCmptClass.hasConfiguredSupertype());
    }

    @Test
    public void testHasConfiguredSupertype_NoConfiguredSupertype() {
        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();
        setUpReturnSupertype(policyCmptClass);
        when(superXType.isConfigured()).thenReturn(false);

        assertFalse(policyCmptClass.hasConfiguredSupertype());
    }

    @Test
    public void testHasConfiguredSupertype_WithConfiguredSupertype() {
        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();
        setUpReturnSupertype(policyCmptClass);
        when(superXType.isConfigured()).thenReturn(true);

        assertTrue(policyCmptClass.hasConfiguredSupertype());
    }

    @Test
    public void testIsFirstConfigurableInHierarchy_NoConfiguredSupertypeNotConfigured() {
        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();
        setUpReturnSupertype(policyCmptClass);
        when(superXType.isConfigured()).thenReturn(false);
        when(type.isConfigurableByProductCmptType()).thenReturn(false);

        assertFalse(policyCmptClass.isFirstConfigurableInHierarchy());
    }

    @Test
    public void testIsFirstConfigurableInHierarchy_NoConfiguredSupertypeConfigured() {
        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();
        setUpReturnSupertype(policyCmptClass);
        when(superXType.isConfigured()).thenReturn(false);
        when(type.isConfigurableByProductCmptType()).thenReturn(true);

        assertTrue(policyCmptClass.isFirstConfigurableInHierarchy());
    }

    @Test
    public void testIsFirstConfigurableInHierarchy_ConfiguredSupertypeNotConfigured() {
        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();
        setUpReturnSupertype(policyCmptClass);
        when(superXType.isConfigured()).thenReturn(true);
        when(type.isConfigurableByProductCmptType()).thenReturn(false);

        assertFalse(policyCmptClass.isFirstConfigurableInHierarchy());
    }

    @Test
    public void testIsFirstConfigurableInHierarchy_ConfiguredSupertypeConfigured() {
        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();
        setUpReturnSupertype(policyCmptClass);
        when(superXType.isConfigured()).thenReturn(true);
        when(type.isConfigurableByProductCmptType()).thenReturn(true);

        assertFalse(policyCmptClass.isFirstConfigurableInHierarchy());
    }

    @Test
    public void testGetImplementedInterfaces_WithSerializableSupportWithoutSupertype() {
        when(generatorConfig.isGenerateSerializablePolicyCmptSupport()).thenReturn(true);
        when(type.hasSupertype()).thenReturn(false);
        XPolicyCmptClass policyCmptClass = new XPolicyCmptClass(type, modelContext, modelService);

        LinkedHashSet<String> interfaces = policyCmptClass.getImplementedInterfaces();
        assertThat(interfaces, hasItem("Serializable"));
    }

    @Test
    public void testGetImplementedInterfaces_WithSerializableSupportWithSupertype() {
        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();
        when(generatorConfig.isGenerateSerializablePolicyCmptSupport()).thenReturn(true);
        when(type.hasSupertype()).thenReturn(true);
        when(type.getIpsProject()).thenReturn(ipsProject);
        when(generatorConfig.isGeneratePublishedInterfaces(ipsProject)).thenReturn(true);
        doReturn("TestInterface").when(policyCmptClass).getInterfaceName();

        LinkedHashSet<String> interfaces = policyCmptClass.getImplementedInterfaces();
        assertFalse(interfaces.contains("Serializable"));
    }

    @Test
    public void testGetImplementedInterfaces_WithoutSerializableSupportWithSupertype() {
        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();
        when(generatorConfig.isGenerateSerializablePolicyCmptSupport()).thenReturn(false);
        when(type.hasSupertype()).thenReturn(true);
        when(type.getIpsProject()).thenReturn(ipsProject);
        when(generatorConfig.isGeneratePublishedInterfaces(ipsProject)).thenReturn(true);
        doReturn("TestInterface").when(policyCmptClass).getInterfaceName();

        LinkedHashSet<String> interfaces = policyCmptClass.getImplementedInterfaces();
        assertFalse(interfaces.contains("Serializable"));
    }

    @Test
    public void testGetImplementedInterfaces_WithoutSerializableSupportWithoutSupertype() {
        when(generatorConfig.isGenerateSerializablePolicyCmptSupport()).thenReturn(false);
        when(type.hasSupertype()).thenReturn(false);
        XPolicyCmptClass policyCmptClass = new XPolicyCmptClass(type, modelContext, modelService);

        LinkedHashSet<String> interfaces = policyCmptClass.getImplementedInterfaces();
        assertFalse(interfaces.contains("Serializable"));
    }

    @Test
    public void testGetExtendedOrImplementedInterfaces_noInterfaces() {
        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();
        setUpReturnSupertype(policyCmptClass);
        when(superXType.isConfigured()).thenReturn(false);
        when(type.isConfigurableByProductCmptType()).thenReturn(false);

        LinkedHashSet<String> interfaces = policyCmptClass.getExtendedOrImplementedInterfaces();
        assertTrue(interfaces.isEmpty());
    }

    @Test
    public void testGetExtendedOrImplementedInterfaces_withAllInterfaces_changingOverTime() throws CoreRuntimeException {
        when(productCmptType.isChangingOverTime()).thenReturn(true);

        when(type.hasSupertype()).thenReturn(false);
        when(type.isDependantType()).thenReturn(true);
        when(type.isConfigurableByProductCmptType()).thenReturn(true);
        when(type.findProductCmptType(any(IIpsProject.class))).thenReturn(productCmptType);

        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();
        doReturn(superXType).when(policyCmptClass).getSupertype();
        when(superXType.isConfigured()).thenReturn(false);
        setUpGenerateSupport(true);

        LinkedHashSet<String> interfaces = policyCmptClass.getExtendedOrImplementedInterfaces();
        assertThat(interfaces, hasItems("INotificationSupport", "ICopySupport", "IDeltaSupport", "IVisitorSupport",
                "IDependantObject", "ITimedConfigurableModelObject"));
    }

    @Test
    public void testGetExtendedOrImplementedInterfaces_withAllInterfaces_notChangingOverTime() throws CoreRuntimeException {
        when(productCmptType.isChangingOverTime()).thenReturn(false);

        when(type.hasSupertype()).thenReturn(false);
        when(type.isDependantType()).thenReturn(true);
        when(type.isConfigurableByProductCmptType()).thenReturn(true);
        when(type.findProductCmptType(any(IIpsProject.class))).thenReturn(productCmptType);

        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();
        doReturn(superXType).when(policyCmptClass).getSupertype();
        when(superXType.isConfigured()).thenReturn(false);
        setUpGenerateSupport(true);

        LinkedHashSet<String> interfaces = policyCmptClass.getExtendedOrImplementedInterfaces();
        assertThat(interfaces, hasItems("INotificationSupport", "ICopySupport", "IDeltaSupport", "IVisitorSupport",
                "IDependantObject", "IConfigurableModelObject"));
    }

    @Test
    public void testGetExtendedOrImplementedInterfaces_DependantSupertype() throws CoreRuntimeException {
        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();
        when(type.hasSupertype()).thenReturn(false);
        when(type.isDependantType()).thenReturn(true);
        doReturn(superXType).when(policyCmptClass).getSupertype();
        when(superXType.isConfigured()).thenReturn(false);
        when(superXType.isDependantType()).thenReturn(true);
        when(superXType.isSupertypeDependantType()).thenReturn(false);
        when(type.isConfigurableByProductCmptType()).thenReturn(false);
        setUpGenerateSupport(false);

        LinkedHashSet<String> interfaces = policyCmptClass.getExtendedOrImplementedInterfaces();
        assertEquals(1, interfaces.size());
        assertThat(interfaces, hasItem("IDependantObject"));
    }

    @Test
    public void testIsGenerateGenerationAccessMethods_isChangingOverTime_true() throws Exception {
        IProductCmptType productCmptType = mock(IProductCmptType.class);
        when(type.getIpsProject()).thenReturn(ipsProject);
        when(type.findProductCmptType(ipsProject)).thenReturn(productCmptType);
        when(productCmptType.isChangingOverTime()).thenReturn(true);

        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();

        assertTrue(policyCmptClass.isGenerateGenerationAccessMethods());
    }

    @Test
    public void testIsGenerateGenerationAccessMethods_isChangingOverTime_false() throws Exception {
        IProductCmptType productCmptType = mock(IProductCmptType.class);
        when(type.getIpsProject()).thenReturn(ipsProject);
        when(type.findProductCmptType(ipsProject)).thenReturn(productCmptType);
        when(productCmptType.isChangingOverTime()).thenReturn(false);

        XPolicyCmptClass policyCmptClass = createXPolicyCmptClassSpy();

        assertFalse(policyCmptClass.isGenerateGenerationAccessMethods());
    }

    private XPolicyCmptClass createXPolicyCmptClassSpy() {
        return spy(new XPolicyCmptClass(type, modelContext, modelService));
    }

    private void setUpReturnSupertype(XPolicyCmptClass policyCmptClass) {
        when(type.hasSupertype()).thenReturn(true);
        doReturn(superXType).when(policyCmptClass).getSupertype();
    }

    private void setUpGenerateSupport(boolean returnValue) {
        when(generatorConfig.isGenerateChangeSupport()).thenReturn(returnValue);
        when(generatorConfig.isGenerateCopySupport()).thenReturn(returnValue);
        when(generatorConfig.isGenerateDeltaSupport()).thenReturn(returnValue);
        when(generatorConfig.isGenerateVisitorSupport()).thenReturn(returnValue);
    }
}
