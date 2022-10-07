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

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource.AResourceType;
import org.faktorips.devtools.abstraction.AWorkspace;
import org.faktorips.devtools.abstraction.AWorkspaceRoot;
import org.faktorips.devtools.abstraction.Abstractions;
import org.junit.Before;
import org.junit.Test;

public class EclipseWorkspaceRootTest extends EclipseAbstractionTestSetup {

    private static final String TEST_PROJECT1 = "TestProject1";
    private static final String TEST_PROJECT2 = "TestProject2";
    private static final String TEST_PROJECT3 = "TestProject3";

    private AProject testProject1;
    private AProject testProject2;
    private AWorkspace workspace;

    @Before
    public void setUp() {
        testProject1 = newSimpleIpsProject(TEST_PROJECT1);
        testProject2 = newSimpleIpsProject(TEST_PROJECT2);
        workspace = Abstractions.getWorkspace();
    }

    @Test
    public void testAEclipseWorkspaceRoot() {
        AWorkspaceRoot root = workspace.getRoot();

        assertThat(root.unwrap(), is(instanceOf(IWorkspaceRoot.class)));
    }

    @Test
    public void testGetProject() {
        AWorkspaceRoot root = workspace.getRoot();

        assertThat(root.getProject(TEST_PROJECT1), is(testProject1));
        assertThat(root.getProject(TEST_PROJECT2), is(testProject2));
        // will create a new project
        assertThat(root.getProject(TEST_PROJECT3), is(notNullValue()));
    }

    @Test
    public void testGetFileForLocation() {
        AFile aFile = workspace.getRoot()
                .getFileForLocation(testProject1.getLocation().resolve(".ipsproject"));

        assertThat(aFile, is(wrapperOf(((IProject)testProject1.unwrap()).getFile(".ipsproject"))));
    }

    @Test
    public void testGetProjects() {
        Set<AProject> projects = workspace.getRoot().getProjects();

        assertThat(projects, hasItems(testProject1, testProject2));
    }

    @Test
    public void testGetType() {
        assertThat(workspace.getRoot().getType(), is(AResourceType.WORKSPACE));
    }
}
