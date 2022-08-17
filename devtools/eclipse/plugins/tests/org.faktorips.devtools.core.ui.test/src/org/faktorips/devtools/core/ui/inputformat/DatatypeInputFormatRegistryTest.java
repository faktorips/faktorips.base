/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.inputformat;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.GregorianCalendarAsDateDatatype;
import org.faktorips.datatype.classtypes.GregorianCalendarDatatype;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
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
    @Mock
    private IIpsProject ipsProject;

    private Map<Class<? extends ValueDatatype>, IDatatypeInputFormatFactory> map;

    private DatatypeInputFormatRegistry inputFormatMap;

    @Before
    public void initMap() {
        inputFormatMap = new DatatypeInputFormatRegistry();
        map = inputFormatMap.getInputFormatMap();
        Mockito.when(factory1.newInputFormat(datatype1, ipsProject)).thenReturn(result1);
        Mockito.when(factory2.newInputFormat(datatype2, ipsProject)).thenReturn(result2);
        map.put(datatype1.getClass(), factory1);
        map.put(datatype2.getClass(), factory2);
    }

    @Test
    public void test_getDatatypeInputFormat() {
        IInputFormat<String> actualResult = inputFormatMap.getDatatypeInputFormat(datatype1, ipsProject);
        assertEquals(result1, actualResult);
        IInputFormat<String> actualResult2 = inputFormatMap.getDatatypeInputFormat(datatype2, ipsProject);
        assertEquals(result2, actualResult2);
    }

    @Test
    public void test_getDefaultInputFormat() {
        IInputFormat<String> actualResult = inputFormatMap.getDatatypeInputFormat(unregisteredDatatype, ipsProject);
        assertEquals(DefaultInputFormat.class, actualResult.getClass());
    }

    @Test
    public void test_superclassInputFormat() {
        Mockito.when(factory1.newInputFormat(subclassDatatype, ipsProject)).thenReturn(result1);
        IInputFormat<String> actualResult = inputFormatMap.getDatatypeInputFormat(subclassDatatype, ipsProject);
        assertEquals(result1, actualResult);
    }

    @Test
    public void test_useNearestSuperclassInputFormat() {
        map.put(subclassDatatype.getClass(), subclassFactory);
        Mockito.when(subclassFactory.newInputFormat(subsubclassDatatype, ipsProject)).thenReturn(subclassResult);

        IInputFormat<String> actualResult = inputFormatMap.getDatatypeInputFormat(subsubclassDatatype, ipsProject);
        assertEquals(subclassResult, actualResult);
    }

    private static class GregoriacCalendarSubDatatype extends GregorianCalendarAsDateDatatype {
        @Override
        public int hashCode() {
            return super.hashCode() * 31 + 13;
        }
    }
}
