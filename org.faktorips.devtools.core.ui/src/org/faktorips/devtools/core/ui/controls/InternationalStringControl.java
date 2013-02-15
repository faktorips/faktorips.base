/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.dialogs.InternationalStringDialog;

/**
 * {@link TextButtonControl} for international value attributes. The text control can be edited to
 * change the current language. The button opens the {@link InternationalStringDialog} to allow
 * users to change the attribute's list of internationalized values.
 */
public class InternationalStringControl extends TextButtonControl {
    /**
     * This constant is used to make a text control have the same height as a button. Unfortunately,
     * setting the same size for both controls results in a text field, which is 7 pixel larger than
     * the button. It is not clear, why exactly this happens, but the size difference is the same on
     * Linux and Windows and also independent of the used font size.
     */
    private final int SIZE_OF_TEXTFIELD_PADDING = 7;

    private final InternationalStringDialogHandler handler;

    public InternationalStringControl(Composite parent, UIToolkit toolkit, InternationalStringDialogHandler handler) {
        super(parent, toolkit, IpsPlugin.getMultiLanguageSupport().getLocalizationLocale().getLanguage());
        this.handler = handler;
        getTextControl().setEditable(true);
    }

    /**
     * Sets the height hint for the text control. Thereby, it is possible to put the control into a
     * cell editor and not needing to resize the table rows.
     * 
     * @param heightHint the height hint to set.
     */
    public void setHeightHint(int heightHint) {
        removeMargins();
        GridData gridDataButton = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        gridDataButton.heightHint = heightHint;
        getButtonControl().setLayoutData(gridDataButton);
        GridData gridDataText = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        gridDataText.heightHint = heightHint - SIZE_OF_TEXTFIELD_PADDING;
        getTextControl().setLayoutData(gridDataText);
    }

    @Override
    public Button getButtonControl() {
        return getSecondControl();
    }

    @Override
    protected void buttonClicked() {
        handler.run();
    }

    /**
     * Registers a mouse listener on the button control. On a linux system, the
     * <code>widgetSelected</code> event, which is bound to the control in the super class is fired
     * too late. Hence, we add the <code>mouseDown</code> event here, in order to be notified as
     * soon as the button is clicked. It is only afterwards that the <code>focus lost</code> event
     * is sent to the cell editor.
     */
    @Override
    protected void addListeners() {
        getButtonControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent event) {
                buttonClicked();
            }
        });
    }
}
