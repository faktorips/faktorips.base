/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.swt.widgets.Text;

/**
 * Edit field for text controls, the value type is {@link String}
 */
public class TextField extends AbstractTextField<String> {

    public TextField() {
        super();
    }

    public TextField(Text control) {
        super(control);
    }

    @Override
    public String parseContent() {
        return StringValueEditField.prepareObjectForGet(text.getText(), supportsNull());
    }

    @Override
    public void setValue(String newValue) {
        setText(StringValueEditField.prepareObjectForSet(newValue, supportsNull()));
    }

}
