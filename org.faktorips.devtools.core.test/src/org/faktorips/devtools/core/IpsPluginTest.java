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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsproject.IIpsLoggingFrameworkConnector;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.xml.sax.SAXException;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsPluginTest extends AbstractIpsPluginTest {

    private final IpsPlugin ipsPlugin = IpsPlugin.getDefault();

    private IpsPreferences pref;

    private String oldPresentationString;

    private IIpsProject ipsProject;

    private IPolicyCmptType policyCmptType;

    public IpsPluginTest() {
        super();
    }

    public IpsPluginTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws CoreException {
        pref = IpsPlugin.getDefault().getIpsPreferences();
        oldPresentationString = pref.getNullPresentation();

        ipsProject = newIpsProject();
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.addSupportedLanguage(Locale.GERMAN);
        properties.addSupportedLanguage(Locale.US);
        properties.setDefaultLanguage(properties.getSupportedLanguage(Locale.GERMAN));
        ipsProject.setProperties(properties);

        policyCmptType = newPolicyCmptType(ipsProject, "TestPolicy");
    }

    @Override
    protected void tearDownExtension() {
        pref.setNullPresentation(oldPresentationString);
    }

    public void testGetDocumentBuilder() throws UnsupportedEncodingException, SAXException, IOException {
        DocumentBuilder docBuilder = IpsPlugin.getDefault().newDocumentBuilder();
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><abc/>";
        docBuilder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    public void testGetLocalizedDescriptionLocalizationLocaleDescriptionExistent() {
        IDescription description = policyCmptType.newDescription();
        description.setLocale(ipsPlugin.getLocalizationLocale());
        description.setText("foo");

        assertEquals("foo", ipsPlugin.getLocalizedDescription(policyCmptType));
    }

    public void testGetLocalizedDescriptionDefaultLocaleExistent() {
        IDescription description = policyCmptType.newDescription();
        description.setLocale(Locale.GERMAN);
        description.setText("foo");

        assertEquals("foo", ipsPlugin.getLocalizedDescription(policyCmptType));
    }

    public void testGetLocalizedDescriptionNoneExistent() {
        assertEquals("", ipsPlugin.getDefaultDescription(policyCmptType));
    }

    public void testGetDefaultDescription() {
        assertEquals("", ipsPlugin.getDefaultDescription(policyCmptType));

        IDescription description = policyCmptType.newDescription();
        description.setLocale(Locale.GERMAN);
        description.setText("foo");

        assertEquals("foo", ipsPlugin.getDefaultDescription(policyCmptType));
    }

    public void testIpsPreferencesInclListener() {
        MyPropertyChangeListener listener = new MyPropertyChangeListener();
        IPreferenceStore store = IpsPlugin.getDefault().getPreferenceStore();
        store.addPropertyChangeListener(listener);
        pref.setNullPresentation("-");
        assertEquals("-", pref.getNullPresentation());
        assertNotNull(listener.lastEvent);
        assertEquals("-", listener.lastEvent.getNewValue());
    }

    class MyPropertyChangeListener implements IPropertyChangeListener {

        PropertyChangeEvent lastEvent;

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            lastEvent = event;
        }
    }

    public void testGetIpsLoggingFrameworkConnectors() {
        IIpsLoggingFrameworkConnector[] connectors = IpsPlugin.getDefault().getIpsLoggingFrameworkConnectors();
        List<String> connectorIds = new ArrayList<String>();
        for (IIpsLoggingFrameworkConnector connector : connectors) {
            connectorIds.add(connector.getId());
        }
        assertTrue(connectorIds.contains("org.faktorips.devtools.core.javaUtilLoggingConnector"));
    }

}
