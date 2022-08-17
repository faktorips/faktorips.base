/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controlfactories;

import org.eclipse.swt.SWT;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.joda.MonthDayDatatype;

public class MonthDayControlFactory extends DefaultControlFactory {

    @Override
    public boolean isFactoryFor(ValueDatatype datatype) {
        return MonthDayDatatype.DATATYPE.equals(datatype);
    }

    @Override
    public int getDefaultAlignment() {
        return SWT.RIGHT;
    }
}
