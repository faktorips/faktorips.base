/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community)
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TimeZone;

import org.faktorips.runtime.AssociationChangedEvent;
import org.faktorips.runtime.DefaultObjectReferenceStore;
import org.faktorips.runtime.DefaultUnresolvedReference;
import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.INotificationSupport;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IUnresolvedReference;
import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.IpsPropertyChangeSupport;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.XmlAbstractTestCase;
import org.w3c.dom.Element;

/**
 * @author Jan Ortmann
 */
public class TestAbstractConfigurablePolicyComponentTest extends XmlAbstractTestCase {

    private InMemoryRuntimeRepository repository;
    private ProductComponent product;
    private ProductComponentGeneration productGen;
    private ProductComponent coverage;
    private ProductComponentGeneration coverageGen;

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        repository = new InMemoryRuntimeRepository();
        product = new TestProductComponent(repository, "TestProduct", "aKind", "aVersion");
        productGen = new TestProductCmptGeneration(product);
        productGen.setValidFrom(new DateTime(2000, 1, 1));
        coverage = new TestProductComponent(repository, "TestCoverage", "aKind", "aVersion");
        coverageGen = new TestProductCmptGeneration(coverage);
        coverageGen.setValidFrom(new DateTime(2000, 1, 1));
        repository.putProductCmptGeneration(productGen);
        repository.putProductCmptGeneration(coverageGen);
    }

    public void testGetProductComponent() {
        PcA a = new PcA(product, null);
        assertEquals(product, a.getProductComponent());
    }

    public void testValidate() {
        PcB b = new PcB(product);
        PcA a = new PcA(product, b);
        assertEquals(product, a.getProductComponent());

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
        pc.initFromXml(XmlUtil.getFirstElement(docEl), true, repository, store);
        assertEquals(product, pc.getProductComponent());
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
        pc.initFromXml(XmlUtil.getElement(docEl, "XmlPc", 1), false, repository, store);
        assertEquals("bla", pc.prop0); // make sure prop0 is not set to null
        // xml datat does not contain any information about prop0
    }

    public void testListenerMechanism() {
        PcB pc = new PcB(null);
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

    private class PcA extends DefaultConfigurableModelObject {

        protected boolean valid = true;
        protected PcB b;

        public PcA(IProductComponent pc, PcB b) {
            super(pc);
            this.b = b;
        }

        @Override
        protected void validateDependants(MessageList list, String businessFunction) {
            b.validate(list, businessFunction);
        }

        @Override
        protected boolean validateSelf(MessageList list, String businessFunction) {
            if (!valid) {
                list.add(Message.newError("A", "A is not valid!"));
            }
            return true;
        }
    }

    private class PcB extends DefaultConfigurableModelObject implements INotificationSupport {

        protected boolean valid = true;

        protected PcB(IProductComponent pc) {
            super(pc);
        }

        @Override
        protected boolean validateSelf(MessageList list, String businessFunction) {
            if (!valid) {
                list.add(Message.newError("B", "B is not valid!"));
            }
            return true;
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

    private class XmlPc extends AbstractConfigurableModelObject {

        GregorianCalendar effectiveDate;
        String prop0;
        String prop1;
        ChildXmlPc child;

        protected XmlPc() {
            super();
        }

        @Override
        protected void initPropertiesFromXml(Map<String, String> propMap) {
            if (propMap.containsKey("effectiveDate")) {
                effectiveDate = DateTime.parseIso(propMap.get("effectiveDate")).toGregorianCalendar(TimeZone.getDefault());
            }
            if (propMap.containsKey("prop0")) {
                prop0 = propMap.get("prop0");
            }
            if (propMap.containsKey("prop1")) {
                prop1 = propMap.get("prop1");
            }
        }

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

        public void setInsuredPerson(PcA person) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
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
        public Calendar getEffectiveFromAsCalendar() {
            return effectiveDate;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void removeChildModelObjectInternal(IModelObject child) {
        }

    }

    private class ChildXmlPc extends AbstractConfigurableModelObject implements DependantObject {

        private AbstractModelObject parent;
        String prop0;

        @Override
        protected void initPropertiesFromXml(Map<String, String> propMap) {
            if (propMap.containsKey("prop0")) {
                prop0 = propMap.get("prop0");
            }
        }

        public void setParent(XmlPc parent) {
            setParentModelObjectInternal(parent);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void initialize() {
            // test that the generation is accessible
            assertNotNull(getProductComponent().getGenerationBase(getEffectiveFromAsCalendar()));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void removeChildModelObjectInternal(IModelObject child) {
        }

        /**
         * {@inheritDoc}
         */
        public void setParentModelObjectInternal(AbstractModelObject newParent) {
            parent = newParent;
        }

        /**
         * {@inheritDoc}
         */
        public IModelObject getParentModelObject() {
            return parent;
        }

        /**
         * {@inheritDoc}
         */
        public Calendar getEffectiveFromAsCalendar() {
            return ((IConfigurableModelObject)parent).getEffectiveFromAsCalendar();
        }

    }

}
