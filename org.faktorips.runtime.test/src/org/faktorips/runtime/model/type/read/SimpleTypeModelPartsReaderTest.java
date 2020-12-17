package org.faktorips.runtime.model.type.read;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.faktorips.runtime.model.type.DocumentationKind;
import org.faktorips.runtime.model.type.ModelElement;
import org.faktorips.runtime.model.type.read.SimpleTypePartsReader.ModelElementCreator;
import org.faktorips.runtime.model.type.read.SimpleTypePartsReader.NameAccessor;
import org.faktorips.runtime.model.type.read.SimpleTypePartsReader.NamesAccessor;
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
    ModelElement parentModel;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private final ModelElementCreator<DummyElement> modelElementCreator = DummyElement::new;

    private final NameAccessor<ChildAnnotation> nameAccessor = ChildAnnotation::value;

    private final NamesAccessor<ParentAnnotation> namesAccessor = ParentAnnotation::kids;

    @Test
    public void testCreateParts() throws Exception {
        SimpleTypePartsReader<DummyElement, ParentAnnotation, ChildAnnotation> simpleTypePartsReader = new SimpleTypePartsReader<DummyElement, ParentAnnotation, ChildAnnotation>(
                ParentAnnotation.class, namesAccessor, ChildAnnotation.class, nameAccessor, modelElementCreator);

        LinkedHashMap<String, DummyElement> parts = simpleTypePartsReader.createParts(Parent.class, PartHolder2.class,
                parentModel);

        assertThat(parts, is(notNullValue()));
        assertThat(parts.size(), is(2));
        Iterator<String> keys = parts.keySet().iterator();
        assertThat(keys.next(), is(equalTo("tick")));
        assertThat(keys.next(), is(equalTo("trick")));
        assertThat(parts.get("tick").parentElement, is(parentModel));
        assertThat(parts.get("tick").getterMethod, is(equalTo(PartHolder2.class.getMethod("foo"))));
    }

    @Test
    public void testCreateParts_tooMany() throws Exception {
        SimpleTypePartsReader<DummyElement, ParentAnnotation, ChildAnnotation> simpleTypePartsReader = new SimpleTypePartsReader<DummyElement, ParentAnnotation, ChildAnnotation>(
                ParentAnnotation.class, namesAccessor, ChildAnnotation.class, nameAccessor, modelElementCreator);
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Cannot find part Track in " + PartHolder3.class.getCanonicalName());

        simpleTypePartsReader.createParts(Parent.class, PartHolder3.class, parentModel);
    }

    @Test
    public void testCreateParts_tooFew() throws Exception {
        SimpleTypePartsReader<DummyElement, ParentAnnotation, ChildAnnotation> simpleTypePartsReader = new SimpleTypePartsReader<DummyElement, ParentAnnotation, ChildAnnotation>(
                ParentAnnotation.class, namesAccessor, ChildAnnotation.class, nameAccessor, modelElementCreator);
        expectedEx.expect(IllegalStateException.class);
        expectedEx.expectMessage("No getter method found for annotated part \"Trick\"");

        simpleTypePartsReader.createParts(Parent.class, PartHolder1.class, parentModel);
    }

    @Test
    public void testCreateParts_manyTooFew() throws Exception {
        SimpleTypePartsReader<DummyElement, ParentAnnotation, ChildAnnotation> simpleTypePartsReader = new SimpleTypePartsReader<DummyElement, ParentAnnotation, ChildAnnotation>(
                ParentAnnotation.class, namesAccessor, ChildAnnotation.class, nameAccessor, modelElementCreator);
        expectedEx.expect(IllegalStateException.class);
        expectedEx.expectMessage("No getter methods found for annotated parts \"Trick\", \"Track\"");

        simpleTypePartsReader.createParts(Parent2.class, PartHolder1.class, parentModel);
    }

    @Test
    public void testCreateParts_Interface() throws Exception {
        SimpleTypePartsReader<DummyElement, ParentAnnotation, ChildAnnotation> simpleTypePartsReader = new SimpleTypePartsReader<DummyElement, ParentAnnotation, ChildAnnotation>(
                ParentAnnotation.class, namesAccessor, ChildAnnotation.class, nameAccessor, modelElementCreator);

        LinkedHashMap<String, DummyElement> parts = simpleTypePartsReader.createParts(ParentInterface.class,
                ParentInterface.class, parentModel);

        assertThat(parts, is(notNullValue()));
        assertThat(parts.size(), is(2));
        Iterator<String> keys = parts.keySet().iterator();
        assertThat(keys.next(), is(equalTo("tick")));
        assertThat(keys.next(), is(equalTo("trick")));
        assertThat(parts.get("tick").parentElement, is(parentModel));
        assertThat(parts.get("tick").getterMethod, is(equalTo(ParentInterface.class.getMethod("foo"))));
    }

    @Test
    public void testCreateParts_SubInterface() throws Exception {
        SimpleTypePartsReader<DummyElement, ParentAnnotation, ChildAnnotation> simpleTypePartsReader = new SimpleTypePartsReader<DummyElement, ParentAnnotation, ChildAnnotation>(
                ParentAnnotation.class, namesAccessor, ChildAnnotation.class, nameAccessor, modelElementCreator);

        LinkedHashMap<String, DummyElement> parts = simpleTypePartsReader.createParts(SubInterface.class,
                SubInterface.class, parentModel);

        assertThat(parts, is(notNullValue()));
        assertThat(parts.size(), is(2));
        Iterator<String> keys = parts.keySet().iterator();
        assertThat(keys.next(), is(equalTo("tick")));
        assertThat(keys.next(), is(equalTo("trick")));
        assertThat(parts.get("tick").parentElement, is(parentModel));
        assertThat(parts.get("tick").getterMethod, is(equalTo(ParentInterface.class.getMethod("foo"))));
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

    private static class DummyElement extends ModelElement {

        private final ModelElement parentElement;
        private final Method getterMethod;

        public DummyElement(ModelElement parentElement, String name, Method getterMethod) {
            super(name, null);
            this.parentElement = parentElement;
            this.getterMethod = getterMethod;
        }

        @Override
        protected String getMessageKey(DocumentationKind messageType) {
            return null;
        }

        @Override
        protected MessagesHelper getMessageHelper() {
            return null;
        }

    }

}
