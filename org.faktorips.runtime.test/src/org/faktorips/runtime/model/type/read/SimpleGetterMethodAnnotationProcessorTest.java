package org.faktorips.runtime.model.type.read;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import org.faktorips.runtime.model.type.read.SimpleTypePartsReader.NameAccessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SimpleGetterMethodAnnotationProcessorTest {

    @Mock
    private NameAccessor<DummyAnnotation> nameAccessor;

    @Test
    public void testProcess() throws SecurityException, NoSuchMethodException {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        SimpleGetterMethodModelDescriptor<?> descriptor = new SimpleGetterMethodModelDescriptor(null);
        Method annotatedElement = getClass().getMethod("bar");

        new SimpleGetterMethodAnnotationProcessor<DummyAnnotation, SimpleGetterMethodModelDescriptor<?>>(
                DummyAnnotation.class, nameAccessor).process(descriptor, null, annotatedElement);

        assertThat(descriptor.getGetterMethod(), is(annotatedElement));
    }

    @Test
    public void testGetName() {
        DummyAnnotation annotation = SimpleGetterMethodAnnotationProcessorTest.class
                .getAnnotation(DummyAnnotation.class);

        new SimpleGetterMethodAnnotationProcessor<DummyAnnotation, SimpleGetterMethodModelDescriptor<?>>(
                DummyAnnotation.class, nameAccessor).getName(annotation);

        verify(nameAccessor).getName(annotation);
    }

    @DummyAnnotation
    public void bar() {
        // does nothing
    }

    @Retention(RetentionPolicy.RUNTIME)
    private static @interface DummyAnnotation {
    }

}
