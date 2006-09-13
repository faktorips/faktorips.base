/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;

/**
 * 
 * @author Daniel Hohenberger
 */
public class RadiobuttonGroup implements ValueChangeListener {

    private Group group;
    private UIToolkit toolkit;
    private List checkboxFields = new ArrayList();

    /**
     * Creates a <code>org.eclipse.swt.widgets.Group</code> and controls
     * <code>org.faktorips.devtools.core.ui.controls.Radiobutton</code>s added to it via the 
     * <code>RadiobuttonGroup#addRadiobutton</code> method.
     * 
     * @param parent
     * @param style
     */
    public RadiobuttonGroup(Composite parent, int style, UIToolkit toolkit) {
        group = new Group(parent, style);
        this.toolkit = toolkit;
    }

    public Radiobutton addRadiobutton(String text) {
        Radiobutton radiobutton = toolkit.createRadiobutton(group, text);
        CheckboxField checkboxField = new CheckboxField(radiobutton);
        checkboxField.addChangeListener(this);
        checkboxFields.add(checkboxField);
        return radiobutton;
    }

    public void valueChanged(FieldValueChangedEvent e) {
        if (e.field instanceof CheckboxField) {
            CheckboxField changedField = (CheckboxField)e.field;
            if (changedField.getCheckbox().isChecked()) {
                for (Iterator it = checkboxFields.iterator(); it.hasNext();) {
                    CheckboxField checkboxField = (CheckboxField)it.next();
                    if (!checkboxField.equals(changedField)) {
                        checkboxField.getCheckbox().setChecked(false);
                    }
                }
            }
        }
    }

    /**
     * @return the group
     */
    public Group getGroup() {
        return group;
    }

}
