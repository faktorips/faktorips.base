/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.faktorips.abstracttest.test.XmlAbstractTestCase;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.values.LocalizedString;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class InternationalStringTest extends XmlAbstractTestCase {

    @Test
    public void testAdd() throws Exception {
        List<PropertyChangeEvent> eventList = new ArrayList<>();
        InternationalString internationalString = new InternationalString(eventList::add);

        LocalizedString localizedStringEn = new LocalizedString(Locale.ENGLISH, "englishValue");
        internationalString.add(localizedStringEn);
        assertEquals(eventList.get(0).getPropertyName(), "localizedString");
        assertEquals(eventList.get(0).getNewValue(), localizedStringEn);
        assertEquals(eventList.size(), 1);

        assertEquals(localizedStringEn, internationalString.get(Locale.ENGLISH));

        eventList.clear();
        internationalString.add(localizedStringEn);
        assertEquals(eventList.size(), 0);
    }

    @Test
    public void testAdd_nullValue() throws Exception {
        PropertyChangeListener listener = mock(PropertyChangeListener.class);
        InternationalString internationalString = new InternationalString(listener);
        LocalizedString localizedString = new LocalizedString(Locale.GERMAN, null);

        internationalString.add(localizedString);

        Collection<LocalizedString> values = internationalString.values();
        assertThat(values, hasItem(new LocalizedString(Locale.GERMAN, IpsStringUtils.EMPTY)));
    }

    @Test
    public void testValues() throws Exception {
        InternationalString internationalString = new InternationalString();

        LocalizedString localizedStringEn = new LocalizedString(Locale.ENGLISH, IpsStringUtils.EMPTY);

        LocalizedString localizedStringDe = new LocalizedString(Locale.GERMAN, IpsStringUtils.EMPTY);

        LocalizedString localizedStringFr = new LocalizedString(Locale.FRENCH, IpsStringUtils.EMPTY);

        internationalString.add(localizedStringEn);
        internationalString.add(localizedStringDe);
        internationalString.add(localizedStringFr);

        assertEquals(3, internationalString.values().size());
        Iterator<LocalizedString> iterator = internationalString.values().iterator();
        assertEquals(localizedStringEn, iterator.next());
        assertEquals(localizedStringDe, iterator.next());
        assertEquals(localizedStringFr, iterator.next());
    }

    @Test
    public void testHasValueFor() throws Exception {
        InternationalString internationalString = new InternationalString();

        LocalizedString localizedStringEn = new LocalizedString(Locale.ENGLISH, IpsStringUtils.EMPTY);

        LocalizedString localizedStringDe = new LocalizedString(Locale.GERMAN, "content");

        internationalString.add(localizedStringEn);
        internationalString.add(localizedStringDe);

        assertTrue(internationalString.hasValueFor(Locale.ENGLISH));
        assertTrue(internationalString.hasValueFor(Locale.GERMAN));
        assertFalse(internationalString.hasValueFor(Locale.FRENCH));
    }

    @Test
    public void testInitFromXml() throws Exception {
        Document doc = getTestDocument();
        Element root = doc.getDocumentElement();

        InternationalString internationalString = new InternationalString();
        internationalString.initFromXml(root);

        LocalizedString expectedDe = new LocalizedString(Locale.GERMAN, "bläblä");
        LocalizedString expectedEn = new LocalizedString(Locale.ENGLISH, "blabla");

        assertEquals(expectedDe, internationalString.get(Locale.GERMAN));
        assertEquals(expectedEn, internationalString.get(Locale.ENGLISH));

        Collection<LocalizedString> values = internationalString.values();
        assertEquals(2, values.size());

        Iterator<LocalizedString> iterator = values.iterator();
        assertEquals(expectedEn, iterator.next());
        assertEquals(expectedDe, iterator.next());
    }

    @Test
    public void testToXml() throws Exception {
        InternationalString internationalString = new InternationalString();

        LocalizedString expectedDe = new LocalizedString(Locale.GERMAN, "bläblä");
        LocalizedString expectedEn = new LocalizedString(Locale.ENGLISH, "blabla");

        internationalString.add(expectedEn);
        internationalString.add(expectedDe);

        Element xml = internationalString.toXml(getTestDocument());

        InternationalString copy = new InternationalString();
        copy.initFromXml(xml);

        assertEquals(expectedDe, copy.get(Locale.GERMAN));
        assertEquals(expectedEn, copy.get(Locale.ENGLISH));

        Collection<LocalizedString> values = copy.values();
        assertEquals(2, values.size());

        Iterator<LocalizedString> iterator = values.iterator();
        assertEquals(expectedEn, iterator.next());
        assertEquals(expectedDe, iterator.next());
    }

    @Test
    public void testEquals() {
        InternationalString internationalString = new InternationalString();

        LocalizedString expectedDe = new LocalizedString(Locale.GERMAN, "bläblä");
        LocalizedString expectedEn = new LocalizedString(Locale.ENGLISH, "blabla");

        internationalString.add(expectedEn);
        internationalString.add(expectedDe);

        InternationalString internationalStringCopy = new InternationalString();
        internationalStringCopy.add(expectedDe);
        internationalStringCopy.add(expectedEn);

        assertTrue(internationalString.equals(internationalStringCopy));

        LocalizedString expectedDe2 = new LocalizedString(Locale.GERMAN, "bläbläblup");
        LocalizedString expectedEn2 = new LocalizedString(Locale.ENGLISH, "blabla");

        internationalStringCopy = new InternationalString();
        internationalStringCopy.add(expectedEn2);
        internationalStringCopy.add(expectedDe2);

        assertFalse(internationalString.equals(internationalStringCopy));
    }
}
