/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 *
 */
public abstract class AbstractCheckbox extends ControlComposite {

    private Button button;
    private boolean invertValue = false;

    protected AbstractCheckbox(Composite parent, UIToolkit toolkit, int checkboxStyle, boolean invertValue) {
        this(parent, toolkit, checkboxStyle);
        this.invertValue = invertValue;
    }

    /**
     * @param parent
     * @param style
     */
    protected AbstractCheckbox(Composite parent, UIToolkit toolkit, int checkboxStyle) {
        super(parent, SWT.NONE);
        GridData data = new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL);
        data.heightHint = 20;
        setLayoutData(data);
        GridLayout layout = new GridLayout();
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.marginHeight = 4;
        layout.marginWidth = 0;
        setLayout(layout);
        if (toolkit.getFormToolkit() != null) {
            button = toolkit.getFormToolkit().createButton(this, null, checkboxStyle);
            toolkit.getFormToolkit().adapt(this);
        } else {
            button = new Button(this, checkboxStyle);
        }
        button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    public Button getButton() {
        return button;
    }

    public boolean isChecked() {
        if (invertValue) {
            return !button.getSelection();
        }
        return button.getSelection();
    }

    public void setChecked(boolean checked) {
        if (invertValue) {
            button.setSelection(!checked);
            return;
        }
        button.setSelection(checked);
    }

    public void setText(String s) {
        button.setText(s);
    }

    public String getText() {
        return button.getText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(int eventType, Listener listener) {
        super.addListener(eventType, listener);
        if (eventType != SWT.Paint) {
            listenToControl(button, eventType);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getEnabled() {
        return button.getEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        button.setEnabled(enabled);
    }

}
