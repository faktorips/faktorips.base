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

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IValueSet;
import org.faktorips.devtools.core.model.ValueSetType;
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
 * This works also when the attribute's datatype is unkown.
 * 
 * @author Andy Roesch
 * @author Jan Ortmann
 */
public abstract class ValueSet extends IpsObjectPart implements IValueSet {
    /**
     * Name of the xml element used in the xml conversion.
     */
    public final static String XML_TAG = "ValueSet"; //$NON-NLS-1$
    
	private ValueSetType type;
	
	protected ValueSet(ValueSetType type) {
		super();
		this.type = type;
	}
	
	protected ValueSet(ValueSetType type, IIpsObjectPart parent) {
		this(type, parent, ((IpsObjectPart)parent).getNextPartId());
	}
	
	protected ValueSet(ValueSetType type, IIpsObjectPart parent, int partId) {
		super(parent, partId);
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
    	Message msg;
    	int severity = Message.ERROR;
    	
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
     * This operation is not supported for value sets. An Attribute or Config Element has
     * to have a value set allways, so it can not be deleted. To remove any restrictins 
     * set the value-set-type to All Values.
     * 
     * {@inheritDoc}
     */
	public void delete() {
		throw new UnsupportedOperationException("ValueSet can not be deleted");		
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isDeleted() {
		return false;
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
		return IpsPlugin.getDefault().getImage("ValueSet.gif");
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected Element createElement(Document doc) {
		return doc.createElement(XML_TAG);
	}

}
