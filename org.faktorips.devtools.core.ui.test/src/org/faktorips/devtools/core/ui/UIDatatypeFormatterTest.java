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

package org.faktorips.devtools.core.ui;

import junit.framework.Assert;

import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.junit.Test;
import org.mockito.Mockito;

public class UIDatatypeFormatterTest {

    @Test
    public void testFormatEnumValueSet() {
        EnumValueSet enumValueSet = Mockito.mock(EnumValueSet.class);
        Mockito.when(enumValueSet.getValues()).thenReturn(new String[] { "1", "two", "three" });

        UIDatatypeFormatter formatter = new UIDatatypeFormatter();
        String formatString = formatter.formatValueSet(enumValueSet);
        Assert.assertEquals("[1 | two | three]", formatString);
    }

    @Test
    public void testFormatRangeValueSet() {
        RangeValueSet rangeValueSet = Mockito.mock(RangeValueSet.class);
        Mockito.when(rangeValueSet.getLowerBound()).thenReturn("1");
        Mockito.when(rangeValueSet.getUpperBound()).thenReturn("11");
        Mockito.when(rangeValueSet.getStep()).thenReturn("5");

        UIDatatypeFormatter formatter = new UIDatatypeFormatter();
        String formatString = formatter.formatValueSet(rangeValueSet);
        Assert.assertTrue("Formatted Range must start with lower and upper bound", formatString.startsWith("[1-11]"));
        Assert.assertTrue("Formatted Range must end with step value", formatString.endsWith("5"));
    }

    @Test
    public void testFormatRangeValueSetUnlimited() {
        RangeValueSet rangeValueSet = Mockito.mock(RangeValueSet.class);
        Mockito.when(rangeValueSet.getLowerBound()).thenReturn("1");
        Mockito.when(rangeValueSet.getUpperBound()).thenReturn(null);
        Mockito.when(rangeValueSet.getStep()).thenReturn("5");

        UIDatatypeFormatter formatter = new UIDatatypeFormatter();
        String formatString = formatter.formatValueSet(rangeValueSet);
        Assert.assertTrue("Formatted Range must start with lower and upper bound",
                formatString.startsWith("[1-unlimited]"));
        Assert.assertTrue("Formatted Range must end with step value", formatString.endsWith("5"));
    }

}
