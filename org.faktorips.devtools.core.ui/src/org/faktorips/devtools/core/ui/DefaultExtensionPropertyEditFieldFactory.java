/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;

/**
 * An {@link IExtensionPropertyEditFieldFactory} that is used for all extension properties for which
 * no specific factory is provided.
 * <p>
 * This factory provides a simple {@link Text} control for editing extension properties.
 * <p>
 * <strong>Subclassing:</strong><br>
 * It is not recommended to subclass this factory. Instead, subclass directly from
 * {@link IExtensionPropertyEditFieldFactory}.
 */
public class DefaultExtensionPropertyEditFieldFactory implements IExtensionPropertyEditFieldFactory {

    @Override
    public EditField<String> newEditField(IIpsObjectPartContainer ipsObjectPartContainer,
            Composite parent,
            UIToolkit toolkit) {
        return new TextField(toolkit.createText(parent));
    }

}
