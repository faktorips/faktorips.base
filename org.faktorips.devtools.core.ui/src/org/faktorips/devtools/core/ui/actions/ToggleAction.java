/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.ArgumentCheck;

/**
 * A toggle button that toggles a boolean property.
 */
public class ToggleAction extends Action {
    private final IObservableValue value;

    /**
     * Creates a new instance of {@link ToggleAction}.
     * 
     * @param description the description of this action used as a tooltip
     * @param iconName the name of the icon
     * @param value the value to toggle
     */
    public ToggleAction(String description, String iconName, IObservableValue value) {
        super(null, SWT.TOGGLE);

        ArgumentCheck.notNull(value);
        ArgumentCheck.isTrue(value.getValueType() == Boolean.class);
        this.value = value;

        setChecked(value.getValue() == Boolean.TRUE ? true : false);
        setToolTipText(description);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(iconName));

        value.addValueChangeListener(new IValueChangeListener() {
            @Override
            public void handleValueChange(ValueChangeEvent event) {
                ToggleAction.this.setChecked((Boolean)event.getObservableValue().getValue());
            }
        });
    }

    @Override
    public void run() {
        Object oldValue = value.getValue();
        value.setValue(oldValue == Boolean.TRUE ? Boolean.FALSE : Boolean.TRUE);
    }

}
