/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.dthelper;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.joda.LocalTimeDatatype;

public class LocalTimeHelperFactory extends AbstractDateHelperFactory<LocalTimeDatatype> {

    public LocalTimeHelperFactory() {
        super(LocalTimeDatatype.class);
    }

    @Override
    DatatypeHelper createDatatypeHelper(LocalTimeDatatype datatype, LocalDateHelperVariant variant) {
        switch (variant) {
            case JAVA8:
                return new org.faktorips.codegen.dthelpers.java8.LocalTimeHelper(datatype);
            default:
                return new org.faktorips.codegen.dthelpers.joda.LocalTimeHelper(datatype);
        }
    }

}
