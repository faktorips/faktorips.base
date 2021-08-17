/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.dthelpers.joda;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.joda.LocalDateTimeDatatype;

/**
 * {@link DatatypeHelper} for {@link LocalDateTimeDatatype}.
 */
public class LocalDateTimeHelper extends BaseJodaDatatypeHelper {

    public static final String ORG_JODA_TIME_LOCAL_DATE_TIME = "org.joda.time.LocalDateTime"; //$NON-NLS-1$

    private static final String PARSE_METHOD = "toLocalDateTime"; //$NON-NLS-1$

    private static final String DATE_TIME_NO_MILLIS = "dateTimeNoMillis"; //$NON-NLS-1$

    public LocalDateTimeHelper() {
        super(ORG_JODA_TIME_LOCAL_DATE_TIME, PARSE_METHOD);
    }

    public LocalDateTimeHelper(LocalDateTimeDatatype d) {
        super(d, ORG_JODA_TIME_LOCAL_DATE_TIME, PARSE_METHOD);
    }

    @Override
    protected void appendToStringParameter(JavaCodeFragment fragment) {
        fragment.appendClassName(ORG_JODA_TIME_FORMAT_ISO_DATE_TIME_FORMAT);
        fragment.append('.');
        fragment.append(DATE_TIME_NO_MILLIS);
        fragment.append("()"); //$NON-NLS-1$
    }

}
