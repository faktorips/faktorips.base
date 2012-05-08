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
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.ui.editors.productcmpt.Messages;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

public class EnumValueSubsetChooserModel extends AbstractSubsetChooserModel {

    final IEnumValueSet resultingEnumValueSet;
    private final List<ListChooserValue> sourceValues = new ArrayList<ListChooserValue>();

    public EnumValueSubsetChooserModel(List<String> allValueIds, IEnumValueSet resultingEnumValueSet,
            ValueDatatype datatype) {
        super(datatype);
        this.resultingEnumValueSet = resultingEnumValueSet;
        for (String string : allValueIds) {
            sourceValues.add(new ListChooserValue(string));
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

}