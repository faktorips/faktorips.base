/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.refactor.IIpsCompositeMoveRefactoring;
import org.faktorips.devtools.core.refactor.IIpsProcessorBasedRefactoring;
import org.faktorips.devtools.core.refactor.IpsMoveProcessor;
import org.faktorips.devtools.core.refactor.IpsPullUpProcessor;
import org.faktorips.devtools.core.refactor.IpsRenameProcessor;
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
        assertTrue(ipsRenameRefactoring.getIpsRefactoringProcessor() instanceof RenameEnumLiteralNameAttributeValueProcessor);
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
        Set<IIpsObject> ipsObjects = new LinkedHashSet<IIpsObject>(Arrays.asList(mock(IIpsObject.class),
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
