/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.builder.TestIpsArtefactBuilderSet;
import org.faktorips.devtools.model.builder.AbstractBuilderSet;
import org.faktorips.devtools.model.builder.DefaultBuilderSet;
import org.faktorips.devtools.model.internal.builder.JavaNamingConvention;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.util.LocalizedStringsSet;
import org.junit.Before;
import org.junit.Test;

public class JavaSourceFileBuilderTest extends AbstractIpsPluginTest {

    private StubJavaSourceFileBuilder builder;

    private IIpsProject ipsProject;

    private IIpsSrcFile ipsSrcFile;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject();
        IIpsPackageFragment pack = ipsProject.getIpsPackageFragmentRoots()[0].createPackageFragment("test", true, null);
        ipsSrcFile = newIpsObject(pack, IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy").getIpsSrcFile();

        TestIpsArtefactBuilderSet builderSet = new TestIpsArtefactBuilderSet();
        builderSet.setIpsProject(ipsProject);

        builder = new StubJavaSourceFileBuilder(builderSet, new LocalizedStringsSet(JavaSourceFileBuilderTest.class),
                ipsSrcFile, true);
        builder.beforeBuildProcess(ipsProject, IncrementalProjectBuilder.INCREMENTAL_BUILD);
    }

    @Test
    public void testBeforeBuild() throws Exception {
        builder.beforeBuild(ipsSrcFile, null);
        assertEquals(ipsSrcFile, builder.getIpsSrcFile());
    }

    @Test
    public void testAfterBuild() throws Exception {
        builder.beforeBuild(ipsSrcFile, null);
        builder.afterBuild(ipsSrcFile);
        assertNull(builder.getIpsObject());
    }

    @Test
    public void testBuild() throws Exception {
        JavaSourceFileBuilder spyBuilder = spy(builder);

        spyBuilder.beforeBuild(ipsSrcFile, null);
        spyBuilder.build(ipsSrcFile);
        spyBuilder.afterBuild(ipsSrcFile);
        verify(spyBuilder).generate();

        reset(spyBuilder);
        spyBuilder.build(ipsSrcFile);
        verify(spyBuilder).generate();

        // check file creation
        IFile file = getFile("org/faktorips/sample/model/test/TestPolicy.java", false);

        assertTrue(file.exists());

        /*
         * this checks if the merge.xml has been found since it will try to merge the content
         * because the java file exists already
         */
        spyBuilder.setMergeEnabled(true);
        reset(spyBuilder);
        spyBuilder.build(ipsSrcFile);
    }

    private IFile getFile(String name, boolean derived) throws CoreException {
        IPackageFragmentRoot artefactDestination = ipsProject.getIpsPackageFragmentRoots()[0]
                .getArtefactDestination(derived);
        IFolder folder = (IFolder)artefactDestination.getResource();
        IFile file = folder.getFile(name);
        return file;
    }

