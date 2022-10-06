/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.eclipse.internal;

import static org.faktorips.testsupport.IpsMatchers.isEmpty;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsSame.sameInstance;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.APackageFragmentRoot;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.testsupport.Wait;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class EclipseJavaProjectTest extends EclipseAbstractionTestSetup {

    private AJavaProject aJavaProject;
    private AProject aProject;
    private IJavaProject javaProject;

    @Before
    public void setUp() {
        aProject = newSimpleIpsProject("TestProject");
        aJavaProject = toJavaProject(aProject);
        javaProject = aJavaProject.unwrap();
    }

    @Test
    public void testAEclipseJavaProject() {
        assertThat(aJavaProject.unwrap(), is(instanceOf(IJavaProject.class)));
    }

    @Test
    public void testExists() {
        assertThat(aJavaProject.exists(), is(true));
    }

    @Test
    public void testExists_not() {
        AProject project = Abstractions.getWorkspace().getRoot().getProject("NotSavedProject");

        assertThat(AJavaProject.from(project).exists(), is(false));
    }

    @Test
    public void testGetOutputLocation() {
        assertThat(aJavaProject.getOutputLocation(), is(Path.of("/TestProject", "bin")));
    }

    @Test
    public void testGetPackageFragmentRoot_asString() {
        APackageFragmentRoot pfr = aJavaProject.toPackageFragmentRoot("RefProject");

        assertThat(pfr.unwrap(), is(instanceOf(IPackageFragmentRoot.class)));
        assertThat(pfr, is(wrapperOf(javaProject.getPackageFragmentRoot("RefProject"))));
    }

    @Test
    public void testGetPackageFragmentRoot_asResource() {
        AFolder srcFodler = aProject.getFolder("src");
        APackageFragmentRoot pfr = aJavaProject.toPackageFragmentRoot(srcFodler);

        assertThat(pfr.exists(), is(true));
        assertThat(pfr.unwrap(), is(instanceOf(IPackageFragmentRoot.class)));
        assertThat(pfr, is(wrapperOf(javaProject.getPackageFragmentRoot((IResource)srcFodler.unwrap()))));
    }

    @Test
    public void testGetProject() {
        assertThat(aJavaProject.getProject(), is(instanceOf(EclipseProject.class)));
        assertThat(aJavaProject.getProject().unwrap(), is(sameInstance(aProject.unwrap())));
    }

    @Test
    public void testHasBuildState() {
        assertThat(aJavaProject.hasBuildState(), is(false));

        aJavaProject.getProject().build(ABuildKind.FULL, null);

        Wait.atMost(Duration.ofSeconds(5)).until(aJavaProject::hasBuildState, aJavaProject + " has no build state");
    }

    @Test
    public void testGetSourceVersion() {
        assertThat(aJavaProject.getSourceVersion().toString(), is("1.5"));
    }

    @Test
    @Ignore
    public void testGetReferencedJavaProjects() throws CoreException {
        AProject refProject = newSimpleIpsProject("refProject");
        IPath path = ((IProject)refProject.unwrap()).getLocation();
        IClasspathEntry[] entries = { JavaCore.newProjectEntry(path, true) };
        IJavaProject unwrap = (IJavaProject)aJavaProject.unwrap();
        IClasspathEntry[] newEntries = Arrays.copyOf(unwrap.getRawClasspath(), unwrap.getRawClasspath().length + 1);
        newEntries[newEntries.length - 1] = entries[0];

        unwrap.setRawClasspath(newEntries, new NullProgressMonitor());

        assertThat(aJavaProject.getReferencedJavaProjects().isEmpty(), is(false));
    }

    @Test
    public void testIsJavaFolder() {
        AFolder srcFolder = aJavaProject.getProject().getFolder("src");

        assertThat(aJavaProject.isJavaFolder(srcFolder), is(true));
    }

    @Test
    public void testIsJavaFolder_isNot() {
        AFolder nonJavaFolder = aJavaProject.getProject().getFolder("nonJavaResource");
        nonJavaFolder.create(new NullProgressMonitor());

        assertThat(aJavaProject.isJavaFolder(nonJavaFolder), is(false));
    }

    @Test
    public void testValidateJavaProjectBuildPath() {

        assertThat(aJavaProject.validateJavaProjectBuildPath(), is(isEmpty()));
    }

    @Test
    public void testGetOptions() {
        assertThat(aJavaProject.getOptions(), is(notNullValue()));
    }

    @Test
    public void testGetAllPackageFragmentRoots() {
        Set<APackageFragmentRoot> allPackageFragmentRoots = aJavaProject.getAllPackageFragmentRoots();
        APackageFragmentRoot fragmentRootSrc = aJavaProject
                .toPackageFragmentRoot(aJavaProject.getProject().getFolder("src"));
        APackageFragmentRoot fragmentRootExt = aJavaProject
                .toPackageFragmentRoot(aJavaProject.getProject().getFolder("extension"));

        assertThat(allPackageFragmentRoots.isEmpty(), is(false));
        assertThat(allPackageFragmentRoots, is(hasItems(fragmentRootSrc, fragmentRootExt)));
    }
}
