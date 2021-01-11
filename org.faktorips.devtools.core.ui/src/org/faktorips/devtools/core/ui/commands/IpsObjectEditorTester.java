/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.expressions.PropertyTester;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;

public class IpsObjectEditorTester extends PropertyTester {

    public static final String PROPERTY_EDITOR_EDITABLE = "isEditorEditable"; //$NON-NLS-1$

    public IpsObjectEditorTester() {
        super();
    }

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        IpsObjectEditor editor = (IpsObjectEditor)receiver;
        if (PROPERTY_EDITOR_EDITABLE.equals(property)) {
            return editor.isDataChangeable();
        } else {
            return false;
        }
    }

}
