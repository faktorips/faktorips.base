/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.datatype;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestEnumType;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.datatype.IDynamicEnumDatatype;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Peter Erzberger
 */
public class DynamicEnumDatatypeTest extends AbstractIpsPluginTest {

    private IpsProject project;
    private IDynamicEnumDatatype dataType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        project = (IpsProject)newIpsProject();
        dataType = newDefinedEnumDatatype(project, new Class[] { TestEnumType.class })[0];
    }

    @Test
    public void testGetAllValueIds() {
        List<String> allValues = Arrays.asList(dataType.getAllValueIds(false));
        assertTrue(allValues.contains(TestEnumType.FIRSTVALUE.getId()));
        assertTrue(allValues.contains(TestEnumType.SECONDVALUE.getId()));
        assertTrue(allValues.contains(TestEnumType.THIRDVALUE.getId()));
        assertFalse(allValues.contains(null));

        allValues = Arrays.asList(dataType.getAllValueIds(true));
        assertTrue(allValues.contains(TestEnumType.FIRSTVALUE.getId()));
        assertTrue(allValues.contains(TestEnumType.SECONDVALUE.getId()));
        assertTrue(allValues.contains(TestEnumType.THIRDVALUE.getId()));
        assertTrue(allValues.contains(null));
    }

    @Test
    public void testGetAllValueIds_CustomToStringMethod() throws IpsException, IOException {
        DynamicEnumDatatype customDataType = (DynamicEnumDatatype)newDefinedEnumDatatype(project,
                new Class[] { TestEnumWithCustomStringRepresentation.class })[1];
        customDataType.setToStringMethodName("getStringRepresentation");
        customDataType.setAllValuesMethodName("values");

        List<String> allValues = Arrays.asList(customDataType.getAllValueIds(false));
        assertTrue(allValues.contains(TestEnumWithCustomStringRepresentation.FOO.getStringRepresentation()));
        assertTrue(allValues.contains(TestEnumWithCustomStringRepresentation.BAR.getStringRepresentation()));
        assertFalse(allValues.contains(TestEnumWithCustomStringRepresentation.FOO.toString()));
        assertFalse(allValues.contains(TestEnumWithCustomStringRepresentation.BAR.toString()));
        assertFalse(allValues.contains(null));

        allValues = Arrays.asList(customDataType.getAllValueIds(true));
        assertTrue(allValues.contains(TestEnumWithCustomStringRepresentation.FOO.getStringRepresentation()));
        assertTrue(allValues.contains(TestEnumWithCustomStringRepresentation.BAR.getStringRepresentation()));
        assertFalse(allValues.contains(TestEnumWithCustomStringRepresentation.FOO.toString()));
        assertFalse(allValues.contains(TestEnumWithCustomStringRepresentation.BAR.toString()));
        assertTrue(allValues.contains(null));
    }

    @Test
    public void testIsSupportingNames() {
        assertTrue(dataType.isSupportingNames());
    }

    @Test
    public void testGetValueByName() {
        Object value = dataType.getValueByName("third");
        // Can't cast to TestEnumType, because that would be loaded by a different classloader

        assertThat(value.toString(), is(TestEnumType.THIRDVALUE.toString()));
        assertThat(value.getClass().getName(), is(TestEnumType.class.getName()));
    }

    @Test
    public void testGetValueName() {
        assertEquals(TestEnumType.FIRSTVALUE.getName(), dataType.getValueName(TestEnumType.FIRSTVALUE.getId()));
        assertEquals(TestEnumType.SECONDVALUE.getName(), dataType.getValueName(TestEnumType.SECONDVALUE.getId()));
        assertEquals(TestEnumType.THIRDVALUE.getName(), dataType.getValueName(TestEnumType.THIRDVALUE.getId()));
    }

    public enum TestEnumWithCustomStringRepresentation {
        FOO,
        BAR;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

        public String getStringRepresentation() {
            return super.toString();
        }
    }

}
