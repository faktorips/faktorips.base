/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.ui.controls.tableedit.IEditTableModel;
import org.faktorips.devtools.core.ui.dialogs.MultiValueTableModel.SingleValueViewItem;
import org.faktorips.devtools.model.internal.productcmpt.MultiValueHolder;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.ISingleValueHolder;
import org.faktorips.runtime.MessageList;

/**
 * Table model for a multi-value attribute. Manages the list of values the attribute contains.
 * 
 * @author Stefan Widmaier
 */
public class MultiValueTableModel implements IEditTableModel<SingleValueViewItem> {
    /**
     * The {@link IAttributeValue attribute value} holding multiple values.
     */
    private final IAttributeValue attributeValue;

    /**
     * Creates a new MultiValueTableModel from the given {@link IAttributeValue attribute value}.
     * 
     * @param attributeValue the {@link IAttributeValue attribute value} being listed.
     */
    public MultiValueTableModel(IAttributeValue attributeValue) {
        this.attributeValue = attributeValue;
    }

    /**
     * Returns all elements of the multi value attribute. Note that the attribute contains
     * {@link SingleValueHolder single value holders}, which are encapsulated by this method call,
     * into {@link SingleValueViewItem single value view items}. This is done on every call of this
     * method to prevent invalid control binding. It also means that two items in the returned list
     * are different, even if they refer to identical {@link SingleValueHolder single value holder
     * objects}.
     * 
     * @return a newly created list of {@link SingleValueViewItem single value view items},
     *             encapsulating the {@link SingleValueHolder single value holder objects}.
     */
    @Override
    public List<SingleValueViewItem> getElements() {
        List<SingleValueViewItem> list = new ArrayList<>();
        int index = 0;
        for (ISingleValueHolder holder : getMultiValueHolder(attributeValue).getValue()) {
            list.add(new SingleValueViewItem(holder, index++));
        }
        return list;
    }

    /**
     * Returns the list of {@link SingleValueHolder single value holders} of this attribute. These
     * are not encapsulated and a modification of the result list directly modifies the attribute.
     * 
     * @return the list of {@link SingleValueHolder single value holders} of this attribute.
     */
    private List<ISingleValueHolder> getValueHolderList() {
        return getMultiValueHolder(attributeValue).getValue();
    }

    /**
     * Returns the {@link MultiValueHolder multi value holder} for the given {@link IAttributeValue
     * attribute value}.
     * 
     * @param attributeValue the {@link IAttributeValue} for which the value holder is to be
     *            retrieved.
     * @return the {@link MultiValueHolder multi value holder} of the given attribute value.
     */
    protected static MultiValueHolder getMultiValueHolder(IAttributeValue attributeValue) {
        return (MultiValueHolder)attributeValue.getValueHolder();
    }

    /**
     * Stores a list of {@link SingleValueViewItem single value view items} back into the
     * {@link IAttributeValue} by retrieving the value holders stored in the item list.
     * 
     * @param list the {@link SingleValueViewItem single value view item list} to be set back into
     *            the model.
     */
    private void applyValueList(List<SingleValueViewItem> list) {
        List<ISingleValueHolder> result = new ArrayList<>();
        for (SingleValueViewItem item : list) {
            result.add(item.holder);
        }
        getMultiValueHolder().setValue(result);
    }

    /**
     * Exchanges the position of two elements at the given indices.
     * 
     * @param index1 index of the first element.
     * @param index2 index of the second element.
     */
    @Override
    public void swapElements(int index1, int index2) {
        List<SingleValueViewItem> list = getElements();
        SingleValueViewItem element1 = list.get(index1);
        list.set(index1, list.get(index2));
        list.set(index2, element1);
        applyValueList(list);
    }

    /**
     * Adds a new value to this multi value attribute.
     * 
     * @return the newly created value wrapped in a {@link SingleValueViewItem single value view
     *             item}.
     */
    @Override
    public SingleValueViewItem addElement() {
        SingleValueHolder holder = new SingleValueHolder(attributeValue);
        List<SingleValueViewItem> list = getElements();
        SingleValueViewItem item = new SingleValueViewItem(holder, list.size());
        list.add(item);
        applyValueList(list);
        return item;
    }

    /**
     * Removes the value at the specified index.
     * 
     * @param index the index of the value, which should be removed from this attribute.
     */
    @Override
    public void removeElement(int index) {
        List<SingleValueViewItem> list = getElements();
        list.remove(index);
        applyValueList(list);
    }

    /**
     * Returns the {@link MultiValueHolder multi value holder} of this attribute value.
     */
    protected MultiValueHolder getMultiValueHolder() {
        return getMultiValueHolder(attributeValue);
    }

    /**
     * Validates the given attribute value.
     * 
     * @param valueToValidate the {@link SingleValueViewItem} to validate.
     * @return the {@link MessageList} containing the error and warning messages.
     */
    @Override
    public MessageList validate(SingleValueViewItem valueToValidate) {
        ISingleValueHolder holder = valueToValidate.getSingleValueHolder();
        if (getValueHolderList().contains(holder)) {
            MessageList messageList;
            messageList = attributeValue.validate(attributeValue.getIpsProject());
            return messageList.getMessagesFor(holder);
        } else {
            throw new IllegalArgumentException(NLS.bind(
                    "Cannot validate \"{0}\" as it is no element of this table model.", valueToValidate)); //$NON-NLS-1$
        }
    }

    /**
     * Wrapper class for {@link SingleValueHolder single value holders}. In contrast to
     * {@link SingleValueHolder} instances of this class are not equal if they contain the same
     * value or holder instance respectively. Using {@link SingleValueHolder}s directly caused
     * problems when traversing in the table viewer as it may contain multiple equal values. Still,
     * equals and hashCode must be overridden, as the list of SingleValueView items is created on
     * the fly at every call of <code>getItemList()</code>. The list cannot be stored, because this
     * would cause bindings to these objects to become invalid. Hence, an artificial
     * <code>index</code> variable is introduced to be able to discern two value view items.
     * 
     * @author Stefan Widmaier
     */
    public static class SingleValueViewItem {
        private final ISingleValueHolder holder;
        private final int index;

        public SingleValueViewItem(ISingleValueHolder holder, int index) {
            this.holder = holder;
            this.index = index;
        }

        public ISingleValueHolder getSingleValueHolder() {
            return holder;
        }

        @Override
        public int hashCode() {
            return (holder == null ? 11 : holder.hashCode()) + index * 37;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof SingleValueViewItem otherItem)) {
                return false;
            }
            return ((holder == null && otherItem.holder == null) || (holder != null && holder.equals(otherItem.holder)))
                    && index == otherItem.index;
        }
    }

}
