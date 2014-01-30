/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.context.messages;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpt.AttributeValue;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.htmlexport.context.AbstractHtmlExportPluginTest;
import org.junit.Test;

public class LanguageSupportTest extends AbstractHtmlExportPluginTest {

    @Test
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

        ProductCmptType type = newProductCmptType(ipsProject, "ProduktTyp");
        ProductCmpt productCmpt = newProductCmpt(type, "Produkt");
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
