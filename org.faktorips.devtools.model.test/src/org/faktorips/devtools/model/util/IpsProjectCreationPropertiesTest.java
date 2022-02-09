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
import static org.faktorips.testsupport.IpsMatchers.containsErrorMessage;
import static org.faktorips.testsupport.IpsMatchers.hasInvalidObject;
import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.faktorips.testsupport.IpsMatchers.isEmpty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestIpsModelExtensions;
import org.faktorips.abstracttest.matcher.FluentAssert.SetUp;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.IIpsProjectConfigurator;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;

public class IpsProjectCreationPropertiesTest extends AbstractIpsPluginTest {

    private IpsProjectCreationProperties properties;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        properties = new IpsProjectCreationProperties();
        properties.getLocales().add(new Locale("en"));
    }

    @Test
    public void testDefaults() {
        IpsProjectCreationProperties props = new IpsProjectCreationProperties();
        assertThat(props.getBasePackageName(), is(Messages.IpsProjectCreation_defaultBasePackageName));
        assertThat(props.getSourceFolderName(), is(Messages.IpsProjectCreation_defaultSourceFolderName));
        assertThat(props.getRuntimeIdPrefix(), is(Messages.IpsProjectCreation_defaultRuntimeIdPrefix));
        assertThat(props.getPersistenceSupport(), is(PersistenceSupportNames.ID_GENERIC_JPA_2));
        assertThat(props.isModelProject(), is(true));
        assertThat(props.isProductDefinitionProject(), is(false));
        assertThat(props.isPersistentProject(), is(false));
        assertThat(props.isGroovySupport(), is(true));
        assertThat(props.getLocales().isEmpty(), is(true));
    }

    @Test
    public void testValidateRequiredProperties() {
        assertThat(properties.validateRequiredProperties(), isEmpty());
    }

    @Test
    public void testValidateRequiredProperties_EmptyBasePackage() {
        properties.setBasePackageName("");

        MessageList messages = properties.validateRequiredProperties();

        assertThat(messages, containsErrorMessage());
        assertThat(messages, hasMessageCode(IpsProjectCreationProperties.MSG_CODE_MISSING_PROPERTY));
        assertThat(messages.getMessageByCode(IpsProjectCreationProperties.MSG_CODE_MISSING_PROPERTY),
                hasInvalidObject(properties, IpsProjectCreationProperties.PROPERTY_BASE_PACKAGE_NAME));
    }

    @Test
    public void testValidateRequiredProperties_EmptyRuntimeIdPrefix() {
        properties.setRuntimeIdPrefix("");

        MessageList messages = properties.validateRequiredProperties();

        assertThat(messages, containsErrorMessage());
        assertThat(messages, hasMessageCode(IpsProjectCreationProperties.MSG_CODE_MISSING_PROPERTY));
        assertThat(messages.getMessageByCode(IpsProjectCreationProperties.MSG_CODE_MISSING_PROPERTY),
                hasInvalidObject(properties, IpsProjectCreationProperties.PROPERTY_RUNTIME_ID_PREFIX));
    }

    @Test
    public void testValidateRequiredProperties_EmptySourceFolder() {
        properties.setSourceFolderName("");

        MessageList messages = properties.validateRequiredProperties();

        assertThat(messages, containsErrorMessage());
        assertThat(messages, hasMessageCode(IpsProjectCreationProperties.MSG_CODE_MISSING_PROPERTY));
        assertThat(messages.getMessageByCode(IpsProjectCreationProperties.MSG_CODE_MISSING_PROPERTY),
                hasInvalidObject(properties, IpsProjectCreationProperties.PROPERTY_SOURCE_FOLDER_NAME));
    }

    @Test
    public void testValidateRequiredProperties_EmptyPersistenceSupport() {
        properties.setPersistentProject(true);
        properties.setPersistenceSupport("");

        MessageList messages = properties.validateRequiredProperties();

        assertThat(messages, containsErrorMessage());
        assertThat(messages, hasMessageCode(IpsProjectCreationProperties.MSG_CODE_MISSING_PROPERTY));
        assertThat(messages.getMessageByCode(IpsProjectCreationProperties.MSG_CODE_MISSING_PROPERTY),
                hasInvalidObject(properties, IpsProjectCreationProperties.PROPERTY_PERSISTENCE_SUPPORT));
    }

    @Test
    public void testValidateRequiredProperties_EmptyPersistenceSupport_NonPersistentProject() {
        properties.setPersistentProject(false);
        properties.setPersistenceSupport("");

        MessageList messages = properties.validateRequiredProperties();

        assertThat(messages, isEmpty());
    }

    @Test
    public void testValidateRequiredProperties_EmptyLocales() {
        properties.getLocales().clear();

        MessageList messages = properties.validateRequiredProperties();

        assertThat(messages, containsErrorMessage());
        assertThat(messages, hasMessageCode(IpsProjectCreationProperties.MSG_CODE_MISSING_PROPERTY));
        assertThat(messages.getMessageByCode(IpsProjectCreationProperties.MSG_CODE_MISSING_PROPERTY),
                hasInvalidObject(properties, IpsProjectCreationProperties.PROPERTY_LOCALES));
    }

    @Test
    public void testValidateRequiredProperties_NullBasePackage() {
        properties.setBasePackageName(null);

        MessageList messages = properties.validateRequiredProperties();

        assertThat(messages, containsErrorMessage());
        assertThat(messages, hasMessageCode(IpsProjectCreationProperties.MSG_CODE_MISSING_PROPERTY));
        assertThat(messages.getMessageByCode(IpsProjectCreationProperties.MSG_CODE_MISSING_PROPERTY),
                hasInvalidObject(properties, IpsProjectCreationProperties.PROPERTY_BASE_PACKAGE_NAME));
    }

    @Test
    public void testValidateRequiredProperties_NullRuntimeIdPrefix() {
        properties.setRuntimeIdPrefix(null);

        MessageList messages = properties.validateRequiredProperties();

        assertThat(messages, containsErrorMessage());
        assertThat(messages, hasMessageCode(IpsProjectCreationProperties.MSG_CODE_MISSING_PROPERTY));
        assertThat(messages.getMessageByCode(IpsProjectCreationProperties.MSG_CODE_MISSING_PROPERTY),
                hasInvalidObject(properties, IpsProjectCreationProperties.PROPERTY_RUNTIME_ID_PREFIX));
    }

    @Test
    public void testValidateRequiredProperties_NullSourceFolder() {
        properties.setSourceFolderName(null);

        MessageList messages = properties.validateRequiredProperties();

        assertThat(messages, containsErrorMessage());
        assertThat(messages, hasMessageCode(IpsProjectCreationProperties.MSG_CODE_MISSING_PROPERTY));
        assertThat(messages.getMessageByCode(IpsProjectCreationProperties.MSG_CODE_MISSING_PROPERTY),
                hasInvalidObject(properties, IpsProjectCreationProperties.PROPERTY_SOURCE_FOLDER_NAME));
    }

    @Test
    public void testValidateRequiredProperties_NullPersistenceSupport() {
        properties.setPersistentProject(true);
        properties.setPersistenceSupport(null);

        MessageList messages = properties.validateRequiredProperties();

        assertThat(messages, containsErrorMessage());
        assertThat(messages, hasMessageCode(IpsProjectCreationProperties.MSG_CODE_MISSING_PROPERTY));
        assertThat(messages.getMessageByCode(IpsProjectCreationProperties.MSG_CODE_MISSING_PROPERTY),
                hasInvalidObject(properties, IpsProjectCreationProperties.PROPERTY_PERSISTENCE_SUPPORT));
    }

    @Test
    public void testValidateRequiredProperties_NullPersistenceSupport_NonPersistentProject() {
        properties.setPersistentProject(false);
        properties.setPersistenceSupport(null);

        MessageList messages = properties.validateRequiredProperties();

        assertThat(messages, isEmpty());
    }

    @Test
    public void testValidateRequiredProperties_NullLocales() {
        properties.setLocales(null);

        MessageList messages = properties.validateRequiredProperties();

        assertThat(messages, containsErrorMessage());
        assertThat(messages, hasMessageCode(IpsProjectCreationProperties.MSG_CODE_MISSING_PROPERTY));
        assertThat(messages.getMessageByCode(IpsProjectCreationProperties.MSG_CODE_MISSING_PROPERTY),
                hasInvalidObject(properties, IpsProjectCreationProperties.PROPERTY_LOCALES));
    }

    @Test
    public void testGettersAndSetters() {
        String basePackageName = "test";
        String sourceFolderName = "source";
        String runtimeIdPrefix = "runtime.";
        String persistenceSupport = "Eclipse Link 2.5";
        boolean isModelProject = false;
        boolean isProductDefinitionProject = true;
        boolean isPersistenceProject = true;
        boolean isGroovySupport = false;
        List<Locale> locales = new ArrayList<>();
        locales.add(new Locale("en"));
        locales.add(new Locale("de"));

        properties.setBasePackageName(basePackageName);
        properties.setSourceFolderName(sourceFolderName);
        properties.setRuntimeIdPrefix(runtimeIdPrefix);
        properties.setPersistenceSupport(persistenceSupport);
        properties.setModelProject(isModelProject);
        properties.setProductDefinitionProject(isProductDefinitionProject);
        properties.setPersistentProject(isPersistenceProject);
        properties.setGroovySupport(isGroovySupport);
        properties.setLocales(locales);

        assertThat(properties.getBasePackageName(), is(basePackageName));
        assertThat(properties.getSourceFolderName(), is(sourceFolderName));
        assertThat(properties.getRuntimeIdPrefix(), is(runtimeIdPrefix));
        assertThat(properties.getPersistenceSupport(), is(persistenceSupport));
        assertThat(properties.isModelProject(), is(isModelProject));
        assertThat(properties.isProductDefinitionProject(), is(isProductDefinitionProject));
        assertThat(properties.isPersistentProject(), is(isPersistenceProject));
        assertThat(properties.isGroovySupport(), is(isGroovySupport));
        assertThat(properties.getLocales(), is(locales));
    }

    @Test
    public void testValidate() throws Exception {
        try (TestIpsModelExtensions testIpsModelExtensions = TestIpsModelExtensions.get()) {
            AProject project = newPlatformProject(UUID.randomUUID().toString());
            AJavaProject javaProject = addJavaCapabilities(project);
            StandardJavaProjectConfigurator standardJavaProjectConfigurator = new StandardJavaProjectConfigurator();
            NonApplicableIpsProjectConfigurator nonApplicableIpsProjectConfigurator = new NonApplicableIpsProjectConfigurator();
            ValidationErrorIpsProjectConfigurator validationErrorIpsProjectConfigurator = new ValidationErrorIpsProjectConfigurator();

            when(configuratorsAre(nonApplicableIpsProjectConfigurator, standardJavaProjectConfigurator))
                    .assertThat(properties.validate(javaProject), isEmpty());
            when(configuratorsAre(standardJavaProjectConfigurator, validationErrorIpsProjectConfigurator))
                    .assertThat(properties.validate(javaProject), containsErrorMessage());
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
        public MessageList validate(AJavaProject javaProject, IpsProjectCreationProperties creationProperties) {
            fail("Validation should never be called when canConfigure returns false");
            return null;
        }

        @Override
        public void configureIpsProject(IIpsProject ipsProject, IpsProjectCreationProperties creationProperties)
                {
            fail("should never be called");
        }

    }

    private static class ValidationErrorIpsProjectConfigurator implements IIpsProjectConfigurator {

        @Override
        public boolean canConfigure(AJavaProject javaProject) {
            return true;
        }

        @Override
        public boolean isGroovySupported(AJavaProject javaProject) {
            return true;
        }

        @Override
        public MessageList validate(AJavaProject javaProject, IpsProjectCreationProperties creationProperties) {
            return MessageList.ofErrors("Not OK");
        }

        @Override
        public void configureIpsProject(IIpsProject ipsProject, IpsProjectCreationProperties creationProperties)
                {
            fail("should never be called");
        }

    }
}
