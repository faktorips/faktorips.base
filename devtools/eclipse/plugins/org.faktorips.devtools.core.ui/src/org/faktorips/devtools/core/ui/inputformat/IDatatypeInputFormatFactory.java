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

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * IInputFormatFactory registered with the <em>inputFormat</em> extension point.
 * 
 * @since 3.11
 */

public interface IDatatypeInputFormatFactory {

    /**
     * Instantiate a specific data type with respect to the provided data type. It is in the
     * responsibility of the factory provider if the data type is considered.
     * 
     * @param datatype the data type for which you want retrieve an input format
     * @param ipsProject the project provided to the input format for example to get the default
     *            currency in money values
     */
    IInputFormat<String> newInputFormat(ValueDatatype datatype, IIpsProject ipsProject);

}
