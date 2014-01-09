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

package org.faktorips.devtools.core.ui.inputformat;

import org.faktorips.datatype.ValueDatatype;

/**
 * Factory that creates a DateTimeISOStringFormat for a Datatype
 */
public class DateTimeISOStringFormatFactory implements IDatatypeInputFormatFactory {

    @Override
    public IInputFormat<String> newInputFormat(ValueDatatype datatype) {
        return DateTimeISOStringFormat.newInstance();
    }
}
