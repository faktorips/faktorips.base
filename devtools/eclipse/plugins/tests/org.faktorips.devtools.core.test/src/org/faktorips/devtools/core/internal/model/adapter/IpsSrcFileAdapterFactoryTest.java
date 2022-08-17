/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.adapter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.junit.Test;

public class IpsSrcFileAdapterFactoryTest {

    @Test
    public void testAdaptToProductCmpt() {
        IIpsObject cmpt = new ProductCmpt();
        IIpsSrcFile srcFile = mock(IIpsSrcFile.class);
        when(srcFile.getIpsObject()).thenReturn(cmpt);

        IpsSrcFileAdapterFactory factory = new IpsSrcFileAdapterFactory();

        when(srcFile.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT_TYPE);
        assertThat(factory.adaptToProductCmpt(srcFile), is(nullValue()));

        when(srcFile.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT);
        assertThat(factory.adaptToProductCmpt(srcFile), is(cmpt));

        when(srcFile.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_TEMPLATE);
        assertThat(factory.adaptToProductCmpt(srcFile), is(cmpt));
    }
}
