package org.faktorips.datatype;

import junit.framework.TestCase;

/**
 * 
 * @author Jan Ortmann
 */
public class ArrayDatatypeTest extends TestCase {

    public void testGetDimenion() {
        assertEquals(0, ArrayDatatype.getDimension("Money"));
        assertEquals(1, ArrayDatatype.getDimension("Money[]"));
        assertEquals(2, ArrayDatatype.getDimension("Money[][]"));
    }
    
    public void testGetBasicDatatypeName() {
        assertEquals("Money", ArrayDatatype.getBasicDatatypeName("Money"));
        assertEquals("Money", ArrayDatatype.getBasicDatatypeName("Money[]"));
        assertEquals("Money", ArrayDatatype.getBasicDatatypeName("Money[][]"));
    }
    
    public void testGetName() {
        ArrayDatatype datatype = new ArrayDatatype(Datatype.MONEY, 2);
        assertEquals("Money[][]", datatype.getName());
    }

    public void testGetQualifiedName() {
        ArrayDatatype datatype = new ArrayDatatype(Datatype.MONEY, 2);
        assertEquals("Money[][]", datatype.getQualifiedName());
    }

    public void testGetJavaClassName() {
        ArrayDatatype datatype = new ArrayDatatype(Datatype.MONEY, 2);
        assertEquals(Datatype.MONEY.getJavaClassName(), datatype.getJavaClassName());
    }

}
