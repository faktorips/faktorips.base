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

package org.faktorips.devtools.core.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.builder.TestIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
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

        builder = new StubJavaSourceFileBuilder(builderSet, DefaultBuilderSet.KIND_POLICY_CMPT_TYPE_INTERFACE,
                new LocalizedStringsSet(JavaSourceFileBuilderTest.class), ipsSrcFile, false);
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
        IFile file = ipsProject.getIpsPackageFragmentRoots()[0].getArtefactDestination(false).getFile(
                "org/faktorips/sample/model/test/TestPolicy.java");
        assertTrue(file.exists());

        /*
         * this checks if the merge.xml has been found since it will try to merge the content
         * because the java file exists already
         */
        spyBuilder.setMergeEnabled(true);
        reset(spyBuilder);
        spyBuilder.build(ipsSrcFile);
    }

    @Test
    public void testMarkGeneratedResourceAsDerived() throws Exception {
        JavaSourceFileBuilder spyBuilder = spy(builder);

        TestIpsArtefactBuilderSet builderSet = (TestIpsArtefactBuilderSet)spyBuilder.getBuilderSet();
        Map<String, Object> properties = builderSet.getConfig().getProperties();
        properties.put(AbstractBuilderSet.CONFIG_MARK_NONE_MERGEABLE_RESOURCES_AS_DERIVED, true);

        spyBuilder.build(ipsSrcFile);

        IFile file = ipsProject.getIpsPackageFragmentRoots()[0].getArtefactDestination(false).getFile(
                "org/faktorips/sample/model/test/TestPolicy.java");
        assertTrue(file.exists());
        assertFalse(file.isDerived());
        assertFalse(file.getParent().isDerived());

        spyBuilder.delete(ipsSrcFile);
        reset(spyBuilder);

        doReturn(true).when(spyBuilder).buildsDerivedArtefacts();
        spyBuilder.build(ipsSrcFile);
        file = ipsProject.getIpsPackageFragmentRoots()[0].getArtefactDestination(true).getFile(
                "org/faktorips/sample/model/test/TestPolicy.java");
        assertTrue(file.exists());
        assertTrue(file.isDerived());
        assertTrue(file.getParent().isDerived());

        spyBuilder.delete(ipsSrcFile);
        reset(spyBuilder);

        properties.put(AbstractBuilderSet.CONFIG_MARK_NONE_MERGEABLE_RESOURCES_AS_DERIVED, false);
        spyBuilder.build(ipsSrcFile);

        file = ipsProject.getIpsPackageFragmentRoots()[0].getArtefactDestination(false).getFile(
                "org/faktorips/sample/model/test/TestPolicy.java");
        assertTrue(file.exists());
        assertFalse(file.isDerived());
        assertFalse(file.getParent().isDerived());

        spyBuilder.delete(ipsSrcFile);
        reset(spyBuilder);
        doReturn(true).when(spyBuilder).buildsDerivedArtefacts();
        spyBuilder.build(ipsSrcFile);
        file = ipsProject.getIpsPackageFragmentRoots()[0].getArtefactDestination(true).getFile(
                "org/faktorips/sample/model/test/TestPolicy.java");
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
        IFile file = ipsProject.getIpsPackageFragmentRoots()[0].getArtefactDestination(false).getFile(
                "org/faktorips/sample/model/test/TestPolicy.java");
        assertTrue(file.exists());

        // check file deletion
        builder.delete(ipsSrcFile);
        file = ipsProject.getIpsPackageFragmentRoots()[0].getArtefactDestination(false).getFile(
                "org/faktorips/sample/model/test/TestPolicy.java");
        assertFalse(file.exists());
    }

    @Test
    public void testGetLocalizedText() throws Exception {
        builder.beforeBuild(ipsSrcFile, null);
        String value = builder.getLocalizedText(ipsSrcFile, "key");
        assertNotNull(value);
        builder.afterBuild(ipsSrcFile);
    }

    public static class StubJavaSourceFileBuilder extends JavaSourceFileBuilder {

        private final IIpsSrcFile ipsSrcFile;

        private final boolean buildingPublishedSourceFile;

        public StubJavaSourceFileBuilder(IIpsSrcFile ipsSrcFile, boolean buildingPublishedSourceFile) {
            this(mock(DefaultBuilderSet.class), "", null, ipsSrcFile, buildingPublishedSourceFile);
        }

        public StubJavaSourceFileBuilder(DefaultBuilderSet builderSet, String kindId,
                LocalizedStringsSet localizedStringsSet, IIpsSrcFile ipsSrcFile, boolean buildingPublishedSourceFile) {

            super(builderSet, kindId, localizedStringsSet);
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

    }

}
