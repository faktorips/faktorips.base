/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.internal.InternationalString;
import org.faktorips.devtools.model.internal.productcmpt.AttributeValue;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.values.LocalizedString;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class InternationalStringValueTest extends AbstractIpsPluginTest {

    private static final String ENGLISCH_BAR = "bar";
    private static final String GERMAN_FOO = "foo";
    private LocalizedString expectedDe;
    private LocalizedString expectedEn;
    private InternationalStringValue internationalStringValue;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        expectedDe = new LocalizedString(Locale.GERMAN, GERMAN_FOO);
        expectedEn = new LocalizedString(Locale.ENGLISH, ENGLISCH_BAR);
        internationalStringValue = new InternationalStringValue();
    }

    @Test
    public void testGetContent() {
        assertNotNull(new InternationalStringValue().getContent());
    }

    @Test
    public void testGetContentAsString() {
        assertNotNull(new InternationalStringValue().getContentAsString());
        internationalStringValue.getContent().add(expectedDe);
        internationalStringValue.getContent().add(expectedEn);
        assertEquals("de=foo|en=bar", internationalStringValue.getContentAsString());
    }

    @Test
    public void testCreateFromXml() {
        Element el = getTestDocument().getDocumentElement();
        InternationalStringValue internationalStringValueXml = InternationalStringValue.createFromXml(el);
        IInternationalString intString = internationalStringValueXml.getContent();
        assertEquals(2, intString.values().size());
        assertEquals("Versicherung", intString.get(Locale.GERMAN).getValue());
        assertEquals("Insurance", intString.get(Locale.ENGLISH).getValue());
    }

    @Test
    public void testToXml() {
        Document testDocument = getTestDocument();
        assertNotNull(new InternationalStringValue().toXml(testDocument));

        internationalStringValue.getContent().add(expectedDe);
        internationalStringValue.getContent().add(expectedEn);
        Element xml = (Element)internationalStringValue.toXml(testDocument);
        assertNotNull(xml);
        InternationalString copy = new InternationalString();
        copy.initFromXml(xml);
        assertEquals(2, copy.values().size());
        assertEquals(GERMAN_FOO, copy.get(Locale.GERMAN).getValue());
        assertEquals(ENGLISCH_BAR, copy.get(Locale.ENGLISH).getValue());

    }

    @Test
    public void testToString() {
        assertNotNull(internationalStringValue.toString());

        internationalStringValue.getContent().add(expectedDe);
        internationalStringValue.getContent().add(expectedEn);
        assertNotNull(internationalStringValue.toString());
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testEquals() {
        internationalStringValue.getContent().add(expectedDe);
        internationalStringValue.getContent().add(expectedEn);

        assertTrue(internationalStringValue.equals(internationalStringValue));
        assertNotNull(internationalStringValue);
        assertFalse(internationalStringValue.equals(new String()));

        InternationalStringValue internationalStringValue2 = new InternationalStringValue();
        internationalStringValue2.getContent().add(expectedDe);
        internationalStringValue2.getContent().add(expectedEn);

        assertFalse(internationalStringValue.equals(new InternationalStringValue()));
        assertTrue(new InternationalStringValue().equals(new InternationalStringValue()));
        assertFalse(new InternationalStringValue().equals(internationalStringValue));

        InternationalStringValue internationalStringValue3 = new InternationalStringValue();

        LocalizedString expectedDe2 = new LocalizedString(Locale.GERMAN, ENGLISCH_BAR);
        LocalizedString expectedEn2 = new LocalizedString(Locale.ENGLISH, GERMAN_FOO);

        internationalStringValue3.getContent().add(expectedEn2);
        internationalStringValue3.getContent().add(expectedDe2);

        assertFalse(internationalStringValue.equals(internationalStringValue3));
    }

    @Test
    public void testValidate() {
        IIpsProject ipsProject = newIpsProject();
        MessageList list = new MessageList();

        // no Value set
        internationalStringValue.validate(null, null, ipsProject, list, new ObjectProperty(this, "Test"));
        assertEquals(0, list.size());

        list = new MessageList();
        // german and english ok
        internationalStringValue = new InternationalStringValue();
        internationalStringValue.getContent().add(expectedDe);
        internationalStringValue.getContent().add(expectedEn);
        internationalStringValue.validate(null, null, ipsProject, list, new ObjectProperty(this, "Test"));
        assertEquals(0, list.size());

        list = new MessageList();
        // german an empty english
        internationalStringValue = new InternationalStringValue();
        internationalStringValue.getContent().add(new LocalizedString(Locale.GERMAN, ENGLISCH_BAR));
        internationalStringValue.getContent().add(new LocalizedString(Locale.ENGLISH, ""));
        internationalStringValue.validate(null, null, ipsProject, list, new ObjectProperty(this, "Test"));
        assertEquals(1, list.size());
        Message messageByCode = list.getMessageByCode(AttributeValue.MSGCODE_MULTILINGUAL_NOT_SET);
        assertNotNull(messageByCode);

        list = new MessageList();
        // german null
        internationalStringValue = new InternationalStringValue();
        internationalStringValue.getContent().add(new LocalizedString(Locale.GERMAN, null));
        internationalStringValue.getContent().add(new LocalizedString(Locale.ENGLISH, ENGLISCH_BAR));
        internationalStringValue.validate(null, null, ipsProject, list, new ObjectProperty(this, "Test"));
        assertEquals(1, list.size());
        messageByCode = list.getMessageByCode(AttributeValue.MSGCODE_MULTILINGUAL_NOT_SET);
        assertNotNull(messageByCode);

        list = new MessageList();
        internationalStringValue = new InternationalStringValue();
        internationalStringValue.getContent().add(new LocalizedString(Locale.ENGLISH, ENGLISCH_BAR));
        internationalStringValue.validate(null, null, ipsProject, list, new ObjectProperty(this, "Test"));
        assertEquals(1, list.size());
        messageByCode = list.getMessageByCode(AttributeValue.MSGCODE_MULTILINGUAL_NOT_SET);
        assertNotNull(messageByCode);

        list = new MessageList();
        internationalStringValue = new InternationalStringValue();
        internationalStringValue.getContent().add(new LocalizedString(Locale.ENGLISH, ""));
        internationalStringValue.validate(null, null, ipsProject, list, new ObjectProperty(this, "Test"));
        assertEquals(0, list.size());
    }

    @Test
    public void testGetLocalizedContentWithLocal() {
        internationalStringValue.getContent().add(expectedDe);
        internationalStringValue.getContent().add(expectedEn);

        assertEquals(GERMAN_FOO, internationalStringValue.getLocalizedContent(Locale.GERMAN));
        assertEquals(ENGLISCH_BAR, internationalStringValue.getLocalizedContent(Locale.ENGLISH));
        assertEquals(IpsStringUtils.EMPTY, internationalStringValue.getLocalizedContent(Locale.US));
    }

    @Test
    public void testGetLocalizedContentDefault() {
        IIpsProject ipsProject = newIpsProject();

        internationalStringValue.getContent().add(expectedDe);
        internationalStringValue.getContent().add(expectedEn);

        assertEquals(GERMAN_FOO, internationalStringValue.getDefaultLocalizedContent(ipsProject));
    }

    @Test
    public void testLocalizedContent() {
        internationalStringValue = new InternationalStringValue();
        internationalStringValue.getContent().add(new LocalizedString(Locale.ENGLISH, IpsStringUtils.EMPTY));
        internationalStringValue.getContent().add(expectedDe);

        assertEquals(GERMAN_FOO, internationalStringValue.getLocalizedContent());
    }

    @Test
    public void testLocalizedContentEmpty() {
        internationalStringValue = new InternationalStringValue();
        internationalStringValue.getContent().add(new LocalizedString(Locale.ENGLISH, IpsStringUtils.EMPTY));
        internationalStringValue.getContent().add(new LocalizedString(Locale.GERMAN, IpsStringUtils.EMPTY));

        assertEquals(IpsStringUtils.EMPTY, internationalStringValue.getLocalizedContent());
    }
}
