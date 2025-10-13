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

import java.util.Locale;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.dialogs.InternationalStringDialog;
import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.values.LocalizedString;

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
    private static final int SIZE_OF_TEXTFIELD_PADDING = 7;

    private final InternationalStringDialogHandler handler;

    public InternationalStringControl(Composite parent, UIToolkit toolkit, InternationalStringDialogHandler handler) {
        super(parent, toolkit, IIpsModel.get().getMultiLanguageSupport()
                .getLocalizationLocaleOrDefault(handler.getIpsProject()).getLanguage());
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
        if (!getTextControl().isDisposed()) {
            String currentText = getTextControl().getText();
            IInternationalString intString = handler.getInternationalString();
            if (intString != null) {
                String language = IIpsModel.get().getMultiLanguageSupport()
                        .getLocalizationLocaleOrDefault(handler.getIpsProject()).getLanguage();
                intString.add(new LocalizedString(Locale.of(language), currentText));
            }
        }

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
