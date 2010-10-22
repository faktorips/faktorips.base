/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.Locale;

import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractPageElement;

public class LanguageSupportTest extends AbstractXmlUnitHtmlExportTest {

    public void testDescriptionLanguage() throws Exception {
        String deBeschreibung = "Deutsche Beschreibung";
        String deXPath = "//div[.='" + deBeschreibung + "']";

        String enBeschreibung = "English Description";
        String enXPath = "//div[.='" + enBeschreibung + "']";

        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragProdukt");

        IDescription deDescription = policy.newDescription();
        deDescription.setLocale(Locale.GERMANY);
        deDescription.setText(deBeschreibung);

        IDescription enDescription = policy.newDescription();
        enDescription.setLocale(Locale.ENGLISH);
        enDescription.setText(enBeschreibung);

        config.setDescriptionLocale(Locale.GERMANY);
        AbstractPageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(policy.getIpsSrcFile(),
                config);
        assertXPathExists(objectContentPage, deXPath);

        config.setDescriptionLocale(Locale.ENGLISH);
        objectContentPage = ContentPageUtil.createObjectContentPageElement(policy.getIpsSrcFile(), config);
        assertXPathExists(objectContentPage, enXPath);
    }
}
