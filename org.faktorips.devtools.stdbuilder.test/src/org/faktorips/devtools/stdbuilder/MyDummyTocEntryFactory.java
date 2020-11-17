/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.stdbuilder.MyDummyTocEntryFactory.MyDummyTypedTocEntryObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.RuntimeObject;
import org.faktorips.runtime.internal.toc.CustomTocEntryObject;
import org.faktorips.runtime.internal.toc.ITocEntryFactory;
import org.faktorips.runtime.internal.toc.TocEntryObject;
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

    public static class MyDummyTypedTocEntryObject extends CustomTocEntryObject<MyDummyRuntimeObject> {

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
