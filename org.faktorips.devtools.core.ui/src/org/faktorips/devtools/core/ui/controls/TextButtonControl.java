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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * A composite that consists of a text control on the left and a button attached to it on the right.
 * If the button is clicked, the method <code>buttonClicked</code> is called. The method has to
 * implemented in subclasses.
 */
public abstract class TextButtonControl extends TextAndSecondControlComposite {

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
        this(parent, toolkit, buttonText, smallMargins, buttonHeightHint, SWT.NONE);
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

    public TextButtonControl(Composite parent, UIToolkit toolkit, String buttonText, boolean smallMargins,
            int buttonHeightHint, int style) {
        super(parent, toolkit, smallMargins, buttonHeightHint, style);
        getButtonControl().setText(buttonText);
    }

    @Override
    protected void addListeners() {
        getButtonControl().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                buttonClicked();
            }
        });
    }

    @Override
    protected Control createSecondControl(UIToolkit toolkit) {
        return toolkit.createButton(this, ""); //$NON-NLS-1$
    }

    protected abstract void buttonClicked();

    @Override
    protected Button getSecondControl() {
        return (Button)super.getSecondControl();
    }

    public void setButtonEnabled(boolean value) {
        getButtonControl().setEnabled(value);
    }

    public void setButtonImage(Image image) {
        getButtonControl().setImage(image);
    }

    protected Button getButtonControl() {
        return getSecondControl();
    }

}
