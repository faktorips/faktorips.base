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

package org.faktorips.devtools.core.internal.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IEnumValueSet;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IValueSet;
import org.faktorips.devtools.core.model.Messages;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.XmlUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * EnumSet represents a value set of discrete values, each value has to be explicitly defined.
 * 
 * @author Thorsten Guenther
 */
public class EnumValueSet extends ValueSet implements IEnumValueSet {

    public static final String XML_TAG = "Enum"; //$NON-NLS-1$    
    private static final String XML_VALUE = "Value"; //$NON-NLS-1$

    private ArrayList elements = new ArrayList();

    public EnumValueSet(IIpsObjectPart parent, int partId) {
    	super(ValueSetType.ENUM, parent, partId);
    }
    
    /**
     * {@inheritDoc}
     */
    public String[] getValues() {
    	return (String[])elements.toArray(new String[elements.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsValue(String value) {
    	return containsValue(value, null, null, null);
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsValue(String value, MessageList list, Object invalidObject, String invalidProperty) {
    	ValueDatatype datatype = getValueDatatype();
    	
    	if (datatype == null) {
    		if (list != null) {
    			list.add(new Message(MSGCODE_UNKNOWN_DATATYPE, "The datatype is unknown", Message.WARNING, invalidObject, invalidProperty));
    		}
    		return false;
    	}
    	
        if (!datatype.isParsable(value)) {
        	if (list != null) {
        		String msg = NLS.bind(Messages.EnumValueSet_msgValueNotParsable, value, datatype.getName());
        		addMsg(list, MSGCODE_VALUE_NOT_PARSABLE, msg, invalidObject, invalidProperty);
        	}
            return false;
        }
        
        Object val = datatype.getValue(value);
        for (Iterator it=elements.iterator(); it.hasNext(); ) {
            String each = (String)it.next();
            if (datatype.isParsable(each)) {
                Object eachVal = datatype.getValue(each);
                if (eachVal == null && val == null) {
                	return true;
                }
                else if (eachVal != null && eachVal.equals(val)) {
                    return true;
                }
            }
        }
        if (list != null) {
        	String text = Messages.EnumValueSet_msgValueNotInEnumeration;
        	addMsg(list, MSGCODE_VALUE_NOT_CONTAINED, text, invalidObject, invalidProperty);
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean containsValueSet(IValueSet subset, MessageList list, Object invalidObject, String invalidProperty) {
    	ValueDatatype datatype = getValueDatatype();
    	ValueDatatype subDatatype = ((ValueSet)subset).getValueDatatype();
    	if (datatype == null || subDatatype == null) {
    		if (list != null) {
    			list.add(new Message(MSGCODE_UNKNOWN_DATATYPE, "The datatype is unknown", Message.WARNING, invalidObject, invalidProperty));
    		}
    		return false;
    	}
    	
    	if (!(subset instanceof EnumValueSet)) {
    		if (list != null) {
    			addMsg(list, MSGCODE_TYPE_OF_VALUESET_NOT_MATCHING, Messages.EnumValueSet_msgNotAnEnumValueset, invalidObject, invalidProperty);
    		}
    		return false;
    	}
    	
    	if (!datatype.getQualifiedName().equals(subDatatype.getQualifiedName())) {
    		if (list != null) {
    			String msg = NLS.bind("The datatype of the subset ({0}) does not match the datatype of this set ({1}).", subDatatype.getQualifiedName(), datatype.getQualifiedName());
    			addMsg(list, MSGCODE_DATATYPES_NOT_MATCHING, msg, invalidObject, invalidProperty);
    		}
    		return false;
    	}
    	
    	IEnumValueSet enumSubset = (IEnumValueSet)subset;
    	String[] subsetValues = enumSubset.getValues();
    	
    	boolean contains = true;
    	for (int i = 0; i < subsetValues.length && contains; i++) {
			contains = this.containsValue(subsetValues[i], list, invalidObject, invalidProperty);
		}
		return contains;
	}

    /**
     * {@inheritDoc}
     */
	public boolean containsValueSet(IValueSet subset) {
		return containsValueSet(subset, null, null, null);
	}

    /**
     * {@inheritDoc}
     */
    public void addValue(String val) {
       	elements.add(val);
        updateSrcFile();
    }

    /**
	 * {@inheritDoc}
	 */
    public void removeValue(int index) {
        elements.remove(index);
        updateSrcFile();
    }

    /**
	 * {@inheritDoc}
	 */
	public void removeValue(String value) {
        elements.remove(value);
        updateSrcFile();
	}

    /**
     * {@inheritDoc}
     */
    public String getValue(int index) {
        return (String)elements.get(index);
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(int index, String value) {
    	String oldValue = (String)elements.get(index);
        elements.set(index, value);
        valueChanged(oldValue, value);
    }
    
    /**
     * {@inheritDoc}
     */
    public int size() {
        return elements.size();
    }

    /**
     * {@inheritDoc}
     */
    public String[] getValuesNotContained(IEnumValueSet otherSet) {
        List result = new ArrayList();
        for (int i = 0; i < otherSet.size(); i++) {
            if (!elements.contains(otherSet.getValue(i))) {
                result.add(otherSet.getValue(i));
            }
        }
        return (String[])result.toArray(new String[result.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public void validate(MessageList list) {
    	ValueDatatype datatype = getValueDatatype();
    	
        int numOfValues = elements.size();
        for (int i = 0; i < numOfValues; i++) {
            String value = (String)elements.get(i);
        	if (datatype == null) {
                list.add(new Message(MSGCODE_UNKNOWN_DATATYPE, Messages.Range_msgUnknownDatatype, Message.WARNING, value));
        	} else if (!datatype.isParsable(value)) {
                String msg = NLS.bind(Messages.EnumValueSet_msgValueNotParsable, value, datatype.getName());
                list.add(new Message(MSGCODE_VALUE_NOT_PARSABLE, msg, Message.ERROR, getNotNullValue(value)));
            }
        }
        for (int i = 0; i < numOfValues - 1; i++) {
            String valueOfi = (String)elements.get(i);
            for (int j = i + 1; j < numOfValues; j++) {
                String valueOfj = (String)elements.get(j);
                if ((valueOfj == null && valueOfi == null) || (valueOfi != null && valueOfi.equals(valueOfj))) {
                    String msg = NLS.bind(Messages.EnumValueSet_msgDuplicateValue, valueOfi);
                    list.add(new Message(MSGCODE_DUPLICATE_VALUE, msg, Message.ERROR, getNotNullValue(valueOfi)));
                    list.add(new Message(MSGCODE_DUPLICATE_VALUE, msg, Message.ERROR, getNotNullValue(valueOfj)));
                }
            }
        }
        
        if (datatype != null && datatype.isPrimitive() && getContainsNull()) {
        	String text = "ValueSet is based on a primitive datatype which does not support null-values, but this valueSet is marked to contain null.";
        	list.add(new Message(MSGCODE_NULL_NOT_SUPPORTED, text, Message.ERROR, this, PROPERTY_CONTAINS_NULL));
        }

    }

    private String getNotNullValue(String value) {
    	if (value == null) {
    		return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
    	}
    	return value;
    }
    
    public String toString() {
        return super.toString() + ":" + elements.toString(); //$NON-NLS-1$
    }

    public String toShortString() {
    	Datatype type = getValueDatatype();
    	if (type != null && type instanceof EnumDatatype && ((EnumDatatype)type).isSupportingNames()) {
    		List result = new ArrayList(elements.size());
    		for (Iterator iter = elements.iterator(); iter.hasNext();) {
    			String id = (String) iter.next();
				result.add(((EnumDatatype)type).getValueName(id) + " (" + id + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			}
    		return result.toString();
    	}
    	return elements.toString();
    }
    
	/**
	 * {@inheritDoc}
	 */
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	protected void initPropertiesFromXml(Element element, Integer id) {
		super.initPropertiesFromXml(element, id);
		elements.clear();
		
		Element el = XmlUtil.getFirstElement(element);

		NodeList children = el.getElementsByTagName(XML_VALUE);
		
		for(int i = 0; i < children.getLength();i++) {
			Element valueEl = (Element)children.item(i);
			String value = ValueToXmlHelper.getValueFromElement(valueEl, "Data"); //$NON-NLS-1$
			elements.add(value);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void propertiesToXml(Element element) {
		super.propertiesToXml(element);
		Document doc = element.getOwnerDocument();
        Element tagElement = doc.createElement(XML_TAG);
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            Element valueElement = doc.createElement(XML_VALUE);
            tagElement.appendChild(valueElement);
            String value = (String) iter.next();
	        ValueToXmlHelper.addValueToElement(value, valueElement, "Data"); //$NON-NLS-1$
		}
        element.appendChild(tagElement);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IValueSet copy(IIpsObjectPart parent, int id) {
		EnumValueSet retValue = new EnumValueSet(parent, id);
		
		retValue.elements = new ArrayList(elements);
		
		return retValue;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addValuesFromDatatype(EnumDatatype datatype) {
		String[] valueIds = datatype.getAllValueIds(true);
        for (int i = 0; i < valueIds.length; i++) {
            addValue(valueIds[i]);
        }
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValuesOf(IValueSet target) {
		if (!(target instanceof EnumValueSet)) {
			throw new IllegalArgumentException("The given value set is not an enum value set"); //$NON-NLS-1$
		}
		
		elements.clear();
		elements.addAll(((EnumValueSet)target).elements);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getContainsNull() {
		return elements.contains(null);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setContainsNull(boolean containsNull) {
		boolean old = getContainsNull();
		
		if (old != containsNull) {
			if (containsNull) {
				elements.add(null);
			} else {
				elements.remove(null);
			}
		}
		
		valueChanged(old, containsNull);
	}
}
