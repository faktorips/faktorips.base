/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controlfactories;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.joda.LocalTimeDatatype;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.AbstractInputFormat;
import org.faktorips.devtools.core.ui.controller.fields.TimeISOStringFormat;
import org.faktorips.devtools.core.ui.controls.AbstractDateTimeControl;
import org.faktorips.devtools.core.ui.controls.TimeControl;

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
