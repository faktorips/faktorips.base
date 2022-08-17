/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen;

import org.faktorips.datatype.ConversionMatrix;
import org.faktorips.datatype.Datatype;

/**
 *
 */
public class DatatypeConverter implements ConversionMatrix {

    /**
     * 
     */
    public DatatypeConverter() {
        super();
    }

    @Override
    public boolean canConvert(Datatype from, Datatype to) {
        return false;
    }

}
