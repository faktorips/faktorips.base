/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.context.messages;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.faktorips.devtools.htmlexport.context.AbstractHtmlExportPluginTest;
import org.faktorips.devtools.model.internal.productcmpt.AttributeValue;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IAttribute;
import org.junit.Test;

public class LanguageSupportTest extends AbstractHtmlExportPluginTest {

    @Test
    public void testDescriptionLanguage() throws Exception {

        String deBeschreibung = "Deutsche Beschreibung";
        String enBeschreibung = "English Description";

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

        assertEquals(deBeschreibung, context.getDescription(policy));

        context.setDocumentationLocale(Locale.ENGLISH);

        assertEquals(enBeschreibung, context.getDescription(policy));
    }

    @Test
    public void testLabelLanguage() throws Exception {
        String deLabel = "Currywurst";
        String enLabel = "Haggis";
        String name = "Schonkost";

        IProductCmptType type = newProductCmptType(ipsProject, "ProduktTyp");
        IProductCmpt productCmpt = newProductCmpt(type, "Produkt");
        IAttribute attribute = type.newAttribute();
        attribute.setName(name);

        IAttributeValue value = new AttributeValue((IProductCmptGeneration)productCmpt.newGeneration(), "xxx",
                attribute.getName());
        value.setValueHolder(new SingleValueHolder(value, "yxz"));
        assertEquals(name, context.getCaption(value));

        context.setDocumentationLocale(Locale.GERMAN);
        attribute.setLabelValue(Locale.GERMAN, deLabel);

        assertEquals(deLabel, context.getCaption(value));

        context.setDocumentationLocale(Locale.ENGLISH);
        assertEquals(deLabel, context.getCaption(value));

        attribute.setLabelValue(Locale.ENGLISH, enLabel);
        assertEquals(enLabel, context.getCaption(value));
    }
}
