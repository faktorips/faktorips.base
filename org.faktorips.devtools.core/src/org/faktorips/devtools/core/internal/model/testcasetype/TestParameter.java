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

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test parameter class. Superclass for all test parameter.
 * 
 * @author Joerg Ortmann
 */
public abstract class TestParameter extends IpsObjectPart implements ITestParameter {
	
    private String name = ""; //$NON-NLS-1$
	
	private boolean inputParameter = true;
	
    private boolean deleted = false;
    
	public TestParameter(IIpsObject parent, int id) {
		super(parent, id);
	}

	public TestParameter(IIpsObjectPart parent, int id) {
		super(parent, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setName(String newName) {
		this.name = newName;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isInputParameter() {
		return inputParameter;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isExpextedResultParameter() {
		return !inputParameter;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInputParameter(boolean isInputType) {
		this.inputParameter = isInputType;
	}	

	/**
	 * {@inheritDoc}
	 */
	protected Element createElement(Document doc) {
		throw new RuntimeException("Not implemented!"); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public void delete() {
        ((TestCaseType)getIpsObject()).removeTestParameter(this);
        updateSrcFile();
        deleted = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type: " + partType); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public Image getImage() {
		return null;
	}

    /**
     * {@inheritDoc}
     */
	protected void initPropertiesFromXml(Element element, Integer id) {
		super.initPropertiesFromXml(element, id);
		name = element.getAttribute(PROPERTY_NAME);
	}

    /**
     * {@inheritDoc}
     */
	protected void propertiesToXml(Element element) {
		super.propertiesToXml(element);
		element.setAttribute(PROPERTY_NAME, name);
	}
}
