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
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.IForeignKey;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ForeignKey extends Key implements IForeignKey {

    final static String TAG_NAME = "ForeignKey"; //$NON-NLS-1$

    // the table structure referenced by this fk.
    private String refTableStructure = ""; //$NON-NLS-1$

    // the unique key referenced by this fk.
    private String refUniqueKey = ""; //$NON-NLS-1$

    public ForeignKey(TableStructure tableStructure, String id) {
        super(tableStructure, id);
    }

    public ForeignKey() {
        super();
    }

    @Override
    public String getName() {
        StringBuffer buffer = new StringBuffer(refTableStructure);
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
    public ITableStructure findReferencedTableStructure(IIpsProject ipsProject) throws CoreException {
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
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
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
            IUniqueKey uk = structure.getUniqueKey(refUniqueKey);
            if (uk == null) {
                String text = NLS.bind(Messages.ForeignKey_msgMissingUniqueKey, refTableStructure, refUniqueKey);
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
            MessageList list) throws CoreException {
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
        String text = NLS.bind(Messages.ForeignKey_msgInvalidKeyItem, fkItem);
        list.add(new Message("", text, Message.ERROR, fkItem)); //$NON-NLS-1$
        return;
    }

    private void validateRangeItem(IColumnRange item,
            ITableStructure refStructure,
            IIpsProject ipsProject,
            String refItem,
            MessageList list) throws CoreException {
        IColumn column = refStructure.getColumn(refItem);
        if (column != null) {
            String text = Messages.ForeignKey_msgKeyItemMissmatch;
            list.add(new Message("", text, Message.ERROR, item.getName())); //$NON-NLS-1$
            return;
        }
        IColumnRange refRange = refStructure.getRange(refItem);
        if (refRange == null) {
            String text = NLS.bind(Messages.ForeignKey_msgNotARange, refItem);
            list.add(new Message("", text, Message.WARNING, item.getName())); //$NON-NLS-1$
            return;
        }
        MessageList ml = item.validate(ipsProject);
        if (!ml.isEmpty()) {
            list.add(ml);
            return;
        }
        IColumn from = getTableStructure().getColumn(item.getFromColumn());
        IColumn to = getTableStructure().getColumn(item.getToColumn());
        IColumn refFrom = refStructure.getColumn(refRange.getFromColumn());
        IColumn refTo = refStructure.getColumn(refRange.getToColumn());
        if (from != null && refFrom != null && !from.getDatatype().equals(refFrom.getDatatype())) {
            String text = NLS.bind(Messages.ForeignKey_msgForeignKeyDatatypeMismatch, from.getName(), refFrom);
            list.add(new Message("", text, Message.ERROR, item.getName())); //$NON-NLS-1$
        } else if ((from == null && refFrom != null) || (from != null && refFrom == null)) {
            String text = Messages.ForeignKey_msgInvalidRange;
            list.add(new Message("", text, Message.WARNING, item.getName())); //$NON-NLS-1$
            return;
        }
        if (to != null && refFrom != null && !to.getDatatype().equals(refTo.getDatatype())) {
            String text = NLS.bind(Messages.ForeignKey_msgForeignKeyDatatypeMismatch, to.getName(), refTo);
            list.add(new Message("", text, Message.ERROR, item.getName())); //$NON-NLS-1$
        } else if ((to == null && refTo != null) || (to != null && refTo == null)) {
            String text = Messages.ForeignKey_msgReferencedRangeInvalid;
            list.add(new Message("", text, Message.WARNING, item.getName())); //$NON-NLS-1$
            return;
        }
    }

    private void validateColumnItem(IColumn item, ITableStructure refStructure, String refItem, MessageList list)
            throws CoreException {
        IColumnRange range = refStructure.getRange(refItem);
        if (range != null) {
            String text = Messages.ForeignKey_msgKeyMissmatch;
            list.add(new Message("", text, Message.ERROR, item.getName())); //$NON-NLS-1$
            return;
        }
        IColumn refColumn = refStructure.getColumn(refItem);
        if (refColumn == null) {
            String text = NLS.bind(Messages.ForeignKey_msgNotAColumn, refItem);
            list.add(new Message("", text, Message.WARNING, item.getName())); //$NON-NLS-1$
            return;
        }
        if (!item.getDatatype().equals(refColumn.getDatatype())) {
            String text = NLS.bind(Messages.ForeignKey_msgKeyDatatypeMismatch, item.getName(), refItem);
            list.add(new Message("", text, Message.ERROR, item.getName())); //$NON-NLS-1$
            return;
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
