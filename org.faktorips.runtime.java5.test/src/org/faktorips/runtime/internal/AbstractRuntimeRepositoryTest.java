/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
