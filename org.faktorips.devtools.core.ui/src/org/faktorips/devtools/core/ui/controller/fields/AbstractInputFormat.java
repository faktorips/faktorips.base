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

import java.text.Format;
import java.text.ParsePosition;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.ui.table.FormattingTextCellEditor;

/**
 * Base class for data type specific formats. {@link AbstractInputFormat}s provide three things:
 * <ul>
 * <li>Formating a value in the model to a locale specific string that will be displayed in the GUI
 * (i.e. "1.2" will be displayed as "1,2" for the german locale)</li>
 * <li>Parsing a locale specific string to a value object, that can be written back to the model</li>
 * <li>Verifying user input to avoid mistakes. No invalid characters may be entered in a
 * {@link FormattingTextField} or {@link FormattingTextCellEditor} (i.e. no letters in an integer
 * field).</li>
 * </ul>
 * <p/>
 * {@link AbstractInputFormat} supports the FIPS null-Presentation mechanism.
 * <p/>
 * Instances of this class reconfigure themselves if the IpsPreference
 * IpsPreferences.DATATYPE_FORMATTING_LOCALE changes. Subclasses need to implement
 * {@link #initFormat(Locale)} for this.
 * <p>
 * The generic type T represents type that is written to the model and NOT the data type of this
 * format. For most of our types this is String because we write every value as String to be type
 * independent. However e.g. to correctly show formatted {@link GregorianCalendar} in the dialogs
 * that need user input not written down to the model, we could need other types here. The type of
 * the format is configured by subtypes.
 * 
 * @author Stefan Widmaier
 */
public abstract class AbstractInputFormat<T> implements VerifyListener {

    /**
     * Calls the initFormat with the input locale configured in the preferences.
     */
    public void initFormat() {
        initFormat(IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormattingLocale());
    }

    /**
     * Parses a string to a value object. Supports the FIPS null-Presentation mechanism.
     * 
     * @param stringToBeParsed the String typed by the user that will be parsed to a value of the
     *            data type this format adheres to.
     * @param supportNull whether or not this method should return <code>null</code> if the string
     *            matches the null-presentation value.
     * @return the value object that was parsed from the users input
     */
    public T parse(String stringToBeParsed, boolean supportNull) {
        if (supportNull && IpsPlugin.getDefault().getIpsPreferences().getNullPresentation().equals(stringToBeParsed)) {
            return null;
        } else {
            return parseInternal(stringToBeParsed);
        }
    }

    /**
     * Returns a formatted string for the given value object. Supports the FIPS null-Presentation
     * mechanism.
     * 
     * @param objectValue the value to be formatted for display
     * @param supportNull whether or not this method should return null-presentation value in case
     *            the given object value is <code>null</code>.
     * @return the formatted string representing the given value
     */
    public String format(T objectValue, boolean supportNull) {
        if (objectValue == null) {
            return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        } else {
            try {
                String formatedValue = formatInternal(objectValue);
                if (formatedValue == null) {
                    return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
                } else {
                    return formatedValue;
                }
            } catch (Exception e) {
                return objectValue.toString();
            }
        }
    }

    /**
     * Parses a string to a value object. Semantically equivalent to parse(String stringToBeparsed,
     * true).
     * 
     * @param stringToBeparsed the String typed by the user that will be parsed to a value of the
     *            data type this format adheres to.
     * @return the value object that was parsed from the users input
     */
    public T parse(String stringToBeparsed) {
        return parse(stringToBeparsed, true);
    }

    /**
     * 
     * Returns a formatted string for the given value object. Semantically equivalent to
     * format(Object objectValue, true).
     * 
     * @param objectValue the value to be formatted for display
     * @return the formatted string representing the given value
     */
    public String format(T objectValue) {
        return format(objectValue, true);
    }

