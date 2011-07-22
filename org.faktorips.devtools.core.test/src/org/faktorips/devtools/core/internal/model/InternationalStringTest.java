/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Observer;

import org.faktorips.abstracttest.test.XmlAbstractTestCase;
import org.faktorips.devtools.core.model.ILocalizedString;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class InternationalStringTest extends XmlAbstractTestCase {

    @Test
    public void testAdd() throws Exception {
        Observer observer = mock(Observer.class);
        InternationalString internationalString = new InternationalString(observer);

        LocalizedString localizedString = mock(LocalizedString.class);
        internationalString.add(localizedString);
        verify(observer).update(internationalString, localizedString);
        verifyNoMoreInteractions(observer);

        assertEquals(localizedString, internationalString.get(null));

        reset(observer);

        LocalizedString localizedStringEn = mock(LocalizedString.class);
        when(localizedStringEn.getLocale()).thenReturn(Locale.ENGLISH);
        internationalString.add(localizedStringEn);
        verify(observer).update(internationalString, localizedStringEn);
        verifyNoMoreInteractions(observer);

        assertEquals(localizedStringEn, internationalString.get(Locale.ENGLISH));

        reset(observer);
        internationalString.add(localizedStringEn);
        verifyZeroInteractions(observer);
    }

    @Test
    public void testValues() throws Exception {
        InternationalString internationalString = new InternationalString();

        LocalizedString localizedStringEn = mock(LocalizedString.class);
        when(localizedStringEn.getLocale()).thenReturn(Locale.ENGLISH);

        LocalizedString localizedStringDe = mock(LocalizedString.class);
        when(localizedStringDe.getLocale()).thenReturn(Locale.GERMAN);

        LocalizedString localizedStringFr = mock(LocalizedString.class);
        when(localizedStringFr.getLocale()).thenReturn(Locale.FRENCH);

        internationalString.add(localizedStringEn);
        internationalString.add(localizedStringDe);
        internationalString.add(localizedStringFr);

        assertEquals(3, internationalString.values().size());
        Iterator<ILocalizedString> iterator = internationalString.values().iterator();
        assertEquals(localizedStringEn, iterator.next());
        assertEquals(localizedStringDe, iterator.next());
        assertEquals(localizedStringFr, iterator.next());
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

        Collection<ILocalizedString> values = internationalString.values();
        assertEquals(2, values.size());

        Iterator<ILocalizedString> iterator = values.iterator();
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

        Collection<ILocalizedString> values = copy.values();
        assertEquals(2, values.size());

        Iterator<ILocalizedString> iterator = values.iterator();
        assertEquals(expectedEn, iterator.next());
        assertEquals(expectedDe, iterator.next());
    }

}
