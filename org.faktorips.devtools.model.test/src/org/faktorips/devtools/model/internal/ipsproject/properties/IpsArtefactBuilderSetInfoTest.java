/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.properties;

import static org.faktorips.devtools.model.internal.ipsproject.properties.IpsArtefactBuilderSetInfoTest.AttributeSetter.clazz;
import static org.faktorips.devtools.model.internal.ipsproject.properties.IpsArtefactBuilderSetInfoTest.AttributeSetter.defaultValue;
import static org.faktorips.devtools.model.internal.ipsproject.properties.IpsArtefactBuilderSetInfoTest.AttributeSetter.description;
import static org.faktorips.devtools.model.internal.ipsproject.properties.IpsArtefactBuilderSetInfoTest.AttributeSetter.disableValue;
import static org.faktorips.devtools.model.internal.ipsproject.properties.IpsArtefactBuilderSetInfoTest.AttributeSetter.label;
import static org.faktorips.devtools.model.internal.ipsproject.properties.IpsArtefactBuilderSetInfoTest.AttributeSetter.name;
import static org.faktorips.devtools.model.internal.ipsproject.properties.IpsArtefactBuilderSetInfoTest.AttributeSetter.type;
import static org.faktorips.devtools.model.internal.ipsproject.properties.IpsArtefactBuilderSetInfoTest.AttributeSetter.value;
import static org.faktorips.devtools.model.internal.ipsproject.properties.IpsArtefactBuilderSetInfoTest.ChildElement.child;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.abstracttest.TestConfigurationElement;
import org.faktorips.abstracttest.TestExtensionRegistry;
import org.faktorips.abstracttest.TestLogger;
import org.faktorips.abstracttest.TestMockingUtils;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.builder.DefaultBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsModelActivator;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;

public class IpsArtefactBuilderSetInfoTest {

    private static final String COPY_SUPPORT = "copySupport";
    private static final String USE_CHANGE_LISTENER = "useChangeListener";
    private static final String LOGGING_CONNECTORS = "loggingConnectors";
    private TestExtensionRegistry registry;
    private TestLogger logger;
    private IpsArtefactBuilderSetInfo builderSetInfo;

    @Before
    public void setUp() {
        if (Abstractions.isEclipseRunning()) {
            TestConfigurationElement propertyDef1 = builderSetPropertyDef(name(LOGGING_CONNECTORS), type("boolean"),
                    label("Logging Connectors"), defaultValue("true"), disableValue("false"), description("Hello"));

            TestConfigurationElement propertyDef2 = builderSetPropertyDef(name(USE_CHANGE_LISTENER), type("boolean"),
                    label("Use Change Listener"), defaultValue("true"), disableValue("false"), description("Hello"));

            TestConfigurationElement propertyDef3 = builderSetPropertyDef(name(COPY_SUPPORT), type("boolean"),
                    label("Copy Support"), defaultValue("true"), disableValue("false"), description("Hello"),
                    // restricted availability
                    child(configElement("jdkComplianceLevels", name("jdkComplianceLevels"),
                            child(configElement("jdkComplianceLevel", value("1.4"))))));

            IExtension extension = TestMockingUtils.mockExtension("mybuilderset",
                    configElement("builderSet", clazz(DefaultBuilderSet.class.getName()), child(propertyDef1),
                            child(propertyDef2), child(propertyDef3)));

            IExtensionPoint extensionPoint = TestMockingUtils.mockExtensionPoint(IpsModelActivator.PLUGIN_ID,
                    "artefactbuilderset",
                    extension);

            registry = new TestExtensionRegistry(new IExtensionPoint[] { extensionPoint });

            logger = new TestLogger();
            ArrayList<IIpsArtefactBuilderSetInfo> builderSetInfoList = new ArrayList<>();
            IIpsModel ipsModel = mock(IIpsModel.class);

            IpsArtefactBuilderSetInfo.loadExtensions(registry, logger, builderSetInfoList, ipsModel);
            builderSetInfo = (IpsArtefactBuilderSetInfo)builderSetInfoList.get(0);
        }

    }

    @Test
    public void testLoadExtensions() {
        if (Abstractions.isEclipseRunning()) {
            assertEquals(3, builderSetInfo.getPropertyDefinitions().length);
            assertEquals("mybuilderset", builderSetInfo.getBuilderSetId());
        }
    }

    @Test
    public void testValidateIpsBuilderSetPropertyValue_OK() {
        if (Abstractions.isEclipseRunning()) {
            IIpsProject ipsProject = mock(IIpsProject.class);
            assertNull(builderSetInfo.validateIpsBuilderSetPropertyValue(ipsProject, LOGGING_CONNECTORS, "true"));
        }
    }

    @Test
    public void testValidateIpsBuilderSetPropertyValue_WrongDatatype() {
        if (Abstractions.isEclipseRunning()) {
            IIpsProject ipsProject = mock(IIpsProject.class);
            assertNotNull(builderSetInfo.validateIpsBuilderSetPropertyValue(ipsProject, LOGGING_CONNECTORS, "hallo"));
        }
    }

    @Test
    public void testValidateIpsBuilderSetPropertyValue_UnknownProperty() {
        if (Abstractions.isEclipseRunning()) {
            IIpsProject ipsProject = mock(IIpsProject.class);
            assertNotNull(builderSetInfo.validateIpsBuilderSetPropertyValue(ipsProject, "hanswurst", "hallo"));
        }
    }

