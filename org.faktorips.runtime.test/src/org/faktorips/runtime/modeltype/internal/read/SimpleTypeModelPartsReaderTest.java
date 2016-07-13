package org.faktorips.runtime.modeltype.internal.read;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.faktorips.runtime.modeltype.IModelElement;
import org.faktorips.runtime.modeltype.internal.AbstractModelElement;
import org.faktorips.runtime.modeltype.internal.DocumentationType;
import org.faktorips.runtime.modeltype.internal.read.SimpleTypeModelPartsReader.ModelElementCreator;
import org.faktorips.runtime.modeltype.internal.read.SimpleTypeModelPartsReader.NameAccessor;
import org.faktorips.runtime.modeltype.internal.read.SimpleTypeModelPartsReader.NamesAccessor;
import org.faktorips.runtime.util.MessagesHelper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SimpleTypeModelPartsReaderTest {

    @Mock
    IModelElement parentModel;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private final ModelElementCreator<DummyElement> modelElementCreator = new ModelElementCreator<DummyElement>() {

        @Override
        public DummyElement create(IModelElement parentElement, String name, Method getterMethod) {
            return new DummyElement(parentElement, name, getterMethod);
        }
    };

    private final NameAccessor<ChildAnnotation> nameAccessor = new NameAccessor<ChildAnnotation>() {

        @Override
        public String getName(ChildAnnotation annotation) {
            return annotation.value();
        }
    };

    private final NamesAccessor<ParentAnnotation> namesAccessor = new NamesAccessor<ParentAnnotation>() {

        @Override
        public String[] getNames(ParentAnnotation annotation) {
            return annotation.kids();
        }
    };

    @Test
    public void testCreateParts() throws Exception {
        SimpleTypeModelPartsReader<DummyElement, ParentAnnotation, ChildAnnotation> simpleTypeModelPartsReader = new SimpleTypeModelPartsReader<DummyElement, ParentAnnotation, ChildAnnotation>(
                ParentAnnotation.class, namesAccessor, ChildAnnotation.class, nameAccessor, modelElementCreator);

        LinkedHashMap<String, DummyElement> parts = simpleTypeModelPartsReader.createParts(Parent.class,
                PartHolder2.class, parentModel);

        assertThat(parts, is(notNullValue()));
        assertThat(parts.size(), is(2));
        Iterator<String> keys = parts.keySet().iterator();
        assertThat(keys.next(), is(equalTo("Tick")));
        assertThat(keys.next(), is(equalTo("Trick")));
        assertThat(parts.get("Tick").parentElement, is(parentModel));
        assertThat(parts.get("Tick").getterMethod, is(equalTo(PartHolder2.class.getMethod("foo"))));
    }

    @Test
    public void testCreateParts_tooMany() throws Exception {
        SimpleTypeModelPartsReader<DummyElement, ParentAnnotation, ChildAnnotation> simpleTypeModelPartsReader = new SimpleTypeModelPartsReader<DummyElement, ParentAnnotation, ChildAnnotation>(
                ParentAnnotation.class, namesAccessor, ChildAnnotation.class, nameAccessor, modelElementCreator);
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Cannot find part Track in " + PartHolder3.class.getCanonicalName());

        simpleTypeModelPartsReader.createParts(Parent.class, PartHolder3.class, parentModel);
    }

    @Test
    public void testCreateParts_tooFew() throws Exception {
        SimpleTypeModelPartsReader<DummyElement, ParentAnnotation, ChildAnnotation> simpleTypeModelPartsReader = new SimpleTypeModelPartsReader<DummyElement, ParentAnnotation, ChildAnnotation>(
                ParentAnnotation.class, namesAccessor, ChildAnnotation.class, nameAccessor, modelElementCreator);
        expectedEx.expect(IllegalStateException.class);
        expectedEx.expectMessage("No getter method found for annotated part \"Trick\"");

        simpleTypeModelPartsReader.createParts(Parent.class, PartHolder1.class, parentModel);
    }

    @Test
    public void testCreateParts_manyTooFew() throws Exception {
        SimpleTypeModelPartsReader<DummyElement, ParentAnnotation, ChildAnnotation> simpleTypeModelPartsReader = new SimpleTypeModelPartsReader<DummyElement, ParentAnnotation, ChildAnnotation>(
                ParentAnnotation.class, namesAccessor, ChildAnnotation.class, nameAccessor, modelElementCreator);
        expectedEx.expect(IllegalStateException.class);
        expectedEx.expectMessage("No getter methods found for annotated parts \"Trick\", \"Track\"");

        simpleTypeModelPartsReader.createParts(Parent2.class, PartHolder1.class, parentModel);
    }

    @Test
    public void testCreateParts_Interface() throws Exception {
        SimpleTypeModelPartsReader<DummyElement, ParentAnnotation, ChildAnnotation> simpleTypeModelPartsReader = new SimpleTypeModelPartsReader<DummyElement, ParentAnnotation, ChildAnnotation>(
                ParentAnnotation.class, namesAccessor, ChildAnnotation.class, nameAccessor, modelElementCreator);

        LinkedHashMap<String, DummyElement> parts = simpleTypeModelPartsReader.createParts(ParentInterface.class,
                ParentInterface.class, parentModel);

        assertThat(parts, is(notNullValue()));
        assertThat(parts.size(), is(2));
        Iterator<String> keys = parts.keySet().iterator();
        assertThat(keys.next(), is(equalTo("Tick")));
        assertThat(keys.next(), is(equalTo("Trick")));
        assertThat(parts.get("Tick").parentElement, is(parentModel));
        assertThat(parts.get("Tick").getterMethod, is(equalTo(ParentInterface.class.getMethod("foo"))));
    }

    @Test
    public void testCreateParts_SubInterface() throws Exception {
        SimpleTypeModelPartsReader<DummyElement, ParentAnnotation, ChildAnnotation> simpleTypeModelPartsReader = new SimpleTypeModelPartsReader<DummyElement, ParentAnnotation, ChildAnnotation>(
                ParentAnnotation.class, namesAccessor, ChildAnnotation.class, nameAccessor, modelElementCreator);

        LinkedHashMap<String, DummyElement> parts = simpleTypeModelPartsReader.createParts(SubInterface.class,
                SubInterface.class, parentModel);

        assertThat(parts, is(notNullValue()));
        assertThat(parts.size(), is(2));
        Iterator<String> keys = parts.keySet().iterator();
        assertThat(keys.next(), is(equalTo("Tick")));
        assertThat(keys.next(), is(equalTo("Trick")));
        assertThat(parts.get("Tick").parentElement, is(parentModel));
        assertThat(parts.get("Tick").getterMethod, is(equalTo(ParentInterface.class.getMethod("foo"))));
    }

    @Retention(RetentionPolicy.RUNTIME)
    private static @interface ParentAnnotation {
        String[] kids();
    }

    @Retention(RetentionPolicy.RUNTIME)
    private static @interface ChildAnnotation {
        String value();
    }

    @ParentAnnotation(kids = { "Tick", "Trick" })
    private static class Parent {

    }

    @ParentAnnotation(kids = { "Tick", "Trick" })
    private static interface ParentInterface {
        @ChildAnnotation("Tick")
        public void foo();

        @ChildAnnotation("Trick")
        public void bar();
    }

    @ParentAnnotation(kids = { "Tick", "Trick" })
    private static interface SubInterface extends ParentInterface {
    }

    @ParentAnnotation(kids = { "Tick", "Trick", "Track" })
    private static class Parent2 {

    }

    private static class PartHolder1 {
        @ChildAnnotation("Tick")
        public void foo() {
        }
    }

    private static class PartHolder2 {
        @ChildAnnotation("Tick")
        public void foo() {
        }

        @ChildAnnotation("Trick")
        public void bar() {
        }
    }

    private static class PartHolder3 extends PartHolder2 {

        @ChildAnnotation("Track")
        public void baz() {
        }
    }

    private static class DummyElement extends AbstractModelElement {

        private final IModelElement parentElement;
        private final Method getterMethod;

        public DummyElement(IModelElement parentElement, String name, Method getterMethod) {
            super(name, null);
            this.parentElement = parentElement;
            this.getterMethod = getterMethod;
        }

        @Override
        protected String getMessageKey(DocumentationType messageType) {
            return null;
        }

        @Override
        protected MessagesHelper getMessageHelper() {
            return null;
        }

    }

}
