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

package org.faktorips.devtools.core.internal.model.testcase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test object class. Superclass of all test objects for a specific test case.
 * 
 * @author Joerg Ortmann
 */
public class TestObject extends IpsObjectPart implements ITestObject {
	
	private boolean inputType = true;
	
    private boolean deleted = false;
    
	public TestObject(IIpsObject parent, int id) {
		super(parent, id);
	}

	public TestObject(IIpsObjectPart parent, int id) {
		super(parent, id);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isInputObject() {
		return inputType;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInputParameter(boolean isInputType) {
		this.inputType = isInputType;
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
        try {
			((TestCase)getIpsObject()).removeTestObject(this);
		} catch (CoreException e) {
			IpsPlugin.logAndShowErrorDialog(e);
		}
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
}
