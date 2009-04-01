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

package org.faktorips.devtools.core.ui.wizards.tableimport;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.CompositeFactory;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.devtools.tableconversion.csv.CSVTableFormat;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Composite for configuring CSV table format options like field delimiters, date formats and the like.
 * 
 * @author Roman Grutza
 */
public class CSVPropertyComposite extends CompositeFactory {

    private ITableFormat tableFormat;
    
    private Text fieldDelimiterText;
    private Text dateFormatText;


    public void setTableFormat(ITableFormat tableFormat) {
        this.tableFormat = tableFormat;
    }

    public Composite createPropertyComposite(Composite parent, UIToolkit toolkit) {
        Composite root = toolkit.createLabelEditColumnComposite(parent);

        // first row: field delimiter controls
        toolkit.createLabel(root, "Field delimiter");
        fieldDelimiterText = toolkit.createText(root);
        TextField fieldDelimiterTextField = new TextField(fieldDelimiterText);
        fieldDelimiterTextField.addChangeListener(this);
        
        // second row: date format controls
        toolkit.createLabel(root, "Date format");
        dateFormatText = toolkit.createText(root);
        TextField dateFormatTextField = new TextField(dateFormatText);
        dateFormatTextField.addChangeListener(this);
        
        initializeProperties();
        
        return root;
    }

    private void initializeProperties() {
        String fieldDelimiter = tableFormat.getProperty(CSVTableFormat.PROPERTY_FIELD_DELIMITER);
        if (fieldDelimiter == null) {
            fieldDelimiter = ",";
        }
        fieldDelimiterText.setText(fieldDelimiter);
        
        String dateFormat = tableFormat.getProperty(CSVTableFormat.PROPERTY_DATE_FORMAT);
        if (dateFormat == null) {
            dateFormat = "DD.MM.YYYY";
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
            ml.add(new Message("", "Date format must not be empty.", Message.ERROR));
        }
    }

    private void validateFieldDelimiter(MessageList ml) {
        String text = fieldDelimiterText.getText();
        if (text.length() != 1) {
            ml.add(new Message("", "Field delimiter must be a single character.", Message.ERROR));
        }
    }
    
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
