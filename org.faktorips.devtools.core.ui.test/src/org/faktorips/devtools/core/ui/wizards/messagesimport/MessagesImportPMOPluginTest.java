/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.messagesimport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.junit.Before;
import org.junit.Test;

public class MessagesImportPMOPluginTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot fragmentRoot;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject();
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.addSupportedLanguage(Locale.ENGLISH);
        properties.addSupportedLanguage(Locale.GERMAN);
        properties.addSupportedLanguage(Locale.FRENCH);
        properties.setDefaultLanguage(Locale.FRENCH);
        ipsProject.setProperties(properties);

        fragmentRoot = newIpsPackageFragmentRoot(ipsProject, null, "root");
    }

    @Test
    public void testDefaultValues() {
        MessagesImportPMO pmo = new MessagesImportPMO();

        assertEquals(";", pmo.getColumnDelimiter());
        assertEquals("1", pmo.getIdentifierColumnIndex());
        assertEquals("2", pmo.getTextColumnIndex());
    }

    @Test
    public void testUpdateSupportedLanguage() {
        MessagesImportPMO pmo = new MessagesImportPMO();

        assertNull(pmo.getSupportedLanguage());

        pmo.setIpsPackageFragmentRoot(fragmentRoot);

        assertEquals(ipsProject.getProperties().getDefaultLanguage(), pmo.getSupportedLanguage());
    }
}
