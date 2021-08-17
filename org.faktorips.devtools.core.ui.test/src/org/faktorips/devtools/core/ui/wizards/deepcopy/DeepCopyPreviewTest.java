/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpt.treestructure.ProductCmptReference;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.internal.tablecontents.TableContents;
import org.faktorips.devtools.model.internal.util.StringUtils;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.productcmpt.DateBasedProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.DateBasedProductCmptNamingStrategyFactory;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.AssociationType;
import org.junit.Before;
import org.junit.Test;

public class DeepCopyPreviewTest extends AbstractIpsPluginTest {

    private IIpsProject subIpsProject;

    private IIpsProject suPerIpsProject;

    private ProductCmptType superType;

    private ProductCmptType subType;

    private ProductCmpt aSub;

    private ProductCmptReference cmptReferenceASub;

    private DeepCopyPreview deepCopy;

    private ProductCmpt bSuper;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        subIpsProject = newIpsProject("SuperProject");
        suPerIpsProject = newIpsProject("SubProject");
        subIpsProject.isReferencing(suPerIpsProject);

        superType = newProductCmptType(suPerIpsProject, "some.superType");
        bSuper = newProductCmpt(superType, "some.BSuper");

        subType = newProductCmptType(superType, "some.subType");
        aSub = newProductCmpt(subType, "some.ASub");

        IProductCmptTypeAssociation subAssociation = subType.newProductCmptTypeAssociation();
        subAssociation.setAssociationType(AssociationType.ASSOCIATION);
        subAssociation.setTarget(superType.getName());

        DeepCopyPresentationModel presentationModel = new DeepCopyPresentationModel(bSuper.getProductCmptGeneration(0));
        deepCopy = new DeepCopyPreview(presentationModel);

