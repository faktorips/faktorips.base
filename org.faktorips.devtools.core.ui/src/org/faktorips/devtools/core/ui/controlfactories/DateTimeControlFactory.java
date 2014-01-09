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
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.joda.LocalDateTimeDatatype;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.AbstractDateTimeControl;
import org.faktorips.devtools.core.ui.controls.DateTimeControl;
import org.faktorips.devtools.core.ui.inputformat.AbstractInputFormat;
import org.faktorips.devtools.core.ui.inputformat.DateTimeISOStringFormat;

/**
 * A factory for edit fields/controls for {@link LocalDateTimeDatatype}.
 * 
 * @since 3.7
 */
public class DateTimeControlFactory extends AbstractDateTimeControlFactory {

    public DateTimeControlFactory() {
        super();
    }

    @Override
    public boolean isFactoryFor(ValueDatatype datatype) {
        return LocalDateTimeDatatype.DATATYPE.equals(datatype);
    }

    @Override
    protected AbstractInputFormat<String> getFormat() {
        return DateTimeISOStringFormat.newInstance();
    }

    @Override
    protected AbstractDateTimeControl createDateTimeControl(Composite parent, UIToolkit toolkit) {
        return new DateTimeControl(parent, toolkit);
    }

}
