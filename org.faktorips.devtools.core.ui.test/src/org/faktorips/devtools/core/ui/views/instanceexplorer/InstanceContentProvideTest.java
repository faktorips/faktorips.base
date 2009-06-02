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
package org.faktorips.devtools.core.ui.views.instanceexplorer;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.enums.EnumContent;
import org.faktorips.devtools.core.internal.model.enums.EnumType;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsArchive;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.devtools.core.internal.model.testcase.TestCase;
import org.faktorips.devtools.core.internal.model.testcasetype.TestCaseType;
import org.faktorips.devtools.core.model.CreateIpsArchiveOperation;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;


/**
 * @author dirmeier
 *
 */
public class InstanceContentProvideTest extends AbstractIpsPluginTest {

	private InstanceContentProvider contentProvider = new InstanceContentProvider();
	
	private IIpsProject ipsProject;
	
	private IIpsProject referencingProject;
	
	private IIpsProject independentProject;

	private IIpsProject leaveProject1;

	private IIpsProject leaveProject2;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		ipsProject = newIpsProject();

		referencingProject = newIpsProject("ReferencingProject");
		IIpsObjectPath path = referencingProject.getIpsObjectPath();
		path.newIpsProjectRefEntry(ipsProject);
		referencingProject.setIpsObjectPath(path);
		
		/* leaveProject1 and leaveProject2 are not directly integrated in any test.
		 * But the tested instance search methods have to search in all project that
		 * holds a reference to the project of the object. So the search for a Object
		 * in e.g. ipsProject have to search for instances in leaveProject1 and
		 * leaveProject2. The tests implicit that no duplicates are found.
		 */
		
		
		leaveProject1 = newIpsProject("LeaveProject1");
		path = leaveProject1.getIpsObjectPath();
		path.newIpsProjectRefEntry(referencingProject);
		leaveProject1.setIpsObjectPath(path);

