/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.abstracttest.TestConfigurationElement;
import org.faktorips.abstracttest.TestExtension;
import org.faktorips.abstracttest.TestExtensionPoint;
import org.faktorips.abstracttest.TestExtensionRegistry;
import org.faktorips.abstracttest.TestLogger;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

public class IpsBuilderSetPropertyDefTest extends TestCase {

    public void testloadExtensions() {

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("value", "first");
        TestConfigurationElement discreteValue1 = new TestConfigurationElement("discreteValue", attributes, null,
                new IConfigurationElement[0]);

        attributes = new HashMap<String, String>();
        attributes.put("value", "second");
        TestConfigurationElement discreteValue2 = new TestConfigurationElement("discreteValue", attributes, null,
                new IConfigurationElement[0]);

        attributes = new HashMap<String, String>();
        attributes.put("value", "third");
        TestConfigurationElement discreteValue3 = new TestConfigurationElement("discreteValue", attributes, null,
                new IConfigurationElement[0]);

        TestConfigurationElement discreteValuesEl = new TestConfigurationElement("discreteValues",
                new HashMap<String, String>(), null, new IConfigurationElement[] { discreteValue1, discreteValue2,
                        discreteValue3 });

        attributes = new HashMap<String, String>();
        attributes.put("name", "logLevel");
        attributes.put("type", "enum");
        attributes.put("label", "Log Level");
        attributes.put("defaultValue", "first");
        attributes.put("disableValue", "second");
        attributes.put("description", "Hello");
        TestConfigurationElement propertyDefEl = new TestConfigurationElement("specifyLoggingLevel", attributes, null,
                new IConfigurationElement[] { discreteValuesEl });

        TestLogger logger = new TestLogger();

        IIpsBuilderSetPropertyDef propertyDef = IpsBuilderSetPropertyDef.loadExtensions(propertyDefEl, null,
                "builderSetId", logger, null);
        assertEquals(0, logger.getLogEntries().size());
        assertEquals("logLevel", propertyDef.getName());
        assertEquals("first", propertyDef.getDefaultValue(null));
        assertEquals("second", propertyDef.getDisableValue(null));
        assertEquals("Hello", propertyDef.getDescription());

        Object[] discreteValues = propertyDef.getDiscreteValues();
        assertEquals("first", discreteValues[0]);
        assertEquals("second", discreteValues[1]);
        assertEquals("third", discreteValues[2]);

        // removing the required type attribute
        attributes.remove("type");
        propertyDef = IpsBuilderSetPropertyDef.loadExtensions(propertyDefEl, null, "builderSetId", logger, null);
        assertEquals(1, logger.getLogEntries().size());

        // add the type attribute and remove the required defaultValue attribute. In this case since
        // null is an always an allowed value
        // as a defaultValue null is the default value. It depends on the program that evaluates the
        // default value how to treat null.
        attributes.put("type", "enum");
        attributes.remove("defaultValue");
        logger.reset();
        propertyDef = IpsBuilderSetPropertyDef.loadExtensions(propertyDefEl, null, "builderSetId", logger, null);
        assertEquals(0, logger.getLogEntries().size());
        assertNull(propertyDef.getDefaultValue(null));

        // add the defaultValue attribute and remove the required disableValue attribute. Here the
        // same applies as to the default value above
        attributes.put("defaultValue", "enum");
        attributes.remove("disableValue");
        logger.reset();
        propertyDef = IpsBuilderSetPropertyDef.loadExtensions(propertyDefEl, null, "builderSetId", logger, null);
        assertEquals(0, logger.getLogEntries().size());
        assertNull(propertyDef.getDisableValue(null));
    }

    public void testloadExtensionsIntegerValue() {

        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("name", "debugLevel");
        attributes.put("type", "integer");
        attributes.put("label", "Debug Level");
        attributes.put("defaultValue", "1");
        attributes.put("disableValue", "0");
        attributes.put("description", "Hello");
        TestConfigurationElement propertyDefEl = new TestConfigurationElement("debugLevelElement", attributes, null,
                new IConfigurationElement[] {});

        TestLogger logger = new TestLogger();
        IIpsBuilderSetPropertyDef propertyDef = IpsBuilderSetPropertyDef.loadExtensions(propertyDefEl, null,
                "builderSetId", logger, null);

        assertTrue(logger.getLogEntries().isEmpty());
        assertEquals("1", propertyDef.getDefaultValue(null));
        assertEquals(new Integer(1), propertyDef.parseValue(propertyDef.getDefaultValue(null)));

        assertEquals("0", propertyDef.getDisableValue(null));
        assertEquals(new Integer(0), propertyDef.parseValue(propertyDef.getDisableValue(null)));

        assertNotNull(propertyDef.validateValue("hallo"));
        assertNull(propertyDef.validateValue("1"));

        try {
            propertyDef.parseValue("hallo");
            fail();
        } catch (Exception e) {
        }

    }

