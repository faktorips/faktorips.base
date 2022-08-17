/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.refactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.faktorips.devtools.core.internal.model.enums.refactor.PullUpEnumAttributeProcessor;
import org.faktorips.devtools.core.internal.model.enums.refactor.RenameEnumAttributeProcessor;
import org.faktorips.devtools.core.internal.model.enums.refactor.RenameEnumLiteralNameAttributeValueProcessor;
import org.faktorips.devtools.core.internal.model.ipsobject.refactor.MoveIpsObjectProcessor;
import org.faktorips.devtools.core.internal.model.ipsobject.refactor.RenameIpsObjectProcessor;
import org.faktorips.devtools.core.internal.model.type.refactor.PullUpAttributeProcessor;
import org.faktorips.devtools.core.internal.model.type.refactor.RenameAssociationProcessor;
import org.faktorips.devtools.core.internal.model.type.refactor.RenameAttributeProcessor;
import org.faktorips.devtools.core.internal.model.type.refactor.RenameValidationRuleProcessor;
import org.faktorips.devtools.core.refactor.IIpsCompositeMoveRefactoring;
import org.faktorips.devtools.core.refactor.IIpsProcessorBasedRefactoring;
import org.faktorips.devtools.core.refactor.IpsMoveProcessor;
import org.faktorips.devtools.core.refactor.IpsPullUpProcessor;
import org.faktorips.devtools.core.refactor.IpsRenameProcessor;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IType;
import org.junit.Before;
import org.junit.Test;

public class IpsRefactoringFactoryTest {

    private IpsRefactoringFactory ipsRefactoringFactory;

    @Before
    public void setUp() {
        ipsRefactoringFactory = new IpsRefactoringFactory();
    }

    @Test
    public void testCreateRenameRefactoringEnumLiteralNameAttributeValue() {
        IIpsProcessorBasedRefactoring ipsRenameRefactoring = ipsRefactoringFactory
                .createRenameRefactoring(mock(IEnumLiteralNameAttributeValue.class));
        assertTrue(ipsRenameRefactoring
                .getIpsRefactoringProcessor() instanceof RenameEnumLiteralNameAttributeValueProcessor);
    }

    @Test
    public void testCreateRenameRefactoringEnumAttribute() {
        IIpsProcessorBasedRefactoring ipsRenameRefactoring = ipsRefactoringFactory
                .createRenameRefactoring(mock(IEnumAttribute.class));
        assertTrue(ipsRenameRefactoring.getIpsRefactoringProcessor() instanceof RenameEnumAttributeProcessor);
    }

    @Test
    public void testCreateRenameRefactoringAttribute() {
        IIpsProcessorBasedRefactoring ipsRenameRefactoring = ipsRefactoringFactory
                .createRenameRefactoring(mock(IAttribute.class));
        assertTrue(ipsRenameRefactoring.getIpsRefactoringProcessor() instanceof RenameAttributeProcessor);
    }

    @Test
    public void testCreateRenameRefactoringAssociation() {
        IIpsProcessorBasedRefactoring ipsRenameRefactoring = ipsRefactoringFactory
                .createRenameRefactoring(mock(IAssociation.class));
        assertTrue(ipsRenameRefactoring.getIpsRefactoringProcessor() instanceof RenameAssociationProcessor);
    }

    @Test
    public void testCreateRenameRefactoringIpsObject() {
        IIpsProcessorBasedRefactoring ipsRenameRefactoring = ipsRefactoringFactory
                .createRenameRefactoring(mock(IIpsObject.class));
        assertTrue(ipsRenameRefactoring.getIpsRefactoringProcessor() instanceof RenameIpsObjectProcessor);
    }

    @Test
    public void testCreateRenameRefactoringIpsPackageFragement() {
        IIpsProcessorBasedRefactoring ipsRenameRefactoring = ipsRefactoringFactory
                .createRenameRefactoring(mock(IIpsPackageFragment.class));
        assertTrue(ipsRenameRefactoring.getIpsRefactoringProcessor() instanceof RenameIpsPackageFragmentProcessor);
    }

    @Test
    public void testCreateRenameRefactoringValidationRule() {
        IIpsProcessorBasedRefactoring ipsRenameRefactoring = ipsRefactoringFactory
                .createRenameRefactoring(mock(IValidationRule.class));
        assertTrue(ipsRenameRefactoring.getIpsRefactoringProcessor() instanceof RenameValidationRuleProcessor);
    }

