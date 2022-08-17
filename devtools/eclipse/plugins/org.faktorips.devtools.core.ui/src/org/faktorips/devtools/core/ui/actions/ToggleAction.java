/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.ArgumentCheck;

/**
 * A toggle button that toggles a boolean property.
 */
public class ToggleAction extends Action {
    private final IObservableValue<Boolean> value;

    /**
     * Creates a new instance of {@link ToggleAction}.
     * 
     * @param description the description of this action used as a tooltip
     * @param iconName the name of the icon
     * @param value the value to toggle
     */
    public ToggleAction(String description, String iconName, IObservableValue<Boolean> value) {
        super(null, SWT.TOGGLE);

        ArgumentCheck.notNull(value);
        ArgumentCheck.isTrue(value.getValueType() == Boolean.class);
        this.value = value;

        setChecked(value.getValue() == Boolean.TRUE);
        setToolTipText(description);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(iconName));

        value.addValueChangeListener(event -> ToggleAction.this.setChecked(event.getObservableValue().getValue()));
    }

    @Override
    public void run() {
        Object oldValue = value.getValue();
        value.setValue(oldValue == Boolean.TRUE ? Boolean.FALSE : Boolean.TRUE);
    }

}
