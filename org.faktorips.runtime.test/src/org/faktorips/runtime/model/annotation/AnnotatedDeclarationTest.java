/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.annotation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import org.junit.Test;

public class AnnotatedDeclarationTest {

    @Test
    public void testGetPublishedInterface_FromImplementation() {
        @SuppressWarnings("unchecked")
        Class<PublishedInterface> ifce = (Class<PublishedInterface>)AnnotatedDeclaration
                .getPublishedInterface(Implementation.class);

        assertThat(ifce, equalTo(PublishedInterface.class));
    }

    @Test
    public void testGetPublishedInterface_FromInterface() {
        @SuppressWarnings("unchecked")
        Class<PublishedInterface> ifce = (Class<PublishedInterface>)AnnotatedDeclaration
                .getPublishedInterface(PublishedInterface.class);

        assertThat(ifce, equalTo(PublishedInterface.class));
    }

    @Test
    public void testGetImplementationClass_FromImplementation() {
        @SuppressWarnings("unchecked")
        Class<Implementation> implementationClass = (Class<Implementation>)AnnotatedDeclaration
                .getImplementationClass(Implementation.class);

        assertThat(implementationClass, equalTo(Implementation.class));
    }

    @Test
    public void testGetImplementationClass_FromInterface() {
        @SuppressWarnings("unchecked")
        Class<Implementation> implementationClass = (Class<Implementation>)AnnotatedDeclaration
                .getImplementationClass(PublishedInterface.class);

        assertThat(implementationClass, equalTo(Implementation.class));
    }

    @Test
    public void testIs_Interface() {
        AnnotatedDeclaration annotatedModelTypes = AnnotatedDeclaration.from(PublishedInterface.class);

        assertTrue(annotatedModelTypes.is(IpsPublishedInterface.class));
    }

    @Test
    public void testIs_Impl() {
        AnnotatedDeclaration annotatedModelTypes = AnnotatedDeclaration.from(Implementation.class);

        assertTrue(annotatedModelTypes.is(IpsPublishedInterface.class));
    }

    @Test
    public void testGet_Interface() {
        AnnotatedDeclaration annotatedModelTypes = AnnotatedDeclaration.from(PublishedInterface.class);

        IpsPublishedInterface annotation = annotatedModelTypes.get(IpsPublishedInterface.class);

        assertThat(annotation, notNullValue());
    }

    @Test
    public void testGet_Impl() {
        AnnotatedDeclaration annotatedModelTypes = AnnotatedDeclaration.from(Implementation.class);

        IpsPublishedInterface annotation = annotatedModelTypes.get(IpsPublishedInterface.class);

        assertThat(annotation, notNullValue());
    }

    @Test
    public void testEquals() {
        assertThat(AnnotatedDeclaration.from(Implementation.class),
                is(equalTo(AnnotatedDeclaration.from(Implementation.class))));
    }

    @Test
    public void testHashcode() {
        assertThat(AnnotatedDeclaration.from(Implementation.class).hashCode(),
                is(equalTo(AnnotatedDeclaration.from(Implementation.class).hashCode())));
    }

    @Test
    public void testFrom_returnsSameInstance() {
        assertThat(AnnotatedDeclaration.from(Implementation.class),
                is(sameInstance(AnnotatedDeclaration.from(Implementation.class))));
    }

    @IpsPublishedInterface(implementation = Implementation.class)
    private static interface PublishedInterface {

    }

    private static class Implementation implements Serializable, PublishedInterface {

        private static final long serialVersionUID = 1L;

    }

}
