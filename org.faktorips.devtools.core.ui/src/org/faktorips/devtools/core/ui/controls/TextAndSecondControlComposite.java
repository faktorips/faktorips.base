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

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.UIToolkit;

public abstract class TextAndSecondControlComposite extends ControlComposite {

    /** text and second controls */
    private Text text;

    private Control secondControl;

    protected boolean immediatelyNotifyListener = false;

    public TextAndSecondControlComposite(Composite parent, UIToolkit toolkit, boolean smallMargins,
            int buttonHeightHint, int style) {

        super(parent, SWT.NONE);
        setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);
        if (toolkit.getFormToolkit() == null) {
            text = toolkit.createTextAppendStyle(this, SWT.SINGLE | style);
        } else {
            Composite c = toolkit.getFormToolkit().createComposite(this);
            GridLayout layout2 = new GridLayout(2, false);
            if (smallMargins) {
                layout2.marginHeight = 2;
                layout2.marginWidth = 1;
            } else {
                layout2.marginHeight = 3;
                layout2.marginWidth = 1;
            }
            c.setLayout(layout2);
            c.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL));
            toolkit.getFormToolkit().paintBordersFor(c);
            text = toolkit.createText(c, style);
            // toolkit.getFormToolkit().adapt(this); // has to be done after the text control is
            // created!
        }
        secondControl = createSecondControl(toolkit);

        text.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL));
        GridData d = new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_VERTICAL);
        if (buttonHeightHint > -1) {
            d.heightHint = buttonHeightHint;
        }
        secondControl.setLayoutData(d);
        addListeners();
    }

    protected abstract void addListeners();

    @Override
    public void setEnabled(boolean value) {
        text.setEnabled(value);
        secondControl.setEnabled(value);
    }

    protected abstract Control createSecondControl(UIToolkit toolkit);

    protected Control getSecondControl() {
        return secondControl;
    }

    public void setText(String newText) {
        immediatelyNotifyListener = true;
        try {
            text.setText(newText);
        } finally {
            immediatelyNotifyListener = false;
        }
    }

    public boolean isImmediatelyNotifyListener() {
        return immediatelyNotifyListener;
    }

    public String getText() {
        return text.getText();
    }

    public Text getTextControl() {
        return text;
    }

    @Override
    public boolean setFocus() {
        return text.setFocus();
    }

    @Override
    public void addListener(int eventType, Listener listener) {
        super.addListener(eventType, listener);
        if (eventType != SWT.Paint && eventType != SWT.Dispose) {
            listenToControl(text, eventType);
        }
    }

}