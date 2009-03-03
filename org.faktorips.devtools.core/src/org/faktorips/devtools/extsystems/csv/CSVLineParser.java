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

package org.faktorips.devtools.extsystems.csv;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.faktorips.util.ArgumentCheck;


/**
 * Class for tokenizing sequences of comma separated values.
 * <p>
 * Actually the comma can be any valid character except for new line characters.
 * The values can also be wrapped by a character, e.g. the follwing strings are all
 * correctly handled by this parser:
 * <p>
 * value1, value2, value3<br/>
 * "value1"; "value2"; "value3"<br/>
 * ´value1´, ´´ ,   ´value3´<br/>
 * 
 * @author Roman Grutza
 */
public class CSVLineParser {

    private final static String[] EMPTY_STRING_ARRAY = new String[0];
    
    private String textDelimiter;
    private String fieldDelimiter;

    private Pattern pattern;

    public final static String DEFAULT_TEXT_DELIMITER = "\"";

    public final static String DEFAULT_FIELD_DELIMITER = ",";

    /**
     * Constructs a parser with the default delimiter characters (comma for field delimiters, and
     * no text delimiter). 
     * @param numberOfColumns number of expected fields
     */
    public CSVLineParser(int numberOfColumns) {
        this(",", "", numberOfColumns);
    }
    
    /**
     * Constructs a parser with the given delimiter characters.  
     * 
     * @param fieldDelimiter character separating the consecutive fields, must not be the same as textDelimiter
     * @param textDelimiter character for beginning and end of the field, must not be the same as fieldDelimiter
     * @param numberOfCells number of expected fields
     */
    public CSVLineParser(String fieldDelimiter, String textDelimiter, int numberOfColumns) {
        ArgumentCheck.notNull(fieldDelimiter);
        ArgumentCheck.notNull(textDelimiter);
        if (fieldDelimiter.equals(textDelimiter)) {
            throw new IllegalArgumentException("Text delimiter and field delimiter must not be equal.");
        }
        
        this.fieldDelimiter = fieldDelimiter;
        this.textDelimiter = textDelimiter;
        pattern = createPattern(numberOfColumns);
    }
    
    /**
     * Returns an array of tokenized Strings for the given input csvLine.  
     * 
     * @param csvLine a line of comma separated values using delimiters set during construction of this class.
     * @return a String array of length  {@code numberOfCells} set during construction time, 
     * or an empty String Array if the given String does not match.   
     */
    public String[] getFields(String csvLine) {
        final Matcher matcher = pattern.matcher(csvLine);
        String[] result = EMPTY_STRING_ARRAY;
        
        // must call the matches() method to compute groups, else an
        // IllegalStateException is thrown
        if (matcher.matches()) {
            final int groupCount = matcher.groupCount();
            result = new String[groupCount];
            
            // Begin at index 1 because there is also a special group, group 0,
            // which always represents the entire expression
            for (int i = 1; i <= groupCount; i++) {
                result[i - 1] = matcher.group(i);
            }
        }

        return result;
    }

    private Pattern createPattern(int numberOfFields) {
        StringBuffer regex = new StringBuffer();

        createPatternForField(regex);

        for (int i = 1; i < numberOfFields; i++) {
            createPatternForFieldDelimiter(regex);
            createPatternForField(regex);
        }
        return Pattern.compile(regex.toString());
    }

    private void createPatternForFieldDelimiter(StringBuffer regex) {
        regex.append("[\\s]*");       // trim leading whitespace
        regex.append(fieldDelimiter); // followed by field delimiter
        regex.append("[\\s]*");       // trim trailing whitespace
    }

    private void createPatternForField(StringBuffer regex) {
        regex.append(textDelimiter);  // possible empty text delimiter at beginning of field
        
        // match text content (anything but a field or text delimiter)
        // Note the use of parentheses "()" for tagging matching groups
        regex.append("([^");
        regex.append(fieldDelimiter);
        regex.append(textDelimiter);
        regex.append("]*)");
        
        regex.append(textDelimiter);  // possible empty text delimiter at end of field
    }

}
