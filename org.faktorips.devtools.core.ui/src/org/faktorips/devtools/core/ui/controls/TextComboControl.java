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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
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
public class TextComboControl extends ControlComposite {

    /** text and combo controls */
    protected Text text;

    private Combo combo;

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
        }
        text.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL));
        combo = toolkit.createCombo(this);
        GridData d = new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_VERTICAL);
        if (buttonHeightHint > -1) {
            d.heightHint = buttonHeightHint;
            d.widthHint = 35;
        }
        combo.setLayoutData(d);
        // must be called after text and combo controls were created
        toolkit.getFormToolkit().adapt(this);
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
    public void setEnabled(boolean value) {
        text.setEnabled(value);
        combo.setEnabled(value);
    }

    public void setText(String newText) {
        text.setText(newText);
    }

    public String getText() {
        return text.getText();
    }

    public Text getTextControl() {
        return text;
    }

    public Combo getComboControl() {
        return combo;
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
            listenToControl(combo, eventType);
        }
    }

}
