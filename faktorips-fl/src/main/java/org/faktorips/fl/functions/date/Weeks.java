/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.functions.date;

import org.faktorips.codegen.dthelpers.ILocalDateHelper.Period;
import org.faktorips.fl.FunctionSignatures;

public class Weeks extends AbstractPeriodFunction {

    public Weeks(String name, String description) {
        super(name, description, FunctionSignatures.WEEKS, Period.WEEKS);
    }

}
