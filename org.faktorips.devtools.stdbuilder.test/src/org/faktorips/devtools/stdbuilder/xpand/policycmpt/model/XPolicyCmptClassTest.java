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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptClass;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptGenerationClass;
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

    private XPolicyAttribute attributeNode1;
    private XPolicyAttribute attributeNode2;
    private XPolicyAssociation associationNode1;
    private XPolicyAssociation associationNode2;

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
        attributeNode1 = mock(XPolicyAttribute.class);
        attributeNode2 = mock(XPolicyAttribute.class);
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
    public void initAssociations() {
        setupAssociationList();

        XPolicyCmptClass policyCmptClass = new XPolicyCmptClass(type, modelContext, modelService);
        Set<XPolicyAssociation> associationNodeSet = policyCmptClass.getAssociations();
        assertEquals(2, associationNodeSet.size());
        assertThat(associationNodeSet, hasItems(associationNode1, associationNode2));
    }

    @Test
    public void initAssociationList() {
        setupAssociationList();

        XPolicyCmptClass policyCmptClass = new XPolicyCmptClass(type, modelContext, modelService);
        Set<XPolicyAssociation> assocList = policyCmptClass.getAssociations();
        Set<XPolicyAssociation> secondAssocList = policyCmptClass.getAssociations();
        // returns copies of the same list
        assertNotSame(assocList, secondAssocList);
        assertEquals(assocList, secondAssocList);
    }

    private void setupAssociationList() {
        associationNode1 = mock(XPolicyAssociation.class);
        associationNode2 = mock(XPolicyAssociation.class);
        IPolicyCmptTypeAssociation assoc1 = mock(IPolicyCmptTypeAssociation.class);
        IPolicyCmptTypeAssociation assoc2 = mock(IPolicyCmptTypeAssociation.class);
        List<IPolicyCmptTypeAssociation> assocList = new ArrayList<IPolicyCmptTypeAssociation>();
        assocList.add(assoc1);
        assocList.add(assoc2);

        doReturn(associationNode1).when(modelService).getModelNode(assoc1, XPolicyAssociation.class, modelContext);
        doReturn(associationNode2).when(modelService).getModelNode(assoc2, XPolicyAssociation.class, modelContext);
        when(type.getPolicyCmptTypeAssociations()).thenReturn(assocList);
    }

    @Test
    public void returnProdGenerationClassName() throws CoreException {
        XPolicyCmptClass policyCmptClass = spy(new XPolicyCmptClass(type, modelContext, modelService));
        IProductCmptType prodType = initProdType(policyCmptClass);

        XProductCmptGenerationClass xProdGenClass = mock(XProductCmptGenerationClass.class);
        when(modelService.getModelNode(prodType, XProductCmptGenerationClass.class, modelContext)).thenReturn(
                xProdGenClass);

        policyCmptClass.getProductGenerationClassName();
        verify(xProdGenClass).getSimpleName(BuilderAspect.IMPLEMENTATION);
    }

    private IProductCmptType initProdType(XPolicyCmptClass policyCmptClass) throws CoreException {
        IPolicyCmptType polType = mock(IPolicyCmptType.class);
        when(policyCmptClass.getPolicyCmptType()).thenReturn(polType);

        IProductCmptType prodType = mock(IProductCmptType.class);
        when(polType.findProductCmptType(any(IIpsProject.class))).thenReturn(prodType);
        return prodType;
    }

    @Test
    public void returnProdClassName() throws CoreException {
        XPolicyCmptClass policyCmptClass = spy(new XPolicyCmptClass(type, modelContext, modelService));
        IProductCmptType prodType = initProdType(policyCmptClass);

        XProductCmptClass xProdClass = mock(XProductCmptClass.class);
        when(xProdClass.getSimpleName(BuilderAspect.IMPLEMENTATION)).thenReturn("ProductName");
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

}
