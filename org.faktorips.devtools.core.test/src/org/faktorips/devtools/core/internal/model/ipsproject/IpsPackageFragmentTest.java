/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.TestContentsChangeListener;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentSortDefinition;
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
        IpsPlugin.getDefault().getPreferenceStore().setValue(IpsPreferences.WORKING_DATE, "2006-01-01");
        GregorianCalendar date = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
        IProductCmpt template = (IProductCmpt)newIpsObject(rootPackage, IpsObjectType.PRODUCT_CMPT, "products.Bla");
        IProductCmptGeneration generation = (IProductCmptGeneration)template.newGeneration(date);
        generation.newLink("testRelation");
        template.getIpsSrcFile().save(true, null);
        TestContentsChangeListener listener = new TestContentsChangeListener();
        template.getIpsModel().addChangeListener(listener);

        IIpsSrcFile file = pack.createIpsFileFromTemplate("copy", template, date, date, true, null);
        assertEquals(1, listener.getNumOfEventsReceived());
        assertEquals(file, listener.getLastEvent().getIpsSrcFile());
        assertEquals(ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED, listener.getLastEvent().getEventType());
        IProductCmpt copy = (IProductCmpt)file.getIpsObject();
        file.save(true, null);

        assertEquals("copy", copy.getName());

        IProductCmptGeneration copyGen = (IProductCmptGeneration)copy.getGenerationByEffectiveDate(date);
        assertEquals(generation.getValidFrom(), copyGen.getValidFrom());

        assertEquals(generation.getLinks().length, copyGen.getLinks().length);

        assertFalse(template.getRuntimeId().equals(copy.getRuntimeId()));
        assertFalse(StringUtils.isEmpty(copy.getRuntimeId()));
        assertEquals("copy", copy.getRuntimeId());
    }

    @Test
    public void testGetSortedChildIpsPackageFragmentsBasics() throws CoreException, IOException {

        IIpsPackageFragment[] children = rootPackage.getDefaultIpsPackageFragment().getSortedChildIpsPackageFragments();
        assertEquals(children.length, 1);

        // sorted
        rootPackage.createPackageFragment("products2.hausrat", true, null);
        rootPackage.createPackageFragment("products2.kranken", true, null);
        IIpsPackageFragment service2 = rootPackage
                .createPackageFragment("products2.kranken.leistungsarten", true, null);
        rootPackage.createPackageFragment("products2.kranken.leistungsarten.fix", true, null);
        rootPackage.createPackageFragment("products2.kranken.leistungsarten.optional", true, null);
        rootPackage.createPackageFragment("products2.kranken.vertragsarten", true, null);
        rootPackage.createPackageFragment("products2.kranken.gruppenarten", true, null);
        rootPackage.createPackageFragment("products2.unfall", true, null);
        rootPackage.createPackageFragment("products2.haftpflicht", true, null);

        IIpsPackageFragment products2 = rootPackage.getIpsPackageFragment("products2");

        List<String> list = new ArrayList<String>();
        list.add("products2");

        createPackageOrderFile((IFolder)rootPackage.getCorrespondingResource(), list);
        list.clear();

        list.add("unfall");
        list.add("kranken");
        list.add("haftpflicht");
        list.add("hausrat");

        createPackageOrderFile((IFolder)products2.getCorrespondingResource(), list);
        list.clear();

        list.add("optional");
        list.add("fix");

        createPackageOrderFile((IFolder)service2.getCorrespondingResource(), list);
        list.clear();

        IIpsPackageFragment products = rootPackage.getIpsPackageFragment("products");

        rootPackage.createPackageFragment("products.hausrat", true, null);
        rootPackage.createPackageFragment("products.kranken", true, null);
        rootPackage.createPackageFragment("products.kranken.leistungsarten", true, null);
        rootPackage.createPackageFragment("products.kranken.leistungsarten.fix", true, null);
        rootPackage.createPackageFragment("products.kranken.leistungsarten.optional", true, null);
        rootPackage.createPackageFragment("products.kranken.vertragsarten", true, null);
        rootPackage.createPackageFragment("products.kranken.gruppenarten", true, null);
        rootPackage.createPackageFragment("products.unfall", true, null);
        rootPackage.createPackageFragment("products.haftpflicht", true, null);

        // unsorted
        children = products.getSortedChildIpsPackageFragments();
        assertEquals(5, children.length);
        assertEquals("products.folder", children[0].getName());
        assertEquals("products.haftpflicht", children[1].getName());
        assertEquals("products.hausrat", children[2].getName());
        assertEquals("products.kranken", children[3].getName());
        assertEquals("products.unfall", children[4].getName());

        // sorted: valid files and entries
        children = products2.getSortedChildIpsPackageFragments();
        assertEquals(4, children.length);
        assertEquals("products2.unfall", children[0].getName());
        assertEquals("products2.kranken", children[1].getName());
        assertEquals("products2.haftpflicht", children[2].getName());
        assertEquals("products2.hausrat", children[3].getName());

        children = service2.getSortedChildIpsPackageFragments();
        assertEquals(2, children.length);
        assertEquals("products2.kranken.leistungsarten.optional", children[0].getName());
        assertEquals("products2.kranken.leistungsarten.fix", children[1].getName());
    }

    @Test
    public void testGetSortedChildIpsPackageFragmentsExtend() throws CoreException, IOException {
        rootPackage.createPackageFragment("products.hausrat", true, null);
        rootPackage.createPackageFragment("products.kranken", true, null);
        IIpsPackageFragment service = rootPackage.createPackageFragment("products.kranken.leistungsarten", true, null);
        rootPackage.createPackageFragment("products.kranken.leistungsarten.fix", true, null);
        rootPackage.createPackageFragment("products.kranken.leistungsarten.optional", true, null);
        rootPackage.createPackageFragment("products.kranken.vertragsarten", true, null);
        rootPackage.createPackageFragment("products.kranken.gruppenarten", true, null);
        rootPackage.createPackageFragment("products.unfall", true, null);
        rootPackage.createPackageFragment("products.haftpflicht", true, null);

        IIpsPackageFragment products = rootPackage.getIpsPackageFragment("products");

        // sorted: valid files and entries
        List<String> list = new ArrayList<String>();
        list.add("products");

        createPackageOrderFile((IFolder)rootPackage.getCorrespondingResource(), list);
        list.clear();

        list.add("unfall");
        list.add("kranken");
        list.add("folder");
        list.add("haftpflicht");
        list.add("hausrat");

        createPackageOrderFile((IFolder)products.getCorrespondingResource(), list);
        list.clear();

        list.add("optional");
        list.add("fix");

        createPackageOrderFile((IFolder)service.getCorrespondingResource(), list);
        list.clear();

        IIpsPackageFragment[] children = rootPackage.getDefaultIpsPackageFragment().getSortedChildIpsPackageFragments();
        assertEquals(children.length, 1);

        children = service.getSortedChildIpsPackageFragments();
        assertEquals(2, children.length);
        assertEquals("products.kranken.leistungsarten.optional", children[0].getName());
        assertEquals("products.kranken.leistungsarten.fix", children[1].getName());

        children = products.getSortedChildIpsPackageFragments();
        assertEquals(5, children.length);
        assertEquals("products.unfall", children[0].getName());
        assertEquals("products.kranken", children[1].getName());
        assertEquals("products.folder", children[2].getName());
        assertEquals("products.haftpflicht", children[3].getName());
        assertEquals("products.hausrat", children[4].getName());

        // test caching
        IIpsPackageFragment[] children2 = products.getSortedChildIpsPackageFragments();
        assertEquals(5, children2.length);
        assertEquals(children2[0], children[0]);
        assertEquals(children2[1], children[1]);
        assertEquals(children2[2], children[2]);
        assertEquals(children2[3], children[3]);
        assertEquals(children2[4], children[4]);
    }

    @Test
    public void testSetSortDefinition() throws CoreException, IOException {
        IIpsPackageFragment hausrat = rootPackage.createPackageFragment("products.hausrat", true, null);
        rootPackage.createPackageFragment("products.kranken", true, null);
        rootPackage.createPackageFragment("products.unfall", true, null);
        rootPackage.createPackageFragment("products.haftpflicht", true, null);
        IIpsPackageFragment fix = rootPackage.createPackageFragment("products.kranken.leistungsarten.fix", true, null);
        rootPackage.createPackageFragment("products.kranken.leistungsarten.optional", true, null);

        IIpsPackageFragment products = rootPackage.getIpsPackageFragment("products");

        // sorted: valid files and entries
        List<String> list = new ArrayList<String>();
        list.add("products");

        createPackageOrderFile((IFolder)rootPackage.getCorrespondingResource(), list);
        list.clear();

        list.add("unfall");
        list.add("kranken");
        list.add("folder");
        list.add("haftpflicht");
        list.add("hausrat");

        createPackageOrderFile((IFolder)products.getCorrespondingResource(), list);
        list.clear();

        IpsPackageFragmentDefaultSortDefinition defaultSortDef = new IpsPackageFragmentDefaultSortDefinition();

        // test deletion default sort order
        fix.setSortDefinition(null);

        // test default sort order
        fix.setSortDefinition(defaultSortDef);
        assertTrue(fix.getSortDefinition() instanceof IpsPackageFragmentDefaultSortDefinition);

        // test set sort order for file
        IpsPackageFragmentArbitrarySortDefinition sortDef = new IpsPackageFragmentArbitrarySortDefinition();
        list.add("unfall");
        list.add("kranken");
        list.add("haftpflicht");
        list.add("hausrat");
        list.add("folder");
        sortDef.setSegmentNames(list.toArray(new String[list.size()]));
        list.clear();

        hausrat.setSortDefinition(sortDef);

        IpsPackageFragmentArbitrarySortDefinition sortDefTest = (IpsPackageFragmentArbitrarySortDefinition)hausrat
                .getSortDefinition();
        String[] fragments = sortDefTest.getSegmentNames();

        assertEquals(5, fragments.length);
        assertEquals(fragments[2], "haftpflicht");

        // test delete sort order
        hausrat.setSortDefinition(null);
        assertTrue(hausrat.getSortDefinition() instanceof IpsPackageFragmentDefaultSortDefinition);
    }

    @Test
    public void testGetSortDefinition() throws CoreException, IOException {
        rootPackage.createPackageFragment("products.hausrat", true, null);
        rootPackage.createPackageFragment("products.kranken", true, null);
        rootPackage.createPackageFragment("products.unfall", true, null);
        rootPackage.createPackageFragment("products.haftpflicht", true, null);
        IIpsPackageFragment fix = rootPackage.createPackageFragment("products.kranken.leistungsarten.fix", true, null);
        rootPackage.createPackageFragment("products.kranken.leistungsarten.optional", true, null);

        IIpsPackageFragment products = rootPackage.getIpsPackageFragment("products");

        // sorted: valid files and entries
        List<String> list = new ArrayList<String>();
        list.add("products");

        createPackageOrderFile((IFolder)rootPackage.getCorrespondingResource(), list);
        list.clear();

        list.add("unfall");
        list.add("kranken");
        list.add("folder");
        list.add("haftpflicht");
        list.add("hausrat");

        createPackageOrderFile((IFolder)products.getCorrespondingResource(), list);
        list.clear();

        // test default sort order
        IIpsPackageFragmentSortDefinition sortDef = fix.getSortDefinition();

        assertNotNull(sortDef);
        assertTrue(sortDef instanceof IpsPackageFragmentDefaultSortDefinition);

        // test sort order
        sortDef = products.getSortDefinition();

        assertNotNull(sortDef);
        assertTrue(sortDef instanceof IpsPackageFragmentArbitrarySortDefinition);

        String[] fragments = ((IpsPackageFragmentArbitrarySortDefinition)sortDef).getSegmentNames();

        assertEquals(1, fragments.length);
        assertEquals(fragments[0], "products");
        IpsModel ipsModel = (IpsModel)IpsPlugin.getDefault().getIpsModel();
        assertFalse(ipsModel.getSortDefinition(products).equals(sortDef));
    }

    @Test
    public void testHasChildPackageFragments() throws CoreException {
        assertFalse(pack.hasChildIpsPackageFragments());

        IIpsPackageFragment products = rootPackage.getIpsPackageFragment("products");
        assertTrue(products.hasChildIpsPackageFragments());

        assertTrue(rootPackage.getDefaultIpsPackageFragment().hasChildIpsPackageFragments());
    }
}
