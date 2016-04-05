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
package org.faktorips.fl.functions.joda;

import org.faktorips.codegen.dthelpers.ILocalDateHelper.Period;
import org.faktorips.fl.FunctionSignatures;

public class Months extends AbstractPeriodFunction {

    public Months(String name, String description) {
        super(name, description, FunctionSignatures.MONTHS, Period.MONTHS);
    }

}
