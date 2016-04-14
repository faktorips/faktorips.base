/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.codegen.dthelpers.joda;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.joda.LocalTimeDatatype;

/**
 * {@link DatatypeHelper} for {@link LocalTimeDatatype}.
 */
public class LocalTimeHelper extends BaseJodaDatatypeHelper {

    public static final String ORG_JODA_TIME_LOCAL_TIME = "org.joda.time.LocalTime"; //$NON-NLS-1$

    private static final String PARSE_METHOD = "toLocalTime"; //$NON-NLS-1$

    public LocalTimeHelper() {
        super(ORG_JODA_TIME_LOCAL_TIME, PARSE_METHOD);
    }

    public LocalTimeHelper(LocalTimeDatatype d) {
        super(d, ORG_JODA_TIME_LOCAL_TIME, PARSE_METHOD);
    }

}
