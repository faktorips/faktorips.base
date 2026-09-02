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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus.CopyOrLink;
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
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.AssociationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    @BeforeEach
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

    private void setVersionAndStrategy() {
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

    @Test
    public void testGetSegmentsToIgnore_SamePackageAsRoot() throws Exception {
        ProductCmpt other = newProductCmpt(superType, "some.Other");

        assertThat(deepCopy.getSegmentsToIgnore(other), is(1));
    }

    @Test
    public void testGetSegmentsToIgnore_SubPackageOfRoot() throws Exception {
        ProductCmpt child = newProductCmpt(superType, "some.sub.Child");

        assertThat(deepCopy.getSegmentsToIgnore(child), is(1));
    }

    @Test
    public void testGetSegmentsToIgnore_SiblingPackageOfRoot() throws Exception {
        ProductCmpt sibling = newProductCmpt(superType, "other.Sibling");

        assertThat(deepCopy.getSegmentsToIgnore(sibling), is(0));
    }

    @Test
    public void testBuildTargetPackageName_Root_LandsDirectlyInTargetPackage() throws Exception {
        IIpsPackageFragment targetBase = bSuper.getIpsPackageFragment().getRoot().getIpsPackageFragment("foo2026");

        String targetPackageName = deepCopy.buildTargetPackageName(targetBase, bSuper,
                deepCopy.getSegmentsToIgnore(bSuper));

        assertThat(targetPackageName, is("foo2026"));
    }

    @Test
    public void testBuildTargetPackageName_PackageOutsideOfRoot_KeepCurrentPackage() throws Exception {
        ProductCmpt sibling = newProductCmpt(superType, "other.Sibling");
        IIpsPackageFragment targetBase = bSuper.getIpsPackageFragment().getRoot().getIpsPackageFragment("foo2026");

        String targetPackageName = deepCopy.buildTargetPackageName(targetBase, sibling,
                deepCopy.getSegmentsToIgnore(sibling));

        assertThat(targetPackageName, is("other"));
    }

    @Test
    public void testBuildTargetPackageName_SubPackageOfRoot_NestsUnderTargetBase() throws Exception {
        ProductCmpt child = newProductCmpt(superType, "some.sub.Child");
        IIpsPackageFragment targetBase = bSuper.getIpsPackageFragment().getRoot().getIpsPackageFragment("foo2026");

        String targetPackageName = deepCopy.buildTargetPackageName(targetBase, child,
                deepCopy.getSegmentsToIgnore(child));

        assertThat(targetPackageName, is("foo2026.sub"));
    }

    @Test
    public void testGetCommonSegmentsToIgnore_EmptySet() {
        assertThat(deepCopy.getCommonSegmentsToIgnore(Set.of()), is(0));
    }

    @Test
    public void testGetCommonSegmentsToIgnore_CommonPackage() throws Exception {
        ProductCmptReference refBSuper = new ProductCmptReference(null, null, bSuper, null);
        ProductCmptReference refASub = new ProductCmptReference(null, null, aSub, null);

        assertThat(deepCopy.getCommonSegmentsToIgnore(Set.of(refBSuper, refASub)), is(1));
    }

    @Test
    public void testGetCommonSegmentsToIgnore_SingleElement_ReturnsAllSegments() throws Exception {
        ProductCmpt child = newProductCmpt(superType, "some.sub.Child");
        ProductCmptReference refChild = new ProductCmptReference(null, null, child, null);

        assertThat(deepCopy.getCommonSegmentsToIgnore(Set.of(refChild)), is(2));
    }

    @Test
    public void testGetCommonSegmentsToIgnore_SiblingPackage_ReturnsZero() throws Exception {
        ProductCmpt sibling = newProductCmpt(superType, "other.Sibling");
        ProductCmptReference refBSuper = new ProductCmptReference(null, null, bSuper, null);
        ProductCmptReference refSibling = new ProductCmptReference(null, null, sibling, null);

        assertThat(deepCopy.getCommonSegmentsToIgnore(Set.of(refBSuper, refSibling)), is(0));
    }

    @Test
    public void testGetSegmentsToIgnore_NullSource_ReturnsZero() {
        assertThat(deepCopy.getSegmentsToIgnore(null), is(0));
    }

    @Test
    public void testBuildTargetPackageName_NullSource_ReturnsEmpty() {
        IIpsPackageFragment targetBase = bSuper.getIpsPackageFragment().getRoot().getIpsPackageFragment("foo2026");

        String targetPackageName = deepCopy.buildTargetPackageName(targetBase, null, 0);

        assertThat(targetPackageName, is(""));
    }

    @Test
    public void testCreateTargetNodes_IncludesAssociationElementsMarkedAsCopy() throws Exception {
        IProductCmptTypeAssociation association = superType.newProductCmptTypeAssociation();
        association.setAssociationType(AssociationType.ASSOCIATION);
        association.setTarget(superType.getQualifiedName());
        association.setTargetRoleSingular("Assoc");

        ProductCmpt associated = newProductCmpt(superType, "some.Associated");
        IProductCmptGeneration generation = bSuper.getProductCmptGeneration(0);
        IProductCmptLink link = generation.newLink("Assoc");
        link.setTarget(associated.getQualifiedName());
        bSuper.getIpsSrcFile().save(null);

        DeepCopyPresentationModel presentationModel = new DeepCopyPresentationModel(generation);
        presentationModel.setTargetPackageRoot(bSuper.getIpsPackageFragment().getRoot());
        presentationModel.setTargetPackage(bSuper.getIpsPackageFragment());
        DeepCopyPreview preview = new DeepCopyPreview(presentationModel);
        IProductCmptStructureReference associatedReference = findReference(presentationModel.getStructure(),
                associated);
        presentationModel.getTreeStatus().setCopyOrLink(associatedReference, CopyOrLink.COPY);

        assertThat(presentationModel.getAllCopyElements(true), hasItem(associatedReference));
        assertThat(presentationModel.getAllCopyElements(false), not(hasItem(associatedReference)));

        preview.createTargetNodes(new NullProgressMonitor());

        assertThat(preview.getNewName(associated), is(notNullValue()));
    }

    private IProductCmptStructureReference findReference(IProductCmptTreeStructure structure, ProductCmpt target) {
        for (IProductCmptStructureReference candidate : structure.toSet(false)) {
            if (candidate instanceof IProductCmptReference productCmptReference
                    && productCmptReference.getProductCmpt().equals(target)) {
                return candidate;
            }
        }
        throw new IllegalStateException("No reference found for " + target);
    }

}
