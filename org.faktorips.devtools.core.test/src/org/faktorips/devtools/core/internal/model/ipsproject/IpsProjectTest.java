/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestEnumType;
import org.faktorips.abstracttest.TestIpsFeatureVersionManager;
import org.faktorips.abstracttest.builder.TestArtefactBuilderSetInfo;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.dthelpers.ArrayOfValueDatatypeHelper;
import org.faktorips.codegen.dthelpers.DecimalHelper;
import org.faktorips.codegen.dthelpers.MoneyHelper;
import org.faktorips.datatype.ArrayOfValueDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.JavaClass2DatatypeAdaptor;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectNamingConventions;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

public class IpsProjectTest extends AbstractIpsPluginTest {

    private IpsProject ipsProject;
    private IpsProject baseProject;
    private IIpsPackageFragmentRoot root;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = (IpsProject)this.newIpsProject("TestProject");
        root = ipsProject.getIpsPackageFragmentRoots()[0];

        baseProject = (IpsProject)this.newIpsProject("BaseProject");
        IIpsProjectProperties props = baseProject.getProperties();
        props.setPredefinedDatatypesUsed(new String[] { "Integer" });
        baseProject.setProperties(props);
    }

    public void testFindIpsPackageFragmentRoot() {
        assertEquals(root, ipsProject.findIpsPackageFragmentRoot(root.getName()));
        assertNull(ipsProject.findIpsPackageFragmentRoot("Unknown"));

        IIpsProject notExistingProject = IpsPlugin.getDefault().getIpsModel().getIpsProject("NotExistingProject");
        assertFalse(notExistingProject.exists());
        assertNull(notExistingProject.findIpsPackageFragmentRoot("src"));
    }

    public void testGetValueSetTypes() {
        List<ValueSetType> types = ipsProject.getValueSetTypes(Datatype.STRING);
        assertEquals(2, types.size());
        assertTrue(types.contains(ValueSetType.UNRESTRICTED));
        assertTrue(types.contains(ValueSetType.ENUM));

        types = ipsProject.getValueSetTypes(Datatype.INTEGER);
        assertEquals(3, types.size());
        assertTrue(types.contains(ValueSetType.UNRESTRICTED));
        assertTrue(types.contains(ValueSetType.RANGE));
        assertTrue(types.contains(ValueSetType.ENUM));
    }

    public void testIsValueSetTypeApplicable() throws CoreException {
        assertTrue(ipsProject.isValueSetTypeApplicable(Datatype.INTEGER, ValueSetType.ENUM));
        assertTrue(ipsProject.isValueSetTypeApplicable(Datatype.INTEGER, ValueSetType.RANGE));
        assertTrue(ipsProject.isValueSetTypeApplicable(Datatype.INTEGER, ValueSetType.UNRESTRICTED));

        assertTrue(ipsProject.isValueSetTypeApplicable(Datatype.STRING, ValueSetType.ENUM));
        assertFalse(ipsProject.isValueSetTypeApplicable(Datatype.STRING, ValueSetType.RANGE));
        assertTrue(ipsProject.isValueSetTypeApplicable(Datatype.STRING, ValueSetType.UNRESTRICTED));
    }

    public void testFindAllProductCmpts() throws CoreException {
        // Create the following types: Type0, Type1 and Type2.
        IProductCmptType type0 = newProductCmptType(ipsProject, "pack.Type0");
        IProductCmptType type1 = newProductCmptType(ipsProject, "pack.Type1");

        IProductCmpt product0 = newProductCmpt(type0, "products.Product0");
        IProductCmpt product1 = newProductCmpt(type1, "products.Product1");
        IProductCmpt product2 = newProductCmpt(type0, "products.Product2");

        IProductCmpt[] result = ipsProject.findAllProductCmpts(type0, true);
        assertEquals(2, result.length);
        assertEquals(product0, result[0]);
        assertEquals(product2, result[1]);

        result = ipsProject.findAllProductCmpts(null, true);
        assertEquals(3, result.length);
        assertEquals(product0, result[0]);
        assertEquals(product1, result[1]);
        assertEquals(product2, result[2]);

        // Consider class hierarchy; make type1 a subtype of type0.
        type1.setSupertype(type0.getQualifiedName());
        result = ipsProject.findAllProductCmpts(type0, true);
        assertEquals(3, result.length);
        assertEquals(product0, result[0]);
        assertEquals(product1, result[1]);
        assertEquals(product2, result[2]);

        result = ipsProject.findAllProductCmpts(type0, false);
        assertEquals(2, result.length);
        assertEquals(product0, result[0]);
        assertEquals(product2, result[1]);

        // TODO v2 - test with more than one project
    }

    public void testFindProductCmptsByPolicyCmptWithExistingProductCmptMissingPolicyCmpt() throws CoreException {
        IProductCmptType type = newProductCmptType(ipsProject, "MotorProduct");
        newProductCmpt(type, "ProductCmpt1");
        IProductCmpt[] result = ipsProject.findAllProductCmpts(type, true);
        assertEquals(1, result.length);

        // Now create a component without product component type.
        newProductCmpt(ipsProject, "ProductCmpt2");
        result = ipsProject.findAllProductCmpts(type, true);
        assertEquals(1, result.length);
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
        setMinRequiredVersion("0.0.0");
        TestIpsFeatureVersionManager manager = new TestIpsFeatureVersionManager();
        manager.setCurrentVersionCompatibleWith(false);
        manager.setCompareToCurrentVersion(-1);
        IpsPlugin.getDefault().setFeatureVersionManagers(new IIpsFeatureVersionManager[] { manager });
        ml = ipsProject.validate();
        assertNull(ml.getMessageByCode(IIpsProject.MSGCODE_NO_VERSIONMANAGER));
        assertNull(ml.getMessageByCode(IIpsProject.MSGCODE_VERSION_TOO_LOW));
        assertNotNull(ml.getMessageByCode(IIpsProject.MSGCODE_INCOMPATIBLE_VERSIONS));

        setMinRequiredVersion("999999.0.0");
        manager.setCompareToCurrentVersion(1);
        ml = ipsProject.validate();
        assertNotNull(ml.getMessageByCode(IIpsProject.MSGCODE_VERSION_TOO_LOW));
    }

    public void testValidate_JavaCodeContainsError() throws CoreException {
        MessageList list = ipsProject.validate();
        assertFalse(list.containsErrorMsg());

        // remove src folder => build path error
        IFolder srcFolder = ipsProject.getProject().getFolder("src");
        srcFolder.delete(true, null);
        list = ipsProject.validate();
        assertNotNull(list.getMessageByCode(IIpsProject.MSGCODE_JAVA_PROJECT_HAS_BUILDPATH_ERRORS));
    }

    public void testIsJavaProjectErrorFree_OnlyThisProject() throws CoreException {
        assertNull(ipsProject.isJavaProjectErrorFree(false));
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        assertNotNull(ipsProject.isJavaProjectErrorFree(false));
        assertTrue(ipsProject.isJavaProjectErrorFree(false).booleanValue());

        // delete the source folder => inconsistent class path
        IFolder srcFolder = ipsProject.getProject().getFolder("src");
        srcFolder.delete(true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

        assertFalse(ipsProject.isJavaProjectErrorFree(false).booleanValue());

        // recreate source folder
        srcFolder.create(true, true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        assertTrue(ipsProject.isJavaProjectErrorFree(false).booleanValue());

        // create Java sourcefile with compile error
        IFile srcFile = srcFolder.getFile("Bla.java");
        srcFile.create(new ByteArrayInputStream("wrong code".getBytes()), true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        assertFalse(ipsProject.isJavaProjectErrorFree(false).booleanValue());

        // change Java Sourcefile to contain warnings
        String code = "import java.lang.String; public class Bla { }";
        srcFile.setContents(new ByteArrayInputStream(code.getBytes()), true, false, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        assertTrue(ipsProject.isJavaProjectErrorFree(false).booleanValue());

        // create Java sourcefile with compile error
        srcFile.delete(true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        assertTrue(ipsProject.isJavaProjectErrorFree(false).booleanValue());

        // project closed
        ipsProject.getProject().close(null);
        assertNull(ipsProject.isJavaProjectErrorFree(false));

        // project does not exist
        IIpsProject project2 = IpsPlugin.getDefault().getIpsModel().getIpsProject("Project2");
        assertNull(project2.isJavaProjectErrorFree(false));
    }

    public void testIsJavaProjectErrorFree_WithRefToOtherProjects() throws CoreException {
        IIpsProject ipsProject2 = newIpsProject("Project2");
        IIpsProject ipsProject3 = newIpsProject("Project3");
        IJavaProject javaProject1 = ipsProject.getJavaProject();
        IJavaProject javaProject2 = ipsProject2.getJavaProject();
        IJavaProject javaProject3 = ipsProject3.getJavaProject();

        IClasspathEntry refEntry = JavaCore.newProjectEntry(new Path("/Project2"));
        addClasspathEntry(javaProject1, refEntry);

        refEntry = JavaCore.newProjectEntry(new Path("/Project3"));
        addClasspathEntry(javaProject2, refEntry);

        assertNull(ipsProject3.isJavaProjectErrorFree(true));
        assertNull(ipsProject2.isJavaProjectErrorFree(true));
        assertNull(ipsProject.isJavaProjectErrorFree(true));

        // delete the source folder => inconsistent class path
        IFolder srcFolder = javaProject3.getProject().getFolder("src");
        srcFolder.delete(true, null);
        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

        assertFalse(ipsProject3.isJavaProjectErrorFree(true).booleanValue());
        assertFalse(ipsProject2.isJavaProjectErrorFree(true).booleanValue());
        assertFalse(ipsProject.isJavaProjectErrorFree(true).booleanValue());
    }

    private void makeIpsProjectDependOnBaseProject() throws CoreException {
        IIpsProjectProperties props = ipsProject.getProperties();
        IIpsObjectPath path = props.getIpsObjectPath();
        path.newIpsProjectRefEntry(baseProject);
        ipsProject.setProperties(props);
    }

    public void testGetJavaProject() {
        IJavaProject javaProject = ipsProject.getJavaProject();
        assertNotNull(javaProject);
        assertEquals(ipsProject.getProject(), javaProject.getProject());
    }

    public void testGetClassLoaderForJavaProject() throws CoreException {
        ClassLoader cl = ipsProject.getClassLoaderForJavaProject();
        assertNotNull(cl);
    }

    public void testIsReferencedBy() throws CoreException {
        assertFalse(baseProject.isReferencedBy(null, true));
        assertFalse(baseProject.isReferencedBy(baseProject, true));
        assertFalse(baseProject.isReferencedBy(ipsProject, true));

        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(baseProject);
        ipsProject.setIpsObjectPath(path);
        assertTrue(baseProject.isReferencedBy(ipsProject, true));
        assertTrue(baseProject.isReferencedBy(ipsProject, false));

        IIpsProject ipsProject2 = newIpsProject("IpsProject2");
        path = ipsProject2.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject);
        ipsProject2.setIpsObjectPath(path);
        assertTrue(baseProject.isReferencedBy(ipsProject, false));
        assertFalse(baseProject.isReferencedBy(ipsProject2, false));
        assertTrue(baseProject.isReferencedBy(ipsProject, true));
        assertTrue(baseProject.isReferencedBy(ipsProject2, true));
    }

    public void testGetReferencingProjects() throws CoreException {
        assertEquals(0, baseProject.findReferencingProjects(true).length);

        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(baseProject);
        ipsProject.setIpsObjectPath(path);

        assertEquals(1, baseProject.findReferencingProjects(true).length);
        assertEquals(ipsProject, baseProject.findReferencingProjects(true)[0]);

        IIpsProject ipsProject2 = newIpsProject("IpsProject2");
        path = ipsProject2.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject);
        ipsProject2.setIpsObjectPath(path);

        assertEquals(1, baseProject.findReferencingProjects(false).length);
        assertEquals(ipsProject, baseProject.findReferencingProjects(false)[0]);

        assertEquals(2, baseProject.findReferencingProjects(true).length);
        List<IIpsProject> list = Arrays.asList(baseProject.findReferencingProjects(true));
        assertTrue(list.contains(ipsProject));
        assertTrue(list.contains(ipsProject2));
    }

    public void testGetReferencingProjectsLeavesOrSelf() throws CoreException {
        assertEquals(1, baseProject.findReferencingProjectLeavesOrSelf().length);
        assertEquals(baseProject, baseProject.findReferencingProjectLeavesOrSelf()[0]);

        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(baseProject);
        ipsProject.setIpsObjectPath(path);

        assertEquals(1, baseProject.findReferencingProjectLeavesOrSelf().length);
        assertEquals(ipsProject, baseProject.findReferencingProjectLeavesOrSelf()[0]);

        IIpsProject ipsProject2 = newIpsProject("IpsProject2");
        path = ipsProject2.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject);
        ipsProject2.setIpsObjectPath(path);

        assertEquals(1, baseProject.findReferencingProjectLeavesOrSelf().length);
        assertEquals(ipsProject2, baseProject.findReferencingProjectLeavesOrSelf()[0]);

        IIpsProject ipsProject3 = newIpsProject("IpsProject3");
        path = ipsProject3.getIpsObjectPath();
        path.newIpsProjectRefEntry(baseProject);
        ipsProject3.setIpsObjectPath(path);

        IIpsProject notIn = newIpsProject("notIn");

        assertEquals(2, baseProject.findReferencingProjectLeavesOrSelf().length);
        List<IIpsProject> list = Arrays.asList(baseProject.findReferencingProjectLeavesOrSelf());
        assertTrue(list.contains(ipsProject2));
        assertTrue(list.contains(ipsProject3));

        path = ipsProject3.getIpsObjectPath();
        path.newIpsProjectRefEntry(notIn);
        ipsProject3.setIpsObjectPath(path);

        assertEquals(2, baseProject.findReferencingProjectLeavesOrSelf().length);
        list = Arrays.asList(baseProject.findReferencingProjectLeavesOrSelf());
        assertTrue(list.contains(ipsProject2));
        assertTrue(list.contains(ipsProject3));

        path = ipsProject2.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject3);
        ipsProject2.setIpsObjectPath(path);

        assertEquals(1, baseProject.findReferencingProjectLeavesOrSelf().length);
        assertEquals(ipsProject2, baseProject.findReferencingProjectLeavesOrSelf()[0]);

        path = ipsProject2.getIpsObjectPath();
        path.removeProjectRefEntry(ipsProject3);
        ipsProject2.setIpsObjectPath(path);

        IIpsProject lastProject = newIpsProject("lastProject");
        path = lastProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject2);
        path.newIpsProjectRefEntry(ipsProject3);
        lastProject.setIpsObjectPath(path);

        assertEquals(1, baseProject.findReferencingProjectLeavesOrSelf().length);
        assertEquals(lastProject, baseProject.findReferencingProjectLeavesOrSelf()[0]);
    }

    public void testIsAccessibleViaIpsObjectPath() throws CoreException {
        assertFalse(ipsProject.isAccessibleViaIpsObjectPath(null));

        IIpsObject obj1 = newPolicyCmptType(baseProject, "Object");
        assertTrue(baseProject.isAccessibleViaIpsObjectPath(obj1));
        assertFalse(ipsProject.isAccessibleViaIpsObjectPath(obj1));

        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(baseProject);
        ipsProject.setIpsObjectPath(path);
        assertTrue(ipsProject.isAccessibleViaIpsObjectPath(obj1));

        // create a second object with the same qualified name that shadows the first object
        IIpsObject obj2 = newPolicyCmptType(ipsProject, "Object");
        assertTrue(ipsProject.isAccessibleViaIpsObjectPath(obj2));
        assertFalse(ipsProject.isAccessibleViaIpsObjectPath(obj1));
    }

    public void testValidate() throws Exception {
        Thread.sleep(500);
        // if the validate is called too fast, the .ipsproject-file is not written to disk and so
        // can't be parsed!
        MessageList list = ipsProject.validate();
        assertTrue(list.isEmpty());
    }

    public void testValidate_MissingPropertyFile() throws CoreException {
        IFile file = ipsProject.getIpsProjectPropertiesFile();
        file.delete(true, false, null);
        MessageList list = ipsProject.validate();
        assertNotNull(list.getMessageByCode(IIpsProject.MSGCODE_MISSING_PROPERTY_FILE));
        assertEquals(1, list.size());
    }

    public void testValidate_UnparsablePropertyFile() throws CoreException {
        IFile file = ipsProject.getIpsProjectPropertiesFile();
        InputStream unparsableContents = new ByteArrayInputStream("blabla".getBytes());
        file.setContents(unparsableContents, true, false, null);
        suppressLoggingDuringExecutionOfThisTestCase();
        MessageList list = ipsProject.validate();
        assertNotNull(list.getMessageByCode(IIpsProject.MSGCODE_UNPARSABLE_PROPERTY_FILE));
        assertEquals(1, list.size());
    }

    public void testGetProperties() {
        assertNotNull(ipsProject.getProperties());
    }

    /**
     * This has once been a bug. If setProperties() only saves the properties to the file without
     * updating it in memory, an access method might return an old value, if it is called before the
     * resource change listener has removed the old prop file from the cache in the model.
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
        assertTrue(propFile.getModificationStamp() != stamp);
        assertEquals("myBuilder", ipsProject.getProperties().getBuilderSetId());

        // test if a copy was created during set
        props.setBuilderSetId("newBuilder");
        assertEquals("myBuilder", ipsProject.getProperties().getBuilderSetId());
    }

    public void testFindDatatype() throws CoreException {
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setPredefinedDatatypesUsed(new String[] { Datatype.DECIMAL.getQualifiedName(),
                Datatype.PRIMITIVE_INT.getQualifiedName() });
        JavaClass2DatatypeAdaptor messageListDatatype = new JavaClass2DatatypeAdaptor("MessageList", "org.MessageList");
        props.addDefinedDatatype(messageListDatatype);
        ipsProject.setProperties(props);

        IPolicyCmptType pcType = newPolicyCmptType(ipsProject, "Policy");
        pcType.getIpsSrcFile().save(true, null);
        assertEquals(pcType, ipsProject.findDatatype("Policy"));

        assertEquals(Datatype.VOID, ipsProject.findDatatype("void"));

        assertEquals(Datatype.DECIMAL, ipsProject.findDatatype("Decimal"));
        assertEquals(messageListDatatype, ipsProject.findDatatype("MessageList"));

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
        assertNull(ipsProject.findValueDatatype(null));

        IIpsProjectProperties props = ipsProject.getProperties();
        props.setPredefinedDatatypesUsed(new String[] { Datatype.DECIMAL.getQualifiedName(),
                Datatype.PRIMITIVE_INT.getQualifiedName() });
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

    public void testFindValueDatatypeWithEnumTypes() throws Exception {
        IEnumType paymentMode = newEnumType(ipsProject, "PaymentMode");
        paymentMode.setAbstract(false);
        paymentMode.setContainingValues(true);
        paymentMode.newEnumLiteralNameAttribute();
        IEnumAttribute id = paymentMode.newEnumAttribute();
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setInherited(false);
        id.setName("id");

        IEnumType gender = newEnumType(ipsProject, "Gender");
        gender.setAbstract(false);
        gender.setContainingValues(true);
        gender.newEnumLiteralNameAttribute();
        id = gender.newEnumAttribute();
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setInherited(false);
        id.setName("id");

        ValueDatatype datatype = ipsProject.findValueDatatype("PaymentMode");
        assertEquals(new EnumTypeDatatypeAdapter(paymentMode, null), datatype);
        datatype = ipsProject.findValueDatatype("Gender");
        assertEquals(new EnumTypeDatatypeAdapter(gender, null), datatype);
    }

    public void testfindEnumTypes() throws Exception {
        IEnumType paymentMode = newEnumType(ipsProject, "PaymentMode");
        paymentMode.setAbstract(true);
        paymentMode.setContainingValues(false);
        IEnumAttribute id = paymentMode.newEnumAttribute();
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setInherited(false);
        id.setName("id");

        IEnumType gender = newEnumType(ipsProject, "Gender");
        gender.setAbstract(false);
        gender.setContainingValues(true);
        gender.newEnumLiteralNameAttribute();
        id = gender.newEnumAttribute();
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setInherited(false);
        id.setName("id");

        List<IEnumType> enumTypes = ipsProject.findEnumTypes(true, true);
        assertEquals(2, enumTypes.size());
        assertTrue(enumTypes.contains(paymentMode));
        assertTrue(enumTypes.contains(gender));

        enumTypes = ipsProject.findEnumTypes(false, true);
        assertEquals(1, enumTypes.size());
        assertTrue(enumTypes.contains(gender));

        enumTypes = ipsProject.findEnumTypes(true, false);
        assertEquals(1, enumTypes.size());
        assertTrue(enumTypes.contains(gender));

        enumTypes = ipsProject.findEnumTypes(false, false);
        assertEquals(1, enumTypes.size());
        assertTrue(enumTypes.contains(gender));
    }

    public void testGetDatatypeHelper() throws Exception {
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setPredefinedDatatypesUsed(new String[] { Datatype.DECIMAL.getQualifiedName() });
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

    public void testFindDatatypeString() throws Exception {
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setPredefinedDatatypesUsed(new String[] { Datatype.DECIMAL.getQualifiedName() });
        ipsProject.setProperties(props);
        Datatype type = ipsProject.findDatatype(Datatype.DECIMAL.getQualifiedName());
        assertNotNull(type);
        assertEquals(type, Datatype.DECIMAL);
        type = ipsProject.findDatatype(Datatype.DECIMAL.getQualifiedName() + "[]");
        assertTrue(type instanceof ArrayOfValueDatatype);
        ArrayOfValueDatatype arrayType = (ArrayOfValueDatatype)type;
        assertEquals(Datatype.DECIMAL, arrayType.getBasicDatatype());
    }

    public void testFindDatatypes() throws Exception {
        IIpsProjectProperties props = ipsProject.getProperties();

        props.setPredefinedDatatypesUsed(new String[] { Datatype.DECIMAL.getQualifiedName() });
        Datatype messageListDatatype = new JavaClass2DatatypeAdaptor("org.MessageList");
        props.addDefinedDatatype(messageListDatatype);
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
        assertEquals(3, types.length);
        assertEquals(Datatype.DECIMAL, types[0]);
        assertEquals(messageListDatatype, types[1]);
        assertEquals(pcType1, types[2]);

        // setup dependency to other project, these datatypes of the refenreced project must also be
        // included.
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
        assertEquals(6, types.length);
        assertEquals(Datatype.DECIMAL, types[0]);
        assertEquals(messageListDatatype, types[1]);
        assertEquals(Datatype.MONEY, types[2]);
        assertEquals(TestEnumType.class.getName(), types[3].getJavaClassName());
        assertEquals(pcType1, types[4]);
        assertEquals(pcType2, types[5]);

        // all types, void included
        types = ipsProject.findDatatypes(false, true);
        assertEquals(7, types.length);
        assertEquals(Datatype.VOID, types[0]);
        assertEquals(Datatype.DECIMAL, types[1]);
        assertEquals(messageListDatatype, types[2]);
        assertEquals(Datatype.MONEY, types[3]);
        assertEquals(TestEnumType.class.getName(), types[4].getJavaClassName());
        assertEquals(pcType1, types[5]);
        assertEquals(pcType2, types[6]);
    }

    public void testFindDatatypes3Parameters() throws CoreException {
        IPolicyCmptType a = newPolicyAndProductCmptType(ipsProject, "a", "aConfig");
        List<Datatype> datatypes = Arrays.asList(ipsProject.findDatatypes(false, false, false));
        assertTrue(datatypes.contains(a));
        IProductCmptType aConfig = a.findProductCmptType(ipsProject);
        assertTrue(datatypes.contains(aConfig));
    }

    public void testFindDatatypes4Parameters() throws CoreException {
        List<Datatype> disallowedTypesTest = new ArrayList<Datatype>(1);
        disallowedTypesTest.add(Datatype.STRING);
        Datatype[] types = ipsProject.findDatatypes(false, false, false, disallowedTypesTest);
        assertFalse(Arrays.asList(types).contains(Datatype.STRING));
    }

    public void testFindDatatypes5Parameters() throws CoreException {
        IEnumType testEnumType = newEnumType(ipsProject, "TestEnumType");
        testEnumType.setAbstract(true);
        Datatype[] types = ipsProject.findDatatypes(false, false, false, null, false);
        assertFalse(Arrays.asList(types).contains(testEnumType));

        testEnumType.setAbstract(false);
        types = ipsProject.findDatatypes(false, false, false, null, true);
        assertTrue(Arrays.asList(types).contains(new EnumTypeDatatypeAdapter(testEnumType, null)));
    }

    public void testFindDatatypesOfEnumType() throws Exception {
        IEnumType paymentMode = newEnumType(ipsProject, "PaymentMode");
        paymentMode.setAbstract(false);
        paymentMode.setContainingValues(true);
        paymentMode.newEnumLiteralNameAttribute();
        IEnumAttribute id = paymentMode.newEnumAttribute();
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setInherited(false);
        id.setName("id");

        IEnumType gender = newEnumType(ipsProject, "Gender");
        gender.setAbstract(false);
        gender.setContainingValues(true);
        gender.newEnumLiteralNameAttribute();
        id = gender.newEnumAttribute();
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setInherited(false);
        id.setName("id");

        Datatype[] datatypes = ipsProject.findDatatypes(true, false, false, null);
        List<Datatype> datatypeList = Arrays.asList(datatypes);
        assertTrue(datatypeList.contains(new EnumTypeDatatypeAdapter(paymentMode, null)));
        assertTrue(datatypeList.contains(new EnumTypeDatatypeAdapter(gender, null)));

        datatypes = ipsProject.findDatatypes(false, false, false, null);
        datatypeList = Arrays.asList(datatypes);
        assertTrue(datatypeList.contains(new EnumTypeDatatypeAdapter(paymentMode, null)));
        assertTrue(datatypeList.contains(new EnumTypeDatatypeAdapter(gender, null)));
    }

    /**
     * Creates an ips project called RefProject that is referenced by the ips project and has 2
     * predefined datatypes and one dynamic datatype.
     */
    private IIpsProject createRefProject() throws Exception {
        IIpsProject refProject = newIpsProject("RefProject");
        IIpsProjectProperties props = refProject.getProperties();
        props.setPredefinedDatatypesUsed(new String[] { Datatype.DECIMAL.getQualifiedName(),
                Datatype.MONEY.getQualifiedName() });
        refProject.setProperties(props);

        newDefinedEnumDatatype(refProject, new Class[] { TestEnumType.class });
        // set the reference from the ips project to the referenced project
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(refProject);
        ipsProject.setIpsObjectPath(path);
        return refProject;
    }

    public void testFindIpsSrcFile() throws CoreException, InterruptedException {
        IPolicyCmptType testObject = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.Test");
        QualifiedNameType qnt = new QualifiedNameType("a.b.Test", IpsObjectType.POLICY_CMPT_TYPE);
        IIpsSrcFile file = ipsProject.findIpsSrcFile(qnt);
        assertNotNull(file);
        assertEquals(testObject, file.getIpsObject());

        // search again should return same instance as src file handles are cached
        assertSame(file, ipsProject.findIpsSrcFile(qnt));

        // if we change the ips project properties, the cache must be cleared and a new file
        // instance returned.
        Thread.sleep(500); // sleep some time to make sure the .ipsproject file has definitly a new
        // modification stamp
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IFolder newFolder = ipsProject.getProject().getFolder("newFolder");
        newFolder.create(true, false, null);
        path.newSourceFolderEntry(newFolder);
        ipsProject.setIpsObjectPath(path);

        assertNotSame(file, ipsProject.findIpsSrcFile(qnt));
        assertNotNull(ipsProject.findIpsSrcFile(qnt));

        // negative tests
        assertNull(ipsProject.findIpsSrcFile(new QualifiedNameType("a.c.Test", IpsObjectType.POLICY_CMPT_TYPE)));
        assertNull(ipsProject.findIpsSrcFile(new QualifiedNameType("a.b.Unknown", IpsObjectType.POLICY_CMPT_TYPE)));

        // invalid package name as it contains a blank => should return null
        assertNull(ipsProject.findIpsSrcFile(new QualifiedNameType("a b.Test", IpsObjectType.POLICY_CMPT_TYPE)));
    }

    public void testFindIpsSrcFile_WithTwoSrcFolderRoots() throws CoreException, InterruptedException {
        IPolicyCmptType testObject = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.Test");
        QualifiedNameType qnt = new QualifiedNameType("a.b.Test", IpsObjectType.POLICY_CMPT_TYPE);
        IIpsSrcFile file = ipsProject.findIpsSrcFile(qnt);
        assertNotNull(file);
        assertEquals(testObject, file.getIpsObject());

        // search again should return same instance as src file handles are cached
        assertSame(file, ipsProject.findIpsSrcFile(qnt));

        // if we change the ips project properties, the cache must be cleared and a new file
        // instance returned.
        Thread.sleep(500); // sleep some time to make sure the .ipsproject file has definitly a new
        // modification stamp
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IFolder newFolder = ipsProject.getProject().getFolder("newFolder");
        newFolder.create(true, false, null);
        IIpsSrcFolderEntry entry = path.newSourceFolderEntry(newFolder);
        ipsProject.setIpsObjectPath(path);

        // test if we also find files on the second entry
        IPolicyCmptType newType = newPolicyCmptType(entry.getIpsPackageFragmentRoot(), "NewPolicy");
        assertEquals(newType.getIpsSrcFile(), ipsProject.findIpsSrcFile(newType.getQualifiedNameType()));

        // test if caching works also for the second entry
        assertEquals(newType.getIpsSrcFile(), ipsProject.findIpsSrcFile(newType.getQualifiedNameType()));
    }

    public void testFindIpsSrcFile_InArchive() throws Exception {
        IIpsProject archiveProject = newIpsProject("ArchiveProject");
        IPolicyCmptType type = newPolicyCmptType(archiveProject, "motor.Policy");
        newPolicyCmptTypeWithoutProductCmptType(archiveProject, "motor.collision.CollisionCoverage");
        newProductCmpt(archiveProject, "motor.MotorProduct");

        IFile archiveFile = ipsProject.getProject().getFile("test.ipsar");
        createArchive(archiveProject, archiveFile);

        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newArchiveEntry(archiveFile.getFullPath());
        ipsProject.setIpsObjectPath(path);

        IIpsSrcFile foundFile = ipsProject.findIpsSrcFile(type.getQualifiedNameType());
        assertEquals(type.getQualifiedNameType(), foundFile.getQualifiedNameType());
    }

    public void testFindIpsSrcFile_InReferencedProject() throws Exception {
        // setup the projects. ipsproject -> baseproject -> project3
        IIpsProject project3 = newIpsProject("Project3");

        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(baseProject);
        ipsProject.setIpsObjectPath(path);

        path = baseProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(project3);
        baseProject.setIpsObjectPath(path);

        IPolicyCmptType basePolicy = newPolicyCmptType(baseProject, "BasePolicy");
        IProductCmptType policy3 = newProductCmptType(project3, "Policy3");
        IPolicyCmptType policy = newPolicyCmptType(ipsProject, "Policy");

        assertEquals(policy, ipsProject.findIpsSrcFile(policy.getQualifiedNameType()).getIpsObject());
        assertEquals(basePolicy, ipsProject.findIpsSrcFile(basePolicy.getQualifiedNameType()).getIpsObject());
        assertEquals(policy3, ipsProject.findIpsSrcFile(policy3.getQualifiedNameType()).getIpsObject());

        assertNull(ipsProject.findIpsSrcFile(new QualifiedNameType("unkown", IpsObjectType.POLICY_CMPT_TYPE)));
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

    @SuppressWarnings("deprecation")
    // ok to supredd, as the method under test is deprecated
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

        IIpsObject ipsObj = ipsProject.findIpsObject(pct.getQualifiedNameType());
        assertEquals(pct, ipsObj);
    }

    /**
     * Test if the findProductCmpts method works with all kind of package fragments root
     */
    public void testFindProductCmptsWithIpsArchive() throws Exception {
        IFile archiveFile = ipsProject.getProject().getFile("test.ipsar");
        createArchive(ipsProject, archiveFile);
        new IpsArchive(ipsProject, archiveFile.getProjectRelativePath());

        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newArchiveEntry(archiveFile.getLocation());
        ipsProject.setIpsObjectPath(path);

        ipsProject.findAllProductCmpts(null, true);
    }

    public IProductCmptType createProductCmptType(IIpsPackageFragment packageFragment,
            IPolicyCmptType policyCmptType,
            String name) throws CoreException {
        IProductCmptType productCmptType = (IProductCmptType)packageFragment.createIpsFile(
                IpsObjectType.PRODUCT_CMPT_TYPE, name, true, null).getIpsObject();
        productCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());
        policyCmptType.setProductCmptType(productCmptType.getQualifiedName());
        return productCmptType;
    }

    public void testFindAllProductCmptSrcFiles() throws CoreException {
        // create the following types: Type0, Type1 and Type2
        IIpsPackageFragment pack = root.createPackageFragment("pack", true, null);
        IPolicyCmptType policyCmptType0 = (IPolicyCmptType)pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Type0",
                true, null).getIpsObject();
        policyCmptType0.setConfigurableByProductCmptType(true);
        createProductCmptType(pack, policyCmptType0, "ProductCmptType0");

        pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Type1", true, null);
        IPolicyCmptType policyCmptType2 = (IPolicyCmptType)pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Type2",
                true, null).getIpsObject();
        IProductCmptType productCmptType2 = (IProductCmptType)pack.createIpsFile(IpsObjectType.PRODUCT_CMPT_TYPE,
                "ProductCmptType2", true, null).getIpsObject();
        productCmptType2.setPolicyCmptType(policyCmptType2.getQualifiedName());
        policyCmptType2.setProductCmptType(productCmptType2.getQualifiedName());

        // create the following product compnent: product0, product1, product2
        IIpsSrcFile productFile0 = pack.createIpsFile(IpsObjectType.PRODUCT_CMPT, "Product0", true, null);
        IIpsSrcFile productFile1 = pack.createIpsFile(IpsObjectType.PRODUCT_CMPT, "Product1", true, null);
        IIpsSrcFile productFile2 = pack.createIpsFile(IpsObjectType.PRODUCT_CMPT, "Product2", true, null);

        IProductCmpt product0 = (IProductCmpt)productFile0.getIpsObject();
        IProductCmpt product1 = (IProductCmpt)productFile1.getIpsObject();
        IProductCmpt product2 = (IProductCmpt)productFile2.getIpsObject();

        product0.setProductCmptType("pack.ProductCmptType0");
        product1.setProductCmptType("pack.ProductCmptType2");
        product2.setProductCmptType("pack.ProductCmptType0");

        product0.getIpsSrcFile().save(true, null);
        product1.getIpsSrcFile().save(true, null);
        product2.getIpsSrcFile().save(true, null);

        assertNotNull(product0.findProductCmptType(product0.getIpsProject()));
        IIpsSrcFile[] result = ipsProject.findAllProductCmptSrcFiles(product0.findProductCmptType(ipsProject), true);
        assertEquals(2, result.length);
        assertEquals(product0.getIpsSrcFile(), result[0]);
        assertEquals(product2.getIpsSrcFile(), result[1]);

        result = ipsProject.findAllProductCmptSrcFiles(null, true);
        assertEquals(3, result.length);
        assertEquals(product0.getIpsSrcFile(), result[0]);
        assertEquals(product1.getIpsSrcFile(), result[1]);
        assertEquals(product2.getIpsSrcFile(), result[2]);

        //
        // test search with different projects
        //
        IIpsProject ipsProject2 = newIpsProject("Project2");

        pack = ipsProject2.getIpsPackageFragmentRoots()[0].createPackageFragment("pack", true, null);
        IPolicyCmptType policyCmptType10 = (IPolicyCmptType)pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE,
                "Type10", true, null).getIpsObject();
        policyCmptType10.setConfigurableByProductCmptType(true);
        createProductCmptType(pack, policyCmptType10, "ProductCmptType10");

        IIpsSrcFile productFile10 = pack.createIpsFile(IpsObjectType.PRODUCT_CMPT, "Product10", true, null);
        IProductCmpt product10 = (IProductCmpt)productFile10.getIpsObject();
        product10.setProductCmptType("pack.ProductCmptType10");
        product10.getIpsSrcFile().save(true, null);

        assertNotNull(product10.findProductCmptType(product10.getIpsProject()));
        result = ipsProject.findAllProductCmptSrcFiles(product10.findProductCmptType(product10.getIpsProject()), true);
        assertEquals(0, result.length);

        IIpsObjectPath ipsObjectPath = ipsProject.getIpsObjectPath();
        ipsObjectPath.newIpsProjectRefEntry(ipsProject2);
        ipsProject.setIpsObjectPath(ipsObjectPath);

        result = ipsProject.findAllProductCmptSrcFiles(product10.findProductCmptType(product10.getIpsProject()), true);
        assertEquals(1, result.length);
        assertEquals(product10.getIpsSrcFile(), result[0]);

        result = ipsProject.findAllProductCmptSrcFiles(null, true);
        assertEquals(4, result.length);
        assertEquals(product0.getIpsSrcFile(), result[0]);
        assertEquals(product1.getIpsSrcFile(), result[1]);
        assertEquals(product2.getIpsSrcFile(), result[2]);
        assertEquals(product10.getIpsSrcFile(), result[3]);

        // Remark: the parameter includeSubtypes of method findAllProductCmpts will be tested in
        // IpsPackageFragmentRootTest;
    }

    public void testFindAllTestCaseSrcFiles() throws CoreException {
        // create the following testcase types: TestType0, TestType1

        IIpsPackageFragment pack = root.createPackageFragment("pack", true, null);

        @SuppressWarnings("unused")
        ITestCaseType testType0 = (ITestCaseType)pack.createIpsFile(IpsObjectType.TEST_CASE_TYPE, "TestType0", true,
                null).getIpsObject();
        @SuppressWarnings("unused")
        ITestCaseType testType1 = (ITestCaseType)pack.createIpsFile(IpsObjectType.TEST_CASE_TYPE, "TestType1", true,
                null).getIpsObject();

        // create the following testcases: test0, test1, test2
        IIpsSrcFile testFile0 = pack.createIpsFile(IpsObjectType.TEST_CASE, "test0", true, null);
        IIpsSrcFile testFile1 = pack.createIpsFile(IpsObjectType.TEST_CASE, "test1", true, null);
        IIpsSrcFile testFile2 = pack.createIpsFile(IpsObjectType.TEST_CASE, "test2", true, null);

        ITestCase test0 = (ITestCase)testFile0.getIpsObject();
        ITestCase test1 = (ITestCase)testFile1.getIpsObject();
        ITestCase test2 = (ITestCase)testFile2.getIpsObject();

        test0.setTestCaseType("pack.TestType0");
        test1.setTestCaseType("pack.TestType1");
        test2.setTestCaseType("pack.TestType0");

        test0.getIpsSrcFile().save(true, null);
        test1.getIpsSrcFile().save(true, null);
        test2.getIpsSrcFile().save(true, null);

        assertNotNull(test0.findTestCaseType((test0.getIpsProject())));
        IIpsSrcFile[] result = ipsProject.findAllTestCaseSrcFiles(test0.findTestCaseType(ipsProject));
        assertEquals(2, result.length);
        assertEquals(test0.getIpsSrcFile(), result[0]);
        assertEquals(test2.getIpsSrcFile(), result[1]);

        result = ipsProject.findAllTestCaseSrcFiles(null);
        assertEquals(3, result.length);

        //
        // test search with different projects
        //
        IIpsProject ipsProject2 = newIpsProject("Project2");

        pack = ipsProject2.getIpsPackageFragmentRoots()[0].createPackageFragment("pack", true, null);

        @SuppressWarnings("unused")
        ITestCaseType testTypeProj2 = (ITestCaseType)pack.createIpsFile(IpsObjectType.TEST_CASE_TYPE, "TestTypeProj2",
                true, null).getIpsObject();
        IIpsSrcFile testFileProj2 = pack.createIpsFile(IpsObjectType.TEST_CASE, "testProj2", true, null);
        ITestCase testProj2 = (ITestCase)testFileProj2.getIpsObject();
        testProj2.setTestCaseType("pack.TestTypeProj2");
        testProj2.getIpsSrcFile().save(true, null);

        assertNotNull(testProj2.findTestCaseType(testProj2.getIpsProject()));
        result = ipsProject.findAllTestCaseSrcFiles(testProj2.findTestCaseType(testProj2.getIpsProject()));
        assertEquals(0, result.length);

        IIpsObjectPath ipsObjectPath = ipsProject.getIpsObjectPath();
        ipsObjectPath.newIpsProjectRefEntry(ipsProject2);
        ipsProject.setIpsObjectPath(ipsObjectPath);

        result = ipsProject.findAllTestCaseSrcFiles(testProj2.findTestCaseType(testProj2.getIpsProject()));
        assertEquals(1, result.length);
        assertEquals(testProj2.getIpsSrcFile(), result[0]);
    }

    public void testFindAllTableContentsSrcFiles() throws CoreException {
        ITableStructure ts0 = newTableStructure(ipsProject, "structure0");
        ITableStructure ts1 = newTableStructure(ipsProject, "structure1");

        ITableContents tc0 = newTableContents(ts0, "contets0");
        @SuppressWarnings("unused")
        ITableContents tc1 = newTableContents(ts1, "contets1");
        ITableContents tc2 = newTableContents(ts0, "contets2");

        IIpsSrcFile[] result = ipsProject.findAllTableContentsSrcFiles(ts0);
        assertEquals(2, result.length);
        assertEquals(tc0.getIpsSrcFile(), result[0]);
        assertEquals(tc2.getIpsSrcFile(), result[1]);

        result = ipsProject.findAllTableContentsSrcFiles(null);
        assertEquals(3, result.length);

        //
        // test search with different projects
        //
        IIpsProject ipsProject2 = newIpsProject("Project2");

        ITableStructure ts2 = newTableStructure(ipsProject2, "structure2");
        ITableContents tcProj2 = newTableContents(ts2, "contetnsP2");

        result = ipsProject.findAllTableContentsSrcFiles(ts2);
        assertEquals(0, result.length);

        IIpsObjectPath ipsObjectPath = ipsProject.getIpsObjectPath();
        ipsObjectPath.newIpsProjectRefEntry(ipsProject2);
        ipsProject.setIpsObjectPath(ipsObjectPath);

        result = ipsProject.findAllTableContentsSrcFiles(ts2);
        assertEquals(1, result.length);
        assertEquals(tcProj2.getIpsSrcFile(), result[0]);
    }

    public void testFindAllEnumContentSrcFiles() throws CoreException {
        IIpsPackageFragment pack = root.createPackageFragment("pack", true, null);

        IEnumType enumType0 = (IEnumType)pack.createIpsFile(IpsObjectType.ENUM_TYPE, "EnumType0", true, null)
                .getIpsObject();
        @SuppressWarnings("unused")
        IEnumType enumType1 = (IEnumType)pack.createIpsFile(IpsObjectType.ENUM_TYPE, "EnumType1", true, null)
                .getIpsObject();
        IEnumType enumType2 = (IEnumType)pack.createIpsFile(IpsObjectType.ENUM_TYPE, "EnumType2", true, null)
                .getIpsObject();
        enumType0.setSuperEnumType("pack.EnumType2");

        IIpsSrcFile enumFile0 = pack.createIpsFile(IpsObjectType.ENUM_CONTENT, "enum0", true, null);
        IIpsSrcFile enumFile1 = pack.createIpsFile(IpsObjectType.ENUM_CONTENT, "enum1", true, null);
        IIpsSrcFile enumFile2 = pack.createIpsFile(IpsObjectType.ENUM_CONTENT, "enum2", true, null);

        IEnumContent enum0 = (IEnumContent)enumFile0.getIpsObject();
        IEnumContent enum1 = (IEnumContent)enumFile1.getIpsObject();
        IEnumContent enum2 = (IEnumContent)enumFile2.getIpsObject();

        enum0.setEnumType("pack.EnumType0");
        enum1.setEnumType("pack.EnumType1");
        enum2.setEnumType("pack.EnumType0");

        enum0.getIpsSrcFile().save(true, null);
        enum1.getIpsSrcFile().save(true, null);
        enum2.getIpsSrcFile().save(true, null);

        assertNotNull(enum0.findEnumType((enum0.getIpsProject())));
        IIpsSrcFile[] result = ipsProject.findAllEnumContentSrcFiles(enum0.findEnumType(ipsProject), true);
        assertEquals(2, result.length);
        assertEquals(enum0.getIpsSrcFile(), result[0]);
        assertEquals(enum2.getIpsSrcFile(), result[1]);

        result = ipsProject.findAllEnumContentSrcFiles(enumType2, true);
        assertEquals(2, result.length);
        assertEquals(enum0.getIpsSrcFile(), result[0]);
        assertEquals(enum2.getIpsSrcFile(), result[1]);

        result = ipsProject.findAllEnumContentSrcFiles(enumType2, false);
        assertEquals(0, result.length);

        result = ipsProject.findAllEnumContentSrcFiles(null, true);
        assertEquals(3, result.length);

        //
        // enum search with different projects
        //
        IIpsProject ipsProject2 = newIpsProject("Project2");

        pack = ipsProject2.getIpsPackageFragmentRoots()[0].createPackageFragment("pack", true, null);

        @SuppressWarnings("unused")
        IEnumType enumTypeProj2 = (IEnumType)pack.createIpsFile(IpsObjectType.ENUM_TYPE, "EnumTypeProj2", true, null)
                .getIpsObject();
        IIpsSrcFile enumFileProj2 = pack.createIpsFile(IpsObjectType.ENUM_CONTENT, "enumProj2", true, null);
        IEnumContent enumProj2 = (IEnumContent)enumFileProj2.getIpsObject();
        enumProj2.setEnumType("pack.EnumTypeProj2");
        enumProj2.getIpsSrcFile().save(true, null);

        assertNotNull(enumProj2.findEnumType(enumProj2.getIpsProject()));
        result = ipsProject.findAllEnumContentSrcFiles(enumProj2.findEnumType(enumProj2.getIpsProject()), true);
        assertEquals(0, result.length);

        IIpsObjectPath ipsObjectPath = ipsProject.getIpsObjectPath();
        ipsObjectPath.newIpsProjectRefEntry(ipsProject2);
        ipsProject.setIpsObjectPath(ipsObjectPath);

        result = ipsProject.findAllEnumContentSrcFiles(enumProj2.findEnumType(enumProj2.getIpsProject()), true);
        assertEquals(1, result.length);
        assertEquals(enumProj2.getIpsSrcFile(), result[0]);
    }

    public void testFindIpsSrcFiles() throws CoreException {
        // create the following types: Type0, a.b.Type1 and c.Type2
        IIpsPackageFragment pack = root.getIpsPackageFragment("");
        IPolicyCmptType type0 = (IPolicyCmptType)pack
                .createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Type0", true, null).getIpsObject();
        IIpsPackageFragment folderAB = root.createPackageFragment("a.b", true, null);
        folderAB.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Type1", true, null);
        IIpsPackageFragment folderC = root.createPackageFragment("c", true, null);
        folderC.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Type2", true, null);

        IProductCmptType productCmptType0 = (IProductCmptType)pack.createIpsFile(IpsObjectType.PRODUCT_CMPT_TYPE,
                "ProductCmptType0", true, null).getIpsObject();
        productCmptType0.setPolicyCmptType(type0.getQualifiedName());
        type0.setProductCmptType(productCmptType0.getQualifiedName());
        type0.setConfigurableByProductCmptType(true);

        // create table c.Table1
        folderC.createIpsFile(IpsObjectType.TABLE_STRUCTURE, "Table1", true, null);

        IIpsSrcFile[] result = ipsProject.findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT);
        assertEquals(0, result.length);

        result = ipsProject.findIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);
        assertEquals(3, result.length);

        IIpsSrcFile pctSrcFile = result[1];
        IPolicyCmptType pct = (IPolicyCmptType)pctSrcFile.getIpsObject();

        result = ipsProject.findIpsSrcFiles(IpsObjectType.TABLE_STRUCTURE);
        assertEquals(1, result.length);

        result = ipsProject.findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE);
        assertEquals(1, result.length);

        result = ipsProject.findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE);
        assertEquals(1, result.length);

        IIpsObject ipsObj = ipsProject.findIpsObject(pct.getQualifiedNameType());
        assertEquals(pct, ipsObj);
    }

    public void testFindIpsSrcFiles_InReferencedProject() throws Exception {
        // setup the projects. ipsproject -> baseproject and project3, baseproject-> project3
        // => project 3 is referenced twice! => make sure objects are only found once!
        IIpsProject project3 = newIpsProject("Project3");

        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(baseProject);
        path.newIpsProjectRefEntry(project3);
        ipsProject.setIpsObjectPath(path);

        path = baseProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(project3);
        baseProject.setIpsObjectPath(path);

        IPolicyCmptType basePolicy = newPolicyAndProductCmptType(baseProject, "BasePolicy", "BaseProduct");
        IPolicyCmptType policy3 = newPolicyAndProductCmptType(project3, "Policy3", "Product3");
        IPolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");

        IIpsSrcFile[] files = ipsProject.findIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);
        assertEquals(3, files.length);
        assertEquals(policy, files[0].getIpsObject());
        assertEquals(basePolicy, files[1].getIpsObject());
        assertEquals(policy3, files[2].getIpsObject());

        files = ipsProject.findIpsSrcFiles(IpsObjectType.TABLE_STRUCTURE);
        assertEquals(0, files.length);
    }

    public void testFindIpsSrcFiles_InArchive() throws Exception {
        IIpsProject archiveProject = newIpsProject("ArchiveProject");
        IPolicyCmptType policy = newPolicyCmptType(archiveProject, "motor.Policy");
        IPolicyCmptType coverage = newPolicyCmptTypeWithoutProductCmptType(archiveProject,
                "motor.collision.CollisionCoverage");
        newProductCmpt(archiveProject, "motor.MotorProduct");

        IFile archiveFile = ipsProject.getProject().getFile("test.ipsar");
        createArchive(archiveProject, archiveFile);

        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newArchiveEntry(archiveFile.getLocation());
        ipsProject.setIpsObjectPath(path);
        IPolicyCmptType basePolicy = newPolicyCmptType(ipsProject, "basePolicy");

        IIpsSrcFile[] files = ipsProject.findIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);
        assertEquals(3, files.length);
        assertEquals(basePolicy, files[0].getIpsObject());
        assertEquals(policy.getQualifiedNameType(), files[1].getQualifiedNameType());
        assertEquals(coverage.getQualifiedNameType(), files[2].getQualifiedNameType());

        files = ipsProject.findIpsSrcFiles(IpsObjectType.TABLE_STRUCTURE);
        assertEquals(0, files.length);
    }

    public void testSetIpsObjectPath() throws CoreException {
        IFile projectFile = ipsProject.getIpsProjectPropertiesFile();
        long stamp = projectFile.getModificationStamp();
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.setOutputDefinedPerSrcFolder(false);
        path.setBasePackageNameForMergableJavaClasses("some.name");
        ipsProject.setIpsObjectPath(path);
        assertTrue(stamp != projectFile.getModificationStamp());

        // following line will receive a new IpsProject instance (as it is only a proxy)
        IIpsProject ipsProject2 = IpsPlugin.getDefault().getIpsModel().getIpsProject(ipsProject.getProject());
        // test if he changed object path is also available with the new instance
        assertEquals("some.name", ipsProject2.getIpsObjectPath().getBasePackageNameForMergableJavaClasses());
    }

    public void testFindReferencingProductCmptGenerations() throws CoreException {
        IIpsPackageFragmentRoot[] roots = ipsProject.getIpsPackageFragmentRoots();
        assertEquals(roots.length, 1);

        IIpsPackageFragment pack = roots[0].getIpsPackageFragment(IIpsPackageFragment.NAME_OF_THE_DEFAULT_PACKAGE);
        IProductCmpt tobereferenced = (IProductCmpt)this.newIpsObject(pack, IpsObjectType.PRODUCT_CMPT,
                "tobereferenced");
        IProductCmpt noref = (IProductCmpt)this.newIpsObject(pack, IpsObjectType.PRODUCT_CMPT, "noref");
        IProductCmpt ref1 = (IProductCmpt)this.newIpsObject(pack, IpsObjectType.PRODUCT_CMPT, "ref1");

        IProductCmptGeneration gen1 = (IProductCmptGeneration)ref1.newGeneration();
        IProductCmptGeneration genNoref = (IProductCmptGeneration)noref.newGeneration();
        IProductCmptGeneration genTobereferenced = (IProductCmptGeneration)tobereferenced.newGeneration();

        IProductCmptGeneration[] result = ipsProject.findReferencingProductCmptGenerations(tobereferenced
                .getQualifiedNameType());
        assertEquals(0, result.length);

        GregorianCalendar cal = new GregorianCalendar(2005, 1, 1);
        gen1.setValidFrom(cal);
        genNoref.setValidFrom(cal);
        genTobereferenced.setValidFrom(cal);
        gen1.newLink("xxx").setTarget(tobereferenced.getQualifiedName());
        IpsPlugin.getDefault().getIpsPreferences().setWorkingDate(cal);

        result = ipsProject.findReferencingProductCmptGenerations(tobereferenced.getQualifiedNameType());
        assertEquals(1, result.length);
        assertEquals(gen1, result[0]);

        IProductCmptGeneration gen2 = (IProductCmptGeneration)ref1.newGeneration();
        gen2.setValidFrom(cal);
        gen2.newLink("xxx").setTarget(tobereferenced.getQualifiedName());
        IProductCmptGeneration[] generations = ipsProject.findReferencingProductCmptGenerations(tobereferenced
                .getQualifiedNameType());

        List<IProductCmptGeneration> resultList = Arrays.asList(generations);
        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(gen1));
        assertTrue(resultList.contains(gen2));
    }

    public void testFindReferencingPolicyCmptTypes() throws CoreException {
        IPolicyCmptType pcTypeReferenced = newPolicyCmptType(root, "tobereferenced");
        IPolicyCmptType pcType = newPolicyCmptType(root, "TestPCType");
        IPolicyCmptTypeAssociation relation = pcType.newPolicyCmptTypeAssociation();
        relation.setTarget(pcTypeReferenced.getQualifiedName());

        IPolicyCmptType pcType2 = newPolicyCmptType(root, "TestPCType2");
        IPolicyCmptTypeAssociation relation2 = pcType2.newPolicyCmptTypeAssociation();
        relation2.setTarget(pcTypeReferenced.getQualifiedName());

        IPolicyCmptType pcType3 = newPolicyCmptType(root, "TestPCType3");
        IPolicyCmptTypeAssociation relation3 = pcType3.newPolicyCmptTypeAssociation();
        relation3.setTarget(pcTypeReferenced.getQualifiedName());

        IPolicyCmptType pcTypeNoRef = newPolicyCmptType(root, "TestPCTypeNoRef");

        IPolicyCmptType pcTypeSuper = newPolicyCmptType(root, "TestPCTypeSuper");
        pcTypeReferenced.setSupertype("TestPCTypeSuper");

        IPolicyCmptType[] results = ipsProject.findReferencingPolicyCmptTypes(pcTypeReferenced);
        assertEquals(4, results.length);

        HashSet<IPolicyCmptType> resultSet = new HashSet<IPolicyCmptType>();
        resultSet.add(results[0]);
        resultSet.add(results[1]);
        resultSet.add(results[2]);
        resultSet.add(results[3]);
        HashSet<IPolicyCmptType> expectedSet = new HashSet<IPolicyCmptType>();
        expectedSet.add(pcType);
        expectedSet.add(pcType2);
        expectedSet.add(pcType3);
        expectedSet.add(pcTypeSuper);

        assertEquals(expectedSet, resultSet);
        assertFalse(resultSet.contains(pcTypeNoRef));
        assertFalse(resultSet.contains(null));

        // test references with faulty supertype
        pcTypeReferenced.setSupertype("incorrectQualifiedName");

        results = ipsProject.findReferencingPolicyCmptTypes(pcTypeReferenced);
        assertEquals(3, results.length);

        resultSet = new HashSet<IPolicyCmptType>();
        resultSet.add(results[0]);
        resultSet.add(results[1]);
        resultSet.add(results[2]);
        expectedSet = new HashSet<IPolicyCmptType>();
        expectedSet.add(pcType);
        expectedSet.add(pcType2);
        expectedSet.add(pcType3);

        assertEquals(expectedSet, resultSet);
        assertFalse(resultSet.contains(pcTypeNoRef));
        assertFalse(resultSet.contains(null));
    }

    public void testFindEnumDatatypes() throws Exception {
        newDefinedEnumDatatype(ipsProject, new Class[] { TestEnumType.class });
        EnumDatatype[] dataTypes = ipsProject.findEnumDatatypes();
        assertEquals(1, dataTypes.length);
        assertEquals("TestEnumType", dataTypes[0].getQualifiedName());

        IEnumType paymentMode = newEnumType(ipsProject, "PaymentMode");
        paymentMode.setAbstract(false);
        paymentMode.setContainingValues(true);
        paymentMode.newEnumLiteralNameAttribute();
        IEnumAttribute id = paymentMode.newEnumAttribute();
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setInherited(false);
        id.setName("id");

        IEnumType gender = newEnumType(ipsProject, "Gender");
        gender.setAbstract(false);
        gender.setContainingValues(true);
        gender.newEnumLiteralNameAttribute();
        id = gender.newEnumAttribute();
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setInherited(false);
        id.setName("id");

        dataTypes = ipsProject.findEnumDatatypes();
        List<EnumDatatype> dataTypeList = Arrays.asList(dataTypes);
        assertTrue(dataTypeList.contains(new EnumTypeDatatypeAdapter(paymentMode, null)));
        assertTrue(dataTypeList.contains(new EnumTypeDatatypeAdapter(gender, null)));
        assertEquals(3, dataTypes.length);

    }

    public void testGetNonIpsResources() throws CoreException {
        IProject projectHandle = ipsProject.getProject();
        IFolder nonIpsRoot = projectHandle.getFolder("nonIpsRoot");
        nonIpsRoot.create(true, false, null);
        IFile nonIpsFile = projectHandle.getFile("nonIpsFile");
        nonIpsFile.create(null, true, null);

        IFolder classpathFolder = projectHandle.getFolder("classpathFolder");
        classpathFolder.create(true, false, null);
        IFolder outputFolder = projectHandle.getFolder("outputFolder");
        outputFolder.create(true, false, null);
        IFile classpathFile = projectHandle.getFile("classpathFile");
        classpathFile.create(null, true, null);

        // add classpathFolder and classpathFile to the javaprojects classpath
        // add outputFolder as apecific outputlocation of classpathFolder
        IJavaProject javaProject = ipsProject.getJavaProject();
        IClasspathEntry[] cpEntries = javaProject.getRawClasspath();
        IClasspathEntry[] newEntries = new IClasspathEntry[2];
        newEntries[0] = JavaCore.newSourceEntry(classpathFolder.getFullPath());
        newEntries[1] = JavaCore
                .newSourceEntry(classpathFile.getFullPath(), new IPath[] {}, outputFolder.getFullPath());
        IClasspathEntry[] result = new IClasspathEntry[cpEntries.length + newEntries.length];
        System.arraycopy(cpEntries, 0, result, 0, cpEntries.length);
        System.arraycopy(newEntries, 0, result, cpEntries.length, newEntries.length);
        ipsProject.getJavaProject().setRawClasspath(result, null);

        Object[] nonIpsResources = ipsProject.getNonIpsResources();
        List<?> list = Arrays.asList(nonIpsResources);
        assertTrue(list.contains(nonIpsRoot));
        assertTrue(list.contains(nonIpsFile));
        // /bin, /src and /extension are outputfolders or classpath entries and thus filtered out
        assertTrue(list.contains(projectHandle.getFile(".project")));
        assertTrue(list.contains(projectHandle.getFile(".ipsproject")));
        assertTrue(list.contains(projectHandle.getFile(".classpath")));
        // assert number of resources returned
        int expectedNumOfResources = 5;
        // @see EclipsePreferences.DEFAULT_PREFERENCES_DIRNAME
        if (list.contains(projectHandle.getFolder(".settings"))) {
            expectedNumOfResources++;
        }
        assertEquals(expectedNumOfResources, nonIpsResources.length);

        assertFalse(list.contains(classpathFolder));
        assertFalse(list.contains(classpathFile));
        assertFalse(list.contains(outputFolder));
    }

    public void testDependsOn() throws CoreException {
        assertFalse(ipsProject.isReferencing(baseProject));

        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(baseProject);
        ipsProject.setIpsObjectPath(path);
        assertTrue(ipsProject.isReferencing(baseProject));

        // transitivitaet der beziehung beruecksichtigt?
        IIpsProject project3 = newIpsProject("Project3");
        path = project3.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject);
        project3.setIpsObjectPath(path);
        assertTrue(project3.isReferencing(ipsProject));
        assertTrue(project3.isReferencing(baseProject));
    }

    public void testValidateMissingMigration() throws Exception {
        MessageList ml = ipsProject.validate();
        assertNull(ml.getMessageByCode(IIpsProject.MSGCODE_INVALID_MIGRATION_INFORMATION));

        setMinRequiredVersion("0.0.3");
        IpsPlugin.getDefault().setFeatureVersionManagers(
                new IIpsFeatureVersionManager[] { new InvalidMigrationMockManager() });
        suppressLoggingDuringExecutionOfThisTestCase();
        ml = ipsProject.validate();
        assertNotNull(ml.getMessageByCode(IIpsProject.MSGCODE_INVALID_MIGRATION_INFORMATION));
    }

    public void testValidateIfOutputFolderSetForSrcFolderEntry() throws CoreException {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IIpsSrcFolderEntry srcFolder = path.newSourceFolderEntry((IFolder)root.getEnclosingResource());
        srcFolder.setSpecificBasePackageNameForDerivedJavaClasses("srctest");
        path.setBasePackageNameForMergableJavaClasses("test");
        ipsProject.setIpsObjectPath(path);

        MessageList msgList = ipsProject.validate();
        Message msg = msgList.getMessageByCode(IIpsSrcFolderEntry.MSGCODE_OUTPUT_FOLDER_DERIVED_MISSING);
        assertNotNull(msg);
        msg = msgList.getMessageByCode(IIpsSrcFolderEntry.MSGCODE_OUTPUT_FOLDER_MERGABLE_MISSING);
        assertNotNull(msg);

        IFolder outMerge = ipsProject.getProject().getFolder("src");
        if (!outMerge.exists()) {
            outMerge.create(true, true, null);
        }
        path.setOutputFolderForMergableSources(outMerge);
        IFolder outDerived = ipsProject.getProject().getFolder("derived");
        if (!outDerived.exists()) {
            outDerived.create(true, true, null);
        }
        path.setOutputFolderForDerivedSources(outDerived);
        path.setOutputDefinedPerSrcFolder(false);
        ipsProject.setIpsObjectPath(path);

        msgList = ipsProject.validate();
        msg = msgList.getMessageByCode(IIpsSrcFolderEntry.MSGCODE_OUTPUT_FOLDER_DERIVED_MISSING);
        assertNull(msg);
        msg = msgList.getMessageByCode(IIpsSrcFolderEntry.MSGCODE_OUTPUT_FOLDER_MERGABLE_MISSING);
        assertNull(msg);
    }

    public void testValidateDuplicateTocFilesInDifferentProjects() throws Exception {
        // check if the validation doesn't fail for a valid non duplicate toc file path
        MessageList ml = ipsProject.validate();
        assertNull(ml.getMessageByCode(IIpsProject.MSGCODE_DUPLICATE_TOC_FILE_PATH_IN_DIFFERENT_PROJECTS));

        // create builder set so that this test case is independent from StandardBuilderSet which is
        // in a different
        // plugin
        IIpsArtefactBuilderSet projectABuilderSet = new DefaultBuilderSet() {

            @Override
            protected IIpsArtefactBuilder[] createBuilders() throws CoreException {
                return new IIpsArtefactBuilder[0];
            }

        };
        projectABuilderSet.setId("projectABuilderSet");
        projectABuilderSet.setIpsProject(ipsProject);

        IIpsProjectProperties props = ipsProject.getProperties();
        props.setBuilderSetId("projectABuilderSet");

        // the project needs to be a product definition project to force a fail of the validation
        props.setProductDefinitionProject(true);
        ipsProject.setProperties(props);
        getIpsModel().setIpsArtefactBuilderSetInfos(
                new IIpsArtefactBuilderSetInfo[] { new TestArtefactBuilderSetInfo(projectABuilderSet) });

        IIpsObjectPath projectAIpsObjectPath = ipsProject.getIpsObjectPathInternal();

        // to have not to care about each ipsobject path entry the properties are set on the path
        projectAIpsObjectPath.setOutputDefinedPerSrcFolder(false);
        // the DefaultBuilderSet uses this package name to determine the toc file name e.g. path
        projectAIpsObjectPath.setBasePackageNameForDerivedJavaClasses("org.faktorzehn.de");

        IFolder outputFolderDerived = ipsProject.getProject().getFolder("derived");
        if (!outputFolderDerived.exists()) {
            outputFolderDerived.create(true, true, null);
        }
        projectAIpsObjectPath.setOutputFolderForDerivedSources(outputFolderDerived);

        IFolder outputFolderMergeable = ipsProject.getProject().getFolder("src");
        if (!outputFolderMergeable.exists()) {
            outputFolderMergeable.create(true, true, null);
        }
        projectAIpsObjectPath.setOutputFolderForDerivedSources(outputFolderMergeable);

        // second ipsproject with its own builderset but the same setting for the toc file name
        IpsProject ipsProjectB = (IpsProject)newIpsProject("TestProjectB");

        IIpsArtefactBuilderSet projectBBuilderSet = new DefaultBuilderSet() {

            @Override
            protected IIpsArtefactBuilder[] createBuilders() throws CoreException {
                return new IIpsArtefactBuilder[0];
            }

        };
        projectBBuilderSet.setId("projectBBuilderSet");
        projectBBuilderSet.setIpsProject(ipsProjectB);

        IIpsProjectProperties projectBProperties = ipsProjectB.getProperties();
        projectBProperties.setBuilderSetId("projectBBuilderSet");

        // the project needs to be a product definition project to force a fail of the validation
        projectBProperties.setProductDefinitionProject(true);
        ipsProjectB.setProperties(projectBProperties);
        getIpsModel().setIpsArtefactBuilderSetInfos(
                new IIpsArtefactBuilderSetInfo[] { new TestArtefactBuilderSetInfo(projectBBuilderSet),
                        new TestArtefactBuilderSetInfo(projectABuilderSet) });

        // etablish the dependency so that projectB is dependent from projectA
        IpsObjectPath projectBIpsObjectPath = ipsProjectB.getIpsObjectPathInternal();
        ArrayList<IIpsObjectPathEntry> projectBIpsObjectPathEntries = new ArrayList<IIpsObjectPathEntry>(
                Arrays.asList(projectBIpsObjectPath.getEntries()));
        projectBIpsObjectPathEntries.add(new IpsProjectRefEntry(projectBIpsObjectPath, ipsProject));

        projectBIpsObjectPath.setEntries(projectBIpsObjectPathEntries
                .toArray(new IIpsObjectPathEntry[projectBIpsObjectPathEntries.size()]));
        projectBIpsObjectPath.setOutputDefinedPerSrcFolder(false);
        projectBIpsObjectPath.setBasePackageNameForDerivedJavaClasses("org.faktorzehn.de");

        outputFolderDerived = ipsProjectB.getProject().getFolder("derived");
        if (!outputFolderDerived.exists()) {
            outputFolderDerived.create(true, true, null);
        }
        projectBIpsObjectPath.setOutputFolderForDerivedSources(outputFolderDerived);

        outputFolderMergeable = ipsProjectB.getProject().getFolder("src");
        if (!outputFolderMergeable.exists()) {
            outputFolderMergeable.create(true, true, null);
        }
        projectBIpsObjectPath.setOutputFolderForMergableSources(outputFolderMergeable);

        // for projectB the validation is expected to fail
        MessageList msgList = ipsProjectB.validate();
        assertNotNull(msgList.getMessageByCode(IIpsProject.MSGCODE_DUPLICATE_TOC_FILE_PATH_IN_DIFFERENT_PROJECTS));

        // for projectA the validation is expected not to fail since A doesn't depend on B
        msgList = ipsProject.validate();
        assertNull(msgList.getMessageByCode(IIpsProject.MSGCODE_DUPLICATE_TOC_FILE_PATH_IN_DIFFERENT_PROJECTS));
    }

    public void testValidateIpsObjectPathCycle() throws CoreException {
        IIpsProject ipsProject2 = this.newIpsProject("TestProject2");
        IIpsObjectPath path = ipsProject2.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject);
        ipsProject2.setIpsObjectPath(path);

        MessageList ml = ipsProject.validate();
        assertNull(ml.getMessageByCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));

        path = ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject2);
        ipsProject.setIpsObjectPath(path);

        List<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        ipsProject.findAllIpsSrcFiles(result);
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

        result = new ArrayList<IIpsSrcFile>();
        ipsProject.findAllIpsSrcFiles(result);
        ipsProject.findIpsObject(new QualifiedNameType("xyz", IpsObjectType.PRODUCT_CMPT));
        // there is an cycle in the ref projects,
        // if we get no stack overflow exception, then the test was successfully executed

        IIpsProject ipsProject10 = this.newIpsProject("TestProject10");
        IIpsProject ipsProject11 = this.newIpsProject("TestProject11");
        IIpsProject ipsProject12 = this.newIpsProject("TestProject12");
        IIpsProject ipsProject13 = this.newIpsProject("TestProject13");

        // test cycle in 4 projects
        path = ipsProject10.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject11);
        ipsProject10.setIpsObjectPath(path);

        path = ipsProject10.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject12);
        ipsProject10.setIpsObjectPath(path);

        path = ipsProject11.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject13);
        path.newIpsProjectRefEntry(ipsProject11); // invalid reference, should not result in a
        // stack overflow exception
        ipsProject11.setIpsObjectPath(path);

        path = ipsProject12.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject13);
        ipsProject12.setIpsObjectPath(path);

        result = new ArrayList<IIpsSrcFile>();
        ipsProject.findAllIpsSrcFiles(result);

        ml = ipsProject10.validate();
        assertNull(ml.getMessageByCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));

        path = ipsProject13.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject10);
        ipsProject13.setIpsObjectPath(path);

        ml = ipsProject10.validate();
        assertNotNull(ml.getMessageByCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));

        ml = ipsProject11.validate();
        assertNotNull(ml.getMessageByCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));
    }

    private void setMinRequiredVersion(String version) throws CoreException {
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setMinRequiredVersionNumber("org.faktorips.feature", version);
        ipsProject.setProperties(props);
    }

    public void testGetNamingConventions() throws CoreException {
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
        assertEquals(1, ml.size());
        assertNotNull(ml.getMessageByCode(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION));

        cmpt2.setRuntimeId("Hugo");
        ml = prj.checkForDuplicateRuntimeIds();
        assertEquals(0, ml.size());
        assertNull(ml.getMessageByCode(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION));

        // test that not linked projects are not checked against each other
        IProductCmpt cmpt3 = newProductCmpt(ipsProject, "product3");
        cmpt3.setRuntimeId("Egon");
        cmpt2.setRuntimeId("Egon");
        ml = prj.checkForDuplicateRuntimeIds();
        assertEquals(1, ml.size());
        assertNotNull(ml.getMessageByCode(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION));

        // test that linked projects will be checked against each other
        IIpsObjectPath objectPath = prj.getIpsObjectPath();
        objectPath.newIpsProjectRefEntry(ipsProject);
        prj.setIpsObjectPath(objectPath);
        ml = prj.checkForDuplicateRuntimeIds();
        assertEquals(ml.toString(), 3, ml.size());
        assertNotNull(ml.getMessageByCode(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION));

        ml = prj.checkForDuplicateRuntimeIds(new IIpsSrcFile[] { cmpt3.getIpsSrcFile() });
        assertEquals(2, ml.size());
        assertNotNull(ml.getMessageByCode(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION));

        ml = prj.checkForDuplicateRuntimeIds(new IIpsSrcFile[] { cmpt1.getIpsSrcFile(), cmpt3.getIpsSrcFile() });
        assertEquals(4, ml.size());
        assertNotNull(ml.getMessageByCode(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION));
    }

    @SuppressWarnings("deprecation")
    public void testFindAllIpsObjects() throws CoreException {
        IIpsObject a = newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0], IpsObjectType.POLICY_CMPT_TYPE, "a");
        IIpsObject b = newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0], IpsObjectType.PRODUCT_CMPT, "b");
        IIpsObject c = newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0], IpsObjectType.TABLE_STRUCTURE, "c");
        IIpsObject d = newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0], IpsObjectType.TABLE_CONTENTS, "d");
        IIpsObject e = newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0], IpsObjectType.BUSINESS_FUNCTION, "e");
        IIpsObject f = newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0], IpsObjectType.TEST_CASE, "f");
        IIpsObject g = newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0], IpsObjectType.TEST_CASE_TYPE, "g");
        IIpsObject h = newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0], IpsObjectType.PRODUCT_CMPT_TYPE, "h");

        List<IIpsObject> result = new ArrayList<IIpsObject>(7);
        ipsProject.findAllIpsObjects(result);

        assertTrue(result.contains(a));
        assertTrue(result.contains(b));
        assertTrue(result.contains(c));
        assertTrue(result.contains(d));
        assertTrue(result.contains(e));
        assertTrue(result.contains(f));
        assertTrue(result.contains(g));
        assertTrue(result.contains(h));
    }

    public void testFindTableContents() throws CoreException {
        ITableStructure structure = (ITableStructure)newIpsObject(ipsProject, IpsObjectType.TABLE_STRUCTURE,
                "EnumTable");
        ITableContents contents1 = (ITableContents)newIpsObject(ipsProject, IpsObjectType.TABLE_CONTENTS, "PaymentMode");
        contents1.setTableStructure(structure.getQualifiedName());
        ITableContents contents2 = (ITableContents)newIpsObject(ipsProject, IpsObjectType.TABLE_CONTENTS, "Gender");
        contents2.setTableStructure(structure.getQualifiedName());

        ArrayList<ITableContents> tableContents = new ArrayList<ITableContents>();
        ipsProject.findTableContents(structure, tableContents);
        assertEquals(2, tableContents.size());
        assertTrue(tableContents.contains(contents1));
        assertTrue(tableContents.contains(contents2));
    }

    public void testIsResourceExcludedFromProductDefinition() throws CoreException {
        assertFalse(ipsProject.isResourceExcludedFromProductDefinition(null));

        IFolder folder1 = ipsProject.getProject().getFolder("exludedFolderWithFile");
        IFile file = ipsProject.getProject().getFile("exludedFolderWithFile/build.xml");
        IFolder folder2 = ipsProject.getProject().getFolder("exludedFolder");

        folder1.create(true, true, null);
        file.create(new ByteArrayInputStream("test".getBytes()), true, null);
        folder2.create(true, true, null);

        assertFalse(ipsProject.isResourceExcludedFromProductDefinition(folder2));
        assertFalse(ipsProject.isResourceExcludedFromProductDefinition(file));
        assertFalse(ipsProject.isResourceExcludedFromProductDefinition(folder1));

        IpsProjectProperties props = (IpsProjectProperties)ipsProject.getProperties();
        props.addResourcesPathExcludedFromTheProductDefiniton("exludedFolderWithFile/build.xml");
        props.addResourcesPathExcludedFromTheProductDefiniton("exludedFolder");
        ipsProject.setProperties(props);

        assertTrue(ipsProject.isResourceExcludedFromProductDefinition(folder2));
        assertTrue(ipsProject.isResourceExcludedFromProductDefinition(file));
        assertFalse(ipsProject.isResourceExcludedFromProductDefinition(folder1));
    }

    public void testFindIpsSourceFiles() throws CoreException {
        // create the following types: Type0, a.b.Type1 and c.Type2, and table structure

        IIpsSrcFile type0 = root.getIpsPackageFragment("").createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Type0", true,
                null);
        IIpsSrcFile productCmptType0 = root.getIpsPackageFragment("").createIpsFile(IpsObjectType.PRODUCT_CMPT_TYPE,
                "ProductCmptType0", true, null);
        ((IPolicyCmptType)type0.getIpsObject()).setProductCmptType(productCmptType0.getQualifiedNameType().getName());
        ((IPolicyCmptType)type0.getIpsObject()).setConfigurableByProductCmptType(true);
        ((IProductCmptType)productCmptType0.getIpsObject()).setPolicyCmptType(type0.getQualifiedNameType().getName());

        IIpsPackageFragment folderAB = root.createPackageFragment("a.b", true, null);
        folderAB.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Type1", true, null);
        IIpsPackageFragment folderC = root.createPackageFragment("c", true, null);
        IIpsSrcFile policyCmptType = folderC.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Type2", true, null);

        IIpsSrcFile tableStructure = folderC.createIpsFile(IpsObjectType.TABLE_STRUCTURE, "Table1", true, null);
        IIpsSrcFile tableContents = folderC.createIpsFile(IpsObjectType.TABLE_CONTENTS, "TableContents1", true, null);
        IIpsSrcFile testCaseType = folderC.createIpsFile(IpsObjectType.TEST_CASE_TYPE, "TestCaseType1", true, null);
        IIpsSrcFile testCase = folderC.createIpsFile(IpsObjectType.TEST_CASE, "TestCase1", true, null);
        IIpsSrcFile productCmpt = folderC.createIpsFile(IpsObjectType.PRODUCT_CMPT, "ProductCmpt1", true, null);

        IIpsSrcFile[] result = null;

        result = ipsProject.findIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);
        assertEquals(3, result.length);
        assertTrue(containsIpsSrcFile(result, policyCmptType));

        result = ipsProject.findIpsSrcFiles(IpsObjectType.TABLE_STRUCTURE);
        assertEquals(1, result.length);
        assertTrue(containsIpsSrcFile(result, tableStructure));

        result = ipsProject.findIpsSrcFiles(IpsObjectType.TABLE_CONTENTS);
        assertEquals(1, result.length);
        assertTrue(containsIpsSrcFile(result, tableContents));

        result = ipsProject.findIpsSrcFiles(IpsObjectType.TEST_CASE_TYPE);
        assertEquals(1, result.length);
        assertTrue(containsIpsSrcFile(result, testCaseType));

        result = ipsProject.findIpsSrcFiles(IpsObjectType.TEST_CASE);
        assertEquals(1, result.length);
        assertTrue(containsIpsSrcFile(result, testCase));

        result = ipsProject.findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT);
        assertEquals(1, result.length);
        assertTrue(containsIpsSrcFile(result, productCmpt));

        result = ipsProject.findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE);
        assertEquals(1, result.length);
        assertTrue(containsIpsSrcFile(result, productCmptType0));

        List<IIpsSrcFile> resultList = new ArrayList<IIpsSrcFile>();
        ipsProject.findAllIpsSrcFiles(resultList);
        assertEquals(9, resultList.size());

    }

    public void testFindAllIpsSrcFilesByPackageFragment() throws Exception {
        IPolicyCmptType a = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.c.A");
        IPolicyCmptType b = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.c.B");
        IPolicyCmptType c = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.c.C");

        IPolicyCmptType a2 = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.d.A");
        IPolicyCmptType b2 = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.d.B");
        IPolicyCmptType c2 = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.d.C");

        IEnumType eA = newEnumType(ipsProject, "a.b.c.EA");
        IEnumType eB = newEnumType(ipsProject, "a.b.c.EB");
        IEnumType eC = newEnumType(ipsProject, "a.b.c.EC");

        IEnumType eA2 = newEnumType(ipsProject, "a.b.d.EA");
        IEnumType eB2 = newEnumType(ipsProject, "a.b.d.EB");
        IEnumType eC2 = newEnumType(ipsProject, "a.b.d.EC");

        ArrayList<IIpsSrcFile> ipsSrcFiles = new ArrayList<IIpsSrcFile>();
        ipsProject.findAllIpsSrcFiles(ipsSrcFiles, IpsObjectType.POLICY_CMPT_TYPE, "a.b.c");

        assertEquals(3, ipsSrcFiles.size());
        assertTrue(ipsSrcFiles.contains(a.getIpsSrcFile()));
        assertTrue(ipsSrcFiles.contains(b.getIpsSrcFile()));
        assertTrue(ipsSrcFiles.contains(c.getIpsSrcFile()));

        ipsSrcFiles = new ArrayList<IIpsSrcFile>();
        ipsProject.findAllIpsSrcFiles(ipsSrcFiles, IpsObjectType.POLICY_CMPT_TYPE, "a.b.d");

        assertEquals(3, ipsSrcFiles.size());
        assertTrue(ipsSrcFiles.contains(a2.getIpsSrcFile()));
        assertTrue(ipsSrcFiles.contains(b2.getIpsSrcFile()));
        assertTrue(ipsSrcFiles.contains(c2.getIpsSrcFile()));

        ipsSrcFiles = new ArrayList<IIpsSrcFile>();
        ipsProject.findAllIpsSrcFiles(ipsSrcFiles, IpsObjectType.ENUM_TYPE, "a.b.c");

        assertEquals(3, ipsSrcFiles.size());
        assertTrue(ipsSrcFiles.contains(eA.getIpsSrcFile()));
        assertTrue(ipsSrcFiles.contains(eB.getIpsSrcFile()));
        assertTrue(ipsSrcFiles.contains(eC.getIpsSrcFile()));

        ipsSrcFiles = new ArrayList<IIpsSrcFile>();
        ipsProject.findAllIpsSrcFiles(ipsSrcFiles, IpsObjectType.ENUM_TYPE, "a.b.d");

        assertEquals(3, ipsSrcFiles.size());
        assertTrue(ipsSrcFiles.contains(eA2.getIpsSrcFile()));
        assertTrue(ipsSrcFiles.contains(eB2.getIpsSrcFile()));
        assertTrue(ipsSrcFiles.contains(eC2.getIpsSrcFile()));
    }

    public void testFindEnumContent() throws Exception {
        IEnumType eA = newEnumType(ipsProject, "a.b.c.EA");
        eA.setContainingValues(false);
        eA.setEnumContentName("a.b.c");
        eA.setEnumContentName("a.b.c.contentA");
        IEnumContent contentA = newEnumContent(eA, "a.b.c.contentA");
        IEnumContent result = ipsProject.findEnumContent(eA);
        assertEquals(contentA, result);
    }

    private boolean containsIpsSrcFile(IIpsSrcFile[] result, IIpsSrcFile policyCmptType) throws CoreException {
        for (IIpsSrcFile element : result) {
            if (element.getIpsObject().equals(policyCmptType.getIpsObject())) {
                return true;
            }
        }
        return false;
    }

    class InvalidMigrationMockManager extends TestIpsFeatureVersionManager {

        @Override
        public AbstractIpsProjectMigrationOperation[] getMigrationOperations(IIpsProject projectToMigrate)
                throws CoreException {
            throw new UnsupportedOperationException();
        }
    }

    public void testGetResourceAsStream() throws CoreException, IOException {
        IIpsPackageFragmentRoot rootOne = newIpsPackageFragmentRoot(ipsProject, null, "rootOne");
        createFileWithContent((IFolder)rootOne.getCorrespondingResource(), "file.txt", "111");
        assertEquals("111", getFileContent(ipsProject.getResourceAsStream("file.txt")));

        IIpsProject referencedIpsProject = newIpsProject("referencedIpsProject");
        IIpsPackageFragmentRoot rootTwo = newIpsPackageFragmentRoot(referencedIpsProject, null, "rootTwo");
        createFileWithContent((IFolder)rootTwo.getCorrespondingResource(), "anotherFile.txt", "222");
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(referencedIpsProject);
        ipsProject.setIpsObjectPath(path);

        // "anotherFile.txt" can be retrieved via the original ipsProject.
        assertEquals("222", getFileContent(ipsProject.getResourceAsStream("anotherFile.txt")));
    }
}
