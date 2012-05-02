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
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.productcmpt.ConfigElement;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
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

}
