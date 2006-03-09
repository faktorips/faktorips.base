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

package org.faktorips.datatype;

import org.faktorips.util.ArgumentCheck;

/**
 * Datatype representing an array of primitive Datatypes with a dimension.
 * 
 * @author Jan Ortmann
 */
public class ArrayDatatype extends AbstractDatatype {

    // The primitive datatype
    private Datatype datatype;
    
    // the dimenion of the array
    private int dimension;
    
    /**
     * Returns the dimensions the datatypeName specifies. 
     * <p>
     * Examples:<br>
     * "Money" specifies dimension 0.
     * "Money[]" specifies dimenesion 1
     * "Money[][]" specifies dimension 2
     * 
     * @param datatypeName
     * @return
     */
    public final static int getDimension(String datatypeName) {
        int dimension = 0;
        while (datatypeName.endsWith("[]")) {
            dimension++;
            datatypeName = datatypeName.substring(0, datatypeName.length()-2);
        }
        return dimension;
    }
    
    /**
     * Returns the basic datatype name specified in the given datatypeName. 
     * <p>
     * Examples:<br>
     * "Money" specifies basic datatype Money.
     * "Money[]" specifies basic datatype Money.
     * "Money[][]" specifies basic datatype Money.
     * 
     * @param datatypeName
     * @return
     */
    public final static String getBasicDatatypeName(String datatypeName) {
        while (datatypeName.endsWith("[]")) {
            datatypeName = datatypeName.substring(0, datatypeName.length()-2);
        }
        return datatypeName;
    }
    
    /**
     * Constructs a new array datatype based on the guven underlying datatype and the dimension. 
     */
    public ArrayDatatype(Datatype datatype, int dimension) {
        super();
        ArgumentCheck.notNull(datatype);
        this.datatype = datatype;
        this.dimension = dimension;
    }
    
    /**
     * Returns the array datatype's basic datatype. E.g. for an array of Money values, <code>Datatype.MONEY</code>
     * is the basic datatype.
     */
    public Datatype getBasicDatatype() {
        return datatype;
    }
    
    /**
     * Returns the array's dimension.
     */
    public int getDimension() {
        return dimension;
    }
    
    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.Datatype#getName()
     */
    public String getName() {
        StringBuffer buffer = new StringBuffer(datatype.getName());
        for (int i=0; i<dimension; i++) {
            buffer.append("[]");
        }
        return buffer.toString();
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.Datatype#getQualifiedName()
     */
    public String getQualifiedName() {
        StringBuffer buffer = new StringBuffer(datatype.getQualifiedName());
        for (int i=0; i<dimension; i++) {
            buffer.append("[]");
        }
        return buffer.toString();
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
        return datatype.getJavaClassName();
    }

}
