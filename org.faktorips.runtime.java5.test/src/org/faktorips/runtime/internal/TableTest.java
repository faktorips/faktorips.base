/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.InputStream;

import org.faktorips.runtime.XmlAbstractTestCase;
import org.faktorips.values.Decimal;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Peter Erzberger
 */
public class TableTest extends XmlAbstractTestCase {

    private TestTable table;

    @Before
    protected void setUp() throws Exception {
        table = new TestTable();
    }

    @Test
    public void testInitFromXmlViaSax() throws Exception {
        String className = getClass().getName();
        int index = className.lastIndexOf('.');
        if (index > -1) {
            className = className.substring(index + 1);
        }
        String resourceName = className + ".xml";
        InputStream is = getClass().getResourceAsStream(resourceName);
        if (is == null) {
            throw new RuntimeException("Can't find resource " + resourceName);
        }

        table.initFromXml(is, null);

        TestTableRow row = (TestTableRow)table.rows.get(0);
        assertNull(row.getCompany());
        assertEquals(Integer.valueOf("0"), row.getGender());
        assertEquals(Decimal.valueOf("0.1"), row.getRate());

        row = (TestTableRow)table.rows.get(1);
        assertEquals("KQV", row.getCompany());
        assertEquals(Integer.valueOf("1"), row.getGender());
        assertEquals(Decimal.valueOf("0.15"), row.getRate());

        row = (TestTableRow)table.rows.get(5);
        assertEquals("BBV", row.getCompany());
        assertEquals(Integer.valueOf("1"), row.getGender());
        assertEquals(Decimal.valueOf("0.35"), row.getRate());
    }
}
