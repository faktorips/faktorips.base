/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.Map;

import org.faktorips.runtime.AssociationChangedEvent;
import org.faktorips.runtime.DefaultObjectReferenceStore;
import org.faktorips.runtime.DefaultUnresolvedReference;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.INotificationSupport;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IUnresolvedReference;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.IpsPropertyChangeSupport;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ValidationContext;
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
        MessageList list = a.validate(new ValidationContext());
        assertEquals(0, list.getNoOfMessages());

        a.valid = false;
        list = a.validate(new ValidationContext());
        assertEquals(1, list.getNoOfMessages());
        assertEquals("A", list.getMessage(0).getCode());

        b.valid = false;
        list = a.validate(new ValidationContext());
        assertEquals(2, list.getNoOfMessages());
        assertEquals("A", list.getMessage(0).getCode());
        assertEquals("B", list.getMessage(1).getCode());

        a.valid = true;
        list = a.validate(new ValidationContext());
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
        pc.addPropertyChangeListener(listener);
        PropertyChangeEvent event = new PropertyChangeEvent(pc, "prop", 0, 1);
        pc.notifyChangeListeners(event);
        assertEquals(pc, listener.lastEvent.getSource());

        pc.removePropertyChangeListener(listener);
        pc.notifyChangeListeners(new PropertyChangeEvent(pc, "prop", 2, 3));
        assertEquals(event, listener.lastEvent);
    }

    private class ChangeListener implements PropertyChangeListener {

        PropertyChangeEvent lastEvent;

        /**
         * {@inheritDoc}
         */
        public void propertyChange(PropertyChangeEvent evt) {
            lastEvent = evt;
        }

    }

    private class PcA extends AbstractModelObject {

        protected boolean valid = true;
        protected PcB b;

        public PcA(PcB b) {
            this.b = b;
        }

        @Override
        protected void validateDependants(MessageList list, IValidationContext context) {
            b.validate(list, context);
        }

        @Override
        protected boolean validateSelf(MessageList list, IValidationContext context) {
            if (!valid) {
                list.add(Message.newError("A", "A is not valid!"));
            }
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void removeChildModelObjectInternal(IModelObject child) {
            // do nothing
        }
    }

    private class PcB extends AbstractModelObject implements INotificationSupport {

        protected boolean valid = true;

        protected PcB() {
            super();
        }

        @Override
        protected boolean validateSelf(MessageList list, IValidationContext context) {
            if (!valid) {
                list.add(Message.newError("B", "B is not valid!"));
            }
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void removeChildModelObjectInternal(IModelObject child) {
            // do nothing
        }

        /**
         * Helper object for Changelistener.
         * 
         * @generated
         */
        protected final IpsPropertyChangeSupport propertyChangeSupport = new IpsPropertyChangeSupport(this);

        /**
         * {@inheritDoc}
         * 
         * @generated
         */
        public void notifyChangeListeners(PropertyChangeEvent event) {
            if (event instanceof AssociationChangedEvent) {
                propertyChangeSupport.fireAssociationChange((AssociationChangedEvent)event);
            } else {
                propertyChangeSupport.firePropertyChange(event);
            }
        }

        /**
         * {@inheritDoc}
         * 
         * @generated
         */
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }

        /**
         * {@inheritDoc}
         * 
         * @generated
         */
        public void addPropertyChangeListener(PropertyChangeListener listener, boolean propagateEventsFromChildren) {
            propertyChangeSupport.addPropertyChangeListener(listener, propagateEventsFromChildren);
        }

        /**
         * {@inheritDoc}
         * 
         * @generated
         */
        public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
            propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
        }

        /**
         * {@inheritDoc}
         * 
         * @generated
         */
        public boolean hasListeners(String propertyName) {
            return propertyChangeSupport.hasListeners(propertyName);
        }

        /**
         * {@inheritDoc}
         * 
         * @generated
         */
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.removePropertyChangeListener(listener);
        }

        /**
         * {@inheritDoc}
         * 
         * @generated
         */
        public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
            propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
        }
    }

    class XmlPc extends AbstractModelObject {

        String prop0;
        String prop1;
        ChildXmlPc child;

        protected XmlPc() {
            super();
        }

        @Override
        protected void initPropertiesFromXml(Map<String, String> propMap, IRuntimeRepository productRepository) {
            if (propMap.containsKey("prop0")) {
                prop0 = propMap.get("prop0");
            }
            if (propMap.containsKey("prop1")) {
                prop1 = propMap.get("prop1");
            }
        }

        // used per reflection
        public void setChild(ChildXmlPc child) {
            this.child = child;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected AbstractModelObject createChildFromXml(Element childEl) {
            if ("Child".equals(childEl.getNodeName())) {
                child = new ChildXmlPc();
                child.setParent(this);
                return child;
            }
            return null;
        }

        // used per reflection
        public void setInsuredPerson(@SuppressWarnings("unused") PcA person) {
            // do nothing
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected IUnresolvedReference createUnresolvedReference(Object objectId, String targetRole, String targetId)
                throws SecurityException, NoSuchMethodException {
            if ("InsuredPerson".equals(targetRole)) {
                Method m = getClass().getMethod("setInsuredPerson", new Class[] { PcA.class });
                return new DefaultUnresolvedReference(this, objectId, m, PcA.class, targetId);
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void removeChildModelObjectInternal(IModelObject child) {
            // do nothing
        }

    }

    class ChildXmlPc extends AbstractModelObject {

        private XmlPc parent;

        String prop0;

        @Override
        protected void initPropertiesFromXml(Map<String, String> propMap, IRuntimeRepository productRepository) {
            if (propMap.containsKey("prop0")) {
                prop0 = propMap.get("prop0");
            }
        }

        public void setParent(XmlPc parent) {
            this.parent = parent;
        }

        // used per reflection
        public IModelObject getParentPolicyComponent() {
            return parent;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void removeChildModelObjectInternal(IModelObject child) {
            // do nothing
        }
    }
}
