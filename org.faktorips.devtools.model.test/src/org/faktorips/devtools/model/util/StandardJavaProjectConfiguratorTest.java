/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.plugin.IpsClasspathContainerInitializer;
import org.junit.Before;
import org.junit.Test;

public class StandardJavaProjectConfiguratorTest extends AbstractIpsPluginTest {

    private static IJavaProject javaProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IProject platformProject = newPlatformProject("TestProject");
        javaProject = addJavaCapabilities(platformProject);
    }

    @Test
    public void testConfigureDefaultIpsProject() throws CoreException {
        List<IClasspathEntry> classpathEntriesBefore = Arrays.asList(javaProject.getRawClasspath());
        StandardJavaProjectConfigurator.configureDefaultIpsProject(javaProject);

        List<IClasspathEntry> classpathEntriesAfter = Arrays.asList(javaProject.getRawClasspath());
        assertThat(classpathEntriesBefore.size(), is(classpathEntriesAfter.size() - 1));

        for (IClasspathEntry entry : classpathEntriesBefore) {
            assertThat(classpathEntriesAfter.contains(entry), is(true));
        }

        boolean containsIpsClasspathContainer = classpathEntriesAfter.stream()
                .anyMatch(entry -> IpsClasspathContainerInitializer.CONTAINER_ID.equals(entry.getPath().segment(0)));
        assertThat(containsIpsClasspathContainer, is(true));
    }

    @Test
    public void testConfigureDefaultIpsProject_malformedJavaVersion() throws CoreException {
        javaProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, "5.5");
        IClasspathEntry[] classpathEntries = javaProject.getRawClasspath();
        StandardJavaProjectConfigurator.configureDefaultIpsProject(javaProject);
        assertThat(javaProject.getRawClasspath(), is(classpathEntries));
    }

    @Test
    public void testConfigureDefaultIpsProject_invalidJavaVersion() throws CoreException {
        javaProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_4);
        IClasspathEntry[] classpathEntries = javaProject.getRawClasspath();
        StandardJavaProjectConfigurator.configureDefaultIpsProject(javaProject);
        assertThat(javaProject.getRawClasspath(), is(classpathEntries));
    }

    @Test
    public void testConfigureGroovyIpsProject_groovyEnabled() throws CoreException {
        List<IClasspathEntry> classpathEntriesBefore = Arrays.asList(javaProject.getRawClasspath());
        StandardJavaProjectConfigurator.configureGroovyIpsProject(javaProject, true);

        List<IClasspathEntry> classpathEntriesAfter = Arrays.asList(javaProject.getRawClasspath());
        assertThat(classpathEntriesBefore.size(), is(classpathEntriesAfter.size() - 1));

        for (IClasspathEntry entry : classpathEntriesBefore) {
            assertThat(classpathEntriesAfter.contains(entry), is(true));
        }

        Optional<IClasspathEntry> containsIpsClasspathContainer = classpathEntriesAfter.stream()
                .filter(entry -> IpsClasspathContainerInitializer.CONTAINER_ID.equals(entry.getPath().segment(0)))
                .findFirst();
        assertThat(containsIpsClasspathContainer.isPresent(), is(true));
        String bundles = containsIpsClasspathContainer.get().getPath().lastSegment().toString();
        // Here, simply checking for "true", as expected, is not possible, since tests within an
        // environment without Groovy installed would fail
        assertThat(bundles.contains("org.faktorips.runtime.groovy"),
                is(IpsClasspathContainerInitializer.isGroovySupportAvailable()));
    }

    @Test
    public void testConfigureGroovyIpsProject_groovyDisabled() throws CoreException {
        List<IClasspathEntry> classpathEntriesBefore = Arrays.asList(javaProject.getRawClasspath());
        StandardJavaProjectConfigurator.configureGroovyIpsProject(javaProject, false);

        List<IClasspathEntry> classpathEntriesAfter = Arrays.asList(javaProject.getRawClasspath());
        assertThat(classpathEntriesBefore.size(), is(classpathEntriesAfter.size() - 1));

        for (IClasspathEntry entry : classpathEntriesBefore) {
            assertThat(classpathEntriesAfter.contains(entry), is(true));
        }

        Optional<IClasspathEntry> containsIpsClasspathContainer = classpathEntriesAfter.stream()
                .filter(entry -> IpsClasspathContainerInitializer.CONTAINER_ID.equals(entry.getPath().segment(0)))
                .findFirst();
        assertThat(containsIpsClasspathContainer.isPresent(), is(true));
        String bundles = containsIpsClasspathContainer.get().getPath().lastSegment().toString();
        assertThat(bundles.contains("org.faktorips.runtime.groovy"), is(false));
    }

    @Test
    public void testConfigureGroovyIpsProject_malformedJavaVersion() throws CoreException {
        javaProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, "5.5");
        IClasspathEntry[] classpathEntries = javaProject.getRawClasspath();
        StandardJavaProjectConfigurator.configureGroovyIpsProject(javaProject, true);
        assertThat(javaProject.getRawClasspath(), is(classpathEntries));
    }

    @Test
    public void testConfigureGroovyIpsProject_invalidJavaVersion() throws CoreException {
        javaProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_4);
        IClasspathEntry[] classpathEntries = javaProject.getRawClasspath();
        StandardJavaProjectConfigurator.configureGroovyIpsProject(javaProject, true);
        assertThat(javaProject.getRawClasspath(), is(classpathEntries));
    }
}
