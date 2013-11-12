/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.inputformat;

import java.util.Map;

import junit.framework.Assert;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.GregorianCalendarAsDateDatatype;
import org.faktorips.datatype.classtypes.GregorianCalendarDatatype;
import org.faktorips.devtools.core.ui.inputformat.DatatypeInputFormatRegistry;
import org.faktorips.devtools.core.ui.inputformat.DefaultInputFormat;
import org.faktorips.devtools.core.ui.inputformat.IDatatypeInputFormatFactory;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DatatypeInputFormatRegistryTest {

    @Mock
    private IDatatypeInputFormatFactory factory1;
    @Mock
    private IDatatypeInputFormatFactory factory2;
    @Mock
    private IDatatypeInputFormatFactory subclassFactory;

    private ValueDatatype datatype1 = new GregorianCalendarDatatype("Date", false);
    private ValueDatatype subclassDatatype = Datatype.GREGORIAN_CALENDAR;
    private ValueDatatype subsubclassDatatype = new GregoriacCalendarSubDatatype();
    @Mock
    private ValueDatatype datatype2;
    private ValueDatatype unregisteredDatatype = Datatype.PRIMITIVE_INT;

    @Mock
    private IInputFormat<String> result1;
    @Mock
    private IInputFormat<String> result2;
    @Mock
    private IInputFormat<String> subclassResult;

    private Map<Class<? extends ValueDatatype>, IDatatypeInputFormatFactory> map;

    private DatatypeInputFormatRegistry inputFormatMap;

    @Before
    public void initMap() {
        inputFormatMap = new DatatypeInputFormatRegistry();
        map = inputFormatMap.getInputFormatMap();
        Mockito.when(factory1.newInputFormat(datatype1)).thenReturn(result1);
        Mockito.when(factory2.newInputFormat(datatype2)).thenReturn(result2);
        map.put(datatype1.getClass(), factory1);
        map.put(datatype2.getClass(), factory2);
    }

    @Test
    public void test_getDatatypeInputFormat() {
        IInputFormat<String> actualResult = inputFormatMap.getDatatypeInputFormat(datatype1);
        Assert.assertEquals(result1, actualResult);
        IInputFormat<String> actualResult2 = inputFormatMap.getDatatypeInputFormat(datatype2);
        Assert.assertEquals(result2, actualResult2);
    }

    @Test
    public void test_getDefaultInputFormat() {
        IInputFormat<String> actualResult = inputFormatMap.getDatatypeInputFormat(unregisteredDatatype);
        Assert.assertEquals(DefaultInputFormat.class, actualResult.getClass());
    }

    @Test
    public void test_superclassInputFormat() {
        Mockito.when(factory1.newInputFormat(subclassDatatype)).thenReturn(result1);
        IInputFormat<String> actualResult = inputFormatMap.getDatatypeInputFormat(subclassDatatype);
        Assert.assertEquals(result1, actualResult);
    }

    @Test
    public void test_useNearestSuperclassInputFormat() {
        map.put(subclassDatatype.getClass(), subclassFactory);
        Mockito.when(subclassFactory.newInputFormat(subsubclassDatatype)).thenReturn(subclassResult);

        IInputFormat<String> actualResult = inputFormatMap.getDatatypeInputFormat(subsubclassDatatype);
        Assert.assertEquals(subclassResult, actualResult);
    }

    private static class GregoriacCalendarSubDatatype extends GregorianCalendarAsDateDatatype {
        @Override
        public int hashCode() {
            return super.hashCode() * 31 + 13;
        }
    }
}
