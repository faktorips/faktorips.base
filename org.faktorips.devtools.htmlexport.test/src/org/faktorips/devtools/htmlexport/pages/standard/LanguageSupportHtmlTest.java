/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.standard;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.faktorips.devtools.htmlexport.context.messages.LanguageSupportTest;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmptGeneration;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.junit.Test;

/**
 * Testet die Ausgabe der Labels und Descriptions in Html, die prinzipielle Funktion testet der
 * {@link LanguageSupportTest}
 * 
 * @author dicker
 */
public class LanguageSupportHtmlTest extends AbstractXmlUnitHtmlExportTest {

    @Test
    public void testDescriptionLanguage() throws Exception {

        String deBeschreibung = "Deutsche Beschreibung";
        String deXPath = "//div[.='" + deBeschreibung + "']";

        String enBeschreibung = "English Description";
        String enXPath = "//div[.='" + enBeschreibung + "']";

        IPolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "VertragDesc", "VertragDescProdukt");

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

        context.setDocumentationLocale(Locale.GERMANY);
        IPageElement objectContentPage = ContentPageUtil
                .createObjectContentPageElement(policy.getIpsSrcFile(), context);
        assertXPathExists(objectContentPage, deXPath);

        context.setDocumentationLocale(Locale.ENGLISH);
        objectContentPage = ContentPageUtil.createObjectContentPageElement(policy.getIpsSrcFile(), context);
        assertXPathExists(objectContentPage, enXPath);
    }

    @Test
    public void testLabelLanguage() throws Exception {
        String deLabel = "Currywurst";
        String enLabel = "Haggis";
        String name = "Schonkost";

        IProductCmptType type = newProductCmptType(ipsProject, "ProduktTyp");
        IProductCmpt productCmpt = newProductCmpt(type, "Produkt");
        IProductCmptTypeAttribute attribute = (IProductCmptTypeAttribute)type.newAttribute();
        attribute.setName(name);
        attribute.setDatatype("String");

        ProductCmptGeneration generation = (ProductCmptGeneration)productCmpt.getFirstGeneration();
        IAttributeValue value = generation.newAttributeValue(attribute);
        value.setValueHolder(new SingleValueHolder(value, "yxz"));

        String deXPath = "//table[@id='" + productCmpt.getName() + "_ProductGenerationAttributeTable']//tr/td[1][.='"
                + deLabel + "']";
        String enXPath = "//table[@id='" + productCmpt.getName() + "_ProductGenerationAttributeTable']//tr/td[1][.='"
                + enLabel + "']";

        context.setDocumentationLocale(Locale.GERMAN);
        attribute.setLabelValue(Locale.GERMAN, deLabel);

        IPageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(productCmpt.getIpsSrcFile(),
                context);
        assertXPathExists(objectContentPage, deXPath);

        assertEquals(deLabel, context.getCaption(value));

        context.setDocumentationLocale(Locale.ENGLISH);
        objectContentPage = ContentPageUtil.createObjectContentPageElement(productCmpt.getIpsSrcFile(), context);
        assertXPathExists(objectContentPage, deXPath);

        attribute.setLabelValue(Locale.ENGLISH, enLabel);
        objectContentPage = ContentPageUtil.createObjectContentPageElement(productCmpt.getIpsSrcFile(), context);
        assertXPathExists(objectContentPage, enXPath);
    }

}
