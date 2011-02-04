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

package org.faktorips.devtools.core.ui.controller.fields;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controls.TextComboControl;
import org.faktorips.util.message.MessageList;

/**
 * This class is a {@link EditField} for money values. The combo control is populated with the
 * currency objects returned by IpsUIPlugin#getCurrencies(). The text control is managed by a
 * FormattedTextField with decimal format.
 * <p/>
 * This field in essence is a "composite" field. It uses a field for each the text and the combo
 * control and forwards events sent by them to its own listeners.
 * 
 * @author Stefan Widmaier, FaktorZehn AG
 */
public class MoneyField extends DefaultEditField {

    private TextComboControl control;
    private ComboField currencyField;
    private FormattingTextField valueField;

    protected boolean immediatelyNotifyListener = false;

    public MoneyField(TextComboControl control) {
        super();
        this.control = control;
        valueField = new FormattingTextField(control.getTextControl(), new DoubleFormat());
        currencyField = new ComboField(control.getComboControl());

        List<Currency> currencies = IpsUIPlugin.getDefault().getCurrencies();
        initComboWithCurrencies(currencies);
    }

    private void initComboWithCurrencies(List<Currency> currencies) {
        List<String> currencyNames = new ArrayList<String>();
        for (Currency currency : currencies) {
            currencyNames.add(currency.getCurrencyCode());
        }
        control.getComboControl().setItems(currencyNames.toArray(new String[currencyNames.size()]));
    }

    @Override
    public Control getControl() {
        return control;
    }

    public TextComboControl getTextComboControl() {
        return control;
    }

    @Override
    public void setMessages(MessageList list) {
        MessageCueController.setMessageCue(control.getTextControl(), list);
        MessageCueController.setMessageCue(control.getComboControl(), list);
    }

    @Override
    public Object parseContent() {
        return prepareObjectForGet(getText());
    }

    @Override
    public void setValue(Object newValue) {
        setText((String)super.prepareObjectForSet(newValue));
    }

    /**
     * {@inheritDoc} Returns (<value>+" "+<currency>).
     */
    @Override
    public String getText() {
        return valueField.getText() + " " + currencyField.getText(); //$NON-NLS-1$        
    }

    /**
     * Returns <code>null</code> if the text control contains the null-presentation string.
     * Otherwise the fields value (<value>+" "+<currency>) is returned.
     */
    @Override
    public Object prepareObjectForGet(Object value) {
        if (super.prepareObjectForGet(valueField.getText()) == null) {
            return null;
        } else {
            return getText();
        }
    }

    @Override
    public void setText(String newText) {
        immediatelyNotifyListener = true;
        try {
            setTextInternal(newText);
        } finally {
            immediatelyNotifyListener = false;
        }
    }

    private void setTextInternal(String newText) {
        if (newText == null) {
            valueField.setText(""); //$NON-NLS-1$
        } else {
            String[] values = newText.split(" "); //$NON-NLS-1$
            if (values.length == 2) {
                valueField.setText(values[0]);
                currencyField.setText(values[1]);
            } else {
                valueField.setText(""); //$NON-NLS-1$
                currencyField.getCombo().select(0);
            }
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
        FieldValueChangeEventForwarder forwarder = new FieldValueChangeEventForwarder();
        valueField.addChangeListener(forwarder);
        currencyField.addChangeListener(forwarder);
    }

    public class FieldValueChangeEventForwarder implements ValueChangeListener {
        @Override
        public void valueChanged(FieldValueChangedEvent e) {
            /*
             * Always send events immediately as this is only a forwarding mechanism. Events are
             * delayed as usual in the called notifyChangeListeners() method.
             */
            MoneyField.this.notifyChangeListeners(new FieldValueChangedEvent(MoneyField.this), true);
        }

    }

}
