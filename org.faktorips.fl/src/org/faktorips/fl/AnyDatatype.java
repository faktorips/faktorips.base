package org.faktorips.fl;

import org.faktorips.datatype.Datatype;

/**
 * 
 * @author Jan Ortmann
 */
public class AnyDatatype implements Datatype {
    
    public final static AnyDatatype INSTANCE = new AnyDatatype();

    private AnyDatatype() {
        super();
    }

    public String getName() {
        return "any";
    }

    /**
     * Overridden Method.
     * @see org.faktorips.datatype.Datatype#getQualifiedName()
     */
    public String getQualifiedName() {
        return "any";
    }

    /**
     * Overridden Method.
     * @see org.faktorips.datatype.Datatype#isVoid()
     */
    public boolean isVoid() {
        return false;
    }

    /**
     * Overridden Method.
     * @see org.faktorips.datatype.Datatype#isPrimitive()
     */
    public boolean isPrimitive() {
        return false;
    }

    /**
     * Overridden Method.
     * @see org.faktorips.datatype.Datatype#isValueDatatype()
     */
    public boolean isValueDatatype() {
        return false;
    }

    /**
     * Overridden Method.
     * @see org.faktorips.datatype.Datatype#getJavaClassName()
     */
    public String getJavaClassName() {
        return null;
    }

    /**
     * Overridden Method.
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        return 0;
    }

}
