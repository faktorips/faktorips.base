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
import org.faktorips.datatype.joda.LocalDateTimeDatatype;

/**
 * {@link DatatypeHelper} for {@link LocalDateTimeDatatype}.
 */
public class LocalDateTimeHelper extends BaseJodaDatatypeHelper {

    public static final String ORG_JODA_TIME_LOCAL_DATE_TIME = "org.joda.time.LocalDateTime"; //$NON-NLS-1$

    public LocalDateTimeHelper() {
        super(ORG_JODA_TIME_LOCAL_DATE_TIME, "toLocalDateTime"); //$NON-NLS-1$
    }

}
