/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.tablestructure;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
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

    protected UniqueKey() {
        super();
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getName()
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

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsObjectPart#delete()
     */
    public void delete() {
        ((TableStructure)getTableStructure()).removeUniqueKey(this);
        deleted = true;
    }

    private boolean deleted = false;

    /**
     * {@inheritDoc}
     */
    public boolean isDeleted() {
    	return deleted;
    }

    
    protected void validate(MessageList list) throws CoreException {
        super.validate(list);
        if (getNumOfKeyItems()==0) {
            String text = Messages.UniqueKey_msgTooLessItems;
            list.add(new Message("", text, Message.ERROR, this)); //$NON-NLS-1$
        }
        String[] items = getKeyItemNames();
        for (int i=0; i<items.length; i++) {
            validateItem(items[i], list);
        }
    }
    
    private void validateItem(String item, MessageList list) {
        IColumn column = getTableStructure().getColumn(item);
        if (column!=null) {
            return;
        }
        IColumnRange range = getTableStructure().getRange(item);
        if (range!=null) {
            return;
        }
        String text = NLS.bind(Messages.UniqueKey_msgKeyItemMismatch, item);
        list.add(new Message("", text, Message.ERROR, item)); //$NON-NLS-1$
        return;
    }

    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.tablestructure.IUniqueKey#containsRangesOnly()
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
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.tablestructure.IUniqueKey#containsRanges()
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
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.tablestructure.IUniqueKey#containsColumns()
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
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#createElement(org.w3c.dom.Document)
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#initPropertiesFromXml(org.w3c.dom.Element)
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        // nothing else to do
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#propertiesToXml(org.w3c.dom.Element)
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        // nothing else to do
    }

	/**
	 * {@inheritDoc}
	 */
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}
}