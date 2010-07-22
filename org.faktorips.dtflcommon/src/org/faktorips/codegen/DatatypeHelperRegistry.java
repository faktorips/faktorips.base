/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.codegen;

import org.faktorips.datatype.Datatype;

/**
 * A registry for DatatypeHelper.
 */
public interface DatatypeHelperRegistry {

    /**
     * Returns the helper for the indicated datatype. Returns null if no helper is registered for
     * the datatype.
     */
    public DatatypeHelper getHelper(Datatype datatype);

    /**
     * Registers the datatype helper.
     * 
     * @throws IllegalArgumentException if helper is null.
     */
    public void register(DatatypeHelper helper);
}