    @Test
    public void testMarkGeneratedResourceAsDerived() throws Exception {
        JavaSourceFileBuilder spyBuilder = spy(builder);

        TestIpsArtefactBuilderSet builderSet = (TestIpsArtefactBuilderSet)spyBuilder.getBuilderSet();
        Map<String, Object> properties = builderSet.getConfig().getProperties();
        properties.put(AbstractBuilderSet.CONFIG_MARK_NONE_MERGEABLE_RESOURCES_AS_DERIVED, true);

        spyBuilder.build(ipsSrcFile);

        IFile file = getFile("org/faktorips/sample/model/test/TestPolicy.java", false);
        assertTrue(file.exists());
        assertFalse(file.isDerived());
        assertFalse(file.getParent().isDerived());

        spyBuilder.delete(ipsSrcFile);
        reset(spyBuilder);

        doReturn(true).when(spyBuilder).buildsDerivedArtefacts();
        spyBuilder.build(ipsSrcFile);
        file = getFile("org/faktorips/sample/model/test/TestPolicy.java", true);
        assertTrue(file.exists());
        assertTrue(file.isDerived());
        assertTrue(file.getParent().isDerived());

        spyBuilder.delete(ipsSrcFile);
        reset(spyBuilder);

        properties.put(AbstractBuilderSet.CONFIG_MARK_NONE_MERGEABLE_RESOURCES_AS_DERIVED, false);
        spyBuilder.build(ipsSrcFile);

        file = getFile("org/faktorips/sample/model/test/TestPolicy.java", false);
        assertTrue(file.exists());
        assertFalse(file.isDerived());
        assertFalse(file.getParent().isDerived());

        spyBuilder.delete(ipsSrcFile);
        reset(spyBuilder);
        doReturn(true).when(spyBuilder).buildsDerivedArtefacts();
        spyBuilder.build(ipsSrcFile);
        file = getFile("org/faktorips/sample/model/test/TestPolicy.java", true);
        assertTrue(file.exists());
        assertFalse(file.isDerived());
        assertFalse(file.getParent().isDerived());
    }

    @Test
    public void testDelete() throws Exception {
        builder.beforeBuild(ipsSrcFile, null);
        builder.build(ipsSrcFile);
        builder.afterBuild(ipsSrcFile);
        // check file creation
        IFile file = getFile("org/faktorips/sample/model/test/TestPolicy.java", false);
        assertTrue(file.exists());

        // check file deletion
        builder.delete(ipsSrcFile);
        file = getFile("org/faktorips/sample/model/test/TestPolicy.java", false);
        assertFalse(file.exists());
    }

    @Test
    public void testGetLocalizedText() throws Exception {
        builder.beforeBuild(ipsSrcFile, null);
        String value = builder.getLocalizedText("key");
        assertNotNull(value);
        builder.afterBuild(ipsSrcFile);
    }

    @Test
    public void testGetQualifiedClassName() throws CoreException {
        builder = new StubJavaSourceFileBuilder(new TestIpsArtefactBuilderSet(),
                new LocalizedStringsSet(JavaSourceFileBuilderTest.class), ipsSrcFile, false);
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(ipsProject.getJavaNamingConvention()).thenReturn(new JavaNamingConvention());
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        IIpsPackageFragment packageFragment = mock(IIpsPackageFragment.class);
        when(packageFragment.getName()).thenReturn("test");
        when(ipsSrcFile.getBasePackageNameForMergableArtefacts()).thenReturn("org.merge");
        when(ipsSrcFile.getIpsPackageFragment()).thenReturn(packageFragment);
        when(ipsSrcFile.getIpsProject()).thenReturn(ipsProject);
        when(ipsSrcFile.getIpsObjectName()).thenReturn("myTable");

        String className = builder.getQualifiedClassName(ipsSrcFile);

        assertEquals("org.merge.internal.test.myTable", className);
    }

    @Test
    public void testGetQualifiedClassName_noPublishedInterfaces() throws CoreException {
        TestIpsArtefactBuilderSet standardBuilderSetSpy = spy(new TestIpsArtefactBuilderSet());
        doReturn(false).when(standardBuilderSetSpy).isGeneratePublishedInterfaces();
        builder = new StubJavaSourceFileBuilder(standardBuilderSetSpy,
                new LocalizedStringsSet(JavaSourceFileBuilderTest.class), ipsSrcFile, false);
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(ipsProject.getJavaNamingConvention()).thenReturn(new JavaNamingConvention());
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        IIpsPackageFragment packageFragment = mock(IIpsPackageFragment.class);
        when(packageFragment.getName()).thenReturn("test");
        when(ipsSrcFile.getBasePackageNameForMergableArtefacts()).thenReturn("org.merge");
        when(ipsSrcFile.getIpsPackageFragment()).thenReturn(packageFragment);
        when(ipsSrcFile.getIpsProject()).thenReturn(ipsProject);
        when(ipsSrcFile.getIpsObjectName()).thenReturn("myTable");

        String className = builder.getQualifiedClassName(ipsSrcFile);

        assertEquals("org.merge.test.myTable", className);
    }

