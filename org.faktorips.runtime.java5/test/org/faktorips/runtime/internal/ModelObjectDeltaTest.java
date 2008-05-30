/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.runtime.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.faktorips.runtime.IDeltaComputationOptions;
import org.faktorips.runtime.IDeltaSupport;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IModelObjectChangeListener;
import org.faktorips.runtime.IModelObjectDelta;
import org.faktorips.runtime.IModelObjectDeltaVisitor;
import org.faktorips.runtime.MessageList;

/**
 * 
 * @author Jan Ortmann
 */
public class ModelObjectDeltaTest extends TestCase {

    private MyModelObject objectA = new MyModelObject("A");
    private MyModelObject objectB = new MyModelObject("B");
    
    public void testNewEmptyDelta() {
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        assertTrue(delta.isEmpty());
        assertFalse(delta.isChanged());
        assertFalse(delta.isPropertyChanged());
        assertFalse(delta.isStructureChanged());
        assertFalse(delta.isChildChanged());
        assertEquals(IModelObjectDelta.EMPTY, delta.getKind());
        assertEquals(IModelObjectDelta.EMPTY, delta.getKindOfChange());
        assertSame(objectA, delta.getOriginalObject());
        assertSame(objectB, delta.getReferenceObject());
    }
    
    public void testNewAddDelta() {
        ModelObjectDelta delta = ModelObjectDelta.newAddDelta(objectB, "childs");
        assertFalse(delta.isEmpty());
        assertFalse(delta.isChanged());
        assertFalse(delta.isPropertyChanged());
        assertFalse(delta.isStructureChanged());
        assertFalse(delta.isChildChanged());
        assertEquals(IModelObjectDelta.ADDED, delta.getKind());
        assertEquals(IModelObjectDelta.EMPTY, delta.getKindOfChange());
        assertNull(delta.getOriginalObject());
        assertSame(objectB, delta.getReferenceObject());
    }
    
    public void testNewRemoveDelta() {
        ModelObjectDelta delta = ModelObjectDelta.newRemoveDelta(objectA, "childs");
        assertFalse(delta.isEmpty());
        assertFalse(delta.isChanged());
        assertFalse(delta.isPropertyChanged());
        assertFalse(delta.isStructureChanged());
        assertFalse(delta.isChildChanged());
        assertEquals(IModelObjectDelta.REMOVED, delta.getKind());
        assertEquals(IModelObjectDelta.EMPTY, delta.getKindOfChange());
        assertSame(objectA, delta.getOriginalObject());
        assertNull(delta.getReferenceObject());
    }
    
    public void testMarkPropertyChanged() {
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        delta.markPropertyChanged("property");
        assertTrue(delta.isChanged());
        assertTrue(delta.isPropertyChanged());
        assertFalse(delta.isStructureChanged());
        assertFalse(delta.isChildChanged());
        
        String[] props = delta.getChangedProperties();
        assertEquals(1, props.length);
        assertEquals("property", props[0]);
        
        delta.markPropertyChanged("property2");
        props = delta.getChangedProperties();
        assertEquals(2, props.length);
        Arrays.sort(props);
        assertEquals("property", props[0]);
        assertEquals("property2", props[1]);
        
        delta.markPropertyChanged("property2");
        props = delta.getChangedProperties();
        Arrays.sort(props);
        assertEquals(2, props.length);
        assertEquals("property", props[0]);
        assertEquals("property2", props[1]);
    }

    public void testAddChildDelta_EmptyDelta() {
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        ModelObjectDelta childDelta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        delta.addChildDelta(childDelta);
        assertFalse(delta.isChanged());
        assertFalse(delta.isPropertyChanged());
        assertFalse(delta.isStructureChanged());
        assertFalse(delta.isChildChanged());
        assertEquals(0, delta.getChildDeltas().length);
    }

    public void testAddChildDelta_PropertyChangeDelta() {
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        ModelObjectDelta childDelta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        childDelta.markPropertyChanged("property");
        delta.addChildDelta(childDelta);
        assertTrue(delta.isChanged());
        assertFalse(delta.isPropertyChanged());
        assertFalse(delta.isStructureChanged());
        assertTrue(delta.isChildChanged());
        assertEquals(1, delta.getChildDeltas().length);
        assertSame(childDelta, delta.getChildDeltas()[0]);
    }

