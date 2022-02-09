/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import static org.faktorips.devtools.abstraction.mapping.PathMapping.toEclipsePath;
import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.faktorips.testsupport.IpsMatchers.lacksMessageCode;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.osgi.util.NLS;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestEnumType;
import org.faktorips.abstracttest.TestIpsFeatureVersionManager;
import org.faktorips.abstracttest.TestIpsModelExtensions;
import org.faktorips.abstracttest.builder.TestArtefactBuilderSetInfo;
import org.faktorips.abstracttest.builder.TestIpsArtefactBuilderSet;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.dthelpers.DecimalHelper;
import org.faktorips.datatype.ArrayOfValueDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.JavaClass2DatatypeAdaptor;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.eclipse.EclipseImplementation;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IVersionProvider;
import org.faktorips.devtools.model.IVersionProviderFactory;
import org.faktorips.devtools.model.builder.DefaultBuilderSet;
import org.faktorips.devtools.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.internal.DefaultVersionProvider;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.enums.EnumType;
import org.faktorips.devtools.model.internal.ipsproject.properties.IpsProjectProperties;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IBuilderKindId;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectNamingConventions;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IIpsProjectRefEntry;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.util.Tree;
import org.faktorips.devtools.model.util.Tree.Node;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IpsProjectTest extends AbstractIpsPluginTest {

    private static final String ROOT_NAME = "myRootName";

    private IpsProject ipsProject;

    private IpsProject baseProject;

    private IIpsPackageFragmentRoot root;

    @Mock
    private IIpsPackageFragmentRoot root2;

    @Mock
    private IIpsPackageFragmentRoot root3;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = (IpsProject)this.newIpsProject();
        root = ipsProject.getIpsPackageFragmentRoots()[0];

        baseProject = (IpsProject)this.newIpsProject();
        setPredefinedDatatypesUsed(baseProject, "Integer");
    }

    @Test
    public void testFindIpsPackageFragmentRoot() throws Exception {
        assertEquals(root, ipsProject.findIpsPackageFragmentRoot(root.getName()));
        assertNull(ipsProject.findIpsPackageFragmentRoot("Unknown"));
    }

    @Test
    public void testFindIpsPackageFragmentRoot_nonExistingProject() throws Exception {
        IIpsProject notExistingProject = IIpsModel.get().getIpsProject("NotExistingProject");

        assertFalse(notExistingProject.exists());
        assertNull(notExistingProject.findIpsPackageFragmentRoot("src"));
    }

    @Test
    public void testgetIpsPackageFragmentRoots_containerEntry() throws Exception {
        mockPathAndContainerEntry();

        IIpsPackageFragmentRoot[] ipsPackageFragmentRoots = ipsProject.getIpsPackageFragmentRoots();

        assertEquals(2, ipsPackageFragmentRoots.length);
        assertEquals(root2, ipsPackageFragmentRoots[0]);
        assertEquals(root3, ipsPackageFragmentRoots[1]);

        ipsPackageFragmentRoots = ipsProject.getIpsPackageFragmentRoots(true);

        assertEquals(2, ipsPackageFragmentRoots.length);
        assertEquals(root2, ipsPackageFragmentRoots[0]);
        assertEquals(root3, ipsPackageFragmentRoots[1]);
    }

    @Test
    public void testgetIpsPackageFragmentRoots_DoNotResolveContainerEntry() throws Exception {
        mockPathAndContainerEntry();

        IIpsPackageFragmentRoot[] ipsPackageFragmentRoots = ipsProject.getIpsPackageFragmentRoots(false);

        assertEquals(0, ipsPackageFragmentRoots.length);
    }

    @Test
    public void testFindIpsPackageFragmentRoot_containerEntry() throws Exception {
        mockPathAndContainerEntry();

        IIpsPackageFragmentRoot ipsPackageFragmentRoot = ipsProject.findIpsPackageFragmentRoot(ROOT_NAME);

        assertEquals(root2, ipsPackageFragmentRoot);
    }

    /**
     * Mocking an {@link IIpsPackageFragmentRoot} and introduce it as part of an
     * {@link IpsContainerEntry}. The {@link #ipsProject} is configured to reference this
     * {@link IpsContainerEntry} in its IPS object path.
     */
    private void mockPathAndContainerEntry() throws Exception {
        when(root2.getName()).thenReturn(ROOT_NAME);

        IpsObjectPath path = (IpsObjectPath)ipsProject.getIpsObjectPath();

        IpsContainerEntry containerEntry = spy(new IpsContainerEntry(path));
        path.setEntries(new IIpsObjectPathEntry[] { containerEntry });

        IIpsObjectPathEntry entry = mock(IIpsObjectPathEntry.class);
        when(entry.getIpsPackageFragmentRoot()).thenReturn(root2);
        IIpsObjectPathEntry entry2 = mock(IIpsObjectPathEntry.class);
        when(entry2.getIpsPackageFragmentRoot()).thenReturn(root3);

        List<IIpsObjectPathEntry> entries = Arrays.asList(entry, entry2);
        doReturn(entries).when(containerEntry).resolveEntries();

        ipsProject = spy(ipsProject);
        doReturn(path).when(ipsProject).getIpsObjectPathInternal();
    }

    @Test
    public void testGetValueSetTypes_Standard() {
        List<ValueSetType> types = ipsProject.getValueSetTypes(Datatype.STRING);
        assertEquals(4, types.size());
        assertTrue(types.contains(ValueSetType.DERIVED));
        assertTrue(types.contains(ValueSetType.UNRESTRICTED));
        assertTrue(types.contains(ValueSetType.ENUM));
        assertTrue(types.contains(ValueSetType.STRINGLENGTH));
    }

    @Test
    public void testGetValueSetTypes_Numeric() {
        List<ValueSetType> types = ipsProject.getValueSetTypes(Datatype.INTEGER);
        assertEquals(4, types.size());
        assertTrue(types.contains(ValueSetType.DERIVED));
        assertTrue(types.contains(ValueSetType.UNRESTRICTED));
        assertTrue(types.contains(ValueSetType.RANGE));
        assertTrue(types.contains(ValueSetType.ENUM));
    }

    @Test
    public void testGetValueSetTypes_Array() {
        List<ValueSetType> types = ipsProject.getValueSetTypes(new ArrayOfValueDatatype(Datatype.INTEGER, 2));
        assertEquals(2, types.size());
        assertTrue(types.contains(ValueSetType.DERIVED));
        assertTrue(types.contains(ValueSetType.UNRESTRICTED));
    }

    @Test
    public void testGetValueSetTypes_Null() {
        List<ValueSetType> types = ipsProject.getValueSetTypes(null);
        assertEquals(2, types.size());
        assertTrue(types.contains(ValueSetType.DERIVED));
        assertTrue(types.contains(ValueSetType.UNRESTRICTED));
    }

    @Test
    public void testIsValueSetTypeApplicable() {
        assertTrue(ipsProject.isValueSetTypeApplicable(Datatype.INTEGER, ValueSetType.ENUM));
        assertTrue(ipsProject.isValueSetTypeApplicable(Datatype.INTEGER, ValueSetType.RANGE));
        assertTrue(ipsProject.isValueSetTypeApplicable(Datatype.INTEGER, ValueSetType.UNRESTRICTED));
        assertTrue(ipsProject.isValueSetTypeApplicable(Datatype.INTEGER, ValueSetType.DERIVED));

        assertTrue(ipsProject.isValueSetTypeApplicable(Datatype.STRING, ValueSetType.ENUM));
        assertFalse(ipsProject.isValueSetTypeApplicable(Datatype.STRING, ValueSetType.RANGE));
        assertTrue(ipsProject.isValueSetTypeApplicable(Datatype.STRING, ValueSetType.UNRESTRICTED));
        assertTrue(ipsProject.isValueSetTypeApplicable(Datatype.STRING, ValueSetType.DERIVED));
    }

    @Test
    public void testFindProductCmptsByPolicyCmptWithExistingProductCmptMissingPolicyCmpt() {
        IProductCmptType type = newProductCmptType(ipsProject, "MotorProduct");
        newProductCmpt(type, "ProductCmpt1");
        IIpsSrcFile[] result = ipsProject.findAllProductCmptSrcFiles(type, true);
        assertEquals(1, result.length);

        // Now create a component without product component type.
        newProductCmpt(ipsProject, "ProductCmpt2");
        result = ipsProject.findAllProductCmptSrcFiles(type, true);
        assertEquals(1, result.length);
    }

    @Test
    public void testValidateRequiredFeatures() {
        MessageList ml = ipsProject.validate();
        assertThat(ml, lacksMessageCode(IIpsProject.MSGCODE_NO_VERSIONMANAGER));
        IIpsProjectProperties props = ipsProject.getProperties();
        IIpsProjectProperties propsOrig = new IpsProjectProperties(ipsProject, (IpsProjectProperties)props);
        props.setMinRequiredVersionNumber("unknown-feature", "1.0.0");
        ipsProject.setProperties(props);

        ml = ipsProject.validate();
        assertThat(ml, hasMessageCode(IIpsProject.MSGCODE_NO_VERSIONMANAGER));

        ipsProject.setProperties(propsOrig);
        setMinRequiredVersion("0.0.0");
        TestIpsFeatureVersionManager manager = new TestIpsFeatureVersionManager();
        manager.setCurrentVersionCompatibleWith(false);
        manager.setCompareToCurrentVersion(-1);
        try (TestIpsModelExtensions testIpsModelExtensions = TestIpsModelExtensions.get()) {
            testIpsModelExtensions.setFeatureVersionManagers(manager);
            ml = ipsProject.validate();
            assertThat(ml, lacksMessageCode(IIpsProject.MSGCODE_NO_VERSIONMANAGER));
            assertThat(ml, lacksMessageCode(IIpsProject.MSGCODE_VERSION_TOO_LOW));
            assertThat(ml, hasMessageCode(IIpsProject.MSGCODE_INCOMPATIBLE_VERSIONS));

            setMinRequiredVersion("999999.0.0");
            manager.setCompareToCurrentVersion(1);
            ml = ipsProject.validate();
            assertThat(ml, hasMessageCode(IIpsProject.MSGCODE_VERSION_TOO_LOW));
        }
    }

    @Category(EclipseImplementation.class)
    @Test
    public void testValidate_JavaCodeContainsError() {
        if (Abstractions.isEclipseRunning()) {
            MessageList list = ipsProject.validate();
            assertFalse(list.containsErrorMsg());

            // remove src folder => build path error
            AFolder srcFolder = ipsProject.getProject().getFolder("src");
            srcFolder.delete(null);
            list = ipsProject.validate();
            assertNotNull(list.getMessageByCode(IIpsProject.MSGCODE_JAVA_PROJECT_HAS_BUILDPATH_ERRORS));
        }
    }

    @Category(EclipseImplementation.class)
    @Test
    public void testIsJavaProjectErrorFree_OnlyThisProject() throws CoreException {
        if (Abstractions.isEclipseRunning()) {
            assertNull(ipsProject.isJavaProjectErrorFree(false));
            ipsProject.getProject().build(ABuildKind.FULL_BUILD, null);
            assertNotNull(ipsProject.isJavaProjectErrorFree(false));
            assertTrue(ipsProject.isJavaProjectErrorFree(false).booleanValue());

            // delete the source folder => inconsistent class path
            AFolder srcFolder = ipsProject.getProject().getFolder("src");
            srcFolder.delete(null);
            ipsProject.getProject().build(ABuildKind.FULL_BUILD, null);

            assertFalse(ipsProject.isJavaProjectErrorFree(false).booleanValue());

            // recreate source folder
            srcFolder.create(null);
            ipsProject.getProject().build(ABuildKind.FULL_BUILD, null);
            assertTrue(ipsProject.isJavaProjectErrorFree(false).booleanValue());

            // create Java sourcefile with compile error
            AFile srcFile = srcFolder.getFile("Bla.java");
            srcFile.create(new ByteArrayInputStream("wrong code".getBytes()), null);
            ipsProject.getProject().build(ABuildKind.FULL_BUILD, null);
            assertFalse(ipsProject.isJavaProjectErrorFree(false).booleanValue());

            // change Java Sourcefile to contain warnings
            String code = "import java.lang.String; public class Bla { }";
            srcFile.setContents(new ByteArrayInputStream(code.getBytes()), false, null);
            ipsProject.getProject().build(ABuildKind.FULL_BUILD, null);
            assertTrue(ipsProject.isJavaProjectErrorFree(false).booleanValue());

            // create Java sourcefile with compile error
            srcFile.delete(null);
            ipsProject.getProject().build(ABuildKind.FULL_BUILD, null);
            assertTrue(ipsProject.isJavaProjectErrorFree(false).booleanValue());

            // project closed
            ((IProject)ipsProject.getProject().unwrap()).close(null);
            assertNull(ipsProject.isJavaProjectErrorFree(false));

            // project does not exist
            IIpsProject project2 = IIpsModel.get().getIpsProject("Project2");
            assertNull(project2.isJavaProjectErrorFree(false));
        }
    }

    @Category(EclipseImplementation.class)
    @Test
    public void testIsJavaProjectErrorFree_WithRefToOtherProjects() throws CoreException {
        if (Abstractions.isEclipseRunning()) {
            IIpsProject ipsProject2 = newIpsProject("Project2");
            IIpsProject ipsProject3 = newIpsProject("Project3");
            AJavaProject javaProject1 = ipsProject.getJavaProject();
            AJavaProject javaProject2 = ipsProject2.getJavaProject();
            AJavaProject javaProject3 = ipsProject3.getJavaProject();

            IClasspathEntry refEntry = JavaCore.newProjectEntry(new Path("/Project2"));
            addClasspathEntry(javaProject1.unwrap(), refEntry);

            refEntry = JavaCore.newProjectEntry(new Path("/Project3"));
            addClasspathEntry(javaProject2.unwrap(), refEntry);

            assertNull(ipsProject3.isJavaProjectErrorFree(true));
            assertNull(ipsProject2.isJavaProjectErrorFree(true));
            assertNull(ipsProject.isJavaProjectErrorFree(true));

            // delete the source folder => inconsistent class path
            AFolder srcFolder = javaProject3.getProject().getFolder("src");
            srcFolder.delete(null);
            ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

            assertFalse(ipsProject3.isJavaProjectErrorFree(true).booleanValue());
            assertFalse(ipsProject2.isJavaProjectErrorFree(true).booleanValue());
            assertFalse(ipsProject.isJavaProjectErrorFree(true).booleanValue());
        }
    }

    private void makeIpsProjectDependOnBaseProject() {
        IIpsProjectProperties props = ipsProject.getProperties();
        IIpsObjectPath path = props.getIpsObjectPath();
        path.newIpsProjectRefEntry(baseProject);
        ipsProject.setProperties(props);
    }

    @Test
    public void testGetJavaProject() {
        AJavaProject javaProject = ipsProject.getJavaProject();
        assertNotNull(javaProject);
        assertEquals(ipsProject.getProject(), javaProject.getProject());
    }

    @Test
    public void testGetClassLoaderForJavaProject() {
        ClassLoader cl = ipsProject.getClassLoaderForJavaProject();
        assertNotNull(cl);
    }

    @Test
    public void testIsReferencedBy() {
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

    @Test
    public void testGetReferencingProjects() {
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

    @Test
    public void testGetReferencingProjectsLeavesOrSelf() {
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

    @Test
    public void testIsAccessibleViaIpsObjectPath() {
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
        assertFalse(ipsProject.isAccessibleViaIpsObjectPath(obj2));
        assertTrue(ipsProject.isAccessibleViaIpsObjectPath(obj1));
    }

    @Test
    public void testValidate() throws Exception {
        Thread.sleep(500);
        // if the validate is called too fast, the .ipsproject-file is not written to disk and so
        // can't be parsed!
        MessageList list = ipsProject.validate();
        assertTrue(list.isEmpty());
    }

    @Test
    public void testValidate_MissingPropertyFile() {
        AFile file = ipsProject.getIpsProjectPropertiesFile();
        file.delete(null);
        MessageList list = ipsProject.validate();
        assertNotNull(list.getMessageByCode(IIpsProject.MSGCODE_MISSING_PROPERTY_FILE));
        assertEquals(1, list.size());
    }

    @Test
    public void testValidate_UnparsablePropertyFile() {
        AFile file = ipsProject.getIpsProjectPropertiesFile();
        InputStream unparsableContents = new ByteArrayInputStream("blabla".getBytes());
        file.setContents(unparsableContents, false, null);
        suppressLoggingDuringExecutionOfThisTestCase();
        MessageList list = ipsProject.validate();
        assertNotNull(list.getMessageByCode(IIpsProject.MSGCODE_UNPARSABLE_PROPERTY_FILE));
        assertEquals(1, list.size());
    }

    @Test
    public void testGetProperties() {
        assertNotNull(ipsProject.getProperties());
    }

    /**
     * This has once been a bug. If setProperties() only saves the properties to the file without
     * updating it in memory, an access method might return an old value, if it is called before the
     * resource change listener has removed the old prop file from the cache in the model.
     */
    @Test
    public void testSetProperties_RacingCondition() {
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setRuntimeIdPrefix("newPrefix");
        ipsProject.setProperties(props);
        assertEquals("newPrefix", ipsProject.getRuntimeIdPrefix());
    }

    @Test
    public void testSetProperties() {
        IIpsProjectProperties props = ipsProject.getProperties();
        String builderSetId = props.getBuilderSetId();
        props.setBuilderSetId("myBuilder");

        // test if a copy was returned
        assertEquals(builderSetId, ipsProject.getProperties().getBuilderSetId());

        // test if prop file is updated
        AFile propFile = ipsProject.getIpsProjectPropertiesFile();
        long stamp = propFile.getModificationStamp();
        ipsProject.setProperties(props);
        assertTrue(propFile.getModificationStamp() != stamp);
        assertEquals("myBuilder", ipsProject.getProperties().getBuilderSetId());

        // test if a copy was created during set
        props.setBuilderSetId("newBuilder");
        assertEquals("myBuilder", ipsProject.getProperties().getBuilderSetId());
    }

    @Test
    public void testFindDatatype() {
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setPredefinedDatatypesUsed(
                new String[] { Datatype.DECIMAL.getQualifiedName(), Datatype.PRIMITIVE_INT.getQualifiedName() });
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

    @Test
    public void testFindDatatype_IndirectRefProject() {
        makeIpsProjectDependOnBaseProjectIndirect(true);

        assertEquals(Datatype.INTEGER, ipsProject.findDatatype("Integer"));
    }

    @Test
    public void testFindDatatype_IndirectRefProject_ReexportedIsFalse() {
        makeIpsProjectDependOnBaseProjectIndirect(false);

        assertNull(ipsProject.findDatatype("Integer"));
    }

    /**
     * Creates a setup where ipsProject is referencing baseProject indirectly over refProject:
     * <em>ipsProject -> refProject -> baseProject</em>.
     * <p>
     * Only baseProject has a datatype of type <code>Integer</code>.
     */
    private void makeIpsProjectDependOnBaseProjectIndirect(boolean reexported) {
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setPredefinedDatatypesUsed(new String[] {});
        ipsProject.setProperties(props);
        assertNull(ipsProject.findDatatype("Integer"));

        IIpsProject refProject = newIpsProject("RefProject");
        IIpsProjectProperties refProps = refProject.getProperties();
        refProps.setPredefinedDatatypesUsed(new String[] {});
        IIpsObjectPath refPath = refProps.getIpsObjectPath();
        IIpsProjectRefEntry refEntry = refPath.newIpsProjectRefEntry(baseProject);
        refEntry.setReexported(reexported);
        refProject.setProperties(refProps);

        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(refProject);
        ipsProject.setIpsObjectPath(path);
    }

    @Test
    public void testFindValueDatatype() {
        assertNull(ipsProject.findValueDatatype(null));

        IIpsProjectProperties props = ipsProject.getProperties();
        props.setPredefinedDatatypesUsed(
                new String[] { Datatype.DECIMAL.getQualifiedName(), Datatype.PRIMITIVE_INT.getQualifiedName() });
        ipsProject.setProperties(props);

        assertEquals(Datatype.DECIMAL, ipsProject.findValueDatatype("Decimal"));
        ArrayOfValueDatatype type = (ArrayOfValueDatatype)ipsProject.findValueDatatype("Decimal[][]");
        assertEquals(Datatype.DECIMAL, type.getBasicDatatype());
        assertEquals(2, type.getDimension());
        assertEquals(Datatype.DECIMAL, ipsProject.findValueDatatype("Decimal"));
        assertNull(ipsProject.findValueDatatype("Unknown"));
        assertNull(ipsProject.findValueDatatype("Integer"));

        makeIpsProjectDependOnBaseProject();
        assertEquals(Datatype.INTEGER, ipsProject.findValueDatatype("Integer"));

        newPolicyCmptType(ipsProject, "Policy");
        assertNull(ipsProject.findValueDatatype("Policy"));
    }

    @Test
    public void testFindValueDatatype_IndirectRefProject() {
        makeIpsProjectDependOnBaseProjectIndirect(true);

        assertEquals(Datatype.INTEGER, ipsProject.findValueDatatype("Integer"));
    }

    @Test
    public void testFindValueDatatype_IndirectRefProject_ReexportedIsFalse() {
        makeIpsProjectDependOnBaseProjectIndirect(false);

        assertNull(ipsProject.findValueDatatype("Integer"));
    }

    @Test
    public void testFindValueDatatypeWithEnumTypes() throws Exception {
        IEnumType paymentMode = newEnumType(ipsProject, "PaymentMode");
        paymentMode.setAbstract(false);
        paymentMode.setExtensible(false);
        paymentMode.newEnumLiteralNameAttribute();
        IEnumAttribute id = paymentMode.newEnumAttribute();
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setInherited(false);
        id.setName("id");

        IEnumType gender = newEnumType(ipsProject, "Gender");
        gender.setAbstract(false);
        gender.setExtensible(false);
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

    @Test
    public void testfindEnumTypes() throws Exception {
        IEnumType paymentMode = newEnumType(ipsProject, "PaymentMode");
        paymentMode.setAbstract(true);
        paymentMode.setExtensible(true);
        IEnumAttribute id = paymentMode.newEnumAttribute();
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setInherited(false);
        id.setName("id");

        IEnumType gender = newEnumType(ipsProject, "Gender");
        gender.setAbstract(false);
        gender.setExtensible(false);
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

    @Test
    public void testGetDatatypeHelper_DelegatesToBuilderSet() {
        // no helper registered in builder set
        assertNull(ipsProject.getDatatypeHelper(Datatype.MONEY));

        // register helper
        setPredefinedDatatypesUsed(ipsProject, Datatype.DECIMAL.getQualifiedName());
        TestIpsArtefactBuilderSet builderSet = (TestIpsArtefactBuilderSet)ipsProject.getIpsArtefactBuilderSet();
        builderSet.testObjectsMap.put(Datatype.DECIMAL, new DecimalHelper(Datatype.DECIMAL));

        DatatypeHelper helper = ipsProject.getDatatypeHelper(Datatype.DECIMAL);
        assertNotNull(helper);
        assertEquals(DecimalHelper.class, helper.getClass());

        // still no helper registered
        assertNull(ipsProject.getDatatypeHelper(Datatype.MONEY));
    }

    @Test
    public void testGetDatatypeHelper_DelegatesToReferencedProjects() throws Exception {
        // no referenced project present, no helper is found
        IIpsProject referencingProject = newIpsProject();
        assertNull(referencingProject.getDatatypeHelper(Datatype.DECIMAL));

        // create a referenced project with a data type helper
        setPredefinedDatatypesUsed(ipsProject, Datatype.DECIMAL.getQualifiedName());
        TestIpsArtefactBuilderSet builderSet = (TestIpsArtefactBuilderSet)ipsProject.getIpsArtefactBuilderSet();
        builderSet.testObjectsMap.put(Datatype.DECIMAL, new DecimalHelper(Datatype.DECIMAL));
        createProjectReference(referencingProject, ipsProject);

        // precondition: helper is found in referenced project
        assertNotNull(ipsProject.getDatatypeHelper(Datatype.DECIMAL));

        // referenced project exists, helper should be found in project
        DatatypeHelper helper = referencingProject.getDatatypeHelper(Datatype.DECIMAL);
        assertNotNull(helper);
        assertEquals(DecimalHelper.class, helper.getClass());
    }

    @Test
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

    @Test
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

        // setup dependency to other project, these datatypes of the referenced project must also be
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
        assertEquals(TestEnumType.class.getSimpleName(), types[2].getName());

        // only value types, void included
        types = ipsProject.findDatatypes(true, true);
        assertEquals(4, types.length);
        assertEquals(Datatype.VOID, types[0]);
        assertEquals(Datatype.DECIMAL, types[1]);
        assertEquals(Datatype.MONEY, types[2]);
        assertEquals(TestEnumType.class.getSimpleName(), types[3].getName());

        // all types, void not included
        types = ipsProject.findDatatypes(false, false);
        assertEquals(6, types.length);
        assertEquals(Datatype.DECIMAL, types[0]);
        assertEquals(messageListDatatype, types[1]);
        assertEquals(Datatype.MONEY, types[2]);
        assertEquals(TestEnumType.class.getSimpleName(), types[3].getName());
        assertEquals(pcType1, types[4]);
        assertEquals(pcType2, types[5]);

        // all types, void included
        types = ipsProject.findDatatypes(false, true);
        assertEquals(7, types.length);
        assertEquals(Datatype.VOID, types[0]);
        assertEquals(Datatype.DECIMAL, types[1]);
        assertEquals(messageListDatatype, types[2]);
        assertEquals(Datatype.MONEY, types[3]);
        assertEquals(TestEnumType.class.getSimpleName(), types[4].getName());
        assertEquals(pcType1, types[5]);
        assertEquals(pcType2, types[6]);
    }

    @Test
    public void testFindDatatypes3Parameters() {
        IPolicyCmptType a = newPolicyAndProductCmptType(ipsProject, "a", "aConfig");
        List<Datatype> datatypes = Arrays.asList(ipsProject.findDatatypes(false, false, false));
        assertTrue(datatypes.contains(a));
        IProductCmptType aConfig = a.findProductCmptType(ipsProject);
        assertTrue(datatypes.contains(aConfig));
    }

    @Test
    public void testFindDatatypes4Parameters() {
        List<Datatype> disallowedTypesTest = new ArrayList<>(1);
        disallowedTypesTest.add(Datatype.STRING);
        Datatype[] types = ipsProject.findDatatypes(false, false, false, disallowedTypesTest);
        assertFalse(Arrays.asList(types).contains(Datatype.STRING));
    }

    @Test
    public void testFindDatatypes5Parameters() {
        IEnumType testEnumType = newEnumType(ipsProject, "TestEnumType");
        testEnumType.setAbstract(true);
        Datatype[] types = ipsProject.findDatatypes(false, false, false, null, false);
        assertFalse(Arrays.asList(types).contains(new EnumTypeDatatypeAdapter(testEnumType, null)));

        testEnumType.setAbstract(false);
        types = ipsProject.findDatatypes(false, false, false, null, true);
        assertTrue(Arrays.asList(types).contains(new EnumTypeDatatypeAdapter(testEnumType, null)));
    }

    @Test
    public void testFindDatatypes_IndirectRefProject() {
        makeIpsProjectDependOnBaseProjectIndirect(true);

        Datatype[] datatypes = ipsProject.findDatatypes(false, false);
        assertEquals(1, datatypes.length);
        assertEquals(Datatype.INTEGER, datatypes[0]);
    }

    @Test
    public void testFindDatatypes_IndirectRefProject_ReexportedIsFalse() {
        makeIpsProjectDependOnBaseProjectIndirect(false);

        Datatype[] datatypes = ipsProject.findDatatypes(false, false);
        assertEquals(0, datatypes.length);
    }

    @Test
    public void testFindDatatypesOfEnumType() throws Exception {
        IEnumType paymentMode = newEnumType(ipsProject, "PaymentMode");
        paymentMode.setAbstract(false);
        paymentMode.setExtensible(false);
        paymentMode.newEnumLiteralNameAttribute();
        IEnumAttribute id = paymentMode.newEnumAttribute();
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setInherited(false);
        id.setName("id");

        IEnumType gender = newEnumType(ipsProject, "Gender");
        gender.setAbstract(false);
        gender.setExtensible(false);
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
        setPredefinedDatatypesUsed(refProject, Datatype.DECIMAL.getQualifiedName(), Datatype.MONEY.getQualifiedName());

        newDefinedEnumDatatype(refProject, new Class[] { TestEnumType.class });
        // set the reference from the ips project to the referenced project
        createProjectReference(ipsProject, refProject);
        return refProject;
    }

    @Test
    public void testFindIpsSrcFile() throws IpsException, InterruptedException {
        IPolicyCmptType testObject = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.Test");
        QualifiedNameType qnt = new QualifiedNameType("a.b.Test", IpsObjectType.POLICY_CMPT_TYPE);
        IIpsSrcFile file = ipsProject.findIpsSrcFile(qnt);
        assertNotNull(file);
        assertEquals(testObject, file.getIpsObject());

        // search again should return same instance as src file handles are cached
        assertSame(file, ipsProject.findIpsSrcFile(qnt));

        // if we change the ips project properties, the cache must be cleared and a new file
        // instance returned.
        // sleep some time to make sure the .ipsproject file has definitly a new
        Thread.sleep(500);
        // modification stamp
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        AFolder newFolder = ipsProject.getProject().getFolder("newFolder");
        newFolder.create(null);
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

    @Test
    public void testFindIpsSrcFile_WithTwoSrcFolderRoots() throws IpsException, InterruptedException {
        IPolicyCmptType testObject = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.Test");
        QualifiedNameType qnt = new QualifiedNameType("a.b.Test", IpsObjectType.POLICY_CMPT_TYPE);
        IIpsSrcFile file = ipsProject.findIpsSrcFile(qnt);
        assertNotNull(file);
        assertEquals(testObject, file.getIpsObject());

        // search again should return same instance as src file handles are cached
        assertSame(file, ipsProject.findIpsSrcFile(qnt));

        // if we change the ips project properties, the cache must be cleared and a new file
        // instance returned.
        // sleep some time to make sure the .ipsproject file has definitly a new
        Thread.sleep(500);
        // modification stamp
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        AFolder newFolder = ipsProject.getProject().getFolder("newFolder");
        newFolder.create(null);
        IIpsSrcFolderEntry entry = path.newSourceFolderEntry(newFolder);
        ipsProject.setIpsObjectPath(path);

        // test if we also find files on the second entry
        IPolicyCmptType newType = newPolicyCmptType(entry.getIpsPackageFragmentRoot(), "NewPolicy");
        assertEquals(newType.getIpsSrcFile(), ipsProject.findIpsSrcFile(newType.getQualifiedNameType()));

        // test if caching works also for the second entry
        assertEquals(newType.getIpsSrcFile(), ipsProject.findIpsSrcFile(newType.getQualifiedNameType()));
    }

    @Test
    public void testFindIpsSrcFile_InArchive() throws Exception {
        IIpsProject archiveProject = newIpsProject("ArchiveProject");
        IPolicyCmptType type = newPolicyCmptType(archiveProject, "motor.Policy");
        newPolicyCmptTypeWithoutProductCmptType(archiveProject, "motor.collision.CollisionCoverage");
        newProductCmpt(archiveProject, "motor.MotorProduct");

        AFile archiveFile = ipsProject.getProject().getFile("test.ipsar");
        createArchive(archiveProject, archiveFile);

        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newArchiveEntry(archiveFile.getWorkspaceRelativePath());
        ipsProject.setIpsObjectPath(path);

        IIpsSrcFile foundFile = ipsProject.findIpsSrcFile(type.getQualifiedNameType());
        assertEquals(type.getQualifiedNameType(), foundFile.getQualifiedNameType());
    }

    @Test
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

    @Test
    public void testFindIpsObject() {
        IIpsPackageFragment folder = root.createPackageFragment("a.b", true, null);
        IIpsSrcFile file = folder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Test", true, null);
        IIpsObject pdObject = ipsProject.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "a.b.Test");
        assertNotNull(pdObject);
        assertEquals(file.getIpsObject(), pdObject);

        assertNull(ipsProject.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "c.Unknown"));

        // invalid package name as it contains a blank => should return null
        assertNull(ipsProject.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "c a.Unknown"));
    }

    /**
     * Test if the findProductCmpts method works with all kind of package fragments root
     */
    @Test
    public void testFindProductCmptsWithIpsArchive() throws Exception {
        AFile archiveFile = ipsProject.getProject().getFile("test.ipsar");
        createArchive(ipsProject, archiveFile);
        new IpsArchive(ipsProject, archiveFile.getProjectRelativePath());

        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newArchiveEntry(archiveFile.getLocation());
        ipsProject.setIpsObjectPath(path);

        IIpsSrcFile[] productCmptSrcFiles = ipsProject.findAllProductCmptSrcFiles(null, true);
        assertEquals(0, productCmptSrcFiles.length);
    }

    public IProductCmptType createProductCmptType(IIpsPackageFragment packageFragment,
            IPolicyCmptType policyCmptType,
            String name) {
        IProductCmptType productCmptType = (IProductCmptType)packageFragment
                .createIpsFile(IpsObjectType.PRODUCT_CMPT_TYPE, name, true, null).getIpsObject();
        productCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());
        policyCmptType.setProductCmptType(productCmptType.getQualifiedName());
        return productCmptType;
    }

    @Test
    public void testFindAllProductCmptSrcFiles() {
        // create the following types: Type0, Type1 and Type2
        IIpsPackageFragment pack = root.createPackageFragment("pack", true, null);
        IPolicyCmptType policyCmptType0 = (IPolicyCmptType)pack
                .createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Type0", true, null).getIpsObject();
        policyCmptType0.setConfigurableByProductCmptType(true);
        createProductCmptType(pack, policyCmptType0, "ProductCmptType0");

        pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Type1", true, null);
        IPolicyCmptType policyCmptType2 = (IPolicyCmptType)pack
                .createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Type2", true, null).getIpsObject();
        IProductCmptType productCmptType2 = (IProductCmptType)pack
                .createIpsFile(IpsObjectType.PRODUCT_CMPT_TYPE, "ProductCmptType2", true, null).getIpsObject();
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
        IPolicyCmptType policyCmptType10 = (IPolicyCmptType)pack
                .createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Type10", true, null).getIpsObject();
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

    @Test
    public void testFindAllTestCaseSrcFiles() {
        // create the following testcase types: TestType0, TestType1

        IIpsPackageFragment pack = root.createPackageFragment("pack", true, null);

        pack.createIpsFile(IpsObjectType.TEST_CASE_TYPE, "TestType0", true, null).getIpsObject();
        pack.createIpsFile(IpsObjectType.TEST_CASE_TYPE, "TestType1", true, null).getIpsObject();

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

        pack.createIpsFile(IpsObjectType.TEST_CASE_TYPE, "TestTypeProj2", true, null).getIpsObject();
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

    @Test
    public void testFindAllTableContentsSrcFiles() {
        ITableStructure ts0 = newTableStructure(ipsProject, "structure0");
        ITableStructure ts1 = newTableStructure(ipsProject, "structure1");

        ITableContents tc0 = newTableContents(ts0, "contets0");
        newTableContents(ts1, "contets1");
        ITableContents tc2 = newTableContents(ts0, "contets2");

        List<IIpsSrcFile> result = ipsProject.findAllTableContentsSrcFiles(ts0);
        assertEquals(2, result.size());
        assertThat(result, hasItem(tc0.getIpsSrcFile()));
        assertThat(result, hasItem(tc2.getIpsSrcFile()));

        result = ipsProject.findAllTableContentsSrcFiles(null);
        assertEquals(3, result.size());
    }

    @Test
    public void testFindAllEnumContentSrcFiles() {
        IIpsPackageFragment pack = root.createPackageFragment("pack", true, null);

        IEnumType enumType0 = (IEnumType)pack.createIpsFile(IpsObjectType.ENUM_TYPE, "EnumType0", true, null)
                .getIpsObject();
        pack.createIpsFile(IpsObjectType.ENUM_TYPE, "EnumType1", true, null).getIpsObject();
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

        pack.createIpsFile(IpsObjectType.ENUM_TYPE, "EnumTypeProj2", true, null).getIpsObject();
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

    @Test
    public void testFindIpsSrcFiles() {
        // create the following types: Type0, a.b.Type1 and c.Type2
        IIpsPackageFragment pack = root.getIpsPackageFragment("");
        IPolicyCmptType type0 = (IPolicyCmptType)pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Type0", true, null)
                .getIpsObject();
        IIpsPackageFragment folderAB = root.createPackageFragment("a.b", true, null);
        folderAB.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Type1", true, null);
        IIpsPackageFragment folderC = root.createPackageFragment("c", true, null);
        folderC.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Type2", true, null);

        IProductCmptType productCmptType0 = (IProductCmptType)pack
                .createIpsFile(IpsObjectType.PRODUCT_CMPT_TYPE, "ProductCmptType0", true, null).getIpsObject();
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

    @Test
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

    @Test
    public void testFindIpsSrcFiles_InArchive() throws Exception {
        IIpsProject archiveProject = newIpsProject("ArchiveProject");
        IPolicyCmptType policy = newPolicyCmptType(archiveProject, "motor.Policy");
        IPolicyCmptType coverage = newPolicyCmptTypeWithoutProductCmptType(archiveProject,
                "motor.collision.CollisionCoverage");
        newProductCmpt(archiveProject, "motor.MotorProduct");

        AFile archiveFile = ipsProject.getProject().getFile("test.ipsar");
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

    @Test
    public void testSetIpsObjectPath() {
        AFile projectFile = ipsProject.getIpsProjectPropertiesFile();
        long stamp = projectFile.getModificationStamp();
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.setOutputDefinedPerSrcFolder(false);
        path.setBasePackageNameForMergableJavaClasses("some.name");
        ipsProject.setIpsObjectPath(path);
        assertTrue(stamp != projectFile.getModificationStamp());

        // following line will receive a new IpsProject instance (as it is only a proxy)
        IIpsProject ipsProject2 = IIpsModel.get().getIpsProject(ipsProject.getProject());
        // test if he changed object path is also available with the new instance
        assertEquals("some.name", ipsProject2.getIpsObjectPath().getBasePackageNameForMergableJavaClasses());
    }

    @Test
    public void testFindEnumDatatypes() throws Exception {
        newDefinedEnumDatatype(ipsProject, new Class[] { TestEnumType.class });
        EnumDatatype[] dataTypes = ipsProject.findEnumDatatypes();
        assertEquals(1, dataTypes.length);
        assertEquals("TestEnumType", dataTypes[0].getQualifiedName());

        IEnumType paymentMode = newEnumType(ipsProject, "PaymentMode");
        paymentMode.setAbstract(false);
        paymentMode.setExtensible(false);
        paymentMode.newEnumLiteralNameAttribute();
        IEnumAttribute id = paymentMode.newEnumAttribute();
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setInherited(false);
        id.setName("id");

        IEnumType gender = newEnumType(ipsProject, "Gender");
        gender.setAbstract(false);
        gender.setExtensible(false);
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

    @Category(EclipseImplementation.class)
    @Test
    public void testGetNonIpsResources() throws CoreException {
        if (Abstractions.isEclipseRunning()) {
            AProject projectHandle = ipsProject.getProject();
            AFolder nonIpsRoot = projectHandle.getFolder("nonIpsRoot");
            nonIpsRoot.create(null);
            AFile nonIpsFile = projectHandle.getFile("nonIpsFile");
            nonIpsFile.create(null, null);

            AFolder classpathFolder = projectHandle.getFolder("classpathFolder");
            classpathFolder.create(null);
            AFolder outputFolder = projectHandle.getFolder("outputFolder");
            outputFolder.create(null);
            AFile classpathFile = projectHandle.getFile("classpathFile");
            classpathFile.create(null, null);

            // add classpathFolder and classpathFile to the javaprojects classpath
            // add outputFolder as apecific outputlocation of classpathFolder
            IJavaProject javaProject = ipsProject.getJavaProject().unwrap();
            IClasspathEntry[] cpEntries = javaProject.getRawClasspath();
            IClasspathEntry[] newEntries = new IClasspathEntry[2];
            newEntries[0] = JavaCore.newSourceEntry(toEclipsePath(classpathFolder.getWorkspaceRelativePath()));
            newEntries[1] = JavaCore.newSourceEntry(toEclipsePath(classpathFile.getWorkspaceRelativePath()),
                    new IPath[] {},
                    toEclipsePath(outputFolder.getWorkspaceRelativePath()));
            IClasspathEntry[] result = new IClasspathEntry[cpEntries.length + newEntries.length];
            System.arraycopy(cpEntries, 0, result, 0, cpEntries.length);
            System.arraycopy(newEntries, 0, result, cpEntries.length, newEntries.length);
            javaProject.setRawClasspath(result, null);

            Object[] nonIpsResources = ipsProject.getNonIpsResources();
            List<?> list = Arrays.asList(nonIpsResources);
            assertTrue(list.contains(nonIpsRoot));
            assertTrue(list.contains(nonIpsFile));
            // /bin, /src and /extension are outputfolders or classpath entries and thus filtered
            // out
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
    }

    @Test
    public void testDependsOn() {
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

    @Test
    public void testValidateMissingMigration() throws Exception {
        MessageList ml = ipsProject.validate();
        assertThat(ml, lacksMessageCode(IIpsProject.MSGCODE_INVALID_MIGRATION_INFORMATION));

        setMinRequiredVersion("0.0.3");
        try (TestIpsModelExtensions testIpsModelExtensions = TestIpsModelExtensions.get()) {
            testIpsModelExtensions.setFeatureVersionManagers(new InvalidMigrationMockManager());
            suppressLoggingDuringExecutionOfThisTestCase();
            ml = ipsProject.validate();
            assertThat(ml, hasMessageCode(IIpsProject.MSGCODE_INVALID_MIGRATION_INFORMATION));
        }
    }

    @Test
    public void testValidateIfOutputFolderSetForSrcFolderEntry() {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IIpsSrcFolderEntry srcFolder = path.newSourceFolderEntry((AFolder)root.getEnclosingResource());
        srcFolder.setSpecificBasePackageNameForDerivedJavaClasses("srctest");
        path.setBasePackageNameForMergableJavaClasses("test");
        ipsProject.setIpsObjectPath(path);

        MessageList msgList = ipsProject.validate();
        Message msg = msgList.getMessageByCode(IIpsSrcFolderEntry.MSGCODE_OUTPUT_FOLDER_DERIVED_MISSING);
        assertNotNull(msg);
        msg = msgList.getMessageByCode(IIpsSrcFolderEntry.MSGCODE_OUTPUT_FOLDER_MERGABLE_MISSING);
        assertNotNull(msg);

        AFolder outMerge = ipsProject.getProject().getFolder("src");
        if (!outMerge.exists()) {
            outMerge.create(null);
        }
        path.setOutputFolderForMergableSources(outMerge);
        AFolder outDerived = ipsProject.getProject().getFolder("derived");
        if (!outDerived.exists()) {
            outDerived.create(null);
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

    @Test
    public void testValidateDuplicateTocFilesInDifferentProjects() throws Exception {
        // check if the validation doesn't fail for a valid non duplicate toc file path
        MessageList ml = ipsProject.validate();
        assertThat(ml, lacksMessageCode(IIpsProject.MSGCODE_DUPLICATE_TOC_FILE_PATH_IN_DIFFERENT_PROJECTS));

        // create builder set so that this test case is independent from StandardBuilderSet which is
        // in a different plugin
        IIpsArtefactBuilderSet projectABuilderSet = new DefaultBuilderSet() {

            @Override
            protected LinkedHashMap<IBuilderKindId, IIpsArtefactBuilder> createBuilders() {
                return new LinkedHashMap<>();
            }

            @Override
            protected String getConfiguredAdditionalAnnotations() {
                return StringUtils.EMPTY;
            }

            @Override
            public boolean isGeneratePublishedInterfaces() {
                return true;
            }

            @Override
            public DatatypeHelper getDatatypeHelper(Datatype datatype) {
                return null;
            }

        };
        projectABuilderSet.setId("projectABuilderSet");
        projectABuilderSet.setIpsProject(ipsProject);

        IIpsProjectProperties props = ipsProject.getProperties();
        props.setBuilderSetId("projectABuilderSet");

        // the project needs to be a product definition project to force a fail of the validation
        props.setProductDefinitionProject(true);
        ipsProject.setProperties(props);
        ((IpsModel)getIpsModel()).setIpsArtefactBuilderSetInfos(
                new IIpsArtefactBuilderSetInfo[] { new TestArtefactBuilderSetInfo(projectABuilderSet) });

        IIpsObjectPath projectAIpsObjectPath = ipsProject.getIpsObjectPathInternal();

        // to have not to care about each ipsobject path entry the properties are set on the path
        projectAIpsObjectPath.setOutputDefinedPerSrcFolder(false);
        // the DefaultBuilderSet uses this package name to determine the toc file name e.g. path
        projectAIpsObjectPath.setBasePackageNameForDerivedJavaClasses("org.faktorzehn.de");
        projectAIpsObjectPath.setBasePackageNameForMergableJavaClasses("org.faktorzehn.de");

        AFolder outputFolderDerived = ipsProject.getProject().getFolder("derived");
        if (!outputFolderDerived.exists()) {
            outputFolderDerived.create(null);
        }
        projectAIpsObjectPath.setOutputFolderForDerivedSources(outputFolderDerived);

        AFolder outputFolderMergeable = ipsProject.getProject().getFolder("src");
        if (!outputFolderMergeable.exists()) {
            outputFolderMergeable.create(null);
        }
        projectAIpsObjectPath.setOutputFolderForDerivedSources(outputFolderMergeable);

        // second ipsproject with its own builderset but the same setting for the toc file name
        IpsProject ipsProjectB = (IpsProject)newIpsProject("TestProjectB");

        IIpsArtefactBuilderSet projectBBuilderSet = new DefaultBuilderSet() {

            @Override
            protected LinkedHashMap<IBuilderKindId, IIpsArtefactBuilder> createBuilders() {
                return new LinkedHashMap<>();
            }

            @Override
            protected String getConfiguredAdditionalAnnotations() {
                return StringUtils.EMPTY;
            }

            @Override
            public boolean isGeneratePublishedInterfaces() {
                return true;
            }

            @Override
            public DatatypeHelper getDatatypeHelper(Datatype datatype) {
                return null;
            }

        };
        projectBBuilderSet.setId("projectBBuilderSet");
        projectBBuilderSet.setIpsProject(ipsProjectB);

        IIpsProjectProperties projectBProperties = ipsProjectB.getProperties();
        projectBProperties.setBuilderSetId("projectBBuilderSet");

        // the project needs to be a product definition project to force a fail of the validation
        projectBProperties.setProductDefinitionProject(true);
        ipsProjectB.setProperties(projectBProperties);
        ((IpsModel)getIpsModel()).setIpsArtefactBuilderSetInfos(
                new IIpsArtefactBuilderSetInfo[] { new TestArtefactBuilderSetInfo(projectBBuilderSet),
                        new TestArtefactBuilderSetInfo(projectABuilderSet) });

        // establish the dependency so that projectB is dependent from projectA
        IpsObjectPath projectBIpsObjectPath = ipsProjectB.getIpsObjectPathInternal();
        ArrayList<IIpsObjectPathEntry> projectBIpsObjectPathEntries = new ArrayList<>(
                Arrays.asList(projectBIpsObjectPath.getEntries()));
        projectBIpsObjectPathEntries.add(new IpsProjectRefEntry(projectBIpsObjectPath, ipsProject));

        projectBIpsObjectPath.setEntries(
                projectBIpsObjectPathEntries.toArray(new IIpsObjectPathEntry[projectBIpsObjectPathEntries.size()]));
        projectBIpsObjectPath.setOutputDefinedPerSrcFolder(false);
        projectBIpsObjectPath.setBasePackageNameForDerivedJavaClasses("org.faktorzehn.de");
        projectBIpsObjectPath.setBasePackageNameForMergableJavaClasses("org.faktorzehn.de");
        ((IpsSrcFolderEntry)projectBIpsObjectPathEntries.get(0)).setUniqueQualifier("B");

        outputFolderDerived = ipsProjectB.getProject().getFolder("derived");
        if (!outputFolderDerived.exists()) {
            outputFolderDerived.create(null);
        }
        projectBIpsObjectPath.setOutputFolderForDerivedSources(outputFolderDerived);

        outputFolderMergeable = ipsProjectB.getProject().getFolder("src");
        if (!outputFolderMergeable.exists()) {
            outputFolderMergeable.create(null);
        }
        projectBIpsObjectPath.setOutputFolderForMergableSources(outputFolderMergeable);

        // for projectB the validation is expected to fail
        MessageList msgList = ipsProjectB.validate();
        assertNotNull(msgList.getMessageByCode(IIpsProject.MSGCODE_DUPLICATE_TOC_FILE_PATH_IN_DIFFERENT_PROJECTS));

        // for projectA the validation is expected not to fail since A doesn't depend on B
        msgList = ipsProject.validate();
        assertNull(msgList.getMessageByCode(IIpsProject.MSGCODE_DUPLICATE_TOC_FILE_PATH_IN_DIFFERENT_PROJECTS));

        // FIPS-5387 : the validation should also work for model projects
        props = ipsProject.getProperties();
        props.setProductDefinitionProject(false);
        ipsProject.setProperties(props);
        projectBProperties = ipsProjectB.getProperties();
        projectBProperties.setProductDefinitionProject(false);
        ipsProjectB.setProperties(projectBProperties);

        msgList = ipsProjectB.validate();
        assertNotNull(msgList.getMessageByCode(IIpsProject.MSGCODE_DUPLICATE_TOC_FILE_PATH_IN_DIFFERENT_PROJECTS));
    }

    @Test
    public void testValidateIpsObjectPathCycle() {
        IIpsProject ipsProject2 = this.newIpsProject("TestProject2");
        IIpsObjectPath path = ipsProject2.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject);
        ipsProject2.setIpsObjectPath(path);
        updateSrcFolderEntryQualifiers(ipsProject2, "2");

        MessageList ml = ipsProject.validate();
        assertThat(ml, lacksMessageCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));

        path = ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject2);
        ipsProject.setIpsObjectPath(path);

        List<IIpsSrcFile> result = new ArrayList<>();
        ipsProject.findAllIpsSrcFiles(result);
        // there is an cycle in the ref projects,
        // if we get no stack overflow exception, then the test was successfully executed

        ml = ipsProject.validate();
        assertThat(ml, hasMessageCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));

        path = ipsProject.getIpsObjectPath();
        path.removeProjectRefEntry(ipsProject2);
        ipsProject.setIpsObjectPath(path);

        ml = ipsProject.validate();
        assertThat(ml, lacksMessageCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));
    }

    /**
     * Sets the unique qualifier of all source folder entries in the given project to the given
     * qualifiers (in order). Thus, if you'd like to set the two source folder entries of a project
     * to different qualifiers, call this method with
     * <code>setUniqueQualifier(prj, "qualifier1", "qualifier2")</code>.
     */
    public static void updateSrcFolderEntryQualifiers(IIpsProject prj, String... qualifiers) {
        IIpsObjectPath path = prj.getIpsObjectPath();
        IIpsObjectPathEntry[] entries = path.getEntries();
        for (int i = 0; i < entries.length; i++) {
            IIpsObjectPathEntry entry = entries[i];
            if (entry instanceof IpsSrcFolderEntry) {
                IpsSrcFolderEntry srcFolderEntry = (IpsSrcFolderEntry)entry;
                srcFolderEntry.setUniqueQualifier(qualifiers[i]);
            }
        }
        prj.setIpsObjectPath(path);
    }

    @Test
    public void testValidateIpsObjectPathCycle_ProjectHasSelfReference() {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject);
        ipsProject.setIpsObjectPath(path);

        List<IIpsSrcFile> result = new ArrayList<>();
        ipsProject.findAllIpsSrcFiles(result);
        ipsProject.findIpsObject(new QualifiedNameType("xyz", IpsObjectType.PRODUCT_CMPT));
        // there is an cycle in the ref projects,
        // if we get no stack overflow exception, then the test was successfully executed

        MessageList ml = ipsProject.validate();
        assertThat(ml, hasMessageCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));
    }

    @Test
    public void testValidateIpsObjectPathCycle_CycleInFourProjects() {
        IIpsProject ipsProject10 = this.newIpsProject("TestProject10");
        IIpsProject ipsProject11 = this.newIpsProject("TestProject11");
        IIpsProject ipsProject12 = this.newIpsProject("TestProject12");
        IIpsProject ipsProject13 = this.newIpsProject("TestProject13");
        updateSrcFolderEntryQualifiers(ipsProject11, "11");
        updateSrcFolderEntryQualifiers(ipsProject12, "12");
        updateSrcFolderEntryQualifiers(ipsProject13, "13");

        IIpsObjectPath path = ipsProject10.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject11);
        ipsProject10.setIpsObjectPath(path);

        path = ipsProject10.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject12);
        ipsProject10.setIpsObjectPath(path);

        path = ipsProject11.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject13);
        // invalid reference, should not result in a stack overflow exception
        path.newIpsProjectRefEntry(ipsProject11);
        ipsProject11.setIpsObjectPath(path);

        path = ipsProject12.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject13);
        ipsProject12.setIpsObjectPath(path);

        List<IIpsSrcFile> result = new ArrayList<>();
        ipsProject.findAllIpsSrcFiles(result);

        MessageList ml = ipsProject10.validate();
        assertThat(ml, lacksMessageCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));

        path = ipsProject13.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject10);
        ipsProject13.setIpsObjectPath(path);

        ml = ipsProject10.validate();
        assertThat(ml, hasMessageCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));
        ml = ipsProject11.validate();
        assertThat(ml, hasMessageCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));
        ml = ipsProject12.validate();
        assertThat(ml, hasMessageCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));
        ml = ipsProject13.validate();
        assertThat(ml, hasMessageCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));
    }

    @Test
    public void testValidateIpsObjectPathCycle_ReexportedIsFalse() {
        IIpsProject ipsProject2 = this.newIpsProject("TestProject2");
        IIpsObjectPath path = ipsProject2.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject).setReexported(false);
        ipsProject2.setIpsObjectPath(path);

        MessageList ml = ipsProject.validate();
        assertThat(ml, lacksMessageCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));

        path = ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject2).setReexported(false);
        ipsProject.setIpsObjectPath(path);

        List<IIpsSrcFile> result = new ArrayList<>();
        ipsProject.findAllIpsSrcFiles(result);
        // there is an cycle in the ref projects,
        // if we get no stack overflow exception, then the test was successfully executed

        ml = ipsProject.validate();
        assertThat(ml, lacksMessageCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));

        path = ipsProject.getIpsObjectPath();
        path.removeProjectRefEntry(ipsProject2);
        ipsProject.setIpsObjectPath(path);

        ml = ipsProject.validate();
        assertThat(ml, lacksMessageCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));
    }

    @Test
    public void testValidateIpsObjectPathCycle_ReexportedIsFalse_ProjectHasSelfReference() {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject).setReexported(false);
        ipsProject.setIpsObjectPath(path);

        List<IIpsSrcFile> result = new ArrayList<>();
        ipsProject.findAllIpsSrcFiles(result);
        ipsProject.findIpsObject(new QualifiedNameType("xyz", IpsObjectType.PRODUCT_CMPT));
        // there is an cycle in the ref projects,
        // if we get no stack overflow exception, then the test was successfully executed

        MessageList ml = ipsProject.validate();
        assertThat(ml, hasMessageCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));
    }

    @Test
    public void testValidateIpsObjectPathCycle_ReexportedIsFalse_CycleInFourProjects() {
        IIpsProject ipsProject10 = this.newIpsProject("TestProject10");
        IIpsProject ipsProject11 = this.newIpsProject("TestProject11");
        IIpsProject ipsProject12 = this.newIpsProject("TestProject12");
        IIpsProject ipsProject13 = this.newIpsProject("TestProject13");

        IIpsObjectPath path = ipsProject10.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject11).setReexported(false);
        ipsProject10.setIpsObjectPath(path);

        path = ipsProject10.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject12).setReexported(false);
        ipsProject10.setIpsObjectPath(path);

        path = ipsProject11.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject13).setReexported(false);
        ipsProject11.setIpsObjectPath(path);

        path = ipsProject12.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject13).setReexported(false);
        // invalid reference, should not result in a stack overflow exception
        path.newIpsProjectRefEntry(ipsProject11).setReexported(false);
        ipsProject12.setIpsObjectPath(path);

        List<IIpsSrcFile> result = new ArrayList<>();
        ipsProject.findAllIpsSrcFiles(result);

        MessageList ml = ipsProject10.validate();
        assertThat(ml, lacksMessageCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));

        path = ipsProject13.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject10).setReexported(false);
        ipsProject13.setIpsObjectPath(path);

        ml = ipsProject10.validate();
        assertThat(ml, lacksMessageCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));
        ml = ipsProject11.validate();
        assertThat(ml, lacksMessageCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));
        ml = ipsProject12.validate();
        assertThat(ml, lacksMessageCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));
        ml = ipsProject13.validate();
        assertThat(ml, lacksMessageCode(IIpsProject.MSGCODE_CYCLE_IN_IPS_OBJECT_PATH));
    }

    private void setMinRequiredVersion(String version) {
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setMinRequiredVersionNumber("org.faktorips.feature", version);
        ipsProject.setProperties(props);
    }

    @Test
    public void testGetNamingConventions() {
        IIpsProjectNamingConventions namingConventions = ipsProject.getNamingConventions();
        assertTrue(namingConventions instanceof DefaultIpsProjectNamingConventions);
        assertFalse(namingConventions.validateIpsPackageName("testPackage").containsErrorMsg());
        assertTrue(namingConventions.validateIpsPackageName("1").containsErrorMsg());
    }

    @Test
    public void testCheckForDuplicateRuntimeIds() {
        IIpsProject prj = newIpsProject("PRJ1");

        IProductCmpt cmpt1 = newProductCmpt(prj, "product1");
        IProductCmpt cmpt2 = newProductCmpt(prj, "product2");
        cmpt1.setRuntimeId("Egon");
        cmpt2.setRuntimeId("Egon");
        assertEquals(cmpt1.getRuntimeId(), cmpt2.getRuntimeId());

        MessageList ml = prj.checkForDuplicateRuntimeIds(prj.findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT));
        assertEquals(2, ml.size());
        assertThat(ml, hasMessageCode(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION));

        cmpt2.setRuntimeId("Hugo");
        ml = prj.checkForDuplicateRuntimeIds(prj.findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT));
        assertEquals(0, ml.size());
        assertThat(ml, lacksMessageCode(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION));

        // test that not linked projects are not checked against each other
        IProductCmpt cmpt3 = newProductCmpt(ipsProject, "product3");
        cmpt3.setRuntimeId("Egon");
        cmpt2.setRuntimeId("Egon");
        ml = prj.checkForDuplicateRuntimeIds(prj.findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT));
        assertEquals(ml.toString(), 2, ml.size());
        assertThat(ml, hasMessageCode(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION));

        // test that linked projects will be checked against each other
        IIpsObjectPath objectPath = prj.getIpsObjectPath();
        objectPath.newIpsProjectRefEntry(ipsProject);
        prj.setIpsObjectPath(objectPath);
        ml = prj.checkForDuplicateRuntimeIds(prj.findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT));
        assertEquals(ml.toString(), 6, ml.size());
        assertThat(ml, hasMessageCode(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION));

        ml = prj.checkForDuplicateRuntimeIds(cmpt3.getIpsSrcFile());
        assertEquals(2, ml.size());
        assertThat(ml, hasMessageCode(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION));

        ml = prj.checkForDuplicateRuntimeIds(cmpt1.getIpsSrcFile(), cmpt3.getIpsSrcFile());
        assertEquals(4, ml.size());
        assertThat(ml, hasMessageCode(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION));
    }

    @Test
    public void testIsResourceExcludedFromProductDefinition() {
        assertFalse(ipsProject.isResourceExcludedFromProductDefinition(null));

        AFolder folder1 = ipsProject.getProject().getFolder("exludedFolderWithFile");
        AFile file = ipsProject.getProject().getFile("exludedFolderWithFile/build.xml");
        AFolder folder2 = ipsProject.getProject().getFolder("exludedFolder");

        folder1.create(null);
        file.create(new ByteArrayInputStream("test".getBytes()), null);
        folder2.create(null);

        assertFalse(ipsProject.isResourceExcludedFromProductDefinition(folder2));
        assertFalse(ipsProject.isResourceExcludedFromProductDefinition(file));
        assertFalse(ipsProject.isResourceExcludedFromProductDefinition(folder1));

        IIpsProjectProperties props = ipsProject.getProperties();
        props.addResourcesPathExcludedFromTheProductDefiniton("exludedFolderWithFile/build.xml");
        props.addResourcesPathExcludedFromTheProductDefiniton("exludedFolder");
        ipsProject.setProperties(props);

        assertTrue(ipsProject.isResourceExcludedFromProductDefinition(folder2));
        assertTrue(ipsProject.isResourceExcludedFromProductDefinition(file));
        assertFalse(ipsProject.isResourceExcludedFromProductDefinition(folder1));
    }

    @Test
    public void testFindIpsSourceFiles() {
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

        List<IIpsSrcFile> resultList = new ArrayList<>();
        ipsProject.findAllIpsSrcFiles(resultList);
        assertEquals(9, resultList.size());

    }

    @Test
    public void testFindEnumContent() throws Exception {
        IEnumType eA = newEnumType(ipsProject, "a.b.c.EA");
        eA.setExtensible(true);
        eA.setEnumContentName("a.b.c");
        eA.setEnumContentName("a.b.c.contentA");
        IEnumContent contentA = newEnumContent(eA, "a.b.c.contentA");
        IEnumContent result = ipsProject.findEnumContent(eA);
        assertEquals(contentA, result);
    }

    @Test
    public void testGetResourceAsStream() throws IpsException, IOException {
        IIpsPackageFragmentRoot rootOne = newIpsPackageFragmentRoot(ipsProject, null, "rootOne");
        createFileWithContent((AFolder)rootOne.getCorrespondingResource(), "file.txt", "111");
        assertEquals("111", getFirstLine(ipsProject.getResourceAsStream("file.txt")));

        IIpsProject referencedIpsProject = newIpsProject("referencedIpsProject");
        IIpsPackageFragmentRoot rootTwo = newIpsPackageFragmentRoot(referencedIpsProject, null, "rootTwo");
        createFileWithContent((AFolder)rootTwo.getCorrespondingResource(), "anotherFile.txt", "222");
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(referencedIpsProject);
        ipsProject.setIpsObjectPath(path);

        // "anotherFile.txt" can be retrieved via the original ipsProject.
        assertEquals("222", getFirstLine(ipsProject.getResourceAsStream("anotherFile.txt")));
    }

    @Test
    public void testDelete() {
        ipsProject.delete();

        assertFalse(root.exists());
        assertFalse(ipsProject.exists());
    }

    private boolean containsIpsSrcFile(IIpsSrcFile[] result, IIpsSrcFile policyCmptType) {
        for (IIpsSrcFile element : result) {
            if (element.getIpsObject().equals(policyCmptType.getIpsObject())) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testFormulaLanguageLocale() {
        assertEquals(Locale.GERMAN, ipsProject.getFormulaLanguageLocale());
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setFormulaLanguageLocale(Locale.ENGLISH);
        ipsProject.setProperties(properties);
        assertEquals(Locale.ENGLISH, ipsProject.getFormulaLanguageLocale());
    }

    @Test
    public void testFindAllIpsSrcFiles_allTypes() throws Exception {
        IPolicyCmptType a = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.c.A");

        List<IIpsSrcFile> allIpsSrcFiles = ipsProject.findAllIpsSrcFiles();

        assertEquals(1, allIpsSrcFiles.size());
        assertTrue(allIpsSrcFiles.contains(a.getIpsSrcFile()));
    }

    @Test
    public void testFindAllIpsSrcFiles_oneIpsObjectType() throws Exception {
        IPolicyCmptType a = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.c.A");

        List<IIpsSrcFile> allIpsSrcFiles = ipsProject.findAllIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);

        assertEquals(1, allIpsSrcFiles.size());
        assertTrue(allIpsSrcFiles.contains(a.getIpsSrcFile()));
    }

    @Test
    public void testFindAllIpsSrcFiles_wrongIpsObjectType() throws Exception {
        newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.c.A");

        List<IIpsSrcFile> allIpsSrcFiles = ipsProject.findAllIpsSrcFiles(IpsObjectType.PRODUCT_CMPT);

        assertTrue(allIpsSrcFiles.isEmpty());
    }

    @Test
    public void testGetVersionProvider() throws Exception {

        IVersionProvider<?> versionProvider = ipsProject.getVersionProvider();

        assertThat(versionProvider, instanceOf(DefaultVersionProvider.class));
    }

    @Test
    public void testValidateMarkerEnums_False() {
        EnumType enumType = initProjectProperty("Enum; falseEnum");
        Set<IIpsSrcFile> markerEnums = ipsProject.getMarkerEnums();
        MessageList msgList = ipsProject.validate();

        assertEquals(1, markerEnums.size());
        assertTrue(markerEnums.contains(enumType.getIpsSrcFile()));
        assertFalse(msgList.isEmpty());
        assertEquals(getExpectedMsgForUnknownMarkerEnums(), msgList.getMessage(0));
    }

    private Object getExpectedMsgForUnknownMarkerEnums() {
        return new Message(IIpsProjectProperties.MSGCODE_INVALID_MARKER_ENUMS,
                Messages.IpsProjectProperties_unknownMarkerEnums, Message.ERROR,
                ipsProject.getIpsProjectPropertiesFile());
    }

    @Test
    public void testValidateMarkerEnums_True() {
        EnumType enumType = initProjectProperty("Enum");
        Set<IIpsSrcFile> markerEnums = ipsProject.getMarkerEnums();
        MessageList msgList = ipsProject.validate();

        assertEquals(1, markerEnums.size());
        assertTrue(markerEnums.contains(enumType.getIpsSrcFile()));
        assertTrue(msgList.isEmpty());
    }

    private EnumType initProjectProperty(String markerString) {
        EnumType enumType = newEnumType(ipsProject, "Enum");
        IIpsProjectProperties ipsProjectProperties = ipsProject.getProperties();
        ipsProjectProperties.addMarkerEnum(markerString);
        ipsProject.setProperties(ipsProjectProperties);
        return enumType;
    }

    @Test
    public void testValidateMarkerEnums_isExtensible() {
        EnumType enumType = initProjectProperty("Enum");
        enumType.setExtensible(true);
        Set<IIpsSrcFile> markerEnums = ipsProject.getMarkerEnums();
        MessageList msgList = ipsProject.validate();

        assertEquals(1, markerEnums.size());
        assertTrue(markerEnums.contains(enumType.getIpsSrcFile()));
        assertFalse(msgList.isEmpty());
        assertEquals(getExpectedMsgForExtensibleMarkerEnums(enumType), msgList.getMessage(0));
    }

    private Message getExpectedMsgForExtensibleMarkerEnums(EnumType enumType) {
        String msg = NLS.bind(Messages.IpsProjectProperties_msgExtensibleMarkerEnumsNotAllowed,
                enumType.getQualifiedName());
        Message expectedMsg = new Message(IIpsProjectProperties.MSGCODE_INVALID_MARKER_ENUMS, msg, Message.ERROR,
                ipsProject.getIpsProjectPropertiesFile());
        return expectedMsg;
    }

    @Test
    public void testValidateMarkerEnums_isAbstract() {
        EnumType enumType = initProjectProperty("Enum");
        enumType.setAbstract(true);
        Set<IIpsSrcFile> markerEnums = ipsProject.getMarkerEnums();
        MessageList msgList = ipsProject.validate();

        assertEquals(1, markerEnums.size());
        assertTrue(markerEnums.contains(enumType.getIpsSrcFile()));
        assertFalse(msgList.isEmpty());
        assertEquals(getExpectedMsgForAbstractMarkerEnums(enumType), msgList.getMessage(0));
    }

    private Message getExpectedMsgForAbstractMarkerEnums(EnumType enumType) {
        String msg = NLS.bind(Messages.IpsProjectProperties_msgAbstractMarkerEnumsNotAllowed,
                enumType.getQualifiedName());
        Message expectedMsg = new Message(IIpsProjectProperties.MSGCODE_INVALID_MARKER_ENUMS, msg, Message.ERROR,
                ipsProject.getIpsProjectPropertiesFile());
        return expectedMsg;
    }

    @Test
    public void testGetMarkerEnums_DisableMarkerEnums() {
        newEnumType(ipsProject, "Enum");
        IIpsProjectProperties ipsProjectProperties = ipsProject.getProperties();
        ipsProjectProperties.addMarkerEnum("Enum");
        ipsProjectProperties.setMarkerEnumsEnabled(false);
        ipsProject.setProperties(ipsProjectProperties);

        Set<IIpsSrcFile> markerEnums = ipsProject.getMarkerEnums();

        assertTrue(markerEnums.isEmpty());
    }

    private void setPredefinedDatatypesUsed(IIpsProject project, String... datatypes) {
        IIpsProjectProperties props = project.getProperties();
        props.setPredefinedDatatypesUsed(datatypes);
        project.setProperties(props);
    }

    private void createProjectReference(IIpsProject from, IIpsProject to) {
        IIpsObjectPath path = from.getIpsObjectPath();
        path.newIpsProjectRefEntry(to);
        from.setIpsObjectPath(path);
    }

    @Test
    public void testFindAllProductTemplates_NoTemplateExists() {
        IProductCmptType baseType = newProductCmptType(ipsProject, "baseType");
        IProductCmptType subType = newProductCmptType(baseType, "subType");

        assertThat(ipsProject.findAllProductTemplates(baseType, true).isEmpty(), is(true));
        assertThat(ipsProject.findAllProductTemplates(baseType, false).isEmpty(), is(true));
        assertThat(ipsProject.findAllProductTemplates(subType, true).isEmpty(), is(true));
        assertThat(ipsProject.findAllProductTemplates(subType, false).isEmpty(), is(true));
    }

    @Test
    public void testFindAllProductTemplates_TemplatesExist() {
        IProductCmptType baseType = newProductCmptType(ipsProject, "baseType");
        IProductCmptType subType = newProductCmptType(baseType, "subType");
        IIpsSrcFile baseTemplate = newProductTemplate(baseType, "baseTemplate").getIpsSrcFile();
        IIpsSrcFile subTemplate = newProductTemplate(subType, "subTemplate").getIpsSrcFile();

        assertThat(ipsProject.findAllProductTemplates(baseType, true), hasItems(baseTemplate, subTemplate));
        assertThat(ipsProject.findAllProductTemplates(baseType, false), hasItem(baseTemplate));
        assertThat(ipsProject.findAllProductTemplates(subType, true), hasItems(subTemplate));
        assertThat(ipsProject.findAllProductTemplates(subType, false), hasItems(subTemplate));
    }

    @Test
    public void testFindAllProductTemplates_TemplateExistsInReferencedProject() {
        makeIpsProjectDependOnBaseProjectIndirect(true);

        IProductCmptType type = newProductCmptType(baseProject, "type");
        IIpsSrcFile template = newProductTemplate(type, "template").getIpsSrcFile();
        assertThat(ipsProject.findAllProductTemplates(type, false), hasItem(template));
    }

    @Test
    public void testFindTemplateHierarchy_EmptyHierarchy() {
        assertThat(ipsProject.findTemplateHierarchy(null).isEmpty(), is(true));
        assertThat(ipsProject.findTemplateHierarchy(newProductCmpt(baseProject, "p")).isEmpty(), is(true));
    }

    @Test
    public void testFindTemplateHierarchy_DirectReferences() {
        makeIpsProjectDependOnBaseProjectIndirect(true);
        IProductCmpt t1 = newProductTemplate(baseProject, "Template-1");
        IProductCmpt t2 = newProductTemplate(ipsProject, "Template-2");
        IProductCmpt p1 = newProductCmpt(baseProject, "Product-1");
        IProductCmpt p2 = newProductCmpt(ipsProject, "Product-2");
        IProductCmpt p3 = newProductCmpt(ipsProject, "Product-3");

        p1.setTemplate(t1.getQualifiedName());
        p2.setTemplate(t1.getQualifiedName());
        t2.setTemplate(t1.getQualifiedName());
        p3.setTemplate("other Template");

        Tree<IIpsSrcFile> t2Hierarchy = ipsProject.findTemplateHierarchy(t2);
        assertThat(t2Hierarchy.isEmpty(), is(false));
        assertThat(t2Hierarchy.getRoot().getElement(), is(t2.getIpsSrcFile()));
        assertThat(t2Hierarchy.getRoot().getChildren().isEmpty(), is(true));

        Tree<IIpsSrcFile> t1Hierarchy = ipsProject.findTemplateHierarchy(t1);
        assertThat(t1Hierarchy.isEmpty(), is(false));
        assertThat(t1Hierarchy.getRoot().getElement(), is(t1.getIpsSrcFile()));
        assertThat(t1Hierarchy.getRoot().getChildren().size(), is(3));

        assertThat(t1Hierarchy.getAllElements(), hasItems(p1.getIpsSrcFile(), p2.getIpsSrcFile(), t2.getIpsSrcFile()));
    }

    @Test
    public void testFindTemplateHierarchy_IndirectReference() {
        makeIpsProjectDependOnBaseProject();
        IProductCmpt t1 = newProductTemplate(baseProject, "Template-1");
        IProductCmpt t2 = newProductTemplate(baseProject, "Template-2");
        IProductCmpt t3 = newProductTemplate(ipsProject, "Template-3");
        IProductCmpt p = newProductCmpt(ipsProject, "Product");

        t2.setTemplate(t1.getQualifiedName());
        t3.setTemplate(t2.getQualifiedName());
        p.setTemplate(t3.getQualifiedName());

        Tree<IIpsSrcFile> hierarchy = baseProject.findTemplateHierarchy(t1);
        Node<IIpsSrcFile> root = hierarchy.getRoot();
        assertThat(root.getElement(), is(t1.getIpsSrcFile()));
        assertThat(root.getChildren().size(), is(1));

        Node<IIpsSrcFile> t2Node = root.getChildren().get(0);
        assertThat(t2Node.getElement(), is(t2.getIpsSrcFile()));
        assertThat(t2Node.getChildren().size(), is(1));

        Node<IIpsSrcFile> t3Node = t2Node.getChildren().get(0);
        assertThat(t3Node.getElement(), is(t3.getIpsSrcFile()));
        assertThat(t3Node.getChildren().size(), is(1));

        Node<IIpsSrcFile> pNode = t3Node.getChildren().get(0);
        assertThat(pNode.getElement(), is(p.getIpsSrcFile()));
        assertThat(pNode.getChildren().isEmpty(), is(true));
    }

    @Test
    public void testValidate_VersionProviderMissing() {
        MessageList list = ipsProject.validate();
        assertFalse(list.containsErrorMsg());

        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setVersionProviderId("foobar");
        try {
            ipsProject.setProperties(properties);

            list = ipsProject.validate();

            assertTrue(list.containsErrorMsg());
            assertNotNull(list.getMessageByCode(IIpsProjectProperties.MSGCODE_INVALID_VERSION_SETTING));
        } finally {
            properties.setVersionProviderId(null);
            ipsProject.setProperties(properties);
        }
    }

    @Test
    public void testValidate_VersionProviderSet() {
        MessageList list = ipsProject.validate();
        assertFalse(list.containsErrorMsg());

        IVersionProvider<?> versionProvider = mock(IVersionProvider.class);
        IVersionProviderFactory versionProviderFactory = $ -> versionProvider;
        IIpsProjectProperties properties = ipsProject.getProperties();
        try (TestIpsModelExtensions testIpsModelExtensions = TestIpsModelExtensions.get()) {
            Map<String, IVersionProviderFactory> versionProviderFactories = new HashMap<>();
            versionProviderFactories.put("foobar", versionProviderFactory);
            testIpsModelExtensions.setVersionProviderFactories(versionProviderFactories);
            properties.setVersionProviderId("foobar");
            ipsProject.setProperties(properties);

            list = ipsProject.validate();
            assertFalse(list.containsErrorMsg());
            assertThat(ipsProject.getVersionProvider(), is(versionProvider));
        } finally {
            properties.setVersionProviderId(null);
            ipsProject.setProperties(properties);
        }
    }

    class InvalidMigrationMockManager extends TestIpsFeatureVersionManager {

        @Override
        public AbstractIpsProjectMigrationOperation[] getMigrationOperations(IIpsProject projectToMigrate) {
            throw new UnsupportedOperationException();
        }
    }
}
