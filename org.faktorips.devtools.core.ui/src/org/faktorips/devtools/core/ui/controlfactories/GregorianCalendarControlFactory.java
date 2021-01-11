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

import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.GregorianCalendarAsDateDatatype;
import org.faktorips.datatype.classtypes.GregorianCalendarDatatype;
import org.faktorips.datatype.joda.LocalDateDatatype;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.AbstractDateTimeControl;
import org.faktorips.devtools.core.ui.controls.DateControl;

/**
 * A factory for edit fields/controls for {@link GregorianCalendarDatatype},
 * {@link GregorianCalendarAsDateDatatype Date} and {@link LocalDateDatatype}.
 * 
 * @author Stefan Widmaier
 * @since 3.2
 */
public class GregorianCalendarControlFactory extends AbstractDateTimeControlFactory {

    public GregorianCalendarControlFactory() {
        super();
    }

    @Override
    public boolean isFactoryFor(ValueDatatype datatype) {
        return Datatype.GREGORIAN_CALENDAR.equals(datatype) || LocalDateDatatype.DATATYPE.equals(datatype);
    }

    @Override
    protected AbstractDateTimeControl createDateTimeControl(Composite parent, UIToolkit toolkit) {
        return new DateControl(parent, toolkit);
    }

}
