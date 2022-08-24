/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.tablestructure;

import java.text.MessageFormat;

import org.faktorips.devtools.model.internal.ValidationUtils;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.IColumnRange;
import org.faktorips.devtools.model.tablestructure.IForeignKey;
import org.faktorips.devtools.model.tablestructure.IIndex;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ForeignKey extends Key implements IForeignKey {

    static final String TAG_NAME = "ForeignKey"; //$NON-NLS-1$

    /** the table structure referenced by this fk. */
    private String refTableStructure = ""; //$NON-NLS-1$

    /** the unique key referenced by this fk. */
    private String refUniqueKey = ""; //$NON-NLS-1$

    public ForeignKey(TableStructure tableStructure, String id) {
        super(tableStructure, id);
    }

    public ForeignKey() {
        super();
    }

    @Override
    public String getName() {
        StringBuilder buffer = new StringBuilder(refTableStructure);
        buffer.append('(');
        buffer.append(refUniqueKey);
        buffer.append(')');
        return buffer.toString();
    }

    @Override
    public String getReferencedTableStructure() {
        return refTableStructure;
    }

    @Override
    public void setReferencedTableStructure(String tableStructure) {
        String oldValue = refTableStructure;
        refTableStructure = tableStructure;
        valueChanged(oldValue, refTableStructure);
    }

    @Override
    public ITableStructure findReferencedTableStructure(IIpsProject ipsProject) {
        return (ITableStructure)ipsProject.findIpsObject(IpsObjectType.TABLE_STRUCTURE, refTableStructure);
    }

    @Override
    public String getReferencedUniqueKey() {
        return refUniqueKey;
    }

    @Override
    public void setReferencedUniqueKey(String uniqueKey) {
        String oldValue = refUniqueKey;
        refUniqueKey = uniqueKey;
        valueChanged(oldValue, refUniqueKey);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        super.validateThis(list, ipsProject);
        ValidationUtils.checkIpsObjectReference(refTableStructure, IpsObjectType.TABLE_STRUCTURE, "referenced table", //$NON-NLS-1$
                this, PROPERTY_REF_TABLE_STRUCTURE, "", list); //$NON-NLS-1$
        ITableStructure structure = (ITableStructure)getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE,
                refTableStructure);
        if (structure != null) {
            if (!ValidationUtils.checkStringPropertyNotEmpty(refUniqueKey,
                    "referenced unique key", this, PROPERTY_REF_UNIQUE_KEY, "", list)) { //$NON-NLS-1$ //$NON-NLS-2$
                return;
            }
            IIndex uk = structure.getUniqueKey(refUniqueKey);
            if (uk == null) {
                String text = MessageFormat.format(Messages.ForeignKey_msgMissingUniqueKey, refTableStructure,
                        refUniqueKey);
                list.add(new Message("", text, Message.ERROR, this, PROPERTY_REF_UNIQUE_KEY)); //$NON-NLS-1$
            } else {
                if (uk.getNumOfKeyItems() != getNumOfKeyItems()) {
                    String text = Messages.ForeignKey_msgMalformedForeignKey;
                    list.add(new Message("", text, Message.ERROR, this, PROPERTY_REF_UNIQUE_KEY)); //$NON-NLS-1$
                } else {
                    String[] ukItems = uk.getKeyItemNames();
                    String[] fkItems = getKeyItemNames();
                    for (int i = 0; i < fkItems.length; i++) {
                        validateKeyItem(fkItems[i], structure, ipsProject, ukItems[i], list);
                    }
                }
            }
        }
    }

    private void validateKeyItem(String fkItem,
            ITableStructure refStructure,
            IIpsProject ipsProject,
            String refItem,
            MessageList list) {

        IColumnRange range = getTableStructure().getRange(fkItem);
        if (range != null) {
            validateRangeItem(range, refStructure, ipsProject, refItem, list);
            return;
        }
        IColumn column = getTableStructure().getColumn(fkItem);
        if (column != null) {
            validateColumnItem(column, refStructure, refItem, list);
            return;
        }
        String text = MessageFormat.format(Messages.ForeignKey_msgInvalidKeyItem, fkItem);
        list.add(new Message("", text, Message.ERROR, fkItem)); //$NON-NLS-1$
    }

    private void validateRangeItem(IColumnRange item,
            ITableStructure refStructure,
            IIpsProject ipsProject,
            String refItem,
            MessageList list) {

        IColumn column = refStructure.getColumn(refItem);
        if (column != null) {
            String text = Messages.ForeignKey_msgKeyItemMissmatch;
            list.add(new Message("", text, Message.ERROR, item.getName())); //$NON-NLS-1$
            return;
        }
        IColumnRange refRange = refStructure.getRange(refItem);
        if (refRange == null) {
            String text = MessageFormat.format(Messages.ForeignKey_msgNotARange, refItem);
            list.add(new Message("", text, Message.WARNING, item.getName())); //$NON-NLS-1$
            return;
        }
        MessageList ml = item.validate(ipsProject);
        if (!ml.isEmpty()) {
            list.add(ml);
            return;
        }
        validateReferencedColumn(item, refStructure, list, refRange);
    }

    private void validateReferencedColumn(IColumnRange item,
            ITableStructure refStructure,
            MessageList list,
            IColumnRange refRange) {
        IColumn from = getTableStructure().getColumn(item.getFromColumn());
        IColumn to = getTableStructure().getColumn(item.getToColumn());
        IColumn refFrom = refStructure.getColumn(refRange.getFromColumn());
        IColumn refTo = refStructure.getColumn(refRange.getToColumn());
        if (from != null && refFrom != null && !from.getDatatype().equals(refFrom.getDatatype())) {
            String text = MessageFormat.format(Messages.ForeignKey_msgForeignKeyDatatypeMismatch, from.getName(),
                    refFrom);
            list.add(new Message("", text, Message.ERROR, item.getName())); //$NON-NLS-1$
        } else if ((from == null) == (refFrom != null)) {
            String text = Messages.ForeignKey_msgInvalidRange;
            list.add(new Message("", text, Message.WARNING, item.getName())); //$NON-NLS-1$
            return;
        }
        if (to != null && refFrom != null && !to.getDatatype().equals(refTo.getDatatype())) {
            String text = MessageFormat.format(Messages.ForeignKey_msgForeignKeyDatatypeMismatch, to.getName(), refTo);
            list.add(new Message("", text, Message.ERROR, item.getName())); //$NON-NLS-1$
        } else if ((to == null) == (refTo != null)) {
            String text = Messages.ForeignKey_msgReferencedRangeInvalid;
            list.add(new Message("", text, Message.WARNING, item.getName())); //$NON-NLS-1$
        }
    }

    private void validateColumnItem(IColumn item, ITableStructure refStructure, String refItem, MessageList list) {
        IColumnRange range = refStructure.getRange(refItem);
        if (range != null) {
            String text = Messages.ForeignKey_msgKeyMissmatch;
            list.add(new Message("", text, Message.ERROR, item.getName())); //$NON-NLS-1$
            return;
        }
        IColumn refColumn = refStructure.getColumn(refItem);
        if (refColumn == null) {
            String text = MessageFormat.format(Messages.ForeignKey_msgNotAColumn, refItem);
            list.add(new Message("", text, Message.WARNING, item.getName())); //$NON-NLS-1$
            return;
        }
        if (!item.getDatatype().equals(refColumn.getDatatype())) {
            String text = MessageFormat.format(Messages.ForeignKey_msgKeyDatatypeMismatch, item.getName(), refItem);
            list.add(new Message("", text, Message.ERROR, item.getName())); //$NON-NLS-1$
        }
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        refTableStructure = element.getAttribute(PROPERTY_REF_TABLE_STRUCTURE);
        refUniqueKey = element.getAttribute(PROPERTY_REF_UNIQUE_KEY);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_REF_TABLE_STRUCTURE, refTableStructure);
        element.setAttribute(PROPERTY_REF_UNIQUE_KEY, refUniqueKey);
    }

}
