/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.refactor;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.util.StringUtil;

/**
 * Tests for move- and rename-operation
 * 
 * @author Thorsten Guenther
 */
public class MoveOperationTest extends AbstractIpsPluginTest {

    private static final String POLICY_CMPT_TYPE_QNAME = "data.coverages.PolicyCmptType"; //$NON-NLS-1$
    private static final String PRODUCT_B_QNAME = "data.products.ProductB"; //$NON-NLS-1$
    private static final String PRODUCT_A_QNAME = "data.products.ProductA"; //$NON-NLS-1$
    private static final String COVERAGE_QNAME = "data.coverages.Coverage"; //$NON-NLS-1$
    private static final String COVERAGE_TYPE_NAME = "CoverageType"; //$NON-NLS-1$
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

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject();
        ipsRoot = ipsProject.getIpsPackageFragmentRoots()[0];
        IProductCmptType productCmptType1 = newProductCmptType(ipsProject, PRODUCT_QNAME);
        IProductCmptType productCmptType2 = newProductCmptType(ipsProject, COVERAGE_TYPE_QNAME);
        IProductCmptTypeAssociation relation = productCmptType1.newProductCmptTypeAssociation();
        relation.setTarget(productCmptType2.getQualifiedName());
        relation.setTargetRoleSingular(COVERAGE_TYPE_NAME);
        productCmptType1.getIpsSrcFile().save(true, null);

        coverage = newProductCmpt(productCmptType2, COVERAGE_QNAME);

        productA = newProductCmpt(productCmptType1, PRODUCT_A_QNAME);
        productAGen = productA.getProductCmptGeneration(0);
        productAGen.newLink(COVERAGE_TYPE_NAME).setTarget(coverage.getQualifiedName());
        productA.getIpsSrcFile().save(true, null);

        productB = newProductCmpt(productCmptType1, PRODUCT_B_QNAME);
        productBGen = productB.getProductCmptGeneration(0);
        productBGen.newLink(COVERAGE_TYPE_NAME).setTarget(coverage.getQualifiedName());
        productB.getIpsSrcFile().save(true, null);

        IProductCmptGeneration[] refs = ipsProject.findReferencingProductCmptGenerations(coverage
                .getQualifiedNameType());
        assertEquals(2, refs.length);

