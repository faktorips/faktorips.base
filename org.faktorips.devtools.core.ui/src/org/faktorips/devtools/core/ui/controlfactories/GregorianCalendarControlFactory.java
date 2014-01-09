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
import org.faktorips.devtools.core.ui.inputformat.AbstractInputFormat;
import org.faktorips.devtools.core.ui.inputformat.DateISOStringFormat;

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

    @SuppressWarnings("deprecation")
    // this is only for compatibility to this deprecation
    @Override
    public boolean isFactoryFor(ValueDatatype datatype) {
        return Datatype.GREGORIAN_CALENDAR.equals(datatype) || Datatype.GREGORIAN_CALENDAR_DATE.equals(datatype)
                || LocalDateDatatype.DATATYPE.equals(datatype);
    }

    @Override
    protected AbstractInputFormat<String> getFormat() {
        return DateISOStringFormat.newInstance();
    }

    @Override
    protected AbstractDateTimeControl createDateTimeControl(Composite parent, UIToolkit toolkit) {
        return new DateControl(parent, toolkit);
    }

}
