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

package org.faktorips.devtools.core.ui.controls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.classtypes.DecimalDatatype;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.productcmpt.ConfigElement;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IUnrestrictedValueSet;
import org.faktorips.devtools.core.ui.controls.chooser.EnumValueSubsetChooserModel;
import org.faktorips.devtools.core.ui.controls.chooser.ListChooserValue;
import org.junit.Test;

public class EnumValueSubsetChooserModelTest {

    @Test
    public void testSelectAll() {
        List<String> initialValues = new ArrayList<String>();
        initialValues.add("1");
        initialValues.add("two");
        initialValues.add("three");
        initialValues.add(null);

        EnumValueSet enumValueSetSpy = getEnumValueSetMock();
        EnumValueSubsetChooserModel model = new EnumValueSubsetChooserModel(initialValues, enumValueSetSpy, null);

        Assert.assertEquals(4, model.getPreDefinedValues().size());
        Assert.assertEquals(0, model.getResultingValues().size());

        model.moveAllValuesFromPreDefinedToResulting();

        Assert.assertEquals(0, model.getPreDefinedValues().size());
        Assert.assertEquals(4, model.getResultingValues().size());
    }

    private EnumValueSet getEnumValueSetMock() {
        IpsModel ipsModelMock = mock(IpsModel.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS));
        ConfigElement configElement = mock(ConfigElement.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS));
        EnumValueSet enumValueSet = new EnumValueSet(configElement, "asd");
        EnumValueSet enumValueSetSpy = spy(enumValueSet);
        doReturn(ipsModelMock).when(enumValueSetSpy).getIpsModel();
        return enumValueSetSpy;
    }

    @Test
    public void testRemoveAll() {
        List<String> initialValues = new ArrayList<String>();
        initialValues.add("1");
        initialValues.add("two");
        initialValues.add("three");
        initialValues.add(null);

        EnumValueSet enumValueSetSpy = getEnumValueSetMock();
        EnumValueSubsetChooserModel model = new EnumValueSubsetChooserModel(initialValues, enumValueSetSpy, null);

        model.moveAllValuesFromPreDefinedToResulting();

        Assert.assertEquals(0, model.getPreDefinedValues().size());
        Assert.assertEquals(4, model.getResultingValues().size());

        model.moveAllValuesFromResultingToPreDefined();

        Assert.assertEquals(4, model.getPreDefinedValues().size());
        Assert.assertEquals(0, model.getResultingValues().size());
    }

    @Test
    public void testMove() {
        List<String> initialValues = new ArrayList<String>();
        initialValues.add("1");
        initialValues.add("two");
        initialValues.add("three");
        initialValues.add(null);
        EnumValueSet enumValueSetSpy = getEnumValueSetMock();
        EnumValueSubsetChooserModel model = new EnumValueSubsetChooserModel(initialValues, enumValueSetSpy, null);

        Assert.assertEquals(4, model.getPreDefinedValues().size());
        Assert.assertEquals(0, model.getResultingValues().size());

        List<ListChooserValue> valuesToBeMoved = new ArrayList<ListChooserValue>();
        valuesToBeMoved.add(new ListChooserValue("two"));
        model.moveValuesFromPreDefinedToResulting(valuesToBeMoved);

        Assert.assertEquals(3, model.getPreDefinedValues().size());
        assertEquals("1", model.getPreDefinedValues().get(0).getValue());
        assertEquals("three", model.getPreDefinedValues().get(1).getValue());
        assertNull(model.getPreDefinedValues().get(2).getValue());
        Assert.assertEquals(1, model.getResultingValues().size());
        assertEquals("two", model.getResultingValues().get(0).getValue());

        valuesToBeMoved = new ArrayList<ListChooserValue>();
        valuesToBeMoved.add(new ListChooserValue(null));
        model.moveValuesFromPreDefinedToResulting(valuesToBeMoved);

        Assert.assertEquals(2, model.getPreDefinedValues().size());
        assertEquals("1", model.getPreDefinedValues().get(0).getValue());
        assertEquals("three", model.getPreDefinedValues().get(1).getValue());
        Assert.assertEquals(2, model.getResultingValues().size());
        assertEquals("two", model.getResultingValues().get(0).getValue());
        assertNull(model.getResultingValues().get(1).getValue());
    }

    @Test
    public void testMoveUp() {
        List<String> initialValues = new ArrayList<String>();
        initialValues.add("1");
        initialValues.add("two");
        initialValues.add("three");
        EnumValueSet enumValueSetSpy = getEnumValueSetMock();
        EnumValueSubsetChooserModel model = new EnumValueSubsetChooserModel(initialValues, enumValueSetSpy, null);

        model.moveAllValuesFromPreDefinedToResulting();

        List<ListChooserValue> movedElements = new ArrayList<ListChooserValue>();
        movedElements.add(new ListChooserValue("two"));

        model.moveUp(movedElements);
        Assert.assertEquals(3, model.getResultingValues().size());
        assertEquals("two", model.getResultingValues().get(0).getValue());
        assertEquals("1", model.getResultingValues().get(1).getValue());
        assertEquals("three", model.getResultingValues().get(2).getValue());

        model.moveUp(movedElements);
        Assert.assertEquals(3, model.getResultingValues().size());
        assertEquals("two", model.getResultingValues().get(0).getValue());
        assertEquals("1", model.getResultingValues().get(1).getValue());
        assertEquals("three", model.getResultingValues().get(2).getValue());
    }

    @Test
    public void testMoveDown() {
        List<String> initialValues = new ArrayList<String>();
        initialValues.add("1");
        initialValues.add("two");
        initialValues.add("three");
        EnumValueSet enumValueSetSpy = getEnumValueSetMock();
        EnumValueSubsetChooserModel model = new EnumValueSubsetChooserModel(initialValues, enumValueSetSpy, null);

        model.moveAllValuesFromPreDefinedToResulting();

        List<ListChooserValue> movedElements = new ArrayList<ListChooserValue>();
        movedElements.add(new ListChooserValue("two"));

        model.moveDown(movedElements);
        Assert.assertEquals(3, model.getResultingValues().size());
        Assert.assertEquals("1", model.getResultingValues().get(0).getValue());
        Assert.assertEquals("three", model.getResultingValues().get(1).getValue());
        Assert.assertEquals("two", model.getResultingValues().get(2).getValue());

        model.moveDown(movedElements);
        Assert.assertEquals(3, model.getResultingValues().size());
        Assert.assertEquals("1", model.getResultingValues().get(0).getValue());
        Assert.assertEquals("three", model.getResultingValues().get(1).getValue());
        Assert.assertEquals("two", model.getResultingValues().get(2).getValue());
    }

    @Test
    public void testGetSourceValueIDs() throws Exception {
        IEnumValueSet sourceValueSet = mockValueSet(true, false);
        IEnumValueSet targetValueSet = mockEmptyValueSet();
        EnumDatatype datatype = mockDatatype();

        EnumValueSubsetChooserModel model = new EnumValueSubsetChooserModel(sourceValueSet, datatype, targetValueSet);
        List<ListChooserValue> sourceValueIDs = model.getAllValues();
        assertEquals(3, sourceValueIDs.size());
        assertEquals("1", sourceValueIDs.get(0).getValue());
        assertEquals("two", sourceValueIDs.get(1).getValue());
        assertEquals("THREE", sourceValueIDs.get(2).getValue());
    }

    @Test
    public void testGetSourceValueIDs_AbstractValueSet() throws Exception {
        IEnumValueSet sourceValueSet = mockValueSet(true, true);
        IEnumValueSet targetValueSet = mockEmptyValueSet();
        EnumDatatype datatype = mockDatatype();

        EnumValueSubsetChooserModel model = new EnumValueSubsetChooserModel(sourceValueSet, datatype, targetValueSet);
        List<ListChooserValue> sourceValueIDs = model.getAllValues();
        assertEquals(4, sourceValueIDs.size());
        assertEquals("1", sourceValueIDs.get(0).getValue());
        assertEquals("2", sourceValueIDs.get(1).getValue());
        assertEquals("3", sourceValueIDs.get(2).getValue());
        assertEquals("4", sourceValueIDs.get(3).getValue());
    }

    @Test
    public void testGetSourceValueIDs_UnrestrictedValueSet() throws Exception {
        IUnrestrictedValueSet sourceValueSet = mock(IUnrestrictedValueSet.class);
        when(sourceValueSet.canBeUsedAsSupersetForAnotherEnumValueSet()).thenReturn(false);
        IEnumValueSet targetValueSet = mockEmptyValueSet();
        EnumDatatype datatype = mockDatatype();

        EnumValueSubsetChooserModel model = new EnumValueSubsetChooserModel(sourceValueSet, datatype, targetValueSet);
        List<ListChooserValue> sourceValueIDs = model.getAllValues();
        assertEquals(4, sourceValueIDs.size());
        assertEquals("1", sourceValueIDs.get(0).getValue());
        assertEquals("2", sourceValueIDs.get(1).getValue());
        assertEquals("3", sourceValueIDs.get(2).getValue());
        assertEquals("4", sourceValueIDs.get(3).getValue());
    }

    @Test
    public void testGetSourceValueIDs_Null() throws Exception {
        IEnumValueSet targetValueSet = mockEmptyValueSet();
        EnumDatatype datatype = mockDatatype();

        EnumValueSubsetChooserModel model = new EnumValueSubsetChooserModel(null, datatype, targetValueSet);
        List<ListChooserValue> sourceValueIDs = model.getAllValues();
        assertEquals(4, sourceValueIDs.size());
        assertEquals("1", sourceValueIDs.get(0).getValue());
        assertEquals("2", sourceValueIDs.get(1).getValue());
        assertEquals("3", sourceValueIDs.get(2).getValue());
        assertEquals("4", sourceValueIDs.get(3).getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSourceValueIDs_Exception() throws Exception {
        IEnumValueSet sourceValueSet = mockValueSet(true, true);
        IEnumValueSet targetValueSet = mockEmptyValueSet();
        DecimalDatatype datatype = mock(DecimalDatatype.class);

        new EnumValueSubsetChooserModel(sourceValueSet, datatype, targetValueSet);
    }

    protected EnumDatatype mockDatatype() {
        EnumDatatype datatype = mock(EnumDatatype.class);
        when(datatype.getAllValueIds(true)).thenReturn(new String[] { "1", "2", "3", "4" });
        when(datatype.isEnum()).thenReturn(true);
        return datatype;
    }

    private IEnumValueSet mockEmptyValueSet() {
        IEnumValueSet valueSet = mock(IEnumValueSet.class);

        List<String> valueList = new ArrayList<String>();
        when(valueSet.getValuesAsList()).thenReturn(valueList);
        when(valueSet.isEnum()).thenReturn(true);
        when(valueSet.isAbstract()).thenReturn(false);
        return valueSet;
    }

    protected IEnumValueSet mockValueSet(boolean enumValueSet, boolean abstractValueSet) {
        IEnumValueSet valueSet = mock(IEnumValueSet.class);

        List<String> valueList = new ArrayList<String>();
        valueList.add("1");
        valueList.add("two");
        valueList.add("THREE");
        when(valueSet.getValuesAsList()).thenReturn(valueList);
        when(valueSet.isEnum()).thenReturn(enumValueSet);
        when(valueSet.isAbstract()).thenReturn(abstractValueSet);
        when(valueSet.canBeUsedAsSupersetForAnotherEnumValueSet()).thenReturn(enumValueSet & !abstractValueSet);
        return valueSet;
    }
}
