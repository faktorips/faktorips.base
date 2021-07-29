/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.SingletonMockHelper;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.ipsproject.properties.IpsProjectProperties;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.IType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NewProductCmptPMOTest extends AbstractIpsPluginTest {

    private static final String PROJECT_NAME = "projectName";

    private SingletonMockHelper singletonMockHelper;

    private IpsModel ipsModel;

    private IIpsProject ipsProject;

    private NewProductCmptPMO pmo = new NewProductCmptPMO();

    @Override
    @Before
    public void setUp() {
        ipsProject = spy(newIpsProject());
        IpsPlugin ipsPlugin = mock(IpsPlugin.class);
        IpsPreferences preferences = mock(IpsPreferences.class);
        ipsModel = mock(IpsModel.class);
        singletonMockHelper = new SingletonMockHelper();
        singletonMockHelper.setSingletonInstance(IpsPlugin.class, ipsPlugin);
        singletonMockHelper.setSingletonInstance(IpsModel.class, ipsModel);
        doReturn(preferences).when(ipsPlugin).getIpsPreferences();
    }

    @Test
    public void testIsFirstPageNeeded_NotNeededWhenAddingToAssociation() {
        singletonMockHelper.reset();

        pmo.setAddToAssociation(mock(IProductCmptGeneration.class), mock(IProductCmptTypeAssociation.class));

        assertFalse(pmo.isFirstPageNeeded());
    }

    @Test
    public void testIsFirstPageNeeded_NotNeededWhenCopyingValidProductCmpt() {
        singletonMockHelper.reset();

        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductCmptType");
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "ProductCmpt");

        pmo.setIpsProject(ipsProject);
        pmo.setCopyProductCmpt(productCmpt);

        assertFalse(pmo.isFirstPageNeeded());
    }

    @Test
    public void testIsFirstPageNeeded_NeededWhenNeitherCopyingProductCmptNorAddingToAssociation() {
        assertTrue(new NewProductCmptPMO().isFirstPageNeeded());
    }

    @Test
    public void testIsFirstPageNeeded_NeededWhenCopyingProductCmptButTypeNotFound() {
        singletonMockHelper.reset();

        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductCmptType");
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "ProductCmpt");
        productCmpt.setProductCmptType("notExistingType");

        pmo.setIpsProject(ipsProject);
        pmo.setCopyProductCmpt(productCmpt);

        assertTrue(pmo.isFirstPageNeeded());
    }

    @Test
    public void testGetSubtypes_OnlyShowSelectedTypeWhenCopyingValidProductCmpt() {
        singletonMockHelper.reset();

        IProductCmptType baseType = newProductCmptType(ipsProject, "BaseType");
        baseType.setAbstract(true);
        IProductCmptType productCmptType1 = newProductCmptType(baseType, "ProductCmptType1");
        IProductCmptType productCmptType2 = newProductCmptType(baseType, "ProductCmptType2");
        IProductCmpt productCmpt = newProductCmpt(productCmptType1, "ProductCmpt");

        pmo.setIpsProject(ipsProject);
        pmo.setCopyProductCmpt(productCmpt);

        assertTrue(pmo.getSubtypes().contains(productCmptType1));
        assertFalse(pmo.getSubtypes().contains(productCmptType2));
        assertEquals(1, pmo.getSubtypes().size());
    }

    @Test
    public void testGetSubtypes_ShowAllSubtypesWhenCopyingProductCmptForWhichTypeIsNotFound() {
        singletonMockHelper.reset();

        IProductCmptType baseType = newProductCmptType(ipsProject, "BaseType");
        baseType.setAbstract(true);
        IProductCmptType productCmptType1 = newProductCmptType(baseType, "ProductCmptType1");
        IProductCmptType productCmptType2 = newProductCmptType(baseType, "ProductCmptType2");
        IProductCmpt productCmpt = newProductCmpt(productCmptType1, "ProductCmpt");
        productCmpt.setProductCmptType("notExistingType");

        pmo.setIpsProject(ipsProject);
        pmo.setCopyProductCmpt(productCmpt);
        pmo.setSelectedBaseType(baseType);

        assertTrue(pmo.getSubtypes().contains(productCmptType1));
        assertTrue(pmo.getSubtypes().contains(productCmptType2));
        assertEquals(2, pmo.getSubtypes().size());
    }

    @Test(expected = IllegalStateException.class)
    public void testSetCopyProductCmpt_IpsProjectNotSet() {
        singletonMockHelper.reset();
        pmo.setCopyProductCmpt(mock(IProductCmpt.class));
    }

    @Test
    public void testSetCopyProductCmpt() {
        singletonMockHelper.reset();

        // Create a small test model
        IProductCmptType layerSupertype = newProductCmptType(ipsProject, "LayerSupertype");
        layerSupertype.setLayerSupertype(true);
        IProductCmptType baseType = newProductCmptType(ipsProject, "BaseType");
        baseType.setSupertype(layerSupertype.getQualifiedName());
        IProductCmptType concreteType = newProductCmptType(ipsProject, "ConcreteType");
        concreteType.setSupertype(baseType.getQualifiedName());

        IProductCmpt template = newProductTemplate(concreteType, "Template");

        // Create the product component to copy
        IProductCmpt productCmptToCopy = newProductCmpt(concreteType, "ProductToCopy");
        productCmptToCopy.setTemplate(template.getQualifiedName());

        // Configure PMO to copy product component
        pmo.setIpsProject(ipsProject);
        pmo.setCopyProductCmpt(productCmptToCopy);

        // Base product component type and product component type should be selected accordingly
        assertThat(pmo.isCopyMode(), is(true));
        assertThat(pmo.isSingleTypeSelection(), is(true));
        assertThat(pmo.getSelectedBaseType(), is(baseType));
        assertThat(pmo.getSelectedType(), is(concreteType));
        assertThat(pmo.getSelectedTemplateAsProductCmpt(), is(template));
        assertThat(pmo.getCopyProductCmpt(), is(productCmptToCopy));
    }

    @Test
    public void testSetCopyProductCmpt_ProductCmptTypeIsNull() {
        singletonMockHelper.reset();

        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductCmptType");
        IProductCmpt productCmptToCopy = newProductCmpt(productCmptType, "ProductToCopy");
        productCmptToCopy.setProductCmptType("notExistent");

        pmo.setIpsProject(ipsProject);
        pmo.setCopyProductCmpt(productCmptToCopy);

        assertTrue(pmo.isCopyMode());
        assertFalse(pmo.isSingleTypeSelection());
        assertEquals(productCmptToCopy, pmo.getCopyProductCmpt());

        assertNull(pmo.getSelectedBaseType());
        assertNull(pmo.getSelectedType());
        assertTrue(pmo.getBaseTypes().contains(productCmptType));
        assertEquals(1, pmo.getBaseTypes().size());
    }

    @Test
    public void testSetCopyProductCmpt_InitializeNameWithValidResourceName() {
        singletonMockHelper.reset();

        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductCmptType");
        IProductCmpt productCmptToCopy = newProductCmpt(productCmptType, "ProductToCopy");
        productCmptToCopy.setProductCmptType("notExistent");
        productCmptToCopy.getIpsSrcFile().save(null);

        pmo.setIpsProject(ipsProject);
        pmo.setCopyProductCmpt(productCmptToCopy);

        assertEquals(productCmptToCopy.getName(), pmo.getName());
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IProductCmpt} is copied, but the user selects a different target
     * {@link IIpsProject}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The runtime id should be updated according to the {@link IProductCmptNamingStrategy} of the
     * target {@link IIpsProject}.
     */
    @Test
    public void testSetIpsProject_UpdateRuntimeId() {
        singletonMockHelper.reset();

        IProductCmptType productCmptType = newProductCmptType(ipsProject, "Type");
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "Product");

        pmo.setIpsProject(ipsProject);
        pmo.setCopyProductCmpt(productCmpt);

        IIpsProject otherIpsProject = mock(IIpsProject.class);
        IIpsPackageFragmentRoot packageRoot = mock(IIpsPackageFragmentRoot.class);
        when(otherIpsProject.getSourceIpsPackageFragmentRoots())
                .thenReturn(new IIpsPackageFragmentRoot[] { packageRoot });
        when(otherIpsProject.findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE)).thenReturn(new IIpsSrcFile[0]);
        IProductCmptNamingStrategy productCmptNamingStrategy = mockProductCmptNamingStrategy(otherIpsProject);
        when(productCmptNamingStrategy.getUniqueRuntimeId(any(IIpsProject.class), anyString()))
                .thenReturn("Foo_Product");

        pmo.setIpsProject(otherIpsProject);

        assertEquals("Foo_Product", pmo.getRuntimeId());
    }

    private IProductCmptNamingStrategy mockProductCmptNamingStrategy(IIpsProject otherIpsProject) {
        IProductCmptNamingStrategy productCmptNamingStrategy = mock(IProductCmptNamingStrategy.class);
        when(otherIpsProject.getProductCmptNamingStrategy()).thenReturn(productCmptNamingStrategy);
        return productCmptNamingStrategy;
    }

    @Test
    public void testUpdateBaseTypeList_noProject() {
        IIpsPackageFragmentRoot ipsPackageFragmentRoot = mockPackageFragmentRoot();

        pmo.setPackageRoot(ipsPackageFragmentRoot);

        assertTrue(pmo.getBaseTypes().isEmpty());
    }

    private IIpsPackageFragmentRoot mockPackageFragmentRoot() {
        IIpsPackageFragmentRoot ipsPackageFragmentRoot = mock(IIpsPackageFragmentRoot.class);
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(ipsPackageFragmentRoot.getIpsProject()).thenReturn(ipsProject);
        when(ipsProject.findIpsSrcFiles(any(IpsObjectType.class))).thenReturn(new IIpsSrcFile[0]);
        mockProductCmptNamingStrategy(ipsProject);
        when(ipsProject.getSourceIpsPackageFragmentRoots())
                .thenReturn(new IIpsPackageFragmentRoot[] { ipsPackageFragmentRoot });
        IIpsProjectProperties properties = new IpsProjectProperties(ipsProject);
        when(ipsProject.getReadOnlyProperties()).thenReturn(properties);
        return ipsPackageFragmentRoot;
    }

    @Test
    public void testUpdateBaseTypeList_withProject() throws Exception {
        pmo.setEffectiveDate(new GregorianCalendar());

        IIpsPackageFragmentRoot packageFragmentRoot = mockPackageFragmentRoot();
        IIpsProject ipsProject = packageFragmentRoot.getIpsProject();

        ArrayList<IIpsSrcFile> ipsSrcFiles = new ArrayList<>();

        when(ipsProject.findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE))
                .thenReturn(ipsSrcFiles.toArray(new IIpsSrcFile[0]));
        when(ipsProject.getName()).thenReturn(PROJECT_NAME);

        when(ipsModel.getIpsProject(PROJECT_NAME)).thenReturn(ipsProject);

        pmo.setIpsProject(ipsProject);
        assertTrue(pmo.getBaseTypes().isEmpty());

        IIpsSrcFile ipsSrcFile1 = mock(IIpsSrcFile.class);
        IProductCmptType productCmptType1 = mock(IProductCmptType.class);
        doReturn(ipsProject).when(productCmptType1).getIpsProject();
        when(productCmptType1.getName()).thenReturn("1");
        IIpsSrcFile ipsSrcFile2 = mock(IIpsSrcFile.class);
        IProductCmptType productCmptType2 = mock(IProductCmptType.class);
        when(productCmptType2.getName()).thenReturn("2");
        doReturn(ipsProject).when(productCmptType2).getIpsProject();
        IIpsSrcFile ipsSrcFile3 = mock(IIpsSrcFile.class);
        IProductCmptType productCmptType3 = mock(IProductCmptType.class);
        when(productCmptType3.getName()).thenReturn("3");
        doReturn(ipsProject).when(productCmptType3).getIpsProject();
        ipsSrcFiles.add(ipsSrcFile1);
        ipsSrcFiles.add(ipsSrcFile2);
        ipsSrcFiles.add(ipsSrcFile3);
        when(ipsProject.findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE))
                .thenReturn(ipsSrcFiles.toArray(new IIpsSrcFile[0]));

        when(ipsSrcFile1.getPropertyValue(IProductCmptType.PROPERTY_LAYER_SUPERTYPE)).thenReturn("true");
        when(ipsSrcFile1.getIpsObject()).thenReturn(productCmptType1);
        when(ipsSrcFile2.getPropertyValue(IProductCmptType.PROPERTY_LAYER_SUPERTYPE)).thenReturn("false");
        when(ipsSrcFile2.getIpsObject()).thenReturn(productCmptType2);
        when(ipsSrcFile3.getPropertyValue(IProductCmptType.PROPERTY_LAYER_SUPERTYPE)).thenReturn(null);
        when(ipsSrcFile3.getIpsObject()).thenReturn(productCmptType3);

        when(ipsSrcFile3.getPropertyValue(IType.PROPERTY_SUPERTYPE)).thenReturn("findSuperType");

        when(ipsProject.findIpsSrcFile(new QualifiedNameType("findSuperType", IpsObjectType.PRODUCT_CMPT_TYPE)))
                .thenReturn(ipsSrcFile1);

        // refresh the list
        pmo.setIpsProject(ipsProject);

        Set<IProductCmptType> baseTypes = pmo.getBaseTypes();
        assertEquals(2, baseTypes.size());
        assertTrue(baseTypes.contains(productCmptType2));
        assertTrue(baseTypes.contains(productCmptType3));
    }

    /**
     * Test for FIPS-899 <strong>Scenario:</strong><br>
     * There are abstract product component types that do not have concrete types in the project.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Types without concrete type in project scope should not be in the list of base types
     */
    @Test
    public void testUpdateBaseTypeList_filterTypesWithoutConcreteTypes() throws Exception {
        pmo.setEffectiveDate(new GregorianCalendar());

        IIpsPackageFragmentRoot packageFragmentRoot = mockPackageFragmentRoot();
        IIpsProject ipsProject = packageFragmentRoot.getIpsProject();

        ArrayList<IIpsSrcFile> ipsSrcFiles = new ArrayList<>();

        doReturn(ipsSrcFiles.toArray(new IIpsSrcFile[0])).when(ipsProject)
                .findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE);
        doReturn(PROJECT_NAME).when(ipsProject).getName();

        doReturn(ipsProject).when(ipsModel).getIpsProject(PROJECT_NAME);

        pmo.setIpsProject(ipsProject);
        assertTrue(pmo.getBaseTypes().isEmpty());

        IIpsSrcFile ipsSrcFile1 = mock(IIpsSrcFile.class);
        IProductCmptType productCmptType1 = mock(IProductCmptType.class);
        doReturn("1").when(productCmptType1).getName();
        doReturn(ipsProject).when(productCmptType1).getIpsProject();
        IIpsSrcFile ipsSrcFile2 = mock(IIpsSrcFile.class);
        IProductCmptType productCmptType2 = mock(IProductCmptType.class);
        doReturn("1").when(productCmptType2).getName();
        doReturn(ipsProject).when(productCmptType2).getIpsProject();
        IIpsSrcFile ipsSrcFile3 = mock(IIpsSrcFile.class);
        IProductCmptType productCmptType3 = mock(IProductCmptType.class);
        doReturn("1").when(productCmptType3).getName();
        doReturn(ipsProject).when(productCmptType3).getIpsProject();
        ipsSrcFiles.add(ipsSrcFile1);
        ipsSrcFiles.add(ipsSrcFile2);
        ipsSrcFiles.add(ipsSrcFile3);
        doReturn(ipsSrcFiles.toArray(new IIpsSrcFile[0])).when(ipsProject)
                .findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE);

        doReturn("true").when(ipsSrcFile1).getPropertyValue(IProductCmptType.PROPERTY_LAYER_SUPERTYPE);
        doReturn(productCmptType1).when(ipsSrcFile1).getIpsObject();
        doReturn("false").when(ipsSrcFile2).getPropertyValue(IProductCmptType.PROPERTY_LAYER_SUPERTYPE);
        doReturn("true").when(ipsSrcFile2).getPropertyValue(IType.PROPERTY_ABSTRACT);
        doReturn(productCmptType2).when(ipsSrcFile2).getIpsObject();
        doReturn(null).when(ipsSrcFile3).getPropertyValue(IProductCmptType.PROPERTY_LAYER_SUPERTYPE);
        doReturn(productCmptType3).when(ipsSrcFile3).getIpsObject();

        doReturn("findSuperType").when(ipsSrcFile3).getPropertyValue(IType.PROPERTY_SUPERTYPE);

        doReturn(ipsSrcFile1).when(ipsProject)
                .findIpsSrcFile(new QualifiedNameType("findSuperType", IpsObjectType.PRODUCT_CMPT_TYPE));
        // refresh the list
        pmo.setIpsProject(ipsProject);

        assertEquals(1, pmo.getBaseTypes().size());
        assertTrue(pmo.getBaseTypes().contains(productCmptType3));
    }

    @Test
    public void testUpdateBaseTypeList_includeAbstractTypesForTemplates() throws Exception {
        pmo.setEffectiveDate(new GregorianCalendar());

        IIpsPackageFragmentRoot packageFragmentRoot = mockPackageFragmentRoot();
        IIpsProject ipsProject = packageFragmentRoot.getIpsProject();

        ArrayList<IIpsSrcFile> ipsSrcFiles = new ArrayList<>();

        doReturn(ipsSrcFiles.toArray(new IIpsSrcFile[0])).when(ipsProject)
                .findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE);
        doReturn(PROJECT_NAME).when(ipsProject).getName();

        doReturn(ipsProject).when(ipsModel).getIpsProject(PROJECT_NAME);

        pmo.setIpsProject(ipsProject);
        assertTrue(pmo.getBaseTypes().isEmpty());

        IIpsSrcFile layerSupertypeIpsSrcFile = mock(IIpsSrcFile.class);
        IProductCmptType layerSupertypeProductCmptType = mock(IProductCmptType.class);
        doReturn("layerSupertype").when(layerSupertypeProductCmptType).getName();
        doReturn(layerSupertypeProductCmptType).when(layerSupertypeIpsSrcFile).getIpsObject();
        doReturn("true").when(layerSupertypeIpsSrcFile).getPropertyValue(IProductCmptType.PROPERTY_LAYER_SUPERTYPE);
        doReturn(ipsProject).when(layerSupertypeProductCmptType).getIpsProject();

        IIpsSrcFile abstractIpsSrcFile = mock(IIpsSrcFile.class);
        IProductCmptType abstractProductCmptType = mock(IProductCmptType.class);
        doReturn("abstract").when(abstractProductCmptType).getName();
        doReturn("false").when(abstractIpsSrcFile).getPropertyValue(IProductCmptType.PROPERTY_LAYER_SUPERTYPE);
        doReturn("true").when(abstractIpsSrcFile).getPropertyValue(IType.PROPERTY_ABSTRACT);
        doReturn(abstractProductCmptType).when(abstractIpsSrcFile).getIpsObject();
        doReturn(ipsProject).when(abstractProductCmptType).getIpsProject();

        IIpsSrcFile concreteIpsSrcFile = mock(IIpsSrcFile.class);
        IProductCmptType concreteProductCmptType = mock(IProductCmptType.class);
        doReturn("concrete").when(concreteProductCmptType).getName();
        doReturn(ipsProject).when(concreteProductCmptType).getIpsProject();
        // Older types may not have the layer supertype property. This is handled as false.
        doReturn(null).when(concreteIpsSrcFile).getPropertyValue(IProductCmptType.PROPERTY_LAYER_SUPERTYPE);
        doReturn(concreteProductCmptType).when(concreteIpsSrcFile).getIpsObject();
        doReturn("findSuperType").when(concreteIpsSrcFile).getPropertyValue(IType.PROPERTY_SUPERTYPE);
        doReturn(layerSupertypeIpsSrcFile).when(ipsProject)
                .findIpsSrcFile(new QualifiedNameType("findSuperType", IpsObjectType.PRODUCT_CMPT_TYPE));

        ipsSrcFiles.add(layerSupertypeIpsSrcFile);
        ipsSrcFiles.add(abstractIpsSrcFile);
        ipsSrcFiles.add(concreteIpsSrcFile);
        doReturn(ipsSrcFiles.toArray(new IIpsSrcFile[0])).when(ipsProject)
                .findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE);

        pmo.setTemplate(true);
        // refresh the list
        pmo.setIpsProject(ipsProject);

        assertEquals(2, pmo.getBaseTypes().size());
        assertTrue(pmo.getBaseTypes().contains(abstractProductCmptType));
        assertTrue(pmo.getBaseTypes().contains(concreteProductCmptType));
    }

    @Test
    public void testUpdateTypeList() throws Exception {
        pmo.setEffectiveDate(new GregorianCalendar());

        IIpsPackageFragmentRoot packageFragmentRoot = mockPackageFragmentRoot();
        IIpsProject ipsProject = packageFragmentRoot.getIpsProject();
        when(ipsProject.getName()).thenReturn(PROJECT_NAME);
        when(ipsModel.getIpsProject(PROJECT_NAME)).thenReturn(ipsProject);

        pmo.setIpsProject(ipsProject);

        IProductCmptType baseType = mock(IProductCmptType.class);

        ArrayList<IType> subTypes = new ArrayList<>();
        when(baseType.findSubtypes(true, true, ipsProject)).thenReturn(subTypes);

        subTypes.add(baseType);

        // setting the base type updates the sub types list
        pmo.setSelectedBaseType(baseType);
        assertTrue(pmo.getSubtypes().contains(baseType));

        when(baseType.isAbstract()).thenReturn(true);

        pmo.setSelectedBaseType(baseType);
        assertTrue(pmo.getSubtypes().isEmpty());

        IProductCmptType subtype1 = mock(IProductCmptType.class);
        IProductCmptType subtype2 = mock(IProductCmptType.class);
        subTypes.add(subtype1);
        subTypes.add(subtype2);

        pmo.setSelectedBaseType(baseType);
        assertEquals(2, pmo.getSubtypes().size());
        assertTrue(pmo.getSubtypes().contains(subtype1));
        assertTrue(pmo.getSubtypes().contains(subtype2));

        when(subtype2.isAbstract()).thenReturn(true);

        pmo.setSelectedBaseType(baseType);
        assertEquals(1, pmo.getSubtypes().size());
        assertTrue(pmo.getSubtypes().contains(subtype1));
    }

    @Test
    public void testIsAddToMode() throws Exception {
        assertFalse(pmo.isAddToMode());

        pmo.setAddToAssociation(null, null);
        assertFalse(pmo.isAddToMode());

        IProductCmptGeneration addToProductCmptGeneration = mock(IProductCmptGeneration.class);
        pmo.setAddToAssociation(addToProductCmptGeneration, null);
        assertFalse(pmo.isAddToMode());

        IProductCmptTypeAssociation addToAssociation = mock(IProductCmptTypeAssociation.class);
        pmo.setAddToAssociation(null, addToAssociation);
        assertFalse(pmo.isAddToMode());

        IProductCmptType productCmptType = mock(IProductCmptType.class);
        when(addToProductCmptGeneration.findProductCmptType(any(IIpsProject.class))).thenReturn(productCmptType);
        pmo.setAddToAssociation(addToProductCmptGeneration, addToAssociation);
        assertTrue(pmo.isAddToMode());
    }

    @Test
    public void testUpdateSubtypeList_copyMode() throws Exception {
        singletonMockHelper.reset();
        IProductCmptType cmptType = newProductCmptType(ipsProject, "typeName");
        IProductCmpt cmpt = newProductCmpt(cmptType, "cmpt");
        pmo.setIpsProject(ipsProject);
        pmo.setCopyProductCmpt(cmpt);

        pmo.updateSubtypeList();

        assertThat(pmo.getSubtypes().size(), is(1));
        assertThat(pmo.getSubtypes(), hasItem(cmptType));
    }

    @Test
    public void testUpdateSubtypeList_noTemplateSelected() throws Exception {
        singletonMockHelper.reset();
        IProductCmptType cmptType = mock(IProductCmptType.class);
        IProductCmptType type1 = mock(IProductCmptType.class);
        when(type1.isAbstract()).thenReturn(false);
        IProductCmptType type2 = mock(IProductCmptType.class);
        when(type2.isAbstract()).thenReturn(true);
        IProductCmptType type3 = mock(IProductCmptType.class);
        when(type3.isAbstract()).thenReturn(false);
        List<IType> result = Arrays.<IType> asList(type1, type2, type3);
        when(cmptType.findSubtypes(true, true, ipsProject)).thenReturn(result);
        pmo.setIpsProject(ipsProject);

        // pmo.updateSubtypesList is called by setSelectedBaseType
        pmo.setSelectedBaseType(cmptType);

        assertThat(pmo.getSubtypes().size(), is(2));
        assertThat(pmo.getSubtypes(), hasItems(type1, type3));
    }

    @Test
    public void testUpdateSubtypeList_noTemplateSelected_createTemplate() throws Exception {
        singletonMockHelper.reset();
        IProductCmptType cmptType = mock(IProductCmptType.class);
        IProductCmptType type1 = mock(IProductCmptType.class);
        when(type1.isAbstract()).thenReturn(false);
        IProductCmptType type2 = mock(IProductCmptType.class);
        when(type2.isAbstract()).thenReturn(true);
        IProductCmptType type3 = mock(IProductCmptType.class);
        when(type3.isAbstract()).thenReturn(false);
        List<IType> result = Arrays.<IType> asList(type1, type2, type3);
        when(cmptType.findSubtypes(true, true, ipsProject)).thenReturn(result);
        pmo.setIpsProject(ipsProject);
        pmo.setTemplate(true);

        // pmo.updateSubtypesList is called by setSelectedBaseType
        pmo.setSelectedBaseType(cmptType);

        assertThat(pmo.getSubtypes().size(), is(3));
        assertThat(pmo.getSubtypes(), hasItems(type1, type2, type3));
    }

    @Test
    public void testUpdateSubtypeList_templateSelected() throws Exception {
        singletonMockHelper.reset();
        IProductCmptType cmptType = mock(IProductCmptType.class);
        IProductCmptType type1 = mock(IProductCmptType.class);
        when(type1.isAbstract()).thenReturn(false);
        IProductCmptType type2 = mock(IProductCmptType.class);
        when(type2.isAbstract()).thenReturn(true);
        IProductCmptType type3 = mock(IProductCmptType.class);
        when(type3.isAbstract()).thenReturn(false);
        when(cmptType.findSubtypes(true, true, ipsProject)).thenReturn(Arrays.<IType> asList(type1, type2, type3));
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        IProductCmpt cmpt = mock(IProductCmpt.class);
        when(ipsSrcFile.getIpsObject()).thenReturn(cmpt);
        when(cmpt.findProductCmptType(ipsProject)).thenReturn(cmptType);
        when(ipsProject.findAllProductTemplates(cmptType, true)).thenReturn(Arrays.asList(ipsSrcFile));
        when(ipsProject.findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE)).thenReturn(new IIpsSrcFile[] {});
        ProductCmptViewItem templateViewItem = new ProductCmptViewItem(ipsSrcFile);
        pmo.setIpsProject(ipsProject);
        pmo.setSelectedBaseType(cmptType);
        pmo.updateTemplatesList();

        // pmo.updateSubtypesList is called by setSelectedTemplate
        pmo.setSelectedTemplate(templateViewItem);

        assertThat(pmo.getSubtypes().size(), is(2));
        assertThat(pmo.getSubtypes(), hasItems(type1, type3));
    }

    @Test
    public void testUpdateSubtypeList_templateSelected_createTemplate() throws Exception {
        singletonMockHelper.reset();
        pmo.setTemplate(true);
        IProductCmptType cmptType = mock(IProductCmptType.class);
        IProductCmptType type1 = mock(IProductCmptType.class);
        when(type1.isAbstract()).thenReturn(false);
        IProductCmptType type2 = mock(IProductCmptType.class);
        when(type2.isAbstract()).thenReturn(true);
        IProductCmptType type3 = mock(IProductCmptType.class);
        when(type3.isAbstract()).thenReturn(false);
        when(cmptType.findSubtypes(true, true, ipsProject)).thenReturn(Arrays.<IType> asList(type1, type2, type3));
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        IProductCmpt cmpt = mock(IProductCmpt.class);
        when(ipsSrcFile.getIpsObject()).thenReturn(cmpt);
        when(cmpt.findProductCmptType(ipsProject)).thenReturn(cmptType);
        when(ipsProject.findAllProductTemplates(cmptType, true)).thenReturn(Arrays.asList(ipsSrcFile));
        when(ipsProject.findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE)).thenReturn(new IIpsSrcFile[] {});
        ProductCmptViewItem templateViewItem = new ProductCmptViewItem(ipsSrcFile);
        pmo.setIpsProject(ipsProject);
        pmo.setSelectedBaseType(cmptType);
        pmo.updateTemplatesList();

        // pmo.updateSubtypesList is called by setSelectedTemplate
        pmo.setSelectedTemplate(templateViewItem);

        assertThat(pmo.getSubtypes().size(), is(3));
        assertThat(pmo.getSubtypes(), hasItems(type1, type2, type3));
    }

    @Test
    public void testUpdateTemplatesList_copyMode() throws Exception {
        singletonMockHelper.reset();
        pmo.setIpsProject(ipsProject);

        IProductCmptType baseType = newProductCmptType(ipsProject, "baseTypeName");
        IProductCmptType subType = newProductCmptType(baseType, "subTypeName");
        IProductCmpt cmpt = newProductCmpt(subType, "cmptName");
        // Matching template exists but is ignored in copy mode
        newProductTemplate(subType, "templateName");

        pmo.setSelectedBaseType(baseType);
        pmo.setSelectedType(subType);
        pmo.setCopyProductCmpt(cmpt);

        pmo.updateTemplatesList();

        assertThat(pmo.getTemplates().size(), is(0));
    }

    @Test
    public void testUpdateTemplatesList_noTypeSelected() {
        singletonMockHelper.reset();
        pmo.setIpsProject(ipsProject);

        pmo.updateTemplatesList();

        assertThat(pmo.getTemplates().size(), is(1));
        assertThat(pmo.getTemplates(), hasItem(NewProductCmptPMO.NULL_TEMPLATE));
    }

    @Test
    public void testUpdateTemplatesList_noMatchingTemplateExists() {
        singletonMockHelper.reset();
        pmo.setIpsProject(ipsProject);

        IProductCmptType type1 = newProductCmptType(ipsProject, "typeName1");
        IProductCmptType type2 = newProductCmptType(ipsProject, "typeName2");
        newProductTemplate(type1, "templateName1");

        pmo.setSelectedBaseType(type2);
        pmo.updateTemplatesList();

        assertThat(pmo.getTemplates().size(), is(1));
        assertThat(pmo.getTemplates(), hasItem(NewProductCmptPMO.NULL_TEMPLATE));
    }

    @Test
    public void testUpdateTemplatesList_baseTypeTemplateIsFound() {
        singletonMockHelper.reset();
        pmo.setIpsProject(ipsProject);

        IProductCmptType baseType = newProductCmptType(ipsProject, "baseTypeName");
        IProductCmptType subType = newProductCmptType(baseType, "subTypeName");
        IProductCmpt baseTemplate = newProductTemplate(baseType, "templateName");

        pmo.setSelectedBaseType(baseType);
        pmo.setSelectedType(subType);
        pmo.updateTemplatesList();

        assertThat(pmo.getTemplates().size(), is(2));
        assertThat(pmo.getTemplates().get(0), is(NewProductCmptPMO.NULL_TEMPLATE));
        assertThat(pmo.getTemplates().get(1).getProductCmpt(), is(baseTemplate));
    }

    @Test
    public void testUpdateTemplatesList_subTypeTemplateIsFound() {
        singletonMockHelper.reset();
        pmo.setIpsProject(ipsProject);

        IProductCmptType baseType = newProductCmptType(ipsProject, "baseTypeName");
        IProductCmptType subType = newProductCmptType(baseType, "subTypeName");
        IProductCmpt subTemplate = newProductTemplate(subType, "templateName");

        pmo.setSelectedBaseType(baseType);
        pmo.setSelectedType(subType);
        pmo.updateTemplatesList();

        assertThat(pmo.getTemplates().size(), is(2));
        assertThat(pmo.getTemplates().get(0), is(NewProductCmptPMO.NULL_TEMPLATE));
        assertThat(pmo.getTemplates().get(1).getProductCmpt(), is(subTemplate));
    }

    @Test
    public void testUpdateTemplatesList_subTypeTemplateIsFoundWhenBaseTypeIsSelected() {
        singletonMockHelper.reset();
        pmo.setIpsProject(ipsProject);

        IProductCmptType baseType = newProductCmptType(ipsProject, "baseTypeName");
        IProductCmptType subType = newProductCmptType(baseType, "subTypeName");
        IProductCmpt subTemplate = newProductTemplate(subType, "templateName");

        pmo.setSelectedBaseType(baseType);
        pmo.setSelectedType(null);
        pmo.updateTemplatesList();

        assertThat(pmo.getTemplates().size(), is(2));
        assertThat(pmo.getTemplates().get(0), is(NewProductCmptPMO.NULL_TEMPLATE));
        assertThat(pmo.getTemplates().get(1).getProductCmpt(), is(subTemplate));
    }

    @Test
    public void testUpdateTemplatesList_selectedTemplateIsReset() {
        singletonMockHelper.reset();
        pmo.setIpsProject(ipsProject);

        IProductCmptType type1 = newProductCmptType(ipsProject, "typeName1");
        IProductCmptType type2 = newProductCmptType(ipsProject, "typeName2");
        newProductTemplate(type1, "templateName1");
        newProductTemplate(type2, "templateName2");

        pmo.setSelectedType(type1);
        pmo.updateTemplatesList();
        assertThat(pmo.getTemplates().size(), is(2));
        assertThat(pmo.getTemplates().get(1), is(not(NewProductCmptPMO.NULL_TEMPLATE)));

        pmo.setSelectedTemplate(pmo.getTemplates().get(1));
        pmo.setSelectedType(type2);
        pmo.updateTemplatesList();
        assertThat(pmo.getSelectedTemplate(), is(NewProductCmptPMO.NULL_TEMPLATE));
    }

    @Test
    public void testSetSelectedType_showsTemplatesOrDescription() {
        singletonMockHelper.reset();
        pmo.setIpsProject(ipsProject);

        IProductCmptType type = newProductCmptType(ipsProject, "typeName");

        // No template exists, description is shown
        pmo.setSelectedType(type);
        assertThat(pmo.isShowTemplates(), is(false));
        assertThat(pmo.isShowDescription(), is(true));

        // Template exists, templates are shown
        newProductTemplate(type, "templateName");
        pmo.setSelectedType(type);
        assertThat(pmo.isShowTemplates(), is(true));
        assertThat(pmo.isShowDescription(), is(false));
    }

    @Test
    public void testSetSelectedBaseType_showsTemplatesOrDescription() {
        singletonMockHelper.reset();
        pmo.setIpsProject(ipsProject);

        IProductCmptType type = newProductCmptType(ipsProject, "typeName");

        // No template exists, description is shown
        pmo.setSelectedBaseType(type);
        assertThat(pmo.isShowTemplates(), is(false));
        assertThat(pmo.isShowDescription(), is(true));

        // Template exists, templates are shown
        newProductTemplate(type, "templateName");
        pmo.setSelectedBaseType(type);
        assertThat(pmo.isShowTemplates(), is(true));
        assertThat(pmo.isShowDescription(), is(false));
    }

    @Test
    public void testUpdateTemplatesList_subSubTemplateIsFiltered() {
        singletonMockHelper.reset();
        pmo.setIpsProject(ipsProject);

        IProductCmptType baseType = newProductCmptType(ipsProject, "baseTypeName");
        IProductCmptType subType = newProductCmptType(baseType, "subTypeName");
        IProductCmptType subSubType = newProductCmptType(subType, "subSubTypeName");
        IProductCmpt baseTemplate = newProductTemplate(baseType, "templateName");
        IProductCmpt subTemplate = newProductTemplate(subType, "subTemplateName");
        // not eligible for product components of subType
        newProductTemplate(subSubType, "subSubTemplateName");
        pmo.setSelectedBaseType(baseType);

        pmo.setSelectedType(subSubType);
        pmo.updateTemplatesList();
        assertThat(pmo.getTemplates().size(), is(4));

        pmo.setSelectedType(subType);
        pmo.updateTemplatesList();

        assertThat(pmo.getTemplates().size(), is(3));
        ProductCmptViewItem baseTemplateViewItem = new ProductCmptViewItem(baseTemplate.getIpsSrcFile());
        ProductCmptViewItem subTemplateViewItem = new ProductCmptViewItem(subTemplate.getIpsSrcFile());
        assertThat(pmo.getTemplates(),
                hasItems(NewProductCmptPMO.NULL_TEMPLATE, baseTemplateViewItem, subTemplateViewItem));
    }

    @Test
    public void testUpdateSubtypeList_baseTypeIsFiltered() {
        singletonMockHelper.reset();
        pmo.setIpsProject(ipsProject);

        // not eligible for product components based on subTemplate
        IProductCmptType baseType = newProductCmptType(ipsProject, "baseTypeName");
        IProductCmptType subType = newProductCmptType(baseType, "subTypeName");
        IProductCmptType subSubType = newProductCmptType(subType, "subSubTypeName");
        IProductCmpt baseTemplate = newProductTemplate(baseType, "templateName");
        IProductCmpt subTemplate = newProductTemplate(subType, "subTemplateName");
        newProductTemplate(subSubType, "subSubTemplateName");
        pmo.setSelectedBaseType(baseType);

        pmo.setSelectedTemplate(new ProductCmptViewItem(baseTemplate.getIpsSrcFile()));
        pmo.updateSubtypeList();
        assertThat(pmo.getSubtypes().size(), is(3));

        pmo.setSelectedTemplate(new ProductCmptViewItem(subTemplate.getIpsSrcFile()));
        pmo.updateSubtypeList();

        assertThat(pmo.getSubtypes().size(), is(2));
        assertThat(pmo.getSubtypes().get(0), is(subSubType));
        assertThat(pmo.getSubtypes().get(1), is(subType));
    }

    @Override
    @After
    public void tearDown() {
        singletonMockHelper.reset();
    }

}
