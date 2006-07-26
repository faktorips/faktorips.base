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

package org.faktorips.devtools.core.model.testcasetype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IAttribute;

/**
 * Specification of a test attribute.
 * 
 * @author Joerg Ortmann
 */
public interface ITestAttribute extends IIpsObjectPart {
	
	/** Property names */
    public final static String PROPERTY_ATTRIBUTE = "attribute"; //$NON-NLS-1$
    
    /**
     * Returns the attribute.
     */
	public String getAttribute();
	
	/**
	 * Sets the given attribute.
	 */
	public void setAttribute(String attribute);
	
    /**
     * Returns the attribute or <code>null</code> if the attribute does not exists.
     * 
     * @throws CoreException if an error occurs while searching for the attribute.
     */	
	public IAttribute findAttribute() throws CoreException;
}
