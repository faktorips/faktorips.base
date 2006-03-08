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

package org.faktorips.devtools.core.internal.model.tablecontents;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 *
 */
public class Row extends IpsObjectPart implements IRow {
    
    final static String TAG_NAME = "Row"; //$NON-NLS-1$
    final static String VALUE_TAG_NAME = "Value"; //$NON-NLS-1$
    
    private String[] values;

    Row(TableContentsGeneration parent, int rowNum) {
        super(parent, rowNum);
        initValues();
    }
    
    private int getNumOfColumns() {
        return ((ITableContents)getParent().getParent()).getNumOfColumns();
    }
    
    /*
     * Initializes the row's values with blanks
     */
    private void initValues() {
        int columns = getNumOfColumns();
        values = new String[columns];
        for (int i=0; i<columns; i++) {
            values[i] = ""; //$NON-NLS-1$
        }
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsObjectPart#delete()
     */
    public void delete() {
        ((TableContentsGeneration)getParent()).removeRow(this);
        deleted = true;
    }

    private boolean deleted = false;

    /**
     * {@inheritDoc}
     */
    public boolean isDeleted() {
    	return deleted;
    }


    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getName()
     */
    public String getName() {
        return "" + getId(); //$NON-NLS-1$
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablecontents.IRow#getRowNumber()
     */
    public int getRowNumber() {
        return getId();
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablecontents.IRow#getValue(int)
     */
    public String getValue(int column) {
        return values[column];
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablecontents.IRow#setValue(int, java.lang.String)
     * 
     * @throws RuntimeException if the column index is invalid.
     */
    public void setValue(int column, String newValue) {
        values[column] = newValue;
        updateSrcFile();
    }
    
    void newColumn(String defaultValue) {
        String[] newValues = new String[values.length+1];
        System.arraycopy(values, 0, newValues, 0, values.length);
        newValues[values.length] = defaultValue;
        values = newValues;
    }
    
    void removeColumn(int column) {
        String[] newValues = new String[values.length-1];
        if (column>0) {
            System.arraycopy(values, 0, newValues, 0, column);    
        }
        if (column+1<values.length) {
            System.arraycopy(values, column+1, newValues, column, values.length - 1 - column);
        }
        values = newValues;
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getImage()
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("TableRow.gif"); //$NON-NLS-1$
    }

    /*
     * Returns the element's first text node.
     */
    private Text getTextNode(Element valueElement) {
        NodeList nl = valueElement.getChildNodes();
        for (int i=0; i<nl.getLength(); i++) {
            if (nl.item(i) instanceof Text) {
                return (Text)nl.item(i);
            }
        }
        return null;
    }


    protected void validate(MessageList list) throws CoreException {
        super.validate(list);
        String structureName = ((ITableContents)getParent().getParent()).getTableStructure();
        ITableStructure structure = (ITableStructure)getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE, structureName);
        for (int i=0; i<getNumOfColumns(); i++) {
            validateValue(i, values[i], structure, list);
        }
    }
    
    private void validateValue(int column, String value, ITableStructure structure, MessageList list) throws CoreException {
//        if (datatypeObject==null) {
//            if (!StringUtils.isEmpty(defaultValue)) {
//                String text = "The default value can't be parsed because the datatype is unkown!";
//                result.add(new Message("", text, Message.WARNING, this, PROPERTY_DEFAULT_VALUE));
//            } else {}
//        } else {
//            if (!datatypeObject.isValueDatatype()) {
//                if (!StringUtils.isEmpty(datatype)) {
//                    String text = "The default value can't be parsed because the datatype is not a value datatype!";
//                    result.add(new Message("", text, Message.WARNING, this, PROPERTY_DEFAULT_VALUE));    
//                } else {}
//            } else {
//                if (StringUtils.isNotEmpty(defaultValue)) {
//                    ValueDatatype valueDatatype = (ValueDatatype)datatypeObject;
//                    try {
//                        valueDatatype.getValue(defaultValue);    
//                    } catch (Exception e) {
//                        String text = "The default value " + defaultValue + " is not a " + datatype +".";
//                        result.add(new Message("", text, Message.ERROR, this, PROPERTY_DEFAULT_VALUE));                    
//                    }
//                }
//            }
//        }
//        
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
        NodeList nl = element.getElementsByTagName(VALUE_TAG_NAME);
        initValues();
        for(int i=0; i<values.length; i++) {
            if (i<nl.getLength()) {
                Element valueElement = (Element)nl.item(i);
                String isNull = valueElement.getAttribute("isNull"); //$NON-NLS-1$
                if (Boolean.valueOf(isNull).booleanValue()) {
                    values[i] = null;
                } else {
                    Text textNode = getTextNode(valueElement);
                    if (textNode==null) {
                        values[i] = ""; //$NON-NLS-1$
                    } else {
                        values[i] = textNode.getNodeValue();
                    }
                }
            }
        }
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#propertiesToXml(org.w3c.dom.Element)
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        Document doc = element.getOwnerDocument();
        for (int i=0; i<values.length; i++) {
            Element valueElement = doc.createElement(VALUE_TAG_NAME);
            valueElement.setAttribute("isNull", values[i]==null?"true":"false");     //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (values[i]!=null) {
                valueElement.appendChild(doc.createTextNode(values[i]));
            }
            element.appendChild(valueElement);
        }
    }

	/**
	 * {@inheritDoc}
	 */
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}
}
