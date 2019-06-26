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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.faktorips.runtime.IDeltaComputationOptions;
import org.faktorips.runtime.IModelObjectDelta;
import org.junit.Test;

public class ModelObjectDeltaChildrenByObjectTest extends AbstractModelObjectDeltaChildrenTest {

    @Test
    public void testCreateChildDeltas_to1_ObjectUnchanged() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject child1 = new TestModelObject("Child");
        TestModelObject child2 = new TestModelObject("Child");
        IDeltaComputationOptions options = computationByObject();

        ModelObjectDelta.createChildDeltas(delta, child1, child2, "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertTrue(childDeltas.isEmpty());
    }

    @Test
    public void testCreateAssociatedChildDeltas_to1_ObjectUnchanged() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject child1 = new TestModelObject("Child");
        TestModelObject child2 = new TestModelObject("Child");
        IDeltaComputationOptions options = computationByObject();

        ModelObjectDelta.createAssociatedChildDeltas(delta, child1, child2, "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertTrue(childDeltas.isEmpty());
    }

    @Test
    public void testCreateChildDeltas_to1_Added_WithoutSubtree() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject addedChild = new TestModelObject("Child", new TestModelObject("Grandchild"));
        IDeltaComputationOptions options = computationByObject();

        ModelObjectDelta.createChildDeltas(delta, null, addedChild, "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(1, childDeltas.size());
        IModelObjectDelta childDelta = childDeltas.get(0);
        assertTrue(childDelta.isAdded());
        assertEquals(addedChild, childDelta.getReferenceObject());
        assertTrue(childDelta.getChildDeltas().isEmpty());
    }

    @Test
    public void testCreateChildDeltas_to1_Added_WithSubtree() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject addedChild = new TestModelObject("Child", new TestModelObject("Grandchild"));
        IDeltaComputationOptions options = computationByObject().withSubtreeDelta();

        ModelObjectDelta.createChildDeltas(delta, null, addedChild, "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(1, childDeltas.size());
        IModelObjectDelta childDelta = childDeltas.get(0);
        assertTrue(childDelta.isAdded());
        assertEquals(addedChild, childDelta.getReferenceObject());
        assertEquals(1, childDelta.getChildDeltas().size());
        assertEquals(addedChild.reference, childDelta.getChildDeltas().get(0).getReferenceObject());
    }

    @Test
    public void testCreateAssociatedChildDeltas_to1_Added_WithoutSubtree() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject addedChild = new TestModelObject("Child", new TestModelObject("Grandchild"));
        IDeltaComputationOptions options = computationByObject();

        ModelObjectDelta.createAssociatedChildDeltas(delta, null, addedChild, "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(1, childDeltas.size());
        IModelObjectDelta childDelta = childDeltas.get(0);
        assertTrue(childDelta.isAdded());
        assertEquals(addedChild, childDelta.getReferenceObject());
        assertTrue(childDelta.getChildDeltas().isEmpty());
    }

    @Test
    public void testCreateAssociatedChildDeltas_to1_Added_WithSubtree() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject addedChild = new TestModelObject("Child", new TestModelObject("Grandchild"));
        IDeltaComputationOptions options = computationByObject().withSubtreeDelta();

        ModelObjectDelta.createAssociatedChildDeltas(delta, null, addedChild, "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(1, childDeltas.size());
        IModelObjectDelta childDelta = childDeltas.get(0);
        assertTrue(childDelta.isAdded());
        assertEquals(addedChild, childDelta.getReferenceObject());
        assertTrue(childDelta.getChildDeltas().isEmpty());
    }

    @Test
    public void testCreateChildDeltas_to1_Removed_WithoutSubtree() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject removedChild = new TestModelObject("Child", new TestModelObject("Grandchild"));
        IDeltaComputationOptions options = computationByObject();

        ModelObjectDelta.createChildDeltas(delta, removedChild, null, "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(1, childDeltas.size());
        IModelObjectDelta childDelta = childDeltas.get(0);
        assertTrue(childDelta.isRemoved());
        assertEquals(removedChild, childDelta.getOriginalObject());
        assertTrue(childDelta.getChildDeltas().isEmpty());
    }

