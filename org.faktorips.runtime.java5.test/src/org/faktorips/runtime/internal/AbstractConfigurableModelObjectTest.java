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
