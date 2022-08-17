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

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IMultiValueHolder;
import org.faktorips.devtools.model.productcmpt.ISingleValueHolder;
import org.faktorips.devtools.model.util.ListElementMover;
import org.faktorips.runtime.MessageList;

public class MultiValueSubsetChooserModel extends AbstractSubsetChooserModel {

    private final IMultiValueHolder multiValueHolder;
    private final List<ListChooserValue> resultingValues = new ArrayList<>();
    private final List<ListChooserValue> sourceValues = new ArrayList<>();

    public MultiValueSubsetChooserModel(List<String> allValues, IMultiValueHolder multiValueHolder,
            ValueDatatype datatype, IAttributeValue attributeValue) {
        super(datatype, attributeValue);
        this.multiValueHolder = multiValueHolder;
        for (ISingleValueHolder holder : multiValueHolder.getValue()) {
            resultingValues.add(new ListChooserValue(holder.getStringValue()));
        }
        for (String string : allValues) {
            sourceValues.add(new ListChooserValue(string));
        }
    }

    @Override
    public List<ListChooserValue> getAllValues() {
        return sourceValues;
    }

    @Override
    public List<ListChooserValue> getResultingValues() {
        return resultingValues;
    }

    @Override
    public MessageList validateValue(ListChooserValue value) {
        ISingleValueHolder holder = findSingleValueHolderFor(value);
        MessageList messageList = multiValueHolder.validate(multiValueHolder.getIpsProject());
        return messageList.getMessagesFor(holder);
    }

    protected ISingleValueHolder findSingleValueHolderFor(ListChooserValue value) {
        List<ISingleValueHolder> holders = multiValueHolder.getValue();
        for (ISingleValueHolder singleValueHolder : holders) {
            if ((value.getValue() == null && singleValueHolder.getStringValue() == null)
                    || (value.getValue() != null && value.getValue().equals(singleValueHolder.getStringValue()))) {
                return singleValueHolder;
            }
        }
        return null;
    }

    @Override
    protected void moveInternal(List<ListChooserValue> selectedValues, boolean up) {
        ListElementMover<ListChooserValue> mover = new ListElementMover<>(resultingValues);
        mover.move(getValueIndices(selectedValues), up);
        updateMultiValueHolder();
    }

    @Override
    protected void moveToPositionInternal(List<ListChooserValue> selectedValues, int targetIndex, boolean insertBelow) {
        ListElementMover<ListChooserValue> mover = new ListElementMover<>(resultingValues);
        mover.moveToIndex(getValueIndices(selectedValues), targetIndex, insertBelow);
        updateMultiValueHolder();
    }

    protected int[] getValueIndices(List<ListChooserValue> selectedValues) {
        int[] indices = new int[selectedValues.size()];
        for (int i = 0; i < selectedValues.size(); i++) {
            indices[i] = resultingValues.indexOf(selectedValues.get(i));
        }
        return indices;
    }

    @Override
    protected void removeFromResultingValues(List<ListChooserValue> values) {
        resultingValues.removeAll(values);
        updateMultiValueHolder();
    }

    protected void updateMultiValueHolder() {
        List<ISingleValueHolder> holderList = new ArrayList<>();
        for (ListChooserValue value : getResultingValues()) {
            ISingleValueHolder holder = new SingleValueHolder(multiValueHolder.getParent(), value.getValue());
            holderList.add(holder);
        }
        multiValueHolder.setValue(holderList);
    }

    @Override
    protected void addToResultingValues(List<ListChooserValue> values) {
        resultingValues.addAll(values);
        updateMultiValueHolder();
    }
}