    @Test
    public void testCreateChildDeltas_to1_Removed_WithSubtreeDelta() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject removedChild = new TestModelObject("Child", new TestModelObject("Grandchild"));
        IDeltaComputationOptions options = computationByObject().withSubtreeDelta();

        ModelObjectDelta.createChildDeltas(delta, removedChild, null, "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(1, childDeltas.size());
        IModelObjectDelta childDelta = childDeltas.get(0);
        assertTrue(childDelta.isRemoved());
        assertEquals(removedChild, childDelta.getOriginalObject());
        assertEquals(1, childDelta.getChildDeltas().size());
        assertEquals(removedChild.reference, childDelta.getChildDeltas().get(0).getOriginalObject());
    }

    @Test
    public void testCreateAssociatedChildDeltas_to1_Removed_WithoutSubtreeDelta() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject removedChild = new TestModelObject("Child", new TestModelObject("Grandchild"));
        IDeltaComputationOptions options = computationByObject();

        ModelObjectDelta.createAssociatedChildDeltas(delta, removedChild, null, "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(1, childDeltas.size());
        IModelObjectDelta childDelta = childDeltas.get(0);
        assertTrue(childDelta.isRemoved());
        assertEquals(removedChild, childDelta.getOriginalObject());
        assertTrue(childDelta.getChildDeltas().isEmpty());
    }

    @Test
    public void testCreateAssociatedChildDeltas_to1_Removed_WithSubtreeDelta() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject removedChild = new TestModelObject("Child", new TestModelObject("Grandchild"));
        IDeltaComputationOptions options = computationByObject().withSubtreeDelta();

        ModelObjectDelta.createAssociatedChildDeltas(delta, removedChild, null, "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(1, childDeltas.size());
        IModelObjectDelta childDelta = childDeltas.get(0);
        assertTrue(childDelta.isRemoved());
        assertEquals(removedChild, childDelta.getOriginalObject());
        assertTrue(childDelta.getChildDeltas().isEmpty());
    }

    @Test
    public void testCreateChildDeltas_to1_Replaced_WithoutSubtreeDelta() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject removedChild = new TestModelObject("OldChild", new TestModelObject("OldGrandchild"));
        TestModelObject addedChild = new TestModelObject("NewChild", new TestModelObject("NewGrandchild"));
        IDeltaComputationOptions options = computationByObject();

        ModelObjectDelta.createChildDeltas(delta, removedChild, addedChild, "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(2, childDeltas.size());
        IModelObjectDelta childDelta1 = childDeltas.get(0);
        assertTrue(childDelta1.isRemoved());
        assertEquals(removedChild, childDelta1.getOriginalObject());
        assertTrue(childDelta1.getChildDeltas().isEmpty());
        IModelObjectDelta childDelta2 = childDeltas.get(1);
        assertTrue(childDelta2.isAdded());
        assertEquals(addedChild, childDelta2.getReferenceObject());
        assertTrue(childDelta2.getChildDeltas().isEmpty());
    }

    @Test
    public void testCreateChildDeltas_to1_Replaced_WithSubtreeDelta() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject removedChild = new TestModelObject("OldChild", new TestModelObject("OldGrandchild"));
        TestModelObject addedChild = new TestModelObject("NewChild", new TestModelObject("NewGrandchild"));
        IDeltaComputationOptions options = computationByObject().withSubtreeDelta();

        ModelObjectDelta.createChildDeltas(delta, removedChild, addedChild, "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(2, childDeltas.size());
        IModelObjectDelta childDelta1 = childDeltas.get(0);
        assertTrue(childDelta1.isRemoved());
        assertEquals(removedChild, childDelta1.getOriginalObject());
        assertEquals(removedChild.reference, childDelta1.getChildDeltas().get(0).getOriginalObject());
        IModelObjectDelta childDelta2 = childDeltas.get(1);
        assertTrue(childDelta2.isAdded());
        assertEquals(addedChild, childDelta2.getReferenceObject());
        assertEquals(addedChild.reference, childDelta2.getChildDeltas().get(0).getReferenceObject());
    }

    @Test
    public void testCreateAssociatedChildDeltas_to1_Replaced_WithoutSubtreeDelta() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject removedChild = new TestModelObject("OldChild", new TestModelObject("OldGrandchild"));
        TestModelObject addedChild = new TestModelObject("NewChild", new TestModelObject("NewGrandchild"));
        IDeltaComputationOptions options = computationByObject();