    @Test
    public void testCreateRenameRefactoringNotSupportedIpsElement() {
        assertNull(ipsRefactoringFactory.createRenameRefactoring(mock(IIpsElement.class)));
    }

    @Test
    public void testCreateFullyConfiguredRenameRefactoring() {
        IIpsProcessorBasedRefactoring ipsRenameRefactoring = ipsRefactoringFactory.createRenameRefactoring(
                mock(IIpsObject.class), "NewName", "NewPluralName", true);

        IpsRenameProcessor ipsRenameProcessor = (IpsRenameProcessor)ipsRenameRefactoring.getIpsRefactoringProcessor();
        assertEquals("NewName", ipsRenameProcessor.getNewName());
        assertEquals("NewPluralName", ipsRenameProcessor.getNewPluralName());
        assertTrue(ipsRenameProcessor.isAdaptRuntimeId());
    }

    @Test
    public void testCreateFullyConfiguredRenameRefactoringNewPluralNameIsNull() {
        ipsRefactoringFactory.createRenameRefactoring(mock(IIpsObject.class), "NewName", null, true);
        // Test successful if no NPE was thrown
    }

    @Test
    public void testCreateMoveRefactoringIpsObject() {
        IIpsProcessorBasedRefactoring ipsMoveRefactoring = ipsRefactoringFactory
                .createMoveRefactoring(mock(IIpsObject.class));
        assertTrue(ipsMoveRefactoring.getIpsRefactoringProcessor() instanceof MoveIpsObjectProcessor);
    }

    @Test
    public void testCreateFullyConfiguredMoveRefactoring() {
        IIpsPackageFragment targetIpsPackageFragment = mock(IIpsPackageFragment.class);
        IIpsProcessorBasedRefactoring ipsMoveRefactoring = ipsRefactoringFactory.createMoveRefactoring(
                mock(IIpsObject.class), targetIpsPackageFragment);

        IpsMoveProcessor ipsMoveProcessor = (IpsMoveProcessor)ipsMoveRefactoring.getIpsRefactoringProcessor();
        assertEquals(targetIpsPackageFragment, ipsMoveProcessor.getTargetIpsPackageFragment());
    }

    @Test
    public void testCreateFullyConfiguredCompositeMoveRefactoring() {
        Set<IIpsElement> ipsObjects = new LinkedHashSet<>(Arrays.asList(mock(IIpsObject.class),
                mock(IIpsObject.class)));
        IIpsPackageFragment targetIpsPackageFragment = mock(IIpsPackageFragment.class);

        IIpsCompositeMoveRefactoring ipsCompositeMoveRefactoring = ipsRefactoringFactory
                .createCompositeMoveRefactoring(ipsObjects, targetIpsPackageFragment);

        assertEquals(2, ipsCompositeMoveRefactoring.getNumberOfRefactorings());
        assertEquals(targetIpsPackageFragment, ipsCompositeMoveRefactoring.getTargetIpsPackageFragment());
    }

    @Test
    public void testCreatePullUpRefactoringAttribute() {
        IIpsProcessorBasedRefactoring ipsPullUpRefactoring = ipsRefactoringFactory
                .createPullUpRefactoring(mock(IAttribute.class));
        assertTrue(ipsPullUpRefactoring.getIpsRefactoringProcessor() instanceof PullUpAttributeProcessor);
    }

    @Test
    public void testCreatePullUpRefactoringEnumAttribute() {
        IIpsProcessorBasedRefactoring ipsPullUpRefactoring = ipsRefactoringFactory
                .createPullUpRefactoring(mock(IEnumAttribute.class));
        assertTrue(ipsPullUpRefactoring.getIpsRefactoringProcessor() instanceof PullUpEnumAttributeProcessor);
    }

    @Test
    public void testCreatePullUpRefactoringNotSupportedIpsObjectPart() {
        assertNull(ipsRefactoringFactory.createPullUpRefactoring(mock(IIpsObjectPart.class)));
    }

    @Test
    public void testCreateFullyConfiguredPullUpRefactoring() {
        IType targetType = mock(IType.class);
        IIpsProcessorBasedRefactoring ipsPullUpRefactoring = ipsRefactoringFactory.createPullUpRefactoring(
                mock(IAttribute.class), targetType);

        IpsPullUpProcessor ipsPullUpProcessor = (IpsPullUpProcessor)ipsPullUpRefactoring.getIpsRefactoringProcessor();
        assertEquals(targetType, ipsPullUpProcessor.getTarget());
    }

}
