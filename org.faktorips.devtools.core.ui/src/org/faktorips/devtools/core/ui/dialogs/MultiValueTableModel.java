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
import org.faktorips.devtools.core.ui.controls.tableedit.AbstractListTableModel;
import org.faktorips.devtools.core.ui.dialogs.MultiValueTableModel.SingleValueViewItem;
import org.faktorips.util.message.MessageList;

/**
 * Table model for a multi-value attribute. Manages the list of values the attribute contains.
 * 
 * @author Stefan Widmaier
 */
public class MultiValueTableModel extends AbstractListTableModel<SingleValueViewItem> {

    private final IAttributeValue attributeValue;
    private List<SingleValueHolder> valueHolderList;

    public MultiValueTableModel(IAttributeValue attributeValue) {
        super(getItemList(attributeValue));
        this.attributeValue = attributeValue;
    }

    private static List<SingleValueViewItem> getItemList(IAttributeValue attributeValue) {
        List<SingleValueViewItem> itemList = new ArrayList<MultiValueTableModel.SingleValueViewItem>();
        for (SingleValueHolder holder : getMultiValueHolder(attributeValue).getValue()) {
            itemList.add(new SingleValueViewItem(holder));
        }
        return itemList;
    }

    protected static MultiValueHolder getMultiValueHolder(IAttributeValue attributeValue) {
        return (MultiValueHolder)attributeValue.getValueHolder();
    }

    private List<SingleValueHolder> getValueHolderList() {
        List<SingleValueHolder> holderList = new ArrayList<SingleValueHolder>();
        for (SingleValueViewItem item : getElements()) {
            holderList.add(item.getSingleValueHolder());
        }
        return holderList;
    }

    @Override
    public Object addElement() {
        SingleValueHolder holder = new SingleValueHolder(attributeValue);
        SingleValueViewItem item = new SingleValueViewItem(holder);
        getElements().add(item);
        applyValueList();
        return item;
    }

    @Override
    public void removeElement(int index) {
        getElements().remove(index);
        applyValueList();
    }

    /**
     * Applies the current list this model holds to the attribute's multi-value holder.
     */
    public void applyValueList() {
        getMultiValueHolder().setValue(getValueHolderList());
    }

    protected MultiValueHolder getMultiValueHolder() {
        return getMultiValueHolder(attributeValue);
    }

    @Override
    public void swapElements(int index1, int index2) {
        super.swapElements(index1, index2);
        applyValueList();
    }

    @Override
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

        public SingleValueViewItem(SingleValueHolder holder) {
            this.holder = holder;
        }

        public SingleValueHolder getSingleValueHolder() {
            return holder;
        }
    }

}
