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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Calendar;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractConfigurableModelObjectTest {

    @Mock
    private IProductComponent productCmpt;

    @Mock
    private IProductComponentGeneration productCmptGeneration;
    private AbstractConfigurableModelObject abstractConfigurableModelObject;

    @Before
    public void createAbstractConfigurableModelObject() throws Exception {
        abstractConfigurableModelObject = mock(AbstractConfigurableModelObject.class, CALLS_REAL_METHODS);
        abstractConfigurableModelObject.setProductComponent(productCmpt);
        abstractConfigurableModelObject.setProductCmptGeneration(productCmptGeneration);
    }

    @Test
    public void testEffectiveFromHasChanged() throws Exception {
        doReturn(null).when(abstractConfigurableModelObject).getEffectiveFromAsCalendar();

        assertNotNull(abstractConfigurableModelObject.getProductCmptGeneration());

        abstractConfigurableModelObject.effectiveFromHasChanged();

        assertNotNull(abstractConfigurableModelObject.getProductCmptGeneration());

        Calendar calendar = mock(Calendar.class);
        when(abstractConfigurableModelObject.getEffectiveFromAsCalendar()).thenReturn(calendar);

        abstractConfigurableModelObject.effectiveFromHasChanged();

        assertNull(abstractConfigurableModelObject.getProductCmptGeneration());
    }

}