    @Test
    public void testAdditionalAnnotations() throws CoreException {
        TestIpsArtefactBuilderSet builderSet = new TestIpsArtefactBuilderSet() {
            @Override
            protected String getConfiguredAdditionalAnnotations() {
                return "foo.bar.Baz";
            }
        };
        builderSet.setIpsProject(ipsProject);
        builderSet.beforeBuildProcess(IncrementalProjectBuilder.INCREMENTAL_BUILD);

        builder = new StubJavaSourceFileBuilder(builderSet, new LocalizedStringsSet(JavaSourceFileBuilderTest.class),
                ipsSrcFile, true) {
            @Override
            protected String generate() throws CoreException {
                //@formatter:off
                return "/**\n" +
                        " * @generated\n" +
                        " */\n" +
                        "public class TestPolicy {\n" +
                        "\n" +
                        "    /**\n" +
                        "     * @generated\n" +
                        "     */\n" +
                        "    public void foo(){\n" +
                        "    }\n" +
                        "}";
                //@formatter:on
            }
        };
        builder.beforeBuildProcess(ipsProject, IncrementalProjectBuilder.INCREMENTAL_BUILD);
        builder.setMergeEnabled(true);

        builder.build(ipsSrcFile);

        IFile file = getFile("org/faktorips/sample/model/test/TestPolicy.java", false);
        String javaFileContents = builder.getJavaFileContents(file, ipsProject.getProject().getDefaultCharset());

        assertThat(javaFileContents, containsString("import foo.bar.Baz;"));
        assertThat(javaFileContents, containsString("@Baz"));
    }

    @Test
    public void testRetainedAnnotations() throws CoreException {
        TestIpsArtefactBuilderSet builderSet = new TestIpsArtefactBuilderSet() {
            @Override
            protected String getConfiguredRetainedAnnotations() {
                return "Baz";
            }
        };
        builderSet.setIpsProject(ipsProject);
        builderSet.beforeBuildProcess(IncrementalProjectBuilder.INCREMENTAL_BUILD);
        IFile file = getFile("org/faktorips/sample/model/test/TestPolicy.java", false);

        builder = new StubJavaSourceFileBuilder(builderSet, new LocalizedStringsSet(JavaSourceFileBuilderTest.class),
                ipsSrcFile, true) {
            @Override
            protected String generate() throws CoreException {
                //@formatter:off
                return "/**\n" +
                        " * @generated\n" +
                        " */\n" +
                        "public class TestPolicy {\n" +
                        "\n" +
                        "    /**\n" +
                        "     * @generated\n" +
                        "     */\n" +
                        "    public void foo(){\n" +
                        "    }\n" +
                        "}";
                //@formatter:on
            }
        };
        builder.createFileIfNotThere(file);
        //@formatter:off
        builder.writeToFile(ipsSrcFile, file,
                "import foo.bar.Baz;\n" +
                "\n" +
                "/**\n" +
                " * @generated\n" +
                " */\n" +
                "@Baz\n" +
                "public class TestPolicy {\n" +
                "\n" +
                "    /**\n" +
                "     * @generated\n" +
                "     */\n" +
                "    @Baz\n" +
                "    public void foo(){\n" +
                "    }\n" +
                "}");
        //@formatter:on
        builder.beforeBuildProcess(ipsProject, IncrementalProjectBuilder.INCREMENTAL_BUILD);
        builder.setMergeEnabled(true);

        builder.build(ipsSrcFile);

        String javaFileContents = builder.getJavaFileContents(file, ipsProject.getProject().getDefaultCharset());

        assertThat(javaFileContents, containsString("import foo.bar.Baz;"));
        assertThat(javaFileContents, containsString("@Baz"));
    }

