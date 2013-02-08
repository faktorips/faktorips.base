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
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.dialogs.MultilingualValueDialog;

/**
 * {@link TextButtonControl} for international value attributes. The text control can be edited to
 * change the current language. The button opens the {@link MultilingualValueDialog} to allow users
 * to change the attribute's list of internationalized values.
 */
public class MultilingualValueAttributeControl extends TextButtonControl {

    private MultilingualValueAttributeHandler handler;

    public MultilingualValueAttributeControl(Composite parent, UIToolkit toolkit,
            ISingleValueHolderProvider valueHolderProvider) {
        super(parent, toolkit, IpsPlugin.getMultiLanguageSupport().getLocalizationLocale().getLanguage());
        handler = new MultilingualValueAttributeHandler(parent.getShell(), valueHolderProvider);
        getTextControl().setEditable(true);
    }

    @Override
    protected void buttonClicked() {
        handler.editValues();
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