		leaveProject2 = newIpsProject("LeaveProject2");
		path = leaveProject2.getIpsObjectPath();
		path.newIpsProjectRefEntry(referencingProject);
		leaveProject2.setIpsObjectPath(path);

		
		independentProject = newIpsProject("ReferencedProject");
	}

	/**
	 * Test method for {@link org.faktorips.devtools.core.ui.views.instanceexplorer.InstanceContentProvider#getElements(java.lang.Object)}.
	 * @throws CoreException 
	 */
	public void testGetElements() throws CoreException {
		Object[] result = contentProvider.getElements(null);
		assertTrue(result.length == 0);
		
		result = contentProvider.getElements(new Object());
		assertTrue(result.length == 0);
	}
	
	private IIpsArchive createArchive(IIpsProject project, IIpsProject projectToArchive) throws CoreException {
        IFile archiveFile = projectToArchive.getProject().getFile("test.ipsar");
        archiveFile.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

        File file = archiveFile.getLocation().toFile();
        CreateIpsArchiveOperation operation = new CreateIpsArchiveOperation(projectToArchive.getIpsPackageFragmentRoots(), file);
        operation.setInclJavaBinaries(true);
        operation.setInclJavaSources(true);
        operation.run(null);
        createLinkIfNecessary(archiveFile, file);

        assertTrue(archiveFile.exists());

        IIpsArchive archive = new IpsArchive(project, archiveFile.getLocation());
        return archive;
	}
	
	/**
	 * Test method for {@link org.faktorips.devtools.core.ui.views.instanceexplorer.InstanceContentProvider#getElements(java.lang.Object)}.
	 * @throws CoreException 
	 */
	public void testGetElementsForProductCmptType() throws CoreException {
		String prodCmptTypeQName = "pack.MyProductCmptType";
		String prodCmptTypeIndepQName = "otherpack.MyProductCmptTypeProj2";
		String prodCmpt1QName = "pack.MyProductCmpt1";
		String prodCmpt2QName = "pack.MyProductCmpt2";
		String prodCmpt3QName = "pack.MyProductCmpt3";
		String prodCmptRef1QName = "otherpack.MyProductCmptRef1";
		
		ProductCmptType prodCmptType = newProductCmptType(ipsProject, prodCmptTypeQName);
		ProductCmpt prodCmpt1 = newProductCmpt(prodCmptType, prodCmpt1QName);
		ProductCmpt prodCmpt2 = newProductCmpt(prodCmptType, prodCmpt2QName);
		ProductCmpt prodCmpt3 = newProductCmpt(ipsProject, prodCmpt3QName);
		
		Object[] result = contentProvider.getElements(prodCmptType);
		assertEquals(result.length, 2);
		List<Object> resultList = Arrays.asList(result);
		assertTrue(resultList.contains(prodCmpt1.getIpsSrcFile()));
		assertTrue(resultList.contains(prodCmpt2.getIpsSrcFile()));
		assertFalse(resultList.contains(prodCmpt3.getIpsSrcFile()));
		
		result = contentProvider.getElements(prodCmpt1);
		assertEquals(result.length, 0);

		ProductCmpt prodCmptRef1 = newProductCmpt(referencingProject, prodCmptRef1QName);
		prodCmptRef1.setProductCmptType(prodCmptTypeQName);

		result = contentProvider.getElements(prodCmptType);
		assertEquals(result.length, 3);
		resultList = Arrays.asList(result);
		assertTrue(resultList.contains(prodCmpt1.getIpsSrcFile()));
		assertTrue(resultList.contains(prodCmpt2.getIpsSrcFile()));
		assertTrue(resultList.contains(prodCmptRef1.getIpsSrcFile()));
		assertFalse(resultList.contains(prodCmpt3.getIpsSrcFile()));
		
		ProductCmptType prodCmptTypeIndep = newProductCmptType(independentProject, prodCmptTypeIndepQName);
		
		result = contentProvider.getElements(prodCmptTypeIndep);
		assertEquals(result.length, 0);
		
		String archTypeQName = "archpack.ArchType"; 
		String archCmptQName = "archpack.ArchCmpt"; 
		IIpsProject archProj = newIpsProject("archProj");
		ProductCmptType archType = newProductCmptType(archProj, archTypeQName);
		@SuppressWarnings("unused")
		ProductCmpt archCmpt = newProductCmpt(archType, archCmptQName);
		IIpsArchive archive = createArchive(ipsProject, archProj);

		IIpsObjectPath path = referencingProject.getIpsObjectPath();
		path.newIpsProjectRefEntry(archProj);
		referencingProject.setIpsObjectPath(path);

		path = leaveProject1.getIpsObjectPath();
		IIpsArchiveEntry aentry = path.newArchiveEntry(archive.getArchivePath());
		path.moveEntries(new int[]{aentry.getIndex()}, true);
		leaveProject1.setIpsObjectPath(path);

		result = contentProvider.getElements(archType);
		assertEquals(2, result.length);
	}

	
	/**
	 * Test method for {@link org.faktorips.devtools.core.ui.views.instanceexplorer.InstanceContentProvider#getElements(java.lang.Object)}.
	 * @throws CoreException 
	 */
	public void testGetElementsForTableStructure() throws CoreException {
		String tableStructureQName = "pack.MyTableStructure";
		String tableStructureProj2QName = "otherpack.MyTableStructureProj2";
		String tabContent1QName = "pack.MyTableContent1";
		String tabContent2QName = "pack.MyTableContent2";
		String tabContent3QName = "pack.MyTableContent3";
		String tabContentProj2QName = "otherpack.MyTableContentProj2";
		
		TableStructure tableStructure = newTableStructure(ipsProject, tableStructureQName);
		TableContents tabContent1 = newTableContents(tableStructure, tabContent1QName);
		TableContents tabContent2 = newTableContents(tableStructure, tabContent2QName);
		TableContents tabContent3 = newTableContents(ipsProject, tabContent3QName);
		
		Object[] result = contentProvider.getElements(tableStructure);
		assertEquals(2, result.length);
		List<Object> resultList = Arrays.asList(result);
		assertTrue(resultList.contains(tabContent1.getIpsSrcFile()));
		assertTrue(resultList.contains(tabContent2.getIpsSrcFile()));
		assertFalse(resultList.contains(tabContent3.getIpsSrcFile()));
		
		result = contentProvider.getElements(tabContent1);
		assertEquals(0, result.length);

		TableContents tabContentProj2 = newTableContents(referencingProject, tabContentProj2QName);
		tabContentProj2.setTableStructure(tableStructureQName);

		result = contentProvider.getElements(tableStructure);
		resultList = Arrays.asList(result);
		assertEquals(3, result.length);
		assertTrue(resultList.contains(tabContent1.getIpsSrcFile()));
		assertTrue(resultList.contains(tabContent2.getIpsSrcFile()));
		assertTrue(resultList.contains(tabContentProj2.getIpsSrcFile()));
		assertFalse(resultList.contains(tabContent3.getIpsSrcFile()));
		
		TableStructure tableStructureProj2 = newTableStructure(independentProject, tableStructureProj2QName);
		
		result = contentProvider.getElements(tableStructureProj2);
		assertEquals(0, result.length);
	}

	/**
	 * Test method for {@link org.faktorips.devtools.core.ui.views.instanceexplorer.InstanceContentProvider#getElements(java.lang.Object)}.
	 * @throws CoreException 
	 */
	public void testGetElementsForTestCaseTypes() throws CoreException {
		String testCaseTypeQName = "pack.MyTestCaseType";
		String testCaseTypeProj2QName = "otherpack.MyTestCaseTypeProj2";
		String testCase1QName = "pack.MyTestCase1";
		String testCase2QName = "pack.MyTestCase2";
		String testCase3QName = "pack.MyTestCase3";
		String testCaseProj2QName = "otherpack.MyTestCaseProj2";
		
		TestCaseType testCaseType = newTestCaseType(ipsProject, testCaseTypeQName);
		TestCase testCase1 = newTestCase(testCaseType, testCase1QName);
		TestCase testCase2 = newTestCase(testCaseType, testCase2QName);
		TestCase testCase3 = newTestCase(ipsProject, testCase3QName);
		
		Object[] result = contentProvider.getElements(testCaseType);
		assertEquals(2, result.length);
		List<Object> resultList = Arrays.asList(result);
		assertTrue(resultList.contains(testCase1.getIpsSrcFile()));
		assertTrue(resultList.contains(testCase2.getIpsSrcFile()));
		assertFalse(resultList.contains(testCase3.getIpsSrcFile()));
		
		result = contentProvider.getElements(testCase1);
		assertEquals(0, result.length);

		TestCase testCaseProj2 = newTestCase(referencingProject, testCaseProj2QName);
		testCaseProj2.setTestCaseType(testCaseTypeQName);

		result = contentProvider.getElements(testCaseType);
		resultList = Arrays.asList(result);
		assertEquals(3, result.length);
		assertTrue(resultList.contains(testCase1.getIpsSrcFile()));
		assertTrue(resultList.contains(testCase2.getIpsSrcFile()));
		assertTrue(resultList.contains(testCaseProj2.getIpsSrcFile()));
		assertFalse(resultList.contains(testCase3.getIpsSrcFile()));
		
		TestCaseType testCaseTypeProj2 = newTestCaseType(independentProject, testCaseTypeProj2QName);
		
		result = contentProvider.getElements(testCaseTypeProj2);
		assertEquals(0, result.length);
	}
	
	/**
	 * Test method for {@link org.faktorips.devtools.core.ui.views.instanceexplorer.InstanceContentProvider#getElements(java.lang.Object)}.
	 * @throws CoreException 
	 */
	public void testGetElementsForEnumTypes() throws CoreException {
		String enumTypeQName = "pack.MyEnumType";
		String enumTypeProj2QName = "otherpack.MyEnumTypeProj2";
		String enum1QName = "pack.MyEnum1";
		String enum2QName = "pack.MyEnum2";
		String enum3QName = "pack.MyEnum3";
		String enumProj2QName = "otherpack.MyEnumProj2";
		
		EnumType enumType = newEnumType(ipsProject, enumTypeQName);
		EnumContent enum1 = newEnumContent(enumType, enum1QName);
		EnumContent enum2 = newEnumContent(enumType, enum2QName);
		EnumContent enum3 = newEnumContent(ipsProject, enum3QName);
		
		Object[] result = contentProvider.getElements(enumType);
		List<Object> resultList = Arrays.asList(result);
		assertEquals(2, result.length);
		assertTrue(resultList.contains(enum1.getIpsSrcFile()));
		assertTrue(resultList.contains(enum2.getIpsSrcFile()));
		assertFalse(resultList.contains(enum3.getIpsSrcFile()));
		
		result = contentProvider.getElements(enum1);
		assertEquals(0, result.length);

		EnumContent enumProj2 = newEnumContent(referencingProject, enumProj2QName);
		enumProj2.setEnumType(enumTypeQName);

		result = contentProvider.getElements(enumType);
		resultList = Arrays.asList(result);
		assertEquals(3, result.length);
		assertTrue(resultList.contains(enum1.getIpsSrcFile()));
		assertTrue(resultList.contains(enum2.getIpsSrcFile()));
		assertTrue(resultList.contains(enumProj2.getIpsSrcFile()));
		assertFalse(resultList.contains(enum3.getIpsSrcFile()));
		
		EnumType enumTypeProj2 = newEnumType(independentProject, enumTypeProj2QName);
		
		result = contentProvider.getElements(enumTypeProj2);
		assertEquals(0, result.length);
	}


}
