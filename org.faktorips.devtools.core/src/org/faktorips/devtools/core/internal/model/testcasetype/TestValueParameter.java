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

package org.faktorips.devtools.core.internal.model.testcasetype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test value parameter class. Defines a test value for a specific test case type.
 * @author Joerg Ortmann
 */
public class TestValueParameter extends TestParameter implements
		ITestValueParameter {

	final static String TAG_NAME = "ValueParameter"; //$NON-NLS-1$
	
	private String datatype = ""; //$NON-NLS-1$
	
	/**
	 * @param parent
	 * @param id
	 */
	public TestValueParameter(IIpsObject parent, int id) {
		super(parent, id);
	}

	/**
	 * @param parent
	 * @param id
	 */
	public TestValueParameter(IIpsObjectPart parent, int id) {
		super(parent, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getValueDatatype() {
		return datatype;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValueDatatype(String datatypeId) {
		datatype = datatypeId;
	}

	/**
	 * {@inheritDoc}
	 */
	public ValueDatatype findValueDatatype() throws CoreException {
        if (StringUtils.isEmpty(datatype)) {
            return null;
        }
		return getIpsProject().findValueDatatype(datatype);
	}

    /**
     * Overridden.
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    /**
     * {@inheritDoc}
     */
	protected void initPropertiesFromXml(Element element, Integer id) {
		super.initPropertiesFromXml(element, id);
		datatype = element.getAttribute(PROPERTY_VALUEDATATYPE);
	}

    /**
     * {@inheritDoc}
     */
	protected void propertiesToXml(Element element) {
		super.propertiesToXml(element);
		element.setAttribute(PROPERTY_VALUEDATATYPE, datatype);
	}

	/**
	 * Returns <code>true</code> as value parameters are always root parameters.
	 * 
	 * {@inheritDoc}
	 */
	public boolean isRootParameter() {
		return true;
	} 

	/**
	 * {@inheritDoc}
	 */
	protected void validateThis(MessageList list) throws CoreException {
		super.validateThis(list);
		ValueDatatype datatype = findValueDatatype();
		if (datatype==null) {
			String text = NLS.bind(Messages.TestValueParameter_ValidateError_ValueDatatypeNotFound, datatype);
			Message msg = new Message(MSGCODE_VALUEDATATYPE_NOT_FOUND, text, Message.ERROR, this, PROPERTY_VALUEDATATYPE);
			list.add(msg);
		}
	}
}
