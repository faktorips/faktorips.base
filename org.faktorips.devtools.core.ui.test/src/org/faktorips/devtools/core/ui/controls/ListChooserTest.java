/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.faktorips.devtools.core.ui.controls.chooser.ListChooserModel;

public class ListChooserTest extends TestCase {
    public void testSelectAll() {
        ListChooserModel model = new ListChooserModel();

        List<String> initialValues = new ArrayList<String>();
        initialValues.add("1");
        initialValues.add("two");
        initialValues.add("three");
        model.setInitialValues(initialValues);

        Assert.assertEquals(3, model.getPreDefinedValues().size());
        Assert.assertEquals(0, model.getResultingValues().size());

        model.moveAllValuesFromPreDefinedToResulting();

        Assert.assertEquals(0, model.getPreDefinedValues().size());
        Assert.assertEquals(3, model.getResultingValues().size());
    }

    public void testRemoveAll() {
        ListChooserModel model = new ListChooserModel();

        List<String> initialValues = new ArrayList<String>();
        initialValues.add("1");
        initialValues.add("two");
        initialValues.add("three");
        model.setInitialValues(initialValues);

        model.moveAllValuesFromPreDefinedToResulting();

        Assert.assertEquals(0, model.getPreDefinedValues().size());
        Assert.assertEquals(3, model.getResultingValues().size());

        model.moveAllValuesFromResultingToPreDefined();

        Assert.assertEquals(3, model.getPreDefinedValues().size());
        Assert.assertEquals(0, model.getResultingValues().size());
    }

    public void testMove() {
        ListChooserModel model = new ListChooserModel();

        List<String> initialValues = new ArrayList<String>();
        initialValues.add("1");
        initialValues.add("two");
        initialValues.add("three");
        model.setInitialValues(initialValues);

        Assert.assertEquals(3, model.getPreDefinedValues().size());
        Assert.assertEquals(0, model.getResultingValues().size());

        List<String> valuesToBeMoved = new ArrayList<String>();
        valuesToBeMoved.add("two");
        model.moveValuesFromPreDefinedToResulting(valuesToBeMoved);

        Assert.assertEquals(2, model.getPreDefinedValues().size());
        Assert.assertEquals(1, model.getResultingValues().size());
    }

    public void testIllegalMove() {
        ListChooserModel model = new ListChooserModel();

        List<String> initialValues = new ArrayList<String>();
        initialValues.add("1");
        initialValues.add("two");
        initialValues.add("three");
        model.setInitialValues(initialValues);

        Assert.assertEquals(3, model.getPreDefinedValues().size());
        Assert.assertEquals(0, model.getResultingValues().size());

        List<String> valuesToBeMoved = new ArrayList<String>();
        valuesToBeMoved.add("SEVEN");
        model.moveValuesFromPreDefinedToResulting(valuesToBeMoved);

        Assert.assertEquals(3, model.getPreDefinedValues().size());
        Assert.assertEquals(0, model.getResultingValues().size());
    }

    public void testMoveUp() {
        ListChooserModel model = new ListChooserModel();

        List<String> initialValues = new ArrayList<String>();
        initialValues.add("1");
        initialValues.add("two");
        initialValues.add("three");
        model.setInitialValues(initialValues);
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

    public void testMoveDown() {
        ListChooserModel model = new ListChooserModel();

        List<String> initialValues = new ArrayList<String>();
        initialValues.add("1");
        initialValues.add("two");
        initialValues.add("three");
        model.setInitialValues(initialValues);
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
