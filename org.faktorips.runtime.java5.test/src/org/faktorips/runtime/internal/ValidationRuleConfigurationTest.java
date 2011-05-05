/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.internal;

import static org.junit.Assert.assertEquals;

import org.faktorips.runtime.XmlAbstractTestCase;
import org.junit.Test;
import org.w3c.dom.Element;

public class ValidationRuleConfigurationTest extends XmlAbstractTestCase {

    @Test
    public void testConstructor() {
        Element docEl = getTestDocument().getDocumentElement();
        Element ruleElement = (Element)docEl.getElementsByTagName("ValidationRuleConfig").item(0);

        ValidationRuleConfiguration config = new ValidationRuleConfiguration(ruleElement);
        assertEquals("Regel1", config.getRuleName());
        assertEquals(true, config.isActive());

        ruleElement = (Element)docEl.getElementsByTagName("ValidationRuleConfig").item(1);
        config = new ValidationRuleConfiguration(ruleElement);
        assertEquals("RegelZwei", config.getRuleName());
        assertEquals(false, config.isActive());
    }

}
