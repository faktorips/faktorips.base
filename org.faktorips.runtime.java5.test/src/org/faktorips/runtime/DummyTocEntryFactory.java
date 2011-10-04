/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime;

import org.faktorips.runtime.DummyTocEntryFactory.DummyTypedTocEntryObject;
import org.faktorips.runtime.internal.RuntimeObject;
import org.faktorips.runtime.internal.toc.ITocEntryFactory;
import org.faktorips.runtime.internal.toc.TocEntryObject;
import org.faktorips.runtime.internal.toc.TypedTocEntryObject;
import org.w3c.dom.Element;

public class DummyTocEntryFactory implements ITocEntryFactory<DummyTypedTocEntryObject> {

    @Override
    public DummyTypedTocEntryObject createFromXml(Element entryElement) {
        String ipsObjectName = entryElement.getAttribute(TocEntryObject.PROPERTY_IPS_OBJECT_QNAME);
        return new DummyTypedTocEntryObject(ipsObjectName);
    }

    @Override
    public String getXmlTag() {
        return DummyTypedTocEntryObject.DUMMY_RUNTIME_OBJECT;
    }

    public static class DummyRuntimeObject extends RuntimeObject {

    }

    public static class DummyTypedTocEntryObject extends TypedTocEntryObject<DummyRuntimeObject> {

        static final String DUMMY_RUNTIME_OBJECT = "DummyRuntimeObject";

        public DummyTypedTocEntryObject(String ipsObjectQualifiedName) {
            super(ipsObjectQualifiedName, "", "");
        }

        @Override
        public DummyRuntimeObject createRuntimeObject(IRuntimeRepository repository) {
            return new DummyRuntimeObject();
        }

        @Override
        public Class<DummyRuntimeObject> getRuntimeObjectClass() {
            return DummyRuntimeObject.class;
        }

        @Override
        public String getIpsObjectTypeId() {
            return "ProductCmpt";
        }

        @Override
        protected String getXmlElementTag() {
            return DUMMY_RUNTIME_OBJECT;
        }

    }
}
