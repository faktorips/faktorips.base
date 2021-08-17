/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.refactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.NLS;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.refactor.IpsRefactoringModificationSet;
import org.faktorips.devtools.model.builder.IDependencyGraph;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.util.StringUtil;
import org.junit.Before;
import org.junit.Test;

public class MoveRenamePackageHelperTest extends AbstractIpsPluginTest {

    private static final String POLICY_CMPT_TYPE_QNAME = "data.coverages.PolicyCmptType"; //$NON-NLS-1$
    private static final String COVERAGE_QNAME = "data.coverages.Coverage"; //$NON-NLS-1$
    private static final String PRODUCT_A_QNAME = "data.products.ProductA"; //$NON-NLS-1$
    private static final String PRODUCT_B_QNAME = "data.products.ProductB"; //$NON-NLS-1$
    private static final String PRODUCT_C_QNAME = "data.products.subproducts.ProductC"; //$NON-NLS-1$
    private static final String COVERAGE_TYPE_NAME = "CoverageType"; //$NON-NLS-1$
    private static final String COVERAGE_TYPE_STATIC_NAME = "StaticCoverageType"; //$NON-NLS-1$
    private static final String COVERAGE_TYPE_QNAME = "model." + COVERAGE_TYPE_NAME; //$NON-NLS-1$
    private static final String PRODUCT_QNAME = "model.Product"; //$NON-NLS-1$

    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot ipsRoot;

    private IProductCmpt productA;
    private IProductCmptGeneration productAGen;
    private IProductCmpt productB;
    private IProductCmptGeneration productBGen;
    private IProductCmpt coverage;

    private IPolicyCmptType policyCmptType;

    private MoveRenamePackageHelper helper;
    private IProductCmptType productCmptType1;
    private IProductCmptType productCmptType2;
    private IProductCmpt productC;
    private IDependency[] refs;
    private IDependencyGraph dependencyGraph;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        ipsRoot = ipsProject.getIpsPackageFragmentRoots()[0];
        productCmptType1 = newProductCmptType(ipsProject, PRODUCT_QNAME);
        productCmptType2 = newProductCmptType(ipsProject, COVERAGE_TYPE_QNAME);
        IProductCmptTypeAssociation association = productCmptType1.newProductCmptTypeAssociation();
        association.setTarget(productCmptType2.getQualifiedName());
        association.setTargetRoleSingular(COVERAGE_TYPE_NAME);
        association.setTargetRolePlural(COVERAGE_TYPE_NAME + "s");
        IProductCmptTypeAssociation staticAssociation = productCmptType1.newProductCmptTypeAssociation();
        staticAssociation.setTarget(productCmptType2.getQualifiedName());
        staticAssociation.setTargetRoleSingular(COVERAGE_TYPE_STATIC_NAME);
        staticAssociation.setTargetRolePlural(COVERAGE_TYPE_STATIC_NAME + "s");
        staticAssociation.setChangingOverTime(false);
        productCmptType1.getIpsSrcFile().save(true, null);
        productCmptType2.getIpsSrcFile().save(true, null);

        coverage = newProductCmpt(productCmptType2, COVERAGE_QNAME);

        productA = newProductCmpt(productCmptType1, PRODUCT_A_QNAME);
        productAGen = productA.getProductCmptGeneration(0);
        productAGen.newLink(COVERAGE_TYPE_NAME).setTarget(coverage.getQualifiedName());
        productA.getIpsSrcFile().save(true, null);

        productB = newProductCmpt(productCmptType1, PRODUCT_B_QNAME);
        productBGen = productB.getProductCmptGeneration(0);
        productBGen.newLink(COVERAGE_TYPE_NAME).setTarget(coverage.getQualifiedName());
        productB.getIpsSrcFile().save(true, null);

        productC = newProductCmpt(productCmptType1, PRODUCT_C_QNAME);
        IProductCmptGeneration productCGen = productC.getProductCmptGeneration(0);
        productCGen.newLink(COVERAGE_TYPE_NAME).setTarget(coverage.getQualifiedName());
        productC.getIpsSrcFile().save(true, null);

        dependencyGraph = ((IpsModel)ipsProject.getIpsModel()).getDependencyGraph(ipsProject);
        refs = dependencyGraph.getDependants(coverage.getQualifiedNameType());

