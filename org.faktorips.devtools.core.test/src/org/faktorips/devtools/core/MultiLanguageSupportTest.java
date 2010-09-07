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

package org.faktorips.devtools.core;

import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;

/**
 * @author Alexander Weickmann
 */
public class MultiLanguageSupportTest extends AbstractIpsPluginTest {

    private MultiLanguageSupport support;

    private IIpsProject ipsProject;

    private IPolicyCmptType policyCmptType;

    private IDescription germanDescription;

    private IDescription usDescription;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        support = IpsPlugin.getMultiLanguageSupport();

        ipsProject = newIpsProject();
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.addSupportedLanguage(Locale.GERMAN);
        properties.addSupportedLanguage(Locale.US);
        properties.setDefaultLanguage(properties.getSupportedLanguage(Locale.GERMAN));
        ipsProject.setProperties(properties);

        policyCmptType = newPolicyCmptType(ipsProject, "TestPolicy");
        germanDescription = policyCmptType.getDescription(Locale.GERMAN);
        germanDescription.setText("German");
        usDescription = policyCmptType.getDescription(Locale.US);
        usDescription.setText("US");
    }

    public void testGetLocalizedDescription() {
        IDescription description = policyCmptType.getDescription(support.getLocalizationLocale());
        if (description == null) {
            description = policyCmptType.newDescription();
            description.setLocale(support.getLocalizationLocale());
        }
        description.setText("foo");

        assertEquals("foo", support.getLocalizedDescription(policyCmptType));
    }

    public void testGetLocalizedDescriptionLocalizedDescriptionMissing() {
        usDescription.delete();
        assertEquals(germanDescription.getText(), support.getLocalizedDescription(policyCmptType));
    }

    public void testGetLocalizedDescriptionLocalizedAndDefaultDescriptionMissing() {
        germanDescription.delete();
        usDescription.delete();
        assertEquals("", support.getDefaultDescription(policyCmptType));
    }

    public void testGetDefaultDescription() {
        assertEquals("German", support.getDefaultDescription(policyCmptType));
    }

}
