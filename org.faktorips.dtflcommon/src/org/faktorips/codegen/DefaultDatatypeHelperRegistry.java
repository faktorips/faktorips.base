/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import java.util.HashMap;
import java.util.Map;

import org.faktorips.datatype.Datatype;

/**
 * A default DatatypeHelperRegistry.
 */
public class DefaultDatatypeHelperRegistry implements DatatypeHelperRegistry {

    private Map<Datatype, DatatypeHelper> helpers = new HashMap<Datatype, DatatypeHelper>(20);

    /**
     * Returns a new empty registry.
     */
    public final static DatatypeHelperRegistry newEmptyRegistry() {
        return new DefaultDatatypeHelperRegistry();
    }

    /**
     * {@inheritDoc}
     */
    public DatatypeHelper getHelper(Datatype datatype) {
        return helpers.get(datatype);
    }

    /**
     * {@inheritDoc}
     */
    public void register(DatatypeHelper helper) {
        helpers.put(helper.getDatatype(), helper);
    }

}
