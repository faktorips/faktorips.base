/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.refactor.modules;

import static org.faktorips.devtools.core.refactor.modules.RequiresTransitiveMatcher.requires;
import static org.faktorips.devtools.core.refactor.modules.RequiresTransitiveMatcher.requiresTransitive;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.junit.Test;

public class ModulesTest extends AbstractIpsPluginTest {

    @Test
    public void testAddRequired() throws Exception {
        IProject project = newPlatformProject("p");
        IJavaProject javaProject = addJavaCapabilities(project);
        convertToModuleProject(javaProject);
        Modules.addRequired(javaProject, false, List.of("foo.bar"));

        assertThat(javaProject.getModuleDescription().getRequiredModuleNames(),
                is(new String[] { "java.base", "foo.bar" }));
        assertThat(javaProject, requires("foo.bar"));
    }

    @Test
    public void testAddRequired_Transitive() throws Exception {
        IProject project = newPlatformProject("p");
        IJavaProject javaProject = addJavaCapabilities(project);
        convertToModuleProject(javaProject);
        Modules.addRequired(javaProject, true, List.of("foo.bar"));

        assertThat(javaProject.getModuleDescription().getRequiredModuleNames(),
                is(new String[] { "java.base", "foo.bar" }));
        assertThat(javaProject, requiresTransitive("foo.bar"));
    }

    @Test
    public void testAddRequired_AlreadyContained() throws Exception {
        IProject project = newPlatformProject("p");
        IJavaProject javaProject = addJavaCapabilities(project);
        convertToModuleProject(javaProject);
        Modules.addRequired(javaProject, false, List.of("foo.bar"));

        Modules.addRequired(javaProject, false, List.of("foo.bar", "foo.baz"));

        assertThat(javaProject.getModuleDescription().getRequiredModuleNames(),
                is(new String[] { "java.base", "foo.bar", "foo.baz" }));
    }

    @Test
    public void testAddRequired_AlreadyContained_AddTransitive() throws Exception {
        IProject project = newPlatformProject("p");
        IJavaProject javaProject = addJavaCapabilities(project);
        convertToModuleProject(javaProject);
        Modules.addRequired(javaProject, false, List.of("foo.bar", "foo.baz"));

        Modules.addRequired(javaProject, true, List.of("foo.bar"));

        assertThat(javaProject.getModuleDescription().getRequiredModuleNames(),
                is(new String[] { "java.base", "foo.bar", "foo.baz" }));
        assertThat(javaProject, requiresTransitive("foo.bar"));
    }

    @Test(expected = CoreException.class)
    public void testAddRequired_NoModule() throws Exception {
        IProject project = newPlatformProject("p");
        IJavaProject javaProject = addJavaCapabilities(project);

        Modules.addRequired(javaProject, false, List.of("foo.bar"));
    }

}
