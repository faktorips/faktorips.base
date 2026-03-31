/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.testrepository.testenum;

import static java.lang.reflect.Modifier.isTransient;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.lang.reflect.Field;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.testrepository.testenum.empty.EmptyEnum;
import org.faktorips.runtime.testrepository.testenum.produkt.ExtensibleEnum;
import org.faktorips.runtime.testrepository.testenum.produkt.ExtensibleEnumWithModell;
import org.faktorips.runtime.testrepository.testenum.produkt.SuperExtensibleEnum;
import org.faktorips.runtime.testrepository.testenum.produkt.SuperExtensibleEnumWithModell;
import org.junit.jupiter.api.Test;

/**
 * Verifies that the {@code productRepository} field in extensible enums is declared
 * {@code transient}. The field holds a non-serializable {@link IRuntimeRepository} reference, but
 * serialization is handled via {@code writeReplace}/{@code SerializationProxy}, so the field must
 * not participate in default serialization.
 */
class ExtensibleEnumSerializationTest {

    @Test
    void productRepository_isTransient_ExtensibleEnum() throws NoSuchFieldException {
        assertProductRepositoryIsTransient(ExtensibleEnum.class);
    }

    @Test
    void productRepository_isTransient_ExtensibleEnumWithModell() throws NoSuchFieldException {
        assertProductRepositoryIsTransient(ExtensibleEnumWithModell.class);
    }

    @Test
    void productRepository_isTransient_SuperExtensibleEnum() throws NoSuchFieldException {
        assertProductRepositoryIsTransient(SuperExtensibleEnum.class);
    }

    @Test
    void productRepository_isTransient_SuperExtensibleEnumWithModell() throws NoSuchFieldException {
        assertProductRepositoryIsTransient(SuperExtensibleEnumWithModell.class);
    }

    @Test
    void productRepository_isTransient_EmptyEnum() throws NoSuchFieldException {
        assertProductRepositoryIsTransient(EmptyEnum.class);
    }

    private void assertProductRepositoryIsTransient(Class<?> enumClass) throws NoSuchFieldException {
        Field field = enumClass.getDeclaredField("productRepository");
        assertThat("Field 'productRepository' in " + enumClass.getSimpleName()
                + " must be transient to avoid serialization warnings",
                isTransient(field.getModifiers()), is(true));
    }
}
