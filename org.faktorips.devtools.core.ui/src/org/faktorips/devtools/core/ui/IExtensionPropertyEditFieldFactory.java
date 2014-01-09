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

package org.faktorips.devtools.core.ui;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.ui.controller.EditField;

public interface IExtensionPropertyEditFieldFactory {

    /**
     * Returns a new EditField that allows to edit the property value. Can return <code>null</code>
     * if <code>isEditedInStandardExtensionArea()</code> returns <code>false</code>.
     * 
     * @param ipsObjectPart The ips object part which extension property an edit field is created
     *            for.
     * @param extensionArea The standard extension area composite. A new control that allows editing
     *            the property value has to be added to this composite in subclasses. The EditField
     *            has to be constructed based on the control.
     * @param toolkit The ui toolkit to be used to create the control to ensure a consistent user
     *            interface.
     */
    public EditField<?> newEditField(IIpsObjectPartContainer ipsObjectPart, Composite extensionArea, UIToolkit toolkit);

}
