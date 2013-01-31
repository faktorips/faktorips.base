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

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.LocalizedString;
import org.faktorips.devtools.core.model.ILocalizedString;
import org.faktorips.devtools.core.ui.controls.TextAndSecondControlComposite;

public class MultilingualEditField extends DefaultEditField<ILocalizedString> {

    private TextAndSecondControlComposite control;

    protected boolean immediatelyNotifyListener = false;

    public MultilingualEditField(TextAndSecondControlComposite control) {
        super();
        this.control = control;
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
        if (text == null) {
            return null;
        }
        return new LocalizedString(IpsPlugin.getMultiLanguageSupport().getLocalizationLocale(), text);
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
        setText(newValue.getValue());
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
        ModifyListener ml = new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                boolean immediatelyNotify = immediatelyNotifyListener | control.isImmediatelyNotifyListener();
                notifyChangeListeners(new FieldValueChangedEvent(MultilingualEditField.this), immediatelyNotify);
            }

        };

        control.getTextControl().addModifyListener(ml);
        control.getTextControl().addDisposeListener(new MyDisposeListener(ml));
    }

    /**
     * Dispose listener to remove modify listener from control when control is disposed.
     * 
     * @author Thorsten Guenther
     */
    private class MyDisposeListener implements DisposeListener {

        /**
         * Listener which has to be removed on dispose.
         */
        private ModifyListener ml;

        /**
         * Create a new Listener.
         * 
         * @param ml The modify listener to remove on dispose
         */
        MyDisposeListener(ModifyListener ml) {
            this.ml = ml;
        }

        @Override
        public void widgetDisposed(DisposeEvent e) {
            control.getTextControl().removeModifyListener(ml);
        }

    }

}
