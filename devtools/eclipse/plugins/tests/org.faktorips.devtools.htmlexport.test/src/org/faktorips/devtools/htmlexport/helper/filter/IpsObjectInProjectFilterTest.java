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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.jupiter.api.Test;

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
