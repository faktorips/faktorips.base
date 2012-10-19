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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.controls.valuesets.ValueListExtractor;
import org.faktorips.devtools.core.ui.editors.productcmpt.Messages;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

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
        super(datatype);
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
        super(datatype);
        this.resultingEnumValueSet = resultingEnumValueSet;
        sourceValues = initSourceValuesForEnumDatatypeAndValueSet(sourceValueSet, datatype);
    }

    /**
     * Creates a subset chooser model that uses the given enumDatatype's values as source values.
     * The enumValueset's values are the target/resulting values.
     * 
     * @param enumDatatype the data type defining the set of valid values
     * @param enumValueSet the resulting value set (e.g. the config-element's value set)
     */
    public EnumValueSubsetChooserModel(EnumDatatype enumDatatype, IEnumValueSet enumValueSet) {
        this(null, enumDatatype, enumValueSet);
    }

    private List<ListChooserValue> convertToListChooserValues(List<String> allValueIds) {
        List<ListChooserValue> values = new ArrayList<ListChooserValue>();
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
            return convertToListChooserValues(ValueListExtractor.extractValues((EnumDatatype)datatype));
        } else {
            throw new IllegalArgumentException(
                    NLS.bind(
                            "Neither the value datatype {0} nor the value set {1} define an list of valid values (enum)", datatype, sourceValueSet)); //$NON-NLS-1$
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
        List<String> stringValues = new ArrayList<String>();
        for (ListChooserValue listChooserValue : values) {
            stringValues.add(listChooserValue.getValue());
        }
        return stringValues;
    }

    @Override
    public MessageList validateValue(ListChooserValue value) {
        try {
            MessageList messageList = resultingEnumValueSet.validate(resultingEnumValueSet.getIpsProject());
            validateContainedInSourceValues(messageList, value);
            return messageList;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    protected void validateContainedInSourceValues(MessageList messageList, ListChooserValue value) {
        if (!getAllValues().contains(value)) {
            String text = NLS.bind(Messages.DefaultsAndRangesEditDialog_valueNotContainedInValueSet, value.getValue(),
                    getAllValues().toString());
            messageList.add(new Message(
                    "EnumValueSubsetChooserModel_ValueNotContainedInPredefinedValues", text, Message.ERROR)); //$NON-NLS-1$
        }
    }

    @Override
    protected void moveInternal(List<ListChooserValue> selectedValues, boolean up) {
        List<Integer> indices = new ArrayList<Integer>();
        for (ListChooserValue value : selectedValues) {
            List<Integer> positions = resultingEnumValueSet.getPositions(value.getValue());
            indices.addAll(positions);
        }
        resultingEnumValueSet.move(indices, up);
    }

    private List<ListChooserValue> convertToValueList(List<String> stringList) {
        List<ListChooserValue> result = new ArrayList<ListChooserValue>();
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
        fireEvents(oldValues);
    }

}