    @Test
    public void testValidateIpsArtefactBuilderSetConfig_OK() {
        if (Abstractions.isEclipseRunning()) {
            IIpsProject ipsProject = mock(IIpsProject.class);
            Map<String, String> properties = new HashMap<>();
            properties.put(LOGGING_CONNECTORS, "true");
            properties.put(USE_CHANGE_LISTENER, "false");
            IpsArtefactBuilderSetConfigModel builderSetConfig = new IpsArtefactBuilderSetConfigModel(properties);

            MessageList msgList = builderSetInfo.validateIpsArtefactBuilderSetConfig(ipsProject, builderSetConfig);

            assertTrue(msgList.isEmpty());
        }
    }

    @Test
    public void testValidateIpsArtefactBuilderSetConfig_UnknownProperty() {
        if (Abstractions.isEclipseRunning()) {
            IIpsProject ipsProject = mock(IIpsProject.class);
            Map<String, String> properties = new HashMap<>();
            properties.put(LOGGING_CONNECTORS, "true");
            properties.put(USE_CHANGE_LISTENER, "false");
            properties.put("hanswurst", "false");
            IpsArtefactBuilderSetConfigModel builderSetConfig = new IpsArtefactBuilderSetConfigModel(properties);

            MessageList msgList = builderSetInfo.validateIpsArtefactBuilderSetConfig(ipsProject, builderSetConfig);

            assertFalse(msgList.isEmpty());
        }
    }

    @Test
    public void testCreateDefaultConfiguration() {
        if (Abstractions.isEclipseRunning()) {
            IIpsProject ipsProject = mockIpsAndJavaProject("1.4");
            IIpsArtefactBuilderSetConfigModel configuration = builderSetInfo.createDefaultConfiguration(ipsProject);
            assertThat(configuration.getPropertyValue(USE_CHANGE_LISTENER), is("true"));
            assertThat(configuration.getPropertyValue(LOGGING_CONNECTORS), is("true"));
            assertThat(configuration.getPropertyValue(COPY_SUPPORT), is("true"));
        }
    }

    @Test
    public void testCreateDefaultConfiguration_OnlyAvailable() {
        if (Abstractions.isEclipseRunning()) {
            IIpsProject ipsProject = mockIpsAndJavaProject("1.5");
            IIpsArtefactBuilderSetConfigModel configuration = builderSetInfo.createDefaultConfiguration(ipsProject);
            assertThat(configuration.getPropertyValue(USE_CHANGE_LISTENER), is("true"));
            assertThat(configuration.getPropertyValue(LOGGING_CONNECTORS), is("true"));
            // only available for compliance level 1.4
            assertThat(configuration.getPropertyValue(COPY_SUPPORT), is(nullValue()));
        }
    }

    private IIpsProject mockIpsAndJavaProject(String complianceLevel) {
        IIpsProject ipsProject = mock(IIpsProject.class);
        IJavaProject javaProject = mock(IJavaProject.class);
        when(ipsProject.getJavaProject()).thenReturn(Wrappers.wrap(javaProject).as(AJavaProject.class));
        when(javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true)).thenReturn(complianceLevel);
        return ipsProject;
    }

    private static TestConfigurationElement builderSetPropertyDef(ConfigElementModifier... modifiers) {
        return configElement("builderSetPropertyDef", modifiers);
    }

    private static TestConfigurationElement configElement(String name, ConfigElementModifier... modifiers) {
        Map<String, String> attributes = new HashMap<>();
        List<IConfigurationElement> children = new LinkedList<>();
        for (ConfigElementModifier modifier : modifiers) {
            if (modifier instanceof AttributeSetter) {
                ((AttributeSetter)modifier).set(attributes);
            }
            if (modifier instanceof ChildElement) {
                children.add(((ChildElement)modifier).get());
            }
        }
        return new TestConfigurationElement(name, attributes, null,
                children.toArray(new IConfigurationElement[children.size()]));
    }

    public interface ConfigElementModifier {

    }

    public static class ChildElement implements ConfigElementModifier {

        private IConfigurationElement child;

        public ChildElement(IConfigurationElement child) {
            this.child = child;
        }

        public IConfigurationElement get() {
            return child;
        }

        public static ChildElement child(IConfigurationElement child) {
            return new ChildElement(child);
        }
    }

    public static class AttributeSetter implements ConfigElementModifier {
        private final String attribute;
        private String value;

        public AttributeSetter(String attribute, String value) {
            this.attribute = attribute;
            this.value = value;
        }

        public void set(Map<String, String> attributes) {
            attributes.put(attribute, value);
        }

        public static AttributeSetter name(String name) {
            return new AttributeSetter("name", name);
        }

        public static AttributeSetter type(String type) {
            return new AttributeSetter("type", type);
        }

        public static AttributeSetter label(String label) {
            return new AttributeSetter("label", label);
        }

        public static AttributeSetter defaultValue(String defaultValue) {
            return new AttributeSetter("defaultValue", defaultValue);
        }

        public static AttributeSetter disableValue(String disableValue) {
            return new AttributeSetter("disableValue", disableValue);
        }

        public static AttributeSetter description(String description) {
            return new AttributeSetter("description", description);
        }

        public static AttributeSetter value(String value) {
            return new AttributeSetter("value", value);
        }

        public static AttributeSetter clazz(String clazz) {
            return new AttributeSetter("class", clazz);
        }
    }

}
