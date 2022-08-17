/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;

/**
 * Aggregates a set of radio buttons into an SWT {@link Group} which is created automatically.
 * <p>
 * Clients provide a list of options via the constructor. For each option, a radio button belonging
 * to the {@link Group} is created.
 * 
 * @param <T> the options data type; using the {@link RadioButtonGroup}, the user chooses between
 *            different instances of this type
 * 
 * @author Daniel Hohenberger
 * @author Alexander Weickmann
 */
public class RadioButtonGroup<T> {

    private final LinkedHashMap<T, Button> radioButtons;
    private final LinkedHashMap<Button, T> buttonsToOptions;

    private final LinkedHashMap<T, String> options;

    private final Composite composite;

    private final UIToolkit toolkit;

    /**
     * @deprecated deprecated since version 3.6, this field merely remains for proper function of
     *                 the deprecated method {@link #addRadiobutton(String)}
     */
    @Deprecated
    private final List<CheckboxField> oldCheckboxFields = new ArrayList<>();

    /**
     * This constructor was used prior to version 3.6. It does not automatically create radio
     * buttons but requires the client to invoke {@link #addRadiobutton(String)}. With version 3.6,
     * a new constructor was introduced which automatically creates the necessary radio buttons on
     * the basis of an <em>options</em> {@link Map}.
     * 
     * @param style the SWT style that is applied to the SWT {@link Group}
     * @param text the text that is displayed as title of the SWT {@link Group}
     * 
     * @deprecated deprecated since version 3.6 due to refactoring, use the new constructor
     *                 {@link #RadioButtonGroup(Composite, String, int, LinkedHashMap, UIToolkit)}
     *                 instead. Note that the new constructor does not feature a 'style' parameter
     *                 as it was decided to generally use the SWT composite default style
     */
    @Deprecated
    public RadioButtonGroup(Composite parent, int style, String text, UIToolkit toolkit) {
        this.toolkit = toolkit;
        radioButtons = new LinkedHashMap<>();
        buttonsToOptions = new LinkedHashMap<>();
        options = null;
        composite = createGroupControl(parent, style, text, 1, toolkit);
    }

    /**
     * Creates a new {@link RadioButtonGroup} and automatically creates a radio button for each
     * provided option.
     * 
     * @param text the text that is displayed as title of the SWT {@link Group}
     * @param numberColumns specifies how many radio buttons are placed horizontally beside each
     *            other
     * @param options the options the user can choose from. The map associates each value with it's
     *            label. For each option, a radio button is created
     */
    public RadioButtonGroup(Composite parent, String text, int numberColumns, LinkedHashMap<T, String> options,
            UIToolkit uiToolkit) {
        this.options = new LinkedHashMap<>(options);
        radioButtons = new LinkedHashMap<>(options.size());
        buttonsToOptions = new LinkedHashMap<>(options.size());
        toolkit = uiToolkit;
        composite = createGroupControl(parent, SWT.NONE, text, numberColumns, uiToolkit);
        createRadioButtons(uiToolkit);
    }

    /**
     * Creates a new {@link RadioButtonGroup} without creating a {@link Group}. The radiobuttons are
     * created directly in the parent composite. You have the choice if the parent composite should
     * be a group, a native composite or any other kind of composite. You also have to specify the
     * layout of the parent yourself.
     * 
     * @param parent The parent composite for the radio buttons
     * @param options the options the user can choose from. The map associates each value with it's
     *            label. For each option, a radio button is created
     */
    public RadioButtonGroup(Composite parent, LinkedHashMap<T, String> options, UIToolkit uiToolkit) {
        this.options = new LinkedHashMap<>(options);
        radioButtons = new LinkedHashMap<>(options.size());
        buttonsToOptions = new LinkedHashMap<>(options.size());
        toolkit = uiToolkit;
        composite = parent;
        createRadioButtons(uiToolkit);
    }

    private Group createGroupControl(Composite parent, int style, String text, int numberColumns, UIToolkit toolkit) {
        Group group = toolkit.createGridGroup(parent, style, text, numberColumns, false);

        // Radio buttons should be apart from each other a little bit farther (horizontally)
        ((GridLayout)group.getLayout()).horizontalSpacing = 16;
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return group;
    }

    private void createRadioButtons(UIToolkit uiToolkit) {
        for (T key : options.keySet()) {
            addRadioButton(key, options.get(key), uiToolkit);
        }
    }

    private Button addRadioButton(T value, String text, UIToolkit uiTookit) {
        Button radioButton = uiTookit.createRadioButton(composite, text);
        radioButtons.put(value, radioButton);
        buttonsToOptions.put(radioButton, value);
        return radioButton;
    }

    /**
     * @deprecated deprecated since version 3.6, use the constructor
     *                 {@link #RadioButtonGroup(Composite, String, int, LinkedHashMap, UIToolkit)}
     *                 instead, which automatically creates a radio button for each possible option.
     *                 <strong>Do not use this method to add further radio buttons after creating a
     *                 {@link RadioButtonGroup} via the new constructor as this method merely
     *                 remains to support usage of the old constructor!</strong>
     */
    @Deprecated
    public final Radiobutton addRadiobutton(String text) {
        Radiobutton radiobutton = toolkit.createRadiobutton(composite, text);
        CheckboxField checkboxField = new CheckboxField(radiobutton);
        checkboxField.addChangeListener(e -> {
            if (e.field instanceof CheckboxField) {
                CheckboxField changedField = (CheckboxField)e.field;
                if (changedField.getCheckbox().isChecked()) {
                    for (CheckboxField checkboxField1 : oldCheckboxFields) {
                        if (!checkboxField1.equals(changedField)) {
                            checkboxField1.getCheckbox().setChecked(false);
                        }
                    }
                }
            }
        });
        oldCheckboxFields.add(checkboxField);
        return radiobutton;
    }

    /**
     * Returns the SWT {@link Group} wrapped by this {@link RadioButtonGroup}.
     */
    public final Composite getComposite() {
        return composite;
    }

    /**
     * Returns the radio buttons belonging to this {@link RadioButtonGroup}.
     */
    public final List<Button> getRadioButtons() {
        return new ArrayList<>(radioButtons.values());
    }

    /**
     * Returns the value that is associated with the indicated radio button or null if no value is
     * associated with the button.
     */
    public final T getOption(Button button) {
        return buttonsToOptions.get(button);
    }

    /**
     * Returns the button that is associated with the indicated option or null if no button is
     * associated with the option.
     * 
     * @param option the option to get the button for
     * @return the button representing the option
     */
    public final Button getRadioButton(T option) {
        return radioButtons.get(option);
    }

    /**
     * Returns the value that is associated with the currently selected radio button or null if no
     * button is selected.
     */
    public final T getSelectedOption() {
        Button selectedButton = getSelectedButton();
        if (selectedButton == null) {
            return null;
        }
        return buttonsToOptions.get(selectedButton);
    }

    /**
     * Returns the radio button that is currently selected or null if no button is selected.
     */
    public final Button getSelectedButton() {
        for (Button button : radioButtons.values()) {
            if (button.getSelection()) {
                return button;
            }
        }
        return null;
    }

    /**
     * Selects the radio button associated with indicated value.
     */
    public final void setSelection(T option) {
        Button currentSelection = getSelectedButton();
        if (currentSelection != null) {
            currentSelection.setSelection(false);
        }
        Button toSelect = radioButtons.get(option);
        if (toSelect != null) {
            toSelect.setSelection(true);
        }
    }

}
