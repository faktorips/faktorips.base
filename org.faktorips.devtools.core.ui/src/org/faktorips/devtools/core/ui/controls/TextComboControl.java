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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.EditField;

/**
 * A composite that consists of a text control on the left and a combo attached to it on the right.
 * Both controls can be retrieved using {@link #getTextControl()} and {@link #getComboControl()}.
 * {@link #setText(String)} and {@link #getText()} only work with the text control, although
 * {@link EditField}s may implement a different behavior by accessing both text and combo controls
 * directly.
 * 
 * @author Stefan Widmaier, FaktorZehn AG
 */
public class TextComboControl extends TextAndSecondControlComposite {

    /**
     * Creates a textfield and a combo. The given comboHeightHint is used to set the heightHint for
     * the layout-data for the combo.
     * 
     * @param parent The parent composite.
     * @param toolkit The UIToolkit to use for the creation of the controlls.
     * @param smallMargins <code>true</code> to get the smallest margin possible.
     * @param buttonHeightHint The preferred height or -1.
     */
    public TextComboControl(Composite parent, UIToolkit toolkit, boolean smallMargins, int buttonHeightHint) {
        super(parent, toolkit, smallMargins, buttonHeightHint, SWT.SINGLE);
    }

    /**
     * Creates a textfield and a combo. The height of the combo is not modified, margins are not
     * minimized.
     * 
     * @param parent The parent composite.
     * @param toolkit The UIToolkit to use for the creation of the controlls.
     */
    public TextComboControl(Composite parent, UIToolkit toolkit) {
        this(parent, toolkit, false, -1);
    }

    @Override
    protected Control createSecondControl(UIToolkit toolkit) {
        return toolkit.createCombo(this);
    }

    @Override
    protected Combo getSecondControl() {
        return (Combo)super.getSecondControl();
    }

    public Combo getComboControl() {
        return getSecondControl();
    }

    @Override
    public void addListener(int eventType, Listener listener) {
        super.addListener(eventType, listener);
        if (eventType != SWT.Paint && eventType != SWT.Dispose) {
            listenToControl(getTextControl(), eventType);
            listenToControl(getComboControl(), eventType);
        }
    }

    @Override
    protected void addListeners() {
        // do nothing
    }

}
