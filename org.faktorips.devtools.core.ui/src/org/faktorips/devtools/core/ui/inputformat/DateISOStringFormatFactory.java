/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.inputformat;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Factory that creates a DateISOStringFormat for a Datatype
 * 
 */
public class DateISOStringFormatFactory implements IDatatypeInputFormatFactory {

    @Override
    public IInputFormat<String> newInputFormat(ValueDatatype datatype, IIpsProject ipsProject) {
        return DateISOStringFormat.newInstance();
    }
}
