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

package org.faktorips.devtools.core.model;

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
	public ValueDatatype getValueDatatype();
	
}
