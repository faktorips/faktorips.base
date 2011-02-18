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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Test;

public class IpsObjectInProjectFilterTest {

    @Test
    public void testObjectInProject() {
        IIpsElement element = mock(IIpsElement.class);
        IIpsElement filteredElement = mock(IIpsElement.class);

        IIpsProject project = mock(IIpsProject.class);
        IIpsProject anotherProject = mock(IIpsProject.class);

        when(element.getIpsProject()).thenReturn(project);
        when(filteredElement.getIpsProject()).thenReturn(anotherProject);

        IpsObjectInProjectFilter filter = new IpsObjectInProjectFilter(project);

        assertTrue(filter.accept(element));
        assertFalse(filter.accept(filteredElement));
    }
}
