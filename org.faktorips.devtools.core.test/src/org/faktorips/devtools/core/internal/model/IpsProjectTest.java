/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.dthelpers.ArrayOfValueDatatypeHelper;
import org.faktorips.codegen.dthelpers.DecimalHelper;
import org.faktorips.codegen.dthelpers.MoneyHelper;
import org.faktorips.datatype.ArrayOfValueDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.TestEnumType;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructureType;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectNamingConventions;
import org.faktorips.devtools.core.model.IIpsProjectProperties;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;


/**
 *
 */
public class IpsProjectTest extends AbstractIpsPluginTest {

    private IpsProject ipsProject;
    private IpsProject baseProject;
    private IIpsPackageFragmentRoot root;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = (IpsProject)this.newIpsProject("TestProject");
        root = ipsProject.getIpsPackageFragmentRoots()[0];
        
        baseProject = (IpsProject)this.newIpsProject("BaseProject");
        IIpsProjectProperties props = baseProject.getProperties();
        props.setPredefinedDatatypesUsed(new String[]{"Integer"});
        baseProject.setProperties(props);
    }
    
    private void makeIpsProjectDependOnBaseProject() throws CoreException {
        IIpsProjectProperties props = ipsProject.getProperties();
        IIpsObjectPath path = props.getIpsObjectPath();
        path.newIpsProjectRefEntry(baseProject);
        ipsProject.setProperties(props);
    }
    
    public void testValidate() throws Exception {
        Thread.sleep(500); // TODO here we have a threading issue
        // if the validate is called too fast, the .ipsproject-file is not written to disk and so can't be parsed!
    	MessageList list = ipsProject.validate();
    	assertTrue(list.isEmpty());
    }

    public void testValidate_MissingPropertyFile() throws CoreException {
    	IFile file = ipsProject.getIpsProjectPropertiesFile();
    	file.delete(true, false, null);
    	MessageList list = ipsProject.validate();
    	assertNotNull(list.getMessageByCode(IIpsProject.MSGCODE_MISSING_PROPERTY_FILE));
    	assertEquals(1, list.getNoOfMessages());
    }
    
    public void testValidate_UnparsablePropertyFile() throws CoreException {
    	IFile file = ipsProject.getIpsProjectPropertiesFile();
    	InputStream unparsableContents = new ByteArrayInputStream("blabla".getBytes());
    	file.setContents(unparsableContents, true, false, null);
    	MessageList list = ipsProject.validate();
    	assertNotNull(list.getMessageByCode(IIpsProject.MSGCODE_UNPARSABLE_PROPERTY_FILE));
    	assertEquals(1, list.getNoOfMessages());
    }

    public void testGetProperties() {
    	assertNotNull(ipsProject.getProperties());
    }
    
    /**
     * This has once been a bug. If setProperties() only saves the properties
     * to the file without updating it in memory, an access method might return
     * an old value, if it is called before the resource change listener has
     * removed the old prop file from the cache in the model.
     */
    public void testSetProperties_RacingCondition() throws CoreException {
    	IIpsProjectProperties props = ipsProject.getProperties();
    	props.setRuntimeIdPrefix("newPrefix");
    	ipsProject.setProperties(props);
    	assertEquals("newPrefix", ipsProject.getRuntimeIdPrefix());
    }
    
    public void testSetProperties() throws CoreException {
    	IIpsProjectProperties props = ipsProject.getProperties();
    	String builderSetId = props.getBuilderSetId();
    	props.setBuilderSetId("myBuilder");
    	
    	// test if a copy was returned
    	assertEquals(builderSetId, ipsProject.getProperties().getBuilderSetId());
    	
    	// test if prop file is updated
    	IFile propFile = ipsProject.getIpsProjectPropertiesFile();
    	long stamp = propFile.getModificationStamp();
    	ipsProject.setProperties(props);
    	assertTrue(propFile.getModificationStamp()!=stamp);
    	assertEquals("myBuilder", ipsProject.getProperties().getBuilderSetId());
    	
    	// test if a copy was created during set
    	props.setBuilderSetId("newBuilder");
    	assertEquals("myBuilder", ipsProject.getProperties().getBuilderSetId());
    }

    public void testFindProductCmptType() throws CoreException {
    	IPolicyCmptType policyCmptType = (IPolicyCmptType)newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "motor.MotorPolicy");
    	policyCmptType.setUnqualifiedProductCmptType("MotorProduct");
    	policyCmptType.getIpsSrcFile().save(true, null);
    	IProductCmptType productCmptType = ipsProject.findProductCmptType("motor.MotorProduct");
    	assertNotNull(productCmptType);
    	assertEquals("motor.MotorProduct", productCmptType.getQualifiedName());
    	assertEquals(policyCmptType, productCmptType.findPolicyCmptyType());
    	
    	IIpsProject prj2 = this.newIpsProject("TestProject2");
    	IIpsObjectPath path = prj2.getIpsObjectPath();
    	path.newIpsProjectRefEntry(ipsProject);
    	prj2.setIpsObjectPath(path);
    	productCmptType = prj2.findProductCmptType("motor.MotorProduct");
    	assertNotNull(productCmptType);
    }

    public void testGetValueDatatypes() throws CoreException {
    	IIpsProjectProperties props = ipsProject.getProperties();
    	props.setPredefinedDatatypesUsed(new String[]{Datatype.DECIMAL.getQualifiedName()});
        ipsProject.setProperties(props);
	    ValueDatatype[] types = ipsProject.getValueDatatypes(false);
	    assertEquals(1, types.length);
	    assertEquals(Datatype.DECIMAL, types[0]);
	    
        createRefProject();

	    types = ipsProject.getValueDatatypes(false);
	    assertEquals(3, types.length);
	    assertEquals(Datatype.DECIMAL, types[0]);
	    assertEquals(Datatype.MONEY, types[1]);
	    assertEquals(TestEnumType.class.getName(), types[2].getJavaClassName());
        
	    types = ipsProject.getValueDatatypes(true);
	    assertEquals(4, types.length);
	    assertEquals(Datatype.VOID, types[0]);
	    assertEquals(Datatype.DECIMAL, types[1]);
	    assertEquals(Datatype.MONEY, types[2]);
	    assertEquals(TestEnumType.class.getName(), types[3].getJavaClassName());
    }
    
    public void testGetValueDatatypes_boolean_boolean() throws CoreException {
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setPredefinedDatatypesUsed(new String[]{Datatype.DECIMAL.getQualifiedName(), Datatype.PRIMITIVE_INT.getQualifiedName()});
        ipsProject.setProperties(props);
        ValueDatatype[] types = ipsProject.getValueDatatypes(false, false);
        assertEquals(1, types.length);
        assertEquals(Datatype.DECIMAL, types[0]);

        types = ipsProject.getValueDatatypes(false, true);
        assertEquals(2, types.length);
        assertEquals(Datatype.DECIMAL, types[0]);
        assertEquals(Datatype.PRIMITIVE_INT, types[1]);
    }

    public void testFindDatatype() throws CoreException {
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setPredefinedDatatypesUsed(new String[]{Datatype.DECIMAL.getQualifiedName(), Datatype.PRIMITIVE_INT.getQualifiedName()});
        ipsProject.setProperties(props);

        IPolicyCmptType pcType = newPolicyCmptType(ipsProject, "Policy");
        pcType.getIpsSrcFile().save(true, null);
        assertEquals(pcType, ipsProject.findDatatype("Policy"));

        assertEquals(Datatype.VOID, ipsProject.findDatatype("void"));
        
        assertEquals(Datatype.DECIMAL, ipsProject.findDatatype("Decimal"));
        ArrayOfValueDatatype type = (ArrayOfValueDatatype)ipsProject.findDatatype("Decimal[][]");
        assertEquals(Datatype.DECIMAL, type.getBasicDatatype());
        assertEquals(2, type.getDimension());
        assertEquals(Datatype.DECIMAL, ipsProject.findDatatype("Decimal"));
        assertNull(ipsProject.findDatatype("Unknown"));
        assertNull(ipsProject.findDatatype("Integer"));
        
        makeIpsProjectDependOnBaseProject();
        assertEquals(Datatype.INTEGER, ipsProject.findDatatype("Integer"));
        
    }

    public void testFindValueDatatype() throws CoreException {
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setPredefinedDatatypesUsed(new String[]{Datatype.DECIMAL.getQualifiedName(), Datatype.PRIMITIVE_INT.getQualifiedName()});
        ipsProject.setProperties(props);

        assertEquals(Datatype.DECIMAL, ipsProject.findValueDatatype("Decimal"));
        ArrayOfValueDatatype type = (ArrayOfValueDatatype)ipsProject.findValueDatatype("Decimal[][]");
        assertEquals(Datatype.DECIMAL, type.getBasicDatatype());
        assertEquals(2, type.getDimension());
        assertEquals(Datatype.DECIMAL, ipsProject.findValueDatatype("Decimal"));
        assertNull(ipsProject.findValueDatatype("Unknown"));
        assertNull(ipsProject.findValueDatatype("Integer"));
        
        makeIpsProjectDependOnBaseProject();
        assertEquals(Datatype.INTEGER, ipsProject.findDatatype("Integer"));
        
        newPolicyCmptType(ipsProject, "Policy");
        assertNull(ipsProject.findValueDatatype("Policy"));
    }
    
    public void testFindValueDatatypeTableBasedEunm() throws CoreException {
        ITableStructure structure = (ITableStructure)newIpsObject(ipsProject, IpsObjectType.TABLE_STRUCTURE, "table.Structure");
        structure.setTableStructureType(TableStructureType.ENUMTYPE_MODEL);
        structure.getIpsSrcFile().save(true, null);
        
        assertNotNull(ipsProject.findValueDatatype("table.Structure"));
        assertNull(ipsProject.findValueDatatype("table.Structurex"));
    }
    
    public void testGetDatatypeHelper() throws CoreException {
    	IIpsProjectProperties props = ipsProject.getProperties();
    	props.setPredefinedDatatypesUsed(new String[]{Datatype.DECIMAL.getQualifiedName()});
        ipsProject.setProperties(props);
        DatatypeHelper helper = ipsProject.getDatatypeHelper(Datatype.DECIMAL);
        assertEquals(DecimalHelper.class, helper.getClass());
        helper = ipsProject.getDatatypeHelper(new ArrayOfValueDatatype(Datatype.DECIMAL, 1));
        assertNotNull(helper);
        assertEquals(ArrayOfValueDatatypeHelper.class, helper.getClass());
        
        helper = ipsProject.getDatatypeHelper(Datatype.MONEY);
        assertNull(helper);
        
        createRefProject();
        helper = ipsProject.getDatatypeHelper(Datatype.MONEY);
        assertEquals(MoneyHelper.class, helper.getClass());
    }
    
    public void testFindDatatypeString() throws Exception{
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setPredefinedDatatypesUsed(new String[]{Datatype.DECIMAL.getQualifiedName()});
        ipsProject.setProperties(props);
        Datatype type = ipsProject.findDatatype(Datatype.DECIMAL.getQualifiedName());
        assertNotNull(type);
        assertEquals(type, Datatype.DECIMAL);
        type = ipsProject.findDatatype(Datatype.DECIMAL.getQualifiedName() + "[]");
        assertTrue(type instanceof ArrayOfValueDatatype);
        ArrayOfValueDatatype arrayType = (ArrayOfValueDatatype)type;
        assertEquals(Datatype.DECIMAL, arrayType.getBasicDatatype());
    }
    
    public void testFindDatatypes() throws CoreException {
    	IIpsProjectProperties props = ipsProject.getProperties();
    	props.setPredefinedDatatypesUsed(new String[]{Datatype.DECIMAL.getQualifiedName()});
        ipsProject.setProperties(props);
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
	    assertEquals(3, types.length);
	    assertEquals(Datatype.DECIMAL, types[0]);
	    assertEquals(Datatype.MONEY, types[1]);
	    assertEquals(TestEnumType.class.getName(), types[2].getJavaClassName());
	    
        // only value types, void included
	    types = ipsProject.findDatatypes(true, true);
	    assertEquals(4, types.length);
	    assertEquals(Datatype.VOID, types[0]);
	    assertEquals(Datatype.DECIMAL, types[1]);
	    assertEquals(Datatype.MONEY, types[2]);
	    assertEquals(TestEnumType.class.getName(), types[3].getJavaClassName());
	    
        // all types, void not included
	    types = ipsProject.findDatatypes(false, false);
	    assertEquals(5, types.length);
	    assertEquals(Datatype.DECIMAL, types[0]);
	    assertEquals(Datatype.MONEY, types[1]);
	    assertEquals(TestEnumType.class.getName(), types[2].getJavaClassName());
	    assertEquals(pcType1, types[3]);
	    assertEquals(pcType2, types[4]);
	    
	    
        // all types, void included
	    types = ipsProject.findDatatypes(false, true);
	    assertEquals(6, types.length);
	    assertEquals(Datatype.VOID, types[0]);
	    assertEquals(Datatype.DECIMAL, types[1]);
	    assertEquals(Datatype.MONEY, types[2]);
	    assertEquals(TestEnumType.class.getName(), types[3].getJavaClassName());
	    assertEquals(pcType1, types[4]);
	    assertEquals(pcType2, types[5]);
	    
    }
    
    /*
     * Creates an ips project called RefProject that is referenced by the ips project and has 2 predefined datatypes and
     * one dynamic datatype.
     */
    private IIpsProject createRefProject() throws CoreException {
        IIpsProject refProject = newIpsProject("RefProject");
    	IIpsProjectProperties props = refProject.getProperties();
    	props.setPredefinedDatatypesUsed(new String[]{Datatype.DECIMAL.getQualifiedName(), Datatype.MONEY.getQualifiedName()});
    	refProject.setProperties(props);
        
        newDefinedEnumDatatype((IpsProject)refProject, new Class[]{TestEnumType.class});
        // set the reference from the ips project to the referenced project
	    IIpsObjectPath path = ipsProject.getIpsObjectPath();
	    path.newIpsProjectRefEntry(refProject);
	    ipsProject.setIpsObjectPath(path);
	    return refProject;
    }

    public void testFindIpsObject() throws CoreException {
        IIpsPackageFragment folder = root.createPackageFragment("a.b", true, null);
        IIpsSrcFile file = folder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Test", true, null);
        IIpsObject pdObject = ipsProject.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "a.b.Test");
        assertNotNull(pdObject);
        assertEquals(file.getIpsObject(), pdObject);
        
        assertNull(ipsProject.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "c.Unknown"));

        // invalid package name as it contains a blank => should return null
        assertNull(ipsProject.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "c a.Unknown"));
    }

    public void testFindIpsObjects() throws CoreException {
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

        IPolicyCmptType pct = (IPolicyCmptType)result[1]; 
        
        result = ipsProject.findIpsObjects(IpsObjectType.TABLE_STRUCTURE);
        assertEquals(1, result.length);
        
        result = ipsProject.findIpsObjects(IpsObjectType.PRODUCT_CMPT_TYPE);
        assertEquals(0, result.length);

        pct.setConfigurableByProductCmptType(true);
        pct.setUnqualifiedProductCmptType("productCmptTypeName");

        result = ipsProject.findIpsObjects(IpsObjectType.PRODUCT_CMPT_TYPE);
        assertEquals(1, result.length);
        
        IIpsObject ipsObj = ipsProject.findIpsObject(pct.getQualifiedNameType());
        assertEquals(pct, ipsObj);
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
    
    public void testSetIpsObjectPath() throws CoreException {
        IFile projectFile = ipsProject.getIpsProjectPropertiesFile();
        long stamp = projectFile.getModificationStamp();
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.setOutputDefinedPerSrcFolder(false);
        path.setBasePackageNameForGeneratedJavaClasses("some.name");
        ipsProject.setIpsObjectPath(path);
        assertTrue(stamp!=projectFile.getModificationStamp());
        
        // following line will receive a new IpsProject instance (as it is only a proxy)
        IIpsProject ipsProject2 = IpsPlugin.getDefault().getIpsModel().getIpsProject(ipsProject.getProject());
        // test if he changed object path is also available with the new instance
        assertEquals("some.name", ipsProject2.getIpsObjectPath().getBasePackageNameForGeneratedJavaClasses());
    }

    public void testFindReferencingProductCmptGenerations() throws CoreException {
    	IIpsPackageFragmentRoot[] roots = this.ipsProject.getIpsPackageFragmentRoots();
    	assertEquals(roots.length, 1);
    	
    	IIpsPackageFragment pack = roots[0].getIpsPackageFragment("");
    	IProductCmpt tobereferenced = (IProductCmpt)this.newIpsObject(pack, IpsObjectType.PRODUCT_CMPT, "tobereferenced");
    	IProductCmpt noref = (IProductCmpt)this.newIpsObject(pack, IpsObjectType.PRODUCT_CMPT, "noref");
    	IProductCmpt ref1 = (IProductCmpt)this.newIpsObject(pack, IpsObjectType.PRODUCT_CMPT, "ref1");
    	
    	IProductCmptGeneration gen1 = (IProductCmptGeneration)ref1.newGeneration();
    	IProductCmptGeneration genNoref = (IProductCmptGeneration)noref.newGeneration();
    	IProductCmptGeneration genTobereferenced = (IProductCmptGeneration)tobereferenced.newGeneration();
    	
    	GregorianCalendar cal = new GregorianCalendar(2005, 1, 1);
    	gen1.setValidFrom(cal);
    	genNoref.setValidFrom(cal);
    	genTobereferenced.setValidFrom(cal);
    	gen1.newRelation("xxx").setTarget(tobereferenced.getQualifiedName());
        IpsPlugin.getDefault().getIpsPreferences().setWorkingDate(cal);
    	
    	IProductCmptGeneration[] result = ipsProject.findReferencingProductCmptGenerations(tobereferenced.getQualifiedName());
    	assertEquals(result.length, 1);
    	assertEquals(result[0], gen1);
    }
    public void testFindReferencingPolicyCmptTypes() throws CoreException{
        IPolicyCmptType pcTypeReferenced = newPolicyCmptType(root, "tobereferenced");
        IPolicyCmptType pcType = newPolicyCmptType(root, "TestPCType");
        IRelation relation= pcType.newRelation();
        relation.setTarget(pcTypeReferenced.getQualifiedName());

        IPolicyCmptType pcType2 = newPolicyCmptType(root, "TestPCType2");
        IRelation relation2= pcType2.newRelation();
        relation2.setTarget(pcTypeReferenced.getQualifiedName());
        
        IPolicyCmptType pcType3 = newPolicyCmptType(root, "TestPCType3");
        IRelation relation3= pcType3.newRelation();
        relation3.setTarget(pcTypeReferenced.getQualifiedName());
        
        IPolicyCmptType pcTypeNoRef = newPolicyCmptType(root, "TestPCTypeNoRef");
        
        IPolicyCmptType pcTypeSuper = newPolicyCmptType(root, "TestPCTypeSuper");
        pcTypeReferenced.setSupertype("TestPCTypeSuper");
        
        IPolicyCmptType[] results= ipsProject.findReferencingPolicyCmptTypes(pcTypeReferenced);
        assertEquals(4, results.length);
        
        HashSet resultSet= new HashSet();
        resultSet.add(results[0]);
        resultSet.add(results[1]);
        resultSet.add(results[2]);
        resultSet.add(results[3]);
        HashSet expectedSet= new HashSet();
        expectedSet.add(pcType);
        expectedSet.add(pcType2);
        expectedSet.add(pcType3);
        expectedSet.add(pcTypeSuper);

        assertEquals(expectedSet, resultSet);
        assertFalse(resultSet.contains(pcTypeNoRef));
        assertFalse(resultSet.contains(null));

        // test references with faulty supertype 
        pcTypeReferenced.setSupertype("incorrectQualifiedName");
        
        results= ipsProject.findReferencingPolicyCmptTypes(pcTypeReferenced);
        assertEquals(3, results.length);
        
        resultSet= new HashSet();
        resultSet.add(results[0]);
        resultSet.add(results[1]);
        resultSet.add(results[2]);
        expectedSet= new HashSet();
        expectedSet.add(pcType);
        expectedSet.add(pcType2);
        expectedSet.add(pcType3);
        
        assertEquals(expectedSet, resultSet);
        assertFalse(resultSet.contains(pcTypeNoRef));
        assertFalse(resultSet.contains(null));
    }
    
    public void testFindEnumDatatypes() throws CoreException{
    	newDefinedEnumDatatype(ipsProject, new Class[]{TestEnumType.class});
    	EnumDatatype[] dataType = ipsProject.findEnumDatatypes();
    	assertEquals(1, dataType.length);
    	assertEquals("TestEnumType", dataType[0].getQualifiedName());
    	
    }
    
    public void testFindEnumDatatype() throws CoreException{
    	newDefinedEnumDatatype(ipsProject, new Class[]{TestEnumType.class});
    	EnumDatatype dataType = ipsProject.findEnumDatatype("TestEnumType");
    	assertEquals("TestEnumType", dataType.getQualifiedName());
    }
    

    public void testGetNonIpsResources() throws CoreException{        
        IProject projectHandle= ipsProject.getProject();
        IFolder nonIpsRoot= projectHandle.getFolder("nonIpsRoot");
        nonIpsRoot.create(true, false, null);
        IFile nonIpsFile= projectHandle.getFile("nonIpsFile");
        nonIpsFile.create(null, true, null);
        
        IFolder classpathFolder= projectHandle.getFolder("classpathFolder");
        classpathFolder.create(true, false, null);
        IFolder outputFolder= projectHandle.getFolder("outputFolder");
        outputFolder.create(true, false, null);
        IFile classpathFile= projectHandle.getFile("classpathFile");
        classpathFile.create(null, true, null);
        
        // add classpathFolder and classpathFile to the javaprojects classpath
        // add outputFolder as apecific outputlocation of classpathFolder
        IJavaProject javaProject= ipsProject.getJavaProject();
        IClasspathEntry[] cpEntries= javaProject.getRawClasspath();
        IClasspathEntry[] newEntries= new IClasspathEntry[2];
        newEntries[0]= JavaCore.newSourceEntry(classpathFolder.getFullPath());
        newEntries[1]= JavaCore.newSourceEntry(classpathFile.getFullPath(), new IPath[]{}, outputFolder.getFullPath());
        IClasspathEntry[] result= new IClasspathEntry[cpEntries.length+newEntries.length];
        System.arraycopy(cpEntries, 0, result, 0, cpEntries.length);
        System.arraycopy(newEntries, 0, result, cpEntries.length, newEntries.length);
        ipsProject.getJavaProject().setRawClasspath(result, null);
        
        Object[] nonIpsResources= ipsProject.getNonIpsResources();
        assertEquals(5, nonIpsResources.length);
        List list= Arrays.asList(nonIpsResources);
        assertTrue(list.contains(nonIpsRoot));
        assertTrue(list.contains(nonIpsFile));
        // /bin, /src and /extension are outputfolders or classpath entries and thus filtered out
        assertTrue(list.contains(projectHandle.getFile(".project")));
        assertTrue(list.contains(projectHandle.getFile(".ipsproject")));
        assertTrue(list.contains(projectHandle.getFile(".classpath")));
        
        assertFalse(list.contains(classpathFolder));
        assertFalse(list.contains(classpathFile));
        assertFalse(list.contains(outputFolder));
    }
    
    public void testDependsOn() throws CoreException {
        assertFalse(ipsProject.dependsOn(baseProject));
        
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(baseProject);
        ipsProject.setIpsObjectPath(path);
        assertTrue(ipsProject.dependsOn(baseProject));
        
        // transitivitaet der beziehung beruecksichtigt?
        IIpsProject project3 = newIpsProject("Project3");
        path = project3.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject);
        project3.setIpsObjectPath(path);
        assertTrue(project3.dependsOn(ipsProject));
        assertTrue(project3.dependsOn(baseProject));
    }
    
    public void testValidateRequiredFeatures() throws CoreException {
        MessageList ml = ipsProject.validate();
        assertNull(ml.getMessageByCode(IIpsProject.MSGCODE_NO_VERSIONMANAGER));
        IIpsProjectProperties props = ipsProject.getProperties();
        IpsProjectProperties propsOrig = new IpsProjectProperties(ipsProject, (IpsProjectProperties)props);
        props.setMinRequiredVersionNumber("unknown-feature", "1.0.0");
        ipsProject.setProperties(props);
        
        ml = ipsProject.validate();
        assertNotNull(ml.getMessageByCode(IIpsProject.MSGCODE_NO_VERSIONMANAGER));

        ipsProject.setProperties(propsOrig);
        setVersion("0.0.0");
        ml = ipsProject.validate();
        assertNull(ml.getMessageByCode(IIpsProject.MSGCODE_NO_VERSIONMANAGER));
        assertNull(ml.getMessageByCode(IIpsProject.MSGCODE_VERSION_TOO_LOW));
        assertNotNull(ml.getMessageByCode(IIpsProject.MSGCODE_INCOMPATIBLE_VERSIONS));
        
        setVersion("999999.0.0");
        ml = ipsProject.validate();
        assertNotNull(ml.getMessageByCode(IIpsProject.MSGCODE_VERSION_TOO_LOW));      
    }
    
    public void testValidateMissingMigration() throws Exception {
        MessageList ml = ipsProject.validate();
        assertNull(ml.getMessageByCode(IIpsProject.MSGCODE_INVALID_MIGRATION_INFORMATION));
        
        setVersion("0.0.3");
        ml = ipsProject.validate();
        assertNotNull(ml.getMessageByCode(IIpsProject.MSGCODE_INVALID_MIGRATION_INFORMATION));
    }
    
    public void testValidateDuplicateBasePackageGenerated() throws Exception {
        IIpsProject ipsProject2 = (IpsProject)this.newIpsProject("TestProject2");

        MessageList ml = ipsProject.validate();
        assertNull(ml.getMessageByCode(IIpsProject.MSGCODE_DUPLICATE_BASE_PACKAGE_NAME_FOR_GENERATED_CLASSES_IN_DIFFERENT_PROJECTS));

        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IIpsSrcFolderEntry srcFolder = path.newSourceFolderEntry((IFolder)root.getEnclosingResource());
        srcFolder.setSpecificBasePackageNameForExtensionJavaClasses("srctest");
        path.setBasePackageNameForGeneratedJavaClasses("test");
        path.newIpsProjectRefEntry(ipsProject2);
        ipsProject.setIpsObjectPath(path);
        
        // check same base package name and product definition project
        path = ipsProject2.getIpsObjectPath();
        path.setBasePackageNameForGeneratedJavaClasses("test");
        ipsProject2.setIpsObjectPath(path);
        
        IIpsProjectProperties props = ipsProject2.getProperties();
        props.setProductDefinitionProject(true);
        ipsProject2.setProperties(props);
        ml = ipsProject.validate();
        assertEquals(IIpsProject.MSGCODE_DUPLICATE_BASE_PACKAGE_NAME_FOR_GENERATED_CLASSES_IN_DIFFERENT_PROJECTS, ml
                .getFirstMessage(Message.ERROR).getCode());
        
        // check same base package name per src folder
        path = ipsProject2.getIpsObjectPath();
        path.setBasePackageNameForGeneratedJavaClasses("test2");
        path.setOutputDefinedPerSrcFolder(true);
        ipsProject2.setIpsObjectPath(path);
        ml = ipsProject.validate();
        assertNotNull(ml.getMessageByCode(IIpsProject.MSGCODE_DUPLICATE_BASE_PACKAGE_NAME_FOR_GENERATED_CLASSES_IN_DIFFERENT_PROJECTS));  
        
        // check different base package name
        path = ipsProject2.getIpsObjectPath();
        path.setBasePackageNameForGeneratedJavaClasses("test2");
        path.setOutputDefinedPerSrcFolder(false);
        ipsProject2.setIpsObjectPath(path);
        ml = ipsProject.validate();
        assertNull(ml.getMessageByCode(IIpsProject.MSGCODE_DUPLICATE_BASE_PACKAGE_NAME_FOR_GENERATED_CLASSES_IN_DIFFERENT_PROJECTS));  
        
        // check same base package name and product definition project
        path = ipsProject2.getIpsObjectPath();
        path.setBasePackageNameForGeneratedJavaClasses("test");
        ipsProject2.setIpsObjectPath(path);        
        props = ipsProject2.getProperties();
        props.setProductDefinitionProject(false);        
        ml = ipsProject.validate();
        assertNull(ml.getMessageByCode(IIpsProject.MSGCODE_DUPLICATE_BASE_PACKAGE_NAME_FOR_GENERATED_CLASSES_IN_DIFFERENT_PROJECTS));  
    }
    
    public void testValidateIpsObjectPathCycle() throws CoreException{
        IIpsProject ipsProject2 = (IpsProject)this.newIpsProject("TestProject2");
        IIpsObjectPath path = ipsProject2.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject);
        ipsProject2.setIpsObjectPath(path);
        
        MessageList ml = ipsProject.validate();
        assertNull(ml.getMessageByCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));
        
        path = ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject2);
        ipsProject.setIpsObjectPath(path);
        
        List result = new ArrayList();
        ipsProject.findAllIpsObjects(result);
        // there is an cycle in the ref projects,
        // if we get no stack overflow exception, then the test was successfully executed
        
        ml = ipsProject.validate();
        assertNotNull(ml.getMessageByCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));
        
        path = ipsProject.getIpsObjectPath();
        path.removeProjectRefEntry(ipsProject2);
        ipsProject.setIpsObjectPath(path);  
        
        ml = ipsProject.validate();
        assertNull(ml.getMessageByCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));
        
        // test cycle if project has a self reference
        path = ipsProject.getIpsObjectPath();
        path.removeProjectRefEntry(ipsProject2);
        path.newIpsProjectRefEntry(ipsProject);
        ipsProject.setIpsObjectPath(path);
        
        result = new ArrayList();
        ipsProject.findAllIpsObjects(result);
        ipsProject.findIpsObject(new QualifiedNameType("xyz", IpsObjectType.PRODUCT_CMPT));
        // there is an cycle in the ref projects,
        // if we get no stack overflow exception, then the test was successfully executed
    }
    
    private void setVersion(String version) throws CoreException {
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setMinRequiredVersionNumber("org.faktorips.feature", version);
        ipsProject.setProperties(props);
    }
    
    public void testGetNamingConventions() throws CoreException{
        IIpsProjectNamingConventions namingConventions = ipsProject.getNamingConventions();
        assertTrue(namingConventions instanceof DefaultIpsProjectNamingConventions);
        assertFalse(namingConventions.validateIpsPackageName("testPackage").containsErrorMsg());
        assertTrue(namingConventions.validateIpsPackageName("1").containsErrorMsg());
    }
    
    public void testCheckForDuplicateRuntimeIds() throws CoreException {
        IIpsProject prj = newIpsProject("PRJ1");
        IProductCmpt cmpt1 = newProductCmpt(prj, "product1");
        IProductCmpt cmpt2 = newProductCmpt(prj, "product2");
        cmpt1.setRuntimeId("Egon");
        cmpt2.setRuntimeId("Egon");
        assertEquals(cmpt1.getRuntimeId(), cmpt2.getRuntimeId());

        MessageList ml = prj.checkForDuplicateRuntimeIds();
        assertEquals(1, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION));

        cmpt2.setRuntimeId("Hugo");
        ml = prj.checkForDuplicateRuntimeIds();
        assertEquals(0, ml.getNoOfMessages());
        assertNull(ml.getMessageByCode(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION));

        // test that not linked projects are not checked against each other
        IProductCmpt cmpt3 = newProductCmpt(ipsProject, "product3");
        cmpt3.setRuntimeId("Egon");
        cmpt2.setRuntimeId("Egon");
        ml = prj.checkForDuplicateRuntimeIds();
        assertEquals(1, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION));

        // test that linked projects will be checked against each other
        IIpsObjectPath objectPath = prj.getIpsObjectPath();
        objectPath.newIpsProjectRefEntry(ipsProject);
        prj.setIpsObjectPath(objectPath);
        ml = prj.checkForDuplicateRuntimeIds();
        assertEquals(3, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION));

        ml = prj.checkForDuplicateRuntimeIds(new IProductCmpt[] { cmpt3 });
        assertEquals(2, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION));

        ml = prj.checkForDuplicateRuntimeIds(new IProductCmpt[] { cmpt1, cmpt3 });
        assertEquals(4, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION));
    }    
    
    public void testFindAllIpsObjects() throws CoreException{
        IIpsObject a = newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0], IpsObjectType.POLICY_CMPT_TYPE, "a");
        IIpsObject b = newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0], IpsObjectType.PRODUCT_CMPT, "b");
        IIpsObject c = newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0], IpsObjectType.TABLE_STRUCTURE, "c");
        IIpsObject d = newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0], IpsObjectType.TABLE_CONTENTS, "d");
        IIpsObject e = newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0], IpsObjectType.BUSINESS_FUNCTION, "e");
        IIpsObject f = newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0], IpsObjectType.TEST_CASE, "f");
        IIpsObject g = newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0], IpsObjectType.TEST_CASE_TYPE, "g");
        
        List result = new ArrayList(7);
        ipsProject.findAllIpsObjects(result);
        
        assertTrue(result.contains(a));
        assertTrue(result.contains(b));
        assertTrue(result.contains(c));
        assertTrue(result.contains(d));
        assertTrue(result.contains(e));
        assertTrue(result.contains(f));
        assertTrue(result.contains(g));
    }
}
