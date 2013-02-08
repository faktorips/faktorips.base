/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controller.fields;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.LocalizedString;
import org.faktorips.devtools.core.model.ILocalizedString;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controls.TextAndSecondControlComposite;

/**
 * {@link EditField} for editing multi lingual strings. The text edited by the text control will
 * always return an {@link ILocalizedString} in the locale given to this class.
 */
public class MultilingualEditField extends DefaultEditField<ILocalizedString> {
    private TextAndSecondControlComposite control;
    private final Locale localeOfEditField;

    protected boolean immediatelyNotifyListener = false;

    public MultilingualEditField(TextAndSecondControlComposite control, Locale localeOfEditField) {
        super();
        this.control = control;
        this.localeOfEditField = localeOfEditField;
    }

    @Override
    public Control getControl() {
        return control;
    }

    public TextAndSecondControlComposite getTextButtonControl() {
        return control;
    }

    @Override
    protected Control getControlForDecoration() {
        return control.getTextControl();
    }

    @Override
    public ILocalizedString parseContent() {
        String text = StringValueEditField.prepareObjectForGet(control.getTextControl().getText(),
                supportsNullStringRepresentation());
        return new LocalizedString(localeOfEditField, text);
    }

    @Override
    public void setValue(ILocalizedString newValue) {
        if (newValue == null) {
            if (supportsNullStringRepresentation()) {
                setText(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
                return;
            } else {
                setText(StringUtils.EMPTY);
                return;
            }
        }
        setText(StringValueEditField.prepareObjectForSet(newValue.getValue(), supportsNullStringRepresentation()));
    }

    @Override
    public String getText() {
        return control.getTextControl().getText();
    }

    @Override
    public void setText(String newText) {
        immediatelyNotifyListener = true;
        try {
            control.getTextControl().setText(newText);
        } finally {
            immediatelyNotifyListener = false;
        }
    }

    @Override
    public void insertText(String text) {
        control.getTextControl().insert(text);
    }

    @Override
    public void selectAll() {
        control.getTextControl().selectAll();
    }

    @Override
    protected void addListenerToControl() {
        final ModifyListener ml = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                boolean immediatelyNotify = immediatelyNotifyListener | control.isImmediatelyNotifyListener();
                notifyChangeListeners(new FieldValueChangedEvent(MultilingualEditField.this), immediatelyNotify);
            }

        };

        control.getTextControl().addModifyListener(ml);
        control.getTextControl().addDisposeListener(new DisposeListener() {
            /**
             * Dispose listener to remove modify listener from control when control is disposed.
             * 
             * @author Thorsten Guenther
             */
            @Override
            public void widgetDisposed(DisposeEvent arg0) {
                control.getTextControl().removeModifyListener(ml);
            }
        });
    }
}
