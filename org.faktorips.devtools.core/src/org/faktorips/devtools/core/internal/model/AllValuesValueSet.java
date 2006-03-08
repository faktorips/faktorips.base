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

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.IAllValuesValueSet;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IValueSet;
import org.faktorips.devtools.core.model.ValueSetType;
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

    public AllValuesValueSet(IIpsObjectPart parent, int partId) {
    	super(ValueSetType.ALL_VALUES, parent, partId);
    }
    
    /**
     * {@inheritDoc}
     */
    protected Element createSubclassElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    /**
     * {@inheritDoc}
     */
    public void validate(ValueDatatype datatype, MessageList list) {
        // nothing to do
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
    public boolean containsValue(String value, ValueDatatype datatype) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsValue(String value, ValueDatatype datatype, MessageList list, Object invalidObject, String invalidProperty) {
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean containsValueSet(IValueSet subset, ValueDatatype datatype, MessageList list, Object invalidObject, String invalidProperty) {
		return true;
	}

    /**
     * {@inheritDoc}
     */
	public boolean containsValueSet(IValueSet subset, ValueDatatype datatype) {
		return true;
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
}
