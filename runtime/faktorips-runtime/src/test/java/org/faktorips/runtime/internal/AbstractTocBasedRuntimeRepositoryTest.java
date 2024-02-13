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

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AbstractTocBasedRuntimeRepositoryTest {

    @Test
    public void testGetAllEnumClasses() throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        IReadonlyTableOfContents referencedToc = mock(IReadonlyTableOfContents.class);
        when(referencedToc.getEnumContentTocEntries()).thenReturn(
                List.of(new EnumContentTocEntry("enumC", "my.EnumC", "my.EnumC.xml", EnumC.class.getName()),
                        // Entry for Type with content in product repository
                        new EnumContentTocEntry("enumA", "my.EnumA.Type", "", EnumC.class.getName()),
                        // Entry for Java Enum
                        new EnumContentTocEntry("enumE", "my.EnumE.Type", "", RealEnum.class.getName())));
        AbstractTocBasedRuntimeRepository referencedRepository = new TestAbstractTocBasedRuntimeRepository("r2", cl,
                referencedToc);
        IReadonlyTableOfContents toc = mock(IReadonlyTableOfContents.class);
        when(toc.getEnumContentTocEntries()).thenReturn(
                List.of(new EnumContentTocEntry("enumA", "my.EnumA", "my.EnumA.xml", EnumA.class.getName()),
                        new EnumContentTocEntry("enumB", "my.EnumB", "my.EnumB.xml", EnumB.class.getName())));
        AbstractTocBasedRuntimeRepository repository = new TestAbstractTocBasedRuntimeRepository("r", cl, toc);
        repository.addDirectlyReferencedRepository(referencedRepository);

        List<Class<?>> allEnumContentClasses = repository.getAllEnumClasses();

        assertThat(allEnumContentClasses, hasItems(EnumA.class, EnumB.class, EnumC.class, RealEnum.class));
    }

    @Test
    public void testGetAllTableIds() throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        IReadonlyTableOfContents toc = mock(IReadonlyTableOfContents.class);
        when(toc.getTableTocEntries()).thenReturn(
                List.of(new TableContentTocEntry("t1", "my.Table1", "my.Table1.ipstablecontent",
                        TestTable.class.getName()),
                        new TableContentTocEntry("t2", "my.Table2", "my.Table2.ipstablecontent",
                                TestTable.class.getName())));
        AbstractTocBasedRuntimeRepository repository = new TestAbstractTocBasedRuntimeRepository("r", cl, toc);

        List<String> allTableIds = repository.getAllTableIds();

        assertThat(allTableIds, hasItems("t1", "t2"));
    }

    private static final class EnumA {
        // an enum
    }

    private static final class EnumB {
        // another enum
    }

    private static final class EnumC {
        // yet another enum
    }

    private enum RealEnum {
        E
    }

    private static final class TestTable implements ITable<Void> {
        @Override
        public String getName() {
            return "qName";
        }

        @Override
        public List<Void> getAllRows() {
            return List.of();
        }

        @Override
        public Element toXml(Document document) {
            return null;
        }
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
        protected <T> IpsEnum<T> createEnumValues(EnumContentTocEntry tocEntry, Class<T> clazz) {
            return null;
        }

        @Override
        protected <T> T createCustomObject(CustomTocEntryObject<T> tocEntry) {
            return null;
        }
    }

}