    /**
     * Allows implementors of {@link #verifyInternal(VerifyEvent, String)} to prevent a text to be
     * entered into a edit field.
     * <p>
     * This method always allows the FIPS null-presentation to be entered. In that case
     * {@link #verifyInternal(VerifyEvent, String)} will not be called in the first place.
     */
    @Override
    public void verifyText(VerifyEvent e) {
        Text textControl = (Text)e.getSource();
        String insertedText = e.text;

        if (insertedText.length() > 0) {
            /*
             * Always use the VerifyEvent's start and end indices instead of the text control's
             * selection. During a replace operation (via setText()) the events start and end values
             * may differ from the selection's. Using the selection caused problems in GTK (calls of
             * setText() were ignored).
             */
            String resultingText = getResultingText(textControl.getText(), e);
            if (isPotentialNullPresentation(resultingText)) {
                e.doit = true;
            } else {
                verifyInternal(e, resultingText);
            }
        } else {
            // ignore non-text inputs, e.g. Enter or Backspace keys
        }
    }

    private boolean isPotentialNullPresentation(String resultingText) {
        return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation().startsWith(resultingText);
    }

    private String getResultingText(String currentText, VerifyEvent e) {
        return currentText.substring(0, e.start) + e.text + currentText.substring(e.end, currentText.length());
    }

    /**
     * Parses a string to a value object. Returns <code>null</code> if the given String is not
     * parsable. The String is given like displayed and should be parsed to the object that is
     * stored in the model. e.g for an attribute of type money we have to parse the value displayed
     * in the editor, convert to money object and return the String representation to store in the
     * model.
     * <p>
     * Implementors must return a value object of the class that is expected by the model.
     * 
     * @param stringToBeparsed the string that should be parsed to a value.
     * @return the parsed value or <code>null</code> if the given String is not parsable.
     */
    protected abstract T parseInternal(String stringToBeparsed);

    /**
     * Formats a value object to a displayable string. The value is of type T, e.g. for attributes
     * in the model type T is String. Depending on this formatter, the String amy be converted to an
     * other datatype, e.g. Money and will be formatted to display with the current input locale.
     * 
     * @param value the value to be represented as string in this format. value can be
     *            <code>null</code>.
     * @return the formatted string representing the given value
     */
    protected abstract String formatInternal(T value);

    /**
     * Allows subclasses to prevent the user from entering invalid characters into an edit field.
     * <p>
     * Note though that {@link #verifyInternal(VerifyEvent, String)} cannot force a field's current
     * text to be valid input string all the time. Prefixes of valid strings must be permitted if
     * they are invalid on their own, or else the user could never enter a valid string; e.g. "1.2."
     * for a date or "-1," for a float.
     * 
     * @param e the {@link VerifyEvent}. set doit=false if the resulting text is invalid.
     * @param resultingText the text, that an edit field will contain if e.doit=true. Implementors
     *            should test whether this string is a valid input for this format.
     */
    protected abstract void verifyInternal(VerifyEvent e, String resultingText);

    /**
     * Is called when creating an instance of this class as well as every time the preference
     * {@link IpsPreferences#DATATYPE_FORMATTING_LOCALE} changes. {@link #initFormat(Locale)} is
     * always called with the currently configured locale.
     * <p>
     * Subclasses can create helper objects for formating and parsing in this method.
     * 
     * @param locale the currently configured locale.
     */
    protected abstract void initFormat(Locale locale);

    /**
     * Returns <code>true</code> if the entire String could be parsed to a value using the given
     * {@link Format}, else <code>false</code> .
     * 
     * @param resultingText the string to be parsed
     * @return <code>true</code> if the entire String could be parsed to a value, <code>false</code>
     *         else.
     */
    protected boolean isParsable(Format format, String resultingText) {
        ParsePosition position = new ParsePosition(0);
        format.parseObject(resultingText, position);
        return position.getIndex() == resultingText.length();
    }

    /**
     * Utility Method to test a given input string for allowed characters.
     * 
     * @param template an example string that contains all allowed non digit characters.
     * @param stringToBeVerified the string whose (non digit) characters should be verified
     * @return <code>true</code> if stringToBeVerified contains only allowed characters.
     *         <code>false</code> else.
     */
    protected boolean containsAllowedCharactersOnly(String template, String stringToBeVerified) {
        stringToBeVerified = getNonDigitString(stringToBeVerified);
        for (int i = 0; i < stringToBeVerified.length(); i++) {
            if (!template.contains(stringToBeVerified.substring(i, i + 1))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes all non digit characters from a string.
     * 
     * @param string the string to read the non digit characters from
     * @return a new filtered string containing only the input string's non digit characters.
     */
    protected String getNonDigitString(String string) {
        return string.replaceAll("[0-9]", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
