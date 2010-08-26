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

package org.faktorips.devtools.core.internal.model;

import java.util.Arrays;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestEnumType;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;

/**
 * 
 * @author Peter Erzberger
 */
public class DynamicEnumDatatypeTest extends AbstractIpsPluginTest {

    private DynamicEnumDatatype dataType;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        IpsProject project = (IpsProject)newIpsProject();
        dataType = newDefinedEnumDatatype(project, new Class[] { TestEnumType.class })[0];
    }

    public void testGetAllValueIds() {
        List<String> allValues = Arrays.asList(dataType.getAllValueIds(false));
        assertTrue(allValues.contains(TestEnumType.FIRSTVALUE.getId()));
        assertTrue(allValues.contains(TestEnumType.SECONDVALUE.getId()));
        assertTrue(allValues.contains(TestEnumType.THIRDVALUE.getId()));

        allValues = Arrays.asList(dataType.getAllValueIds(true));
        assertTrue(allValues.contains(null));
    }

    public void testIsSupportingNames() {
        assertTrue(dataType.isSupportingNames());
    }

    public void testGetValueName() {
        assertEquals(TestEnumType.FIRSTVALUE.getName(), dataType.getValueName(TestEnumType.FIRSTVALUE.getId()));
        assertEquals(TestEnumType.SECONDVALUE.getName(), dataType.getValueName(TestEnumType.SECONDVALUE.getId()));
        assertEquals(TestEnumType.THIRDVALUE.getName(), dataType.getValueName(TestEnumType.THIRDVALUE.getId()));
    }

}
