package org.faktorips.runtime.internal;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.faktorips.runtime.DefaultCacheFactory;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.internal.toc.CustomTocEntryObject;
import org.faktorips.runtime.internal.toc.EnumContentTocEntry;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.IReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.TableContentTocEntry;
import org.faktorips.runtime.internal.toc.TestCaseTocEntry;
import org.faktorips.runtime.test.IpsTestCaseBase;
import org.junit.Test;

public class AbstractTocBasedRuntimeRepositoryTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testGetAllEnumContentClasses() throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        IReadonlyTableOfContents referencedToc = mock(IReadonlyTableOfContents.class);
        when(referencedToc.getEnumContentTocEntries()).thenReturn(
                Arrays.asList(new EnumContentTocEntry("enumC", "my.EnumC", "my.EnumC.xml", EnumC.class.getName())));
        AbstractTocBasedRuntimeRepository referencedRepository = new TestAbstractTocBasedRuntimeRepository("r2", cl, referencedToc);
        IReadonlyTableOfContents toc = mock(IReadonlyTableOfContents.class);
        when(toc.getEnumContentTocEntries()).thenReturn(
                Arrays.asList(new EnumContentTocEntry("enumA", "my.EnumA", "my.EnumA.xml", EnumA.class.getName()),
                        new EnumContentTocEntry("enumB", "my.EnumB", "my.EnumB.xml", EnumB.class.getName())));
        AbstractTocBasedRuntimeRepository repository = new TestAbstractTocBasedRuntimeRepository("r", cl, toc);
        repository.addDirectlyReferencedRepository(referencedRepository);

        List<Class<?>> allEnumContentClasses = repository.getAllEnumContentClasses();

        assertThat(allEnumContentClasses, hasItems(EnumA.class, EnumB.class, EnumC.class));
    }

    private static final class EnumA {

    }

    private static final class EnumB {

    }

    private static final class EnumC {

    }

    private static final class TestAbstractTocBasedRuntimeRepository extends AbstractTocBasedRuntimeRepository {

        private final IReadonlyTableOfContents toc;

        private TestAbstractTocBasedRuntimeRepository(String name, ClassLoader cl, IReadonlyTableOfContents toc) {
            super(name, new DefaultCacheFactory(cl), cl);
            this.toc = toc;
            initialize();
        }

        @Override
        public boolean isModifiable() {
            return false;
        }

        @Override
        protected IReadonlyTableOfContents loadTableOfContents() {
            return toc;
        }

        @Override
        protected IpsTestCaseBase createTestCase(TestCaseTocEntry tocEntry, IRuntimeRepository runtimeRepository) {
            return null;
        }

        @Override
        protected ITable<?> createTable(TableContentTocEntry tocEntry) {
            return null;
        }

        @Override
        protected IProductComponentGeneration createProductCmptGeneration(GenerationTocEntry generationTocEntry) {
            return null;
        }

        @Override
        protected IProductComponent createProductCmpt(ProductCmptTocEntry tocEntry) {
            return null;
        }

        @Override
        protected <T> List<T> createEnumValues(EnumContentTocEntry tocEntry, Class<T> clazz) {
            return null;
        }

        @Override
        protected <T> T createCustomObject(CustomTocEntryObject<T> tocEntry) {
            return null;
        }
    }

}
