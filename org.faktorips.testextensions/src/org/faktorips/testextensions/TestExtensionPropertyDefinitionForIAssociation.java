/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.testextensions;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.IExtensionPropertyEditFieldFactory;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.model.extproperties.BooleanExtensionPropertyDefinition;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;

public class TestExtensionPropertyDefinitionForIAssociation extends BooleanExtensionPropertyDefinition {

    public TestExtensionPropertyDefinitionForIAssociation() {
        super();
    }

    public static class BooleanExtensionPropertyEditFieldFactory implements IExtensionPropertyEditFieldFactory {

        public BooleanExtensionPropertyEditFieldFactory() {
            // default constructor for Extension Point
        }

        @Override
        public EditField<?> newEditField(IIpsObjectPartContainer ipsObjectPart,
                Composite extensionArea,
                UIToolkit toolkit) {
            return new CheckboxField(toolkit.createCheckbox(extensionArea, "chosen"));
        }

    }

}
