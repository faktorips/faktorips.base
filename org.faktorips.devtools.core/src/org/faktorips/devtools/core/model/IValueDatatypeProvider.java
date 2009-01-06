/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;

/**
 * A value datatype provider is a class that can be asked for a <class>ValueDatatype</code>.
 *  
 * @author Thorsten Guenther
 */
public interface IValueDatatypeProvider {

	/**
	 * Returns the <code>ValueDatatype</code> if available or null if not. This
	 * means, that null is returned if a datatype is available that is not a 
	 * <code>ValueDatatype</code>.
	 */
	public ValueDatatype getValueDatatype() throws CoreException;
	
}
