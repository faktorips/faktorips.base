/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.codegen.dthelpers.joda;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.joda.LocalDateDatatype;

/**
 * {@link DatatypeHelper} for {@link LocalDateDatatype}.
 */
public class LocalDateHelper extends BaseJodaDatatypeHelper {

    public LocalDateHelper() {
        super("toLocalDate"); //$NON-NLS-1$
    }

}
