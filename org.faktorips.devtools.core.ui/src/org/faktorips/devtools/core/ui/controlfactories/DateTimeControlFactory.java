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
import org.faktorips.datatype.joda.LocalDateTimeDatatype;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.AbstractInputFormat;
import org.faktorips.devtools.core.ui.controller.fields.DateTimeISOStringFormat;
import org.faktorips.devtools.core.ui.controls.AbstractDateTimeControl;
import org.faktorips.devtools.core.ui.controls.DateTimeControl;

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
