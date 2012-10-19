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

package org.faktorips.devtools.stdbuilder.xpand.productcmpt.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.junit.matchers.JUnitMatchers.hasItems;
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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelCaches;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAttribute;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass;
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
    public void createTypes() throws CoreException {
        when(type.getIpsProject()).thenReturn(ipsProject);
        when(superType.getIpsProject()).thenReturn(ipsProject);
        when(superSuperType.getIpsProject()).thenReturn(ipsProject);
        when(type.findSupertype(any(IIpsProject.class))).thenReturn(superType);
        when(superType.findSupertype(any(IIpsProject.class))).thenReturn(superSuperType);
    }

    @Before
    public void setUpAssociations() {
        List<IProductCmptTypeAssociation> assocList = new ArrayList<IProductCmptTypeAssociation>();
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
        List<IProductCmptTypeAttribute> attrList = new ArrayList<IProductCmptTypeAttribute>();
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
    }

    @Test
    public void getChangableProductAttributes() {
        XProductCmptClass productClass = new XProductCmptClass(type, modelContext, modelService);
        Set<XProductAttribute> attributes = productClass.getAttributes();
        assertEquals(1, attributes.size());
        assertThat(attributes, hasItem(attrNode2));
    }

    @Test
    public void getStaticProductAttributes() {
        XProductClass productClass = new XProductCmptGenerationClass(type, modelContext, modelService);
        Set<XProductAttribute> attributes = productClass.getAttributes();
        assertEquals(2, attributes.size());
        assertThat(attributes, hasItems(attrNode1, attrNode3));
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
    public void testGetPolicyClassName() throws Exception {
        when(type.findPolicyCmptType(ipsProject)).thenReturn(policyType);
        when(modelService.getModelNode(policyType, XPolicyCmptClass.class, modelContext)).thenReturn(xPolicyCmpt);

        when(xPolicyCmpt.getSimpleName(BuilderAspect.INTERFACE)).thenReturn("IPolicyCmpt");
        when(xPolicyCmpt.getSimpleName(BuilderAspect.IMPLEMENTATION)).thenReturn("PolicyCmpt");

        assertEquals("IPolicyCmpt", xProductClass.getPolicyClassName(BuilderAspect.INTERFACE));
        assertEquals("PolicyCmpt", xProductClass.getPolicyClassName(BuilderAspect.IMPLEMENTATION));

        assertEquals("IPolicyCmpt", xProductClass.getPolicyInterfaceName());
        assertEquals("PolicyCmpt", xProductClass.getPolicyImplClassName());
    }

    @Test
    public void testIsContainsNotDerivedAssociations_noAssociation() throws Exception {
        doReturn(new HashSet<XProductAssociation>()).when(xProductClass).getAssociations();
        assertFalse(xProductClass.isContainsNotDerivedAssociations());
    }

    @Test
    public void testIsContainsNotDerivedAssociations_someAssociation() throws Exception {
        when(assocNode1.isDerived()).thenReturn(true);
        when(assocNode2.isOneToMany()).thenReturn(true);

        HashSet<XProductAssociation> associations = new HashSet<XProductAssociation>();
        associations.add(assocNode1);
        associations.add(assocNode2);
        doReturn(associations).when(xProductClass).getAssociations();
        assertTrue(xProductClass.isContainsNotDerivedAssociations());
    }

    @Test
    public void testGetConfiguredAttributes() throws Exception {
        when(xProductClass.getType()).thenReturn(type);
        doReturn(true).when(xProductClass).isChangeOverTimeClass();
        doReturn(xPolicyCmpt).when(xProductClass).getPolicyCmptClass();

        assertTrue(xProductClass.getConfiguredAttributesInternal().isEmpty());

        when(xProductClass.isConfigurationForPolicyCmptType()).thenReturn(true);

        assertTrue(xProductClass.getConfiguredAttributesInternal().isEmpty());

        when(xProductClass.isConfigurationForPolicyCmptType()).thenReturn(true);

        when(polAttrNode1.isProductRelevant()).thenReturn(true);
        when(polAttrNode1.isGenerateGetAllowedValuesFor()).thenReturn(true);
        when(polAttrNode2.isProductRelevant()).thenReturn(true);

        Set<XPolicyAttribute> associations = new LinkedHashSet<XPolicyAttribute>();
        associations.add(polAttrNode1);
        associations.add(polAttrNode2);
        associations.add(polAttrNode3);

        when(xPolicyCmpt.getAttributes()).thenReturn(associations);

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

}
