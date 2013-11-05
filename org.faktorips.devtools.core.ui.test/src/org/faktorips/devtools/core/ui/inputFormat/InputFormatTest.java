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

import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.controller.fields.IInputFormat;
import org.faktorips.devtools.core.ui.inputFormat.IInputFormatFactory;
import org.faktorips.devtools.core.ui.inputFormat.InputFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InputFormatTest {

    private static final String QUALIFIED_NAME = "test.Test";
    private static final String NOT_QUALIFIED_NAME = "notTest.Test";

    @Mock
    private ValueDatatype inputValueDatatype;

    @Mock
    private ValueDatatype datatype1;

    @Mock
    private ValueDatatype datatype2;

    @Mock
    private IInputFormatFactory<String> formatFactory;

    @Mock
    private IInputFormat<String> result;

    @Mock
    Map<ValueDatatype, IInputFormatFactory<String>> map;

    private InputFormat inputFormat;

    @Before
    public void setUp() {
        initMap();
        inputFormat = new InputFormat(map);

        when(inputValueDatatype.getQualifiedName()).thenReturn(QUALIFIED_NAME);
        when(formatFactory.newInputFormat(inputValueDatatype)).thenReturn(result);
    }

    private void initMap() {
        Set<ValueDatatype> setValueDatatypes = new HashSet<ValueDatatype>();
        setValueDatatypes.add(datatype1);
        setValueDatatypes.add(datatype2);

        when(map.keySet()).thenReturn(setValueDatatypes);
        when(map.get(datatype1)).thenReturn(formatFactory);
        when(map.get(datatype2)).thenReturn(formatFactory);
    }

    @Test
    public void test_getInputFormat_AllWithDifferentQualifiedNames() {
        when(datatype1.getQualifiedName()).thenReturn(NOT_QUALIFIED_NAME);
        when(datatype2.getQualifiedName()).thenReturn(NOT_QUALIFIED_NAME);

        IInputFormat<String> format = inputFormat.getInputFormat(inputValueDatatype);
        Assert.assertEquals(null, format);
    }

    @Test
    public void test_getInputFormat_SomeWithSameQualifiedNames() {
        when(datatype1.getQualifiedName()).thenReturn(QUALIFIED_NAME);
        when(datatype2.getQualifiedName()).thenReturn(QUALIFIED_NAME);

        IInputFormat<String> format = inputFormat.getInputFormat(inputValueDatatype);
        Assert.assertEquals(result, format);
    }

    @Test
    public void test_getInputFormat_SomeWIthDifferentQualifiedNames() {
        when(datatype1.getQualifiedName()).thenReturn(NOT_QUALIFIED_NAME);
        when(datatype2.getQualifiedName()).thenReturn(QUALIFIED_NAME);

        IInputFormat<String> format = inputFormat.getInputFormat(inputValueDatatype);
        Assert.assertEquals(result, format);
    }
}
