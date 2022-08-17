/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.junit.Test;

public class IpsElementInIIpsPackageFilterTest {

    DocumentationContext context = mock(DocumentationContext.class);

    @Test
    public void testIpsObject() {
        IIpsPackageFragment ipsPackageFragment = mock(IIpsPackageFragment.class);
        IIpsPackageFragment anotherIpsPackageFragment = mock(IIpsPackageFragment.class);

        when(ipsPackageFragment.getName()).thenReturn("de.package");
        when(anotherIpsPackageFragment.getName()).thenReturn("de.package.filtered");

        IIpsObject object = mock(IIpsObject.class);
        when(object.getIpsPackageFragment()).thenReturn(ipsPackageFragment);

        IIpsObject filteredObject = mock(IIpsObject.class);
        when(filteredObject.getIpsPackageFragment()).thenReturn(anotherIpsPackageFragment);

        IpsElementInIIpsPackageFilter filter = new IpsElementInIIpsPackageFilter(ipsPackageFragment);

        assertTrue(filter.accept(object));
        assertFalse(filter.accept(filteredObject));
    }

    @Test
    public void testIpsObjectInPackageWithSameName() {
        IIpsPackageFragment ipsPackageFragment = mock(IIpsPackageFragment.class);
        IIpsPackageFragment anotherIpsPackageFragment = mock(IIpsPackageFragment.class);

        when(ipsPackageFragment.getName()).thenReturn("de.package");
        when(anotherIpsPackageFragment.getName()).thenReturn("de.package");

        IIpsObject object = mock(IIpsObject.class);
        when(object.getIpsPackageFragment()).thenReturn(anotherIpsPackageFragment);

        IpsElementInIIpsPackageFilter filter = new IpsElementInIIpsPackageFilter(ipsPackageFragment);

        assertTrue(filter.accept(object));
    }

    @Test
    public void testIpsSrcFile() {
        IIpsPackageFragment ipsPackageFragment = mock(IIpsPackageFragment.class);
        IIpsPackageFragment anotherIpsPackageFragment = mock(IIpsPackageFragment.class);

        when(ipsPackageFragment.getName()).thenReturn("de.package");
        when(anotherIpsPackageFragment.getName()).thenReturn("de.package.filtered");

        IIpsObject object = mock(IIpsObject.class);
        when(object.getIpsPackageFragment()).thenReturn(ipsPackageFragment);

        IIpsObject filteredObject = mock(IIpsObject.class);
        when(filteredObject.getIpsPackageFragment()).thenReturn(anotherIpsPackageFragment);

        IIpsSrcFile srcFile = mock(IIpsSrcFile.class);
        when(srcFile.getIpsObject()).thenReturn(object);

        IIpsSrcFile filteredSrcFile = mock(IIpsSrcFile.class);
        when(filteredSrcFile.getIpsObject()).thenReturn(filteredObject);

        IpsElementInIIpsPackageFilter filter = new IpsElementInIIpsPackageFilter(ipsPackageFragment);

        assertTrue(filter.accept(srcFile));
        assertFalse(filter.accept(filteredSrcFile));
    }

}
