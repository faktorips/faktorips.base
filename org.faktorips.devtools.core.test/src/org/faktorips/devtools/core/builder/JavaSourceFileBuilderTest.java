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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.builder.DumyJavaSourceFileBuilder;
import org.faktorips.abstracttest.builder.TestIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.LocalizedStringsSet;
import org.junit.Before;
import org.junit.Test;

public class JavaSourceFileBuilderTest extends AbstractIpsPluginTest {

    private DumyJavaSourceFileBuilder builder;
    private IIpsProject project;
    private IIpsSrcFile ipsSrcFile;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject();
        ipsSrcFile = newIpsObject(project, IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy").getIpsSrcFile();
        TestIpsArtefactBuilderSet builderSet = new TestIpsArtefactBuilderSet();
        builderSet.setIpsProject(project);
        builder = new DumyJavaSourceFileBuilder(builderSet, "dumy", new LocalizedStringsSet(
                JavaSourceFileBuilderTest.class));
        builder.beforeBuildProcess(project, IncrementalProjectBuilder.INCREMENTAL_BUILD);
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
        builder.beforeBuild(ipsSrcFile, null);
        builder.isBuilderFor = true;
        builder.build(ipsSrcFile);
        builder.afterBuild(ipsSrcFile);
        assertTrue(builder.generateCalled);

        builder.reset();
        builder.build(ipsSrcFile);
        assertFalse(builder.generateCalled);

        // check file creation
        IFile file = project.getIpsPackageFragmentRoots()[0].getArtefactDestination(false).getFile("TestPolicy.java");
        assertTrue(file.exists());

        /*
         * this checks if the merge.xml has been found since it will try to merge the content
         * because the java file exists already
         */
        builder.setMergeEnabled(true);
        builder.reset();
        builder.isBuilderFor = true;
        builder.build(ipsSrcFile);
    }

    @Test
    public void testDelete() throws Exception {
        builder.beforeBuild(ipsSrcFile, null);
        builder.isBuilderFor = true;
        builder.build(ipsSrcFile);
        builder.afterBuild(ipsSrcFile);
        // check file creation
        IFile file = project.getIpsPackageFragmentRoots()[0].getArtefactDestination(false).getFile("TestPolicy.java");
        assertTrue(file.exists());

        // check file deletion
        builder.delete(ipsSrcFile);
        file = project.getIpsPackageFragmentRoots()[0].getArtefactDestination(false).getFile("TestPolicy.java");
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
    public void testGetGeneratedJavaImplementationType() throws CoreException {
        JavaSourceFileBuilder spyBuilder = spy(builder);
        when(spyBuilder.isBuildingPublishedSourceFile()).thenReturn(false);
        IType mockJavaType = mock(IType.class);
        when(spyBuilder.getGeneratedJavaImplementationTypeThis()).thenReturn(mockJavaType);
        when(spyBuilder.isBuilderFor(ipsSrcFile)).thenReturn(true);

        IType generatedJavaImplementationType = spyBuilder
                .getGeneratedJavaImplementationType(ipsSrcFile.getIpsObject());

        assertEquals(mockJavaType, generatedJavaImplementationType);
    }

    @Test(expected = NullPointerException.class)
    public void testGetGeneratedJavaImplementationTypeNullPointer() {
        builder.getGeneratedJavaImplementationType(null);
    }

    @Test
    public void testGetGeneratedJavaImplementationTypeBuildingPublishedSourceFile() throws CoreException {
        JavaSourceFileBuilder spyBuilder = spy(builder);
        when(spyBuilder.isBuildingPublishedSourceFile()).thenReturn(true);
        IType mockJavaType = mock(IType.class);
        when(spyBuilder.getGeneratedJavaImplementationTypeThis()).thenReturn(mockJavaType);
        when(spyBuilder.isBuilderFor(ipsSrcFile)).thenReturn(true);

        IType generatedJavaImplementationType = spyBuilder
                .getGeneratedJavaImplementationType(ipsSrcFile.getIpsObject());

        assertNull(generatedJavaImplementationType);
    }

}
