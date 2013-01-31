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

package org.faktorips.devtools.core.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.util.message.MessageList;

/**
 * Table model for a multi-value attribute. Manages the list of values the attribute contains.
 * 
 * @author Stefan Widmaier
 */
public class MultiValueTableModel {

    private final IAttributeValue attributeValue;

    public MultiValueTableModel(IAttributeValue attributeValue) {
        this.attributeValue = attributeValue;
    }

    public List<SingleValueViewItem> getElements() {
        return getItemList(attributeValue);
    }

    private List<SingleValueHolder> getValueHolderList() {
        return getMultiValueHolder(attributeValue).getValue();
    }

    private static List<SingleValueViewItem> getItemList(IAttributeValue attributeValue) {
        List<SingleValueViewItem> list = new ArrayList<MultiValueTableModel.SingleValueViewItem>();
        int index = 0;
        for (SingleValueHolder holder : getMultiValueHolder(attributeValue).getValue()) {
            list.add(new SingleValueViewItem(holder, index++));
        }
        return list;
    }

    protected static MultiValueHolder getMultiValueHolder(IAttributeValue attributeValue) {
        return (MultiValueHolder)attributeValue.getValueHolder();
    }

    private void applyValueList(List<SingleValueViewItem> list) {
        List<SingleValueHolder> result = new ArrayList<SingleValueHolder>();
        for (SingleValueViewItem item : list) {
            result.add(item.holder);
        }
        getMultiValueHolder().setValue(result);
    }

    public void swapElements(int index1, int index2) {
        List<SingleValueViewItem> list = getItemList(attributeValue);
        SingleValueViewItem element1 = list.get(index1);
        list.set(index1, list.get(index2));
        list.set(index2, element1);
        applyValueList(list);
    }

    public SingleValueViewItem addElement() {
        SingleValueHolder holder = new SingleValueHolder(attributeValue);
        List<SingleValueViewItem> list = getItemList(attributeValue);
        SingleValueViewItem item = new SingleValueViewItem(holder, list.size());
        list.add(item);
        applyValueList(list);
        return item;
    }

    public void removeElement(int index) {
        List<SingleValueViewItem> list = getItemList(attributeValue);
        list.remove(index);
        applyValueList(list);
    }

    protected MultiValueHolder getMultiValueHolder() {
        return getMultiValueHolder(attributeValue);
    }

    public MessageList validate(Object elementToValidate) {
        SingleValueHolder holder = ((SingleValueViewItem)elementToValidate).getSingleValueHolder();
        if (getValueHolderList().contains(holder)) {
            MessageList messageList;
            try {
                messageList = attributeValue.validate(attributeValue.getIpsProject());
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
            return messageList.getMessagesFor(holder);
        } else {
            throw new IllegalArgumentException(NLS.bind(
                    "Cannot validate \"{0}\" as it is no element of this table model.", elementToValidate)); //$NON-NLS-1$
        }
    }

    /**
     * Wrapper class for {@link SingleValueHolder single value holders}. In contrast to
     * {@link SingleValueHolder} instances of this class are not equal if they contain the same
     * value or holder instance respectively. Using {@link SingleValueHolder}s directly caused
     * problems when traversing in the table viewer as it may contain multiple equal values.
     * 
     * @author Stefan Widmaier
     */
    public static class SingleValueViewItem {
        private final SingleValueHolder holder;
        private final int index;

        public SingleValueViewItem(SingleValueHolder holder, int index) {
            this.holder = holder;
            this.index = index;
        }

        public SingleValueHolder getSingleValueHolder() {
            return holder;
        }

        @Override
        public int hashCode() {
            return (holder == null ? 11 : holder.hashCode()) + index * 37;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof SingleValueViewItem)) {
                return false;
            }
            SingleValueViewItem otherItem = (SingleValueViewItem)other;
            return holder.equals(otherItem.holder) && index == otherItem.index;
        }
    }
}
