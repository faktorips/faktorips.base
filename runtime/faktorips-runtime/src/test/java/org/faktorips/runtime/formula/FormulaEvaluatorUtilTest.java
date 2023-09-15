/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.formula;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.formula.FormulaEvaluatorUtil.AssociationTo1Helper;
import org.faktorips.runtime.formula.FormulaEvaluatorUtil.AssociationToManyHelper;
import org.faktorips.runtime.formula.FormulaEvaluatorUtil.AttributeAccessorHelper;
import org.faktorips.runtime.formula.FormulaEvaluatorUtil.ExistsHelper;
import org.faktorips.runtime.formula.FormulaEvaluatorUtil.FunctionWithListAsArgumentHelper;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link FormulaEvaluatorUtil}.
 */
public class FormulaEvaluatorUtilTest {

    private ITree tree;
    private IBranch branch;
    private IProductComponent treePC;

    public interface ITree extends IConfigurableModelObject {
        List<IBranch> getBranches();

        IBranch getBranch();
    }

    public interface IBranch extends IModelObject {
        Integer getValue();
    }

    @Before
    public void setUp() throws Exception {
        tree = mock(ITree.class);
        treePC = mock(IProductComponent.class);
        when(tree.getProductComponent()).thenReturn(treePC);
        branch = mock(IBranch.class);
    }

    /**
     * <strong>Scenario:</strong><br>
     * A single source object has one object of the target type.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The target object is returned
     */
    @Test
    public void testAssociationTo1Helper() {
        when(tree.getBranch()).thenReturn(branch);
        List<? extends IBranch> targets = new AssociationTo1Helper<ITree, IBranch>() {

            @Override
            protected IBranch getTargetInternal(ITree sourceObject) {
                return sourceObject.getBranch();
            }
        }.getTargets(List.of(tree));
        assertNotNull(targets);
        assertEquals(1, targets.size());
        assertEquals(branch, targets.get(0));
    }

    /**
     * <strong>Scenario:</strong><br>
     * A single source object has multiple objects of the target type.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * All target objects are returned in the order the source object defines.
     */
    @Test
    public void testAssociationToManyHelper() {
        IBranch branch2 = mock(IBranch.class);
        when(tree.getBranches()).thenReturn(List.of(branch, branch2));
        List<? extends IBranch> targets = new AssociationToManyHelper<ITree, IBranch>() {

            @Override
            protected List<IBranch> getTargetsInternal(ITree sourceObject) {
                return sourceObject.getBranches();
            }
        }.getTargets(List.of(tree));
        assertNotNull(targets);
        assertEquals(2, targets.size());
        assertEquals(branch, targets.get(0));
        assertEquals(branch2, targets.get(1));
    }

    /**
     * <strong>Scenario:</strong><br>
     * Multiple source objects have multiple objects of the target type, including target objects
     * that are reachable from more than one source.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * All target objects are returned in the order the source objects define, excluding duplicates.
     */
    @Test
    public void testAssociationToManyHelper_multipleSources() {
        IBranch branch2 = mock(IBranch.class);
        ITree tree2 = mock(ITree.class);
        IBranch branch3 = mock(IBranch.class);
        when(tree.getBranches()).thenReturn(List.of(branch, branch2));
        when(tree2.getBranches()).thenReturn(List.of(branch2, branch3));
        List<? extends IBranch> targets = new AssociationToManyHelper<ITree, IBranch>() {

            @Override
            protected List<IBranch> getTargetsInternal(ITree sourceObject) {
                return sourceObject.getBranches();
            }
        }.getTargets(List.of(tree, tree2));
        assertNotNull(targets);
        assertEquals(3, targets.size());
        assertEquals(branch, targets.get(0));
        assertEquals(branch2, targets.get(1));
        assertEquals(branch3, targets.get(2));
    }

    /**
     * <strong>Scenario:</strong><br>
     * Multiple source objects have a value in special type
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * All the values are in the list
     */
    @Test
    public void testAttributeAccessorHelper() {
        IBranch branch1 = mock(IBranch.class);
        IBranch branch2 = mock(IBranch.class);

        List<IBranch> branches = new ArrayList<>();
        when(branch1.getValue()).thenReturn(Integer.valueOf(2));
        when(branch2.getValue()).thenReturn(Integer.valueOf(3));
        branches.add(branch1);
        branches.add(branch2);

        List<Integer> values = new AttributeAccessorHelper<IBranch, Integer>() {

            @Override
            protected Integer getValueInternal(IBranch sourceObject) {
                return sourceObject.getValue();
            }

        }.getAttributeValues(branches);

        assertTrue(values.contains(Integer.valueOf(2)));
        assertTrue(values.contains(Integer.valueOf(3)));
    }

