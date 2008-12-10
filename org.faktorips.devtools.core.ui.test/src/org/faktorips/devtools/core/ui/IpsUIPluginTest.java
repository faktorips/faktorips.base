/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.TestConfigurationElement;
import org.faktorips.devtools.core.TestExtension;
import org.faktorips.devtools.core.TestExtensionPoint;
import org.faktorips.devtools.core.TestExtensionRegistry;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.ui.controller.EditField;

public class IpsUIPluginTest extends AbstractIpsPluginTest {

    private IExtensionPropertyEditFieldFactory factory;
    @SuppressWarnings("unchecked")
    private Map attributes; 
    
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        super.setUp();
        factory = new IExtensionPropertyEditFieldFactory() {
            public EditField newEditField(IIpsObjectPartContainer ipsObjectPart,
                    Composite extensionArea,
                    UIToolkit toolkit) {
                return null;
            }
        };
        HashMap execAttr = new HashMap();
        execAttr.put("class", factory);
        attributes = new HashMap();
        attributes.put("propertyId", "additionalProperty");
        TestConfigurationElement configEl = new TestConfigurationElement("", attributes, "",
                new IConfigurationElement[0], execAttr);
        TestExtension extension = new TestExtension(new IConfigurationElement[] { configEl }, IpsUIPlugin.PLUGIN_ID,
                IpsUIPlugin.EXTENSION_POINT_ID_EXTENSION_PROPERTY_EDIT_FIELD_FACTORY);
        TestExtensionPoint extPoint = new TestExtensionPoint(new IExtension[] { extension }, IpsUIPlugin.PLUGIN_ID,
                IpsUIPlugin.EXTENSION_POINT_ID_EXTENSION_PROPERTY_EDIT_FIELD_FACTORY);
        IpsUIPlugin.getDefault().setExtensionRegistry(new TestExtensionRegistry(new IExtensionPoint[] { extPoint }));
    }

    
    public void testGetExtensionPropertyEditFieldFactory() throws Exception {
        IExtensionPropertyEditFieldFactory resultFactory = IpsUIPlugin.getDefault()
                .getExtensionPropertyEditFieldFactory("additionalProperty");
        assertEquals(factory, resultFactory);

    }
    
    public void testGetExtensionPropertyEditFieldFactoryNotRegistered() throws Exception {
        IExtensionPropertyEditFieldFactory resultFactory = IpsUIPlugin.getDefault()
        .getExtensionPropertyEditFieldFactory("notRegisteredProperty");
        assertTrue(resultFactory instanceof DefaultExtensionPropertyEditFieldFactory);
        
    }

}