    public void testAddChildDelta_AddDelta() {
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        ModelObjectDelta childDelta = ModelObjectDelta.newAddDelta(objectB, "childs");
        delta.addChildDelta(childDelta);
        assertTrue(delta.isChanged());
        assertFalse(delta.isPropertyChanged());
        assertTrue(delta.isStructureChanged());
        assertFalse(delta.isChildChanged());
        assertEquals(1, delta.getChildDeltas().length);
        assertSame(childDelta, delta.getChildDeltas()[0]);
    }

    public void testAddChildDelta_RemoveDelta() {
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        ModelObjectDelta childDelta = ModelObjectDelta.newRemoveDelta(objectA, "childs");
        delta.addChildDelta(childDelta);
        assertTrue(delta.isChanged());
        assertFalse(delta.isPropertyChanged());
        assertTrue(delta.isStructureChanged());
        assertFalse(delta.isChildChanged());
        assertEquals(1, delta.getChildDeltas().length);
        assertSame(childDelta, delta.getChildDeltas()[0]);
    }
    
    public void testCreateChildDeltas_to1Association_ComputationByObject_ObjectUnchanged() {
        IDeltaComputationOptions options = new Options(IDeltaComputationOptions.COMPUTE_BY_OBJECT);
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        
        //note: objectsA and B are used as parent and child objects (not realistic but of no harm for this test)
        MyModelObject objectA1 = new MyModelObject("A");
        ModelObjectDelta.createChildDeltas(delta, objectA, objectA1, "childs", options);
        IModelObjectDelta[] childDeltas = delta.getChildDeltas();
        assertEquals(0, childDeltas.length);
    }

    public void testCreateChildDeltas_to1Association_ComputationByObject_Added() {
        IDeltaComputationOptions options = new Options(IDeltaComputationOptions.COMPUTE_BY_OBJECT);
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        
        //note: objectsA and B are used as parent and child objects (not realistic but of no harm for this test)
        ModelObjectDelta.createChildDeltas(delta, null, objectB, "childs", options);
        IModelObjectDelta[] childDeltas = delta.getChildDeltas();
        assertEquals(1, childDeltas.length);
        assertTrue(childDeltas[0].isAdded());
        assertEquals(objectB, childDeltas[0].getReferenceObject());
    }

    public void testCreateChildDeltas_to1Association_ComputationByObject_Removed() {
        IDeltaComputationOptions options = new Options(IDeltaComputationOptions.COMPUTE_BY_OBJECT);
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        
        //note: objectsA and B are used as parent and child objects (not realistic but of no harm for this test)
        ModelObjectDelta.createChildDeltas(delta, objectA, null, "childs", options);
        IModelObjectDelta[] childDeltas = delta.getChildDeltas();
        assertEquals(1, childDeltas.length);
        assertTrue(childDeltas[0].isRemoved());
        assertEquals(objectA, childDeltas[0].getOriginalObject());
    }

    public void testCreateChildDeltas_to1Association_ComputationByObject_RemovedAdded() {
        IDeltaComputationOptions options = new Options(IDeltaComputationOptions.COMPUTE_BY_OBJECT);
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        
        //note: objectsA and B are used as parent and child objects (not realistic but of no harm for this test)
        ModelObjectDelta.createChildDeltas(delta, objectA, objectB, "childs", options);
        IModelObjectDelta[] childDeltas = delta.getChildDeltas();
        assertEquals(2, childDeltas.length);
        assertTrue(childDeltas[0].isRemoved());
        assertEquals(objectA, childDeltas[0].getOriginalObject());
        assertTrue(childDeltas[1].isAdded());
        assertEquals(objectB, childDeltas[1].getReferenceObject());
    }

