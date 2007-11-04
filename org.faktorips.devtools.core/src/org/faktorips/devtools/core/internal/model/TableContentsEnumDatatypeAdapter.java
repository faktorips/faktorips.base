/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.AbstractDatatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * 
 * @author Peter Erzberger
 */
//TODO write test case
public class TableContentsEnumDatatypeAdapter extends AbstractDatatype implements EnumDatatype {

    private ITableContents tableContents;
    private ITableContentsGeneration generation;
    private IIpsProject ipsProject;
    
    /**
     * @param tableContents
     * @throws CoreException 
     */
    public TableContentsEnumDatatypeAdapter(ITableContents tableContents) throws CoreException {
        super();
        ArgumentCheck.notNull(tableContents, this);
        this.tableContents = tableContents;
        generation = (ITableContentsGeneration)tableContents.getFirstGeneration();
        ArgumentCheck.notNull(generation, this);
        ipsProject = tableContents.getIpsProject();
    }

    public ITableContents getTableContents(){
        return tableContents;
    }
    
    /**
     * {@inheritDoc}
     */
    public String[] getAllValueIds(boolean includeNull) {
        IRow[] rows = generation.getRows();
        String[] ids = new String[includeNull ? rows.length + 1 : rows.length];
        for (int i = 0; i < rows.length; i++) {
            ids[i] = rows[i].getValue(0);
        }
        if(includeNull){
            ids[rows.length] = null;
        }
        return ids;
    }

    /**
     * {@inheritDoc}
     */
    public String getValueName(String id) {
        if(id == null){
            return null;
        }
        IRow[] rows = generation.getRows();
        for (int i = 0; i < rows.length; i++) {
            if(id.equals(rows[i].getValue(0))){
                return rows[i].getValue(1);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean areValuesEqual(String valueA, String valueB) {
        //TODO possible bottle neck if called often
        ITableStructure structure;
        try {
            structure = tableContents.findTableStructure();
            if(structure == null){
                return ObjectUtils.equals(valueA, valueB);
            }
            IUniqueKey[] keys = structure.getUniqueKeys();
            if(keys.length == 0){
                return ObjectUtils.equals(valueA, valueB);
            }
            if(keys[0].getNumOfKeyItems() == 0){
                return ObjectUtils.equals(valueA, valueB);
            }
            String datatypeStr = keys[0].getKeyItemAt(0).getDatatype();
            ValueDatatype datatype = ipsProject.findValueDatatype(datatypeStr);
            if(datatype == null){
                return ObjectUtils.equals(valueA, valueB);
            }
            return datatype.areValuesEqual(valueA, valueB);
        } catch (CoreException e) {
            return ObjectUtils.equals(valueA, valueB);
        }
    }

    public MessageList validate() throws CoreException {
        MessageList msgList = new MessageList();
        ITableContents currentTableContents = (ITableContents)ipsProject.findIpsObject(IpsObjectType.TABLE_CONTENTS, tableContents.getQualifiedName());
        if(currentTableContents == null){
            msgList.add(new Message("", Messages.TableContentsEnumDatatypeAdapter_1, Message.ERROR)); //$NON-NLS-1$
        }
        MessageList tableContentsMsgList = tableContents.validate();
        if(!tableContentsMsgList.isEmpty()){
            msgList.add(new Message("", Messages.TableContentsEnumDatatypeAdapter_3, tableContentsMsgList.getSeverity())); //$NON-NLS-1$
        }
        return msgList;
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsCompare() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public int compare(String valueA, String valueB) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("TableStructureEnumDatatype " + getQualifiedName() + "does not support comparison for values"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * {@inheritDoc}
     */
    public String getDefaultValue() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isNull(String value) {
        return value == null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isParsable(String value) {
        if(value == null){
            return true;
        }
        IRow[] rows = generation.getRows();
        for (int i = 0; i < rows.length; i++) {
            if(value.equals(rows[i].getValue(0))){
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public String getJavaClassName() {
        return ipsProject.getIpsArtefactBuilderSet().getDatatypeHelperForTableBasedEnum(this).getJavaClassName();
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return tableContents.getName();
    }

    /**
     * {@inheritDoc}
     */
    public String getQualifiedName() {
        return tableContents.getQualifiedName();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSupportingNames() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public ValueDatatype getWrapperType() {
        return null;
    }

    /**
     * Returns false
     */
    public boolean isPrimitive() {
        return false;
    }

    /**
     * Returns true.
     */
    public boolean isValueDatatype() {
        return true;
    }
}
