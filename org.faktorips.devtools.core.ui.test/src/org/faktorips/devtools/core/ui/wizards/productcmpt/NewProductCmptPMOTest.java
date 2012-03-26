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

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.SingletonMockHelper;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.commands.NewResourceNameValidator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NewProductCmptPMOTest extends AbstractIpsPluginTest {

    private static final String PROJECT_NAME = "projectName";
    private SingletonMockHelper singletonMockHelper;
    private IIpsModel ipsModel;

    @Override
    @Before
    public void setUp() {
        IpsPlugin ipsPlugin = mock(IpsPlugin.class);
        ipsModel = mock(IIpsModel.class);
        singletonMockHelper = new SingletonMockHelper();
        singletonMockHelper.setSingletonInstance(IpsPlugin.class, ipsPlugin);
        when(ipsPlugin.getIpsModel()).thenReturn(ipsModel);
        IpsPreferences preferences = mock(IpsPreferences.class);
        when(ipsPlugin.getIpsPreferences()).thenReturn(preferences);
    }

    @Test
    public void testIsFirstPageNeeded_NotNeededWhenAddingToAssociation() {
        NewProductCmptPMO pmo = new NewProductCmptPMO();
        pmo.setAddToAssociation(mock(IProductCmptGeneration.class), mock(IProductCmptTypeAssociation.class));

        assertFalse(pmo.isFirstPageNeeded());
    }

    @Test
    public void testIsFirstPageNeeded_NotNeededWhenCopyingValidProductCmpt() throws CoreException {
        singletonMockHelper.reset();

        IIpsProject ipsProject = newIpsProject();
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductCmptType");
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "ProductCmpt");

        NewProductCmptPMO pmo = new NewProductCmptPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setCopyProductCmpt(productCmpt);

        assertFalse(pmo.isFirstPageNeeded());
    }

    @Test
    public void testIsFirstPageNeeded_NeededWhenNeitherCopyingProductCmptNorAddingToAssociation() {
        assertTrue(new NewProductCmptPMO().isFirstPageNeeded());
    }

    @Test
    public void testIsFirstPageNeeded_NeededWhenCopyingProductCmptButTypeNotFound() throws CoreException {
        singletonMockHelper.reset();

        IIpsProject ipsProject = newIpsProject();
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductCmptType");
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "ProductCmpt");
        productCmpt.setProductCmptType("notExistingType");

        NewProductCmptPMO pmo = new NewProductCmptPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setCopyProductCmpt(productCmpt);

        assertTrue(pmo.isFirstPageNeeded());
    }

    @Test
    public void testGetSubtypes_OnlyShowSelectedTypeWhenCopyingValidProductCmpt() throws CoreException {
        singletonMockHelper.reset();

        IIpsProject ipsProject = newIpsProject();
        IProductCmptType baseType = newProductCmptType(ipsProject, "BaseType");
        baseType.setAbstract(true);
        IProductCmptType productCmptType1 = newProductCmptType(baseType, "ProductCmptType1");
        IProductCmptType productCmptType2 = newProductCmptType(baseType, "ProductCmptType2");
        IProductCmpt productCmpt = newProductCmpt(productCmptType1, "ProductCmpt");

        NewProductCmptPMO pmo = new NewProductCmptPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setCopyProductCmpt(productCmpt);

        assertTrue(pmo.getSubtypes().contains(productCmptType1));
        assertFalse(pmo.getSubtypes().contains(productCmptType2));
        assertEquals(1, pmo.getSubtypes().size());
    }

    @Test
    public void testGetSubtypes_ShowAllSubtypesWhenCopyingProductCmptForWhichTypeIsNotFound() throws CoreException {
        singletonMockHelper.reset();

        IIpsProject ipsProject = newIpsProject();
        IProductCmptType baseType = newProductCmptType(ipsProject, "BaseType");
        baseType.setAbstract(true);
        IProductCmptType productCmptType1 = newProductCmptType(baseType, "ProductCmptType1");
        IProductCmptType productCmptType2 = newProductCmptType(baseType, "ProductCmptType2");
        IProductCmpt productCmpt = newProductCmpt(productCmptType1, "ProductCmpt");
        productCmpt.setProductCmptType("notExistingType");

        NewProductCmptPMO pmo = new NewProductCmptPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setCopyProductCmpt(productCmpt);
        pmo.setSelectedBaseType(baseType);

        assertTrue(pmo.getSubtypes().contains(productCmptType1));
        assertTrue(pmo.getSubtypes().contains(productCmptType2));
        assertEquals(2, pmo.getSubtypes().size());
    }

    @Test(expected = IllegalStateException.class)
    public void testSetCopyProductCmpt_IpsProjectNotSet() {
        new NewProductCmptPMO().setCopyProductCmpt(mock(IProductCmpt.class));
    }

    @Test
    public void testSetCopyProductCmpt() throws CoreException {
        singletonMockHelper.reset();

        // Create a small test model
        IIpsProject ipsProject = newIpsProject();
        IProductCmptType layerSupertype = newProductCmptType(ipsProject, "LayerSupertype");
        layerSupertype.setLayerSupertype(true);
        IProductCmptType baseType = newProductCmptType(ipsProject, "BaseType");
        baseType.setSupertype(layerSupertype.getQualifiedName());
        IProductCmptType concreteType = newProductCmptType(ipsProject, "ConcreteType");
        concreteType.setSupertype(baseType.getQualifiedName());

        // Create the product component to copy
        IProductCmpt productCmptToCopy = newProductCmpt(concreteType, "ProductToCopy");

        // Configure PMO to copy product component
        NewProductCmptPMO pmo = new NewProductCmptPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setCopyProductCmpt(productCmptToCopy);

        // Base product component type and product component type should be selected accordingly
        assertEquals(baseType, pmo.getSelectedBaseType());
        assertEquals(concreteType, pmo.getSelectedType());
        assertTrue(pmo.isCopyMode());
        assertTrue(pmo.isCopyValidMode());
        assertEquals(productCmptToCopy, pmo.getCopyProductCmpt());
    }

    @Test
    public void testSetCopyProductCmpt_ProductCmptTypeIsNull() throws CoreException {
        singletonMockHelper.reset();

        IIpsProject ipsProject = newIpsProject();
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductCmptType");
        IProductCmpt productCmptToCopy = newProductCmpt(productCmptType, "ProductToCopy");
        productCmptToCopy.setProductCmptType("notExistent");

        NewProductCmptPMO pmo = new NewProductCmptPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setCopyProductCmpt(productCmptToCopy);

        assertTrue(pmo.isCopyMode());
        assertFalse(pmo.isCopyValidMode());
        assertEquals(productCmptToCopy, pmo.getCopyProductCmpt());

        assertNull(pmo.getSelectedBaseType());
        assertNull(pmo.getSelectedType());
        assertTrue(pmo.getBaseTypes().contains(productCmptType));
        assertEquals(1, pmo.getBaseTypes().size());
    }

    @Test
    public void testSetCopyProductCmpt_InitializeNameWithValidResourceName() throws CoreException {
        singletonMockHelper.reset();

        IIpsProject ipsProject = newIpsProject();
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductCmptType");
        IProductCmpt productCmptToCopy = newProductCmpt(productCmptType, "ProductToCopy");
        productCmptToCopy.setProductCmptType("notExistent");
        productCmptToCopy.getIpsSrcFile().save(true, null);

        NewProductCmptPMO pmo = new NewProductCmptPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setCopyProductCmpt(productCmptToCopy);

        IPath targetPath = productCmptToCopy.getIpsPackageFragment().getCorrespondingResource().getFullPath();
        NewResourceNameValidator resourceNameValidator = new NewResourceNameValidator(targetPath, IResource.FILE,
                '.' + IpsObjectType.PRODUCT_CMPT.getFileExtension(), productCmptToCopy.getIpsSrcFile());
        assertEquals(resourceNameValidator.getValidResourceName(productCmptToCopy.getName()), pmo.getName());
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
    public void testSetIpsProject_UpdateRuntimeId() throws CoreException {
        singletonMockHelper.reset();

        IIpsProject ipsProject = newIpsProject();
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "Type");
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "Product");

        NewProductCmptPMO pmo = new NewProductCmptPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setCopyProductCmpt(productCmpt);

        IIpsProject otherIpsProject = mock(IIpsProject.class);
        IIpsPackageFragmentRoot packageRoot = mock(IIpsPackageFragmentRoot.class);
        when(otherIpsProject.getSourceIpsPackageFragmentRoots()).thenReturn(
                new IIpsPackageFragmentRoot[] { packageRoot });
        when(otherIpsProject.findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE)).thenReturn(new IIpsSrcFile[0]);
        IProductCmptNamingStrategy productCmptNamingStrategy = mock(IProductCmptNamingStrategy.class);
        when(otherIpsProject.getProductCmptNamingStrategy()).thenReturn(productCmptNamingStrategy);
        when(productCmptNamingStrategy.getUniqueRuntimeId(any(IIpsProject.class), anyString())).thenReturn(
                "Foo_Product");

        pmo.setIpsProject(otherIpsProject);

        assertEquals("Foo_Product", pmo.getRuntimeId());
    }

    @Test
    public void testUpdateBaseTypeList_noProject() throws Exception {
        NewProductCmptPMO pmo = new NewProductCmptPMO();

        IIpsPackageFragmentRoot ipsPackageFragmentRoot = mockPackageFragmentRoot();

        pmo.setPackageRoot(ipsPackageFragmentRoot);

        assertTrue(pmo.getBaseTypes().isEmpty());
    }

    private IIpsPackageFragmentRoot mockPackageFragmentRoot() throws CoreException {
        IIpsPackageFragmentRoot ipsPackageFragmentRoot = mock(IIpsPackageFragmentRoot.class);
        IIpsProject ipsProject = mock(IIpsProject.class);
        IProductCmptNamingStrategy productCmptNamingStrategy = mock(IProductCmptNamingStrategy.class);
        when(ipsPackageFragmentRoot.getIpsProject()).thenReturn(ipsProject);
        when(ipsProject.findIpsSrcFiles(any(IpsObjectType.class))).thenReturn(new IIpsSrcFile[0]);
        when(ipsProject.getProductCmptNamingStrategy()).thenReturn(productCmptNamingStrategy);
        when(ipsProject.getSourceIpsPackageFragmentRoots()).thenReturn(
                new IIpsPackageFragmentRoot[] { ipsPackageFragmentRoot });
        return ipsPackageFragmentRoot;
    }

    @Test
    public void testUpdateBaseTypeList_withProject() throws Exception {
        NewProductCmptPMO pmo = new NewProductCmptPMO();
        pmo.setEffectiveDate(new GregorianCalendar());

        IIpsPackageFragmentRoot packageFragmentRoot = mockPackageFragmentRoot();
        IIpsProject ipsProject = packageFragmentRoot.getIpsProject();

        ArrayList<IIpsSrcFile> ipsSrcFiles = new ArrayList<IIpsSrcFile>();

        when(ipsProject.findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE)).thenReturn(
                ipsSrcFiles.toArray(new IIpsSrcFile[0]));
        when(ipsProject.getName()).thenReturn(PROJECT_NAME);

        when(ipsModel.getIpsProject(PROJECT_NAME)).thenReturn(ipsProject);

        pmo.setIpsProject(ipsProject);
        assertTrue(pmo.getBaseTypes().isEmpty());

        IIpsSrcFile ipsSrcFile1 = mock(IIpsSrcFile.class);
        IProductCmptType productCmptType1 = mock(IProductCmptType.class);
        when(productCmptType1.getName()).thenReturn("1");
        IIpsSrcFile ipsSrcFile2 = mock(IIpsSrcFile.class);
        IProductCmptType productCmptType2 = mock(IProductCmptType.class);
        when(productCmptType2.getName()).thenReturn("1");
        IIpsSrcFile ipsSrcFile3 = mock(IIpsSrcFile.class);
        IProductCmptType productCmptType3 = mock(IProductCmptType.class);
        when(productCmptType3.getName()).thenReturn("1");
        ipsSrcFiles.add(ipsSrcFile1);
        ipsSrcFiles.add(ipsSrcFile2);
        ipsSrcFiles.add(ipsSrcFile3);
        when(ipsProject.findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE)).thenReturn(
                ipsSrcFiles.toArray(new IIpsSrcFile[0]));

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

        assertEquals(2, pmo.getBaseTypes().size());
        assertTrue(pmo.getBaseTypes().contains(productCmptType2));
        assertTrue(pmo.getBaseTypes().contains(productCmptType3));
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
        NewProductCmptPMO pmo = new NewProductCmptPMO();
        pmo.setEffectiveDate(new GregorianCalendar());

        IIpsPackageFragmentRoot packageFragmentRoot = mockPackageFragmentRoot();
        IIpsProject ipsProject = packageFragmentRoot.getIpsProject();

        ArrayList<IIpsSrcFile> ipsSrcFiles = new ArrayList<IIpsSrcFile>();

        when(ipsProject.findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE)).thenReturn(
                ipsSrcFiles.toArray(new IIpsSrcFile[0]));
        when(ipsProject.getName()).thenReturn(PROJECT_NAME);

        when(ipsModel.getIpsProject(PROJECT_NAME)).thenReturn(ipsProject);

        pmo.setIpsProject(ipsProject);
        assertTrue(pmo.getBaseTypes().isEmpty());

        IIpsSrcFile ipsSrcFile1 = mock(IIpsSrcFile.class);
        IProductCmptType productCmptType1 = mock(IProductCmptType.class);
        when(productCmptType1.getName()).thenReturn("1");
        IIpsSrcFile ipsSrcFile2 = mock(IIpsSrcFile.class);
        IProductCmptType productCmptType2 = mock(IProductCmptType.class);
        when(productCmptType2.getName()).thenReturn("1");
        IIpsSrcFile ipsSrcFile3 = mock(IIpsSrcFile.class);
        IProductCmptType productCmptType3 = mock(IProductCmptType.class);
        when(productCmptType3.getName()).thenReturn("1");
        ipsSrcFiles.add(ipsSrcFile1);
        ipsSrcFiles.add(ipsSrcFile2);
        ipsSrcFiles.add(ipsSrcFile3);
        when(ipsProject.findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE)).thenReturn(
                ipsSrcFiles.toArray(new IIpsSrcFile[0]));

        when(ipsSrcFile1.getPropertyValue(IProductCmptType.PROPERTY_LAYER_SUPERTYPE)).thenReturn("true");
        when(ipsSrcFile1.getIpsObject()).thenReturn(productCmptType1);
        when(ipsSrcFile2.getPropertyValue(IProductCmptType.PROPERTY_LAYER_SUPERTYPE)).thenReturn("false");
        when(ipsSrcFile2.getPropertyValue(IType.PROPERTY_ABSTRACT)).thenReturn("true");
        when(ipsSrcFile2.getIpsObject()).thenReturn(productCmptType2);
        when(ipsSrcFile3.getPropertyValue(IProductCmptType.PROPERTY_LAYER_SUPERTYPE)).thenReturn(null);
        when(ipsSrcFile3.getIpsObject()).thenReturn(productCmptType3);

        when(ipsSrcFile3.getPropertyValue(IType.PROPERTY_SUPERTYPE)).thenReturn("findSuperType");

        when(ipsProject.findIpsSrcFile(new QualifiedNameType("findSuperType", IpsObjectType.PRODUCT_CMPT_TYPE)))
                .thenReturn(ipsSrcFile1);

        // refresh the list
        pmo.setIpsProject(ipsProject);

        assertEquals(1, pmo.getBaseTypes().size());
        assertTrue(pmo.getBaseTypes().contains(productCmptType3));
    }

    @Test
    public void testUpdateTypeList() throws Exception {
        NewProductCmptPMO pmo = new NewProductCmptPMO();
        pmo.setEffectiveDate(new GregorianCalendar());

        IIpsPackageFragmentRoot packageFragmentRoot = mockPackageFragmentRoot();
        IIpsProject ipsProject = packageFragmentRoot.getIpsProject();
        when(ipsProject.getName()).thenReturn(PROJECT_NAME);
        when(ipsModel.getIpsProject(PROJECT_NAME)).thenReturn(ipsProject);

        pmo.setIpsProject(ipsProject);

        IProductCmptType baseType = mock(IProductCmptType.class);

        ArrayList<IType> subTypes = new ArrayList<IType>();
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
        NewProductCmptPMO pmo = new NewProductCmptPMO();
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

    @Override
    @After
    public void tearDown() {
        singletonMockHelper.reset();
    }

}
