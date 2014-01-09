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
        return StringValueEditField.prepareObjectForGet(getText(), supportsNullStringRepresentation());
    }

    @Override
    public void setValue(String newValue) {
        setText(StringValueEditField.prepareObjectForSet(newValue, supportsNullStringRepresentation()));
    }

}
