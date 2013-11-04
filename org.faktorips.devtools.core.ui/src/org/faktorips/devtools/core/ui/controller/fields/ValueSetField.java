/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.editors.productcmpt.AnyValueSetControl;

public class ValueSetField extends FormattingTextField<IValueSet> {

    private AnyValueSetControl valueSetControl;

    public ValueSetField(AnyValueSetControl valueSetControl, ValueSetFormat format) {
        this(valueSetControl, format, true);
    }

    public ValueSetField(AnyValueSetControl valueSetControl, ValueSetFormat format, boolean formatOnFocusLost) {
        super(valueSetControl.getTextControl(), format, formatOnFocusLost);
        this.valueSetControl = valueSetControl;
    }

    @Override
    public Control getControl() {
        return valueSetControl;
    }

    @Override
    public boolean isTextContentParsable() {
        return true;
    }
}
