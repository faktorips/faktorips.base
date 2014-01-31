/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestConfigurationElement;
import org.faktorips.abstracttest.TestExtensionRegistry;
import org.faktorips.abstracttest.TestMockingUtils;
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
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // PropertyEditFieldFactory setup code
        editFieldFactory = new IExtensionPropertyEditFieldFactory() {
            @Override
            public EditField<?> newEditField(IIpsObjectPartContainer ipsObjectPart,
                    Composite extensionArea,
                    UIToolkit toolkit) {
                return null;
            }
        };
        editFieldFactory2 = new IExtensionPropertyEditFieldFactory() {
            @Override
            public EditField<?> newEditField(IIpsObjectPartContainer ipsObjectPart,
                    Composite extensionArea,
                    UIToolkit toolkit) {
                return null;
            }
        };
        HashMap<String, Object> execAttr = new HashMap<String, Object>();
        execAttr.put("class", editFieldFactory);
        editFieldFactoryAttributes = new HashMap<String, String>();
        editFieldFactoryAttributes.put("propertyId", "additionalProperty");
        TestConfigurationElement configEl = new TestConfigurationElement("", editFieldFactoryAttributes, "",
                new IConfigurationElement[0], execAttr);
        IExtension extension = TestMockingUtils.mockExtension(IpsUIPlugin.PLUGIN_ID + "."
                + IpsUIPlugin.EXTENSION_POINT_ID_EXTENSION_PROPERTY_EDIT_FIELD_FACTORY, configEl);

        HashMap<String, Object> execAttr2 = new HashMap<String, Object>();
        execAttr2.put("class", editFieldFactory2);
        Map<String, String> editFieldFactory2Attributes = new HashMap<String, String>();
        editFieldFactory2Attributes.put("propertyId", "additionalProperty2");
        TestConfigurationElement configEl2 = new TestConfigurationElement("", editFieldFactory2Attributes, "",
                new IConfigurationElement[0], execAttr2);
        IExtension extension2 = TestMockingUtils.mockExtension(IpsUIPlugin.PLUGIN_ID + "."
                + IpsUIPlugin.EXTENSION_POINT_ID_EXTENSION_PROPERTY_EDIT_FIELD_FACTORY, configEl2);

        IExtensionPoint extPoint = TestMockingUtils.mockExtensionPoint(IpsUIPlugin.PLUGIN_ID,
                IpsUIPlugin.EXTENSION_POINT_ID_EXTENSION_PROPERTY_EDIT_FIELD_FACTORY, extension, extension2);

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

        execAttr = new HashMap<String, Object>();
        execAttr.put("class", TestTableFormat.class.getName());
        execAttr.put("guiClass", tableFormatPropertiesFactory);
        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("defaultExtension", ".ext");
        attributes.put("guiClass", tableFormatPropertiesFactory.getClass().getName());
        attributes.put("class", TestTableFormat.class.getName());
        configEl = new TestConfigurationElement("guiClass", attributes, null, new IConfigurationElement[0], execAttr);
        extension = TestMockingUtils.mockExtension(IpsPlugin.PLUGIN_ID + "." + "externalTableFormat", configEl);
        IExtensionPoint tableFormatExtPoint = TestMockingUtils.mockExtensionPoint(IpsPlugin.PLUGIN_ID,
                "externalTableFormat", extension);
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

    @Test
    public void testGetDefaultValidityDate() {
        GregorianCalendar date = new GregorianCalendar(1986, 4, 16);
        IpsUIPlugin.getDefault().setDefaultValidityDate(date);

        assertEquals(date, IpsUIPlugin.getDefault().getDefaultValidityDate());
    }

    @Test
    public void testGetDefaultValidityDate_ReturnSameDateTwiceIfNotSet() throws InterruptedException {
        clearDefaultValidityDate();

        GregorianCalendar defaultValidityDate = IpsUIPlugin.getDefault().getDefaultValidityDate();
        Thread.sleep(5);

        assertEquals(defaultValidityDate, defaultValidityDate);
    }

    @Test
    public void testGetDefaultValidityDate_TimeIsSetToZero() {
        clearDefaultValidityDate();

        GregorianCalendar defaultValidityDate = IpsUIPlugin.getDefault().getDefaultValidityDate();
        assertEquals(0, defaultValidityDate.get(Calendar.SECOND));
        assertEquals(0, defaultValidityDate.get(Calendar.MINUTE));
        assertEquals(0, defaultValidityDate.get(Calendar.HOUR));
        assertEquals(0, defaultValidityDate.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, defaultValidityDate.get(Calendar.MILLISECOND));
    }

    @Test
    public void testGetDefaultValidityDate_TimeIsSetToZeroEvenAfterDefaultValidityHasBeenChanged() {
        GregorianCalendar now = new GregorianCalendar();
        IpsUIPlugin.getDefault().setDefaultValidityDate(now);

        GregorianCalendar defaultValidityDate = IpsUIPlugin.getDefault().getDefaultValidityDate();
        assertEquals(0, defaultValidityDate.get(Calendar.SECOND));
        assertEquals(0, defaultValidityDate.get(Calendar.MINUTE));
        assertEquals(0, defaultValidityDate.get(Calendar.HOUR));
        assertEquals(0, defaultValidityDate.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, defaultValidityDate.get(Calendar.MILLISECOND));
    }

    private void clearDefaultValidityDate() {
        String pluginId = IpsUIPlugin.getDefault().getBundle().getSymbolicName();
        IEclipsePreferences node = new InstanceScope().getNode(pluginId);
        node.remove(IpsUIPlugin.PREFERENCE_ID_DEFAULT_VALIDITY_DATE);
    }

    // TODO test cases for loading extension points

}