    @Test
    public void testRetainedAnnotationsWithAt() throws CoreException {
        TestIpsArtefactBuilderSet builderSet = new TestIpsArtefactBuilderSet() {
            @Override
            protected String getConfiguredRetainedAnnotations() {
                return "@Baz";
            }
        };
        builderSet.setIpsProject(ipsProject);
        builderSet.beforeBuildProcess(IncrementalProjectBuilder.INCREMENTAL_BUILD);
        IFile file = getFile("org/faktorips/sample/model/test/TestPolicy.java", false);

        builder = new StubJavaSourceFileBuilder(builderSet, new LocalizedStringsSet(JavaSourceFileBuilderTest.class),
                ipsSrcFile, true) {
            @Override
            protected String generate() throws CoreException {
                //@formatter:off
                return "/**\n" +
                        " * @generated\n" +
                        " */\n" +
                        "public class TestPolicy {\n" +
                        "\n" +
                        "    /**\n" +
                        "     * @generated\n" +
                        "     */\n" +
                        "    public void foo(){\n" +
                        "    }\n" +
                        "}";
                //@formatter:on
            }
        };
        builder.createFileIfNotThere(file);
        //@formatter:off
        builder.writeToFile(ipsSrcFile, file,
                "import foo.bar.Baz;\n" +
                "\n" +
                "/**\n" +
                " * @generated\n" +
                " */\n" +
                "@Baz\n" +
                "public class TestPolicy {\n" +
                "\n" +
                "    /**\n" +
                "     * @generated\n" +
                "     */\n" +
                "    @Baz\n" +
                "    public void foo(){\n" +
                "    }\n" +
                "}");
        //@formatter:on
        builder.beforeBuildProcess(ipsProject, IncrementalProjectBuilder.INCREMENTAL_BUILD);
        builder.setMergeEnabled(true);

        builder.build(ipsSrcFile);

        String javaFileContents = builder.getJavaFileContents(file, ipsProject.getProject().getDefaultCharset());

        assertThat(javaFileContents, containsString("import foo.bar.Baz;"));
        assertThat(javaFileContents, containsString("@Baz"));
    }

    @Test
    public void testRetainedAnnotationsQualified() throws CoreException {
        TestIpsArtefactBuilderSet builderSet = new TestIpsArtefactBuilderSet() {
            @Override
            protected String getConfiguredRetainedAnnotations() {
                return "foo.bar.Baz";
            }
        };
        builderSet.setIpsProject(ipsProject);
        builderSet.beforeBuildProcess(IncrementalProjectBuilder.INCREMENTAL_BUILD);
        IFile file = getFile("org/faktorips/sample/model/test/TestPolicy.java", false);

        builder = new StubJavaSourceFileBuilder(builderSet, new LocalizedStringsSet(JavaSourceFileBuilderTest.class),
                ipsSrcFile, true) {
            @Override
            protected String generate() throws CoreException {
                //@formatter:off
                return "/**\n" +
                        " * @generated\n" +
                        " */\n" +
                        "public class TestPolicy {\n" +
                        "\n" +
                        "    /**\n" +
                        "     * @generated\n" +
                        "     */\n" +
                        "    public void foo(){\n" +
                        "    }\n" +
                        "}";
                //@formatter:on
            }
        };
        builder.createFileIfNotThere(file);
        //@formatter:off
        builder.writeToFile(ipsSrcFile, file,
                "import foo.bar.Baz;\n" +
                "\n" +
                "/**\n" +
                " * @generated\n" +
                " */\n" +
                "@foo.bar.Baz\n" +
                "public class TestPolicy {\n" +
                "\n" +
                "    /**\n" +
                "     * @generated\n" +
                "     */\n" +
                "    @Baz\n" +
                "    public void foo(){\n" +
                "    }\n" +
                "}");
        //@formatter:on
        builder.beforeBuildProcess(ipsProject, IncrementalProjectBuilder.INCREMENTAL_BUILD);
        builder.setMergeEnabled(true);

        builder.build(ipsSrcFile);

        String javaFileContents = builder.getJavaFileContents(file, ipsProject.getProject().getDefaultCharset());

        assertThat(javaFileContents, not(containsString("import foo.bar.Baz;")));
        assertThat(javaFileContents, not(containsString("@Baz")));
    }

