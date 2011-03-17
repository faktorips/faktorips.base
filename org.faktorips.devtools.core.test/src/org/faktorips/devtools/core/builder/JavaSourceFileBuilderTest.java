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

package org.faktorips.devtools.core.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.builder.TestIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.LocalizedStringsSet;
import org.junit.Before;
import org.junit.Test;

public class JavaSourceFileBuilderTest extends AbstractIpsPluginTest {

    private StubJavaSourceFileBuilder builder;

    private IIpsProject ipsProject;

    private IIpsSrcFile ipsSrcFile;

    private IIpsObject ipsObject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject();
        ipsSrcFile = newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy").getIpsSrcFile();
        ipsObject = ipsSrcFile.getIpsObject();

        TestIpsArtefactBuilderSet builderSet = new TestIpsArtefactBuilderSet();
        builderSet.setIpsProject(ipsProject);

        builder = new StubJavaSourceFileBuilder(builderSet, "dummy", new LocalizedStringsSet(
                JavaSourceFileBuilderTest.class), ipsSrcFile, false);
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
        IFile file = ipsProject.getIpsPackageFragmentRoots()[0].getArtefactDestination(false)
                .getFile("TestPolicy.java");
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
    public void testDelete() throws Exception {
        builder.beforeBuild(ipsSrcFile, null);
        builder.build(ipsSrcFile);
        builder.afterBuild(ipsSrcFile);
        // check file creation
        IFile file = ipsProject.getIpsPackageFragmentRoots()[0].getArtefactDestination(false)
                .getFile("TestPolicy.java");
        assertTrue(file.exists());

        // check file deletion
        builder.delete(ipsSrcFile);
        file = ipsProject.getIpsPackageFragmentRoots()[0].getArtefactDestination(false).getFile("TestPolicy.java");
        assertFalse(file.exists());
    }

    @Test
    public void testGetLocalizedText() throws Exception {
        builder.beforeBuild(ipsSrcFile, null);
        String value = builder.getLocalizedText(ipsSrcFile, "key");
        assertNotNull(value);
        builder.afterBuild(ipsSrcFile);
    }

    @Test
    public void testGetGeneratedJavaImplementationType() {
        StubJavaSourceFileBuilder builder = new StubJavaSourceFileBuilder(ipsSrcFile, false);
        IType mockJavaType = mock(IType.class);
        builder.setGeneratedJavaImplementationType(mockJavaType);

        IType generatedJavaImplementationType = builder.getGeneratedJavaImplementationType(ipsObject);

        assertEquals(mockJavaType, generatedJavaImplementationType);
    }

    @Test(expected = NullPointerException.class)
    public void testGetGeneratedJavaImplementationTypeNullPointer() {
        StubJavaSourceFileBuilder builder = new StubJavaSourceFileBuilder(null, false);
        builder.getGeneratedJavaImplementationType(null);
    }

    @Test
    public void testGetGeneratedJavaImplementationTypeBuildingPublishedSourceFile() {
        StubJavaSourceFileBuilder builder = new StubJavaSourceFileBuilder(null, true);
        builder.setGeneratedJavaImplementationType(mock(IType.class));

        IType generatedJavaImplementationType = builder.getGeneratedJavaImplementationType(ipsObject);

        assertNull(generatedJavaImplementationType);
    }

    @Test
    public void testGetGeneratedJavaImplementationTypeNotABuilderForGivenIpsObject() {
        StubJavaSourceFileBuilder builder = new StubJavaSourceFileBuilder(mock(IIpsSrcFile.class), false);
        builder.setGeneratedJavaImplementationType(mock(IType.class));

        IType generatedJavaImplementationType = builder.getGeneratedJavaImplementationType(ipsObject);

        assertNull(generatedJavaImplementationType);
    }

    public static class StubJavaSourceFileBuilder extends JavaSourceFileBuilder {

        private IIpsSrcFile ipsSrcFile;

        private boolean buildingPublishedSourceFile;

        private IType generatedJavaImplementationType;

        public StubJavaSourceFileBuilder(IIpsSrcFile ipsSrcFile, boolean buildingPublishedSourceFile) {
            this(mock(IIpsArtefactBuilderSet.class), "", null, ipsSrcFile, buildingPublishedSourceFile);
        }

        public StubJavaSourceFileBuilder(IIpsArtefactBuilderSet builderSet, String kindId,
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
        protected IType getGeneratedJavaImplementationTypeThis() {
            return generatedJavaImplementationType;
        }

        @Override
        public boolean isBuildingPublishedSourceFile() {
            return buildingPublishedSourceFile;
        }

        public void setIpsSrcFile(IIpsSrcFile ipsSrcFile) {
            this.ipsSrcFile = ipsSrcFile;
        }

        public void setBuildingPublishedSourceFile(boolean buildingPublishedSourceFile) {
            this.buildingPublishedSourceFile = buildingPublishedSourceFile;
        }

        public void setGeneratedJavaImplementationType(IType generatedJavaImplementationType) {
            this.generatedJavaImplementationType = generatedJavaImplementationType;
        }

    }

}
