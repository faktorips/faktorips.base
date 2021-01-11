/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.tablestructure;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.IIndex;
import org.faktorips.devtools.core.model.tablestructure.IKey;
import org.faktorips.devtools.core.model.tablestructure.IKeyItem;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Index extends Key implements IIndex {

    static final String TAG_NAME = "Index"; //$NON-NLS-1$

    private static final String XML_ATTRIBUTE_UNIQUE_KEY = "uniqueKey"; //$NON-NLS-1$

    private boolean uniqueKey = true;

    public Index(TableStructure tableStructure, String id) {
        super(tableStructure, id);
    }

    public Index() {
        super();
    }

    @Override
    public String getName() {
        return IpsStringUtils.join(getKeyItemNames());
    }

    private void validateItemSequence(MessageList msgList) {
        if (containsRanges() && containsColumns()) {
            IKeyItem[] keyItems = getKeyItems();
            boolean rangeFound = false;
            boolean wrongSequenceFound = false;
            int potentiallyWrongKeyElement = -1;
            for (int j = 0; j < keyItems.length; j++) {
                if (getTableStructure().getRange(keyItems[j].getName()) != null) {
                    rangeFound = true;
                    potentiallyWrongKeyElement = j;
                } else if (rangeFound) {
                    wrongSequenceFound = true;
                    break;
                }
            }
            if (wrongSequenceFound) {
                msgList.add(new Message("", Messages.Index_wrong_sequence, //$NON-NLS-1$
                        Message.ERROR, new ObjectProperty(this, IKey.PROPERTY_KEY_ITEMS, potentiallyWrongKeyElement)));
            }
        }
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        if (getNumOfKeyItems() == 0) {
            String text = Messages.Index_msgTooLessItems;
            list.add(new Message(IIndex.MSGCODE_TOO_LESS_ITEMS, text, Message.ERROR, this));
        }
        String[] items = getKeyItemNames();
        for (int i = 0; i < items.length; i++) {
            validateItem(items[i], i, list);
        }
        validateItemSequence(list);
    }

    private void validateItem(String item, int itemmIndex, MessageList list) {
        IColumn column = getTableStructure().getColumn(item);
        if (column != null) {
            return;
        }
        IColumnRange range = getTableStructure().getRange(item);
        if (range != null) {
            return;
        }
        String text = NLS.bind(Messages.Index_msgKeyItemMismatch, item);
        list.add(new Message(IIndex.MSGCODE_KEY_ITEM_MISMATCH, text, Message.ERROR, new ObjectProperty(this,
                IKey.PROPERTY_KEY_ITEMS, itemmIndex)));
        return;
    }

    @Override
    public boolean containsRangesOnly() {
        String[] items = getKeyItemNames();
        for (String item : items) {
            if (getTableStructure().hasColumn(item)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean containsRanges() {
        String[] items = getKeyItemNames();
        for (String item : items) {
            if (getTableStructure().hasRange(item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsTwoColumnRanges() {
        IKeyItem[] keyItems = getKeyItems();
        for (IKeyItem keyItem : keyItems) {
            IColumnRange[] ranges = getTableStructure().getRanges();
            for (IColumnRange range : ranges) {
                if (ColumnRangeType.TWO_COLUMN_RANGE.equals(range.getColumnRangeType())
                        && keyItem.getName().equals(range.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean containsColumns() {
        String[] items = getKeyItemNames();
        for (String item : items) {
            if (getTableStructure().hasColumn(item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    public List<String> getDatatypes() {
        List<String> keyDatatype = new ArrayList<String>();
        for (IKeyItem keyItem : getKeyItems()) {
            keyDatatype.add(keyItem.getDatatype());
        }
        return keyDatatype;
    }

    @Override
    public boolean isUniqueKey() {
        return uniqueKey;
    }

    @Override
    public void setUniqueKey(boolean unique) {
        boolean oldValue = this.uniqueKey;
        this.uniqueKey = unique;
        valueChanged(oldValue, uniqueKey, PROPERTY_UNIQUE_KEY);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(XML_ATTRIBUTE_UNIQUE_KEY, Boolean.valueOf(isUniqueKey()).toString());
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        if (element.hasAttribute(XML_ATTRIBUTE_UNIQUE_KEY)) {
            String uniqueKeyAttributeValue = element.getAttribute(XML_ATTRIBUTE_UNIQUE_KEY);
            uniqueKey = Boolean.valueOf(uniqueKeyAttributeValue);
        }
    }

}
