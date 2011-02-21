/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestConfigurationElement;
import org.faktorips.abstracttest.TestExtension;
import org.faktorips.abstracttest.TestExtensionPoint;
import org.faktorips.abstracttest.TestExtensionRegistry;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;

public class IpsUIPluginTest extends AbstractIpsPluginTest {

    private IExtensionPropertyEditFieldFactory editFieldFactory;

    private Map<String, String> editFieldFactoryAttributes;

    private IExtensionPropertyEditFieldFactory editFieldFactory2;

    @Override
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // PropertyEditFieldFactory setup code
        editFieldFactory = new IExtensionPropertyEditFieldFactory() {
            @Override
            public EditField newEditField(IIpsObjectPartContainer ipsObjectPart,
                    Composite extensionArea,
                    UIToolkit toolkit) {
                return null;
            }
        };
        editFieldFactory2 = new IExtensionPropertyEditFieldFactory() {
            @Override
            public EditField newEditField(IIpsObjectPartContainer ipsObjectPart,
                    Composite extensionArea,
                    UIToolkit toolkit) {
                return null;
            }
        };
        HashMap execAttr = new HashMap();
        execAttr.put("class", editFieldFactory);
        editFieldFactoryAttributes = new HashMap();
        editFieldFactoryAttributes.put("propertyId", "additionalProperty");
        TestConfigurationElement configEl = new TestConfigurationElement("", editFieldFactoryAttributes, "",
                new IConfigurationElement[0], execAttr);
        TestExtension extension = new TestExtension(new IConfigurationElement[] { configEl }, IpsUIPlugin.PLUGIN_ID,
                IpsUIPlugin.EXTENSION_POINT_ID_EXTENSION_PROPERTY_EDIT_FIELD_FACTORY);

        HashMap execAttr2 = new HashMap();
        execAttr2.put("class", editFieldFactory2);
        Map<String, String> editFieldFactory2Attributes = new HashMap<String, String>();
        editFieldFactory2Attributes.put("propertyId", "additionalProperty2");
        TestConfigurationElement configEl2 = new TestConfigurationElement("", editFieldFactory2Attributes, "",
                new IConfigurationElement[0], execAttr2);
        TestExtension extension2 = new TestExtension(new IConfigurationElement[] { configEl2 }, IpsUIPlugin.PLUGIN_ID,
                IpsUIPlugin.EXTENSION_POINT_ID_EXTENSION_PROPERTY_EDIT_FIELD_FACTORY);

        TestExtensionPoint extPoint = new TestExtensionPoint(new IExtension[] { extension, extension2 },
                IpsUIPlugin.PLUGIN_ID, IpsUIPlugin.EXTENSION_POINT_ID_EXTENSION_PROPERTY_EDIT_FIELD_FACTORY);

        // TableFormatPropertiesControlFactory setup code
        TableFormatConfigurationCompositeFactory tableFormatPropertiesFactory = new TableFormatConfigurationCompositeFactory() {
            private ITableFormat tableFormat;

            @Override
            public Composite createPropertyComposite(Composite parent, UIToolkit toolkit) {
                return null;
            }

            @Override
            protected void setTableFormat(ITableFormat tableFormat) {
                this.tableFormat = tableFormat;
            }

            @Override
            public MessageList validate() {
                MessageList messageList = new MessageList();
                if (tableFormat == null) {
                    messageList.add(Message.newError("ERROR", "Table format not set."));
                }
                return messageList;
            }
        };

        execAttr = new HashMap();
        execAttr.put("class", TestTableFormat.class.getName());
        execAttr.put("guiClass", tableFormatPropertiesFactory);
        HashMap attributes = new HashMap();
        attributes.put("defaultExtension", ".ext");
        attributes.put("guiClass", tableFormatPropertiesFactory.getClass().getName());
        attributes.put("class", TestTableFormat.class.getName());
        configEl = new TestConfigurationElement("guiClass", attributes, null, new IConfigurationElement[0], execAttr);
        extension = new TestExtension(new IConfigurationElement[] { configEl }, IpsPlugin.PLUGIN_ID,
                "externalTableFormat");
        TestExtensionPoint tableFormatExtPoint = new TestExtensionPoint(new IExtension[] { extension },
                IpsPlugin.PLUGIN_ID, "externalTableFormat");
        IpsUIPlugin.getDefault().setExtensionRegistry(
                new TestExtensionRegistry(new IExtensionPoint[] { extPoint, tableFormatExtPoint }));
    }

    @Test
    public void testGetExtensionPropertyEditFieldFactory() throws Exception {
        IExtensionPropertyEditFieldFactory resultFactory = IpsUIPlugin.getDefault()
                .getExtensionPropertyEditFieldFactory("additionalProperty");
        assertEquals("False factory for additionalProperty", editFieldFactory, resultFactory);
        resultFactory = IpsUIPlugin.getDefault().getExtensionPropertyEditFieldFactory("additionalProperty2");
        assertEquals("False factory for additionalProperty2", editFieldFactory2, resultFactory);
    }

    @Test
    public void testGetExtensionPropertyEditFieldFactoryNotRegistered() throws Exception {
        IExtensionPropertyEditFieldFactory resultFactory = IpsUIPlugin.getDefault()
                .getExtensionPropertyEditFieldFactory("notRegisteredProperty");
        assertTrue(resultFactory instanceof DefaultExtensionPropertyEditFieldFactory);
    }

    @Test
    public void testGetTableFormatPropertiesControlFactory() throws CoreException {
        ITableFormat tableFormatWithFactory = new TestTableFormat();
        ITableFormat tableFormatWithoutFactory = new TestTableFormatTwo();

        TableFormatConfigurationCompositeFactory resultFactory = IpsUIPlugin.getDefault()
                .getTableFormatPropertiesControlFactory(tableFormatWithFactory);
        assertNotNull(resultFactory);

        resultFactory = IpsUIPlugin.getDefault().getTableFormatPropertiesControlFactory(tableFormatWithoutFactory);
        assertNull(resultFactory);
    }

    // TODO test cases for loading extension points

}
