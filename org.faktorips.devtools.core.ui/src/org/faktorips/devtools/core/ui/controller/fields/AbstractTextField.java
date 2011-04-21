/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.util.ArgumentCheck;

/**
 * Abstract Field that handles a {@link Text} component. The value type must not be {@link String}
 * 
 * @see EditField for details about generic type T
 * 
 * @author dirmeier
 */
public abstract class AbstractTextField<T> extends DefaultEditField<T> {

    protected Text text;
    private boolean immediatelyNotifyListener = false;

    public AbstractTextField() {
        super();
    }

    public AbstractTextField(Text text) {
        this();
        ArgumentCheck.notNull(text);
        this.text = text;
    }

    @Override
    public Control getControl() {
        return text;
    }

    /**
     * Returns the text control this is an edit field for.
     */
    public Text getTextControl() {
        return text;
    }

    @Override
    public String getText() {
        return text.getText();
    }

    @Override
    public void setText(String newText) {
        immediatelyNotifyListener = true;
        try {
            if (newText == null) {
                // AbstractNumberFormats call this method with null values
                if (supportsNull()) {
                    newText = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
                } else {
                    newText = ""; //$NON-NLS-1$
                }
            }
            text.setText(newText);
        } finally {
            immediatelyNotifyListener = false;
        }
    }

    @Override
    public void insertText(String insertText) {
        text.insert(insertText);
    }

    @Override
    public void selectAll() {
        text.selectAll();
    }

    @Override
    protected void addListenerToControl() {
        text.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                notifyChangeListeners(new FieldValueChangedEvent(AbstractTextField.this), immediatelyNotifyListener);
            }
        });
    }

}