/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;

/**
 * 
 * @author Daniel Hohenberger
 * @author Alexander Weickmann
 */
public class RadioButtonGroup {

    private final List<Button> radioButtons = new ArrayList<Button>();

    private final Group group;

    private final UIToolkit toolkit;

    /**
     * @deprecated merely remains for proper function of the deprecated method
     *             {@link #addRadiobutton(String)}
     */
    @Deprecated
    private final List<CheckboxField> oldCheckboxFields = new ArrayList<CheckboxField>();

    public RadioButtonGroup(Composite parent, int style, String text, int numberColumns, UIToolkit toolkit) {
        this.toolkit = toolkit;
        group = createGroupControl(parent, style, text, numberColumns, toolkit);
    }

    private Group createGroupControl(Composite parent, int style, String text, int numberColumns, UIToolkit toolkit) {
        Group group = toolkit.createGridGroup(parent, style, text, numberColumns, false);

        // Radio buttons should be apart from each other a little bit farther (horizontally)
        ((GridLayout)group.getLayout()).horizontalSpacing = 16;
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return group;
    }

    /**
     * @deprecated use {@link #addRadioButton(String)} instead
     */
    @Deprecated
    public final Radiobutton addRadiobutton(String text) {
        Radiobutton radiobutton = toolkit.createRadiobutton(group, text);
        CheckboxField checkboxField = new CheckboxField(radiobutton);
        checkboxField.addChangeListener(new ValueChangeListener() {
            @Override
            public void valueChanged(FieldValueChangedEvent e) {
                if (e.field instanceof CheckboxField) {
                    CheckboxField changedField = (CheckboxField)e.field;
                    if (changedField.getCheckbox().isChecked()) {
                        for (CheckboxField checkboxField : oldCheckboxFields) {
                            if (!checkboxField.equals(changedField)) {
                                checkboxField.getCheckbox().setChecked(false);
                            }
                        }
                    }
                }
            }
        });
        oldCheckboxFields.add(checkboxField);
        return radiobutton;
    }

    public final Button addRadioButton(String text) {
        Button radioButton = toolkit.createRadioButton(group, text);
        radioButtons.add(radioButton);
        return radioButton;
    }

    public final Button getSelectedButton() {
        for (Button button : radioButtons) {
            if (button.getSelection()) {
                return button;
            }
        }
        return null;
    }

    public final Group getGroup() {
        return group;
    }

    public final List<Button> getRadioButtons() {
        return new ArrayList<Button>(radioButtons);
    }

}