    /**
     * <strong>Scenario:</strong><br>
     * a list containing multiple entries for the wanted id is searched
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * the first object with a matching id is returned
     */
    @Test
    public void testGetModelObjectByIdListOfTString() {
        when(treePC.getId()).thenReturn("id1");
        ITree modelObject = FormulaEvaluatorUtil.getModelObjectById(List.of(tree), "id2");
        assertNull(modelObject);

        modelObject = FormulaEvaluatorUtil.getModelObjectById(List.of(tree), "id1");
        assertNotNull(modelObject);
        assertEquals(tree, modelObject);

        ITree tree2 = mock(ITree.class);
        IProductComponent treePC2 = mock(IProductComponent.class);
        when(tree2.getProductComponent()).thenReturn(treePC2);
        when(treePC2.getId()).thenReturn("id2");

        ITree tree3 = mock(ITree.class);
        IProductComponent treePC3 = mock(IProductComponent.class);
        when(tree3.getProductComponent()).thenReturn(treePC3);
        when(treePC3.getId()).thenReturn("id3");

        ITree tree4 = mock(ITree.class);
        IProductComponent treePC4 = mock(IProductComponent.class);
        when(tree4.getProductComponent()).thenReturn(treePC4);
        when(treePC4.getId()).thenReturn("id2");

        modelObject = FormulaEvaluatorUtil.getModelObjectById(List.of(tree, tree2, tree3, tree4), "id2");
        assertNotNull(modelObject);
        assertEquals(tree2, modelObject);
    }

    /**
     * <strong>Scenario:</strong><br>
     * A empty list is searched for an id
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * {@code null} is returned
     */
    @Test
    public void testGetModelObjectByIdListOfTString_emptyList() {
        ITree modelObject = FormulaEvaluatorUtil.getModelObjectById(new ArrayList<>(), "id1");
        assertNull(modelObject);
    }

    /**
     * <strong>Scenario:</strong><br>
     * A list not containing the wanted id is searched
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * {@code null} is returned
     */
    @Test
    public void testGetModelObjectByIdListOfTString_notInList() {
        when(treePC.getId()).thenReturn("id1");
        ITree modelObject = FormulaEvaluatorUtil.getModelObjectById(List.of(tree), "id2");
        assertNull(modelObject);
    }

    /**
     * <strong>Scenario:</strong><br>
     * a list containing only an object with the wanted id is searched.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * the object in the list is returned
     */
    @Test
    public void testGetModelObjectByIdListOfTString_singleTarget() {
        when(treePC.getId()).thenReturn("id1");
        ITree modelObject = FormulaEvaluatorUtil.getModelObjectById(List.of(tree), "id1");
        assertNotNull(modelObject);
        assertEquals(tree, modelObject);
    }

    /**
     * <strong>Scenario:</strong><br>
     * A not product configured model object is tested
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * {@code null} is returned
     */
    @Test
    public void testGetModelObjectByIdListOfTString_notConfigurable() {
        IBranch modelObject = FormulaEvaluatorUtil.getModelObjectById(List.of(branch), "id1");
        assertNull(modelObject);
    }

    /**
     * <strong>Scenario:</strong><br>
     * A model object with a matching id is tested
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * the model object is returned
     */
    @Test
    public void testGetModelObjectByIdTString() {
        when(treePC.getId()).thenReturn("id1");
        ITree modelObject = FormulaEvaluatorUtil.getModelObjectById(tree, "id1");
        assertNotNull(modelObject);
        assertEquals(tree, modelObject);
    }

    /**
     * <strong>Scenario:</strong><br>
     * A model object with a mismatched id is tested
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * {@code null} is returned
     */
    @Test
    public void testGetModelObjectByIdTString_noMatch() {
        when(treePC.getId()).thenReturn("id1");
        ITree modelObject = FormulaEvaluatorUtil.getModelObjectById(tree, "id2");
        assertNull(modelObject);
    }

    /**
     * <strong>Scenario:</strong><br>
     * A not product configured model object is tested
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * {@code null} is returned
     */
    @Test
    public void testGetModelObjectByIdTString_notConfigurable() {
        IBranch modelObject = FormulaEvaluatorUtil.getModelObjectById(branch, "id1");
        assertNull(modelObject);
    }

    /**
     * <strong>Scenario:</strong><br>
     * a list containing multiple entries for the wanted id is searched
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * all objects with a matching id is returned
     */
    @Test
    public void testGetListModelObjectByIdListOfTString() {
        when(treePC.getId()).thenReturn("id1");

        ITree tree2 = mock(ITree.class);
        IProductComponent treePC2 = mock(IProductComponent.class);
        when(tree2.getProductComponent()).thenReturn(treePC2);
        when(treePC2.getId()).thenReturn("id2");

        ITree tree22 = mock(ITree.class);
        IProductComponent treePC22 = mock(IProductComponent.class);
        when(tree22.getProductComponent()).thenReturn(treePC2);
        when(treePC22.getId()).thenReturn("id2");

        ITree tree3 = mock(ITree.class);
        IProductComponent treePC3 = mock(IProductComponent.class);
        when(tree3.getProductComponent()).thenReturn(treePC3);
        when(treePC3.getId()).thenReturn("id3");

        List<? extends ITree> modelObjectList = FormulaEvaluatorUtil
                .getListModelObjectById(List.of(tree, tree2, tree22, tree3), "id2");
        assertFalse(modelObjectList.isEmpty());
        assertEquals(2, modelObjectList.size());
    }

