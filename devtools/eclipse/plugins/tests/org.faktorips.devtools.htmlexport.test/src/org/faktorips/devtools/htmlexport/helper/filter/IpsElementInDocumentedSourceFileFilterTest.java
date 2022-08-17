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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.junit.Test;

public class IpsElementInDocumentedSourceFileFilterTest {

    @Test
    public void testFilterIpsSrcFile() {
        IIpsSrcFile element = mock(IIpsSrcFile.class);
        IIpsSrcFile filteredElement = mock(IIpsSrcFile.class);

        Set<IIpsSrcFile> documentedSourceFiles = new LinkedHashSet<>(1);
        documentedSourceFiles.add(element);

        DocumentationContext context = mock(DocumentationContext.class);
        when(context.getDocumentedSourceFiles()).thenReturn(documentedSourceFiles);

        IpsElementInDocumentedSourceFileFilter filter = new IpsElementInDocumentedSourceFileFilter(context);

        assertTrue(filter.accept(element));
        assertFalse(filter.accept(filteredElement));
    }

    @Test
    public void testFilterIpsObject() {
        IIpsObject object = mock(IIpsObject.class);
        IIpsSrcFile srcFile = mock(IIpsSrcFile.class);

        when(object.getIpsSrcFile()).thenReturn(srcFile);

        IIpsObject filteredObject = mock(IIpsObject.class);
        IIpsSrcFile filteredSrcFile = mock(IIpsSrcFile.class);

        when(filteredObject.getIpsSrcFile()).thenReturn(filteredSrcFile);

        Set<IIpsSrcFile> documentedSourceFiles = new LinkedHashSet<>(1);
        documentedSourceFiles.add(srcFile);

        DocumentationContext context = mock(DocumentationContext.class);
        when(context.getDocumentedSourceFiles()).thenReturn(documentedSourceFiles);

        IpsElementInDocumentedSourceFileFilter filter = new IpsElementInDocumentedSourceFileFilter(context);

        assertTrue(filter.accept(object));
        assertFalse(filter.accept(filteredObject));
    }

    @Test
    public void testFilterIpsPackage() {
        IIpsPackageFragment packageFragment = mock(IIpsPackageFragment.class);
        IIpsPackageFragment filteredPackageFragment = mock(IIpsPackageFragment.class);

        List<IIpsPackageFragment> linkedPackageFragments = new ArrayList<>(1);
        linkedPackageFragments.add(packageFragment);

        DocumentationContext context = mock(DocumentationContext.class);
        when(context.getLinkedPackageFragments()).thenReturn(linkedPackageFragments);

        IpsElementInDocumentedSourceFileFilter filter = new IpsElementInDocumentedSourceFileFilter(context);

        assertTrue(filter.accept(packageFragment));
        assertFalse(filter.accept(filteredPackageFragment));
    }
}
