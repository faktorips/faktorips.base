/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
     * Returns the code generation helper for the given datatype or <code>null</code> if either
     * datatype is <code>null</code> or the provide can't provide a helper.
     */
    public DatatypeHelper getDatatypeHelper(Datatype datatype);

}
