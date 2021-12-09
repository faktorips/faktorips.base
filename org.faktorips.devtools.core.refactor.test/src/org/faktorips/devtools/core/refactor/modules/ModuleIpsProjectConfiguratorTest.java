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

import static org.faktorips.abstracttest.matcher.FluentAssert.when;
import static org.faktorips.devtools.core.refactor.modules.RequiresTransitiveMatcher.requiresTransitive;
import static org.faktorips.testsupport.IpsMatchers.containsErrorMessage;
import static org.faktorips.testsupport.IpsMatchers.isEmpty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestIpsModelExtensions;
import org.faktorips.abstracttest.matcher.FluentAssert.SetUp;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.IIpsProjectConfigurator;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsClasspathContainerInitializer;
import org.faktorips.devtools.model.util.IpsProjectCreationProperties;
import org.faktorips.devtools.model.util.PersistenceSupportNames;
import org.faktorips.runtime.MessageList;
import org.junit.Test;

public class ModuleIpsProjectConfiguratorTest extends AbstractIpsPluginTest {

    private ModuleIpsProjectConfigurator moduleIpsProjectConfigurator;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        moduleIpsProjectConfigurator = new ModuleIpsProjectConfigurator();
    }

    @Test
    public void testCanConfigure() throws Exception {
        IProject project = newPlatformProject("p");
        IJavaProject javaProject = addJavaCapabilities(project);
        convertToModuleProject(javaProject);

        assertThat(moduleIpsProjectConfigurator.canConfigure(javaProject), is(true));
    }

    @Test
    public void testCanConfigure_NoModule() throws Exception {
        IProject project = newPlatformProject("p");
        IJavaProject javaProject = addJavaCapabilities(project);

        assertThat(moduleIpsProjectConfigurator.canConfigure(javaProject), is(false));
    }

    @Test
    public void testIsGroovySupported() throws Exception {
        try (TestIpsModelExtensions testIpsModelExtensions = new TestIpsModelExtensions()) {
            IProject project = newPlatformProject("p");
            IJavaProject javaProject = addJavaCapabilities(project);
            convertToModuleProject(javaProject);

            when(configuratorsAre(moduleIpsProjectConfigurator))
                    .assertThat(moduleIpsProjectConfigurator.isGroovySupported(javaProject),
                            is(IpsClasspathContainerInitializer.isGroovySupportAvailable()));
            when(configuratorsAre(new NonApplicableIpsProjectConfigurator(), moduleIpsProjectConfigurator))
                    .assertThat(moduleIpsProjectConfigurator.isGroovySupported(javaProject),
                            is(IpsClasspathContainerInitializer.isGroovySupportAvailable()));
            when(configuratorsAre(new GroovyIpsProjectConfigurator(), moduleIpsProjectConfigurator))
                    .assertThat(moduleIpsProjectConfigurator.isGroovySupported(javaProject), is(false));
            when(configuratorsAre(new NonGroovyIpsProjectConfigurator(), moduleIpsProjectConfigurator))
                    .assertThat(moduleIpsProjectConfigurator.isGroovySupported(javaProject), is(false));
        }
    }

    @Test
    public void testValidate_NonPersistentProject() throws Exception {
        IProject project = newPlatformProject("p");
        IJavaProject javaProject = addJavaCapabilities(project);
        convertToModuleProject(javaProject);
        IpsProjectCreationProperties properties = new IpsProjectCreationProperties();

        MessageList messages = moduleIpsProjectConfigurator.validate(javaProject, properties);

        assertThat(messages, isEmpty());
    }

    @Test
    public void testValidate_PersistentProject() throws Exception {
        IProject project = newPlatformProject("p");
        IJavaProject javaProject = addJavaCapabilities(project);
        convertToModuleProject(javaProject);
        IpsProjectCreationProperties properties = new IpsProjectCreationProperties();
        properties.setPersistentProject(true);

        when(() -> properties.setPersistenceSupport(PersistenceSupportNames.ID_GENERIC_JPA_2))
                .assertThat(moduleIpsProjectConfigurator.validate(javaProject, properties), isEmpty());
        when(() -> properties.setPersistenceSupport(PersistenceSupportNames.ID_GENERIC_JPA_2_1))
                .assertThat(moduleIpsProjectConfigurator.validate(javaProject, properties), isEmpty());
        when(() -> properties.setPersistenceSupport(PersistenceSupportNames.ID_ECLIPSE_LINK_1_1))
                .assertThat(moduleIpsProjectConfigurator.validate(javaProject, properties), containsErrorMessage());
        when(() -> properties.setPersistenceSupport(PersistenceSupportNames.ID_ECLIPSE_LINK_2_5))
                .assertThat(moduleIpsProjectConfigurator.validate(javaProject, properties), containsErrorMessage());
    }

    @Test
    public void testConfigureIpsProject() throws Exception {
        IIpsProject ipsProject = newIpsProject("p");
        IJavaProject javaProject = ipsProject.getJavaProject().unwrap();
        convertToModuleProject(javaProject);
        IpsProjectCreationProperties properties = new IpsProjectCreationProperties();
        properties.setGroovySupport(false);

        moduleIpsProjectConfigurator.configureIpsProject(ipsProject, properties);

        assertThat(javaProject.getModuleDescription().getRequiredModuleNames(),
                is(new String[] { "java.base", "org.faktorips.runtime" }));
        assertThat(javaProject, requiresTransitive("org.faktorips.runtime"));
    }

    @Test
    public void testConfigureIpsProject_Standard() throws Exception {
        try (TestIpsModelExtensions testIpsModelExtensions = new TestIpsModelExtensions()) {
            IIpsProject ipsProject = newIpsProject("p");
            IJavaProject javaProject = ipsProject.getJavaProject().unwrap();
            convertToModuleProject(javaProject);
            IpsProjectCreationProperties properties = new IpsProjectCreationProperties();
            properties.setGroovySupport(false);
            configuratorsAre(moduleIpsProjectConfigurator).setUp();

            moduleIpsProjectConfigurator.configureIpsProject(ipsProject, properties);

            Optional<IClasspathEntry> ipsClasspathContainerEntry = Arrays.stream(javaProject.getRawClasspath())
                    .filter(entry -> IpsClasspathContainerInitializer.CONTAINER_ID.equals(entry.getPath().segment(0)))
                    .findFirst();
            assertThat(ipsClasspathContainerEntry.isPresent(), is(true));
            Optional<IClasspathAttribute> moduleAttribute = Arrays
                    .stream(ipsClasspathContainerEntry.get().getExtraAttributes())
                    .filter(a -> IClasspathAttribute.MODULE.equals(a.getName())).findFirst();
            assertThat(moduleAttribute.isPresent(), is(true));
            assertThat(moduleAttribute.get().getValue(), is("true"));
        }
    }

    @Test
    public void testConfigureIpsProject_Groovy() throws Exception {
        IIpsProject ipsProject = newIpsProject("p");
        IJavaProject javaProject = ipsProject.getJavaProject().unwrap();
        convertToModuleProject(javaProject);
        IpsProjectCreationProperties properties = new IpsProjectCreationProperties();
        properties.setGroovySupport(true);

        moduleIpsProjectConfigurator.configureIpsProject(ipsProject, properties);

        assertThat(javaProject.getModuleDescription().getRequiredModuleNames(),
                is(new String[] { "java.base", "org.faktorips.runtime", "org.faktorips.runtime.groovy" }));
        assertThat(javaProject, requiresTransitive("org.faktorips.runtime"));
        assertThat(javaProject, requiresTransitive("org.faktorips.runtime.groovy"));
    }

    @Test
    public void testConfigureIpsProject_JPA() throws Exception {
        IIpsProject ipsProject = newIpsProject("p");
        IJavaProject javaProject = ipsProject.getJavaProject().unwrap();
        convertToModuleProject(javaProject);
        IpsProjectCreationProperties properties = new IpsProjectCreationProperties();
        properties.setGroovySupport(false);
        properties.setPersistentProject(true);
        properties.setPersistenceSupport(PersistenceSupportNames.ID_GENERIC_JPA_2);

        moduleIpsProjectConfigurator.configureIpsProject(ipsProject, properties);

        assertThat(javaProject.getModuleDescription().getRequiredModuleNames(),
                is(new String[] { "java.base", "org.faktorips.runtime", "javax.persistence" }));
    }

    private SetUp configuratorsAre(IIpsProjectConfigurator... configurators) {
        return () -> ((TestIpsModelExtensions)IIpsModelExtensions.get()).setIpsProjectConfigurators(
                Arrays.asList(configurators));
    }

    private static class NonApplicableIpsProjectConfigurator implements IIpsProjectConfigurator {

        @Override
        public boolean canConfigure(IJavaProject javaProject) {
            return false;
        }

        @Override
        public boolean isGroovySupported(IJavaProject javaProject) {
            return true;
        }

        @Override
        public void configureIpsProject(IIpsProject ipsProject, IpsProjectCreationProperties creationProperties)
                throws CoreRuntimeException {
            fail("should never be called");
        }

    }

    private static class GroovyIpsProjectConfigurator implements IIpsProjectConfigurator {

        @Override
        public boolean canConfigure(IJavaProject javaProject) {
            return true;
        }

        @Override
        public boolean isGroovySupported(IJavaProject javaProject) {
            return true;
        }

        @Override
        public void configureIpsProject(IIpsProject ipsProject, IpsProjectCreationProperties creationProperties)
                throws CoreRuntimeException {
            fail("should never be called");
        }

    }

    private static class NonGroovyIpsProjectConfigurator implements IIpsProjectConfigurator {

        @Override
        public boolean canConfigure(IJavaProject javaProject) {
            return true;
        }

        @Override
        public boolean isGroovySupported(IJavaProject javaProject) {
            return false;
        }

        @Override
        public void configureIpsProject(IIpsProject ipsProject, IpsProjectCreationProperties creationProperties)
                throws CoreRuntimeException {
            fail("should never be called");
        }

    }

}
