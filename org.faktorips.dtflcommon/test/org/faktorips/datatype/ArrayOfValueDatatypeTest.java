/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.datatype;

import junit.framework.TestCase;

/**
 * 
 * @author Jan Ortmann
 */
public class ArrayOfValueDatatypeTest extends TestCase {

    public void testGetDimenion() {
        assertEquals(0, ArrayOfValueDatatype.getDimension(null));
        assertEquals(0, ArrayOfValueDatatype.getDimension("Money")); //$NON-NLS-1$
        assertEquals(1, ArrayOfValueDatatype.getDimension("Money[]")); //$NON-NLS-1$
        assertEquals(2, ArrayOfValueDatatype.getDimension("Money[][]")); //$NON-NLS-1$
    }

    public void testGetBasicDatatypeName() {
        assertEquals("Money", ArrayOfValueDatatype.getBasicDatatypeName("Money")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("Money", ArrayOfValueDatatype.getBasicDatatypeName("Money[]")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("Money", ArrayOfValueDatatype.getBasicDatatypeName("Money[][]")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void testGetName() {
        ArrayOfValueDatatype datatype = new ArrayOfValueDatatype(Datatype.MONEY, 2);
        assertEquals(datatype.getBasicDatatype().getName() + "[][]", datatype.getName()); //$NON-NLS-1$
    }

    public void testGetQualifiedName() {
        ArrayOfValueDatatype datatype = new ArrayOfValueDatatype(Datatype.MONEY, 2);
        assertEquals(datatype.getBasicDatatype().getQualifiedName() + "[][]", datatype.getQualifiedName()); //$NON-NLS-1$
    }

    public void testGetJavaClassName() {
        ArrayOfValueDatatype datatype = new ArrayOfValueDatatype(Datatype.MONEY, 2);
        assertEquals(datatype.getBasicDatatype().getJavaClassName() + "[][]", datatype.getJavaClassName()); //$NON-NLS-1$
    }

}
