/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import static org.faktorips.abstracttest.matcher.ExistsFileMatcher.exists;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.TestContentsChangeListener;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsPackageFragment.DefinedOrderComparator;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.util.StringUtil;
import org.junit.Before;
import org.junit.Test;

public class IpsPackageFragmentTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot rootPackage;
    private IpsPackageFragment pack;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        rootPackage = ipsProject.getIpsPackageFragmentRoots()[0];
        pack = (IpsPackageFragment)rootPackage.createPackageFragment("products.folder", true, null);
    }

    @Test
    public void testGetRelativePath() {
        String[] expectedSegments = pack.getName().split("\\.");
        String[] segments = pack.getRelativePath().segments();
        for (int i = 0; i < segments.length; i++) {
            assertEquals(expectedSegments[i], segments[i]);
        }
    }

    @Test
    public void testGetElementName() {
        assertEquals("products.folder", pack.getName());
    }

    @Test
    public void testGetIpsRootFolder() {
        assertEquals(rootPackage, pack.getRoot());
    }

    @Test
    public void testGetCorrespondingResource() {
        IResource resource = pack.getCorrespondingResource();
        assertTrue(resource instanceof IFolder);
        assertEquals("folder", resource.getName());
        assertTrue(resource.exists());
        IResource parent = resource.getParent();
        assertTrue(parent.exists());
        assertEquals("products", parent.getName());

        // default folder
        IIpsPackageFragment defaultFolder = rootPackage.getIpsPackageFragment("");
        assertEquals(rootPackage.getCorrespondingResource(), defaultFolder.getCorrespondingResource());
    }

    @Test
    public void testExists() throws CoreException {
        assertTrue(pack.exists());
        // parent exists, but not the corresponding folder
        IIpsPackageFragment folder = rootPackage.getIpsPackageFragment("unkownFolder");
        assertFalse(folder.exists());
        // corresponding folder exists but not the parent (because root2
        // is not on the classpath
        IIpsPackageFragmentRoot root2 = ipsProject.getIpsPackageFragmentRoot("notonpath");
        ((IFolder)root2.getCorrespondingResource()).create(true, true, null);
        IIpsPackageFragment pck2 = root2.getIpsPackageFragment("pck2");
        ((IFolder)pck2.getCorrespondingResource()).create(true, true, null);
        assertFalse(pck2.exists());
    }

    @Test
    public void testGetChildren() throws CoreException {
        assertEquals(0, pack.getChildren().length);

        pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "MotorProduct", true, null);
        IIpsElement[] children = pack.getChildren();
        assertEquals(1, children.length);
        String filename = IpsObjectType.POLICY_CMPT_TYPE.getFileName("MotorProduct");
        assertEquals(pack.getIpsSrcFile(filename), children[0]);

        // folders should be ignored
        IFolder folder = (IFolder)pack.getCorrespondingResource();
        IFolder subfolder = folder.getFolder("subfolder");
        subfolder.create(true, true, null);
        assertEquals(1, pack.getChildren().length);

        // files with unkown file extentions should be ignored
        IFile newFile = folder.getFile("Blabla.unkownExtension");
        ByteArrayInputStream is = new ByteArrayInputStream("Contents".getBytes());
        newFile.create(is, true, null);
        assertEquals(1, pack.getChildren().length);
    }

    @Test
    public void testGetIpsSrcFiles() throws CoreException {
        assertEquals(0, pack.getIpsSrcFiles().length);

        pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "MotorProduct", true, null);
        IIpsSrcFile[] children = pack.getIpsSrcFiles();
        assertEquals(1, children.length);
        String filename = IpsObjectType.POLICY_CMPT_TYPE.getFileName("MotorProduct");
        assertEquals(pack.getIpsSrcFile(filename), children[0]);

        // folders should be ignored
        IFolder folder = (IFolder)pack.getCorrespondingResource();
        IFolder subfolder = folder.getFolder("subfolder");
        subfolder.create(true, true, null);
        assertEquals(1, pack.getIpsSrcFiles().length);

        // files with unkown file extentions should be ignored
        IFile newFile = folder.getFile("Blabla.unkownExtension");
        ByteArrayInputStream is = new ByteArrayInputStream("Contents".getBytes());
        newFile.create(is, true, null);
        assertEquals(1, pack.getIpsSrcFiles().length);
    }

    @Test
    public void testGetIpsSrcFile() {
        String fileName = "file." + IpsObjectType.POLICY_CMPT_TYPE.getFileExtension();
        IIpsSrcFile file = pack.getIpsSrcFile(fileName);
        assertEquals(fileName, file.getName());
        assertEquals(pack, file.getParent());
    }

    @Test
    public void testCreateIpsFile() throws CoreException, IOException {
        // need to supress the file's contents is not valid!
        suppressLoggingDuringExecutionOfThisTestCase();
        String filename = IpsObjectType.POLICY_CMPT_TYPE.getFileName("TestType");
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><PolicyCmptType/>";
        IIpsSrcFile file = pack.createIpsFile(filename, xml, true, null);
        assertTrue(file.exists());
        InputStream is = file.getCorrespondingFile().getContents();
        String contents = StringUtil.readFromInputStream(is, ipsProject.getXmlFileCharset());
        assertEquals(xml, contents);
    }

    @Test
    public void testCreateProductComponent() throws Exception {
        IIpsSrcFile file = pack.createIpsFile(IpsObjectType.PRODUCT_CMPT, "Test", true, null);
        IProductCmpt product = (IProductCmpt)file.getIpsObject();
        assertFalse(StringUtils.isEmpty(product.getRuntimeId()));
    }

    @Test
    public void testFindIpsObjects_PackContainsInvalidFile() throws CoreException {
        IIpsSrcFile srcFile = pack.createIpsFile(IpsObjectType.PRODUCT_CMPT, "Test", true, null);
        IFile file = srcFile.getCorrespondingFile();
        file.setContents(new ByteArrayInputStream("<ProductCmpt/>".getBytes()), true, false, null);

        ArrayList<IIpsObject> result = new ArrayList<IIpsObject>();
        pack.findIpsObjects(IpsObjectType.PRODUCT_CMPT, result);
        assertEquals(1, result.size());
        assertEquals(srcFile.getIpsObject(), result.get(0));
    }

    @Test
    public void testFindIpsObjects() throws Exception {
        IIpsObject obj1 = newIpsObject(rootPackage, IpsObjectType.PRODUCT_CMPT_TYPE, "a.b.A");
        IIpsObject obj2 = newIpsObject(rootPackage, IpsObjectType.PRODUCT_CMPT_TYPE, "a.b.B");
        IIpsObject obj3 = newIpsObject(rootPackage, IpsObjectType.PRODUCT_CMPT_TYPE, "a.b.C");

        IIpsObject obj4 = newIpsObject(rootPackage, IpsObjectType.PRODUCT_CMPT_TYPE, "a.c.D");
        IIpsObject obj5 = newIpsObject(rootPackage, IpsObjectType.PRODUCT_CMPT_TYPE, "a.c.E");
        IIpsObject obj6 = newIpsObject(rootPackage, IpsObjectType.PRODUCT_CMPT_TYPE, "a.c.F");

        IpsPackageFragment fragment = (IpsPackageFragment)obj1.getIpsPackageFragment();

        ArrayList<IIpsObject> result = new ArrayList<IIpsObject>();
        fragment.findIpsObjects(result);

        assertTrue(result.contains(obj1));
        assertTrue(result.contains(obj2));
        assertTrue(result.contains(obj3));
        assertFalse(result.contains(obj4));
        assertFalse(result.contains(obj5));
        assertFalse(result.contains(obj6));

        fragment = (IpsPackageFragment)obj4.getIpsPackageFragment();

        result.clear();
        fragment.findIpsObjects(result);

        assertTrue(result.contains(obj4));
        assertTrue(result.contains(obj5));
        assertTrue(result.contains(obj6));
        assertFalse(result.contains(obj1));
        assertFalse(result.contains(obj2));
        assertFalse(result.contains(obj3));
    }

    @Test
    public void testFindIpsObjectsStartingWith() throws CoreException {
        IIpsObject obj1 = newIpsObject(pack, IpsObjectType.POLICY_CMPT_TYPE, "MotorPolicy");
        IIpsObject obj2 = newIpsObject(pack, IpsObjectType.POLICY_CMPT_TYPE, "motorCoverage");

        ArrayList<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();

        // case sensitive
        pack.findIpsSourceFilesStartingWith(IpsObjectType.POLICY_CMPT_TYPE, "Motor", false, result);
        assertEquals(1, result.size());
        assertTrue(result.contains(obj1.getIpsSrcFile()));

        // ignore case
        result.clear();
        pack.findIpsSourceFilesStartingWith(IpsObjectType.POLICY_CMPT_TYPE, "Motor", true, result);
        assertEquals(2, result.size());
        assertTrue(result.contains(obj1.getIpsSrcFile()));
        assertTrue(result.contains(obj2.getIpsSrcFile()));

        // nothing found because no policy component type exists starting with z
        result.clear();
        pack.findIpsSourceFilesStartingWith(IpsObjectType.POLICY_CMPT_TYPE, "Z", true, result);
        assertEquals(0, result.size());

        // nothing found, because no product component exists
        result.clear();
        pack.findIpsSourceFilesStartingWith(IpsObjectType.PRODUCT_CMPT, "M", true, result);
        assertEquals(0, result.size());

        // pack does not exists
        IpsPackageFragment pack2 = (IpsPackageFragment)rootPackage.getIpsPackageFragment("notExistingPack");
        pack2.findIpsSourceFilesStartingWith(IpsObjectType.POLICY_CMPT_TYPE, "Motor", true, result);
        assertEquals(0, result.size());

        // ipsobjecttype null
        try {
            pack.findIpsSourceFilesStartingWith(null, "M", true, result);
            fail();
        } catch (NullPointerException e) {
        }

        // prefix null
        try {
            pack.findIpsSourceFilesStartingWith(IpsObjectType.POLICY_CMPT_TYPE, null, true, result);
            fail();
        } catch (NullPointerException e) {
        }

        // result null
        try {
            pack.findIpsSourceFilesStartingWith(IpsObjectType.POLICY_CMPT_TYPE, "M", true, null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetParentIpsPackageFragment() throws CoreException {
        IIpsPackageFragment testPackage = rootPackage.createPackageFragment("products.test.subtest", true, null);
        assertEquals("products.test.subtest", testPackage.getName());
        testPackage = testPackage.getParentIpsPackageFragment();
        assertEquals("products.test", testPackage.getName());
        testPackage = testPackage.getParentIpsPackageFragment();
        assertEquals("products", testPackage.getName());
        testPackage = testPackage.getParentIpsPackageFragment();
        assertEquals("", testPackage.getName());
        assertNull(testPackage.getParentIpsPackageFragment());
    }

    @Test
    public void testGetIpsChildPackageFragments() throws CoreException {
        rootPackage.createPackageFragment("products.test1", true, null);
        rootPackage.createPackageFragment("products.test2", true, null);

        IIpsPackageFragment[] children = rootPackage.getDefaultIpsPackageFragment().getChildIpsPackageFragments();
        assertEquals(children.length, 1);
        children = children[0].getChildIpsPackageFragments();
        assertEquals(children.length, 3);
        assertEquals(children[0].getName(), "products.folder");
        assertEquals(children[1].getName(), "products.test1");
        assertEquals(children[2].getName(), "products.test2");

    }

    @Test
    public void testGetNonIpsResources() throws CoreException {
        IFolder packageHandle = (IFolder)pack.getCorrespondingResource();
        IFolder folder = packageHandle.getFolder("folder");
        folder.create(true, false, null);
        IFile nonIpsFile = packageHandle.getFile("nonIpsFile");
        nonIpsFile.create(null, true, null);
        IFile nonIpsFile2 = packageHandle.getFile("nonIpsFile2");
        nonIpsFile2.create(null, true, null);

        Object[] nonIpsResources = pack.getNonIpsResources();
        assertEquals(2, nonIpsResources.length);
        List<?> list = Arrays.asList(nonIpsResources);
        assertTrue(list.contains(nonIpsFile));
        assertTrue(list.contains(nonIpsFile2));
        // manually created folder is interpreted as IpsPackageFragment
        assertFalse(list.contains(folder));
    }

    @Test
    public void testCreateIpsFileFromTemplate() throws CoreException {
        GregorianCalendar date = new GregorianCalendar(2006, 0, 1);
        IProductCmpt template = (IProductCmpt)newIpsObject(rootPackage, IpsObjectType.PRODUCT_CMPT, "products.Bla");
        IProductCmptGeneration generation = (IProductCmptGeneration)template.newGeneration(date);
        generation.newLink("testRelation");
        template.getIpsSrcFile().save(true, null);
        TestContentsChangeListener listener = new TestContentsChangeListener();
        template.getIpsModel().addChangeListener(listener);

        @SuppressWarnings("deprecation")
        IIpsSrcFile file = pack.createIpsFileFromTemplate("copy", template, date, date, true, null);
        assertEquals(1, listener.getNumOfEventsReceived());
        assertEquals(file, listener.getLastEvent().getIpsSrcFile());
        assertEquals(ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED, listener.getLastEvent().getEventType());
        IProductCmpt copy = (IProductCmpt)file.getIpsObject();
        file.save(true, null);

        assertEquals("copy", copy.getName());

        IProductCmptGeneration copyGen = copy.getGenerationByEffectiveDate(date);
        assertEquals(generation.getValidFrom(), copyGen.getValidFrom());

        assertEquals(generation.getLinks().length, copyGen.getLinks().length);

        assertFalse(template.getRuntimeId().equals(copy.getRuntimeId()));
        assertFalse(StringUtils.isEmpty(copy.getRuntimeId()));
        assertEquals("copy", copy.getRuntimeId());
    }

    @Test
    public void testSetChildOrderComparator_createsSortOrderFile() throws CoreException {
        IIpsPackageFragment hausrat = rootPackage.createPackageFragment("products.hausrat", true, null);
        IIpsPackageFragment kranken = rootPackage.createPackageFragment("products.kranken", true, null);
        IIpsPackageFragment unfall = rootPackage.createPackageFragment("products.unfall", true, null);
        IIpsPackageFragment haftpflicht = rootPackage.createPackageFragment("products.haftpflicht", true, null);
        IpsPackageFragment products = (IpsPackageFragment)rootPackage.getIpsPackageFragment("products");

        Comparator<IIpsElement> childOrderComparator = new DefinedOrderComparator(haftpflicht, kranken, unfall,
                hausrat);

        products.setChildOrderComparator(childOrderComparator);

        assertThat(products.getChildOrderComparator(), is(sameInstance(childOrderComparator)));
        IFolder folder = (IFolder)products.getCorrespondingResource();
        IFile sortOrderFile = folder.getFile(new Path(IIpsPackageFragment.SORT_ORDER_FILE_NAME));

        assertThat(sortOrderFile, is(not(nullValue())));
        assertThat(sortOrderFile, exists());
    }

    @Test
    public void testSetChildOrderComparator_deletedSortOrderFile() throws CoreException {
        IIpsPackageFragment hausrat = rootPackage.createPackageFragment("products.hausrat", true, null);
        IIpsPackageFragment kranken = rootPackage.createPackageFragment("products.kranken", true, null);
        IIpsPackageFragment unfall = rootPackage.createPackageFragment("products.unfall", true, null);
        IIpsPackageFragment haftpflicht = rootPackage.createPackageFragment("products.haftpflicht", true, null);
        IpsPackageFragment products = (IpsPackageFragment)rootPackage.getIpsPackageFragment("products");
        products.setChildOrderComparator(new DefinedOrderComparator(haftpflicht, kranken, unfall, hausrat));
        IFolder folder = (IFolder)products.getCorrespondingResource();
        IFile sortOrderFile = folder.getFile(new Path(IIpsPackageFragment.SORT_ORDER_FILE_NAME));
        assertThat(sortOrderFile, exists());

        products.setChildOrderComparator(AbstractIpsPackageFragment.DEFAULT_CHILD_ORDER_COMPARATOR);

        assertThat(sortOrderFile, not(exists()));
    }

    @Test
    public void testGetChildOrderComparator_default() throws CoreException {
        rootPackage.createPackageFragment("products.hausrat", true, null);
        rootPackage.createPackageFragment("products.kranken", true, null);
        rootPackage.createPackageFragment("products.unfall", true, null);
        rootPackage.createPackageFragment("products.haftpflicht", true, null);
        IIpsPackageFragment products = rootPackage.getIpsPackageFragment("products");
        Comparator<IIpsElement> defaultChildOrderComparator = AbstractIpsPackageFragment.DEFAULT_CHILD_ORDER_COMPARATOR;

        Comparator<IIpsElement> childOrderComparator = products.getChildOrderComparator();

        assertThat(childOrderComparator, is(sameInstance(defaultChildOrderComparator)));
    }

    @Test
    public void testGetChildOrderComparator_withSortorder() throws CoreException, IOException {
        rootPackage.createPackageFragment("products.hausrat", true, null);
        rootPackage.createPackageFragment("products.kranken", true, null);
        rootPackage.createPackageFragment("products.unfall", true, null);
        rootPackage.createPackageFragment("products.haftpflicht", true, null);
        IIpsPackageFragment products = rootPackage.getIpsPackageFragment("products");
        createSortOrderFile((IFolder)products.getCorrespondingResource(), "unfall", "kranken", "haftpflicht",
                "hausrat");

        Comparator<IIpsElement> childOrderComparator = products.getChildOrderComparator();

        assertThat(childOrderComparator, is(instanceOf(DefinedOrderComparator.class)));
    }

    @Test
    public void testGetChildOrderComparator_withUpdatedSortorder() throws CoreException, IOException {
        rootPackage.createPackageFragment("products.hausrat", true, null);
        rootPackage.createPackageFragment("products.kranken", true, null);
        rootPackage.createPackageFragment("products.unfall", true, null);
        rootPackage.createPackageFragment("products.haftpflicht", true, null);
        IIpsPackageFragment products = rootPackage.getIpsPackageFragment("products");
        createSortOrderFile((IFolder)products.getCorrespondingResource(), "unfall", "kranken", "haftpflicht",
                "hausrat");

        Comparator<IIpsElement> oldChildOrderComparator = products.getChildOrderComparator();

        assertThat(oldChildOrderComparator, is(instanceOf(DefinedOrderComparator.class)));

        createSortOrderFile((IFolder)products.getCorrespondingResource(), "unfall", "haftpflicht", "hausrat",
                "kranken");

        Comparator<IIpsElement> newChildOrderComparator = products.getChildOrderComparator();

        assertThat(newChildOrderComparator, is(not(oldChildOrderComparator)));
    }

    @Test
    public void testHasChildPackageFragments() throws CoreException {
        assertFalse(pack.hasChildIpsPackageFragments());

        IIpsPackageFragment products = rootPackage.getIpsPackageFragment("products");
        assertTrue(products.hasChildIpsPackageFragments());

        assertTrue(rootPackage.getDefaultIpsPackageFragment().hasChildIpsPackageFragments());
    }

    @Test
    public void testDelete() throws CoreException {
        IIpsPackageFragment childPackage = pack.createSubPackage("foo", true, null);
        IIpsSrcFile childSrcFile = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "FooBar", true, null);

        pack.delete();

        assertFalse(pack.exists());
        assertFalse(childPackage.exists());
        assertFalse(childSrcFile.exists());
    }

}
