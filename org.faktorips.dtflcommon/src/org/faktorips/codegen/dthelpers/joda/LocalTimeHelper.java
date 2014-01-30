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
import org.faktorips.datatype.joda.LocalTimeDatatype;

/**
 * {@link DatatypeHelper} for {@link LocalTimeDatatype}.
 */
public class LocalTimeHelper extends BaseJodaDatatypeHelper {

    public LocalTimeHelper() {
        super("toLocalTime"); //$NON-NLS-1$
    }

}
