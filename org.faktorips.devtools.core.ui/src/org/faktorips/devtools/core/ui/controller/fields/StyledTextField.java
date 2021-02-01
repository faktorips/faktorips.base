/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.swt.custom.StyledText;

public class StyledTextField extends AbstractStyledTextField<String> {

    public StyledTextField() {
        super();
    }

    public StyledTextField(StyledText control) {
        super(control);
    }

    @Override
    public void setValue(String newValue) {
        setText(StringValueEditField.prepareObjectForSet(newValue, supportsNullStringRepresentation()));
    }

    @Override
    public String parseContent() {
        return StringValueEditField.prepareObjectForGet(getText(), supportsNullStringRepresentation());
    }

}
