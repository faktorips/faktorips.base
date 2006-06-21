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

import junit.framework.TestCase;

/**
 * 
 * @author Jan Ortmann
 */
public class ArrayDatatypeTest extends TestCase {

    public void testGetDimenion() {
        assertEquals(0, ArrayOfValueDatatype.getDimension("Money"));
        assertEquals(1, ArrayOfValueDatatype.getDimension("Money[]"));
        assertEquals(2, ArrayOfValueDatatype.getDimension("Money[][]"));
    }
    
    public void testGetBasicDatatypeName() {
        assertEquals("Money", ArrayOfValueDatatype.getBasicDatatypeName("Money"));
        assertEquals("Money", ArrayOfValueDatatype.getBasicDatatypeName("Money[]"));
        assertEquals("Money", ArrayOfValueDatatype.getBasicDatatypeName("Money[][]"));
    }
    
    public void testGetName() {
        ArrayOfValueDatatype datatype = new ArrayOfValueDatatype(Datatype.MONEY, 2);
        assertEquals("Money[][]", datatype.getName());
    }

    public void testGetQualifiedName() {
        ArrayOfValueDatatype datatype = new ArrayOfValueDatatype(Datatype.MONEY, 2);
        assertEquals("Money[][]", datatype.getQualifiedName());
    }

    public void testGetJavaClassName() {
        ArrayOfValueDatatype datatype = new ArrayOfValueDatatype(Datatype.MONEY, 2);
        assertEquals(Datatype.MONEY.getJavaClassName(), datatype.getJavaClassName());
    }

}
