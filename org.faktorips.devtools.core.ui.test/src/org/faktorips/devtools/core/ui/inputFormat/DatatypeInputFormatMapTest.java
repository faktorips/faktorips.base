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

package org.faktorips.devtools.core.ui.inputFormat;

import java.util.Map;

import junit.framework.Assert;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.controller.fields.IInputFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DatatypeInputFormatMapTest {

    @Mock
    private IDatatypeInputFormatFactory factory1;

    @Mock
    private IDatatypeInputFormatFactory factory2;

    @Mock
    private ValueDatatype datatype1;

    @Mock
    private ValueDatatype datatype2;

    @Mock
    private IInputFormat<String> result;

    private Map<ValueDatatype, IDatatypeInputFormatFactory> map;

    private DatatypeInputFormatMap inputFormatMap;

    @Before
    public void initMap() {
        inputFormatMap = new DatatypeInputFormatMap();
        map = inputFormatMap.getInputFormatMap();
        Mockito.when(factory1.newInputFormat(datatype1)).thenReturn(result);
        Mockito.when(factory2.newInputFormat(datatype2)).thenReturn(result);
    }

    @Test
    public void test_getDatatypeInputFormat() {
        map.put(datatype1, factory1);
        map.put(datatype2, factory2);

        IInputFormat<String> actualResult = inputFormatMap.getDatatypeInputFormat(datatype1);
        Assert.assertEquals(result, actualResult);
    }
}
