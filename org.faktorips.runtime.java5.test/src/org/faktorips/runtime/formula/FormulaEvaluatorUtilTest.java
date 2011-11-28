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

package org.faktorips.runtime.formula;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IModelTypeAssociation;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link FormulaEvaluatorUtil}.
 * 
 * @author Daniel Schwering, Faktor Zehn AG
 */
public class FormulaEvaluatorUtilTest {

    private ITree tree;
    private IBranch branch;
    private IModelTypeAssociation associationTreeToBranch;
    private IRuntimeRepository repository;
    private IModelType treeModelType;
    private IProductComponent treePC;

    public interface ITree extends IConfigurableModelObject {
    }

    public interface IBranch extends IModelObject {
    }

    @Before
    public void setUp() throws Exception {
        tree = mock(ITree.class);
        treePC = mock(IProductComponent.class);
        when(tree.getProductComponent()).thenReturn(treePC);
        branch = mock(IBranch.class);
        associationTreeToBranch = mock(IModelTypeAssociation.class);
        when(associationTreeToBranch.getName()).thenReturn("branch");
        treeModelType = mock(IModelType.class);
        when(treeModelType.getAssociation(associationTreeToBranch.getName())).thenReturn(associationTreeToBranch);
        repository = mock(IRuntimeRepository.class);
        when(repository.getModelType(tree)).thenReturn(treeModelType);
    }

    /**
     * <strong>Scenario:</strong><br>
     * A single source object has one object of the target type.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The target object is returned
     */
    @Test
    public void testGetTargets_singleTarget() {
        when(associationTreeToBranch.getTargetObjects(tree)).thenReturn(Arrays.asList((IModelObject)branch));
        List<IBranch> targets = FormulaEvaluatorUtil.getTargets(Arrays.asList(tree), associationTreeToBranch.getName(),
                IBranch.class, repository);
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
    public void testGetTargets_multipleTargets() {
        IBranch branch2 = mock(IBranch.class);
        when(associationTreeToBranch.getTargetObjects(tree)).thenReturn(
                Arrays.asList((IModelObject)branch, (IModelObject)branch2));
        List<IBranch> targets = FormulaEvaluatorUtil.getTargets(Arrays.asList(tree), associationTreeToBranch.getName(),
                IBranch.class, repository);
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
    public void testGetTargets_multipleSourcesAndTargets() {
        IBranch branch2 = mock(IBranch.class);
        when(associationTreeToBranch.getTargetObjects(tree)).thenReturn(
                Arrays.asList((IModelObject)branch, (IModelObject)branch2));
        ITree tree2 = mock(ITree.class);
        IBranch branch3 = mock(IBranch.class);
        when(repository.getModelType(tree2)).thenReturn(treeModelType);
        when(associationTreeToBranch.getTargetObjects(tree2)).thenReturn(
                Arrays.asList((IModelObject)branch2, (IModelObject)branch3));
        List<IBranch> targets = FormulaEvaluatorUtil.getTargets(Arrays.asList(tree, tree2),
                associationTreeToBranch.getName(), IBranch.class, repository);
        assertNotNull(targets);
        assertEquals(3, targets.size());
        assertEquals(branch, targets.get(0));
        assertEquals(branch2, targets.get(1));
        assertEquals(branch3, targets.get(2));
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
        ITree modelObject = FormulaEvaluatorUtil.getModelObjectById(Arrays.asList(tree), "id2");
        assertNull(modelObject);

        modelObject = FormulaEvaluatorUtil.getModelObjectById(Arrays.asList(tree), "id1");
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

        modelObject = FormulaEvaluatorUtil.getModelObjectById(Arrays.asList(tree, tree2, tree3, tree4), "id2");
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
        ITree modelObject = FormulaEvaluatorUtil.getModelObjectById(new ArrayList<ITree>(), "id1");
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
        ITree modelObject = FormulaEvaluatorUtil.getModelObjectById(Arrays.asList(tree), "id2");
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
        ITree modelObject = FormulaEvaluatorUtil.getModelObjectById(Arrays.asList(tree), "id1");
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
        IBranch modelObject = FormulaEvaluatorUtil.getModelObjectById(Arrays.asList(branch), "id1");
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

}
