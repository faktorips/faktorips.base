/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * A composite that consists of a text control on the left and a button attached to it on the right.
 * If the button is clicked, the method <code>buttonClicked</code> is called. The method has to
 * implemented in subclasses.
 */
public abstract class TextButtonControl extends ControlComposite {

    /** text and button controls */
    protected Text text;

    private Button button;

    protected boolean immediatelyNotifyListener = false;

    /**
     * Creates a textfield and a button. The given buttonHeightHint is used to set the heightHint
     * for the layout-data for the button.
     * 
     * @param parent The parent composite.
     * @param toolkit The UIToolkit to use for the creation of the controlls.
     * @param buttonText The label for the button.
     * @param smallMargins <code>true</code> to get the smallest margin possible.
     * @param buttonHeightHint The preferred height or -1.
     */
    public TextButtonControl(Composite parent, UIToolkit toolkit, String buttonText, boolean smallMargins,
            int buttonHeightHint) {

        super(parent, SWT.NONE);
        setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);
        if (toolkit.getFormToolkit() == null) {
            text = toolkit.createText(this);
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
            text = toolkit.createText(c);
            toolkit.getFormToolkit().adapt(this); // has to be done after the text control is
            // created!
        }
        text.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL));
        button = toolkit.createButton(this, buttonText);
        GridData d = new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_VERTICAL);
        if (buttonHeightHint > -1) {
            d.heightHint = buttonHeightHint;
        }
        button.setLayoutData(d);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                buttonClicked();
            }
        });
    }

    /**
     * Creates a textfield and a button. The height of the button is not modified, margins are not
     * minimized.
     * 
     * @param parent The parent composite.
     * @param toolkit The UIToolkit to use for the creation of the controlls.
     * @param buttonText The label for the button.
     */
    public TextButtonControl(Composite parent, UIToolkit toolkit, String buttonText) {
        this(parent, toolkit, buttonText, false, -1);
    }

    protected abstract void buttonClicked();

    @Override
    public void setEnabled(boolean value) {
        text.setEnabled(value);
        button.setEnabled(value);
    }

    public void setButtonEnabled(boolean value) {
        button.setEnabled(value);
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
