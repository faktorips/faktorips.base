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

import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.IAllValuesValueSet;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IValueSet;
import org.faktorips.devtools.core.model.Messages;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a value set containing all values. That means either all
 * values declared by the underlying datatype or all values declared
 * by the super-valueset.
 * 
 * @author Thorsten Guenther
 */
public class AllValuesValueSet extends ValueSet implements IAllValuesValueSet  {
    public final static String XML_TAG = "AllValues"; //$NON-NLS-1$

    /**
     * Creates a new value set representing all values of the datatype provided by the
     * parent. The parent therefore has to implement IValueDatatypeProvider.
     * 
     * @param parent The parent this valueset belongs to. 
     * @param partId The id this part is knwon by by the parent.
	 * @throws IllegalArgumentException if the parent does not implement the interface 
	 * <code>IValueDatatypeProvider</code>.
     */
    public AllValuesValueSet(IIpsObjectPart parent, int partId) {
    	super(ValueSetType.ALL_VALUES, parent, partId);
    }
    
    /**
     * {@inheritDoc}
     */
    public void validate(MessageList list) {
    	ValueDatatype datatype = getValueDatatype();
        if (datatype==null) {
            String text = Messages.Range_msgUnknownDatatype;
            list.add(new Message(MSGCODE_UNKNOWN_DATATYPE, text, Message.WARNING, this));
            return;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String toShortString () {
        return "AllValuesValueSet"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public String toString () {
    	return super.toString() + ":" + "AllValuesValueSet"; //$NON-NLS-1$ //$NON-NLS-2$
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
    			list.add(new Message(MSGCODE_UNKNOWN_DATATYPE, Messages.AllValuesValueSet_msgUnknownDatatype, Message.WARNING, invalidObject, invalidProperty));
    		}
    		return false;
    	}
    	
    	if (!datatype.isParsable(value)) {
        	if (list != null) {
        		String msg = NLS.bind(Messages.AllValuesValueSet_msgValueNotParsable, value, datatype.getName());
        		addMsg(list, MSGCODE_VALUE_NOT_PARSABLE, msg, invalidObject, invalidProperty);

        		// the value can not be parsed - so it is not contained, too...
        		msg = NLS.bind(Messages.AllValuesValueSet_msgValueNotContained, datatype.getName());
        		addMsg(list, MSGCODE_VALUE_NOT_CONTAINED, msg, invalidObject, invalidProperty);
        	}
            return false;
		}

        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean containsValueSet(IValueSet subset, MessageList list, Object invalidObject, String invalidProperty) {
    	ValueDatatype datatype = getValueDatatype();
    	ValueDatatype subDatatype = ((ValueSet)subset).getValueDatatype();
    	
    	if (datatype == null || subDatatype == null) {
    		if (list != null) {
    			list.add(new Message(MSGCODE_UNKNOWN_DATATYPE, Messages.AllValuesValueSet_msgUnknowndDatatype, Message.WARNING, invalidObject, invalidProperty));
    		}
    		return false;
    	}
    	
    	if (datatype.getQualifiedName().equals(subDatatype.getQualifiedName())) {
    		return true;
    	}

    	if (list != null) {
    		String msg = NLS.bind(Messages.AllValuesValueSet_msgNoSubset, subset.toShortString(), this.toShortString());
    		list.add(new Message(MSGCODE_NOT_SUBSET, msg, Message.ERROR, invalidObject, invalidProperty));
    	}
		return false;
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
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	protected void initPropertiesFromXml(Element element, Integer id) {
		super.initPropertiesFromXml(element, id);
		// nothing more to do...
	}

	/**
	 * {@inheritDoc}
	 */
	protected void propertiesToXml(Element element) {
		super.propertiesToXml(element);
		Document doc = element.getOwnerDocument();
        Element tagElement = doc.createElement(XML_TAG);
        element.appendChild(tagElement);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IValueSet copy(IIpsObjectPart parent, int id) {
		return new AllValuesValueSet(parent, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValuesOf(IValueSet target) {
		// nothing to do.
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getContainsNull() {
		ValueDatatype type = getValueDatatype();
		return type == null || !type.isPrimitive();
	}

	/**
	 * Because this is an <strong>All</strong>Values valueset, this method throws an
	 * UnsupportedOperationException if the underlying datatype is non-primitive and
	 * this method is called with <code>false</code> for containsNull, too.
	 * 
	 * {@inheritDoc}
	 */
	public void setContainsNull(boolean containsNull) {
		if (getValueDatatype().isPrimitive() && containsNull) {
			throw new UnsupportedOperationException("Datatype is primitive, therefore this all-values valueset can not contain null"); //$NON-NLS-1$
		}		
		if (!getValueDatatype().isPrimitive() && !containsNull) {
			throw new UnsupportedOperationException("Datatype is nonPrimitive, therefore this all-values values has to contain null"); //$NON-NLS-1$
		}
	}
}