        ModelObjectDelta.createAssociatedChildDeltas(delta, removedChild, addedChild, "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(2, childDeltas.size());
        IModelObjectDelta childDelta1 = childDeltas.get(0);
        assertTrue(childDelta1.isRemoved());
        assertEquals(removedChild, childDelta1.getOriginalObject());
        assertTrue(childDelta1.getChildDeltas().isEmpty());
        IModelObjectDelta childDelta2 = childDeltas.get(1);
        assertTrue(childDelta2.isAdded());
        assertEquals(addedChild, childDelta2.getReferenceObject());
        assertTrue(childDelta2.getChildDeltas().isEmpty());
    }

    @Test
    public void testCreateAssociatedChildDeltas_to1_Replaced_WithSubtreeDelta() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject removedChild = new TestModelObject("OldChild", new TestModelObject("OldGrandchild"));
        TestModelObject addedChild = new TestModelObject("NewChild", new TestModelObject("NewGrandchild"));
        IDeltaComputationOptions options = computationByObject().withSubtreeDelta();

        ModelObjectDelta.createAssociatedChildDeltas(delta, removedChild, addedChild, "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(2, childDeltas.size());
        IModelObjectDelta childDelta1 = childDeltas.get(0);
        assertTrue(childDelta1.isRemoved());
        assertEquals(removedChild, childDelta1.getOriginalObject());
        assertTrue(childDelta1.getChildDeltas().isEmpty());
        IModelObjectDelta childDelta2 = childDeltas.get(1);
        assertTrue(childDelta2.isAdded());
        assertEquals(addedChild, childDelta2.getReferenceObject());
        assertTrue(childDelta2.getChildDeltas().isEmpty());
    }

    @Test
    public void testCreateChildDeltas_to1_ObjectHasChanged() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject originalChild = new TestModelObject("Child");
        TestModelObject changedChild = new TestModelObject("Child");
        changedChild.setProperty(42);
        IDeltaComputationOptions options = computationByObject();

        ModelObjectDelta.createChildDeltas(delta, originalChild, changedChild, "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(1, childDeltas.size());
        IModelObjectDelta childDelta = childDeltas.get(0);
        assertTrue(childDelta.isChanged());
        assertEquals(originalChild, childDelta.getOriginalObject());
        assertEquals(changedChild, childDelta.getReferenceObject());
        assertTrue(childDelta.getChildDeltas().isEmpty());
    }

    @Test
    public void testCreateAssociatedChildDeltas_to1_ObjectHasChanged() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject originalChild = new TestModelObject("Child");
        TestModelObject changedChild = new TestModelObject("Child");
        changedChild.setProperty(42);
        IDeltaComputationOptions options = computationByObject();

        ModelObjectDelta.createAssociatedChildDeltas(delta, originalChild, changedChild, "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertTrue(childDeltas.isEmpty());
    }

    @Test
    public void testCreateChildDeltas_toMany_Added_WithoutSubtree() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject addedChild1 = new TestModelObject("Child1", new TestModelObject("Grandchild1"));
        TestModelObject addedChild2 = new TestModelObject("Child2", new TestModelObject("Grandchild2"));
        IDeltaComputationOptions options = computationByObject();

        ModelObjectDelta.createChildDeltas(delta, emptyList(), asList(addedChild1, addedChild2), "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(2, childDeltas.size());
        IModelObjectDelta childDelta1 = childDeltas.get(0);
        assertTrue(childDelta1.isAdded());
        assertEquals(addedChild1, childDelta1.getReferenceObject());
        assertTrue(childDelta1.getChildDeltas().isEmpty());
        IModelObjectDelta childDelta2 = childDeltas.get(1);
        assertTrue(childDelta2.isAdded());
        assertEquals(addedChild2, childDelta2.getReferenceObject());
        assertTrue(childDelta2.getChildDeltas().isEmpty());
    }

    @Test
    public void testCreateChildDeltas_toMany_Added_WithSubtree() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject addedChild1 = new TestModelObject("Child1", new TestModelObject("Grandchild1"));
        TestModelObject addedChild2 = new TestModelObject("Child2", new TestModelObject("Grandchild2"));
        IDeltaComputationOptions options = computationByObject().withSubtreeDelta();