    @Test
    public void testRetainedAnnotationsQualifiedVsUnqualified() throws CoreException {
        TestIpsArtefactBuilderSet builderSet = new TestIpsArtefactBuilderSet() {
            @Override
            protected String getConfiguredRetainedAnnotations() {
                return "foo.bar.Baz";
            }
        };
        builderSet.setIpsProject(ipsProject);
        builderSet.beforeBuildProcess(IncrementalProjectBuilder.INCREMENTAL_BUILD);
        IFile file = getFile("org/faktorips/sample/model/test/TestPolicy.java", false);

        builder = new StubJavaSourceFileBuilder(builderSet, new LocalizedStringsSet(JavaSourceFileBuilderTest.class),
                ipsSrcFile, true) {
            @Override
            protected String generate() throws CoreException {
                //@formatter:off
                return "/**\n" +
                        " * @generated\n" +
                        " */\n" +
                        "public class TestPolicy {\n" +
                        "\n" +
                        "    /**\n" +
                        "     * @generated\n" +
                        "     */\n" +
                        "    public void foo(){\n" +
                        "    }\n" +
                        "}";
                //@formatter:on
            }
        };
        builder.createFileIfNotThere(file);
        //@formatter:off
        builder.writeToFile(ipsSrcFile, file,
                "import foo.bar.Baz;\n" +
                "\n" +
                "/**\n" +
                " * @generated\n" +
                " */\n" +
                "@Baz\n" +
                "public class TestPolicy {\n" +
                "\n" +
                "    /**\n" +
                "     * @generated\n" +
                "     */\n" +
                "    @Baz\n" +
                "    public void foo(){\n" +
                "    }\n" +
                "}");
        //@formatter:on
        builder.beforeBuildProcess(ipsProject, IncrementalProjectBuilder.INCREMENTAL_BUILD);
        builder.setMergeEnabled(true);

        builder.build(ipsSrcFile);

        String javaFileContents = builder.getJavaFileContents(file, ipsProject.getProject().getDefaultCharset());

        assertThat(javaFileContents, not(containsString("import foo.bar.Baz;")));
        assertThat(javaFileContents, not(containsString("@Baz")));
    }

    @Test
    public void testLinebreaks_NewFile_Windows() throws CoreException {
        TestIpsArtefactBuilderSet builderSet = new TestIpsArtefactBuilderSet();
        builderSet.setIpsProject(ipsProject);
        builderSet.beforeBuildProcess(IncrementalProjectBuilder.INCREMENTAL_BUILD);
        IFile file = getFile("org/faktorips/sample/model/test/TestPolicy.java", false);

        builder = new StubJavaSourceFileBuilder(builderSet, new LocalizedStringsSet(JavaSourceFileBuilderTest.class),
                ipsSrcFile, true) {
            @Override
            protected String generate() throws CoreException {
                //@formatter:off
                return "/**\n" +
                        " * @generated\n" +
                        " */\n" +
                        "public class TestPolicy {\n" +
                        "\n" +
                        "    /**\n" +
                        "     * @generated\n" +
                        "     */\n" +
                        "    public void foo(){\n" +
                        "    }\n" +
                        "}";
                //@formatter:on
            }
        };

        builder.setLineSeparatorPreference("\r\n");
        builder.beforeBuildProcess(ipsProject, IncrementalProjectBuilder.INCREMENTAL_BUILD);
        builder.setMergeEnabled(true);
        builder.build(ipsSrcFile);

        String javaFileContents = builder.getJavaFileContents(file, ipsProject.getProject().getDefaultCharset());
        assertThat(javaFileContents, containsString("public void foo() {\r\n"));
        assertThat(javaFileContents, not(containsString("public void foo() {\n")));
        assertThat(javaFileContents, containsString("public class TestPolicy {\r\n"));
        assertThat(javaFileContents, not(containsString("public class TestPolicy {\n")));
    }