    public void testloadExtensionsBooleanValue() {
        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("name", "useChangeListener");
        attributes.put("type", "boolean");
        attributes.put("label", "use Change  Listener");
        attributes.put("defaultValue", "true");
        attributes.put("disableValue", "false");
        attributes.put("description", "Hello");
        TestConfigurationElement propertyDefEl = new TestConfigurationElement("useChangeListenerElement", attributes,
                null, new IConfigurationElement[] {});

        TestLogger logger = new TestLogger();
        IIpsBuilderSetPropertyDef propertyDef = IpsBuilderSetPropertyDef.loadExtensions(propertyDefEl, null,
                "builderSetId", logger, null);

        assertTrue(logger.getLogEntries().isEmpty());

        assertEquals("true", propertyDef.getDefaultValue(null));
        assertEquals(Boolean.TRUE, propertyDef.parseValue(propertyDef.getDefaultValue(null)));

        assertEquals("false", propertyDef.getDisableValue(null));
        assertEquals(Boolean.FALSE, propertyDef.parseValue(propertyDef.getDisableValue(null)));
    }

    public void testloadExtensionsExtensionPointValue() {
        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("name", "loggingConnectorRef");
        attributes.put("type", "extensionPoint");
        attributes.put("label", "Logging Connector Ref");
        attributes.put("defaultValue", IpsPlugin.PLUGIN_ID + ".javaLoggingConnector");
        attributes.put("disableValue", "None");
        attributes.put("description", "Hello");
        attributes.put("pluginId", IpsPlugin.PLUGIN_ID);
        attributes.put("extensionPointId", IpsPlugin.PLUGIN_ID + ".loggingFrameworkConnector");

        TestConfigurationElement propertyDefEl = new TestConfigurationElement("loggingConnectorElement", attributes,
                null, new IConfigurationElement[] {});

        TestExtension loggingConnectorExt1 = new TestExtension(new IConfigurationElement[] {}, IpsPlugin.PLUGIN_ID,
                "javaLoggingConnector");
        TestExtension loggingConnectorExt2 = new TestExtension(new IConfigurationElement[] {}, IpsPlugin.PLUGIN_ID,
                "log4jLoggingConnector");
        TestExtension loggingConnectorExt3 = new TestExtension(new IConfigurationElement[] {}, IpsPlugin.PLUGIN_ID,
                "ownLoggingConnector");
        TestExtension loggingConnectorExt4 = new TestExtension(new IConfigurationElement[] {}, "None");

        TestExtensionPoint loggingConnectorExtensionPoint = new TestExtensionPoint(new IExtension[] {
                loggingConnectorExt1, loggingConnectorExt2, loggingConnectorExt3, loggingConnectorExt4 },
                IpsPlugin.PLUGIN_ID, "loggingFrameworkConnector");
        TestExtensionRegistry registry = new TestExtensionRegistry(
                new IExtensionPoint[] { loggingConnectorExtensionPoint });

        TestLogger logger = new TestLogger();
        IIpsBuilderSetPropertyDef propertyDef = IpsBuilderSetPropertyDef.loadExtensions(propertyDefEl, registry,
                "builderSetId", logger, null);

        assertEquals(IpsPlugin.PLUGIN_ID + ".javaLoggingConnector", propertyDef.getDefaultValue(null));
        assertEquals(IpsPlugin.PLUGIN_ID + ".javaLoggingConnector", propertyDef.parseValue(propertyDef
                .getDefaultValue(null)));

        assertEquals("None", propertyDef.getDisableValue(null));
        assertEquals("None", propertyDef.parseValue(propertyDef.getDisableValue(null)));

        assertNotNull(propertyDef.validateValue("anotherConnector"));
        assertNull(propertyDef.validateValue(IpsPlugin.PLUGIN_ID + ".javaLoggingConnector"));

        List<String> values = Arrays.asList(propertyDef.getDiscreteValues());
        assertTrue(values.contains(IpsPlugin.PLUGIN_ID + ".javaLoggingConnector"));
        assertTrue(values.contains(IpsPlugin.PLUGIN_ID + ".log4jLoggingConnector"));
        assertTrue(values.contains(IpsPlugin.PLUGIN_ID + ".ownLoggingConnector"));
    }