        ModelObjectDelta.createChildDeltas(delta, emptyList(), asList(addedChild1, addedChild2), "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(2, childDeltas.size());
        IModelObjectDelta childDelta1 = childDeltas.get(0);
        assertTrue(childDelta1.isAdded());
        assertEquals(addedChild1, childDelta1.getReferenceObject());
        assertEquals(1, childDelta1.getChildDeltas().size());
        assertEquals(addedChild1.reference, childDelta1.getChildDeltas().get(0).getReferenceObject());
        IModelObjectDelta childDelta2 = childDeltas.get(1);
        assertTrue(childDelta2.isAdded());
        assertEquals(addedChild2, childDelta2.getReferenceObject());
        assertEquals(1, childDelta2.getChildDeltas().size());
        assertEquals(addedChild2.reference, childDelta2.getChildDeltas().get(0).getReferenceObject());
    }

    @Test
    public void testCreateChildDeltas_toMany_Added_AndUnchanged() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject oldUnchangedChild = new TestModelObject("Child0", new TestModelObject("Grandchild0"));
        TestModelObject addedChild1 = new TestModelObject("Child1", new TestModelObject("Grandchild1"));
        TestModelObject addedChild2 = new TestModelObject("Child2", new TestModelObject("Grandchild2"));
        TestModelObject newUnchangedChild = new TestModelObject("Child0", new TestModelObject("Grandchild0"));
        IDeltaComputationOptions options = computationByObject();

        ModelObjectDelta.createChildDeltas(delta, asList(oldUnchangedChild),
                asList(newUnchangedChild, addedChild1, addedChild2), "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(2, childDeltas.size());
        IModelObjectDelta childDelta1 = childDeltas.get(0);
        assertTrue(childDelta1.isAdded());
        assertEquals(addedChild1, childDelta1.getReferenceObject());
        assertTrue(childDelta1.getChildDeltas().isEmpty());
        IModelObjectDelta childDelta2 = childDeltas.get(1);
        assertTrue(childDelta2.isAdded());
        assertEquals(addedChild2, childDelta2.getReferenceObject());
        assertTrue(childDelta2.getChildDeltas().isEmpty());
    }

    @Test
    public void testCreateChildDeltas_toMany_Removed_WithoutSubtree() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject removedChild1 = new TestModelObject("Child1", new TestModelObject("Grandchild1"));
        TestModelObject removedChild2 = new TestModelObject("Child2", new TestModelObject("Grandchild2"));
        IDeltaComputationOptions options = computationByObject();

        ModelObjectDelta.createChildDeltas(delta, asList(removedChild1, removedChild2), emptyList(), "children",
                options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(2, childDeltas.size());
        IModelObjectDelta childDelta1 = childDeltas.get(0);
        assertTrue(childDelta1.isRemoved());
        assertEquals(removedChild1, childDelta1.getOriginalObject());
        assertTrue(childDelta1.getChildDeltas().isEmpty());
        IModelObjectDelta childDelta2 = childDeltas.get(1);
        assertTrue(childDelta2.isRemoved());
        assertEquals(removedChild2, childDelta2.getOriginalObject());
        assertTrue(childDelta2.getChildDeltas().isEmpty());
    }

    @Test
    public void testCreateChildDeltas_toMany_Removed_WithSubtree() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject removedChild1 = new TestModelObject("Child1", new TestModelObject("Grandchild1"));
        TestModelObject removedChild2 = new TestModelObject("Child2", new TestModelObject("Grandchild2"));
        IDeltaComputationOptions options = computationByObject().withSubtreeDelta();

        ModelObjectDelta.createChildDeltas(delta, asList(removedChild1, removedChild2), emptyList(), "children",
                options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(2, childDeltas.size());
        IModelObjectDelta childDelta1 = childDeltas.get(0);
        assertTrue(childDelta1.isRemoved());
        assertEquals(removedChild1, childDelta1.getOriginalObject());
        assertEquals(1, childDelta1.getChildDeltas().size());
        assertEquals(removedChild1.reference, childDelta1.getChildDeltas().get(0).getOriginalObject());
        IModelObjectDelta childDelta2 = childDeltas.get(1);
        assertTrue(childDelta2.isRemoved());
        assertEquals(removedChild2, childDelta2.getOriginalObject());
        assertEquals(1, childDelta2.getChildDeltas().size());
        assertEquals(removedChild2.reference, childDelta2.getChildDeltas().get(0).getOriginalObject());
    }

