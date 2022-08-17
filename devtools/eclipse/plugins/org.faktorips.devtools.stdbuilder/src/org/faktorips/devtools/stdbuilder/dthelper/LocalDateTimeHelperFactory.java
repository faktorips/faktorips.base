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
import org.faktorips.datatype.joda.LocalDateTimeDatatype;

public class LocalDateTimeHelperFactory extends AbstractDateHelperFactory<LocalDateTimeDatatype> {

    public LocalDateTimeHelperFactory() {
        super(LocalDateTimeDatatype.class);
    }

    @Override
    DatatypeHelper createDatatypeHelper(LocalDateTimeDatatype datatype, LocalDateHelperVariant variant) {
        switch (variant) {
            case JAVA8:
                return new org.faktorips.codegen.dthelpers.java8.LocalDateTimeHelper(datatype);
            default:
                return new org.faktorips.codegen.dthelpers.joda.LocalDateTimeHelper(datatype);
        }
    }

}
