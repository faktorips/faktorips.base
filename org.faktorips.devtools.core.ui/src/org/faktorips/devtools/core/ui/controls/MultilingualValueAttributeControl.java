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

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.value.IValue;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.dialogs.MultilingualValueDialog;

/**
 * {@link TextButtonControl} for international value attributes. The text control can be edited to
 * change the current language. The button opens the {@link MultilingualValueDialog} to allow users
 * to change the attribute's list of internationalized values.
 */
public class MultilingualValueAttributeControl extends TextButtonControl {

    private MultilingualValueAttributeHandler handler;

    public MultilingualValueAttributeControl(Composite parent, UIToolkit toolkit, IAttributeValue attributeValue,
            IIpsProject ipsProject, IValueHolder<IValue<?>> valueHolder) {
        super(parent, toolkit, IpsPlugin.getDefault().getUsedLanguagePackLocale().getLanguage());
        handler = new MultilingualValueAttributeHandler(parent.getShell(), attributeValue, ipsProject, valueHolder);
        getTextControl().setEditable(true);
    }

    @Override
    protected void buttonClicked() {
        handler.editValues();
    }
}
