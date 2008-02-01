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

package org.faktorips.devtools.core.internal.model.valueset;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.IValueDatatypeProvider;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A ValueSet is the specification of a set of values. It is asumed that all values in a ValueSet are of the same
 * datatype.
 * <p>
 * Values in the set are represented by strings so that we don't have to deal with type conversion
 * when the datatype changes. E.g. If an attributes datatype is changed by the user from Decimal to Money,
 * lower bound and upper bound from a range value set become invalid (if they were valid before) but the 
 * string values remain. The user can switch back the datatype to Decimal and the range is valid again. 
 * This works also when the attribute's datatype is unknown.
 * 
 * @author Thorsten Guenther
 * @author Jan Ortmann
 */
public abstract class ValueSet extends AtomicIpsObjectPart implements IValueSet {
    /**
     * Name of the xml element used in the xml conversion.
     */
    public final static String XML_TAG = "ValueSet"; //$NON-NLS-1$
    
    /**
     * The type this value set is of.
     */
	private ValueSetType type;
	
	/**
	 * Creates a new value set of the given type and with the given parent and id.
	 * 
	 * @param type The type for the new valueset.
	 * @param parent The parent this valueset belongs to. Must implement IValueDatatypeProvider.
	 * @param partId The id this valueset is known by the parent.
	 * @throws IllegalArgumentException if the parent does not implement the interface 
	 * <code>IValueDatatypeProvider</code>.
	 */
	protected ValueSet(ValueSetType type, IIpsObjectPart parent, int partId) {
		super(parent , partId);
		if (!(parent instanceof IValueDatatypeProvider)) {
			super.parent = null;
			throw new IllegalArgumentException("Parent has to implement IValueDatatypeProvider."); //$NON-NLS-1$
		}
		this.type = type;
	}

    /**
     * Creates a new message with severity ERROR and adds the new message to the given message list.
     * 
     * @param list The message list to add the new message to 
     * @param id The message code
     * @param text The message text
     * @param invalidObject The object this message is for. Can be null if no relation to an object exists.
     * @param invalidProperty The name of the property the message is created for. Can be null.
     */
    protected void addMsg(MessageList list, String id, String text, Object invalidObject, String invalidProperty) {
    	addMsg(list, Message.ERROR, id, text, invalidObject, invalidProperty);
    }

    /**
     * Creates a new message with severity ERROR and adds the new message to the given message list.
     * 
     * @param list The message list to add the new message to 
     * @param id The message code
     * @param text The message text
     * @param invalidObject The object this message is for. Can be null if no relation to an object exists.
     * @param invalidProperty The name of the property the message is created for. Can be null.
     */
    protected void addMsg(MessageList list, int severity, String id, String text, Object invalidObject, String invalidProperty) {
    	Message msg;
    	
    	if (invalidObject == null) {
    		msg = new Message(id, text, severity);
    	}
    	else if (invalidProperty == null) {
    		msg = new Message(id, text, severity, invalidObject);
    	}
    	else {
    		msg = new Message(id, text, severity, invalidObject, invalidProperty);
    	}
    	
    	list.add(msg);
    }

	/**
	 * {@inheritDoc}
	 */
	public ValueSetType getValueSetType() {
		return type;
	}

	/**
	 * {@inheritDoc}
	 */
	public Image getImage() {
		return IpsPlugin.getDefault().getImage("ValueSet.gif"); //$NON-NLS-1$
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected Element createElement(Document doc) {
		return doc.createElement(XML_TAG);
	}

    /**
     * @param original The value to use if available (might be <code>null</code>).
     * @param alternative The value to use if the original value is <code>null</code>.
     * 
     * @return Either the original value (if not <code>null</code>) or the 
     * alternative string.
     */
    protected String getProperty(String original, String alternative) {
        if (original == null) {
            return alternative;
        }
        return original;
    }

    /**
     * Returns the datatype this value set is based on or <code>null</code>, if the 
     * datatype is not provided by the parent or the datatype provided is not a 
     * <code>ValueDatatype</code>.
     */
	public ValueDatatype getValueDatatype() {
		try {
            return ((IValueDatatypeProvider)parent).getValueDatatype();
        }
        catch (CoreException e) {
            throw new RuntimeException(e);
        }
	}
}