        policyCmptType = newPolicyCmptType(ipsProject, POLICY_CMPT_TYPE_QNAME);
        policyCmptType.getIpsSrcFile().save(true, null);
    }

    @Test
    public void testCheckInitialConditions_WithInValidAssociation() throws Exception {
        // Association with no Plural Name
        IProductCmptTypeAssociation newAssociation = productCmptType1.newProductCmptTypeAssociation();
        newAssociation.setTarget(productCmptType2.getQualifiedName());
        newAssociation.setTargetRoleSingular(COVERAGE_TYPE_NAME);

        RefactoringStatus status = new RefactoringStatus();
        IIpsPackageFragment source = ipsRoot.getIpsPackageFragment("data.products");

        helper = new MoveRenamePackageHelper(source);
        helper.checkInitialConditions(status);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testCheckInitialConditions_Valid() throws Exception {
        RefactoringStatus status = new RefactoringStatus();
        IIpsPackageFragment source = ipsRoot.getIpsPackageFragment("data.products");

        helper = new MoveRenamePackageHelper(source);
        helper.checkInitialConditions(status);

        assertTrue(status.isOK());
    }

    @Test
    public void testCheckFinalConditions() throws Exception {
        RefactoringStatus status = new RefactoringStatus();
        IIpsPackageFragment source = ipsRoot.getIpsPackageFragment("data.products");
        IIpsPackageFragment target = ipsRoot.getIpsPackageFragment("target");

        helper = new MoveRenamePackageHelper(source);
        helper.checkFinalConditions(target, status, new NullProgressMonitor());

        assertTrue(status.isOK());
        assertThatFilesOk(source, target);
    }

    @Test
    public void testCheckFinalConditions_WithInValidAssociation() throws Exception {
        // Association with no Plural Name
        IProductCmptTypeAssociation newAssociation = productCmptType1.newProductCmptTypeAssociation();
        newAssociation.setTarget(productCmptType2.getQualifiedName());
        newAssociation.setTargetRoleSingular(COVERAGE_TYPE_NAME);

        RefactoringStatus status = new RefactoringStatus();
        IIpsPackageFragment source = ipsRoot.getIpsPackageFragment("data.products");
        IIpsPackageFragment target = ipsRoot.getIpsPackageFragment("target");

        helper = new MoveRenamePackageHelper(source);
        helper.checkFinalConditions(target, status, new NullProgressMonitor());

        assertTrue(status.hasError());
        assertTrue(status.getEntries().length == 3);
        assertThatFilesOk(source, target);
    }

    private void assertThatFilesOk(IIpsPackageFragment source, IIpsPackageFragment target) {
        assertFalse(target.exists());
        assertTrue(source.exists());
        assertTrue(source.getIpsSrcFile("ProductA", IpsObjectType.PRODUCT_CMPT).exists());
        assertTrue(source.getIpsSrcFile("ProductB", IpsObjectType.PRODUCT_CMPT).exists());
        assertTrue(source.getSubPackage("subproducts").getIpsSrcFile("ProductC", IpsObjectType.PRODUCT_CMPT).exists());
    }

    @Test
    public void testGetAffectedIpsSrcFiles() throws Exception {
        IIpsPackageFragment source = ipsRoot.getIpsPackageFragment("data.products");
        IFile file = ((IFolder)source.getCorrespondingResource()).getFile("test.unknown");
        file.create(StringUtil.getInputStreamForString("Test content for file.", "UTF-8"), true, null);
        assertTrue(file.exists());

        helper = new MoveRenamePackageHelper(source);
        Set<IIpsSrcFile> ipsSrcFiles = helper.getAffectedIpsSrcFiles();
        assertEquals(3, ipsSrcFiles.size());

        ipsSrcFiles.contains(productA.getIpsSrcFile());
        ipsSrcFiles.contains(productB.getIpsSrcFile());
        ipsSrcFiles.contains(productC.getIpsSrcFile());
    }

    @Test
    public void testMovePackageFragment_InSameIpsProject() throws Exception {
        IIpsPackageFragment source = ipsRoot.getIpsPackageFragment("data.products");
        IIpsPackageFragment target = ipsRoot.getIpsPackageFragment("target");

        assertTrue(source.exists());
        assertFalse(target.exists());

        IFile file = ((IFolder)source.getCorrespondingResource()).getFile("test.unknown");
        file.create(StringUtil.getInputStreamForString("Test content for file.", "UTF-8"), true, null);
        assertTrue(file.exists());

        helper = new MoveRenamePackageHelper(source);
        IpsRefactoringModificationSet modificationSet = helper.movePackageFragment(target, new NullProgressMonitor());

        assertNotNull(modificationSet);
        assertEquals(3, modificationSet.getModifications().size());

        assertFalse(source.exists());

        IIpsSrcFile oldIpsSourceFileProductA = source.getIpsSrcFile("ProductA", IpsObjectType.PRODUCT_CMPT);
        assertFalse(oldIpsSourceFileProductA.exists());

        assertTrue(target.exists());
        IIpsPackageFragment newTarget = ipsRoot.getIpsPackageFragment("target.products");
        assertTrue(newTarget.exists());

        IFile newFile = ((IFolder)newTarget.getCorrespondingResource()).getFile("test.unknown");
        assertTrue(newFile.exists());

        IIpsSrcFile newIpsSrcFileProductA = newTarget.getIpsSrcFile("ProductA", IpsObjectType.PRODUCT_CMPT);
        assertTrue(newIpsSrcFileProductA.exists());

        IIpsSrcFile newIpsSrcFileProductB = newTarget.getIpsSrcFile("ProductB", IpsObjectType.PRODUCT_CMPT);
        assertTrue(newIpsSrcFileProductB.exists());

        IIpsPackageFragment newTargetSubPackage = ipsRoot.getIpsPackageFragment("target.products.subproducts");

        IIpsSrcFile newIpsSrcFileProductC = newTargetSubPackage.getIpsSrcFile("ProductC", IpsObjectType.PRODUCT_CMPT);
        assertTrue(newIpsSrcFileProductC.exists());

    }

    @Test
    public void testMovePackageFragment_EmptyFolder() throws Exception {
        IIpsPackageFragment empty = ipsRoot.createPackageFragment("empty", true, null);
        IIpsPackageFragment target = ipsRoot.createPackageFragment("target", true, null);

        assertTrue(empty.exists());

        helper = new MoveRenamePackageHelper(empty);
        helper.movePackageFragment(target, new NullProgressMonitor());

        assertFalse(empty.exists());

        empty = ipsRoot.getIpsPackageFragment("target.empty");

        assertTrue(empty.exists());
    }

    @Test
    public void testMovePackageFragment_InOtherIpsProject() throws Exception {
        IIpsProject ipsProject2 = newIpsProject();
        IIpsPackageFragmentRoot ipsRoot2 = ipsProject2.getIpsPackageFragmentRoots()[0];
        IIpsObjectPath path = ipsProject2.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject);
        ipsProject2.setIpsObjectPath(path);
        assertTrue(ipsProject2.isReferencing(ipsProject));

        IIpsPackageFragment source = ipsRoot.getIpsPackageFragment("data.products");
        IIpsPackageFragment target = ipsRoot2.getIpsPackageFragment("target");

        assertTrue(source.exists());
        assertFalse(target.exists());

        IFile file = ((IFolder)source.getCorrespondingResource()).getFile("test.unknown");
        file.create(StringUtil.getInputStreamForString("Test content for file.", "UTF-8"), true, null);
        assertTrue(file.exists());

        helper = new MoveRenamePackageHelper(source);
        IpsRefactoringModificationSet modificationSet = helper.movePackageFragment(target, new NullProgressMonitor());

        assertNotNull(modificationSet);
        assertEquals(3, modificationSet.getModifications().size());

        assertFalse(source.exists());

        IIpsSrcFile oldIpsSourceFileProductA = source.getIpsSrcFile("ProductA", IpsObjectType.PRODUCT_CMPT);
        assertFalse(oldIpsSourceFileProductA.exists());

        assertTrue(target.exists());
        IIpsPackageFragment newTarget = ipsRoot2.getIpsPackageFragment("target.products");
        assertTrue(newTarget.exists());

        IFile newFile = ((IFolder)newTarget.getCorrespondingResource()).getFile("test.unknown");
        assertTrue(newFile.exists());

        IIpsSrcFile newIpsSrcFileProductA = newTarget.getIpsSrcFile("ProductA", IpsObjectType.PRODUCT_CMPT);
        assertTrue(newIpsSrcFileProductA.exists());

        IIpsSrcFile newIpsSrcFileProductB = newTarget.getIpsSrcFile("ProductB", IpsObjectType.PRODUCT_CMPT);
        assertTrue(newIpsSrcFileProductB.exists());

        IIpsPackageFragment newTargetSubPackage = ipsRoot2.getIpsPackageFragment("target.products.subproducts");

        IIpsSrcFile newIpsSrcFileProductC = newTargetSubPackage.getIpsSrcFile("ProductC", IpsObjectType.PRODUCT_CMPT);
        assertTrue(newIpsSrcFileProductC.exists());
    }

    @Test
    public void testMovePackageFragment_InDefaultPackage() throws Exception {
        IIpsPackageFragment source = ipsRoot.getIpsPackageFragment("data.products");
        IIpsPackageFragment target = ipsRoot.getIpsPackageFragment("");

        assertTrue(source.exists());
        assertTrue(target.exists());

        IFile file = ((IFolder)source.getCorrespondingResource()).getFile("test.unknown");
        file.create(StringUtil.getInputStreamForString("Test content for file.", "UTF-8"), true, null);
        assertTrue(file.exists());

        helper = new MoveRenamePackageHelper(source);
        IpsRefactoringModificationSet modificationSet = helper.movePackageFragment(target, new NullProgressMonitor());

        assertNotNull(modificationSet);
        assertEquals(3, modificationSet.getModifications().size());

        assertFalse(source.exists());

        IIpsSrcFile oldIpsSourceFileProductA = source.getIpsSrcFile("ProductA", IpsObjectType.PRODUCT_CMPT);
        assertFalse(oldIpsSourceFileProductA.exists());

        assertTrue(target.exists());
        IIpsPackageFragment newTarget = ipsRoot.getIpsPackageFragment("products");
        assertTrue(newTarget.exists());

        IFile newFile = ((IFolder)newTarget.getCorrespondingResource()).getFile("test.unknown");
        assertTrue(newFile.exists());

        IIpsSrcFile newIpsSrcFileProductA = newTarget.getIpsSrcFile("ProductA", IpsObjectType.PRODUCT_CMPT);
        assertTrue(newIpsSrcFileProductA.exists());

        IIpsSrcFile newIpsSrcFileProductB = newTarget.getIpsSrcFile("ProductB", IpsObjectType.PRODUCT_CMPT);
        assertTrue(newIpsSrcFileProductB.exists());

        IIpsPackageFragment newTargetSubPackage = ipsRoot.getIpsPackageFragment("products.subproducts");

        IIpsSrcFile newIpsSrcFileProductC = newTargetSubPackage.getIpsSrcFile("ProductC", IpsObjectType.PRODUCT_CMPT);
        assertTrue(newIpsSrcFileProductC.exists());
    }

    @Test
    public void testMovePackageFragment_DefaultPackageInOtherIpsProject() throws Exception {
        IIpsProject ipsProject2 = newIpsProject();
        IIpsPackageFragmentRoot ipsRoot2 = ipsProject2.getIpsPackageFragmentRoots()[0];
        IIpsObjectPath path = ipsProject2.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject);
        ipsProject2.setIpsObjectPath(path);
        assertTrue(ipsProject2.isReferencing(ipsProject));

        IIpsPackageFragment source = ipsRoot.getIpsPackageFragment("");
        IIpsPackageFragment target = ipsRoot2.getIpsPackageFragment("target");

        assertTrue(source.exists());
        assertFalse(target.exists());

        IFile file = ((IFolder)source.getCorrespondingResource()).getFile("test.unknown");
        file.create(StringUtil.getInputStreamForString("Test content for file.", "UTF-8"), true, null);
        assertTrue(file.exists());

        helper = new MoveRenamePackageHelper(source);
        IpsRefactoringModificationSet modificationSet = helper.movePackageFragment(target, new NullProgressMonitor());

        assertNotNull(modificationSet);
        assertEquals(8, modificationSet.getModifications().size());

        assertFalse(source.exists());

        IIpsSrcFile oldIpsSourceFileProductA = source.getIpsSrcFile("ProductA", IpsObjectType.PRODUCT_CMPT);
        assertFalse(oldIpsSourceFileProductA.exists());

        assertTrue(target.exists());

        IFile newFile = ((IFolder)target.getCorrespondingResource()).getFile("test.unknown");
        assertTrue(newFile.exists());

        IIpsPackageFragment newTargetProducts = ipsRoot2.getIpsPackageFragment("target.data.products");
        assertTrue(newTargetProducts.exists());
        IIpsSrcFile newIpsSrcFileProductA = newTargetProducts.getIpsSrcFile("ProductA", IpsObjectType.PRODUCT_CMPT);
        assertTrue(newIpsSrcFileProductA.exists());

        IIpsSrcFile newIpsSrcFileProductB = newTargetProducts.getIpsSrcFile("ProductB", IpsObjectType.PRODUCT_CMPT);
        assertTrue(newIpsSrcFileProductB.exists());

        IIpsPackageFragment newTargetProductsSubPackage = ipsRoot2
                .getIpsPackageFragment("target.data.products.subproducts");

        IIpsSrcFile newIpsSrcFileProductC = newTargetProductsSubPackage.getIpsSrcFile("ProductC",
                IpsObjectType.PRODUCT_CMPT);
        assertTrue(newIpsSrcFileProductC.exists());
    }

    @Test
    public void testRenamePackageFragment() throws Exception {
        IIpsPackageFragment sourcePackage = coverage.getIpsPackageFragment();
        IIpsPackageFragment targetPackage = ipsRoot.getIpsPackageFragment("data.renamed");

        assertFalse(targetPackage.exists());
        assertTrue(coverage.getIpsSrcFile().exists());

        IIpsPackageFragment pack = ipsRoot.createPackageFragment("data.coverages.subpackage", true, null);
        IFile file = ((IFolder)pack.getCorrespondingResource()).getFile("test.unknown");
        file.create(StringUtil.getInputStreamForString("Test content for file.", "UTF-8"), true, null);
        assertTrue(pack.exists());
        assertTrue(file.exists());

        helper = new MoveRenamePackageHelper(sourcePackage);
        helper.renamePackageFragment("data.renamed", new NullProgressMonitor());

        assertTrue(targetPackage.exists());
        assertFalse(sourcePackage.exists());

        IIpsSrcFile targetFile = targetPackage.getIpsSrcFile(coverage.getName(), IpsObjectType.PRODUCT_CMPT);
        assertTrue(targetFile.exists());

        IProductCmpt targetObject = (IProductCmpt)targetFile.getIpsObject();
        dependencyGraph.reInit();
        IDependency[] targetRefs = dependencyGraph.getDependants(targetObject.getQualifiedNameType());

        assertEquals(refs.length, targetRefs.length);

        assertFalse(sourcePackage.getIpsSrcFile(policyCmptType.getName(), IpsObjectType.POLICY_CMPT_TYPE).exists());
        assertNotNull(targetPackage.getIpsSrcFile(policyCmptType.getName(), IpsObjectType.POLICY_CMPT_TYPE));

        assertFalse(pack.exists());
        assertFalse(file.exists());

        pack = ipsRoot.createPackageFragment("data.renamed.subpackage", true, null);
        assertTrue(pack.exists());
        file = ((IFolder)pack.getCorrespondingResource()).getFile("test.unknown");
        assertTrue(file.exists());
    }

    @Test
    public void testRenamePackageFragment_EmptyFolder() throws Exception {
        IIpsPackageFragment pack = ipsRoot.createPackageFragment("empty", true, null);

        assertTrue(pack.exists());

        helper = new MoveRenamePackageHelper(pack);
        helper.renamePackageFragment("empty2", new NullProgressMonitor());

        assertFalse(pack.exists());

        pack = ipsRoot.getIpsPackageFragment("empty2");

        assertTrue(pack.exists());
    }

    @Test
    public void testRenamePackageFragment_ContainingOnlyPackages() throws Exception {
        IIpsPackageFragment level1 = ipsRoot.createPackageFragment("level1", true, null);

        ipsRoot.createPackageFragment("level1.level2_1", true, null);
        ipsRoot.createPackageFragment("level1.level2_2", true, null);
        ipsRoot.createPackageFragment("level1.level2_3", true, null);
        ipsRoot.createPackageFragment("level1.level2_4", true, null);

        helper = new MoveRenamePackageHelper(level1);
        helper.renamePackageFragment("levela", new NullProgressMonitor());

        assertFalse(level1.exists());
        assertTrue(ipsRoot.getIpsPackageFragment("levela").exists());
        assertTrue(ipsRoot.getIpsPackageFragment("levela.level2_1").exists());
        assertTrue(ipsRoot.getIpsPackageFragment("levela.level2_2").exists());
        assertTrue(ipsRoot.getIpsPackageFragment("levela.level2_3").exists());
        assertTrue(ipsRoot.getIpsPackageFragment("levela.level2_4").exists());
    }

    @Test
    public void testRenamePackageFragment_CreateSubpackageByRename() throws Exception {
        IIpsPackageFragment source = ipsRoot.createPackageFragment("source", true, null);
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "source.TestProductType");
        newProductCmpt(productCmptType, "source.TestProduct");

        helper = new MoveRenamePackageHelper(source);
        helper.renamePackageFragment("source.target", new NullProgressMonitor());

        assertTrue(source.exists());
        assertTrue(ipsRoot.getIpsPackageFragment("source").exists());
        assertTrue(ipsRoot.getIpsPackageFragment("source.target").exists());
        assertTrue(ipsProject.findIpsObject(IpsObjectType.PRODUCT_CMPT_TYPE, "source.target.TestProductType").exists());
        assertTrue(ipsProject.findIpsObject(IpsObjectType.PRODUCT_CMPT, "source.target.TestProduct").exists());
        assertNull(ipsProject.findIpsObject(IpsObjectType.PRODUCT_CMPT, "source.TestProduct"));
    }

    @Test
    public void testIsSourceFilesSavedRequired() throws Exception {
        IIpsPackageFragment source = ipsRoot.getIpsPackageFragment("data.products");
        helper = new MoveRenamePackageHelper(source);
        assertFalse(helper.isSourceFilesSavedRequired());
    }

    @Test
    public void testValidateUserInput_TargetPackageNotValid() throws Exception {
        IIpsPackageFragment source = ipsRoot.getIpsPackageFragment("data.products");
        RefactoringStatus status = new RefactoringStatus();

        helper = new MoveRenamePackageHelper(source);
        helper.validateUserInput(null, status);
        assertTrue(status.hasFatalError());
        assertEquals(Messages.MoveRenamePackageHelper_errorTargetPackageNotValid, status.getEntryWithHighestSeverity()
                .getMessage());
    }

    @Test
    public void testValidateUserInput_TargetPackageExists() throws Exception {
        IIpsPackageFragment source = ipsRoot.getIpsPackageFragment("data.products");
        RefactoringStatus status = new RefactoringStatus();

        helper = new MoveRenamePackageHelper(source);
        helper.validateUserInput(source, status);
        assertTrue(status.hasFatalError());
        assertEquals(NLS.bind(Messages.MoveRenamePackageHelper_errorPackageAlreadyContains, source.getName()), status
                .getEntryWithHighestSeverity().getMessage());
    }

    @Test
    public void testValidateUserInput_TargetPackageSubPackage() throws Exception {
        IIpsPackageFragment source = ipsRoot.getIpsPackageFragment("data.products");
        IIpsPackageFragment target = ipsRoot.getIpsPackageFragment("data.products.subProducts");
        RefactoringStatus status = new RefactoringStatus();

        helper = new MoveRenamePackageHelper(source);
        helper.validateUserInput(target, status);
        assertTrue(status.hasFatalError());
        assertEquals(
                NLS.bind(Messages.MoveRenamePackageHelper_errorMessage_disallowMoveIntoSubPackage, source.getName()),
                status.getEntryWithHighestSeverity().getMessage());
    }

    @Test
    public void testValidateUserInput_prohibitMoveDefaultPackage() throws Exception {
        RefactoringStatus status = new RefactoringStatus();
        IIpsPackageFragment source = ipsRoot.getIpsPackageFragment("");
        IIpsPackageFragment target = ipsRoot.getIpsPackageFragment("data.products");

        helper = new MoveRenamePackageHelper(source);
        helper.validateUserInput(target, status);

        assertFalse(status.isOK());
    }
}
