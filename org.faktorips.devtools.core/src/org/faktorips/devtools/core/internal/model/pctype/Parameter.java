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

package org.faktorips.devtools.core.internal.model.pctype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IParameter;
import org.faktorips.devtools.core.model.pctype.IParameterContainer;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of the published interface.
 * 
 * @author Jan Ortmann
 */
public class Parameter extends IpsObjectPart implements IParameter {

    final static String TAG_NAME = "Parameter"; //$NON-NLS-1$

	private String datatype = "";
	
	public Parameter(IParameterContainer container, int id) {
		super(container, id);
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
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		valueChanged(oldName, name);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDatatype(String type) {
		String oldType = datatype;
		datatype = type;
		valueChanged(oldType, datatype);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDatatype() {
		return datatype;
	}

	/**
	 * {@inheritDoc}
	 */
	public Datatype findDatatype() throws CoreException {
		return getIpsProject().findDatatype(datatype);
	}

	/**
	 * {@inheritDoc}
	 */
	public void delete() {
		((IParameterContainer)getParent()).removeParameter(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isDeleted() {
		return false;
	}

	/**
	 * Returns <code>null</code> as a parameter has no parts.
	 * 
	 * {@inheritDoc}
	 */
	public IIpsObjectPart newPart(Class partType) {
		return null;
	}

	
	/**
	 * {@inheritDoc}
	 */
	protected void initPropertiesFromXml(Element element, Integer id) {
		super.initPropertiesFromXml(element, id);
		name = element.getAttribute(PROPERTY_NAME);
		datatype = element.getAttribute(PROPERTY_DATATYPE);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void propertiesToXml(Element element) {
		super.propertiesToXml(element);
		element.setAttribute(PROPERTY_NAME, name);
		element.setAttribute(PROPERTY_DATATYPE, datatype);
	}

	/**
	 * {@inheritDoc}
	 */
	public Image getImage() {
		return IpsPlugin.getDefault().getImage("Parameter.gif"); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	protected void validateThis(MessageList result) throws CoreException {
		super.validateThis(result);
        if (StringUtils.isEmpty(name)) {
            result.add(new Message("", Messages.Method_msgNameEmpty, Message.ERROR, this, PROPERTY_NAME)); //$NON-NLS-1$
        } else {
	        IStatus status = JavaConventions.validateIdentifier(getName());
	        if (!status.isOK()) {
	            result.add(new Message("", Messages.Method_msgInvalidParameterName, Message.ERROR, this, PROPERTY_NAME)); //$NON-NLS-1$
	        }
        }
        if (StringUtils.isEmpty(datatype)) {
            result.add(new Message("", Messages.Method_msgDatatypeEmpty, Message.ERROR, this, PROPERTY_DATATYPE)); //$NON-NLS-1$
        } else {
            Datatype datatypeObject = getIpsProject().findDatatype(datatype);
            if (datatypeObject==null) {
                result.add(new Message("", NLS.bind(Messages.Method_msgDatatypeNotFound, datatype), Message.ERROR, this, PROPERTY_DATATYPE)); //$NON-NLS-1$
            }
        }
	}

	
}
