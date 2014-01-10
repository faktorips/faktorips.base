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
import org.faktorips.datatype.joda.LocalTimeDatatype;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.AbstractDateTimeControl;
import org.faktorips.devtools.core.ui.controls.TimeControl;
import org.faktorips.devtools.core.ui.inputformat.AbstractInputFormat;
import org.faktorips.devtools.core.ui.inputformat.TimeISOStringFormat;

/**
 * A factory for edit fields/controls for {@link LocalTimeDatatype}.
 * 
 * @since 3.7
 */
public class TimeControlFactory extends AbstractDateTimeControlFactory {

    public TimeControlFactory() {
        super();
    }

    @Override
    public boolean isFactoryFor(ValueDatatype datatype) {
        return LocalTimeDatatype.DATATYPE.equals(datatype);
    }

    @Override
    protected AbstractInputFormat<String> getFormat() {
        return TimeISOStringFormat.newInstance();
    }

    @Override
    protected AbstractDateTimeControl createDateTimeControl(Composite parent, UIToolkit toolkit) {
        return new TimeControl(parent, toolkit);
    }

}
