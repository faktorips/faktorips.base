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

import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Edit field for text controls, used only for MultiValue inputs, the value type is {@link String}.
 * <p>
 * Empty strings are now considered {@code null}, there read the null presentation but do not write
 * it.
 * 
 * @since 23.6
 */
public class MultiValueTextField extends AbstractTextField<String> {

    public MultiValueTextField() {
        super();
    }

    public MultiValueTextField(Text control) {
        super(control);
    }

    @Override
    public void setValue(String object) {
        if (object == null) {
            setText(IpsStringUtils.EMPTY);
        } else {
            setText(object);
        }
    }

    @Override
    protected String parseContent() {
        String content = getText();
        if (content == null || content.isEmpty()
                || IpsPlugin.getDefault().getIpsPreferences().getNullPresentation().equals(content)) {
            return null;
        }
        return content;
    }

}
