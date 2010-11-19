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

import java.util.Locale;

import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.ui.table.FormattingTextCellEditor;

/**
 * Base class for data type specific formats. Formats provide three things:
 * <ul>
 * <li>Formating a value in the model to a locale specific string (i.e. "1.2" will be displayed as
 * "1,2" in the german locale) to be displayed in the GUI</li>
 * <li>Parsing a locale specific string to a value object, that can be written back to the model</li>
 * <li>Verifying user input to support the user. No invalid characters may be entered in a
 * {@link FormattingTextField} or {@link FormattingTextCellEditor} (i.e. no letters in an integer
 * field).</li>
 * </ul>
 * <p>
 * Format supports the FIPS null-Presentation mechanism.
 * 
 * @author Stefan Widmaier
 */
public abstract class Format implements VerifyListener {

    public Format() {
        initFormat(IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormattingLocale());
        // only needed when the format-locale can be configured in the FIPS Preferences
        // IpsPlugin.getDefault().getIpsPreferences().addChangeListener(new
        // IPropertyChangeListener() {
        // @Override
        // public void propertyChange(PropertyChangeEvent event) {
        // if (event.getProperty().equals(IpsPreferences.DATATYPE_FORMATTING_LOCALE)) {
        // initFormat(IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormattingLocale());
        // }
        // }
        // });
    }

    /**
     * @param stringToBeParsed the String typed by the user that will be parsed to a value of the
     *            data type this format adheres to.
     * @param supportNull whether or not this method should return <code>null</code> if the string
     *            matches null-presentation value.
     * @return the value object that was parsed from the users input
     */
    public Object parse(String stringToBeParsed, boolean supportNull) {
        if (supportNull && IpsPlugin.getDefault().getIpsPreferences().getNullPresentation().equals(stringToBeParsed)) {
            return null;
        } else {
            return parseInternal(stringToBeParsed);
        }
    }

    /**
     * @param objectValue the value to be formatted for display
     * @param supportNull whether or not this method should return null-presentation value in case
     *            the given object value is <code>null</code>.
     * @return the formatted string representing the given value
     */
    public String format(Object objectValue, boolean supportNull) {
        if (objectValue == null && supportNull) {
            return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        } else {
            return formatInternal(objectValue);
        }
    }

    /**
     * Supports the FIPS null-Presentation mechanism. Semantically equivalent to parse(String
     * stringToBeparsed, true).
     * 
     * @param stringToBeparsed the String typed by the user that will be parsed to a value of the
     *            data type this format adheres to.
     * @return the value object that was parsed from the users input
     */
    public Object parse(String stringToBeparsed) {
        return parse(stringToBeparsed, true);
    }

    /**
     * 
     * Supports the FIPS null-Presentation mechanism. Semantically equivalent to format(Object
     * objectValue, true).
     * 
     * @param objectValue the value to be formatted for display
     * @return the formatted string representing the given value
     */
    public String format(Object objectValue) {
        return format(objectValue, true);
    }

    /**
     * Allows implementors of {@link #verifyInternal(VerifyEvent, String)} to prevent a text to be
     * written to the GUI. This method always allows the FIPS null-presentation to be typed into a
     * field. In this case {@link #verifyInternal(VerifyEvent, String)} will not be called in the
     * first place.
     */
    @Override
    public void verifyText(VerifyEvent e) {
        Text textControl = (Text)e.getSource();
        String insertedText = e.text;

        if (insertedText.length() > 0) {
            String resultingText = getResultingText(textControl, insertedText);
            if (isPotentialNullPresentation(resultingText)) {
                e.doit = true;
            } else {
                verifyInternal(e, resultingText);
            }
        }
    }

    private boolean isPotentialNullPresentation(String resultingText) {
        return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation().startsWith(resultingText);
    }

    private String getResultingText(Text text, String insertedText) {
        String currentText = text.getText();
        Point selection = text.getSelection();
        return currentText.substring(0, selection.x) + insertedText
                + currentText.substring(selection.y, currentText.length());
    }

    protected abstract Object parseInternal(String stringToBeparsed);

    protected abstract String formatInternal(Object value);

    protected abstract void verifyInternal(VerifyEvent e, String resultingText);

    /**
     * Is called when creating an instance of this class as well as every time the preference
     * {@link IpsPreferences#DATATYPE_FORMATTING_LOCALE} changes. {@link #initFormat(Locale)} is
     * always called with the currently configured locale.
     * <p>
     * Subclasses can create helper objects for Formating and parsing in this method.
     * 
     * @param locale the currently configured locale.
     */
    protected abstract void initFormat(Locale locale);

}
