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

package org.faktorips.devtools.htmlexport.test.documentor;

import java.util.Locale;

import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpt.AttributeValue;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.type.IAttribute;

public class LanguageSupportTest extends AbstractHtmlExportTest {

    public void testDescriptionLanguage() throws Exception {

        String deBeschreibung = "Deutsche Beschreibung";
        String enBeschreibung = "English Description";

        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "VertragDesc", "VertragDescProdukt");

        IDescription deDescription = policy.getDescription(Locale.GERMANY);
        if (deDescription == null) {
            deDescription = policy.newDescription();
            deDescription.setLocale(Locale.GERMANY);
        }
        deDescription.setText(deBeschreibung);

        IDescription enDescription = policy.getDescription(Locale.ENGLISH);
        if (enDescription == null) {
            enDescription = policy.newDescription();
            enDescription.setLocale(Locale.ENGLISH);
        }
        enDescription.setText(enBeschreibung);

        config.setDescriptionLocale(Locale.GERMANY);

        assertEquals(deBeschreibung, config.getDescription(policy));

        config.setDescriptionLocale(Locale.ENGLISH);

        assertEquals(enBeschreibung, config.getDescription(policy));
    }

    public void testLabelLanguage() throws Exception {
        String deLabel = "Currywurst";
        String enLabel = "Haggis";
        String name = "Schonkost";

        ProductCmptType type = newProductCmptType(ipsProject, "ProduktTyp");
        ProductCmpt productCmpt = newProductCmpt(type, "Produkt");
        IAttribute attribute = type.newAttribute();
        attribute.setName(name);

        IAttributeValue value = new AttributeValue(productCmpt.newGeneration(), "xxx", attribute.getName(), "yxz");
        assertEquals(name, config.getCaption(value));

        config.setDescriptionLocale(Locale.GERMAN);
        attribute.setLabelValue(Locale.GERMAN, deLabel);

        assertEquals(deLabel, config.getCaption(value));

        config.setDescriptionLocale(Locale.ENGLISH);
        assertEquals(deLabel, config.getCaption(value));

        attribute.setLabelValue(Locale.ENGLISH, enLabel);
        assertEquals(enLabel, config.getCaption(value));
    }
}
