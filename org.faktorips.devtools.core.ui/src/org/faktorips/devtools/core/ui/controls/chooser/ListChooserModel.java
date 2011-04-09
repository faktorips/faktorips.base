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

package org.faktorips.devtools.core.ui.controls.chooser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ListChooserModel {
    public List<String> preDefinedValues;
    public List<String> resultingValues;

    public ListChooserModel() {
        this.preDefinedValues = new ArrayList<String>();
        this.resultingValues = new ArrayList<String>();
    }

    public void setInitialValues(List<String> initialValues) {
        preDefinedValues.addAll(initialValues);
    }

    /**
     * Adds the given values to the resulting list of enum values and removes them from the
     * predefined list of values.
     * <p>
     * IOW the values are moved from the predefined values to the list of resulting values.
     * <p>
     * Unknown values are ignored.
     * 
     * @param values the values to be moved
     */
    public void moveValuesFromPreDefinedToResulting(List<String> values) {
        moveFromTo(values, preDefinedValues, resultingValues);
    }

    public void moveAllValuesFromPreDefinedToResulting() {
        moveFromTo(preDefinedValues, preDefinedValues, resultingValues);
    }

    public void moveValuesFromResultingToPredefined(List<String> values) {
        moveFromTo(values, resultingValues, preDefinedValues);
    }

    public void moveAllValuesFromResultingToPreDefined() {
        moveFromTo(resultingValues, resultingValues, preDefinedValues);
    }

    private void moveFromTo(List<String> values, List<String> from, List<String> to) {
        for (String value : new CopyOnWriteArrayList<String>(values)) {
            boolean valueRemoved = from.remove(value);
            if (valueRemoved) {
                to.add(value);
            }
        }
    }

    public List<String> getPreDefinedValues() {
        return preDefinedValues;
    }

    public List<String> getResultingValues() {
        return resultingValues;
    }

    public void move(List<String> selectedValues, boolean up) {
        if (selectedValues.isEmpty()) {
            return;
        }
        String firstSelected = selectedValues.get(0);
        int index = resultingValues.indexOf(firstSelected);
        if (index >= 0) {
            if (up && index > 0) {
                resultingValues.remove(index);
                resultingValues.add(index - 1, firstSelected);
            }
            if (!up && index < resultingValues.size() - 1) {
                resultingValues.remove(index);
                resultingValues.add(index + 1, firstSelected);
            }
        }
    }

    public void moveUp(List<String> selectedValues) {
        move(selectedValues, true);
    }

    public void moveDown(List<String> selectedValues) {
        move(selectedValues, false);
    }

    public void setInitialPredefinedValues(List<String> predefValues) {
        preDefinedValues.clear();
        preDefinedValues.addAll(predefValues);
    }

    public void setInitialResultingValues(List<String> resValues) {
        resultingValues.clear();
        resultingValues.addAll(resValues);
    }

}