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

package org.faktorips.devtools.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsLoggingFrameworkConnector;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsPluginTest extends AbstractIpsPluginTest {

    private IpsPreferences pref;

    private String oldPresentationString;

    public IpsPluginTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws CoreException {
        pref = IpsPlugin.getDefault().getIpsPreferences();
        oldPresentationString = pref.getNullPresentation();
    }

    @Override
    protected void tearDownExtension() {
        pref.setNullPresentation(oldPresentationString);
    }

    @Test
    public void testGetDocumentBuilder() throws UnsupportedEncodingException, SAXException, IOException {
        DocumentBuilder docBuilder = IpsPlugin.getDefault().getDocumentBuilder();
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><abc/>";
        docBuilder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    @Test
    public void testIpsPreferencesInclListener() {
        MyPropertyChangeListener listener = new MyPropertyChangeListener();
        IPreferenceStore store = IpsPlugin.getDefault().getPreferenceStore();
        store.addPropertyChangeListener(listener);
        pref.setNullPresentation("-");
        assertEquals("-", pref.getNullPresentation());
        assertNotNull(listener.lastEvent);
        assertEquals("-", listener.lastEvent.getNewValue());
    }

    @Test
    public void testGetIpsLoggingFrameworkConnectors() {
        IIpsLoggingFrameworkConnector[] connectors = IpsPlugin.getDefault().getIpsLoggingFrameworkConnectors();
        List<String> connectorIds = new ArrayList<String>();
        for (IIpsLoggingFrameworkConnector connector : connectors) {
            connectorIds.add(connector.getId());
        }
        assertTrue(connectorIds.contains("org.faktorips.devtools.core.javaUtilLoggingConnector"));
    }

    class MyPropertyChangeListener implements IPropertyChangeListener {

        PropertyChangeEvent lastEvent;

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            lastEvent = event;
        }
    }

}
