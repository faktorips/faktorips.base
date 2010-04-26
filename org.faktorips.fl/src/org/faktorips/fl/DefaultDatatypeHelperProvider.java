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

package org.faktorips.fl;

import java.util.HashMap;
import java.util.Map;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.Datatype;

/**
 * Default provider for the default datatypes and their helpers.
 * 
 * @author Jan Ortmann
 */
public class DefaultDatatypeHelperProvider implements DatatypeHelperProvider {

    private Map helpers = new HashMap();

    public DefaultDatatypeHelperProvider() {
        helpers.put(Datatype.INTEGER, DatatypeHelper.INTEGER);
        helpers.put(Datatype.BOOLEAN, DatatypeHelper.BOOLEAN);
        helpers.put(Datatype.STRING, DatatypeHelper.STRING);
        helpers.put(Datatype.DECIMAL, DatatypeHelper.DECIMAL);
        helpers.put(Datatype.MONEY, DatatypeHelper.MONEY);

        helpers.put(Datatype.PRIMITIVE_BOOLEAN, DatatypeHelper.PRIMITIVE_BOOLEAN);
        helpers.put(Datatype.PRIMITIVE_INT, DatatypeHelper.PRIMITIVE_INTEGER);
    }

    /**
     * {@inheritDoc}
     */
    public DatatypeHelper getDatatypeHelper(Datatype datatype) {
        return (DatatypeHelper)helpers.get(datatype);
    }

}