    @Test
    public void testCreateChildDeltas_toMany_Moved_WithoutSubtree() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject oldChild1 = new TestModelObject("Child1", new TestModelObject("Grandchild1"));
        TestModelObject oldChild2 = new TestModelObject("Child2", new TestModelObject("Grandchild2"));
        TestModelObject newChild1 = new TestModelObject("Child1", new TestModelObject("Grandchild3"));
        TestModelObject newChild2 = new TestModelObject("Child2", new TestModelObject("Grandchild4"));
        IDeltaComputationOptions options = computationByObject();

        ModelObjectDelta.createChildDeltas(delta, asList(oldChild1, oldChild2), asList(newChild2, newChild1),
                "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(2, childDeltas.size());
        IModelObjectDelta childDelta1 = childDeltas.get(0);
        assertTrue(childDelta1.isMoved());
        assertEquals(oldChild1, childDelta1.getOriginalObject());
        assertEquals(newChild1, childDelta1.getReferenceObject());
        // subtree only affects added/removed, not moved
        assertEquals(2, childDelta1.getChildDeltas().size());
        assertEquals(oldChild1.reference, childDelta1.getChildDeltas().get(0).getOriginalObject());
        assertEquals(newChild1.reference, childDelta1.getChildDeltas().get(1).getReferenceObject());
        IModelObjectDelta childDelta2 = childDeltas.get(1);
        assertTrue(childDelta2.isMoved());
        assertEquals(oldChild2, childDelta2.getOriginalObject());
        assertEquals(newChild2, childDelta2.getReferenceObject());
        assertEquals(2, childDelta2.getChildDeltas().size());
        assertEquals(oldChild2.reference, childDelta2.getChildDeltas().get(0).getOriginalObject());
        assertEquals(newChild2.reference, childDelta2.getChildDeltas().get(1).getReferenceObject());
    }

    @Test
    public void testCreateChildDeltas_toMany_Moved_WithSubtree() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject oldChild1 = new TestModelObject("Child1", new TestModelObject("Grandchild1"));
        TestModelObject oldChild2 = new TestModelObject("Child2", new TestModelObject("Grandchild2"));
        TestModelObject newChild1 = new TestModelObject("Child1", new TestModelObject("Grandchild3"));
        TestModelObject newChild2 = new TestModelObject("Child2", new TestModelObject("Grandchild4"));
        IDeltaComputationOptions options = computationByObject().withSubtreeDelta();

        ModelObjectDelta.createChildDeltas(delta, asList(oldChild1, oldChild2), asList(newChild2, newChild1),
                "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(2, childDeltas.size());
        IModelObjectDelta childDelta1 = childDeltas.get(0);
        assertTrue(childDelta1.isMoved());
        assertEquals(oldChild1, childDelta1.getOriginalObject());
        assertEquals(newChild1, childDelta1.getReferenceObject());
        assertEquals(2, childDelta1.getChildDeltas().size());
        assertEquals(oldChild1.reference, childDelta1.getChildDeltas().get(0).getOriginalObject());
        assertEquals(newChild1.reference, childDelta1.getChildDeltas().get(1).getReferenceObject());
        IModelObjectDelta childDelta2 = childDeltas.get(1);
        assertTrue(childDelta2.isMoved());
        assertEquals(oldChild2, childDelta2.getOriginalObject());
        assertEquals(newChild2, childDelta2.getReferenceObject());
        assertEquals(2, childDelta2.getChildDeltas().size());
        assertEquals(oldChild2.reference, childDelta2.getChildDeltas().get(0).getOriginalObject());
        assertEquals(newChild2.reference, childDelta2.getChildDeltas().get(1).getReferenceObject());
    }

    @Test
    public void testCreateAssociatedChildDeltas_toMany_Added_WithoutSubtree() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject addedChild1 = new TestModelObject("Child1", new TestModelObject("Grandchild1"));
        TestModelObject addedChild2 = new TestModelObject("Child2", new TestModelObject("Grandchild2"));
        IDeltaComputationOptions options = computationByObject();

