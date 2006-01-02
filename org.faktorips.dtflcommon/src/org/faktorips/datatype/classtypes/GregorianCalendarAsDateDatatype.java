package org.faktorips.datatype.classtypes;

/**
 * A datatype using the GregorianCalendar class to represent dates without any information about the time.
 * 
 * @author Jan Ortmann
 */
public class GregorianCalendarAsDateDatatype extends GregorianCalendarDatatype {

    /**
     * Constructs a new instance with the name "date".
     */
    public GregorianCalendarAsDateDatatype() {
        this("Date");
    }
    
    /**
     * Constructs a new instance with the given name.
     */
    public GregorianCalendarAsDateDatatype(String name) {
        super(name, false);
    }

}