    @Test
    public void testLinebreaks_NewFile_Linux() throws CoreException {
        TestIpsArtefactBuilderSet builderSet = new TestIpsArtefactBuilderSet();
        builderSet.setIpsProject(ipsProject);
        builderSet.beforeBuildProcess(IncrementalProjectBuilder.INCREMENTAL_BUILD);
        IFile file = getFile("org/faktorips/sample/model/test/TestPolicy.java", false);

        builder = new StubJavaSourceFileBuilder(builderSet, new LocalizedStringsSet(JavaSourceFileBuilderTest.class),
                ipsSrcFile, true) {
            @Override
            protected String generate() throws CoreException {
                //@formatter:off
                return "/**\r\n" +
                        " * @generated\r\n" +
                        " */\r\n" +
                        "public class TestPolicy {\r\n" +
                        "\r\n" +
                        "    /**\r\n" +
                        "     * @generated\r\n" +
                        "     */\r\n" +
                        "    public void foo(){\r\n" +
                        "    }\r\n" +
                        "}";
                //@formatter:on
            }
        };

        builder.setLineSeparatorPreference("\n");
        builder.beforeBuildProcess(ipsProject, IncrementalProjectBuilder.INCREMENTAL_BUILD);
        builder.setMergeEnabled(true);
        builder.build(ipsSrcFile);

        String javaFileContents = builder.getJavaFileContents(file, ipsProject.getProject().getDefaultCharset());
        assertThat(javaFileContents, containsString("public void foo() {\n"));
        assertThat(javaFileContents, not(containsString("public void foo() {\r\n")));
        assertThat(javaFileContents, containsString("public class TestPolicy {\n"));
        assertThat(javaFileContents, not(containsString("public class TestPolicy {\r\n")));
    }

    public static class StubJavaSourceFileBuilder extends JavaSourceFileBuilder {

        private final IIpsSrcFile ipsSrcFile;

        private final boolean buildingPublishedSourceFile;

        private String lineSeparator = "\n";

        public StubJavaSourceFileBuilder(IIpsSrcFile ipsSrcFile, boolean buildingPublishedSourceFile) {
            this(mock(DefaultBuilderSet.class), null, ipsSrcFile, buildingPublishedSourceFile);
        }

        public StubJavaSourceFileBuilder(DefaultBuilderSet builderSet, LocalizedStringsSet localizedStringsSet,
                IIpsSrcFile ipsSrcFile, boolean buildingPublishedSourceFile) {
            super(builderSet, localizedStringsSet);
            this.ipsSrcFile = ipsSrcFile;
            this.buildingPublishedSourceFile = buildingPublishedSourceFile;
        }

        @Override
        public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
            return this.ipsSrcFile.equals(ipsSrcFile);
        }

        @Override
        protected String generate() throws CoreException {
            return "";
        }

        @Override
        protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements,
                IIpsObjectPartContainer ipsObjectPartContainer) {

        }

        @Override
        public boolean isBuildingPublishedSourceFile() {
            return buildingPublishedSourceFile;
        }

        @Override
        protected boolean generatesInterface() {
            return false;
        }

        @Override
        protected String getLineSeparatorPreference() {
            return lineSeparator;
        }

        public void setLineSeparatorPreference(String lineSeparator) {
            this.lineSeparator = lineSeparator;
        }

        @Override
        protected boolean createFileIfNotThere(IFile file) throws CoreException {
            return super.createFileIfNotThere(file);
        }

    }
}
