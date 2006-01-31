package org.faktorips.devtools.core.internal.model.tablecontents;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.internal.model.IpsObjectGeneration;
import org.faktorips.devtools.core.internal.model.TimedIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;


/**
 *
 */
public class TableContents extends TimedIpsObject implements ITableContents {

    /**
     * The name of the table structure property
     */
    public final static String PROPERTY_TABLE_STRUCTURE = "tableStructure";

    private String structure = "";
    private int numOfColumns = 0;
    
    /**
     * @param file
     */
    public TableContents(IIpsSrcFile file) {
        super(file);
    }

    /** 
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.TimedIpsObject#createNewGeneration(int)
     */
    protected IpsObjectGeneration createNewGeneration(int id) {
        return new TableContentsGeneration(this, id);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsObject#getIpsObjectType()
     */
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.TABLE_CONTENTS;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration#getStructure()
     */
    public String getTableStructure() {
        return structure;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration#setTableStructure(java.lang.String)
     */
    public void setTableStructure(String qName) {
        String oldStructure = structure;
        structure = qName;
        valueChanged(oldStructure, structure);
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablecontents.ITableContents#findTableStructure()
     */
    public ITableStructure findTableStructure() throws CoreException {
        return (ITableStructure)getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE, structure);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablecontents.ITableContents#getNumOfColumns()
     */
    public int getNumOfColumns() {
        return numOfColumns;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablecontents.ITableContents#newColumn(java.lang.String)
     */
    public int newColumn(String defaultValue) {
        IIpsObjectGeneration[] generations = getGenerations();
        for (int i=0; i<generations.length; i++) {
            ((TableContentsGeneration)generations[i]).newColumn(defaultValue);
        }
        numOfColumns++;
        updateSrcFile();
        return numOfColumns;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablecontents.ITableContents#deleteColumn(int)
     */
    public void deleteColumn(int columnIndex) {
        if (columnIndex<0 || columnIndex>=numOfColumns) {
            throw new IllegalArgumentException("Illegal column index " + columnIndex);
        }
        IIpsObjectGeneration[] generations = getGenerations();
        for (int i=0; i<generations.length; i++) {
            ((TableContentsGeneration)generations[i]).removeColumn(columnIndex);
        }
        numOfColumns--;
        updateSrcFile();        
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsObject#getJavaType(int)
     */
    public IType getJavaType(int kind) {
        throw new IllegalArgumentException("Unkown kind " + kind);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsObject#getAllJavaTypes()
     */
    public IType[] getAllJavaTypes() {
        return new IType[0];
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IIpsObject#dependsOn()
     */
    public QualifiedNameType[] dependsOn() throws CoreException {
        if(StringUtils.isEmpty(getTableStructure())){
            return new QualifiedNameType[0];
        }
        return new QualifiedNameType[]{new QualifiedNameType(getTableStructure(), IpsObjectType.TABLE_STRUCTURE)};
    }
    
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);
        if (findTableStructure() == null) {
            String text = "The table structure " + this.structure + " does not exist";
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_TABLE_STRUCTURE));
        }
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObject#propertiesToXml(org.w3c.dom.Element)
     */
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_TABLESTRUCTURE, structure);
        newElement.setAttribute("numOfColumns", "" + numOfColumns);
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObject#initPropertiesFromXml(org.w3c.dom.Element)
     */
    protected void initPropertiesFromXml(Element element) {
        super.initPropertiesFromXml(element);
        structure = element.getAttribute(PROPERTY_TABLESTRUCTURE);
        numOfColumns = Integer.parseInt(element.getAttribute("numOfColumns"));
    }

    /**
	 * {@inheritDoc}
	 */
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType);
	}
}
