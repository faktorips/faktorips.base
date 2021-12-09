/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestConfigurationElement;
import org.faktorips.abstracttest.TestExtensionRegistry;
import org.faktorips.abstracttest.TestMockingUtils;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.wizards.deepcopy.DefaultDeepCopySmartModeBehavior;
import org.faktorips.devtools.core.ui.wizards.deepcopy.IAdditionalDeepCopyWizardPage;
import org.faktorips.devtools.core.ui.wizards.deepcopy.IDeepCopySmartModeBehavior;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IpsUIPluginTest extends AbstractIpsPluginTest {

    private static final String LINK_GIF = "over_co.gif";

    private static final String PRODUCT_GIF = "Table.gif";

    private IExtensionPropertyEditFieldFactory editFieldFactory;

    private Map<String, String> editFieldFactoryAttributes;

    private IExtensionPropertyEditFieldFactory editFieldFactory2;

    private IExtensionRegistry oldRegistry;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // PropertyEditFieldFactory setup code
        editFieldFactory = (ipsObjectPart, extensionArea, toolkit) -> null;
        editFieldFactory2 = (ipsObjectPart, extensionArea, toolkit) -> null;
        HashMap<String, Object> execAttr = new HashMap<>();
        execAttr.put("class", editFieldFactory);
        editFieldFactoryAttributes = new HashMap<>();
        editFieldFactoryAttributes.put("propertyId", "additionalProperty");
        TestConfigurationElement configEl = new TestConfigurationElement("", editFieldFactoryAttributes, "",
                new IConfigurationElement[0], execAttr);
        IExtension extension = TestMockingUtils.mockExtension(
                IpsUIPlugin.PLUGIN_ID + "." + IpsUIPlugin.EXTENSION_POINT_ID_EXTENSION_PROPERTY_EDIT_FIELD_FACTORY,
                configEl);

        HashMap<String, Object> execAttr2 = new HashMap<>();
        execAttr2.put("class", editFieldFactory2);
        Map<String, String> editFieldFactory2Attributes = new HashMap<>();
        editFieldFactory2Attributes.put("propertyId", "additionalProperty2");
        TestConfigurationElement configEl2 = new TestConfigurationElement("", editFieldFactory2Attributes, "",
                new IConfigurationElement[0], execAttr2);
        IExtension extension2 = TestMockingUtils.mockExtension(
                IpsUIPlugin.PLUGIN_ID + "." + IpsUIPlugin.EXTENSION_POINT_ID_EXTENSION_PROPERTY_EDIT_FIELD_FACTORY,
                configEl2);

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

        execAttr = new HashMap<>();
        execAttr.put("class", TestTableFormat.class.getName());
        execAttr.put("guiClass", tableFormatPropertiesFactory);
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("defaultExtension", ".ext");
        attributes.put("guiClass", tableFormatPropertiesFactory.getClass().getName());
        attributes.put("class", TestTableFormat.class.getName());
        configEl = new TestConfigurationElement("guiClass", attributes, null, new IConfigurationElement[0], execAttr);
        extension = TestMockingUtils.mockExtension(IpsPlugin.PLUGIN_ID + "." + "externalTableFormat", configEl);
        IExtensionPoint tableFormatExtPoint = TestMockingUtils.mockExtensionPoint(IpsPlugin.PLUGIN_ID,
                "externalTableFormat", extension);
        oldRegistry = IpsUIPlugin.getDefault().setExtensionRegistry(
                new TestExtensionRegistry(new IExtensionPoint[] { extPoint, tableFormatExtPoint }));
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        IpsUIPlugin.getDefault().setExtensionRegistry(oldRegistry);
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
    public void testGetTableFormatPropertiesControlFactory() throws CoreRuntimeException {
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
        IEclipsePreferences node = InstanceScope.INSTANCE.getNode(pluginId);
        node.remove(IpsUIPlugin.PREFERENCE_ID_DEFAULT_VALIDITY_DATE);
    }

    @Test
    public void testGetSharedOverlayImage() throws Exception {
        ImageDescriptor sharedOverlayImage = IpsUIPlugin.getImageHandling().getSharedOverlayImageDescriptor(PRODUCT_GIF,
                LINK_GIF,
                IDecoration.BOTTOM_LEFT);
        Image sharedImage = IpsUIPlugin.getImageHandling().getSharedImage(PRODUCT_GIF, false);
        ImageDescriptor sharedOverlayByImage = IpsUIPlugin.getImageHandling()
                .getSharedOverlayImageDescriptor(sharedImage, LINK_GIF, IDecoration.BOTTOM_LEFT);

        assertNotNull(sharedOverlayImage);
        assertSame(sharedOverlayByImage, sharedOverlayImage);
    }

    @Test
    public void testGetSharedOverlayImage_Empty() throws Exception {
        ImageDescriptor sharedOverlayImage = IpsUIPlugin.getImageHandling().getSharedOverlayImageDescriptor(
                StringUtils.EMPTY,
                StringUtils.EMPTY, IDecoration.BOTTOM_LEFT);

        assertNotNull(sharedOverlayImage);
        assertSame(IpsUIPlugin.getImageHandling().getSharedImageDescriptor(StringUtils.EMPTY, true),
                sharedOverlayImage);
    }

    @Test
    public void testGetSharedOverlayImageDescriptor() throws Exception {
        ImageDescriptor prodCmptTypeGif = IpsUIPlugin.getImageHandling().createImageDescriptor(PRODUCT_GIF);
        Image baseImage = IpsUIPlugin.getImageHandling().createImage(prodCmptTypeGif);

        ImageDescriptor resultImageD = IpsUIPlugin.getImageHandling().getSharedOverlayImageDescriptor(baseImage,
                LINK_GIF, IDecoration.BOTTOM_LEFT);
        ImageDescriptor overlayedImageDescriptor = IpsUIPlugin.getImageHandling()
                .getSharedImageDescriptor(LINK_GIF + "_" + baseImage.hashCode(), false);

        assertNotNull(resultImageD);
        assertSame(overlayedImageDescriptor, resultImageD);
    }

    @Test
    public void testGetDeepCopySmartModeBehavior_NoExtensionDefined() {
        IpsUIPlugin.getDefault().setExtensionRegistry(new TestExtensionRegistry(
                new IExtensionPoint[] { TestMockingUtils.mockExtensionPoint(IpsUIPlugin.PLUGIN_ID,
                        IAdditionalDeepCopyWizardPage.EXTENSION_POINT_ID_DEEP_COPY_WIZARD) }));
        IpsUIPlugin.getDefault().initDeepCopySmartModeBehavior();

        IDeepCopySmartModeBehavior deepCopySmartModeBehavior = IpsUIPlugin.getDefault().getDeepCopySmartModeBehavior();

        assertThat(deepCopySmartModeBehavior, is(instanceOf(DefaultDeepCopySmartModeBehavior.class)));
    }

    @Test
    public void testGetDeepCopySmartModeBehavior_ExtensionDefined() throws Exception {
        IDeepCopySmartModeBehavior testDeepCopySmartModeBehavior = new DefaultDeepCopySmartModeBehavior();
        mockBehaviorExtensions(testDeepCopySmartModeBehavior);

        IDeepCopySmartModeBehavior deepCopySmartModeBehavior = IpsUIPlugin.getDefault().getDeepCopySmartModeBehavior();

        assertThat(deepCopySmartModeBehavior, is(sameInstance(testDeepCopySmartModeBehavior)));
    }

    @Test
    public void testGetDeepCopySmartModeBehavior_MultipleExtensionsDefined() throws Exception {
        IDeepCopySmartModeBehavior testDeepCopySmartModeBehavior1 = new DefaultDeepCopySmartModeBehavior();
        IDeepCopySmartModeBehavior testDeepCopySmartModeBehavior2 = new DefaultDeepCopySmartModeBehavior();
        ILogListener listener = (status, plugin) -> {
            assertEquals(IStatus.ERROR, status.getSeverity());
            assertThat(status.getMessage(),
                    containsString(IAdditionalDeepCopyWizardPage.EXTENSION_POINT_ID_DEEP_COPY_WIZARD));
            assertThat(status.getMessage(),
                    containsString(IDeepCopySmartModeBehavior.CONFIG_ELEMENT_ID_SMART_MODE_BEHAVIOR));
        };
        try {
            IpsPlugin.getDefault().getLog().addLogListener(listener);
            mockBehaviorExtensions(testDeepCopySmartModeBehavior1, testDeepCopySmartModeBehavior2);
        } finally {
            IpsPlugin.getDefault().getLog().removeLogListener(listener);
        }

        IDeepCopySmartModeBehavior deepCopySmartModeBehavior = IpsUIPlugin.getDefault().getDeepCopySmartModeBehavior();

        assertThat(deepCopySmartModeBehavior, is(instanceOf(DefaultDeepCopySmartModeBehavior.class)));
        assertThat(deepCopySmartModeBehavior, is(not(sameInstance(testDeepCopySmartModeBehavior1))));
        assertThat(deepCopySmartModeBehavior, is(not(sameInstance(testDeepCopySmartModeBehavior2))));
    }

    private void mockBehaviorExtensions(IDeepCopySmartModeBehavior... testDeepCopySmartModeBehaviors) {
        IExtension[] extensions = new IExtension[testDeepCopySmartModeBehaviors.length];
        for (int i = 0; i < testDeepCopySmartModeBehaviors.length; i++) {
            Map<String, Object> executableExtensionMap = new HashMap<>();
            executableExtensionMap.put("class", testDeepCopySmartModeBehaviors[i]);
            extensions[i] = TestMockingUtils.mockExtension("TestDeepCopySmartModeBehavior",
                    new TestConfigurationElement(IDeepCopySmartModeBehavior.CONFIG_ELEMENT_ID_SMART_MODE_BEHAVIOR,
                            new HashMap<String, String>(), null, new IConfigurationElement[0], executableExtensionMap));
        }
        IExtensionPoint extensionPoint = TestMockingUtils.mockExtensionPoint(IpsUIPlugin.PLUGIN_ID,
                IAdditionalDeepCopyWizardPage.EXTENSION_POINT_ID_DEEP_COPY_WIZARD, extensions);
        IpsUIPlugin.getDefault()
                .setExtensionRegistry(new TestExtensionRegistry(new IExtensionPoint[] { extensionPoint }));
        IpsUIPlugin.getDefault().initDeepCopySmartModeBehavior();
    }

}
