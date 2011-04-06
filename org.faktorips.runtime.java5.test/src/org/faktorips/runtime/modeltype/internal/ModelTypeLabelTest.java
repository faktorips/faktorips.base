/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.modeltype.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.faktorips.runtime.modeltype.IModelTypeLabel;
import org.faktorips.runtime.modeltype.IModelElement;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ModelTypeLabelTest {

    @Mock
    private IModelElement parentModelElement;

    private IModelTypeLabel label;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        label = new ModelTypeLabel(parentModelElement);
    }

    @Test
    public void testGetModelElement() {
        assertSame(parentModelElement, label.getModelElement());
    }

    @Test
    public void testInitFromXml() throws XMLStreamException {
        XMLStreamReader parser = mock(XMLStreamReader.class);
        when(parser.getAttributeCount()).thenReturn(3);
        when(parser.getAttributeLocalName(0)).thenReturn("locale");
        when(parser.getAttributeValue(0)).thenReturn("de");
        when(parser.getAttributeLocalName(1)).thenReturn("value");
        when(parser.getAttributeValue(1)).thenReturn("foo");
        when(parser.getAttributeLocalName(2)).thenReturn("pluralValue");
        when(parser.getAttributeValue(2)).thenReturn("bar");

        label.initFromXml(parser);

        assertEquals(new Locale("de"), label.getLocale());
        assertEquals("foo", label.getValue());
        assertEquals("bar", label.getPluralValue());
    }

}
