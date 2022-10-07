/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.eclipse.internal;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.APackageFragmentRoot;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.Abstractions;
import org.hamcrest.core.IsEqual;
import org.junit.Before;
import org.junit.Test;

public class EclipseJavaElementTest extends EclipseAbstractionTestSetup {

    private AJavaProject aJavaProject;
    private AProject aProject;
    private IJavaProject javaProject;

    @Before
    public void setUp() throws CoreException {
        aProject = newSimpleIpsProject("TestProject");
        ((IProject)aProject.unwrap()).open(null);
        aJavaProject = toJavaProject(aProject);
        javaProject = aJavaProject.unwrap();
    }

    @Test
    public void testAEclipseJavaElement() {
        assertThat(aJavaProject.unwrap(), is(instanceOf(IJavaElement.class)));
    }

    @Test
    public void testGetJavaProject() {
        assertThat(aJavaProject.getJavaProject(), is(IsEqual.equalTo(aJavaProject)));
        assertThat(aJavaProject, is(wrapperOf(javaProject)));
    }

    @Test
    public void testGetResource() {
        assertThat(aJavaProject.getResource(), is(notNullValue()));
    }

    @Test
    public void testGetResource_Packagefragment() {
        AFolder srcFolder = aProject.getFolder("src");
        APackageFragmentRoot pkgFragRoot = aJavaProject.toPackageFragmentRoot(srcFolder);

        assertThat(pkgFragRoot, is(wrapperOf(javaProject.getPackageFragmentRoot((IResource)srcFolder.unwrap()))));
    }

    @Test
    public void testExists() {
        assertThat(aJavaProject.exists(), is(true));
    }

    @Test
    public void testGetPath() {
        assertThat(aJavaProject.getPath(),
                is(Abstractions.getWorkspace().getRoot().getProject("TestProject").getWorkspaceRelativePath()));
    }
}