    /**
     * <strong>Scenario:</strong><br>
     * a empty list is searched for an id
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * A empty list is returned
     */
    @Test
    public void testGetListModelObjectByIdListOfTString_emptyList() {
        List<? extends ITree> modelObjectList = FormulaEvaluatorUtil.getListModelObjectById(new ArrayList<>(),
                "id1");
        assertTrue(modelObjectList.isEmpty());
    }

    /**
     * <strong>Scenario:</strong><br>
     * A list not containing the wanted id is searched
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * A empty list is returned
     */
    @Test
    public void testGetListModelObjectByIdListOfTString_notInList() {
        when(treePC.getId()).thenReturn("id1");
        List<? extends ITree> modelObjectList = FormulaEvaluatorUtil.getListModelObjectById(List.of(tree), "id2");
        assertTrue(modelObjectList.isEmpty());
    }

    /**
     * <strong>Scenario:</strong><br>
     * A not product configured model object is tested
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * {@code null} is returned
     */
    @Test
    public void testGetListModelObjectByIdListOfTString_notConfigurable() {
        List<? extends IBranch> modelObjectList = FormulaEvaluatorUtil.getListModelObjectById(List.of(branch),
                "id1");
        assertTrue(modelObjectList.isEmpty());
    }

    @Test
    public void testExistsHelper() {
        assertEquals(true, new ExistsHelper() {

            @Override
            protected boolean existsInternal() {
                return true;
            }
        }.exists());
        assertEquals(false, new ExistsHelper() {

            @Override
            protected boolean existsInternal() {
                return false;
            }
        }.exists());
        assertEquals(false, new ExistsHelper() {

            @Override
            protected boolean existsInternal() {
                throw new IllegalStateException();
            }
        }.exists());
    }

    @Test
    public void testFunctionWithListAsArgumentHelperEmptyList() {
        FunctionWithListAsArgumentHelper<Integer> helper = setUpFunctionWithListArgumentHelperSum();
        List<Integer> emptyList = new ArrayList<>();
        assertEquals(Integer.valueOf(0), helper.getResult(emptyList));
    }

    @Test
    public void testFunctionWithListAsArgumentHelperOneElementList() {
        FunctionWithListAsArgumentHelper<Integer> helper = setUpFunctionWithListArgumentHelperSum();
        List<Integer> oneElementList = setUpIntList(21);
        assertEquals(21, helper.getResult(oneElementList).intValue());
    }

    @Test
    public void testFunctionWithListAsArgumentHelperSum() {
        FunctionWithListAsArgumentHelper<Integer> helper = setUpFunctionWithListArgumentHelperSum();
        List<Integer> manyElementList = setUpIntList(47, 11, 8, 15);
        assertEquals(81, helper.getResult(manyElementList).intValue());
    }

    protected FunctionWithListAsArgumentHelper<Integer> setUpFunctionWithListArgumentHelperSum() {
        return new FunctionWithListAsArgumentHelper<>() {
            @Override
            public Integer getPreliminaryResult(Integer currentResult, Integer nextElement) {
                return currentResult + nextElement;
            }

            @Override
            public Integer getFallBackValue() {
                return 0;
            }
        };
    }

    protected List<Integer> setUpIntList(Integer... values) {
        return new ArrayList<>(List.of(values));
    }

    @Test
    public void testFunctionWithListAsArgumentHelperMax() {
        FunctionWithListAsArgumentHelper<Integer> helper = setUpFunctionWithListArgumentHelperMax();
        List<Integer> manyElementList = setUpIntList(47, 11, 8, 15);
        assertEquals(47, helper.getResult(manyElementList).intValue());
    }

    @Test
    public void testFunctionWithEmptyListAsArgumentHelperMax() {
        FunctionWithListAsArgumentHelper<Integer> helper = setUpFunctionWithListArgumentHelperMax();
        List<Integer> emptyList = new ArrayList<>();
        assertEquals(Integer.valueOf(0), helper.getResult(emptyList));
    }

    @Test
    public void testFunctionWithOneListAsArgumentHelperMax() {
        FunctionWithListAsArgumentHelper<Integer> helper = setUpFunctionWithListArgumentHelperMax();
        List<Integer> oneElementList = setUpIntList(21);
        assertEquals(21, helper.getResult(oneElementList).intValue());
    }

    protected FunctionWithListAsArgumentHelper<Integer> setUpFunctionWithListArgumentHelperMax() {
        return new FunctionWithListAsArgumentHelper<>() {
            @Override
            public Integer getPreliminaryResult(Integer currentResult, Integer nextElement) {
                return Math.max(currentResult, nextElement);
            }

            @Override
            public Integer getFallBackValue() {
                return 0;
            }
        };
    }
}
