/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestEnumType;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Peter Erzberger
 */
public class DynamicEnumDatatypeTest extends AbstractIpsPluginTest {

    private DynamicEnumDatatype dataType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IpsProject project = (IpsProject)newIpsProject();
        dataType = newDefinedEnumDatatype(project, new Class[] { TestEnumType.class })[0];
    }

    @Test
    public void testGetAllValueIds() {
        List<String> allValues = Arrays.asList(dataType.getAllValueIds(false));
        assertTrue(allValues.contains(TestEnumType.FIRSTVALUE.getId()));
        assertTrue(allValues.contains(TestEnumType.SECONDVALUE.getId()));
        assertTrue(allValues.contains(TestEnumType.THIRDVALUE.getId()));

        allValues = Arrays.asList(dataType.getAllValueIds(true));
        assertTrue(allValues.contains(null));
    }

    @Test
    public void testIsSupportingNames() {
        assertTrue(dataType.isSupportingNames());
    }

    @Test
    public void testGetValueName() {
        assertEquals(TestEnumType.FIRSTVALUE.getName(), dataType.getValueName(TestEnumType.FIRSTVALUE.getId()));
        assertEquals(TestEnumType.SECONDVALUE.getName(), dataType.getValueName(TestEnumType.SECONDVALUE.getId()));
        assertEquals(TestEnumType.THIRDVALUE.getName(), dataType.getValueName(TestEnumType.THIRDVALUE.getId()));
    }

}
