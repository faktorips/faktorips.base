/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class AbstractRuntimeRepositoryTest {

    @Test
    public void testGetEnumValuesDefinedInType() throws Exception {
        AbstractRuntimeRepository abstractRuntimeRepository = mock(AbstractRuntimeRepository.class, CALLS_REAL_METHODS);

        List<EnumTestClass> enumValues = abstractRuntimeRepository.getEnumValuesDefinedInType(EnumTestClass.class);

        assertEquals(EnumTestClass.VALUES, enumValues);
    }

    public static class EnumTestClass {

        public static final EnumTestClass VALUE1 = new EnumTestClass();

        public static final EnumTestClass VALUE2 = new EnumTestClass();

        public static final List<EnumTestClass> VALUES = Arrays.asList(VALUE1, VALUE2);

    }

}
