/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
