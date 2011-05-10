/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.ui.controls.DateControl;

public class DateControlField<T> extends FormattingTextField<T> {

    private final DateControl dateControl;

    public DateControlField(DateControl dateControl, AbstractInputFormat<T> format) {
        this(dateControl, format, true);
    }
    
    public DateControlField(DateControl dateControl, AbstractInputFormat<T> format, boolean formatOnFocusLost) {
        super(dateControl.getTextControl(), format, formatOnFocusLost);
        this.dateControl = dateControl;
    }
    
    

    @Override
    public Control getControl() {
        return dateControl;
    }

}
