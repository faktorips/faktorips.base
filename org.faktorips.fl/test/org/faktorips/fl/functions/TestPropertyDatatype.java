package org.faktorips.fl.functions;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.AbstractDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.PropertyDatatype;
import org.faktorips.util.ArgumentCheck;

/**
 * Implementation of PropertyDatatype for testing purposes.
 * 
 * @author Jan Ortmann
 */
public class TestPropertyDatatype extends AbstractDatatype implements PropertyDatatype {

    private String name;
    private Datatype datatype;
    
    public TestPropertyDatatype(String name, Datatype datatype) {
        ArgumentCheck.notNull(name);
        ArgumentCheck.notNull(datatype);
        this.name = name;
        this.datatype = datatype;
    }
    
    /**
     * Overridden Method.
     *
     * @see org.faktorips.fl.PropertyDatatype#getDatatype()
     */
    public Datatype getDatatype() {
        return null;
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.fl.PropertyDatatype#getGetterMethod()
     */
    public String getGetterMethod() {
        return "get" + StringUtils.capitalise(name);
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.Datatype#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.Datatype#getQualifiedName()
     */
    public String getQualifiedName() {
        return name;
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.Datatype#isPrimitive()
     */
    public boolean isPrimitive() {
        return false;
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.Datatype#isValueDatatype()
     */
    public boolean isValueDatatype() {
        return false;
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.Datatype#getJavaClassName()
     */
    public String getJavaClassName() {
        return null;
    }

}
