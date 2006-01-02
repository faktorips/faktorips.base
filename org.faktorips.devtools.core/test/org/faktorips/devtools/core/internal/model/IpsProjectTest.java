package org.faktorips.devtools.core.internal.model;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.dthelpers.DecimalHelper;
import org.faktorips.codegen.dthelpers.MoneyHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.PluginTest;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;


/**
 *
 */
public class IpsProjectTest extends PluginTest {

    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot root;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        root = ipsProject.getIpsPackageFragmentRoots()[0];
    }

    public void testGetValueDatatypes() throws CoreException {
        setDatatypesForIpsProject();
	    ValueDatatype[] types = ipsProject.getValueDatatypes(false);
	    assertEquals(1, types.length);
	    assertEquals(Datatype.DECIMAL, types[0]);
	    
        createRefProject();

	    types = ipsProject.getValueDatatypes(false);
	    assertEquals(2, types.length);
	    assertEquals(Datatype.DECIMAL, types[0]);
	    assertEquals(Datatype.MONEY, types[1]);
        
	    types = ipsProject.getValueDatatypes(true);
	    assertEquals(3, types.length);
	    assertEquals(Datatype.VOID, types[0]);
	    assertEquals(Datatype.DECIMAL, types[1]);
	    assertEquals(Datatype.MONEY, types[2]);
    }
    
    public void testGetDatatypeHelper() throws CoreException {
        setDatatypesForIpsProject();
        DatatypeHelper helper = ipsProject.getDatatypeHelper(Datatype.DECIMAL);
        assertEquals(DecimalHelper.class, helper.getClass());
        helper = ipsProject.getDatatypeHelper(Datatype.MONEY);
        assertNull(helper);
        
        createRefProject();
        helper = ipsProject.getDatatypeHelper(Datatype.MONEY);
        assertEquals(MoneyHelper.class, helper.getClass());
    }
    
    public void testFindDatatypes() throws CoreException {
        setDatatypesForIpsProject();
        IIpsPackageFragment pack = ipsProject.getIpsPackageFragmentRoots()[0].getIpsPackageFragment("");
        IIpsSrcFile file1 = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestObject1", true, null);
        IPolicyCmptType pcType1 = (IPolicyCmptType)file1.getIpsObject();
        
        // only value types, void not included
	    Datatype[] types = ipsProject.findDatatypes(true, false);
	    assertEquals(1, types.length);
	    assertEquals(Datatype.DECIMAL, types[0]);
	    
        // only value types, void included
	    types = ipsProject.findDatatypes(true, true);
	    assertEquals(2, types.length);
	    assertEquals(Datatype.VOID, types[0]);
	    assertEquals(Datatype.DECIMAL, types[1]);
        
        // all types, void not included
	    types = ipsProject.findDatatypes(false, false);
	    assertEquals(2, types.length);
	    assertEquals(Datatype.DECIMAL, types[0]);
	    assertEquals(pcType1, types[1]);
	    
	    // setup dependency to other project, these datatypes of the refenreced project must also be included.
	    IIpsProject refProject = createRefProject();
        pack = refProject.getIpsPackageFragmentRoots()[0].getIpsPackageFragment("");
        IIpsSrcFile file2 = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestObject2", true, null);
        IPolicyCmptType pcType2 = (IPolicyCmptType)file2.getIpsObject();
	    
        // only value types, void not included
	    types = ipsProject.findDatatypes(true, false);
	    assertEquals(2, types.length);
	    assertEquals(Datatype.DECIMAL, types[0]);
	    assertEquals(Datatype.MONEY, types[1]);
	    
        // only value types, void included
	    types = ipsProject.findDatatypes(true, true);
	    assertEquals(3, types.length);
	    assertEquals(Datatype.VOID, types[0]);
	    assertEquals(Datatype.DECIMAL, types[1]);
	    assertEquals(Datatype.MONEY, types[2]);
        
        // all types, void not included
	    types = ipsProject.findDatatypes(false, false);
	    assertEquals(4, types.length);
	    assertEquals(Datatype.DECIMAL, types[0]);
	    assertEquals(Datatype.MONEY, types[1]);
	    assertEquals(pcType1, types[2]);
	    assertEquals(pcType2, types[3]);
        
        // all types, void included
	    types = ipsProject.findDatatypes(false, true);
	    assertEquals(5, types.length);
	    assertEquals(Datatype.VOID, types[0]);
	    assertEquals(Datatype.DECIMAL, types[1]);
	    assertEquals(Datatype.MONEY, types[2]);
	    assertEquals(pcType1, types[3]);
	    assertEquals(pcType2, types[4]);
    }
    
    private void setDatatypesForIpsProject() throws CoreException {
	    IFile typeFile = ipsProject.getDatatypesDefinitionFile();
	    String contents = 
	        "<?xml version=\"1.0\"?>" + 
	        "<DatatypesDefinition>" + 
	        	"<Datatype id=\"Decimal\"/>" + 
	        "</DatatypesDefinition>";
	    ByteArrayInputStream is = new ByteArrayInputStream(contents.getBytes());
	    typeFile.setContents(is, true, false, null);
    }
    
    /*
     * Creates an ips project called RefProject that is referenced by the ips project and has two defined datatypes.
     */
    private IIpsProject createRefProject() throws CoreException {
        IIpsProject refProject = newIpsProject("RefProject");
        // ... set Money and Decimal as the datatype for the referenced ips project
	    IFile typeFile = refProject.getDatatypesDefinitionFile();
	    String contents = 
	        "<?xml version=\"1.0\"?>" + 
	        "<DatatypesDefinition>" + 
	        	"<Datatype id=\"Decimal\"/>" + 
	        	"<Datatype id=\"Money\"/>" + 
	        "</DatatypesDefinition>";
	    ByteArrayInputStream is = new ByteArrayInputStream(contents.getBytes());
	    typeFile.setContents(is, true, false, null);
        
        // set the reference from the ips project to the referenced project
	    IIpsObjectPath path = ipsProject.getIpsObjectPath();
	    path.newIpsProjectRefEntry(refProject);
	    ipsProject.setIpsObjectPath(path);
	    return refProject;
    }

    public void testFindPdObject() throws CoreException {
        IIpsPackageFragment folder = root.createPackageFragment("a.b", true, null);
        IIpsSrcFile file = folder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Test", true, null);
        IIpsObject pdObject = ipsProject.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "a.b.Test");
        assertNotNull(pdObject);
        assertEquals(file.getIpsObject(), pdObject);
        
        assertNull(ipsProject.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "c.Unknown"));
    }

    public void testFindPdObjects() throws CoreException {
        // create the following types: Type0, a.b.Type1 and c.Type2
        root.getIpsPackageFragment("").createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Type0", true, null);
        IIpsPackageFragment folderAB = root.createPackageFragment("a.b", true, null);
        folderAB.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Type1", true, null);
        IIpsPackageFragment folderC = root.createPackageFragment("c", true, null);
        folderC.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Type2", true, null);
        
        // create table c.Table1
        folderC.createIpsFile(IpsObjectType.TABLE_STRUCTURE, "Table1", true, null);
        
        IIpsObject[] result = ipsProject.findIpsObjects(IpsObjectType.PRODUCT_CMPT);
        assertEquals(0, result.length);
        
        result = ipsProject.findIpsObjects(IpsObjectType.POLICY_CMPT_TYPE);
        assertEquals(3, result.length);
        
        result = ipsProject.findIpsObjects(IpsObjectType.TABLE_STRUCTURE);
        assertEquals(1, result.length);
    }
    
    public void testFindProductCmpts() throws CoreException {
        // create the following types: Type0, Type1 and Type2
        IIpsPackageFragment pack = root.createPackageFragment("pack", true, null);
        pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Type0", true, null);
        pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Type1", true, null);
        pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Type2", true, null);
        
        // create the following product compnent: product0, product1, product2
        IIpsSrcFile productFile0 = pack.createIpsFile(IpsObjectType.PRODUCT_CMPT, "Product0", true, null);
        IIpsSrcFile productFile1 = pack.createIpsFile(IpsObjectType.PRODUCT_CMPT, "Product1", true, null);
        IIpsSrcFile productFile2 = pack.createIpsFile(IpsObjectType.PRODUCT_CMPT, "Product2", true, null);
        
        IProductCmpt product0 = (IProductCmpt)productFile0.getIpsObject();
        IProductCmpt product1 = (IProductCmpt)productFile1.getIpsObject();
        IProductCmpt product2 = (IProductCmpt)productFile2.getIpsObject();
        
        product0.setPolicyCmptType("pack.Type0");
        product1.setPolicyCmptType("pack.Type1");
        product2.setPolicyCmptType("pack.Type0");
        
        IProductCmpt[] result = ipsProject.findProductCmpts("pack.Type0", true);
        assertEquals(2, result.length);
        assertEquals(product0, result[0]);
        assertEquals(product2, result[1]);
        
        result = ipsProject.findProductCmpts(null, true);
        assertEquals(3, result.length);
        assertEquals(product0, result[0]);
        assertEquals(product1, result[1]);
        assertEquals(product2, result[2]);
    }
    
    public void testFindIpsSrcFile() throws CoreException {
        IJavaProject javaProject = ipsProject.getJavaProject();
        IPackageFragmentRoot javaRoot = root.getJavaPackageFragmentRoot(IIpsPackageFragmentRoot.JAVA_ROOT_GENERATED_CODE);
        
        IIpsPackageFragment ipsPack = root.getIpsPackageFragment("motor.coverages");
        IPackageFragment interfacePack = ipsPack.getJavaPackageFragment(IIpsPackageFragment.JAVA_PACK_PUBLISHED_INTERFACE);
        ICompilationUnit interfaceCu = interfacePack.getCompilationUnit("CollisionCoverage.java");
        IPackageFragment implementationPack = ipsPack.getJavaPackageFragment(IIpsPackageFragment.JAVA_PACK_IMPLEMENTATION);
        ICompilationUnit implementationCu = implementationPack.getCompilationUnit("CollisionCoverageImpl.java");
        
        // published interface in correct package
        IIpsSrcFile file = ipsProject.findIpsSrcFile(interfaceCu);
        String expectedFileName = IpsObjectType.POLICY_CMPT_TYPE.getFileName("CollisionCoverage");  
        assertNotNull(file);
        assertEquals(expectedFileName, file.getName());
        assertEquals("motor.coverages", file.getIpsPackageFragment().getName());
        
        // published interface in wrong package
        interfaceCu = implementationPack.getCompilationUnit("CollisionCoverage.java");
        file = ipsProject.findIpsSrcFile(interfaceCu);
        assertNull(file);
        
        // implementation class in correct package
        file = ipsProject.findIpsSrcFile(implementationCu);
        assertNotNull(file);
        assertEquals(expectedFileName, file.getName());
        assertEquals("motor.coverages", file.getIpsPackageFragment().getName());
        
        // implementation class in wrong package
        implementationCu = interfacePack.getCompilationUnit("CollisionCoverageImpl.java");
        file = ipsProject.findIpsSrcFile(implementationCu);
        assertNotNull(file); // there is no way to find out that CollisionCoverageImpl is
        					 // not a ips object (unless we prohibit ips names ending with Impl). 
        
        // Java package fragment root does not correspond to the ips root.
        javaRoot = javaProject.getPackageFragmentRoot("test.jar");
        interfacePack = javaRoot.getPackageFragment("motor.coverages");
        interfaceCu = interfacePack.getCompilationUnit("CollisionCoverage.java");
        
        file = ipsProject.findIpsSrcFile(interfaceCu);
        assertNull(file);
    }
    
    public void testSetIpsObjectPath() throws CoreException {
        IFile objectPathFile = ipsProject.getIpsObjectPathFile();
        long stamp = objectPathFile.getModificationStamp();
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.setOutputDefinedPerSrcFolder(false);
        path.setBasePackageNameForGeneratedJavaClasses("some.name");
        ipsProject.setIpsObjectPath(path);
        assertTrue(stamp!=objectPathFile.getModificationStamp());
        
        // following line will receive a new IpsProject instance (as it is only a proxy)
        ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(ipsProject.getProject());
        // test if he changed object path is also available with the new instance
        assertEquals("some.name", ipsProject.getIpsObjectPath().getBasePackageNameForGeneratedJavaClasses());
    }
    
}
