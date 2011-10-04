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

package org.faktorips.devtools.stdbuilder;

import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.stdbuilder.MyDummyTocEntryFactory.MyDummyTypedTocEntryObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.RuntimeObject;
import org.faktorips.runtime.internal.toc.ITocEntryFactory;
import org.faktorips.runtime.internal.toc.TocEntryObject;
import org.faktorips.runtime.internal.toc.TypedTocEntryObject;
import org.w3c.dom.Element;

public class MyDummyTocEntryFactory implements ITocEntryFactory<MyDummyTypedTocEntryObject> {

    @Override
    public MyDummyTypedTocEntryObject createFromXml(Element entryElement) {
        String ipsObjectName = entryElement.getAttribute(TocEntryObject.PROPERTY_IPS_OBJECT_QNAME);
        return new MyDummyTypedTocEntryObject(ipsObjectName);
    }

    @Override
    public String getXmlTag() {
        return MyDummyTypedTocEntryObject.DUMMY_RUNTIME_OBJECT;
    }

    public static class MyDummyTypedTocEntryObject extends TypedTocEntryObject<MyDummyRuntimeObject> {

        static final String DUMMY_RUNTIME_OBJECT = "MyDummyRuntimeObject";

        protected MyDummyTypedTocEntryObject(String ipsObjectQualifiedName) {
            super(ipsObjectQualifiedName, "", "");
        }

        @Override
        public MyDummyRuntimeObject createRuntimeObject(IRuntimeRepository repository) {
            return new MyDummyRuntimeObject();
        }

        @Override
        public Class<MyDummyRuntimeObject> getRuntimeObjectClass() {
            return MyDummyRuntimeObject.class;
        }

        @Override
        public String getIpsObjectTypeId() {
            return IpsObjectType.PRODUCT_CMPT.getId();
        }

        @Override
        protected String getXmlElementTag() {
            return DUMMY_RUNTIME_OBJECT;
        }

    }

    public static class MyDummyRuntimeObject extends RuntimeObject {

    }
}
