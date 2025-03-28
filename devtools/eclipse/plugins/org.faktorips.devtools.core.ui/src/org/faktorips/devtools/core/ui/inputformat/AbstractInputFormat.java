/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.inputformat;

import java.text.Format;
import java.text.ParsePosition;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.ui.controller.fields.FormattingTextField;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.ArgumentCheck;

/**
 * Base class for data type specific formats. {@link AbstractInputFormat}s provide three things:
 * <ul>
 * <li>Formating a value in the model to a locale specific string that will be displayed in the GUI
 * (i.e. "1.2" will be displayed as "1,2" for the german locale)</li>
 * <li>Parsing a locale specific string to a value object, that can be written back to the
 * model</li>
 * <li>Verifying user input to avoid mistakes. No invalid characters may be entered in a
 * {@link FormattingTextField} (i.e. no letters in an integer field).</li>
 * </ul>
 * <p>
 * {@link AbstractInputFormat} supports the FIPS null-Presentation mechanism.
 * <p>
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
public abstract class AbstractInputFormat<T> implements VerifyListener, IInputFormat<T> {

    private final Locale datatypeLocale;

    private String nullStringRepresentation;

    public AbstractInputFormat(String defaultNullString, Locale datatypeLocale) {
        ArgumentCheck.notNull(datatypeLocale);
        ArgumentCheck.notNull(defaultNullString);
        nullStringRepresentation = defaultNullString;
        this.datatypeLocale = datatypeLocale;
    }

    @Override
    public void initFormat() {
        initFormat(datatypeLocale);
    }

    @Override
    public String getNullString() {
        return nullStringRepresentation;
    }

    @Override
    public void setNullString(String nullString) {
        nullStringRepresentation = nullString;
    }

    @Override
    public T parse(String stringToBeParsed, boolean supportNull) {
        if (supportNull && isRepresentingNull(stringToBeParsed)) {
            return null;
        } else {
            return parseInternal(stringToBeParsed);
        }
    }

    /**
     * Assumes empty string always represents the <code>null</code> value. This is true for all
     * number and date datatypes. Override this method if another behavior is required.
     */
    protected boolean isRepresentingNull(String stringToBeParsed) {
        return getNullString().equals(stringToBeParsed) || IpsStringUtils.EMPTY.equals(stringToBeParsed)
                || isPreferencesNullPresentation(stringToBeParsed);
    }

    /**
     * Checks if the parameter equals the NullPresentation of the {@link IpsPreferences}
     */
    protected boolean isPreferencesNullPresentation(String stringToBeParsed) {
        return IpsStringUtils.trimEquals(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation(),
                stringToBeParsed);
    }

    @Override
    public String format(T objectValue, boolean supportNull) {
        if (objectValue == null) {
            return nullStringRepresentation;
        } else {
            try {
                String formatedValue = formatInternal(objectValue);
                if (formatedValue == null) {
                    return nullStringRepresentation;
                } else {
                    return formatedValue;
                }
                // CSOFF: IllegalCatch
            } catch (Exception e) {
                return objectValue.toString();
            }
            // CSON: IllegalCatch
        }
    }

    @Override
    public T parse(String stringToBeparsed) {
        return parse(stringToBeparsed, true);
    }

    @Override
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
        String insertedText = e.text;

        if (insertedText.length() > 0) {
            String resultingText = getResultingText(e);
            if (isPotentialNullPresentation(resultingText)) {
                e.doit = true;
            } else {
                verifyInternal(e, resultingText);
            }
        }
        // ignore non-text inputs, e.g. Enter or Backspace keys
    }

    private boolean isPotentialNullPresentation(String resultingText) {
        return nullStringRepresentation.startsWith(resultingText);
    }

    /**
     * Returns the resulting text from the control that is currently changed.
     * <p>
     * Always use the VerifyEvent's start and end indices instead of the text control's selection.
     * During a replace operation (via setText()) the events start and end values may differ from
     * the selection's. Using the selection caused problems in GTK (calls of setText() were
     * ignored).
     */
    private String getResultingText(VerifyEvent e) {
        String currentText = getTextFromControl(e);
        return currentText.substring(0, e.start) + e.text + currentText.substring(e.end, currentText.length());
    }

    private String getTextFromControl(VerifyEvent e) {
        return switch (e.getSource()) {
            case Text text -> text.getText();
            case Combo combo -> combo.getText();
            default -> IpsStringUtils.EMPTY;
        };
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
     *             else.
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
     * @param inputString the string whose (non digit) characters should be verified
     * @return <code>true</code> if stringToBeVerified contains only allowed characters.
     *             <code>false</code> else.
     */
    protected boolean containsAllowedCharactersOnly(String template, String inputString) {
        String stringToBeVerified = getNonDigitString(inputString);
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
