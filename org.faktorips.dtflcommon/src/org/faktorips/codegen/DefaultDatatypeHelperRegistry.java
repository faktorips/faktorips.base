/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.codegen;

import java.util.HashMap;
import java.util.Map;

import org.faktorips.datatype.Datatype;


/**
 * A default DatatypeHelperRegistry. 
 */
public class DefaultDatatypeHelperRegistry implements DatatypeHelperRegistry {
    
    private Map helpers = new HashMap(20);
    
    /**
     * Returns a new empty registry.
     */
    public final static DatatypeHelperRegistry newEmptyRegistry() {
        return new DefaultDatatypeHelperRegistry();
    }

    /**
     * Overridden method.
     * @see org.faktorips.codegen.DatatypeHelperRegistry#getHelper(org.faktorips.datatype.Datatype)
     */
    public DatatypeHelper getHelper(Datatype datatype) {
        return (DatatypeHelper)helpers.get(datatype);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.codegen.DatatypeHelperRegistry#register(org.faktorips.codegen.DatatypeHelper)
     */
    public void register(DatatypeHelper helper) {
        helpers.put(helper.getDatatype(), helper);
    }

}
