/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.util.Collections;
import java.util.List;

import org.faktorips.runtime.IDeltaComputationOptions;
import org.faktorips.runtime.IDeltaSupport;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IModelObjectDelta;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;

public abstract class AbstractModelObjectDeltaChildrenTest {

    static final ModelObjectDelta emptyParentDelta() {
        TestModelObject parent1 = new TestModelObject("Parent");
        TestModelObject parent2 = new TestModelObject("Parent");
        return ModelObjectDelta.newEmptyDelta(parent1, parent2);
    }

    static List<IModelObject> emptyList() {
        return Collections.<IModelObject> emptyList();
    }

    public static class TestModelObject implements IModelObject, IDeltaSupport {

        private final String id;

        private int property;

        TestModelObject reference;

        public TestModelObject() {
            id = "";
        }

        public TestModelObject(String id) {
            this.id = id;
        }

        public TestModelObject(String id, TestModelObject ref) {
            this.id = id;
            reference = ref;
        }

        public void setProperty(int property) {
            this.property = property;
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
            TestModelObject other = (TestModelObject)otherObject;
            ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(this, otherObject);
            delta.checkPropertyChange("property", property, other.property, options);
            TestModelObject otherMyModelObject = (TestModelObject)otherObject;
            ModelObjectDelta.createChildDeltas(delta, reference, otherMyModelObject.reference, "reference", options);
            return delta;
        }
    }

    static class TestOptions implements IDeltaComputationOptions {

        private final ComputationMethod computationMethod;
        private boolean subtree = false;

        public TestOptions(ComputationMethod computationMethod) {
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
            TestModelObject mo1 = (TestModelObject)object1;
            TestModelObject mo2 = (TestModelObject)object2;
            return mo1.id.equals(mo2.id);
        }

        @Override
        public boolean isCreateSubtreeDelta() {
            return subtree;
        }

        public TestOptions withSubtreeDelta() {
            subtree = true;
            return this;
        }

        @Override
        public boolean ignoreAssociations() {
            return false;
        }

    }

}
