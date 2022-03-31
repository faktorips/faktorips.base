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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.controls.valuesets.ValueListExtractor;
import org.faktorips.devtools.core.ui.editors.productcmpt.Messages;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

public class EnumValueSubsetChooserModel extends AbstractSubsetChooserModel {

    private IEnumValueSet resultingEnumValueSet;
    private final List<ListChooserValue> sourceValues;

    /**
     * This constructor cannot take abstract value sets into account as the source values are given
     * in the form of a string list. Use the constructor
     * {@link EnumValueSubsetChooserModel#EnumValueSubsetChooserModel(IValueSet, ValueDatatype, IEnumValueSet)}
     * instead.
     * <p>
     * This constructor was left deprecated to ensure compatibility with the product variant
     * plugins.
     * 
     * @deprecated As of 3.7.
     */
    @Deprecated
    public EnumValueSubsetChooserModel(List<String> sourceValueIds, IEnumValueSet resultingEnumValueSet,
            ValueDatatype datatype) {
        super(datatype, resultingEnumValueSet);
        this.resultingEnumValueSet = resultingEnumValueSet;
        sourceValues = convertToListChooserValues(sourceValueIds);
    }

    /**
     * Creates a subset chooser model that uses the given enumDatatype's or the source-valueset's
     * values as source values. If the source value set is abstract (i.e. does not define values)
     * then the enum datatype's values are used. If the source value set contains values those
     * values are used. The resultingEnumValueSet's values are the target/resulting values.
     * 
     * @param sourceValueSet the value set on model side (abstract or not)
     * @param datatype the data type of the attribute
     * @param resultingEnumValueSet the enum value set that is modified by this model
     * 
     * @throws IllegalArgumentException if the value set is no enum value set and at the same time
     *             the value datatype is no enum datatype. In that case the subset chooser model
     *             should not have been used in the first place.
     */
    public EnumValueSubsetChooserModel(IValueSet sourceValueSet, ValueDatatype datatype,
            IEnumValueSet resultingEnumValueSet) {
        super(datatype, resultingEnumValueSet);
        this.resultingEnumValueSet = resultingEnumValueSet;
        sourceValues = initSourceValuesForEnumDatatypeAndValueSet(sourceValueSet, datatype);
    }

    private List<ListChooserValue> convertToListChooserValues(List<String> allValueIds) {
        List<ListChooserValue> values = new ArrayList<>();
        for (String string : allValueIds) {
            values.add(new ListChooserValue(string));
        }
        return values;
    }

    private List<ListChooserValue> initSourceValuesForEnumDatatypeAndValueSet(IValueSet sourceValueSet,
            ValueDatatype datatype) {
        if (sourceValueSet != null && sourceValueSet.canBeUsedAsSupersetForAnotherEnumValueSet()) {
            return convertToListChooserValues(ValueListExtractor.extractValues((IEnumValueSet)sourceValueSet));
        } else if (datatype.isEnum()) {
            return convertToListChooserValues(ValueListExtractor.extractValues((EnumDatatype)datatype,
                    sourceValueSet == null ? true : sourceValueSet.isContainsNull()));
        } else {
            throw new IllegalArgumentException(NLS.bind(
                    "Neither the value datatype {0} nor the value set {1} define an list of valid values (enum)", //$NON-NLS-1$
                    datatype, sourceValueSet));
        }
    }

    @Override
    public List<ListChooserValue> getAllValues() {
        return sourceValues;
    }

    @Override
    public List<ListChooserValue> getResultingValues() {
        return convertToValueList(resultingEnumValueSet.getValuesAsList());
    }

    @Override
    protected void removeFromResultingValues(List<ListChooserValue> values) {
        resultingEnumValueSet.removeValues(toStringList(values));
    }

    @Override
    protected void addToResultingValues(List<ListChooserValue> values) {
        resultingEnumValueSet.addValues(toStringList(values));
    }

    protected List<String> toStringList(List<ListChooserValue> values) {
        List<String> stringValues = new ArrayList<>();
        for (ListChooserValue listChooserValue : values) {
            stringValues.add(listChooserValue.getValue());
        }
        return stringValues;
    }

    @Override
    public MessageList validateValue(ListChooserValue value) {
        MessageList messageList = resultingEnumValueSet.validate(resultingEnumValueSet.getIpsProject());
        validateContainedInSourceValues(messageList, value);
        return messageList;
    }

    protected void validateContainedInSourceValues(MessageList messageList, ListChooserValue value) {
        if (!getAllValues().contains(value)) {
            String text = NLS.bind(Messages.DefaultsAndRangesEditDialog_valueNotContainedInValueSet, value.getValue(),
                    getAllValues().toString());
            messageList.add(new Message("EnumValueSubsetChooserModel_ValueNotContainedInPredefinedValues", text, //$NON-NLS-1$
                    Message.ERROR));
        }
    }

    @Override
    protected void moveInternal(List<ListChooserValue> selectedValues, boolean up) {
        List<Integer> indices = new ArrayList<>();
        for (ListChooserValue value : selectedValues) {
            List<Integer> positions = resultingEnumValueSet.getPositions(value.getValue());
            indices.addAll(positions);
        }
        resultingEnumValueSet.move(indices, up);
    }

    @Override
    protected void moveToPositionInternal(List<ListChooserValue> selectedValues, int targetIndex, boolean insertBelow) {
        List<Integer> indices = new ArrayList<>();
        for (ListChooserValue value : selectedValues) {
            List<Integer> positions = resultingEnumValueSet.getPositions(value.getValue());
            indices.addAll(positions);
        }
        resultingEnumValueSet.move(indices, targetIndex, insertBelow);
    }

    private List<ListChooserValue> convertToValueList(List<String> stringList) {
        List<ListChooserValue> result = new ArrayList<>();
        for (String stringValue : stringList) {
            result.add(new ListChooserValue(stringValue));
        }
        return result;
    }

    public IEnumValueSet getResultingEnumValueSet() {
        return resultingEnumValueSet;
    }

    public void setResultingEnumValueSet(IEnumValueSet newValueSet) {
        List<ListChooserValue> oldValues = getResultingValues();
        resultingEnumValueSet = newValueSet;
        fireValuesChangedEvents(oldValues);
    }
}