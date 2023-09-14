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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Arrays;
import java.util.List;

import org.faktorips.runtime.ClassloaderRuntimeRepository;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.internal.productvariant.ProductVariantRuntimeHelper;
import org.faktorips.runtime.internal.toc.EnumContentTocEntry;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.TableContentTocEntry;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AbstractClassLoadingRuntimeRepositoryTest {

    private AbstractClassLoadingRuntimeRepository repo;
    private ProductComponent loadedProdCmpt;

    @Before
    public void setUp() throws Exception {
        loadedProdCmpt = mock(ProductComponent.class);
        repo = new ClassloaderRuntimeRepository(getClass().getClassLoader(), "",
                "org/faktorips/runtime/internal/ClassLoaderRuntimeRepoToc.xml");
        repo = spy(repo);
        doReturn(loadedProdCmpt).when(repo).getProductComponent(anyString());
        doNothing().when(repo).initialize();
    }

    @Test
    public void shouldLoadNormalProductComponentNormally() {
        ProductCmptTocEntry entry = mock(ProductCmptTocEntry.class, withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        when(entry.getIpsObjectQualifiedName()).thenReturn("qualified.Name");
        Element element = mock(Element.class);
        ProductComponent newProdCmptInstance = mock(ProductComponent.class);

        doReturn(element).when(repo).getDocumentElement(entry);
        when(element.hasAttribute(anyString())).thenReturn(false);
        doReturn(newProdCmptInstance).when(repo).createProductComponentInstance(anyString(), anyString(), anyString(),
                anyString());

        repo.createProductCmpt(entry);

        verify(newProdCmptInstance).initFromXml(element);
        verify(newProdCmptInstance).setQualifiedName("qualified.Name");
        verifyNoMoreInteractions(newProdCmptInstance);
    }

    @Test
    public void shouldLoadVariedProductComponent() {
        ProductCmptTocEntry entry = mock(ProductCmptTocEntry.class, withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        when(entry.getIpsObjectQualifiedName()).thenReturn("qualified.Name");
        Element element = mock(Element.class, withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        Element copyElement = mock(Element.class);
        Document docMock = mock(Document.class);
        ProductComponent newProdCmptInstance = mock(ProductComponent.class);

        when(element.getOwnerDocument()).thenReturn(docMock);
        when(docMock.cloneNode(anyBoolean())).thenReturn(docMock);
        doReturn(element).when(repo).getDocumentElement(entry);
        when(element.hasAttribute(anyString())).thenReturn(true);
        doReturn(copyElement).when(loadedProdCmpt).toXml(any(Document.class));
        doReturn(newProdCmptInstance).when(repo).createProductComponentInstance(anyString(), anyString(), anyString(),
                anyString());

        repo.createProductCmpt(entry);

        verify(newProdCmptInstance).initFromXml(element);
        verify(newProdCmptInstance).initFromXml(copyElement);
        verify(newProdCmptInstance).setQualifiedName("qualified.Name");
        verifyNoMoreInteractions(newProdCmptInstance);
    }

    @Test
    public void shouldLoadNormalGenerationNormally() {
        GenerationTocEntry genEntry = mock(GenerationTocEntry.class);
        ProductCmptTocEntry parentEntry = mock(ProductCmptTocEntry.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        Element element = mock(Element.class);
        Element prodCmptElement = mock(Element.class);
        ProductComponentGeneration gen = mock(ProductComponentGeneration.class);

        doReturn(parentEntry).when(genEntry).getParent();
        doReturn(element).when(repo).getDocumentElement(genEntry);
        doReturn(prodCmptElement).when(repo).getDocumentElement(parentEntry);
        when(prodCmptElement.hasAttribute(anyString())).thenReturn(false);
        doReturn(gen).when(repo).createProductComponentGenerationInstance(genEntry, loadedProdCmpt);

        repo.createProductCmptGeneration(genEntry);

        verify(gen).initFromXml(element);
    }

    @Test
    public void shouldLoadVariedGeneration() {
        GenerationTocEntry genEntry = mock(GenerationTocEntry.class);
        Element genElement = mock(Element.class);
        ProductVariantRuntimeHelper helper = mock(ProductVariantRuntimeHelper.class);

        doReturn(genElement).when(repo).getDocumentElement(genEntry);
        when(genElement.hasAttribute(anyString())).thenReturn(true);
        doReturn(helper).when(repo).getProductVariantHelper();

        when(helper.isProductVariantXML(genElement)).thenReturn(true);

        repo.createProductCmptGeneration(genEntry);

        verify(helper).initProductComponentGenerationVariation(repo, genEntry, genElement);
    }

    @Test
    public void testCreateEnumValues_NoXmlResourceName() throws Exception {
        EnumContentTocEntry tocEntry = mock(EnumContentTocEntry.class);
        // in a DetachedContentRuntimeRepository, the XML resource name will be empty, but an XML
        // input stream may be created nonetheless
        doReturn(EnumSaxHandlerTest.class.getClassLoader()
                .getResourceAsStream("org/faktorips/runtime/internal/EnumSaxHandlerTest.xml")).when(repo)
                        .getXmlAsStream(tocEntry);
        IpsEnum<TestEnum> enumContent = repo.createEnumValues(tocEntry, TestEnum.class);
        List<TestEnum> enumValues = enumContent.getEnums();
        assertThat(enumValues.size(), is(3));
        for (int i = 0; i < enumValues.size(); i++) {
            // repo.createEnumValues only creates extend values, the two static values are not
            // included, but the index must start with 2 instead of 0
            assertThat(enumValues.get(i).index, is(i + 2));
        }
    }

    @Test
    public void testCreateEnumValues_NoXml() throws Exception {
        EnumContentTocEntry tocEntry = mock(EnumContentTocEntry.class);

        IpsEnum<Object> enumContent = repo.createEnumValues(tocEntry, Object.class);

        assertThat(enumContent, is(nullValue()));
    }

    @Test
    public void testCreateEnumValues_Empty() throws Exception {
        EnumContentTocEntry tocEntry = mock(EnumContentTocEntry.class);
        when(tocEntry.getXmlResourceName()).thenReturn("org/faktorips/runtime/internal/EmptyEnum.xml");

        IpsEnum<Object> enumContent = repo.createEnumValues(tocEntry, Object.class);

        assertThat(enumContent.getEnums().isEmpty(), is(true));
    }

    @Test
    public void testCreateEnumValues_WithCorrectIndex() throws Exception {
        EnumContentTocEntry tocEntry = mock(EnumContentTocEntry.class);
        when(tocEntry.getXmlResourceName()).thenReturn("org/faktorips/runtime/internal/EnumSaxHandlerTest.xml");

        IpsEnum<TestEnum> enumContent = repo.createEnumValues(tocEntry, TestEnum.class);
        List<TestEnum> enumValues = enumContent.getEnums();

        assertThat(enumValues.size(), is(3));
        for (int i = 0; i < enumValues.size(); i++) {
            // repo.createEnumValues only creates extend values, the two static values are not
            // included, but the index must start with 2 instead of 0
            assertThat(enumValues.get(i).index, is(i + 2));
        }
    }

    @Test
    public void testCreateTable() {
        String ipsObjectId = "qualifiedTableName";
        String ipsObjectQualifiedName = "qualifiedTableName";
        String xmlResourceName = "org/faktorips/runtime/internal/TableTest.xml";
        String implementationClassName = "org.faktorips.runtime.internal.TestTable";
        TableContentTocEntry tocEntry = new TableContentTocEntry(ipsObjectId, ipsObjectQualifiedName, xmlResourceName,
                implementationClassName);

        ITable<?> table = repo.createTable(tocEntry);

        assertNotNull(table.getName());
        assertEquals(tocEntry.getIpsObjectId(), table.getName());
    }

    public static class TestEnum {

        public static final List<TestEnum> VALUES = Arrays.asList(new TestEnum(0, "A", "A", null),
                new TestEnum(1, "B", "B", null));

        private final int index;

        private final String id;

        private final String name;

        protected TestEnum(int index, String id, String name,
                @SuppressWarnings("unused") IRuntimeRepository runtimeRepository) {
            this.index = index;
            this.id = id;
            this.name = name;
        }

        public int getIndex() {
            return index;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "TestEnum [index=" + index + ", id=" + id + ", name=" + name + "]";
        }

    }

}
