/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.lang.reflect.Method;
import java.util.HashMap;

import org.faktorips.runtime.DefaultObjectReferenceStore;
import org.faktorips.runtime.DefaultUnresolvedReference;
import org.faktorips.runtime.IModelObjectChangeListener;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IUnresolvedReference;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.IModelObjectChangedEvent;
import org.faktorips.runtime.XmlAbstractTestCase;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class TestAbstractPolicyComponentTest extends XmlAbstractTestCase {

    
    public void testValidate() {
        PcB b = new PcB();
        PcA a = new PcA(b);
        
        a.valid = true;
        b.valid = true;
        MessageList list = a.validate(null);
        assertEquals(0, list.getNoOfMessages());
        
        a.valid = false;
        list = a.validate(null);
        assertEquals(1, list.getNoOfMessages());
        assertEquals("A", list.getMessage(0).getCode());
        
        b.valid = false;
        list = a.validate(null);
        assertEquals(2, list.getNoOfMessages());
        assertEquals("A", list.getMessage(0).getCode());
        assertEquals("B", list.getMessage(1).getCode());

        a.valid = true;
        list = a.validate(null);
        assertEquals(1, list.getNoOfMessages());
        assertEquals("B", list.getMessage(0).getCode());
    }
    
    public void testInitFromXml() {
        DefaultObjectReferenceStore store = new DefaultObjectReferenceStore();
        XmlPc pc = new XmlPc();
        pc.prop0 = "";
        pc.prop1 = "";
        Element docEl = getTestDocument().getDocumentElement(); 
        pc.initFromXml(XmlUtil.getFirstElement(docEl), true, null, store);
        assertEquals("blabla", pc.prop0);
        assertNull(pc.prop1);
        assertNotNull(pc.child);
        assertEquals("hello world", pc.child.prop0);
        assertEquals(pc, store.getObject(pc.getClass(), "1"));
        assertEquals(pc.child, store.getObject(pc.child.getClass(), "2"));
        assertEquals(1, store.getAllUnresolvedReferences().size());
    }

    public void testInitFromXml_KeepObjectValueIfNotSpecifiedInXml() {
        Element docEl = getTestDocument().getDocumentElement(); 
        DefaultObjectReferenceStore store = new DefaultObjectReferenceStore();
        XmlPc pc = new XmlPc();
        pc.prop0 = "bla";
        pc.initFromXml(XmlUtil.getElement(docEl, "XmlPc", 1), false, null, store);
        assertEquals("bla", pc.prop0); // make sure prop0 is not set to null
        // xml datat does not contain any information about prop0
    }
    
    public void testListenerMechanism() {
        PcB pc = new PcB();
        ChangeListener listener = new ChangeListener();
        pc.addChangeListener(listener);
        IModelObjectChangedEvent event = new ModelObjectChangedEvent(pc, 0, "prop");
        pc.notifyChangeListeners(event);
        assertEquals(pc, listener.lastEvent.getChangedObject());

        pc.removeChangeListener(listener);
        pc.notifyChangeListeners(new ModelObjectChangedEvent(pc, 0, "prop"));
        assertEquals(event, listener.lastEvent);
    }
    
    private class ChangeListener implements IModelObjectChangeListener {

        IModelObjectChangedEvent lastEvent;
        
        /**
         * {@inheritDoc}
         */
        public void modelObjectChanged(IModelObjectChangedEvent event) {
            lastEvent = event;
        }
        
    }
        
    private class PcA extends AbstractModelObject {

        protected boolean valid = true;
        protected PcB b;

        public PcA(PcB b) {
            this.b = b;
        }

        protected void validateDependants(MessageList list, String businessFunction) {
            b.validate(list, businessFunction);
        }

        protected boolean validateSelf(MessageList list, String businessFunction) {
            if (!valid) {
                list.add(Message.newError("A", "A is not valid!"));
            }
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public void removeChildModelObjectInternal(IModelObject child) {
        }
    }
    
    private class PcB extends AbstractModelObject {
        
        protected boolean valid = true;

        protected PcB() {
            super();
        }
        
        protected boolean validateSelf(MessageList list, String businessFunction) {
            if (!valid) {
                list.add(Message.newError("B", "B is not valid!"));
            }
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public void removeChildModelObjectInternal(IModelObject child) {
        }
    }
    
    private class XmlPc extends AbstractModelObject {
        
        String prop0;
        String prop1;
        ChildXmlPc child;

        protected XmlPc() {
            super();
        }

        protected void initPropertiesFromXml(HashMap propMap) {
            if (propMap.containsKey("prop0")) {
                prop0 = (String)propMap.get("prop0");
            }
            if (propMap.containsKey("prop1")) {
                prop1 = (String)propMap.get("prop1");
            }
        }
        
        public void setChild(ChildXmlPc child) {
            this.child = child;
        }

        /**
         * {@inheritDoc}
         */
        protected AbstractModelObject createChildFromXml(Element childEl) {
            if ("Child".equals(childEl.getNodeName())) {
                child = new ChildXmlPc();
                child.setParent(this);
                return child;
            }
            return null;
        }
        
        public void setInsuredPerson(PcA person) {
        }

        /**
         * {@inheritDoc}
         */
        protected IUnresolvedReference createUnresolvedReference(Object objectId, String targetRole, String targetId) throws SecurityException, NoSuchMethodException {
            if ("InsuredPerson".equals(targetRole)) {
                Method m = getClass().getMethod("setInsuredPerson", new Class[]{PcA.class});
                return new DefaultUnresolvedReference(this, objectId, m, PcA.class, targetId);
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public void removeChildModelObjectInternal(IModelObject child) {
        }

    }
    
    private class ChildXmlPc extends AbstractModelObject {

        private XmlPc parent;
        
        String prop0;

        protected void initPropertiesFromXml(HashMap propMap) {
            if (propMap.containsKey("prop0")) {
                prop0 = (String)propMap.get("prop0");
            }
        }
        
        public void setParent(XmlPc parent) {
            this.parent = parent;
        }

        /**
         * {@inheritDoc}
         */
        public IModelObject getParentPolicyComponent() {
            return parent;
        }

        /**
         * {@inheritDoc}
         */
        public void removeChildModelObjectInternal(IModelObject child) {
        }        
    }
}
