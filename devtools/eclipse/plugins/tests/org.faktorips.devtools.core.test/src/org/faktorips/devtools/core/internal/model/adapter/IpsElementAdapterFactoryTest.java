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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.model.IIpsElement;
import org.junit.jupiter.api.Test;

/**
 * Regression tests for FIPS-15454/FIPS-15456: {@code getTraversals()} used to check the
 * {@link AResource} returned by {@link IIpsElement#getEnclosingResource()} directly for
 * {@code instanceof IResource}, which was always {@code false}, so it always returned {@code null}
 * and callers crashed with an NPE reading {@code traversals.length}.
 */
public class IpsElementAdapterFactoryTest {

    @Test
    public void testGetTraversalsUnwrapsEnclosingAResourceToFile() throws Exception {
        IResource file = mock(IResource.class);
        when(file.getType()).thenReturn(IResource.FILE);

        ResourceTraversal[] traversals = getTraversals(file);

        assertThat(traversals.length, is(1));
        assertThat(traversals[0].getResources()[0], is(file));
    }

    @Test
    public void testGetTraversalsUnwrapsEnclosingAResourceToWorkspaceRoot() throws Exception {
        IWorkspaceRoot root = mock(IWorkspaceRoot.class);
        when(root.getType()).thenReturn(IResource.ROOT);
        IProject[] projects = { mock(IProject.class) };
        when(root.getProjects()).thenReturn(projects);

        ResourceTraversal[] traversals = getTraversals(root);

        assertThat(traversals.length, is(1));
        assertThat(traversals[0].getResources(), is(projects));
    }

    @Test
    public void testGetTraversalsReturnsEmptyArrayInsteadOfNullWhenUnwrappedObjectIsNotAResource() throws Exception {
        ResourceTraversal[] traversals = getTraversals(null);

        assertThat(traversals, is(notNullValue()));
        assertThat(traversals.length, is(0));
    }

    /**
     * Builds the {@code IpsElementResourceMapping} directly via reflection (it is a private nested
     * class of {@link IpsElementAdapterFactory} with no public factory other than
     * {@link IpsElementAdapterFactory#getAdapter(Object, Class)}, whose own resource-type dispatch
     * is unrelated to the {@code getTraversals()} behavior under test here).
     */
    private ResourceTraversal[] getTraversals(Object unwrapped) throws Exception {
        IIpsElement ipsElement = mock(IIpsElement.class);
        AResource enclosingResource = mock(AResource.class);
        when(enclosingResource.unwrap()).thenReturn(unwrapped);
        when(ipsElement.getEnclosingResource()).thenReturn(enclosingResource);

        Class<?> mappingClass = Class
                .forName(IpsElementAdapterFactory.class.getName() + "$IpsElementResourceMapping");
        Constructor<?> constructor = mappingClass.getDeclaredConstructor(IIpsElement.class);
        constructor.setAccessible(true);
        ResourceMapping mapping = (ResourceMapping)constructor.newInstance(ipsElement);
        return mapping.getTraversals(null, null);
    }
}
