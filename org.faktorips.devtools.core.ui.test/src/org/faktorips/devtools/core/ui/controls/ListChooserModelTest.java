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

package org.faktorips.devtools.core.ui.controls;

import static org.junit.Assert.assertTrue;
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
import org.faktorips.devtools.core.ui.controls.chooser.ListChooserModel;
import org.junit.Test;

public class ListChooserModelTest {

    @Test
    public void testSelectAll() {
        List<String> initialValues = new ArrayList<String>();
        initialValues.add("1");
        initialValues.add("two");
        initialValues.add("three");
        initialValues.add(null);

        EnumValueSet enumValueSetSpy = getEnumValueSetMock();
        ListChooserModel model = new ListChooserModel(initialValues, enumValueSetSpy);

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
        ListChooserModel model = new ListChooserModel(initialValues, enumValueSetSpy);

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
        ListChooserModel model = new ListChooserModel(initialValues, enumValueSetSpy);

        Assert.assertEquals(4, model.getPreDefinedValues().size());
        Assert.assertEquals(0, model.getResultingValues().size());

        List<String> valuesToBeMoved = new ArrayList<String>();
        valuesToBeMoved.add("two");
        model.moveValuesFromPreDefinedToResulting(valuesToBeMoved);

        Assert.assertEquals(3, model.getPreDefinedValues().size());
        assertTrue(model.getPreDefinedValues().contains("1"));
        assertTrue(model.getPreDefinedValues().contains("three"));
        assertTrue(model.getPreDefinedValues().contains(null));
        Assert.assertEquals(1, model.getResultingValues().size());
        assertTrue(model.getResultingValues().contains("two"));

        valuesToBeMoved = new ArrayList<String>();
        valuesToBeMoved.add(null);
        model.moveValuesFromPreDefinedToResulting(valuesToBeMoved);

        Assert.assertEquals(2, model.getPreDefinedValues().size());
        assertTrue(model.getPreDefinedValues().contains("1"));
        assertTrue(model.getPreDefinedValues().contains("three"));
        Assert.assertEquals(2, model.getResultingValues().size());
        assertTrue(model.getResultingValues().contains("two"));
        assertTrue(model.getResultingValues().contains(null));
    }

    @Test
    public void testMoveUp() {
        List<String> initialValues = new ArrayList<String>();
        initialValues.add("1");
        initialValues.add("two");
        initialValues.add("three");
        EnumValueSet enumValueSetSpy = getEnumValueSetMock();
        ListChooserModel model = new ListChooserModel(initialValues, enumValueSetSpy);

        model.moveAllValuesFromPreDefinedToResulting();

        List<String> movedElements = new ArrayList<String>();
        movedElements.add("two");

        model.moveUp(movedElements);
        Assert.assertEquals(3, model.getResultingValues().size());
        Assert.assertEquals("two", model.getResultingValues().get(0));
        Assert.assertEquals("1", model.getResultingValues().get(1));
        Assert.assertEquals("three", model.getResultingValues().get(2));

        model.moveUp(movedElements);
        Assert.assertEquals(3, model.getResultingValues().size());
        Assert.assertEquals("two", model.getResultingValues().get(0));
        Assert.assertEquals("1", model.getResultingValues().get(1));
        Assert.assertEquals("three", model.getResultingValues().get(2));
    }

    @Test
    public void testMoveDown() {
        List<String> initialValues = new ArrayList<String>();
        initialValues.add("1");
        initialValues.add("two");
        initialValues.add("three");
        EnumValueSet enumValueSetSpy = getEnumValueSetMock();
        ListChooserModel model = new ListChooserModel(initialValues, enumValueSetSpy);

        model.moveAllValuesFromPreDefinedToResulting();

        List<String> movedElements = new ArrayList<String>();
        movedElements.add("two");

        model.moveDown(movedElements);
        Assert.assertEquals(3, model.getResultingValues().size());
        Assert.assertEquals("1", model.getResultingValues().get(0));
        Assert.assertEquals("three", model.getResultingValues().get(1));
        Assert.assertEquals("two", model.getResultingValues().get(2));

        model.moveDown(movedElements);
        Assert.assertEquals(3, model.getResultingValues().size());
        Assert.assertEquals("1", model.getResultingValues().get(0));
        Assert.assertEquals("three", model.getResultingValues().get(1));
        Assert.assertEquals("two", model.getResultingValues().get(2));
    }

}
