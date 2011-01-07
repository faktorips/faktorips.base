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
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmptGeneration;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.test.context.LanguageSupportTest;

/**
 * Testet die Ausgabe der Labels und Descriptions in Html, die prinzipielle Funktion testet der
 * {@link LanguageSupportTest}
 * 
 * @author dicker
 */
public class LanguageSupportHtmlTest extends AbstractXmlUnitHtmlExportTest {

    public void testDescriptionLanguage() throws Exception {

        String deBeschreibung = "Deutsche Beschreibung";
        String deXPath = "//div[.='" + deBeschreibung + "']";

        String enBeschreibung = "English Description";
        String enXPath = "//div[.='" + enBeschreibung + "']";

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

        context.setDescriptionLocale(Locale.GERMANY);
        PageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(policy.getIpsSrcFile(),
                context);
        assertXPathExists(objectContentPage, deXPath);

        context.setDescriptionLocale(Locale.ENGLISH);
        objectContentPage = ContentPageUtil.createObjectContentPageElement(policy.getIpsSrcFile(), context);
        assertXPathExists(objectContentPage, enXPath);
    }

    public void testLabelLanguage() throws Exception {
        String deLabel = "Currywurst";
        String enLabel = "Haggis";
        String name = "Schonkost";

        ProductCmptType type = newProductCmptType(ipsProject, "ProduktTyp");
        ProductCmpt productCmpt = newProductCmpt(type, "Produkt");
        IProductCmptTypeAttribute attribute = (IProductCmptTypeAttribute)type.newAttribute();
        attribute.setName(name);
        attribute.setDatatype("String");

        ProductCmptGeneration generation = (ProductCmptGeneration)productCmpt.getFirstGeneration();
        IAttributeValue value = generation.newAttributeValue(attribute, "yxz");

        String deXPath = "//table[@id='" + productCmpt.getName() + "_ProductGenerationAttributeTable']//tr/td[1][.='"
                + deLabel + "']";
        String enXPath = "//table[@id='" + productCmpt.getName() + "_ProductGenerationAttributeTable']//tr/td[1][.='"
                + enLabel + "']";

        context.setDescriptionLocale(Locale.GERMAN);
        attribute.setLabelValue(Locale.GERMAN, deLabel);

        PageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(
                productCmpt.getIpsSrcFile(), context);
        assertXPathExists(objectContentPage, deXPath);

        assertEquals(deLabel, context.getCaption(value));

        context.setDescriptionLocale(Locale.ENGLISH);
        objectContentPage = ContentPageUtil.createObjectContentPageElement(productCmpt.getIpsSrcFile(), context);
        assertXPathExists(objectContentPage, deXPath);

        attribute.setLabelValue(Locale.ENGLISH, enLabel);
        objectContentPage = ContentPageUtil.createObjectContentPageElement(productCmpt.getIpsSrcFile(), context);
        assertXPathExists(objectContentPage, enXPath);
    }

}
