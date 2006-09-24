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

import java.util.ArrayList;

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
    
    private ArrayList values;

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
        values = new ArrayList(columns+5);
        for (int i=0; i<columns; i++) {
            values.add(""); //$NON-NLS-1$
        }
    }
    
	/**
	 * {@inheritDoc}
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
	 * {@inheritDoc}
	 */
    public String getName() {
        return "" + getId(); //$NON-NLS-1$
    }

	/**
	 * {@inheritDoc}
	 */
    public int getRowNumber() {
        return getId();
    }
    
	/**
	 * {@inheritDoc}
	 */
    public String getValue(int column) {
        return (String)values.get(column);
    }

	/**
	 * {@inheritDoc}
	 */
    public void setValue(int column, String newValue) {
    	values.set(column, newValue);
        objectHasChanged();
    }
    
    void newColumn(int insertAfter, String defaultValue) {
    	if (insertAfter < values.size()) {
    		values.add(Math.max(0, insertAfter), defaultValue);
    	}
    	else {
    		values.add(defaultValue);
    	}
        objectHasChanged();
    }
    
    void removeColumn(int column) {
    	column = Math.max(0, column);
    	column = Math.min(values.size(), column);
    	values.remove(column);
    }
    
	/**
	 * {@inheritDoc}
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

	/**
	 * {@inheritDoc}
	 */
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);
        String structureName = ((ITableContents)getParent().getParent()).getTableStructure();
        ITableStructure structure = (ITableStructure)getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE, structureName);
        for (int i=0; i<getNumOfColumns(); i++) {
            validateValue(i, (String)values.get(i), structure, list);
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
		NodeList nl = element.getElementsByTagName(VALUE_TAG_NAME);
		initValues();
		for (int i = 0; i < values.size(); i++) {
			if (i < nl.getLength()) {
				Element valueElement = (Element) nl.item(i);
				String isNull = valueElement.getAttribute("isNull"); //$NON-NLS-1$
				if (Boolean.valueOf(isNull).booleanValue()) {
					values.set(i, null);
				} else {
					Text textNode = getTextNode(valueElement);
					if (textNode == null) {
						values.set(i, ""); //$NON-NLS-1$
					} else {
						values.set(i, textNode.getNodeValue());
					}
				}
			}
		}
	}
    
	/**
	 * {@inheritDoc}
	 */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        Document doc = element.getOwnerDocument();
        for (int i=0; i<values.size(); i++) {
            Element valueElement = doc.createElement(VALUE_TAG_NAME);
            valueElement.setAttribute("isNull", values.get(i)==null?"true":"false");     //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (values.get(i)!=null) {
                valueElement.appendChild(doc.createTextNode((String)values.get(i)));
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