    public void testCreateChildDeltas_to1Association_ComputationByObject_ObjectHasChanged() {
        IDeltaComputationOptions options = new Options(IDeltaComputationOptions.COMPUTE_BY_OBJECT);
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        
        //note: objectsA and B are used as parent and child objects (not realistic but of no harm for this test)
        MyModelObject objectA1 = new MyModelObject("A");
        objectA1.setProperty(42);
        ModelObjectDelta.createChildDeltas(delta, objectA, objectA1, "childs", options);
        IModelObjectDelta[] childDeltas = delta.getChildDeltas();
        assertEquals(1, childDeltas.length);
        assertTrue(childDeltas[0].isChanged());
        assertEquals(objectA, childDeltas[0].getOriginalObject());
        assertEquals(objectA1, childDeltas[0].getReferenceObject());
    }

    public void testCreateChildDeltas_to1Association_ComputationByPositionAdded() {
        IDeltaComputationOptions options = new Options(IDeltaComputationOptions.COMPUTE_BY_POSITION);
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        
        //note: objectsA and B are used as parent and child objects (not realistic but of no harm for this test)
        ModelObjectDelta.createChildDeltas(delta, null, objectB, "childs", options);
        IModelObjectDelta[] childDeltas = delta.getChildDeltas();
        assertEquals(1, childDeltas.length);
        assertTrue(childDeltas[0].isAdded());
        assertEquals(objectB, childDeltas[0].getReferenceObject());
    }

    public void testCreateChildDeltas_to1Association_ComputationByPosition_Removed() {
        IDeltaComputationOptions options = new Options(IDeltaComputationOptions.COMPUTE_BY_POSITION);
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        
        //note: objectsA and B are used as parent and child objects (not realistic but of no harm for this test)
        ModelObjectDelta.createChildDeltas(delta, objectA, null, "childs", options);
        IModelObjectDelta[] childDeltas = delta.getChildDeltas();
        assertEquals(1, childDeltas.length);
        assertTrue(childDeltas[0].isRemoved());
        assertEquals(objectA, childDeltas[0].getOriginalObject());
    }

    public void testCreateChildDeltas_to1Association_ComputationByPosition_RemovedAdded() {
        IDeltaComputationOptions options = new Options(IDeltaComputationOptions.COMPUTE_BY_POSITION);
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        
        //note: objectsA and B are used as parent and child objects (not realistic but of no harm for this test)
        ModelObjectDelta.createChildDeltas(delta, objectA, objectB, "childs", options);
        IModelObjectDelta[] childDeltas = delta.getChildDeltas();
        assertEquals(1, childDeltas.length);
        assertEquals(IModelObjectDelta.DIFFERENT_OBJECT_AT_POSITION, childDeltas[0].getKind());
        assertEquals(objectA, childDeltas[0].getOriginalObject());
        assertEquals(objectB, childDeltas[0].getReferenceObject());
    }
    
    public void testCreateChildDeltas_toManyAssociation_ComputationByObject_Added() {
        IDeltaComputationOptions options = new Options(IDeltaComputationOptions.COMPUTE_BY_OBJECT);
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        
        //note: objectsA and B are used as parent and child objects (not realistic but of no harm for this test)
        List newChilds = new ArrayList();
        newChilds.add(objectA);
        newChilds.add(objectB);
        ModelObjectDelta.createChildDeltas(delta, new ArrayList(), newChilds, "childs", options);
        IModelObjectDelta[] childDeltas = delta.getChildDeltas();
        assertEquals(2, childDeltas.length);
        assertTrue(childDeltas[0].isAdded());
        assertEquals(objectA, childDeltas[0].getReferenceObject());
        assertTrue(childDeltas[1].isAdded());
        assertEquals(objectB, childDeltas[1].getReferenceObject());
    }

    public void testCreateChildDeltas_toManyAssociation_ComputationByObject_Removed() {
        IDeltaComputationOptions options = new Options(IDeltaComputationOptions.COMPUTE_BY_OBJECT);
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        
        //note: objectsA and B are used as parent and child objects (not realistic but of no harm for this test)
        List removedChilds = new ArrayList();
        removedChilds.add(objectA);
        removedChilds.add(objectB);
        ModelObjectDelta.createChildDeltas(delta, removedChilds, new ArrayList(), "childs", options);
        IModelObjectDelta[] childDeltas = delta.getChildDeltas();
        assertEquals(2, childDeltas.length);
        assertTrue(childDeltas[0].isRemoved());
        assertEquals(objectA, childDeltas[0].getOriginalObject());
        assertTrue(childDeltas[1].isRemoved());
        assertEquals(objectB, childDeltas[1].getOriginalObject());
    }

