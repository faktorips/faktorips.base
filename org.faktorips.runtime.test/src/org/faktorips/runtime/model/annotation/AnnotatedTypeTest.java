package org.faktorips.runtime.model.annotation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import org.junit.Test;

public class AnnotatedTypeTest {

    @Test
    public void testGetPublishedInterface_FromImplementation() {
        @SuppressWarnings("unchecked")
        Class<PublishedInterface> ifce = (Class<PublishedInterface>)AnnotatedType
                .getPublishedInterface(Implementation.class);

        assertThat(ifce, equalTo(PublishedInterface.class));
    }

    @Test
    public void testGetPublishedInterface_FromInterface() {
        @SuppressWarnings("unchecked")
        Class<PublishedInterface> ifce = (Class<PublishedInterface>)AnnotatedType
                .getPublishedInterface(PublishedInterface.class);

        assertThat(ifce, equalTo(PublishedInterface.class));
    }

    @Test
    public void testGetImplementationClass_FromImplementation() {
        @SuppressWarnings("unchecked")
        Class<Implementation> implementationClass = (Class<Implementation>)AnnotatedType
                .getImplementationClass(Implementation.class);

        assertThat(implementationClass, equalTo(Implementation.class));
    }

    @Test
    public void testGetImplementationClass_FromInterface() {
        @SuppressWarnings("unchecked")
        Class<Implementation> implementationClass = (Class<Implementation>)AnnotatedType
                .getImplementationClass(PublishedInterface.class);

        assertThat(implementationClass, equalTo(Implementation.class));
    }

    @Test
    public void testIs_Interface() {
        AnnotatedType annotatedModelTypes = AnnotatedType.from(PublishedInterface.class);

        assertTrue(annotatedModelTypes.is(IpsPublishedInterface.class));
    }

    @Test
    public void testIs_Impl() {
        AnnotatedType annotatedModelTypes = AnnotatedType.from(Implementation.class);

        assertTrue(annotatedModelTypes.is(IpsPublishedInterface.class));
    }

    @Test
    public void testGet_Interface() {
        AnnotatedType annotatedModelTypes = AnnotatedType.from(PublishedInterface.class);

        IpsPublishedInterface annotation = annotatedModelTypes.get(IpsPublishedInterface.class);

        assertThat(annotation, notNullValue());
    }

    @Test
    public void testGet_Impl() {
        AnnotatedType annotatedModelTypes = AnnotatedType.from(Implementation.class);

        IpsPublishedInterface annotation = annotatedModelTypes.get(IpsPublishedInterface.class);

        assertThat(annotation, notNullValue());
    }

    @IpsPublishedInterface(implementation = Implementation.class)
    private static interface PublishedInterface {

    }

    private static class Implementation implements Serializable, PublishedInterface {

        private static final long serialVersionUID = 1L;

    }

}
