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

import static org.faktorips.abstracttest.matcher.FluentAssert.when;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestIpsModelExtensions;
import org.faktorips.abstracttest.matcher.FluentAssert.SetUp;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.IIpsProjectConfigurator;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Test;

public class IpsProjectConfiguratorsTest extends AbstractIpsPluginTest {

    private AJavaProject javaProject;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        AProject project = newPlatformProject(UUID.randomUUID().toString());
        javaProject = addJavaCapabilities(project);
    }

    @Test
    public void testApplicableTo() throws Exception {
        try (TestIpsModelExtensions testIpsModelExtensions = TestIpsModelExtensions.get()) {
            StandardJavaProjectConfigurator standardJavaProjectConfigurator = new StandardJavaProjectConfigurator();
            NonApplicableIpsProjectConfigurator nonApplicableIpsProjectConfigurator = new NonApplicableIpsProjectConfigurator();
            testIpsModelExtensions.setIpsProjectConfigurators(
                    Arrays.asList(standardJavaProjectConfigurator, nonApplicableIpsProjectConfigurator));

            List<IIpsProjectConfigurator> applicableConfigurators = IpsProjectConfigurators.applicableTo(javaProject)
                    .collect(Collectors.toList());

            assertThat(applicableConfigurators, hasItem(standardJavaProjectConfigurator));
            assertThat(applicableConfigurators, not(hasItem(nonApplicableIpsProjectConfigurator)));
        }
    }

    @Test
    public void testIsGroovySupported() throws Exception {
        try (TestIpsModelExtensions testIpsModelExtensions = TestIpsModelExtensions.get()) {
            NonApplicableIpsProjectConfigurator nonApplicableIpsProjectConfigurator = new NonApplicableIpsProjectConfigurator();
            GroovyIpsProjectConfigurator groovyIpsProjectConfigurator = new GroovyIpsProjectConfigurator();
            NonGroovyIpsProjectConfigurator nonGroovyIpsProjectConfigurator = new NonGroovyIpsProjectConfigurator();

            when(configuratorsAre(nonApplicableIpsProjectConfigurator, nonGroovyIpsProjectConfigurator))
                    .assertThat(IpsProjectConfigurators.isGroovySupported(javaProject), is(false));
            when(configuratorsAre(nonGroovyIpsProjectConfigurator, groovyIpsProjectConfigurator))
                    .assertThat(IpsProjectConfigurators.isGroovySupported(javaProject), is(true));
        }
    }

    private SetUp configuratorsAre(IIpsProjectConfigurator... configurators) {
        return () -> ((TestIpsModelExtensions)IIpsModelExtensions.get()).setIpsProjectConfigurators(
                Arrays.asList(configurators));
    }

    private static class NonApplicableIpsProjectConfigurator implements IIpsProjectConfigurator {

        @Override
        public boolean canConfigure(AJavaProject javaProject) {
            return false;
        }

        @Override
        public boolean isGroovySupported(AJavaProject javaProject) {
            return true;
        }

        @Override
        public void configureIpsProject(IIpsProject ipsProject, IpsProjectCreationProperties creationProperties) {
            fail("should never be called");
        }

    }

    private static class GroovyIpsProjectConfigurator implements IIpsProjectConfigurator {

        @Override
        public boolean canConfigure(AJavaProject javaProject) {
            return true;
        }

        @Override
        public boolean isGroovySupported(AJavaProject javaProject) {
            return true;
        }

        @Override
        public void configureIpsProject(IIpsProject ipsProject, IpsProjectCreationProperties creationProperties) {
            fail("should never be called");
        }

    }

    private static class NonGroovyIpsProjectConfigurator implements IIpsProjectConfigurator {

        @Override
        public boolean canConfigure(AJavaProject javaProject) {
            return true;
        }

        @Override
        public boolean isGroovySupported(AJavaProject javaProject) {
            return false;
        }

        @Override
        public void configureIpsProject(IIpsProject ipsProject, IpsProjectCreationProperties creationProperties) {
            fail("should never be called");
        }

    }

}
