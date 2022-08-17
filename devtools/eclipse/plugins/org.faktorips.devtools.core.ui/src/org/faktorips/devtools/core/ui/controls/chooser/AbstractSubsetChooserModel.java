/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.chooser;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.runtime.MessageList;

/**
 * Model for choosing a subset of values from a given set. Subclasses specify the set of all
 * available values by implementing the method {@link #getAllValues()}. The already chosen subset of
 * values must be provided using {@link #getResultingValues()}. This class takes care of calculating
 * the set of predefined values (disjunct to the resulting set of values).
 * 
 * @author Stefan Widmaier
 */
public abstract class AbstractSubsetChooserModel extends IpsObjectPartPmo {

    protected static final String PROPERTY_RESULTING_VALUES = "resultingValues"; //$NON-NLS-1$
    protected static final String PROPERTY_PREDEFINED_VALUES = "predefinedValues"; //$NON-NLS-1$
    private final ValueDatatype datatype;

    public AbstractSubsetChooserModel(ValueDatatype datatype, IIpsObjectPart part) {
        super(part);
        this.datatype = datatype;
    }

    public ValueDatatype getValueDatatype() {
        return datatype;
    }

    protected static List<ListChooserValue> createValueListFromStringList(List<String> stringValues) {
        List<ListChooserValue> chooserValues = new ArrayList<>();
        for (String string : stringValues) {
            chooserValues.add(new ListChooserValue(string));
        }
        return chooserValues;
    }

    public void moveAllValuesFromPreDefinedToResulting() {
        moveValuesFromPreDefinedToResulting(getPreDefinedValues());
    }

    public void moveAllValuesFromResultingToPreDefined() {
        moveValuesFromResultingToPredefined(getResultingValues());
    }

    public void moveValuesFromResultingToPredefined(List<ListChooserValue> values) {
        removeFromResultingValues(new CopyOnWriteArrayList<>(values));
    }

    protected void fireValuesChangedEvents(List<ListChooserValue> oldResultingValues) {
        notifyListeners(
                new PropertyChangeEvent(this, PROPERTY_RESULTING_VALUES, oldResultingValues, getResultingValues()));
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_PREDEFINED_VALUES, null, null));
    }

    /**
     * Adds the given values to the resulting list of resulting values and removes them from the
     * predefined list of values.
     * <p>
     * IOW the values are moved from the predefined values to the list of resulting values.
     * <p>
     * Unknown values are ignored.
     * 
     * @param values the values to be moved
     */
    public void moveValuesFromPreDefinedToResulting(List<ListChooserValue> values) {
        addToResultingValues(new CopyOnWriteArrayList<>(values));
    }

    public abstract List<ListChooserValue> getAllValues();

    public abstract List<ListChooserValue> getResultingValues();

    /**
     * Removes all given values from the list of resulting values. Only one
     * {@link ContentChangeEvent} should be fired during this operation to ensure UI performance and
     * responsiveness.
     * 
     * @param values the list of values to be removed from the chooser's result.
     */
    protected abstract void removeFromResultingValues(List<ListChooserValue> values);

    /**
     * Add all given values to the list of resulting values. Only one {@link ContentChangeEvent}
     * should be fired during this operation to ensure UI performance and responsiveness.
     * 
     * @param values the list of values to be removed from the chooser's result.
     */
    protected abstract void addToResultingValues(List<ListChooserValue> values);

    protected abstract void moveInternal(List<ListChooserValue> selectedValues, boolean up);

    protected abstract void moveToPositionInternal(List<ListChooserValue> selectedValues,
            int targetIndex,
            boolean insertBelow);

    public abstract MessageList validateValue(ListChooserValue value);

    protected void move(List<ListChooserValue> selectedValues, boolean up) {
        List<ListChooserValue> oldValues = getResultingValues();
        moveInternal(selectedValues, up);
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_RESULTING_VALUES, oldValues, getResultingValues()));
    }

    public List<ListChooserValue> getPreDefinedValues() {
        LinkedHashSet<ListChooserValue> allValues = new LinkedHashSet<>(getAllValues());
        Set<ListChooserValue> resultingValues = new HashSet<>(getResultingValues());
        allValues.removeAll(resultingValues);
        return new ArrayList<>(allValues);
    }

    public void moveUp(List<ListChooserValue> selectedValues) {
        move(selectedValues, true);
    }

    public void moveDown(List<ListChooserValue> selectedValues) {
        move(selectedValues, false);
    }

    public int getIndexOfResultingValue(ListChooserValue value) {
        List<ListChooserValue> allValues = getResultingValues();
        return allValues.indexOf(value);
    }

    public void moveToPosition(List<ListChooserValue> selectedValues, int targetIndex, boolean insertBelow) {
        List<ListChooserValue> oldValues = getResultingValues();
        moveToPositionInternal(selectedValues, targetIndex, insertBelow);
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_RESULTING_VALUES, oldValues, getResultingValues()));
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        fireValuesChangedEvents(getResultingValues());
        super.contentsChanged(event);
    }

}
