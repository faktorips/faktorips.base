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

package org.faktorips.fl;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.Datatype;

/**
 * Provides code generation helpers for the datatypes.
 * 
 * @author Jan Ortmann
 */
public interface DatatypeHelperProvider {
    
    /**
     * Returns the code generation helper for the given datatype or <code>null</code>
     * if either datatype is <code>null</code> or the provide can't provide a helper.
     */
    public DatatypeHelper getDatatypeHelper(Datatype datatype);

}
