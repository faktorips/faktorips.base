/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.internal.toc;

import static org.faktorips.runtime.internal.toc.ProductCmptTocEntry.PROPERTY_GENERATION_IMPL_CLASS_NAME;
import static org.faktorips.runtime.internal.toc.ProductCmptTocEntry.PROPERTY_KIND_ID;
import static org.faktorips.runtime.internal.toc.ProductCmptTocEntry.PROPERTY_VALID_TO;
import static org.faktorips.runtime.internal.toc.ProductCmptTocEntry.PROPERTY_VERSION_ID;
import static org.faktorips.runtime.internal.toc.TocEntry.PROPERTY_IMPLEMENTATION_CLASS;
import static org.faktorips.runtime.internal.toc.TocEntry.PROPERTY_XML_RESOURCE;
import static org.faktorips.runtime.internal.toc.TocEntryObject.PROPERTY_IPS_OBJECT_ID;
import static org.faktorips.runtime.internal.toc.TocEntryObject.PROPERTY_IPS_OBJECT_QNAME;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.runtime.internal.DateTime;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class AbstractTocEntryFactory<T extends TocEntryObject> implements ITocEntryFactory<T> {

    public T createFromXml(Element entryElement) {
        String ipsObjectId = entryElement.getAttribute(PROPERTY_IPS_OBJECT_ID);
        String ipsObjectQualifiedName = entryElement.getAttribute(PROPERTY_IPS_OBJECT_QNAME);
        String xmlResourceName = entryElement.getAttribute(PROPERTY_XML_RESOURCE);
        String implementationClassName = entryElement.getAttribute(PROPERTY_IMPLEMENTATION_CLASS);
        return createFromXmlInternal(entryElement, ipsObjectId, ipsObjectQualifiedName, xmlResourceName,
                implementationClassName);
    }

    abstract T createFromXmlInternal(Element entryElement,
            String ipsObjectId,
            String ipsObjectQualifiedName,
            String xmlResourceName,
            String implementationClassName);

    public static Set<ITocEntryFactory<?>> getBaseTocEntryFactories() {
        HashSet<ITocEntryFactory<?>> set = new HashSet<ITocEntryFactory<?>>();
        set.add(new ProductCmptTocEntryFactory());
        set.add(new TableContentTocEntryFactory());
        set.add(new TestCaseTocEntryFactory());
        set.add(new EnumContentTocEntryFactory());
        set.add(new EnumXmlAdapterTocEntryFactory());
        set.add(new FormulaTestTocEntryFactory());
        set.add(new ProductCmptTypeTocEntryFactory());
        set.add(new PolicyCmptTypeTocEntryFactory());
        return set;
    }

    public static class ProductCmptTocEntryFactory extends AbstractTocEntryFactory<ProductCmptTocEntry> {

        @Override
        ProductCmptTocEntry createFromXmlInternal(Element entryElement,
                String ipsObjectId,
                String ipsObjectQualifiedName,
                String xmlResourceName,
                String implementationClassName) {
            String generationImplClassName = entryElement.getAttribute(PROPERTY_GENERATION_IMPL_CLASS_NAME);

            DateTime validTo = DateTime.parseIso(entryElement.getAttribute(PROPERTY_VALID_TO));
            String kindId = entryElement.getAttribute(PROPERTY_KIND_ID);
            String versionId = entryElement.getAttribute(PROPERTY_VERSION_ID);

            ProductCmptTocEntry newEntry = new ProductCmptTocEntry(ipsObjectId, ipsObjectQualifiedName, kindId,
                    versionId, xmlResourceName, implementationClassName, generationImplClassName, validTo);

            NodeList nl = entryElement.getElementsByTagName(GenerationTocEntry.XML_TAG);
            List<GenerationTocEntry> generationEntries = new ArrayList<GenerationTocEntry>(nl.getLength());
            for (int i = 0; i < nl.getLength(); i++) {
                GenerationTocEntry entry = GenerationTocEntry.createFromXml(newEntry, (Element)nl.item(i));
                generationEntries.add(entry);
            }
            newEntry.setGenerationEntries(generationEntries);
            return newEntry;
        }

        public String getXmlTag() {
            return ProductCmptTocEntry.XML_TAG;
        }

    }

    public static class TableContentTocEntryFactory extends AbstractTocEntryFactory<TableContentTocEntry> {

        @Override
        TableContentTocEntry createFromXmlInternal(Element entryElement,
                String ipsObjectId,
                String ipsObjectQualifiedName,
                String xmlResourceName,
                String implementationClassName) {
            return new TableContentTocEntry(ipsObjectId, ipsObjectQualifiedName, xmlResourceName,
                    implementationClassName);
        }

        public String getXmlTag() {
            return TableContentTocEntry.XML_TAG;
        }

    }

    public static class TestCaseTocEntryFactory extends AbstractTocEntryFactory<TestCaseTocEntry> {

        @Override
        TestCaseTocEntry createFromXmlInternal(Element entryElement,
                String ipsObjectId,
                String ipsObjectQualifiedName,
                String xmlResourceName,
                String implementationClassName) {
            return new TestCaseTocEntry(ipsObjectId, ipsObjectQualifiedName, xmlResourceName, implementationClassName);
        }

        public String getXmlTag() {
            return TestCaseTocEntry.TEST_XML_TAG;
        }

    }

    public static class EnumContentTocEntryFactory extends AbstractTocEntryFactory<EnumContentTocEntry> {

        @Override
        EnumContentTocEntry createFromXmlInternal(Element entryElement,
                String ipsObjectId,
                String ipsObjectQualifiedName,
                String xmlResourceName,
                String implementationClassName) {
            return new EnumContentTocEntry(ipsObjectId, ipsObjectQualifiedName, xmlResourceName,
                    implementationClassName);
        }

        public String getXmlTag() {
            return EnumContentTocEntry.XML_TAG;
        }

    }

    public static class EnumXmlAdapterTocEntryFactory extends AbstractTocEntryFactory<EnumXmlAdapterTocEntry> {

        @Override
        EnumXmlAdapterTocEntry createFromXmlInternal(Element entryElement,
                String ipsObjectId,
                String ipsObjectQualifiedName,
                String xmlResourceName,
                String implementationClassName) {
            return new EnumXmlAdapterTocEntry(ipsObjectId, ipsObjectQualifiedName, implementationClassName);
        }

        public String getXmlTag() {
            return EnumXmlAdapterTocEntry.XML_TAG;
        }

    }

    public static class FormulaTestTocEntryFactory extends AbstractTocEntryFactory<FormulaTestTocEntry> {

        @Override
        FormulaTestTocEntry createFromXmlInternal(Element entryElement,
                String ipsObjectId,
                String ipsObjectQualifiedName,
                String xmlResourceName,
                String implementationClassName) {
            String kindId = entryElement.getAttribute(ProductCmptTocEntry.PROPERTY_KIND_ID);
            String versionId = entryElement.getAttribute(ProductCmptTocEntry.PROPERTY_VERSION_ID);

            return new FormulaTestTocEntry(ipsObjectId, ipsObjectQualifiedName, kindId, versionId,
                    implementationClassName);
        }

        public String getXmlTag() {
            return FormulaTestTocEntry.FORMULA_TEST_XML_TAG;
        }

    }

    public static class ProductCmptTypeTocEntryFactory extends AbstractTocEntryFactory<ProductCmptTypeTocEntry> {

        @Override
        ProductCmptTypeTocEntry createFromXmlInternal(Element entryElement,
                String ipsObjectId,
                String ipsObjectQualifiedName,
                String xmlResourceName,
                String implementationClassName) {
            return new ProductCmptTypeTocEntry(ipsObjectId, ipsObjectQualifiedName, xmlResourceName,
                    implementationClassName);
        }

        public String getXmlTag() {
            return ProductCmptTypeTocEntry.XML_TAG;
        }

    }

    public static class PolicyCmptTypeTocEntryFactory extends AbstractTocEntryFactory<PolicyCmptTypeTocEntry> {

        @Override
        PolicyCmptTypeTocEntry createFromXmlInternal(Element entryElement,
                String ipsObjectId,
                String ipsObjectQualifiedName,
                String xmlResourceName,
                String implementationClassName) {
            return new PolicyCmptTypeTocEntry(ipsObjectId, ipsObjectQualifiedName, xmlResourceName,
                    implementationClassName);
        }

        public String getXmlTag() {
            return PolicyCmptTypeTocEntry.XML_TAG;
        }

    }

}
