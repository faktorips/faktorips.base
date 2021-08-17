/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.dthelper;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractDateHelperFactoryTest {

    @Mock
    private ValueDatatype datatype1;

    @Mock
    private ValueDatatype datatype2;

    @Mock
    private StandardBuilderSet builderSet;

    @Test
    public void testCreateDatatypeHelper_CacheTest() throws Exception {
        AbstractDateHelperFactory<ValueDatatype> dateHelperFactory = getDatatypeHelperFactory();

        assertThat(getHelper(LocalDateHelperVariant.JAVA8, datatype1, dateHelperFactory),
                is(getHelper(LocalDateHelperVariant.JAVA8, datatype1, dateHelperFactory)));

        assertThat(getHelper(LocalDateHelperVariant.JAVA8, datatype1, dateHelperFactory),
                is(not(getHelper(LocalDateHelperVariant.JAVA8, datatype2, dateHelperFactory))));

        assertThat(getHelper(LocalDateHelperVariant.JAVA8, datatype1, dateHelperFactory),
                is(not(getHelper(LocalDateHelperVariant.JODA, datatype1, dateHelperFactory))));
    }

    protected DatatypeHelper getHelper(LocalDateHelperVariant variant,
            ValueDatatype datatype,
            AbstractDateHelperFactory<ValueDatatype> dateHelperFactory) {
        when(builderSet.getLocalDateHelperVariant()).thenReturn(variant);
        DatatypeHelper datatypeHelper = dateHelperFactory.createDatatypeHelper(datatype, builderSet);
        return datatypeHelper;
    }

    protected AbstractDateHelperFactory<ValueDatatype> getDatatypeHelperFactory() {
        AbstractDateHelperFactory<ValueDatatype> dateHelperFactory = new AbstractDateHelperFactory<>(
                ValueDatatype.class) {

            @Override
            DatatypeHelper createDatatypeHelper(ValueDatatype datatype, LocalDateHelperVariant variant) {
                return mock(DatatypeHelper.class);
            }

        };
        return dateHelperFactory;
    }

}
