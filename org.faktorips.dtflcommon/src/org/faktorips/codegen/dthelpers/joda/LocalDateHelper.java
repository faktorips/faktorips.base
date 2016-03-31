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
import org.faktorips.datatype.joda.LocalDateDatatype;

/**
 * {@link DatatypeHelper} for {@link LocalDateDatatype}.
 */
public class LocalDateHelper extends BaseJodaDatatypeHelper {

    public static final String ORG_JODA_TIME_LOCAL_DATE = "org.joda.time.LocalDate"; //$NON-NLS-1$

    public LocalDateHelper() {
        super(ORG_JODA_TIME_LOCAL_DATE, "toLocalDate"); //$NON-NLS-1$
    }

}
