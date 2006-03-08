package org.faktorips.datatype;

/**
 * A value class to test the generic value datatype.
 * 
 * @author Jan Ortmann
 */
public class TestValueClass {

    public final static Integer getInteger(String s) {
        return Integer.valueOf(s);
    }

    public final static boolean isInteger(String s) {
        try {
            Integer.valueOf(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public TestValueClass() {
        super();
    }
    

}
