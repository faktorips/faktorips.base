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

import org.eclipse.core.resources.IFile;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.adapter.IIpsSrcFileWrapper;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IType;
import org.junit.Test;

public class IpsSrcFileWrapperAdapterFactoryTest {

    private IpsSrcFileWrapperAdapterFactory factory = new IpsSrcFileWrapperAdapterFactory();

    @Test
    public void testGetAdapter_Nulls() {
        assertThat(factory.getAdapter(null, null), is(nullValue()));
    }

    @Test
    public void testGetAdapter_NullType() {
        ProductCmpt productCmpt = new ProductCmpt();
        assertThat(factory.getAdapter(productCmpt, null), is(nullValue()));
    }

    @Test
    public void testGetAdapter_NoSrcFile() {
        IIpsSrcFileWrapper wrapper = mock(IIpsSrcFileWrapper.class);
        assertThat(factory.getAdapter(wrapper, IIpsSrcFile.class), is(nullValue()));
    }

    @Test
    public void testGetAdapter_SrcFile() {
        IIpsSrcFileWrapper wrapper = mock(IIpsSrcFileWrapper.class);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(wrapper.getWrappedIpsSrcFile()).thenReturn(ipsSrcFile);
        assertThat(factory.getAdapter(wrapper, IIpsSrcFile.class), is(ipsSrcFile));
    }

    @Test
    public void testGetAdapter_NoIpsObject() {
        IIpsSrcFileWrapper wrapper = mock(IIpsSrcFileWrapper.class);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(wrapper.getWrappedIpsSrcFile()).thenReturn(ipsSrcFile);
        assertThat(factory.getAdapter(wrapper, IIpsObject.class), is(nullValue()));
    }

    @Test
    public void testGetAdapter_IpsObject() {
        ProductCmpt productCmpt = new ProductCmpt();
        IIpsSrcFileWrapper wrapper = mock(IIpsSrcFileWrapper.class);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(wrapper.getWrappedIpsSrcFile()).thenReturn(ipsSrcFile);
        when(ipsSrcFile.getIpsObject()).thenReturn(productCmpt);
        assertThat(factory.getAdapter(wrapper, IIpsObject.class), is(productCmpt));
    }

    @Test
    public void testGetAdapter_NoProductCmpt() {
        ProductCmpt productCmpt = new ProductCmpt();
        IIpsSrcFileWrapper wrapper = mock(IIpsSrcFileWrapper.class);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(wrapper.getWrappedIpsSrcFile()).thenReturn(ipsSrcFile);
        when(ipsSrcFile.getIpsObject()).thenReturn(productCmpt);
        assertThat(factory.getAdapter(wrapper, IProductCmpt.class), is(nullValue()));
    }

    @Test
    public void testGetAdapter_ProductCmpt() {
        ProductCmpt productCmpt = new ProductCmpt();
        IIpsSrcFileWrapper wrapper = mock(IIpsSrcFileWrapper.class);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(wrapper.getWrappedIpsSrcFile()).thenReturn(ipsSrcFile);
        when(ipsSrcFile.getIpsObject()).thenReturn(productCmpt);
        when(ipsSrcFile.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT);
        assertThat(factory.getAdapter(wrapper, IProductCmpt.class), is(productCmpt));
    }

    @Test
    public void testGetAdapter_ProductCmptTemplate() {
        ProductCmpt productCmpt = new ProductCmpt();
        IIpsSrcFileWrapper wrapper = mock(IIpsSrcFileWrapper.class);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(wrapper.getWrappedIpsSrcFile()).thenReturn(ipsSrcFile);
        when(ipsSrcFile.getIpsObject()).thenReturn(productCmpt);
        when(ipsSrcFile.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_TEMPLATE);
        assertThat(factory.getAdapter(wrapper, IProductCmpt.class), is(productCmpt));
    }

    @Test
    public void testGetAdapter_NoType() {
        ProductCmpt productCmpt = new ProductCmpt();
        IIpsSrcFileWrapper wrapper = mock(IIpsSrcFileWrapper.class);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(wrapper.getWrappedIpsSrcFile()).thenReturn(ipsSrcFile);
        when(ipsSrcFile.getIpsObject()).thenReturn(productCmpt);
        assertThat(factory.getAdapter(wrapper, IType.class), is(nullValue()));
    }

    @Test
    public void testGetAdapter_PolicyCmptType() {
        IIpsSrcFileWrapper wrapper = mock(IIpsSrcFileWrapper.class);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(wrapper.getWrappedIpsSrcFile()).thenReturn(ipsSrcFile);
        IPolicyCmptType policyCmptType = mock(IPolicyCmptType.class);
        when(ipsSrcFile.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);
        when(ipsSrcFile.getIpsObject()).thenReturn(policyCmptType);
        assertThat(factory.getAdapter(wrapper, IType.class), is(policyCmptType));
    }

    @Test
    public void testGetAdapter_ProductCmptType() {
        IIpsSrcFileWrapper wrapper = mock(IIpsSrcFileWrapper.class);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(wrapper.getWrappedIpsSrcFile()).thenReturn(ipsSrcFile);
        IProductCmptType productCmptType = mock(IProductCmptType.class);
        when(ipsSrcFile.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT_TYPE);
        when(ipsSrcFile.getIpsObject()).thenReturn(productCmptType);
        assertThat(factory.getAdapter(wrapper, IType.class), is(productCmptType));
    }

    @Test
    public void testGetAdapter_CorrespondingFile_SimpleMenu() {
        IIpsSrcFileWrapper wrapper = mock(IIpsSrcFileWrapper.class);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(wrapper.getWrappedIpsSrcFile()).thenReturn(ipsSrcFile);
        IFile file = mock(IFile.class);
        AFile aFile = Wrappers.wrap(file).as(AFile.class);
        when(ipsSrcFile.getCorrespondingFile()).thenReturn(aFile);
        IpsPlugin.getDefault().getIpsPreferences().setSimpleContextMenuEnabled(true);
        assertThat(factory.getAdapter(wrapper, IFile.class), is(nullValue()));
    }

    @Test
    public void testGetAdapter_CorrespondingFile() {
        IIpsSrcFileWrapper wrapper = mock(IIpsSrcFileWrapper.class);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(wrapper.getWrappedIpsSrcFile()).thenReturn(ipsSrcFile);
        IFile file = mock(IFile.class);
        AFile aFile = Wrappers.wrap(file).as(AFile.class);
        when(ipsSrcFile.getCorrespondingFile()).thenReturn(aFile);
        IpsPlugin.getDefault().getIpsPreferences().setSimpleContextMenuEnabled(false);
        assertThat(factory.getAdapter(wrapper, IFile.class), is(file));
    }

}
