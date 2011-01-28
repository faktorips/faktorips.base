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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;

public class IpsElementInIIpsPackageFilterTest extends TestCase {

    DocumentationContext context = mock(DocumentationContext.class);

    public void testIpsObject() {
        IIpsPackageFragment ipsPackageFragment = mock(IIpsPackageFragment.class);
        IIpsPackageFragment anotherIpsPackageFragment = mock(IIpsPackageFragment.class);

        when(ipsPackageFragment.getName()).thenReturn("de.package");
        when(anotherIpsPackageFragment.getName()).thenReturn("de.package.filtered");

        IIpsObject object = mock(IIpsObject.class);
        when(object.getIpsPackageFragment()).thenReturn(ipsPackageFragment);

        IIpsObject filteredObject = mock(IIpsObject.class);
        when(filteredObject.getIpsPackageFragment()).thenReturn(anotherIpsPackageFragment);

        IpsElementInIIpsPackageFilter filter = new IpsElementInIIpsPackageFilter(ipsPackageFragment, context);

        assertTrue(filter.accept(object));
        assertFalse(filter.accept(filteredObject));
    }

    public void testIpsObjectInPackageWithSameName() {
        IIpsPackageFragment ipsPackageFragment = mock(IIpsPackageFragment.class);
        IIpsPackageFragment anotherIpsPackageFragment = mock(IIpsPackageFragment.class);

        when(ipsPackageFragment.getName()).thenReturn("de.package");
        when(anotherIpsPackageFragment.getName()).thenReturn("de.package");

        IIpsObject object = mock(IIpsObject.class);
        when(object.getIpsPackageFragment()).thenReturn(anotherIpsPackageFragment);

        IpsElementInIIpsPackageFilter filter = new IpsElementInIIpsPackageFilter(ipsPackageFragment, context);

        assertTrue(filter.accept(object));
    }

    public void testIpsSrcFile() throws CoreException {
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

        IpsElementInIIpsPackageFilter filter = new IpsElementInIIpsPackageFilter(ipsPackageFragment, context);

        assertTrue(filter.accept(srcFile));
        assertFalse(filter.accept(filteredSrcFile));
    }

    public void testIpsSrcFileException() throws CoreException {
        IIpsPackageFragment ipsPackageFragment = mock(IIpsPackageFragment.class);

        IIpsObject object = mock(IIpsObject.class);
        when(object.getIpsPackageFragment()).thenReturn(ipsPackageFragment);

        IIpsSrcFile srcFile = mock(IIpsSrcFile.class);
        when(srcFile.getIpsObject()).thenThrow(new CoreException(new IpsStatus("Exception")));

        IpsElementInIIpsPackageFilter filter = new IpsElementInIIpsPackageFilter(ipsPackageFragment, context);

        assertFalse(filter.accept(srcFile));
        verify(context).addStatus((IStatus)any());
    }

}
