/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl;

import java.util.HashMap;
import java.util.Map;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;

/**
 * Default provider for the default datatypes and their Java helpers.
 * 
 * @author Jan Ortmann
 */
public class DefaultDatatypeHelperProvider implements DatatypeHelperProvider<JavaCodeFragment> {

    private Map<Datatype, DatatypeHelper> helpers = new HashMap<Datatype, DatatypeHelper>();

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
        return helpers.get(datatype);
    }

}
