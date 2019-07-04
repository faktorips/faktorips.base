/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.ui;

import java.text.SimpleDateFormat;

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
    private Text decimalSeparatorText;

    // TODO rg: commented out for 2.4rc1
    // private Text decimalGroupingText;

    @Override
    public void setTableFormat(ITableFormat tableFormat) {
        this.tableFormat = tableFormat;
    }

    @Override
    public Composite createPropertyComposite(Composite parent, UIToolkit toolkit) {
        Composite root = toolkit.createLabelEditColumnComposite(parent);

        // First row: field delimiter controls.
        toolkit.createLabel(root, Messages.CSVPropertyCompositeFactory_fieldDelimiterLabel);
        fieldDelimiterText = toolkit.createText(root);
        TextField fieldDelimiterTextField = new TextField(fieldDelimiterText);
        fieldDelimiterTextField.addChangeListener(this);

        // Second row: date format controls.
        toolkit.createLabel(root, Messages.CSVPropertyCompositeFactory_dateFormatLabel);
        dateFormatText = toolkit.createText(root);
        TextField dateFormatTextField = new TextField(dateFormatText);
        dateFormatTextField.addChangeListener(this);

        // Third row: Help label for date format syntax.
        toolkit.createLabel(root, ""); //$NON-NLS-1$
        Text helpText = toolkit.createText(root, SWT.MULTI | SWT.READ_ONLY);
        helpText.setEnabled(false);
        helpText.setText(Messages.CSVPropertyCompositeFactory_dateFormatHelp1);
        helpText.setBackground(root.getBackground());

        // Fourth row: Decimal separator character control
        toolkit.createLabel(root, Messages.CSVPropertyCompositeFactory_labelDecimalSeparator);
        decimalSeparatorText = toolkit.createText(root);
        TextField decimalSeparatorTextField = new TextField(decimalSeparatorText);
        decimalSeparatorTextField.addChangeListener(this);
        decimalSeparatorText.setText("."); //$NON-NLS-1$

        // TODO rg: commented out for 2.4rc1, needs adaptation of IValueConverter's
        // Fifth row: Decimal grouping character control
        // toolkit.createLabel(root, Messages.CSVPropertyCompositeFactory_labelDecimalGrouping);
        // decimalGroupingText = toolkit.createText(root);
        // TextField groupingSeparatorTextField = new TextField(decimalGroupingText);
        // groupingSeparatorTextField.addChangeListener(this);
        //        decimalGroupingText.setText(","); //$NON-NLS-1$

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

        String decimalSeparator = tableFormat.getProperty(CSVTableFormat.PROPERTY_DECIMAL_SEPARATOR_CHAR);
        if (decimalSeparator == null) {
            decimalSeparator = "."; //$NON-NLS-1$
        }
        decimalSeparatorText.setText(decimalSeparator);

        String decimalGrouping = tableFormat.getProperty(CSVTableFormat.PROPERTY_DECIMAL_GROUPING_CHAR);
        if (decimalGrouping == null) {
            decimalGrouping = ","; //$NON-NLS-1$
        }
        // decimalGroupingText.setText(decimalGrouping);
    }

    @Override
    public MessageList validate() {
        MessageList ml = new MessageList();

        validateFieldDelimiter(ml);
        validateDateFormat(ml);
        validateDecimalSeparator(ml);

        return ml;
    }

    private void validateDateFormat(MessageList ml) {
        try {
            String dateFormat = dateFormatText.getText();
            if ("".equals(dateFormat)) { //$NON-NLS-1$
                dateFormat = null;
            }
            // Construction of a new SimpleDateFormat instance fails if the pattern is invalid
            new SimpleDateFormat(dateFormat);
        } catch (Exception e) {
            ml.add(new Message("", Messages.CSVPropertyCompositeFactory_errMsgInvalidDateFormat, Message.ERROR)); //$NON-NLS-1$
        }
    }

    private void validateDecimalSeparator(MessageList ml) {
        String decimalSeparator = decimalSeparatorText.getText();
        if (decimalSeparator.length() != 1) {
            ml.add(new Message("", Messages.CSVPropertyCompositeFactory_errMsgDecimalSeparatorLength, //$NON-NLS-1$
                    Message.ERROR));
        }
    }

    private void validateFieldDelimiter(MessageList ml) {
        String text = fieldDelimiterText.getText();
        if (text.length() != 1) {
            ml.add(new Message("", Messages.CSVPropertyCompositeFactory_errMsgFieldDelimiterLength, Message.ERROR)); //$NON-NLS-1$
        }
    }

    @Override
    public void valueChanged(FieldValueChangedEvent e) {
        if (tableFormat == null || validate().containsErrorMsg()) {
            super.valueChanged(e);
            return;
        }

        if (e.field.getControl() == dateFormatText) {
            tableFormat.setProperty(CSVTableFormat.PROPERTY_DATE_FORMAT, dateFormatText.getText());
        } else if (e.field.getControl() == fieldDelimiterText) {
            tableFormat.setProperty(CSVTableFormat.PROPERTY_FIELD_DELIMITER, fieldDelimiterText.getText());
        } else if (e.field.getControl() == decimalSeparatorText) {
            tableFormat.setProperty(CSVTableFormat.PROPERTY_DECIMAL_SEPARATOR_CHAR, decimalSeparatorText.getText());

            // TODO rg: commented out for 2.4rc1, needs adaptation of IValueConverter's
            // } else if (e.field.getControl() == decimalGroupingText) {
            // tableFormat.setProperty(CSVTableFormat.PROPERTY_DECIMAL_GROUPING_CHAR,
            // decimalGroupingText.getText());
        }

        super.valueChanged(e);
    }

}
