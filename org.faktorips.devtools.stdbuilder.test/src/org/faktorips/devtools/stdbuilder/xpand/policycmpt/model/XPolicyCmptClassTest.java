/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.xpand.policycmpt.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelCaches;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptClass;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptGenerationClass;
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
    private ModelService modelService;

    @Mock
    private IPolicyCmptType type;

    @Mock
    private XPolicyAttribute attributeNode1;

    @Mock
    private XPolicyAttribute attributeNode2;

    @Mock
    private XPolicyAssociation associationNode1;

    @Mock
    private XPolicyAssociation associationNode2;

    @Before
    public void initModelContext() {
        GeneratorModelCaches generatorModelCache = new GeneratorModelCaches();
        when(modelContext.getGeneratorModelCache()).thenReturn(generatorModelCache);
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
        List<IPolicyCmptTypeAttribute> attrList = new ArrayList<IPolicyCmptTypeAttribute>();
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
        List<IPolicyCmptTypeAssociation> assocList = new ArrayList<IPolicyCmptTypeAssociation>();
        assocList.add(assoc1);
        assocList.add(assoc2);

        doReturn(associationNode1).when(modelService).getModelNode(assoc1, XPolicyAssociation.class, modelContext);
        doReturn(associationNode2).when(modelService).getModelNode(assoc2, XPolicyAssociation.class, modelContext);
        when(type.getAssociations()).thenReturn(new ArrayList<IAssociation>(assocList));
        when(type.getPolicyCmptTypeAssociations()).thenReturn(assocList);
    }

    @Test
    public void returnProdGenerationClassName() throws CoreException {
        XPolicyCmptClass policyCmptClass = spy(new XPolicyCmptClass(type, modelContext, modelService));
        IProductCmptType prodType = initProdType(policyCmptClass);

        XProductCmptGenerationClass xProdGenClass = mock(XProductCmptGenerationClass.class);
        when(modelService.getModelNode(prodType, XProductCmptGenerationClass.class, modelContext)).thenReturn(
                xProdGenClass);

        policyCmptClass.getProductCmptGenerationClassName();
        verify(xProdGenClass).getInterfaceName();
    }

    private IProductCmptType initProdType(XPolicyCmptClass policyCmptClass) throws CoreException {
        IPolicyCmptType polType = mock(IPolicyCmptType.class);
        when(policyCmptClass.getType()).thenReturn(polType);

        IProductCmptType prodType = mock(IProductCmptType.class);
        when(polType.findProductCmptType(any(IIpsProject.class))).thenReturn(prodType);
        return prodType;
    }

    @Test
    public void returnProdClassName() throws CoreException {
        XPolicyCmptClass policyCmptClass = spy(new XPolicyCmptClass(type, modelContext, modelService));
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
        Set<XPolicyAttribute> list = new HashSet<XPolicyAttribute>();
        doReturn(list).when(policyCmptClass).getAttributes();
        assertFalse(policyCmptClass.isGenerateInitPropertiesFromXML());
    }

    private XPolicyCmptClass setUpAttrList(boolean init1, boolean init2, boolean init3) {
        XPolicyCmptClass policyCmptClass = spy(new XPolicyCmptClass(type, modelContext, modelService));
        Set<XPolicyAttribute> list = new HashSet<XPolicyAttribute>();
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
        xPolicyCmptClass.findDetailToMasterDerivedUnionAssociations(Arrays.asList(new XPolicyAssociation[] {
                associationNode1, associationNode2 }));

        verify(associationNode1).getSubsettedDetailToMasterAssociations();
        verify(associationNode2, times(0)).getSubsettedDetailToMasterAssociations();
    }

    @Test
    public void testGetAttributesToInitWithProductData() {
        // Set up
        XPolicyCmptClass policyCmptClass = spy(new XPolicyCmptClass(type, modelContext, modelService));

        XPolicyAttribute a1 = mock(XPolicyAttribute.class);
        when(a1.isGenerateInitWithProductData()).thenReturn(true);
        when(a1.isOverwrite()).thenReturn(true);

        XPolicyAttribute a2 = mock(XPolicyAttribute.class);

        XPolicyAttribute a3 = mock(XPolicyAttribute.class);
        when(a3.isGenerateInitWithProductData()).thenReturn(true);

        doReturn(new HashSet<XPolicyAttribute>(Arrays.asList(a1, a2, a3))).when(policyCmptClass).getAttributes();

        // Execute
        Set<XPolicyAttribute> result = policyCmptClass.getAttributesToInitWithProductData();

        // Verify
        assertTrue(result.contains(a1));
        assertTrue(result.contains(a3));
        assertEquals(2, result.size());
    }

    @Test
    public void testGetAttributesToInitWithoutProductDataAndOverwritten() {
        // Set up
        XPolicyCmptClass policyCmptClass = spy(new XPolicyCmptClass(type, modelContext, modelService));

        XPolicyAttribute a1 = mock(XPolicyAttribute.class);
        when(a1.isOverwrite()).thenReturn(true);

        XPolicyAttribute a2 = mock(XPolicyAttribute.class);
        when(a2.isOverwrite()).thenReturn(true);
        when(a2.isGenerateInitWithProductData()).thenReturn(true);

        XPolicyAttribute a3 = mock(XPolicyAttribute.class);
        when(a3.isOverwrite()).thenReturn(true);
        when(a3.isGenerateInitWithoutProductData()).thenReturn(true);

        doReturn(new HashSet<XPolicyAttribute>(Arrays.asList(a1, a2, a3))).when(policyCmptClass).getAttributes();

        // Execute
        Set<XPolicyAttribute> result = policyCmptClass.getAttributesToInitWithoutProductDataAndOverwritten();

        // Verify
        assertTrue(result.contains(a3));
        assertEquals(1, result.size());
    }

}
