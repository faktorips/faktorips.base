/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;

public class DefaultExtensionPropertyEditFieldFactory implements IExtensionPropertyEditFieldFactory {

    /**
     * Adds a <code>Text</code> control to the extension area and returns a <code>TextField</code>
     * based on it.
     */
    @Override
    public EditField<String> newEditField(IIpsObjectPartContainer ipsObjectPart,
            Composite extensionArea,
            UIToolkit toolkit) {
        Text text = toolkit.createText(extensionArea);
        return new TextField(text);
    }

}
