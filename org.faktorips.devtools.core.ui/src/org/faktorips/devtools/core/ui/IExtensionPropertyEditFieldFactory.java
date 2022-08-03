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
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;

/**
 * Allows implementors to provide specific controls for extension properties.
 * <p>
 * <strong>Subclassing:</strong><br>
 * One {@link IExtensionPropertyEditFieldFactory} can be used for creating the controls of multiple
 * extension properties (it can be referenced by the extension point any number of times).
 */
public interface IExtensionPropertyEditFieldFactory {

    /**
     * Returns a new {@link EditField} that allows to edit the value of the extension property this
     * factory is associated with.
     * <p>
     * <strong>Subclassing:</strong><br>
     * Implementors must take care of the fact that the given {@link IIpsObjectPartContainer} could
     * be {@code null}. In this case, this operation is still expected to create an appropriate
     * {@link EditField}. If an object is given, this operation is free to exploit any information
     * given by the object, for example to provide features like auto-complete.
     * <p>
     * Furthermore, implementors must take care to create the control as a child of the given parent
     * {@link Composite}. Otherwise the control won't appear at the location expected by the caller.
     * <p>
     * It is strongly recommended to use the given {@link UIToolkit} for creating the control as
     * this is the only way to ensure that it's style will be consistent with the rest of the UI.
     *
     * @param ipsObjectPartContainer The {@link IIpsObjectPartContainer} that contains the extension
     *            property for which the edit field is created. May also be {@code null} if the
     *            containing object is unknown
     * @param parent The parent composite of the control to be created
     * @param toolkit The {@link UIToolkit} that should be used to create the control to ensure a
     *            consistent user interface
     */
    EditField<?> newEditField(IIpsObjectPartContainer ipsObjectPartContainer,
            Composite parent,
            UIToolkit toolkit);

}
