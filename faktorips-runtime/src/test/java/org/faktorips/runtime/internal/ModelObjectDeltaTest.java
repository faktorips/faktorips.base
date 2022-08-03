/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org Copyright (c) Faktor Zehn AG.
 * <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.runtime.DeltaComputationOptionsByPosition;
import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IDeltaComputationOptions;
import org.faktorips.runtime.IDeltaSupport;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IModelObjectDelta;
import org.faktorips.runtime.IModelObjectDeltaVisitor;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.ITimedConfigurableModelObject;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.type.AssociationKind;
import org.faktorips.runtime.model.type.AttributeKind;
import org.faktorips.runtime.model.type.ValueSetKind;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class ModelObjectDeltaTest {

    private static final DeltaComputationOptionsByPosition OPTIONS = new DeltaComputationOptionsByPosition();

    private final MyModelObject objectA = new MyModelObject("A");

    private final MyModelObject objectB = new MyModelObject("B");

    @Test
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

    @Test
    public void testNewDelta_SameClasses() {
        ModelObjectDelta delta = ModelObjectDelta.newDelta(objectA, objectB, OPTIONS);
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

    @Test
    public void testNewDelta_DifferentClasses() {
        MyModelObject2 objectC = new MyModelObject2("C");
        ModelObjectDelta delta = ModelObjectDelta.newDelta(objectA, objectC, OPTIONS);
        assertTrue(delta.isClassChanged());
        assertFalse(delta.isEmpty());
        assertTrue(delta.isChanged());
        assertFalse(delta.isPropertyChanged());
        assertFalse(delta.isStructureChanged());
        assertFalse(delta.isChildChanged());
        assertEquals(IModelObjectDelta.CHANGED, delta.getKind());
        assertEquals(IModelObjectDelta.CLASS_CHANGED, delta.getKindOfChange());
        assertSame(objectA, delta.getOriginalObject());
        assertSame(objectC, delta.getReferenceObject());

        // vice versa
        delta = ModelObjectDelta.newDelta(objectC, objectA, OPTIONS);
        assertTrue(delta.isClassChanged());
        assertFalse(delta.isEmpty());
        assertTrue(delta.isChanged());
    }

    @Test
    public void testNewDelta_ConfigurableModelObjectsSameProductComponent() {
        IProductComponent productComponent = mock(IProductComponent.class);
        IModelObject obj1 = new MyConfigurableModelObject("obj1", productComponent);
        IModelObject obj2 = new MyConfigurableModelObject("obj2", productComponent);

        ModelObjectDelta delta = ModelObjectDelta.newDelta(obj1, obj2, new DeltaComputationOptionsByPosition());

        assertFalse(delta.isChanged());
    }

    @Test
    public void testNewDelta_ConfigurableModelObjectsDifferentProductComponent() {
        IProductComponent productComponent1 = mock(IProductComponent.class);
        IProductComponent productComponent2 = mock(IProductComponent.class);
        IModelObject obj1 = new MyConfigurableModelObject("obj1", productComponent1);
        IModelObject obj2 = new MyConfigurableModelObject("obj2", productComponent2);

        ModelObjectDelta delta = ModelObjectDelta.newDelta(obj1, obj2, new DeltaComputationOptionsByPosition());

        assertTrue(delta.isChanged());
        assertTrue(delta.isPropertyChanged(IConfigurableModelObject.PROPERTY_PRODUCT_COMPONENT));
    }

    @Test
    public void testNewDelta_TimedConfigurableModelObjectsSameProductComponentGeneration() {
        IProductComponent productComponent = mock(IProductComponent.class);
        IProductComponentGeneration productComponentGeneration = mock(IProductComponentGeneration.class);
        IModelObject obj1 = new MyTimedConfigurableModelObject("obj1", productComponent, productComponentGeneration);
        IModelObject obj2 = new MyTimedConfigurableModelObject("obj2", productComponent, productComponentGeneration);

        ModelObjectDelta delta = ModelObjectDelta.newDelta(obj1, obj2, new DeltaComputationOptionsByPosition());

        assertFalse(delta.isChanged());
    }

    @Test
    public void testNewDelta_TimedConfigurableModelObjectsDifferentProductComponentGeneration() {
        IProductComponent productComponent1 = mock(IProductComponent.class);
        IProductComponent productComponent2 = mock(IProductComponent.class);
        IProductComponentGeneration productComponentGeneration1 = mock(IProductComponentGeneration.class);
        IProductComponentGeneration productComponentGeneration2 = mock(IProductComponentGeneration.class);
        IModelObject obj1 = new MyTimedConfigurableModelObject("obj1", productComponent1, productComponentGeneration1);
        IModelObject obj2 = new MyTimedConfigurableModelObject("obj2", productComponent2, productComponentGeneration2);

        ModelObjectDelta delta = ModelObjectDelta.newDelta(obj1, obj2, new DeltaComputationOptionsByPosition());

        assertTrue(delta.isChanged());
        assertTrue(delta.isPropertyChanged(ITimedConfigurableModelObject.PROPERTY_PRODUCT_CMPT_GENERATION));
    }

    @Test
    public void testNewAddDelta() {
        ModelObjectDelta delta = ModelObjectDelta.newAddDelta(objectB, "childs", AssociationKind.Composition, OPTIONS);
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

    @Test
    public void testNewRemoveDelta() {
        ModelObjectDelta delta = ModelObjectDelta.newRemoveDelta(objectA, "childs", AssociationKind.Composition,
                OPTIONS);
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

    @Test
    public void testMarkPropertyChanged() {
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        delta.markPropertyChanged("property");
        assertTrue(delta.isChanged());
        assertTrue(delta.isPropertyChanged());
        assertFalse(delta.isStructureChanged());
        assertFalse(delta.isChildChanged());

        List<String> props = delta.getChangedProperties();
        assertEquals(1, props.size());
        assertEquals("property", props.iterator().next());

        delta.markPropertyChanged("property2");
        props = new ArrayList<>();
        props.addAll(delta.getChangedProperties());
        assertEquals(2, props.size());
        assertEquals("property2", props.get(0));
        assertEquals("property", props.get(1));

        delta.markPropertyChanged("property2");
        props = new ArrayList<>();
        props.addAll(delta.getChangedProperties());
        assertEquals(2, props.size());
        assertEquals("property2", props.get(0));
        assertEquals("property", props.get(1));
    }

    @Test
    public void testGetChangedProperties() {
        MyModelObject objectC = new MyModelObject("C");
        objectC.setProperty(1);
        objectC.setAdditionalProperty(2);

        ModelObjectDelta delta = (ModelObjectDelta)objectA.computeDelta(objectC, OPTIONS);
        delta.markPropertyChanged("unkownProperty");
        List<String> properties = delta.getChangedProperties();

        assertThat(properties, is(Arrays.asList("unkownProperty", "property", "additionalProperty")));
    }

    @Test
    public void testAddChildDelta_EmptyDelta() {
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        ModelObjectDelta childDelta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        delta.addChildDelta(childDelta);
        assertFalse(delta.isChanged());
        assertFalse(delta.isPropertyChanged());
        assertFalse(delta.isStructureChanged());
        assertFalse(delta.isChildChanged());
        assertEquals(0, delta.getChildDeltas().size());
    }

    @Test
    public void testAddChildDelta_PropertyChangeDelta() {
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        ModelObjectDelta childDelta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        childDelta.markPropertyChanged("property");
        delta.addChildDelta(childDelta);
        assertTrue(delta.isChanged());
        assertFalse(delta.isPropertyChanged());
        assertFalse(delta.isStructureChanged());
        assertTrue(delta.isChildChanged());
        assertEquals(1, delta.getChildDeltas().size());
        assertSame(childDelta, delta.getChildDeltas().get(0));
    }

    @Test
    public void testAddChildDelta_AddDelta() {
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        ModelObjectDelta childDelta = ModelObjectDelta.newAddDelta(objectB, "childs", AssociationKind.Composition,
                OPTIONS);
        delta.addChildDelta(childDelta);
        assertTrue(delta.isChanged());
        assertFalse(delta.isPropertyChanged());
        assertTrue(delta.isStructureChanged());
        assertFalse(delta.isChildChanged());
        assertEquals(1, delta.getChildDeltas().size());
        assertSame(childDelta, delta.getChildDeltas().get(0));
    }

    @Test
    public void testAddChildDelta_RemoveDelta() {
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        ModelObjectDelta childDelta = ModelObjectDelta.newRemoveDelta(objectA, "childs", AssociationKind.Composition,
                OPTIONS);
        delta.addChildDelta(childDelta);
        assertTrue(delta.isChanged());
        assertFalse(delta.isPropertyChanged());
        assertTrue(delta.isStructureChanged());
        assertFalse(delta.isChildChanged());
        assertEquals(1, delta.getChildDeltas().size());
        assertSame(childDelta, delta.getChildDeltas().get(0));
    }

    @Test
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

    @Test
    public void testCheckPropertyChange_ReturnTrueIfObjectAreDifferent() {
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        delta.checkPropertyChange("property", objectA, objectB, OPTIONS);
        assertTrue(delta.isPropertyChanged("property"));
    }

    @Test
    public void testCheckPropertyChange_ReturnFalseIfObjectAreEqual() {
        ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(objectA, objectB);
        delta.checkPropertyChange("property", objectA, objectA, OPTIONS);
        assertFalse(delta.isPropertyChanged("property"));
    }

    @IpsPolicyCmptType(name = "MyModelObject")
    @IpsAttributes({ "property", "additionalProperty" })
    static class MyModelObject implements IModelObject, IDeltaSupport {

        private final String id;
        private int property;
        private int additionalProperty;

        public MyModelObject() {
            id = null;
        }

        public MyModelObject(String id) {
            this.id = id;
        }

        @IpsAttribute(name = "property", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public int getProperty() {
            return property;
        }

        @IpsAttributeSetter("property")
        public void setProperty(int property) {
            this.property = property;
        }

        @IpsAttribute(name = "additionalProperty", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public int getAdditionalProperty() {
            return additionalProperty;
        }

        @IpsAttributeSetter("additionalProperty")
        public void setAdditionalProperty(int additionalProperty) {
            this.additionalProperty = additionalProperty;
        }

        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }

        @Override
        public String toString() {
            return id;
        }

        @Override
        public IModelObjectDelta computeDelta(IModelObject otherObject, IDeltaComputationOptions options) {
            MyModelObject other = (MyModelObject)otherObject;
            ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(this, otherObject);
            delta.checkPropertyChange("property", property, other.property, options);
            delta.checkPropertyChange("additionalProperty", additionalProperty, other.additionalProperty, options);
            return delta;
        }
    }

    static class MyModelObject2 extends MyModelObject {

        public MyModelObject2() {
            super();
        }

        public MyModelObject2(String id) {
            super(id);
        }

        private String anotherProperty;

        public String getAnotherProperty() {
            return anotherProperty;
        }

        public void setAnotherProperty(String anotherProperty) {
            this.anotherProperty = anotherProperty;
        }

    }

    class MyConfigurableModelObject extends MyModelObject implements IConfigurableModelObject {

        private IProductComponent productComponent;

        public MyConfigurableModelObject(String id, IProductComponent productComponent) {
            super(id);
            this.productComponent = productComponent;
        }

        @Override
        public IProductComponent getProductComponent() {
            return productComponent;
        }

        @Override
        public void setProductComponent(IProductComponent productComponent) {
            this.productComponent = productComponent;
        }

        @Override
        public Calendar getEffectiveFromAsCalendar() {
            return null;
        }

        @Override
        public void initialize() {
            // don't do anything
        }

    }

    class MyTimedConfigurableModelObject extends MyConfigurableModelObject implements ITimedConfigurableModelObject {

        private final IProductComponentGeneration productComponentGeneration;

        public MyTimedConfigurableModelObject(String id, IProductComponent productComponent,
                IProductComponentGeneration productComponentGeneration) {
            super(id, productComponent);
            this.productComponentGeneration = productComponentGeneration;
        }

        @Override
        public IProductComponentGeneration getProductCmptGeneration() {
            return productComponentGeneration;
        }

    }

    static class Visitor implements IModelObjectDeltaVisitor {

        private final boolean rc;
        private final Set<IModelObjectDelta> visitedDeltas = new HashSet<>();

        public Visitor(boolean rc) {
            super();
            this.rc = rc;
        }

        @Override
        public boolean visit(IModelObjectDelta delta) {
            visitedDeltas.add(delta);
            return rc;
        }

    }

    static class Options implements IDeltaComputationOptions {

        private final ComputationMethod computationMethod;

        public Options(ComputationMethod computationMethod) {
            super();
            this.computationMethod = computationMethod;
        }

        @Override
        public ComputationMethod getMethod(String association) {
            return computationMethod;
        }

        @Override
        public boolean ignore(Class<?> clazz, String property) {
            return false;
        }

        @Override
        public boolean isSame(IModelObject object1, IModelObject object2) {
            MyModelObject mo1 = (MyModelObject)object1;
            MyModelObject mo2 = (MyModelObject)object2;
            return mo1.id.equals(mo2.id);
        }

        @Override
        public boolean isCreateSubtreeDelta() {
            return true;
        }

        @Override
        public boolean areValuesEqual(Class<?> modelClass, String property, Object value1, Object value2) {
            return false;
        }

        @Override
        public boolean ignoreAssociations() {
            return false;
        }

    }

}