        policyCmptType = newPolicyCmptType(ipsProject, POLICY_CMPT_TYPE_QNAME);
        policyCmptType.getIpsSrcFile().save(true, null);
    }

    /**
     * For this test, one object of the comfort-product of the default test content is moved to a
     * product with the same package, but another name. After that, the new file is expected to be
     * existent and the references to this object have to be the same as to the source.
     */
    public void testRenameProduct() throws CoreException, InvocationTargetException, InterruptedException {
        String targetName = "Moved" + coverage.getName(); //$NON-NLS-1$
        IIpsSrcFile targetFile = coverage.getIpsPackageFragment().getIpsSrcFile(targetName, IpsObjectType.PRODUCT_CMPT);

        assertFalse(targetFile.exists());
        assertTrue(coverage.getIpsSrcFile().exists());

        IProductCmptGeneration[] sourceRefs = ipsProject.findReferencingProductCmptGenerations(coverage
                .getQualifiedNameType());

        String targetQName = coverage.getIpsPackageFragment().getName() + '.' + targetName;
        MoveOperation move = new MoveOperation(coverage, targetQName);
        move.run(null);

        assertTrue(targetFile.exists());
        assertFalse(coverage.getIpsSrcFile().exists());
        IProductCmpt targetObject = (IProductCmpt)targetFile.getIpsObject();
        IProductCmptGeneration[] targetRefs = targetObject.getIpsProject().findReferencingProductCmptGenerations(
                targetObject.getQualifiedNameType());

        assertEquals(sourceRefs.length, targetRefs.length);
    }

    /**
     * For this test, one package is renamed. After that, the new package is expected to be existent
     * and the references to the contained objects have to be the same as to the source.
     */
    public void testRenamePackage() throws CoreException, InvocationTargetException, InterruptedException {
        IIpsPackageFragment sourcePackage = coverage.getIpsPackageFragment();
        IIpsPackageFragment target = sourcePackage.getRoot().getIpsPackageFragment("renamed");

        assertFalse(target.exists());
        assertTrue(coverage.getIpsSrcFile().exists());

        IProductCmptGeneration[] sourceRefs = coverage.getIpsProject().findReferencingProductCmptGenerations(
                coverage.getQualifiedNameType());

        MoveOperation move = new MoveOperation(new IIpsElement[] { sourcePackage }, new String[] { "renamed" });
        move.run(null);

        assertTrue(target.exists());
        assertFalse(sourcePackage.exists());

        IIpsSrcFile targetFile = target.getIpsSrcFile(coverage.getName(), IpsObjectType.PRODUCT_CMPT);

        assertTrue(targetFile.exists());

        IProductCmpt targetObject = (IProductCmpt)targetFile.getIpsObject();
        IProductCmptGeneration[] targetRefs = targetObject.getIpsProject().findReferencingProductCmptGenerations(
                targetObject.getQualifiedNameType());

        assertEquals(sourceRefs.length, targetRefs.length);

        assertFalse(sourcePackage.getIpsSrcFile(policyCmptType.getName(), IpsObjectType.POLICY_CMPT_TYPE).exists());
        assertNotNull(target.getIpsSrcFile(policyCmptType.getName(), IpsObjectType.POLICY_CMPT_TYPE));
    }

    /**
     * For this test, one object is moved to a new, non-existing package. After that, the new file
     * is expected to be existent and the references to this object have to be the same as to the
     * source.
     */
    public void testMoveProduct() throws CoreException, InvocationTargetException, InterruptedException {
        String runtimeId = coverage.getRuntimeId();

        IIpsSrcFile target = coverage.getIpsPackageFragment().getRoot().getIpsPackageFragment("test.my.pack")
                .getIpsSrcFile(coverage.getName(), IpsObjectType.PRODUCT_CMPT);
        String targetName = target.getIpsPackageFragment().getName() + "." + coverage.getName();

        assertFalse(target.exists());
        assertTrue(coverage.getIpsSrcFile().exists());

        IProductCmptGeneration[] sourceRefs = ipsProject.findReferencingProductCmptGenerations(coverage
                .getQualifiedNameType());

        MoveOperation move = new MoveOperation(coverage, targetName);
        move.run(null);

        // assert that the runtime id hasn't changed
        assertEquals(runtimeId, ((IProductCmpt)target.getIpsObject()).getRuntimeId());

        assertFalse(target.isDirty());

        assertTrue(target.exists());
        assertFalse(coverage.getIpsSrcFile().exists());
        IProductCmpt targetObject = (IProductCmpt)target.getIpsObject();
        IProductCmptGeneration[] targetRefs = targetObject.getIpsProject().findReferencingProductCmptGenerations(
                targetObject.getQualifiedNameType());

        assertEquals(sourceRefs.length, targetRefs.length);
    }

    /**
     * Test if the references of product components will be correctly updated in ips test cases,
     */
    public void testMoveProductRefByTestCase() throws Exception {
        IIpsSrcFile target = coverage.getIpsPackageFragment().getRoot().getIpsPackageFragment("test.my.pack")
                .getIpsSrcFile(coverage.getName() + ".ipsproduct");
        String targetName = target.getIpsPackageFragment().getName() + "." + coverage.getName();
        assertFalse(target.exists());
        assertTrue(coverage.getIpsSrcFile().exists());

        ITestCase testCase = (ITestCase)newIpsObject(ipsProject, IpsObjectType.TEST_CASE, "TestCase1");
        ITestPolicyCmpt testPolicyCmpt = testCase.newTestPolicyCmpt();
        testPolicyCmpt.setProductCmpt(coverage.getQualifiedName());
        testCase.getIpsSrcFile().save(true, null);

        MoveOperation move = new MoveOperation(coverage, targetName);
        move.run(null);

        // assert move
        assertFalse(target.isDirty());
        assertTrue(target.exists());
        assertFalse(coverage.getIpsSrcFile().exists());

        // assert references to test case
        IProductCmpt productCmpt = ipsProject.findProductCmptByRuntimeId(coverage.getRuntimeId());
        assertEquals(productCmpt.getQualifiedName(), testPolicyCmpt.getProductCmpt());
        assertEquals(productCmpt, testPolicyCmpt.findProductCmpt(ipsProject));
        assertFalse(testCase.getIpsSrcFile().isDirty());

        move = new MoveOperation(productCmpt, targetName + "_2");
        move.run(null);
        productCmpt = ipsProject.findProductCmptByRuntimeId(coverage.getRuntimeId());

        assertEquals(StringUtil.unqualifiedName(productCmpt.getQualifiedName()), testPolicyCmpt.getName());
    }

    /**
     * Test if the references of table contents will be correctly updated in product cmpts,
     */
    public void testTableContentRefByProductCmpt() throws Exception {
        // create table content
        IIpsSrcFile file = coverage.getIpsPackageFragment().createIpsFile(IpsObjectType.TABLE_CONTENTS, "table", true,
                null);
        assertTrue(file.exists());
        ITableContents tableContent = (ITableContents)file.getIpsObject();
        assertNotNull(tableContent);

        // add ref to table content
        coverage.newGeneration();
        IIpsObjectGeneration[] generations = coverage.getGenerationsOrderedByValidDate();
        for (IIpsObjectGeneration generation : generations) {
            ITableContentUsage tableContentUsage = ((IProductCmptGeneration)generation).newTableContentUsage();
            tableContentUsage.setTableContentName(tableContent.getQualifiedName());
        }

        MoveOperation move = new MoveOperation(new IIpsElement[] { file.getIpsObject() }, new String[] { "table" });
        move.run(null);
        IIpsSrcFile target = coverage.getIpsPackageFragment().getRoot().getDefaultIpsPackageFragment().getIpsSrcFile(
                IpsObjectType.TABLE_CONTENTS.getFileName("table"));

        // assert move
        assertTrue(target.exists());
        assertFalse(file.exists());

        // assert references to moved table content in product componet
        for (IIpsObjectGeneration generation : generations) {
            ITableContentUsage[] tableContentUsages = ((IProductCmptGeneration)generation).getTableContentUsages();
            for (ITableContentUsage tableContentUsage : tableContentUsages) {
                assertEquals(target.getIpsObject().getQualifiedName(), tableContentUsage.getTableContentName());
            }
        }
    }

    /**
     * For this test, one package of the comfort-product of the default test content is renamed.
     * After that, the new package is expected to be existant and the references to the contained
     * objects have to be the same as to the source.
     */
    public void testMovePackage() throws CoreException, InvocationTargetException, InterruptedException {
        IIpsPackageFragment sourcePackage = coverage.getIpsPackageFragment();
        ITestCase testCase = (ITestCase)sourcePackage.createIpsFile(IpsObjectType.TEST_CASE, "testcase", true, null)
                .getIpsObject();
        ITableContents tableContents = (ITableContents)sourcePackage.createIpsFile(IpsObjectType.TABLE_CONTENTS,
                "tablecontents", true, null).getIpsObject();
        IIpsPackageFragment target = sourcePackage.getRoot().getIpsPackageFragment("moved");

        assertFalse(target.exists());
        assertTrue(coverage.getIpsSrcFile().exists());
        assertTrue(testCase.getIpsSrcFile().exists());
        assertTrue(tableContents.getIpsSrcFile().exists());

        IProductCmptGeneration[] sourceRefs = coverage.getIpsProject().findReferencingProductCmptGenerations(
                coverage.getQualifiedNameType());

        MoveOperation move = new MoveOperation(new IIpsElement[] { sourcePackage }, target);
        move.run(null);

        assertTrue(target.exists());
        assertFalse(sourcePackage.exists());
        assertFalse(coverage.getIpsSrcFile().exists());
        assertFalse(testCase.getIpsSrcFile().exists());
        assertFalse(tableContents.getIpsSrcFile().exists());

        target = target.getRoot().getIpsPackageFragment("moved.coverages");

        IIpsSrcFile coverageFile = target.getIpsSrcFile(coverage.getName(), coverage.getIpsObjectType());

        assertTrue(coverageFile.exists());

        IProductCmpt targetObject = (IProductCmpt)coverageFile.getIpsObject();
        IProductCmptGeneration[] targetRefs = targetObject.getIpsProject().findReferencingProductCmptGenerations(
                targetObject.getQualifiedNameType());

        assertEquals(sourceRefs.length, targetRefs.length);
        assertTrue(target.getIpsSrcFile(IpsObjectType.TABLE_CONTENTS.getFileName("tablecontents")).exists());
        assertTrue(target.getIpsSrcFile(IpsObjectType.TEST_CASE.getFileName("testcase")).exists());

        assertFalse(sourcePackage.getIpsSrcFile(policyCmptType.getName(), IpsObjectType.POLICY_CMPT_TYPE).exists());
        assertNotNull(target.getIpsSrcFile(policyCmptType.getName(), IpsObjectType.POLICY_CMPT_TYPE));
    }

    public void testMovePackageWithUnsupportetFile() throws Exception {
        IIpsPackageFragment sourcePackage = coverage.getIpsPackageFragment();
        IIpsPackageFragment target = sourcePackage.getRoot().getIpsPackageFragment("moved");

        assertFalse(target.exists());
        assertTrue(coverage.getIpsSrcFile().exists());

        // test move of unsupported file
        testMoveOfUnsupportedObject(IpsObjectType.TEST_CASE_TYPE, sourcePackage, target);
        testMoveOfUnsupportedObject(IpsObjectType.TABLE_STRUCTURE, sourcePackage, target);

        // test move valid types again
        MoveOperation move = new MoveOperation(new IIpsElement[] { sourcePackage }, target);
        move.run(null);
        assertTrue(target.exists());
        assertFalse(sourcePackage.exists());
    }

    private void testMoveOfUnsupportedObject(IpsObjectType type,
            IIpsPackageFragment sourcePackage,
            IIpsPackageFragment target) throws CoreException, InvocationTargetException, InterruptedException {
        IIpsSrcFile src = sourcePackage.createIpsFile(type, "unsupported", true, null);
        boolean exceptionThrown = false;
        try {
            MoveOperation move = new MoveOperation(new IIpsElement[] { sourcePackage }, target);
            move.run(null);
        } catch (CoreException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        assertFalse(target.exists());
        assertTrue(sourcePackage.exists());
        src.getCorrespondingFile().delete(true, null);
    }

    public void testMovePackageInDifferentRoot() throws Exception {
        IIpsPackageFragmentRoot targetRoot = createIpsPackageFrgmtRoot();

        IIpsPackageFragment sourcePackage = coverage.getIpsPackageFragment();
        IIpsPackageFragment target = targetRoot.getIpsPackageFragment("moved");

        assertFalse(target.exists());
        assertTrue(coverage.getIpsSrcFile().exists());

        IProductCmptGeneration[] sourceRefs = ipsProject.findReferencingProductCmptGenerations(coverage
                .getQualifiedNameType());

        MoveOperation move = new MoveOperation(new IIpsElement[] { sourcePackage }, target);
        move.run(null);

        assertTrue(target.exists());
        // TODO AW: Out commented temporarily
        // assertFalse(sourcePackage.exists());

        target = target.getRoot().getIpsPackageFragment("moved.coverages");

        IIpsSrcFile coverageFile = target.getIpsSrcFile(coverage.getName(), IpsObjectType.PRODUCT_CMPT);

        assertTrue(coverageFile.exists());

        IProductCmpt targetObject = (IProductCmpt)coverageFile.getIpsObject();
        IProductCmptGeneration[] targetRefs = targetObject.getIpsProject().findReferencingProductCmptGenerations(
                targetObject.getQualifiedNameType());

        assertEquals(sourceRefs.length, targetRefs.length);
    }

    public void testMoveTableContent() throws CoreException, InvocationTargetException, InterruptedException {
        IIpsSrcFile file = coverage.getIpsPackageFragment().createIpsFile(IpsObjectType.TABLE_CONTENTS, "table", true,
                null);

        assertTrue(file.exists());

        MoveOperation move = new MoveOperation(new IIpsElement[] { file.getIpsObject() }, new String[] { "table" });
        move.run(null);

        IIpsSrcFile target = coverage.getIpsPackageFragment().getRoot().getDefaultIpsPackageFragment().getIpsSrcFile(
                IpsObjectType.TABLE_CONTENTS.getFileName("table"));
        assertTrue(target.exists());
        assertFalse(file.exists());
    }

    public void testRenameTableContent() throws Exception {
        IIpsSrcFile file = coverage.getIpsPackageFragment().createIpsFile(IpsObjectType.TABLE_CONTENTS, "Table", true,
                null);
        assertTrue(file.exists());

        MoveOperation move = new MoveOperation(new IIpsElement[] { file.getIpsObject() },
                new String[] { "products.newTable" });
        move.run(null);

        IIpsSrcFile target = ipsRoot.getIpsPackageFragment("products").getIpsSrcFile("newTable",
                IpsObjectType.TABLE_CONTENTS);
        assertTrue(target.exists());
        assertFalse(file.exists());
    }

    public void testMoveTestCase() throws CoreException, InvocationTargetException, InterruptedException {
        IIpsSrcFile file = coverage.getIpsPackageFragment().createIpsFile(IpsObjectType.TEST_CASE, "testCase", true,
                null);

        assertTrue(file.exists());

        MoveOperation move = new MoveOperation(new IIpsElement[] { file.getIpsObject() }, new String[] { "testCase" });
        move.run(null);

        IIpsSrcFile target = ipsRoot.getDefaultIpsPackageFragment().getIpsSrcFile(
                IpsObjectType.TEST_CASE.getFileName("testCase"));
        assertTrue(target.exists());
        assertFalse(file.exists());
    }

    public void testRenameTestCase() throws Exception {
        IIpsSrcFile file = coverage.getIpsPackageFragment().createIpsFile(IpsObjectType.TEST_CASE, "testcase", true,
                null);

        assertTrue(file.exists());

        MoveOperation move = new MoveOperation(new IIpsElement[] { file.getIpsObject() },
                new String[] { "products.newTestCase" });
        move.run(null);

        IIpsSrcFile target = ipsRoot.getIpsPackageFragment("products").getIpsSrcFile("newTestCase",
                IpsObjectType.TEST_CASE);
        assertTrue(target.exists());
        assertFalse(file.exists());
    }

    /**
     * Test to rename a package framgent which contains at least one file that is NOT a product
     * component or a table content
     */
    public void testRenamePackageWithFiles() throws Exception {
        IIpsPackageFragmentRoot root = coverage.getIpsPackageFragment().getRoot();
        IIpsPackageFragment pack = root.createPackageFragment("test.subpackage", true, null);
        IFile file = ((IFolder)pack.getCorrespondingResource()).getFile("test.unknown");
        file.create(StringUtil.getInputStreamForString("Test content for file.", "UTF-8"), true, null);
        assertTrue(pack.exists());
        assertTrue(file.exists());

        int count = pack.getParentIpsPackageFragment().getChildIpsPackageFragments().length;

        MoveOperation move = new MoveOperation(new IIpsElement[] { pack }, new String[] { "test.renamedPackage" });
        move.run(null);

        assertFalse(pack.exists());
        assertFalse(file.exists());

        pack = root.createPackageFragment("test.renamedPackage", true, null);
        file = ((IFolder)pack.getCorrespondingResource()).getFile("test.unknown");
        assertTrue(pack.exists());
        assertTrue(file.exists());
        assertEquals(count, pack.getParentIpsPackageFragment().getChildIpsPackageFragments().length);

    }

    public void testRenameEmptyFolder() throws Exception {
        IIpsPackageFragment pack = ipsRoot.createPackageFragment("empty", true, null);

        assertTrue(pack.exists());

        MoveOperation move = new MoveOperation(new IIpsElement[] { pack }, new String[] { "empty2" });
        move.run(null);

        assertFalse(pack.exists());

        pack = ipsRoot.getIpsPackageFragment("empty2");

        assertTrue(pack.exists());
    }

    public void testRenamePackageContainingOnlyPackages() throws Exception {
        IIpsPackageFragment level1 = ipsRoot.createPackageFragment("level1", true, null);

        ipsRoot.createPackageFragment("level1.level2_1", true, null);
        ipsRoot.createPackageFragment("level1.level2_2", true, null);
        ipsRoot.createPackageFragment("level1.level2_3", true, null);
        ipsRoot.createPackageFragment("level1.level2_4", true, null);

        super.newIpsObject(ipsRoot, IpsObjectType.TABLE_CONTENTS, "level1.level2_1.TableContent");

        MoveOperation move = new MoveOperation(new IIpsElement[] { level1 }, new String[] { "levela" });
        move.run(null);

        assertFalse(level1.exists());
        assertTrue(ipsRoot.getIpsPackageFragment("levela").exists());

        super.newIpsObject(ipsRoot, IpsObjectType.TABLE_STRUCTURE, "levela.level2_2.TableStructure");

        try {
            move = new MoveOperation(new IIpsElement[] { ipsRoot.getIpsPackageFragment("levela") },
                    new String[] { "levelb" });
            move.run(null);
            fail();
        } catch (CoreException ce) {
            // success
        }
    }

    public void testMovePackageInRootContainingEmptyPackage() throws Exception {
        IIpsPackageFragment target = ipsRoot.createPackageFragment("target", true, null);
        IIpsPackageFragment source = ipsRoot.createPackageFragment("source", true, null);
        newProductCmpt(ipsRoot, "source.TestProduct");
        ipsRoot.createPackageFragment("source.empty", true, null);

        MoveOperation move = new MoveOperation(new IIpsElement[] { source }, target);
        move.run(null);

        assertFalse(source.exists());
        assertTrue(ipsRoot.getIpsPackageFragment("target.source").exists());
        assertTrue(ipsRoot.getIpsPackageFragment("target.source.empty").exists());
        assertTrue(ipsProject.findIpsObject(IpsObjectType.PRODUCT_CMPT, "target.source.TestProduct").exists());

    }

    public void testMovePackageContainingEmptyPackage() throws Exception {
        IIpsPackageFragment target = ipsRoot.createPackageFragment("target", true, null);
        IIpsPackageFragment source = ipsRoot.createPackageFragment("parent.source", true, null);
        newProductCmpt(ipsRoot, "parent.source.TestProduct");
        ipsRoot.createPackageFragment("parent.source.empty", true, null);

        MoveOperation move = new MoveOperation(new IIpsElement[] { source }, target);
        move.run(null);

        assertFalse(source.exists());
        assertTrue(ipsRoot.getIpsPackageFragment("parent").exists());
        assertTrue(ipsRoot.getIpsPackageFragment("target.source").exists());
        assertTrue(ipsRoot.getIpsPackageFragment("target.source.empty").exists());
        assertTrue(ipsProject.findIpsObject(IpsObjectType.PRODUCT_CMPT, "target.source.TestProduct").exists());

    }

    public void testMoveInDifferentRoot() throws Exception {
        IIpsPackageFragmentRoot targetRoot = createIpsPackageFrgmtRoot();

        IIpsPackageFragment sourcePackageFrgmt = coverage.getIpsPackageFragment();
        String sourcePackageFrgmtName = sourcePackageFrgmt.getName();

        // prepare the source object
        IIpsSrcFile file = sourcePackageFrgmt.createIpsFile(IpsObjectType.TEST_CASE, "testCase", true, null);
        IIpsSrcFile target = sourcePackageFrgmt.getRoot().getIpsPackageFragment(sourcePackageFrgmtName).getIpsSrcFile(
                IpsObjectType.TEST_CASE.getFileName("testCase"));
        assertTrue(file.exists());

        // now move the source object into the target root default frgmt
        MoveOperation move = new MoveOperation(new IIpsElement[] { file.getIpsObject() }, targetRoot
                .getDefaultIpsPackageFragment());
        move.run(null);

        target = sourcePackageFrgmt.getRoot().getIpsPackageFragment(sourcePackageFrgmtName).getIpsSrcFile(
                IpsObjectType.TEST_CASE.getFileName("testCase"));
        assertFalse(target.exists());
        target = sourcePackageFrgmt.getRoot().getDefaultIpsPackageFragment().getIpsSrcFile(
                IpsObjectType.TEST_CASE.getFileName("testCase"));
        assertFalse(target.exists());

        target = targetRoot.getDefaultIpsPackageFragment().getIpsSrcFile(
                IpsObjectType.TEST_CASE.getFileName("testCase"));
        assertTrue(target.exists());
    }

    public void testMoveFiles() throws Exception {
        // set up source and target
        IProject project = ipsProject.getProject();
        IFolder folderSource = project.getFolder("source");
        IFolder folderTarget = project.getFolder("target");
        folderSource.create(true, true, null);
        folderTarget.create(true, true, null);
        assertTrue(folderSource.exists());
        assertTrue(folderTarget.exists());

        IFile file1 = folderSource.getFile("file1");
        IFile file2 = folderSource.getFile("file2");
        file1.create(new ByteArrayInputStream("File1".getBytes()), true, null);
        file2.create(new ByteArrayInputStream("File1".getBytes()), true, null);
        assertTrue(folderSource.exists());
        assertTrue(folderTarget.exists());

        // test move to folder
        MoveOperation operation = new MoveOperation(folderTarget.getProject(), new Object[] { file1, file2 },
                folderTarget.getLocation().toOSString());
        operation.run(null);

        assertTrue(folderTarget.getFile("file1").exists());
        assertTrue(folderTarget.getFile("file2").exists());

        // test move to ips package fragment
        newPolicyCmptType(ipsProject, "target.dummy");
        IIpsPackageFragment targetIpsPackageFragment = ipsProject.getIpsPackageFragmentRoots()[0]
                .getIpsPackageFragment("source");
        IResource source01 = ((IContainer)targetIpsPackageFragment.getEnclosingResource()).findMember("file1");
        assertTrue(source01 == null);
        IFile source1 = folderTarget.getFile("file1");
        operation = new MoveOperation(new Object[] { source1 }, targetIpsPackageFragment);
        operation.run(null);
        source01 = ((IContainer)targetIpsPackageFragment.getEnclosingResource()).findMember("file1");
        assertTrue(source01.exists());

        // test move to project
        operation = new MoveOperation(ipsProject.getProject(), new Object[] { source01 }, ipsProject.getProject()
                .getLocation().toOSString());
        operation.run(null);
        assertTrue(((IContainer)targetIpsPackageFragment.getEnclosingResource()).findMember("file1") == null);
        assertTrue(ipsProject.getProject().findMember("file1").exists());
    }

    public void testMoveLinks() throws Exception {
        // set up source and target
        IProject project = ipsProject.getProject();
        IFolder folderSource = project.getFolder("source");
        IFolder folderTarget = project.getFolder("target");
        folderSource.create(true, true, null);
        folderTarget.create(true, true, null);
        assertTrue(folderSource.exists());
        assertTrue(folderTarget.exists());

        IFile file1 = folderSource.getFile("file1");
        IFile file2 = folderSource.getFile("file2");
        file1.create(new ByteArrayInputStream("File1".getBytes()), true, null);
        file2.create(new ByteArrayInputStream("File1".getBytes()), true, null);
        assertTrue(folderSource.exists());
        assertTrue(folderTarget.exists());

        MoveOperation operation = new MoveOperation(folderTarget.getProject(), new Object[] {
                file1.getLocation().toOSString(), file2.getLocation().toOSString() }, folderTarget.getLocation()
                .toOSString());
        operation.run(null);

        assertTrue(folderTarget.getFile("file1").exists());
        assertTrue(folderTarget.getFile("file2").exists());
    }

    private IIpsPackageFragmentRoot createIpsPackageFrgmtRoot() throws CoreException {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IFolder rootFolder = ipsProject.getProject().getFolder("targetRoot");
        rootFolder.create(true, true, null);
        path.newSourceFolderEntry(rootFolder);
        ipsProject.setIpsObjectPath(path);
        IIpsPackageFragmentRoot targetRoot = ipsProject.getIpsPackageFragmentRoot("targetRoot");
        assertNotNull(targetRoot);
        assertTrue(targetRoot.exists());
        return targetRoot;
    }

}