        cmptReferenceASub = new ProductCmptReference(null, null, aSub, null);

    }

    @Test
    public void testValidateAlreadyExistingFile_FileExists() {
        deepCopy.validateAlreadyExistingFile("some", aSub.getName(), IpsObjectType.PRODUCT_CMPT.getFileExtension(),
                cmptReferenceASub);
        Map<IProductCmptStructureReference, String> errorElements = deepCopy.getErrorElements();

        assertEquals(1, errorElements.size());
        assertTrue(errorElements.get(cmptReferenceASub)
                .contains(Messages.ReferenceAndPreviewPage_msgFileAllreadyExists));
        assertTrue(errorElements.get(cmptReferenceASub).contains(Messages.ReferenceAndPreviewPage_msgCanNotCreateFile));
    }

    @Test
    public void testValidateAlreadyExistingFile_ExistInOtherPackage() {

        deepCopy.validateAlreadyExistingFile("somenew", aSub.getName(), IpsObjectType.PRODUCT_CMPT.getFileExtension(),
                cmptReferenceASub);
        Map<IProductCmptStructureReference, String> errorElements = deepCopy.getErrorElements();

        assertNotNull(errorElements);
        assertEquals(0, errorElements.size());
    }

    @Test
    public void testValidateAlreadyExistingFile_notExisting() {

        deepCopy.validateAlreadyExistingFile("some", "NewName", IpsObjectType.PRODUCT_CMPT.getFileExtension(),
                cmptReferenceASub);
        Map<IProductCmptStructureReference, String> errorElements = deepCopy.getErrorElements();

        assertNotNull(errorElements);
        assertEquals(0, errorElements.size());
    }

    @Test
    public void testGetNewName_productCmpt() throws Exception {
        setVersionAndStrategy();
        bSuper = newProductCmpt(superType, "some.BSuper 2014-09");
        deepCopy.getPresentationModel().setVersionId("2015-09");
        deepCopy.getPresentationModel().setSearchInput("B");
        deepCopy.getPresentationModel().setReplaceInput("X");

        String newName = deepCopy.getNewName(bSuper.getIpsPackageFragment(), bSuper);

        assertEquals("XSuper 2015-09", newName);
    }

    @Test
    public void testGetNewName_productCmpt_otherPackage() throws Exception {
        setVersionAndStrategy();
        bSuper = newProductCmpt(superType, "some.BSuper 2014-09");
        deepCopy.getPresentationModel().setVersionId("2015-09");
        deepCopy.getPresentationModel().setSearchInput("B");
        deepCopy.getPresentationModel().setReplaceInput("X");
        IIpsPackageFragment ipsPackageFragment = bSuper.getIpsPackageFragment().createSubPackage("subPack", true, null);

        String newName = deepCopy.getNewName(ipsPackageFragment, bSuper);

        assertEquals("XSuper 2015-09", newName);
    }

    private void setVersionAndStrategy() throws CoreException {
        IIpsProjectProperties properties = suPerIpsProject.getProperties();
        DateBasedProductCmptNamingStrategy productCmptNamingStrategy = (DateBasedProductCmptNamingStrategy)new DateBasedProductCmptNamingStrategyFactory()
                .newProductCmptNamingStrategy(suPerIpsProject);
        productCmptNamingStrategy.setVersionIdSeparator(" ");
        productCmptNamingStrategy.setDateFormatPattern("yyyy-MM");
        properties.setProductCmptNamingStrategy(productCmptNamingStrategy);
        suPerIpsProject.setProperties(properties);
    }

    @Test
    public void testGetNewName_productCmpt_noVersion() throws Exception {
        deepCopy.getPresentationModel().setSearchInput("B");
        deepCopy.getPresentationModel().setReplaceInput("X");

        String newName = deepCopy.getNewName(bSuper.getIpsPackageFragment(), bSuper);

        assertEquals("XSuper", newName);
    }

    @Test
    public void testGetNewName_productCmpt_otherPackage_noVersion() throws Exception {
        deepCopy.getPresentationModel().setSearchInput("B");
        deepCopy.getPresentationModel().setReplaceInput("X");
        IIpsPackageFragment ipsPackageFragment = bSuper.getIpsPackageFragment().createSubPackage("subPack", true, null);

        String newName = deepCopy.getNewName(ipsPackageFragment, bSuper);

        assertEquals("XSuper", newName);
    }

    @Test
    public void testGetNewName_table() throws Exception {
        setVersionAndStrategy();
        TableContents tableContents = newTableContents(subIpsProject, "some.AContent 2014-09");
        deepCopy.getPresentationModel().setVersionId("2015-09");
        deepCopy.getPresentationModel().setSearchInput("A");
        deepCopy.getPresentationModel().setReplaceInput("X");

        String newName = deepCopy.getNewName(tableContents.getIpsPackageFragment(), tableContents);

        assertEquals("XContent 2015-09", newName);
    }

    @Test
    public void testGetNewName_table_sameVersion() throws Exception {
        setVersionAndStrategy();
        TableContents tableContents = newTableContents(subIpsProject, "some.AContent 2014-09");
        deepCopy.getPresentationModel().setVersionId("2014-09");
        deepCopy.getPresentationModel().setSearchInput("A");
        deepCopy.getPresentationModel().setReplaceInput("X");

        String newName = deepCopy.getNewName(tableContents.getIpsPackageFragment(), tableContents);

        assertEquals("XContent 2014-09", newName);
    }

    @Test
    public void testGetNewName_table_sameVersion_noReplace() throws Exception {
        setVersionAndStrategy();
        TableContents tableContents = newTableContents(subIpsProject, "some.AContent 2014-09");
        deepCopy.getPresentationModel().setVersionId("2014-09");
        deepCopy.getPresentationModel().setSearchInput("A");
        deepCopy.getPresentationModel().setReplaceInput("A");

        String newName = deepCopy.getNewName(tableContents.getIpsPackageFragment(), tableContents);

        assertEquals(StringUtils.computeCopyOfName(0, "AContent 2014-09"), newName);
    }

    @Test
    public void testGetNewName_table_otherPacakge() throws Exception {
        setVersionAndStrategy();
        TableContents tableContents = newTableContents(subIpsProject, "some.AContent 2014-09");
        deepCopy.getPresentationModel().setVersionId("2015-09");
        deepCopy.getPresentationModel().setSearchInput("A");
        deepCopy.getPresentationModel().setReplaceInput("X");
        IIpsPackageFragment ipsPackageFragment = tableContents.getIpsPackageFragment().createSubPackage("subPack",
                true, null);

        String newName = deepCopy.getNewName(ipsPackageFragment, tableContents);

        assertEquals("XContent 2015-09", newName);
    }

    @Test
    public void testGetNewName_table_noVersion() throws Exception {
        TableContents tableContents = newTableContents(subIpsProject, "some.AContent");
        deepCopy.getPresentationModel().setSearchInput("A");
        deepCopy.getPresentationModel().setReplaceInput("X");

        String newName = deepCopy.getNewName(bSuper.getIpsPackageFragment(), tableContents);

        assertEquals("XContent", newName);
    }

    @Test
    public void testGetNewName_table_otherPackage_noVersion() throws Exception {
        TableContents tableContents = newTableContents(subIpsProject, "some.AContent");
        deepCopy.getPresentationModel().setSearchInput("A");
        deepCopy.getPresentationModel().setReplaceInput("X");
        IIpsPackageFragment ipsPackageFragment = bSuper.getIpsPackageFragment().createSubPackage("subPack", true, null);

        String newName = deepCopy.getNewName(ipsPackageFragment, tableContents);

        assertEquals("XContent", newName);
    }

}