    public void testloadExtensionsWithClassSpecfied() {
        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("class", IpsBuilderSetPropertyDef.class.getName());

        HashMap<String, Object> executableExtensionMap = new HashMap<String, Object>();

        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("name", "testProperty");
        properties.put("type", "boolean");
        properties.put("defaultValue", "true");
        properties.put("disableValue", "true");

        IpsBuilderSetPropertyDef ownDefClass = new IpsBuilderSetPropertyDef();
        ownDefClass.initialize(null, properties);
        executableExtensionMap.put("class", ownDefClass);

        TestConfigurationElement propertyDefEl = new TestConfigurationElement("testProperty", attributes, null,
                new IConfigurationElement[] {}, executableExtensionMap);

        TestLogger logger = new TestLogger();
        IIpsBuilderSetPropertyDef propertyDef = IpsBuilderSetPropertyDef.loadExtensions(propertyDefEl, null,
                "builderSetId", logger, null);
        assertSame(ownDefClass, propertyDef);
    }

    public void testValidateAvailability() {

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("value", "1.4");
        TestConfigurationElement jdk14 = new TestConfigurationElement("level", attributes, null,
                new IConfigurationElement[] {});

        attributes = new HashMap<String, String>();
        attributes.put("value", "1.5");
        TestConfigurationElement jdk15 = new TestConfigurationElement("level", attributes, null,
                new IConfigurationElement[] {});

        attributes = new HashMap<String, String>();
        attributes.put("value", "1.6");
        TestConfigurationElement jdk16 = new TestConfigurationElement("level", attributes, null,
                new IConfigurationElement[] {});

        TestConfigurationElement jdkCompliances = new TestConfigurationElement("jdkComplianceLevels",
                new HashMap<String, String>(), null, new IConfigurationElement[] { jdk14, jdk15, jdk16 });

        attributes = new HashMap<String, String>();
        attributes.put("name", "logLevel");
        attributes.put("type", "integer");
        attributes.put("label", "Log Level");
        attributes.put("defaultValue", "1");
        attributes.put("disableValue", "0");
        attributes.put("description", "Hello");
        TestConfigurationElement propertyDefEl = new TestConfigurationElement("logLevelElement", attributes, null,
                new IConfigurationElement[] { jdkCompliances });

        TestLogger logger = new TestLogger();
        IIpsBuilderSetPropertyDef propertyDef = IpsBuilderSetPropertyDef.loadExtensions(propertyDefEl, null,
                "builderSetId", logger, null);
        assertTrue(logger.getLogEntries().isEmpty());

        IIpsProject ipsProject = createTestIpsProject("1.4");
        assertTrue(propertyDef.isAvailable(ipsProject));

        ipsProject = createTestIpsProject("1.5");
        assertTrue(propertyDef.isAvailable(ipsProject));

        ipsProject = createTestIpsProject("1.6");
        assertTrue(propertyDef.isAvailable(ipsProject));

        ipsProject = createTestIpsProject("1.3");
        assertFalse(propertyDef.isAvailable(ipsProject));
    }

    private IIpsProject createTestIpsProject(final String complianceLevel) {

        InvocationHandler javaProjectHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("getOption") && args.length > 0
                        && ((String)args[0]).equals(JavaCore.COMPILER_COMPLIANCE)) {
                    return complianceLevel;
                }
                return null;
            }
        };
        final IJavaProject javaProject = (IJavaProject)Proxy.newProxyInstance(IJavaProject.class.getClassLoader(),
                new Class[] { IJavaProject.class }, javaProjectHandler);

        InvocationHandler ipsProjectHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("getJavaProject")) {
                    return javaProject;
                }
                return null;
            }
        };
        return (IIpsProject)Proxy.newProxyInstance(IIpsProject.class.getClassLoader(),
                new Class[] { IIpsProject.class }, ipsProjectHandler);
    }
}
