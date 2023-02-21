/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.xml.jakarta3;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.internal.AbstractModelObject;
import org.faktorips.runtime.internal.AbstractRuntimeRepository;
import org.faktorips.runtime.test.IpsTest2;
import org.faktorips.runtime.test.IpsTestCaseBase;
import org.faktorips.runtime.xml.IIpsXmlAdapter;
import org.faktorips.runtime.xml.jakarta.ProductConfigurationXmlAdapter;
import org.junit.Test;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlRootElement;

public class AbstractRuntimeRepositoryJaxbTest {

    @Test
    public void testNewJAXBContext() throws JAXBException {
        AbstractRuntimeRepository repository = new TestAbstractRuntimeRepository(Foo.class);

        @SuppressWarnings("deprecation")
        JAXBContext jaxbContext = repository.newJAXBContext();
        Foo foo = new Foo();
        foo.setX(42);
        StringWriter writer = new StringWriter();
        jaxbContext.createMarshaller().marshal(foo, writer);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setAdapter(new ProductConfigurationXmlAdapter(repository));
        String xml = writer.toString();
        System.out.println(xml);
        Foo foo2 = (Foo)unmarshaller.unmarshal(new StringReader(xml));
        assertThat(foo2.getX(), is(foo.getX()));
        assertThat(foo2.getMyId(), is(foo.getMyId()));
    }

    @Test
    public void testNewJAXBContext_SuperSuperClass() {
        AbstractRuntimeRepository repository = new TestAbstractRuntimeRepository(FooSuppe2.class);

        @SuppressWarnings("deprecation")
        JAXBContext jaxbContext = repository.newJAXBContext();

        assertNotNull(jaxbContext);
    }

    private static final class TestAbstractRuntimeRepository extends AbstractRuntimeRepository {
        private final Class<? extends IModelObject> modelObjectClass;

        private TestAbstractRuntimeRepository(Class<? extends IModelObject> modelObjectClass) {
            super("Repository with only" + modelObjectClass.getName());
            this.modelObjectClass = modelObjectClass;
        }

        @Override
        public boolean isModifiable() {
            return false;
        }

        @Override
        protected ITable<?> getTableInternal(String qualifiedTableName) {
            return null;
        }

        @Override
        protected <T extends ITable<?>> T getTableInternal(Class<T> tableClass) {
            return null;
        }

        @Override
        protected IProductComponent getProductComponentInternal(String kindId, String versionId) {
            return null;
        }

        @Override
        protected IProductComponent getProductComponentInternal(String id) {
            return null;
        }

        @Override
        public void getProductComponentGenerations(IProductComponent productCmpt,
                List<IProductComponentGeneration> result) {
            // don't care
        }

        @Override
        protected IProductComponentGeneration getProductComponentGenerationInternal(String id, Calendar effectiveDate) {
            return null;
        }

        @Override
        protected IProductComponentGeneration getPreviousProductComponentGenerationInternal(
                IProductComponentGeneration generation) {
            return null;
        }

        @Override
        protected int getNumberOfProductComponentGenerationsInternal(IProductComponent productCmpt) {
            return 0;
        }

        @Override
        protected IProductComponentGeneration getNextProductComponentGenerationInternal(
                IProductComponentGeneration generation) {
            return null;
        }

        @Override
        protected IProductComponentGeneration getLatestProductComponentGenerationInternal(
                IProductComponent productCmpt) {
            return null;
        }

        @Override
        protected void getIpsTestCasesStartingWith(String qNamePrefix,
                List<IpsTest2> result,
                IRuntimeRepository runtimeRepository) {
            // don't care
        }

        @Override
        protected IpsTestCaseBase getIpsTestCaseInternal(String qName, IRuntimeRepository runtimeRepository) {
            return null;
        }

        @Override
        protected <T> List<T> getEnumValuesInternal(Class<T> clazz) {
            return Collections.emptyList();
        }

        @Override
        protected <T> T getCustomRuntimeObjectInternal(Class<T> type, String ipsObjectQualifiedName) {
            return null;
        }

        @Override
        protected void getAllTables(List<ITable<?>> result) {
            // don't care
        }

        @Override
        protected void getAllProductComponents(List<IProductComponent> result) {
            // don't care
        }

        @Override
        protected void getAllProductComponents(String kindId, List<IProductComponent> result) {
            // don't care
        }

        @Override
        protected void getAllProductComponentIds(List<String> result) {
            // don't care
        }

        @Override
        protected void getAllModelTypeImplementationClasses(Set<String> result) {
            result.add(modelObjectClass.getName());
        }

        @Override
        protected void getAllIpsTestCases(List<IpsTest2> result, IRuntimeRepository runtimeRepository) {
            // don't care
        }

        @Override
        protected List<IIpsXmlAdapter<?, ?>> getAllInternalEnumXmlAdapters(IRuntimeRepository repository) {
            return Collections.emptyList();
        }

        @Override
        protected void getAllEnumClasses(LinkedHashSet<Class<?>> result) {
            // don't care
        }
    }

    public static class FooSuppe2 extends FooSuppe {
        // another class
    }

    public static class FooSuppe extends Foo {
        // another class
    }

    @XmlRootElement(name = "Foo")
    public static class Foo extends Bar {

        @XmlAttribute
        private int x;

        protected int getX() {
            return x;
        }

        protected void setX(int x) {
            this.x = x;
        }

    }

    @XmlAccessorType(XmlAccessType.NONE)
    @XmlRootElement
    public static class Bar extends AbstractModelObject {

        /** Uniquely identifies this model object within the object graph it belongs to. */
        @XmlAttribute(name = "myId")
        @XmlID
        private String myId = UUID.randomUUID().toString();

        public String getMyId() {
            return myId;
        }

        public void setMyId(String myId) {
            this.myId = myId;
        }

    }

}