    public void testCreateChildDeltas_toManyAssociation_ComputationByObject_Moved() {
        IDeltaComputationOptions options = new Options(IDeltaComputationOptions.COMPUTE_BY_OBJECT);
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        
        //note: objectsA and B are used as parent and child objects (not realistic but of no harm for this test)
        List childs = new ArrayList();
        childs.add(objectA);
        childs.add(objectB);
        List differentOrder = new ArrayList();
        differentOrder.add(objectB);
        differentOrder.add(objectA);
        ModelObjectDelta.createChildDeltas(delta, childs, differentOrder, "childs", options);
        IModelObjectDelta[] childDeltas = delta.getChildDeltas();
        assertEquals(2, childDeltas.length);
        assertTrue(childDeltas[0].isMoved());
        assertEquals(objectA, childDeltas[0].getOriginalObject());
        assertEquals(objectA, childDeltas[0].getReferenceObject());
        assertTrue(childDeltas[1].isMoved());
        assertEquals(objectB, childDeltas[1].getOriginalObject());
        assertEquals(objectB, childDeltas[1].getReferenceObject());
    }

    public void testAccept() {
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        ModelObjectDelta childDelta1 = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        childDelta1.markPropertyChanged("property");
        ModelObjectDelta childDelta2 = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        childDelta2.markPropertyChanged("property");
        ModelObjectDelta grandchildDelta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        grandchildDelta.markPropertyChanged("property");
        childDelta1.addChildDelta(grandchildDelta);
        delta.addChildDelta(childDelta1);
        delta.addChildDelta(childDelta2);
        
        Visitor visitor = new Visitor(false);
        delta.accept(visitor);
        assertEquals(1, visitor.visitedDeltas.size());
        assertTrue(visitor.visitedDeltas.contains(delta));
        
        visitor = new Visitor(true);
        delta.accept(visitor);
        assertEquals(4, visitor.visitedDeltas.size());
        assertTrue(visitor.visitedDeltas.contains(delta));
        assertTrue(visitor.visitedDeltas.contains(childDelta1));
        assertTrue(visitor.visitedDeltas.contains(childDelta2));
        assertTrue(visitor.visitedDeltas.contains(grandchildDelta));
    }

    class MyModelObject implements IModelObject, IDeltaSupport {

        private String id;
        private int property;
        
        public MyModelObject(String id) {
            this.id = id;
        }
        
        public int getProperty() {
            return property;
        }

        public void setProperty(int property) {
            this.property = property;
        }

        /**
         * {@inheritDoc}
         */
        public void addChangeListener(IModelObjectChangeListener listener) {
        }

        /**
         * {@inheritDoc}
         */
        public void removeChangeListener(IModelObjectChangeListener listener) {
        }

        /**
         * {@inheritDoc}
         */
        public MessageList validate(String businessFunction) {
            return null;
        }

        public String toString() {
            return id;
        }

        /**
         * {@inheritDoc}
         */
        public IModelObjectDelta computeDelta(IModelObject otherObject, IDeltaComputationOptions options) {
            MyModelObject other = (MyModelObject)otherObject;
            ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(this, otherObject);
            delta.checkPropertyChange("property", this.property, other.property, options);
            return delta;
        }
    }
    
    class Visitor implements IModelObjectDeltaVisitor {

        private boolean rc;
        private Set visitedDeltas = new HashSet();
        
        public Visitor(boolean rc) {
            super();
            this.rc = rc;
        }

        public boolean visit(IModelObjectDelta delta) {
            visitedDeltas.add(delta);
            return rc;
        }
        
    }
    
    class Options implements IDeltaComputationOptions {

        private int computationMethod;
        
        public Options(int computationMethod) {
            super();
            this.computationMethod = computationMethod;
        }

        public int getMethod(String association) {
            return computationMethod;
        }

        public boolean ignore(Class clazz, String property) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public boolean isSame(IModelObject object1, IModelObject object2) {
            MyModelObject mo1 = (MyModelObject)object1;
            MyModelObject mo2 = (MyModelObject)object2;
            return mo1.id.equals(mo2.id);
        }
        
    }
}
