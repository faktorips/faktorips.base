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

package org.faktorips.devtools.core.ui.controls.chooser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class AbstractSubsetChooserModelTest {

    protected AbstractSubsetChooserModel setUpModel(List<ListChooserValue> allValues,
            List<ListChooserValue> resultingValues) {
        AbstractSubsetChooserModel model = mock(AbstractSubsetChooserModel.class);
        when(model.getAllValues()).thenReturn(allValues);
        when(model.getResultingValues()).thenReturn(resultingValues);
        when(model.getPreDefinedValues()).thenCallRealMethod();
        return model;
    }

    protected List<ListChooserValue> letModelCalculatePredefinedValues(List<ListChooserValue> allValues,
            List<ListChooserValue> resultingValues) {
        AbstractSubsetChooserModel model = setUpModel(allValues, resultingValues);
        List<ListChooserValue> preDefinedValues = model.getPreDefinedValues();
        return preDefinedValues;
    }

    @Test
    public void calculatePredefinedValuesBothEmpty() {
        List<ListChooserValue> allValues = new ArrayList<ListChooserValue>();
        List<ListChooserValue> resultingValues = new ArrayList<ListChooserValue>();
        List<ListChooserValue> preDefinedValues = letModelCalculatePredefinedValues(allValues, resultingValues);
        assertTrue(preDefinedValues.isEmpty());
    }

    @Test
    public void calculatePredefinedValuesEmptyAllValues() {
        List<ListChooserValue> allValues = new ArrayList<ListChooserValue>();
        List<ListChooserValue> resultingValues = new ArrayList<ListChooserValue>();
        resultingValues.add(new ListChooserValue("A"));
        resultingValues.add(new ListChooserValue("B"));
        resultingValues.add(new ListChooserValue("1"));
        resultingValues.add(new ListChooserValue("2"));

        List<ListChooserValue> preDefinedValues = letModelCalculatePredefinedValues(allValues, resultingValues);
        assertTrue(preDefinedValues.isEmpty());
    }

    @Test
    public void calculatePredefinedValuesEmptyResultingValues() {
        List<ListChooserValue> allValues = new ArrayList<ListChooserValue>();
        allValues.add(new ListChooserValue("A"));
        allValues.add(new ListChooserValue("B"));
        allValues.add(new ListChooserValue("1"));
        allValues.add(new ListChooserValue("2"));
        List<ListChooserValue> resultingValues = new ArrayList<ListChooserValue>();

        List<ListChooserValue> preDefinedValues = letModelCalculatePredefinedValues(allValues, resultingValues);
        assertFalse(preDefinedValues.isEmpty());
        assertEquals(4, preDefinedValues.size());
    }

    @Test
    public void calculatePredefinedValuesAndTestOrder() {
        List<ListChooserValue> allValues = new ArrayList<ListChooserValue>();
        allValues.add(new ListChooserValue("A"));
        allValues.add(new ListChooserValue("B"));
        allValues.add(new ListChooserValue("1"));
        allValues.add(new ListChooserValue("2"));
        List<ListChooserValue> resultingValues = new ArrayList<ListChooserValue>();
        resultingValues.add(new ListChooserValue("1"));
        resultingValues.add(new ListChooserValue("B"));

        List<ListChooserValue> preDefinedValues = letModelCalculatePredefinedValues(allValues, resultingValues);
        assertFalse(preDefinedValues.isEmpty());
        assertEquals(2, preDefinedValues.size());
        assertEquals("A", preDefinedValues.get(0).getValue());
        assertEquals("2", preDefinedValues.get(1).getValue());
    }

}
