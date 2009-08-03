/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.tableconversion.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.TableFormatConfigurationCompositeFactory;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.devtools.tableconversion.csv.CSVTableFormat;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Composite for configuring CSV table format options like field delimiters, date formats and the
 * like.
 * 
 * @author Roman Grutza
 */
public class CSVPropertyCompositeFactory extends TableFormatConfigurationCompositeFactory {

    private ITableFormat tableFormat;

    private Text fieldDelimiterText;
    private Text dateFormatText;

    public void setTableFormat(ITableFormat tableFormat) {
        this.tableFormat = tableFormat;
    }

    public Composite createPropertyComposite(Composite parent, UIToolkit toolkit) {
        Composite root = toolkit.createLabelEditColumnComposite(parent);

        // First row: field delimiter controls.
        toolkit.createLabel(root, Messages.CSVPropertyFactory_fieldDelimiterLabel);
        fieldDelimiterText = toolkit.createText(root);
        TextField fieldDelimiterTextField = new TextField(fieldDelimiterText);
        fieldDelimiterTextField.addChangeListener(this);

        // Second row: date format controls.
        toolkit.createLabel(root, Messages.CSVPropertyFactory_dateFormatLabel);
        dateFormatText = toolkit.createText(root);
        TextField dateFormatTextField = new TextField(dateFormatText);
        dateFormatTextField.addChangeListener(this);

        // Help area for date format syntax.
        toolkit.createLabel(root, ""); //$NON-NLS-1$
        Text helpText = toolkit.createText(root, SWT.MULTI | SWT.READ_ONLY);
        helpText.setText(Messages.CSVPropertyFactory_dateFormatHelp1);
        helpText.setBackground(root.getBackground());

        initializeProperties();

        return root;
    }

    private void initializeProperties() {
        String fieldDelimiter = tableFormat.getProperty(CSVTableFormat.PROPERTY_FIELD_DELIMITER);
        if (fieldDelimiter == null) {
            fieldDelimiter = ","; //$NON-NLS-1$
        }
        fieldDelimiterText.setText(fieldDelimiter);

        String dateFormat = tableFormat.getProperty(CSVTableFormat.PROPERTY_DATE_FORMAT);
        if (dateFormat == null) {
            dateFormat = "yyyy-MM-dd"; //$NON-NLS-1$
        }
        dateFormatText.setText(dateFormat);
    }

    public MessageList validate() {
        MessageList ml = new MessageList();

        validateFieldDelimiter(ml);
        validateDateFormat(ml);

        return ml;
    }

    private void validateDateFormat(MessageList ml) {
        String text = dateFormatText.getText();
        if (text.length() < 1) {
            ml.add(new Message("", Messages.CSVPropertyFactory_errMsgEmptyDateFormat, Message.ERROR)); //$NON-NLS-1$
        }
    }

    private void validateFieldDelimiter(MessageList ml) {
        String text = fieldDelimiterText.getText();
        if (text.length() != 1) {
            ml.add(new Message("", Messages.CSVPropertyFactory_errMsgFieldDelimiterLength, Message.ERROR)); //$NON-NLS-1$
        }
    }

    @Override
    public void valueChanged(FieldValueChangedEvent e) {
        super.valueChanged(e);

        if (tableFormat == null || validate().containsErrorMsg()) {
            return;
        }

        if (e.field.getControl() == dateFormatText) {
            tableFormat.setProperty(CSVTableFormat.PROPERTY_DATE_FORMAT, dateFormatText.getText());
        } else if (e.field.getControl() == fieldDelimiterText) {
            tableFormat.setProperty(CSVTableFormat.PROPERTY_FIELD_DELIMITER, fieldDelimiterText.getText());
        }
    }

}
