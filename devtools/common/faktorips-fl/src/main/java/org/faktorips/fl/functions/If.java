/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.functions;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.FunctionSignatures;

/**
 * This class implements the if statement for the condition of type
 * {@link Datatype#PRIMITIVE_BOOLEAN}.
 */
public class If extends AbstractIf {

    public If(String name, String description) {
        super(name, description, FunctionSignatures.If);
    }

}