        ModelObjectDelta.createAssociatedChildDeltas(delta, emptyList(), asList(addedChild1, addedChild2), "children",
                options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(2, childDeltas.size());
        IModelObjectDelta childDelta1 = childDeltas.get(0);
        assertTrue(childDelta1.isAdded());
        assertEquals(addedChild1, childDelta1.getReferenceObject());
        assertTrue(childDelta1.getChildDeltas().isEmpty());
        IModelObjectDelta childDelta2 = childDeltas.get(1);
        assertTrue(childDelta2.isAdded());
        assertEquals(addedChild2, childDelta2.getReferenceObject());
        assertTrue(childDelta2.getChildDeltas().isEmpty());
    }

    @Test
    public void testCreateAssociatedChildDeltas_toMany_Added_WithSubtree() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject addedChild1 = new TestModelObject("Child1", new TestModelObject("Grandchild1"));
        TestModelObject addedChild2 = new TestModelObject("Child2", new TestModelObject("Grandchild2"));
        IDeltaComputationOptions options = computationByObject().withSubtreeDelta();

        ModelObjectDelta.createAssociatedChildDeltas(delta, emptyList(), asList(addedChild1, addedChild2), "children",
                options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(2, childDeltas.size());
        IModelObjectDelta childDelta1 = childDeltas.get(0);
        assertTrue(childDelta1.isAdded());
        assertEquals(addedChild1, childDelta1.getReferenceObject());
        assertTrue(childDelta1.getChildDeltas().isEmpty());
        IModelObjectDelta childDelta2 = childDeltas.get(1);
        assertTrue(childDelta2.isAdded());
        assertEquals(addedChild2, childDelta2.getReferenceObject());
        assertTrue(childDelta2.getChildDeltas().isEmpty());
    }

    @Test
    public void testCreateAssociatedChildDeltas_toMany_Added_AndUnchanged() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject oldUnchangedChild = new TestModelObject("Child0", new TestModelObject("Grandchild0"));
        TestModelObject addedChild1 = new TestModelObject("Child1", new TestModelObject("Grandchild1"));
        TestModelObject addedChild2 = new TestModelObject("Child2", new TestModelObject("Grandchild2"));
        TestModelObject newUnchangedChild = new TestModelObject("Child0", new TestModelObject("Grandchild0"));
        IDeltaComputationOptions options = computationByObject();

        ModelObjectDelta.createAssociatedChildDeltas(delta, asList(oldUnchangedChild),
                asList(newUnchangedChild, addedChild1, addedChild2), "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(2, childDeltas.size());
        IModelObjectDelta childDelta1 = childDeltas.get(0);
        assertTrue(childDelta1.isAdded());
        assertEquals(addedChild1, childDelta1.getReferenceObject());
        assertTrue(childDelta1.getChildDeltas().isEmpty());
        IModelObjectDelta childDelta2 = childDeltas.get(1);
        assertTrue(childDelta2.isAdded());
        assertEquals(addedChild2, childDelta2.getReferenceObject());
        assertTrue(childDelta2.getChildDeltas().isEmpty());
    }

    @Test
    public void testCreateAssociatedChildDeltas_toMany_Removed_WithoutSubtree() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject removedChild1 = new TestModelObject("Child1", new TestModelObject("Grandchild1"));
        TestModelObject removedChild2 = new TestModelObject("Child2", new TestModelObject("Grandchild2"));
        IDeltaComputationOptions options = computationByObject();

        ModelObjectDelta.createAssociatedChildDeltas(delta, asList(removedChild1, removedChild2), emptyList(),
                "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(2, childDeltas.size());
        IModelObjectDelta childDelta1 = childDeltas.get(0);
        assertTrue(childDelta1.isRemoved());
        assertEquals(removedChild1, childDelta1.getOriginalObject());
        assertTrue(childDelta1.getChildDeltas().isEmpty());
        IModelObjectDelta childDelta2 = childDeltas.get(1);
        assertTrue(childDelta2.isRemoved());
        assertEquals(removedChild2, childDelta2.getOriginalObject());
        assertTrue(childDelta2.getChildDeltas().isEmpty());
    }

