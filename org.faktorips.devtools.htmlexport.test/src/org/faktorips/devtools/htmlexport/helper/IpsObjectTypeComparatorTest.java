/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.junit.Test;

public class IpsObjectTypeComparatorTest {

    private IpsObjectTypeComparator comparator = new IpsObjectTypeComparator();

    @Test
    public void testComparatorProductDefinition() {
        IpsObjectType modellIpsObjectType = mock(IpsObjectType.class);
        when(modellIpsObjectType.isProductDefinitionType()).thenReturn(false);

        IpsObjectType productDefinitionIpsObjectType = mock(IpsObjectType.class);
        when(productDefinitionIpsObjectType.isProductDefinitionType()).thenReturn(true);

        assertTrue(comparator.compare(modellIpsObjectType, productDefinitionIpsObjectType) < 0);
        assertTrue(comparator.compare(productDefinitionIpsObjectType, modellIpsObjectType) > 0);
    }

    @Test
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

    @Test
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

    @Test
    public void testComparatorStandardObjectTypes() {
        assertTrue(comparator.compare(IpsObjectType.PRODUCT_CMPT_TYPE, IpsObjectType.POLICY_CMPT_TYPE) < 0);
        assertTrue(comparator.compare(IpsObjectType.POLICY_CMPT_TYPE, IpsObjectType.ENUM_TYPE) < 0);
        assertTrue(comparator.compare(IpsObjectType.ENUM_TYPE, IpsObjectType.TABLE_STRUCTURE) < 0);
        assertTrue(comparator.compare(IpsObjectType.TABLE_STRUCTURE, IpsObjectType.PRODUCT_CMPT) < 0);
        assertTrue(comparator.compare(IpsObjectType.PRODUCT_CMPT, IpsObjectType.ENUM_CONTENT) < 0);
        assertTrue(comparator.compare(IpsObjectType.ENUM_CONTENT, IpsObjectType.TABLE_CONTENTS) < 0);
    }

    @Test
    public void testComparatorZusaetzlicheObjectTypes() {
        IpsObjectType modellIpsObjectType = mock(IpsObjectType.class);
        when(modellIpsObjectType.isProductDefinitionType()).thenReturn(false);

        IpsObjectType productDefinitionIpsObjectType = mock(IpsObjectType.class);
        when(productDefinitionIpsObjectType.isProductDefinitionType()).thenReturn(true);

        assertEquals(0, comparator.compare(modellIpsObjectType, modellIpsObjectType));
        assertEquals(0, comparator.compare(productDefinitionIpsObjectType, productDefinitionIpsObjectType));

        assertTrue(comparator.compare(modellIpsObjectType, productDefinitionIpsObjectType) < 0);

        assertTrue(comparator.compare(IpsObjectType.PRODUCT_CMPT_TYPE, modellIpsObjectType) < 0);
        assertTrue(comparator.compare(IpsObjectType.POLICY_CMPT_TYPE, modellIpsObjectType) < 0);
        assertTrue(comparator.compare(IpsObjectType.ENUM_TYPE, modellIpsObjectType) < 0);
        assertTrue(comparator.compare(IpsObjectType.TABLE_STRUCTURE, modellIpsObjectType) < 0);
        assertTrue(comparator.compare(IpsObjectType.PRODUCT_CMPT, modellIpsObjectType) > 0);
        assertTrue(comparator.compare(IpsObjectType.ENUM_CONTENT, modellIpsObjectType) > 0);
        assertTrue(comparator.compare(IpsObjectType.TABLE_CONTENTS, modellIpsObjectType) > 0);

        assertTrue(comparator.compare(IpsObjectType.PRODUCT_CMPT_TYPE, productDefinitionIpsObjectType) < 0);
        assertTrue(comparator.compare(IpsObjectType.POLICY_CMPT_TYPE, productDefinitionIpsObjectType) < 0);
        assertTrue(comparator.compare(IpsObjectType.ENUM_TYPE, productDefinitionIpsObjectType) < 0);
        assertTrue(comparator.compare(IpsObjectType.TABLE_STRUCTURE, productDefinitionIpsObjectType) < 0);
        assertTrue(comparator.compare(IpsObjectType.PRODUCT_CMPT, productDefinitionIpsObjectType) < 0);
        assertTrue(comparator.compare(IpsObjectType.ENUM_CONTENT, productDefinitionIpsObjectType) < 0);
        assertTrue(comparator.compare(IpsObjectType.TABLE_CONTENTS, productDefinitionIpsObjectType) < 0);
    }

}
