/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IRuntimeRepositoryLookup;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.jaxb.ProductConfigurationXmlAdapter;
import org.faktorips.runtime.test.IpsTest2;
import org.faktorips.runtime.test.IpsTestCaseBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class AbstractRuntimeRepositoryMockTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractRuntimeRepository repositoryA;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractRuntimeRepository repositoryB;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractRuntimeRepository repositoryC;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractRuntimeRepository repositoryD;

    @Test
    public void testGetEnumValuesDefinedInType() {
        AbstractRuntimeRepository abstractRuntimeRepository = mock(AbstractRuntimeRepository.class, CALLS_REAL_METHODS);

        List<ExtensibleEnum> enumValues = abstractRuntimeRepository.getEnumValuesDefinedInType(ExtensibleEnum.class);

        assertEquals(ExtensibleEnum.VALUES, enumValues);
    }

    @Test
    public void testGetEnumValuesDefinedInType_JavaEnum() {
        AbstractRuntimeRepository abstractRuntimeRepository = mock(AbstractRuntimeRepository.class, CALLS_REAL_METHODS);

        List<RealEnum> enumValues = abstractRuntimeRepository.getEnumValuesDefinedInType(RealEnum.class);

        assertEquals(Arrays.asList(RealEnum.values()), enumValues);
    }

    @Test
    public void testGetEnumValuesReferencedContent() throws Exception {
        ExtensibleEnum myEnum = mock(ExtensibleEnum.class);
        List<ExtensibleEnum> list = new ArrayList<>();
        list.add(myEnum);
        initRepositoryReferences(repositoryA, repositoryB, repositoryC, repositoryD);
        doReturn(list).when(repositoryD).getEnumValuesInternal(ExtensibleEnum.class);
        List<ExtensibleEnum> expected = new ArrayList<>(ExtensibleEnum.VALUES);
        expected.addAll(list);

        List<ExtensibleEnum> enumValues = repositoryA.getEnumValues(ExtensibleEnum.class);

        assertEquals(expected, enumValues);
    }

    @Test
    public void testGetEnumValuesNoContent() throws Exception {
        initRepositoryReferences(repositoryA, repositoryB, repositoryC, repositoryD);
        List<ExtensibleEnum> expected = new ArrayList<>(ExtensibleEnum.VALUES);

        List<ExtensibleEnum> enumValues = repositoryA.getEnumValues(ExtensibleEnum.class);

        assertEquals(expected, enumValues);
    }

    @Test
    public void testGetAllModelTypeImplementationClasses() throws Exception {
        initRepositoryReferences(repositoryA, repositoryB);

        Set<String> modelTypeImplementationClasses = repositoryA.getAllModelTypeImplementationClasses();

        assertNotNull(modelTypeImplementationClasses);
        verify(repositoryA).getAllModelTypeImplementationClasses(anySet());
        verify(repositoryB).getAllModelTypeImplementationClasses(anySet());
    }

    private void initRepositoryReferences(AbstractRuntimeRepository referencingRepository,
            AbstractRuntimeRepository... referencedRepositories) throws Exception {
        Field declaredField = AbstractRuntimeRepository.class.getDeclaredField("repositories");
        declaredField.setAccessible(true);
        declaredField.set(referencingRepository, new ArrayList<IRuntimeRepository>());
        mockRepository(referencingRepository);

        for (AbstractRuntimeRepository referencedRepository : referencedRepositories) {
            declaredField.set(referencedRepository, new ArrayList<IRuntimeRepository>());
            referencingRepository.addDirectlyReferencedRepository(referencedRepository);
            mockRepository(referencedRepository);
        }

    }

    private void mockRepository(AbstractRuntimeRepository repository) {
        doReturn(null).when(repository).getEnumValueLookupService(ExtensibleEnum.class);
        doReturn(null).when(repository).getEnumValuesInternal(ExtensibleEnum.class);
        doNothing().when(repository).getAllModelTypeImplementationClasses(anySet());
    }

    @Test
    public void testSetGetRuntimeRepositoryLookup() {
        IRuntimeRepositoryLookup repositoryLookupMock = mock(IRuntimeRepositoryLookup.class);
        repositoryA.setRuntimeRepositoryLookup(repositoryLookupMock);

        IRuntimeRepositoryLookup runtimeRepositoryLookup = repositoryA.getRuntimeRepositoryLookup();

        assertSame(repositoryLookupMock, runtimeRepositoryLookup);
    }

    @Test
    public void testNewJAXBContext() throws JAXBException {
        AbstractRuntimeRepository repository = new TestAbstractRuntimeRepository(Foo.class);

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
        protected List<XmlAdapter<?, ?>> getAllInternalEnumXmlAdapters(IRuntimeRepository repository) {
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

    public static class ExtensibleEnum {

        public static final ExtensibleEnum VALUE1 = new ExtensibleEnum();

        public static final ExtensibleEnum VALUE2 = new ExtensibleEnum();

        public static final List<ExtensibleEnum> VALUES = Arrays.asList(VALUE1, VALUE2);

    }

    public enum RealEnum {
        FOO,
        BAR
    }

}