    @Test
    public void testCreateAssociatedChildDeltas_toMany_Removed_WithSubtree() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject removedChild1 = new TestModelObject("Child1", new TestModelObject("Grandchild1"));
        TestModelObject removedChild2 = new TestModelObject("Child2", new TestModelObject("Grandchild2"));
        IDeltaComputationOptions options = computationByObject().withSubtreeDelta();

        ModelObjectDelta.createAssociatedChildDeltas(delta, asList(removedChild1, removedChild2), emptyList(),
                "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(2, childDeltas.size());
        IModelObjectDelta childDelta1 = childDeltas.get(0);
        assertTrue(childDelta1.isRemoved());
        assertEquals(removedChild1, childDelta1.getOriginalObject());
        assertTrue(childDelta1.getChildDeltas().isEmpty());
        IModelObjectDelta childDelta2 = childDeltas.get(1);
        assertTrue(childDelta2.isRemoved());
        assertEquals(removedChild2, childDelta2.getOriginalObject());
        assertTrue(childDelta2.getChildDeltas().isEmpty());
    }

    @Test
    public void testCreateAssociatedChildDeltas_toMany_Moved_WithoutSubtree() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject oldChild1 = new TestModelObject("Child1", new TestModelObject("Grandchild1"));
        TestModelObject oldChild2 = new TestModelObject("Child2", new TestModelObject("Grandchild2"));
        TestModelObject newChild1 = new TestModelObject("Child1", new TestModelObject("Grandchild3"));
        TestModelObject newChild2 = new TestModelObject("Child2", new TestModelObject("Grandchild4"));
        IDeltaComputationOptions options = computationByObject();

        ModelObjectDelta.createAssociatedChildDeltas(delta, asList(oldChild1, oldChild2), asList(newChild2, newChild1),
                "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(2, childDeltas.size());
        IModelObjectDelta childDelta1 = childDeltas.get(0);
        assertTrue(childDelta1.isMoved());
        assertEquals(oldChild1, childDelta1.getOriginalObject());
        assertEquals(newChild1, childDelta1.getReferenceObject());
        assertTrue(childDelta1.getChildDeltas().isEmpty());
        IModelObjectDelta childDelta2 = childDeltas.get(1);
        assertTrue(childDelta2.isMoved());
        assertEquals(oldChild2, childDelta2.getOriginalObject());
        assertEquals(newChild2, childDelta2.getReferenceObject());
        assertTrue(childDelta2.getChildDeltas().isEmpty());
    }

    @Test
    public void testCreateAssociatedChildDeltas_toMany_Moved_WithSubtree() {
        ModelObjectDelta delta = emptyParentDelta();
        TestModelObject oldChild1 = new TestModelObject("Child1", new TestModelObject("Grandchild1"));
        TestModelObject oldChild2 = new TestModelObject("Child2", new TestModelObject("Grandchild2"));
        TestModelObject newChild1 = new TestModelObject("Child1", new TestModelObject("Grandchild3"));
        TestModelObject newChild2 = new TestModelObject("Child2", new TestModelObject("Grandchild4"));
        IDeltaComputationOptions options = computationByObject().withSubtreeDelta();

        ModelObjectDelta.createAssociatedChildDeltas(delta, asList(oldChild1, oldChild2), asList(newChild2, newChild1),
                "children", options);

        List<IModelObjectDelta> childDeltas = delta.getChildDeltas();
        assertEquals(2, childDeltas.size());
        IModelObjectDelta childDelta1 = childDeltas.get(0);
        assertTrue(childDelta1.isMoved());
        assertEquals(oldChild1, childDelta1.getOriginalObject());
        assertEquals(newChild1, childDelta1.getReferenceObject());
        assertTrue(childDelta1.getChildDeltas().isEmpty());
        IModelObjectDelta childDelta2 = childDeltas.get(1);
        assertTrue(childDelta2.isMoved());
        assertEquals(oldChild2, childDelta2.getOriginalObject());
        assertEquals(newChild2, childDelta2.getReferenceObject());
        assertTrue(childDelta2.getChildDeltas().isEmpty());
    }

    private static TestOptions computationByObject() {
        return new TestOptions(IDeltaComputationOptions.ComputationMethod.BY_OBJECT);
    }

}
