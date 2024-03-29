/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.inputformat;

import org.faktorips.datatype.NamedDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

public class NamedDatatypeInputFormatFactory implements IDatatypeInputFormatFactory {

    @Override
    public IInputFormat<String> newInputFormat(ValueDatatype datatype, IIpsProject ipsProject) {
        return NamedDatatypeInputFormat.newInstance((NamedDatatype)datatype);
    }

}
