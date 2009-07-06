/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.tablestructure;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.IKey;
import org.faktorips.devtools.core.model.tablestructure.IKeyItem;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 */
public class UniqueKey extends Key implements IUniqueKey {
    
    final static String TAG_NAME = "UniqueKey"; //$NON-NLS-1$

    public UniqueKey(TableStructure tableStructure, int id) {
        super(tableStructure, id);
    }

    public UniqueKey() {
        super();
    }
    
    /**
     * {@inheritDoc}
     */
    public String getName() {
        StringBuffer buffer = new StringBuffer();
        String[] items = getKeyItemNames();
        for (int i=0; i<items.length; i++) {
            if (i>0) {
                buffer.append(", "); //$NON-NLS-1$
            }
            buffer.append(items[i]);
        }
        return buffer.toString();
    }

    private void validateItemSequence(MessageList msgList){
        if(containsRanges() && containsColumns()){
            IKeyItem[] keyItems = getKeyItems();
            boolean rangeFound = false;
            boolean wrongSequenceFound = false;
            int potentiallyWrongKeyElement = -1;
            for (int j = 0; j < keyItems.length; j++) {
                if(getTableStructure().getRange(keyItems[j].getName()) != null){
                    rangeFound = true;
                    potentiallyWrongKeyElement = j;
                }
                else if(rangeFound){
                    wrongSequenceFound = true;
                    break;
                }
            }
            if(wrongSequenceFound){
                msgList.add(new Message("", Messages.UniqueKey_wrong_sequence,  //$NON-NLS-1$
                        Message.ERROR, new ObjectProperty(this, IKey.PROPERTY_KEY_ITEMS, potentiallyWrongKeyElement)));
            }
        }
    }
    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        if (getNumOfKeyItems()==0) {
            String text = Messages.UniqueKey_msgTooLessItems;
            list.add(new Message(IUniqueKey.MSGCODE_TOO_LESS_ITEMS, text, Message.ERROR, this)); //$NON-NLS-1$
        }
        String[] items = getKeyItemNames();
        for (int i=0; i<items.length; i++) {
            validateItem(items[i], i, list);
        }
        validateItemSequence(list);
    }
    
    private void validateItem(String item, int itemmIndex, MessageList list) {
        IColumn column = getTableStructure().getColumn(item);
        if (column!=null) {
            return;
        }
        IColumnRange range = getTableStructure().getRange(item);
        if (range!=null) {
            return;
        }
        String text = NLS.bind(Messages.UniqueKey_msgKeyItemMismatch, item);
        list.add(new Message(IUniqueKey.MSGCODE_KEY_ITEM_MISMATCH, text, Message.ERROR, new ObjectProperty(this, IKey.PROPERTY_KEY_ITEMS, itemmIndex))); //$NON-NLS-1$
        return;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsRangesOnly() {
        String[] items = getKeyItemNames();
        for (int i = 0; i < items.length; i++) {
            if(getTableStructure().hasColumn(items[i])){
               return false; 
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsRanges() {
        String[] items = getKeyItemNames();
        for (int i = 0; i < items.length; i++) {
            if(getTableStructure().hasRange(items[i])){
                return true; 
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsTwoColumnRanges() {
        IKeyItem[] keyItems = getKeyItems();
        for (int i = 0; i < keyItems.length; i++) {
            IColumnRange[] ranges = getTableStructure().getRanges();
            for (int j = 0; j < ranges.length; j++) {
                if (ColumnRangeType.TWO_COLUMN_RANGE.equals(ranges[j].getColumnRangeType()) 
                        && keyItems[i].getName().equals(ranges[j].getName())){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsColumns() {
        String[] items = getKeyItemNames();
        for (int i = 0; i < items.length; i++) {
            if(getTableStructure().hasColumn(items[i])){
               return true; 
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        // nothing else to do
    }
    
    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        // nothing else to do
    }

}
