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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.faktorips.devtools.core.ui.UIToolkit;

public abstract class InputAndSecondControlComposite<T extends Control> extends ControlComposite {
    /** text and second controls */
    private Control secondControl;
    private Composite innerComposite;
    private T text;
    private boolean immediatelyNotifyListener = false;

    public InputAndSecondControlComposite(Composite parent, UIToolkit toolkit, boolean smallMargins,
            int buttonHeightHint, int style) {
        super(parent, SWT.NONE);
        setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);
        if (!(toolkit.getFormToolkit() == null)) {
            innerComposite = toolkit.getFormToolkit().createComposite(this);
            GridLayout layout2 = new GridLayout(2, false);
            if (smallMargins) {
                layout2.marginHeight = 2;
                layout2.marginWidth = 1;
            } else {
                layout2.marginHeight = 3;
                layout2.marginWidth = 1;
            }
            innerComposite.setLayout(layout2);
            innerComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL));
            toolkit.getFormToolkit().paintBordersFor(innerComposite);
            // toolkit.getFormToolkit().adapt(this); // has to be done after the text control is
            // created!
        }
        T textControl = createTextControl(toolkit, style);
        setTextControl(textControl);
        GridData textGridData = new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL);
        /*
         * SW 26.7.2011: Workaround for several layout/resize problems with TextButtonControls (see
         * FIPS-617).
         * 
         * In the ProdCmptEditor especially the formula edit fields caused problems as they
         * potentially contain long texts. Collapsing or expanding the formula section then caused
         * the whole section to be drawn as large as it needed to be to display the whole formula at
         * once. Setting the width hint to a "small" value prevents the text control to acquire
         * space at will. I have no idea why that is and why the text control grabbed so much space
         * in the first place.
         */
        textGridData.widthHint = 25;
        textControl.setLayoutData(textGridData);

        secondControl = createSecondControl(toolkit);
        GridData d = new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_VERTICAL);
        if (buttonHeightHint > -1) {
            d.heightHint = buttonHeightHint;
        }
        secondControl.setLayoutData(d);

        addListeners();
    }

    protected abstract T createTextControl(UIToolkit toolkit, int style);

    /**
     * Removes the margin from the inner composite. Can be used to reduce the size of the second
     * control.
     */
    protected void removeMargins() {
        if (innerComposite != null) {
            GridLayout layout2 = new GridLayout(2, false);
            layout2.marginHeight = 0;
            layout2.marginWidth = 0;
            innerComposite.setLayout(layout2);
            innerComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));
        }
    }

    protected abstract void addListeners();

    @Override
    public void setEnabled(boolean value) {
        super.setEnabled(value);
        text.setEnabled(value);
        secondControl.setEnabled(value);
    }

    protected abstract Control createSecondControl(UIToolkit toolkit);

    protected Control getSecondControl() {
        return secondControl;
    }

    protected Composite getInnerComposite() {
        return innerComposite;
    }

    public void setText(String newText) {
        setImmediatelyNotifyListener(true);
        try {
            setTextInternal(newText);
        } finally {
            setImmediatelyNotifyListener(false);
        }
    }

    protected abstract void setTextInternal(String newText);

    public boolean isImmediatelyNotifyListener() {
        return immediatelyNotifyListener;
    }

    public abstract String getText();

    public T getTextControl() {
        return text;
    }

    public void setTextControl(T text) {
        this.text = text;
    }

    public void setImmediatelyNotifyListener(boolean immediatelyNotifyListener) {
        this.immediatelyNotifyListener = immediatelyNotifyListener;
    }

    @Override
    public boolean setFocus() {
        return text.setFocus();
    }

    @Override
    public void addListener(int eventType, Listener listener) {
        super.addListener(eventType, listener);
        if (eventType != SWT.Paint && eventType != SWT.Dispose) {
            listenToControl((text), eventType);
        }
    }

    /**
     * When we set the background of a text composite control we only want the text field to get the
     * color. We use the background color e.g. in test cases to highlight the expected results.
     * 
     * {@inheritDoc}
     */
    @Override
    public void setBackground(Color color) {
        text.setBackground(color);
    }
}
