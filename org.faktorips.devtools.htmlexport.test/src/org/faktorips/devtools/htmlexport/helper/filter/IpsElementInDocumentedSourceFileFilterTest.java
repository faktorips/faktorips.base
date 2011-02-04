/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;

public class IpsElementInDocumentedSourceFileFilterTest extends TestCase {

    public void testFilterIpsSrcFile() {
        IIpsSrcFile element = mock(IIpsSrcFile.class);
        IIpsSrcFile filteredElement = mock(IIpsSrcFile.class);

        List<IIpsSrcFile> documentedSourceFiles = new ArrayList<IIpsSrcFile>(1);
        documentedSourceFiles.add(element);

        DocumentationContext context = mock(DocumentationContext.class);
        when(context.getDocumentedSourceFiles()).thenReturn(documentedSourceFiles);

        IpsElementInDocumentedSourceFileFilter filter = new IpsElementInDocumentedSourceFileFilter(context);

        assertTrue(filter.accept(element));
        assertFalse(filter.accept(filteredElement));
    }

    public void testFilterIpsObject() {
        IIpsObject object = mock(IIpsObject.class);
        IIpsSrcFile srcFile = mock(IIpsSrcFile.class);

        when(object.getIpsSrcFile()).thenReturn(srcFile);

        IIpsObject filteredObject = mock(IIpsObject.class);
        IIpsSrcFile filteredSrcFile = mock(IIpsSrcFile.class);

        when(filteredObject.getIpsSrcFile()).thenReturn(filteredSrcFile);

        List<IIpsSrcFile> documentedSourceFiles = new ArrayList<IIpsSrcFile>(1);
        documentedSourceFiles.add(srcFile);

        DocumentationContext context = mock(DocumentationContext.class);
        when(context.getDocumentedSourceFiles()).thenReturn(documentedSourceFiles);

        IpsElementInDocumentedSourceFileFilter filter = new IpsElementInDocumentedSourceFileFilter(context);

        assertTrue(filter.accept(object));
        assertFalse(filter.accept(filteredObject));
    }

    public void testFilterIpsPackage() {
        IIpsPackageFragment packageFragment = mock(IIpsPackageFragment.class);
        IIpsPackageFragment filteredPackageFragment = mock(IIpsPackageFragment.class);

        List<IIpsPackageFragment> linkedPackageFragments = new ArrayList<IIpsPackageFragment>(1);
        linkedPackageFragments.add(packageFragment);

        DocumentationContext context = mock(DocumentationContext.class);
        when(context.getLinkedPackageFragments()).thenReturn(linkedPackageFragments);

        IpsElementInDocumentedSourceFileFilter filter = new IpsElementInDocumentedSourceFileFilter(context);

        assertTrue(filter.accept(packageFragment));
        assertFalse(filter.accept(filteredPackageFragment));
    }
}
