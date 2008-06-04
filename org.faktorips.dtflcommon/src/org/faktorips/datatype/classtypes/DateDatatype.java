/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.datatype.classtypes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.ValueClassDatatype;


/**
 * A Datatype for the <code>java.util.Date </code> class. The string representation supported by this
 * datatype is <i>yyyy-MM-dd</i>.
 * 
 * @author Peter Erzberger
 */
public class DateDatatype extends ValueClassDatatype {

    private final static String format = "yyyy-MM-dd";
    
    private static SimpleDateFormat formatter = new SimpleDateFormat(format);
    
    /**
     * Creates a new DateDatatype where the name is the short class name.
     */
    public DateDatatype() {
        super(Date.class);
    }

    /**
     * Creates a new DateDatatype with the specified name.
     */
    public DateDatatype(String name) {
        super(Date.class, name);
    }
    
    /**
     * {@inheritDoc}
     */
    public Object getValue(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try{
            String regex = "^\\d{4}-\\d{1,2}-\\d{1,2}$";
            if (!Pattern.matches(regex, value)){
                throw new IllegalArgumentException("Date value must have the format " + format);
            }
            
            return formatter.parse(value);
        }
        catch (Exception e){
            IllegalArgumentException ill = new IllegalArgumentException(
                    "Unable to convert the provided string parameter: \"" + value + "\"  into a " + 
                    Date.class + " instance");
            ill.initCause(e);
            throw ill;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String valueToString(Object value) {
        if(value == null){
            return null;
        }
        return formatter.format(value);
    }
    
    /**
     * Calls the <code>getValue(String)</code> method and casts the result to a Date. 
     */
    public Date getDateValue(String value) {
        return (Date) getValue(value);
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsCompare() {
        return true;
    }
}
