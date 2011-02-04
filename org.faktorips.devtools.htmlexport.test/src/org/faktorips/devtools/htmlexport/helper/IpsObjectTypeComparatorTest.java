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

package org.faktorips.devtools.htmlexport.helper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import junit.framework.TestCase;

import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.htmlexport.helper.IpsObjectTypeComparator;

public class IpsObjectTypeComparatorTest extends TestCase {

    private IpsObjectTypeComparator comparator = new IpsObjectTypeComparator();

    public void testComparatorProductDefinition() {
        IpsObjectType nonProductDefinitionIpsObjectType = mock(IpsObjectType.class);
        when(nonProductDefinitionIpsObjectType.isProductDefinitionType()).thenReturn(false);

        IpsObjectType productDefinitionIpsObjectType = mock(IpsObjectType.class);
        when(productDefinitionIpsObjectType.isProductDefinitionType()).thenReturn(true);

        assertTrue(comparator.compare(nonProductDefinitionIpsObjectType, productDefinitionIpsObjectType) < 0);
        assertTrue(comparator.compare(productDefinitionIpsObjectType, nonProductDefinitionIpsObjectType) > 0);
    }

    public void testComparatorDatatype() {
        IpsObjectType nonDatatypeIpsObjectType = mock(IpsObjectType.class);
        when(nonDatatypeIpsObjectType.isProductDefinitionType()).thenReturn(false);
        when(nonDatatypeIpsObjectType.isDatatype()).thenReturn(false);

        IpsObjectType datatypeIpsObjectType = mock(IpsObjectType.class);
        when(datatypeIpsObjectType.isProductDefinitionType()).thenReturn(false);
        when(nonDatatypeIpsObjectType.isDatatype()).thenReturn(true);

        assertTrue(comparator.compare(nonDatatypeIpsObjectType, datatypeIpsObjectType) < 0);
        assertTrue(comparator.compare(datatypeIpsObjectType, nonDatatypeIpsObjectType) > 0);
    }

    public void testComparatorEqual() {
        IpsObjectType firstIpsObjectType = mock(IpsObjectType.class);
        when(firstIpsObjectType.isProductDefinitionType()).thenReturn(false);
        when(firstIpsObjectType.isDatatype()).thenReturn(false);

        IpsObjectType secondIpsObjectType = mock(IpsObjectType.class);
        when(secondIpsObjectType.isProductDefinitionType()).thenReturn(false);
        when(firstIpsObjectType.isDatatype()).thenReturn(false);

        assertEquals(0, comparator.compare(firstIpsObjectType, secondIpsObjectType));
        assertEquals(0, comparator.compare(secondIpsObjectType, firstIpsObjectType));
    }
}
