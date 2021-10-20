/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.enums.refactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.faktorips.abstracttest.core.AbstractIpsRefactoringTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.refactor.IpsRefactoringProcessor;
import org.faktorips.devtools.core.refactor.IpsRenameProcessor;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.junit.Test;

public class RenameEnumAttributeProcessorTest extends AbstractIpsRefactoringTest {

    @Test
    public void testCheckInitialConditionsValid() throws OperationCanceledException, CoreException {
        IpsRefactoringProcessor ipsRefactoringProcessor = new RenameEnumAttributeProcessor(enumAttribute);
        RefactoringStatus status = ipsRefactoringProcessor.checkInitialConditions(new NullProgressMonitor());
        assertFalse(status.hasError());
    }

    @Test
    public void testCheckFinalConditionsValid() throws OperationCanceledException, CoreException {
        IpsRenameProcessor ipsRenameProcessor = new RenameEnumAttributeProcessor(enumAttribute);
        ipsRenameProcessor.setNewName("test");
        RefactoringStatus status = ipsRenameProcessor.checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertFalse(status.hasError());
    }

    @Test
    public void testCheckFinalConditionsInvalidAttributeName() throws OperationCanceledException, CoreException {
        // Create another enumeration attribute to test against
        IEnumAttribute otherEnumAttribute = enumType.newEnumAttribute();
        otherEnumAttribute.setName("otherEnumAttribute");

        IpsRenameProcessor ipsRenameProcessor = new RenameEnumAttributeProcessor(enumAttribute);
        ipsRenameProcessor.setNewName("otherEnumAttribute");
        RefactoringStatus status = ipsRenameProcessor.checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertTrue(status.hasError());
    }

    @Test
    public void testRenameEnumAttribute() throws CoreException {
        enumType.setAbstract(true);

        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setEnumContentName(enumType.getEnumContentName());
        subEnumType.setSuperEnumType(enumType.getQualifiedName());

        IEnumAttribute inheritedEnumAttribute = subEnumType.newEnumAttribute();
        inheritedEnumAttribute.setName(enumAttribute.getName());
        inheritedEnumAttribute.setInherited(true);

        String newAttributeName = "newAttributeName";
        performRenameRefactoring(enumAttribute, newAttributeName);

        // Check for changed attribute name
        assertNull(enumType.getEnumAttribute(ENUM_ATTRIBUTE_NAME));
        assertNotNull(enumType.getEnumAttribute(newAttributeName));
        assertTrue(enumAttribute.getName().equals(newAttributeName));

        // Check for inherited attribute update
        assertNull(subEnumType.getEnumAttributeIncludeSupertypeCopies(ENUM_ATTRIBUTE_NAME));
        assertNotNull(subEnumType.getEnumAttributeIncludeSupertypeCopies(newAttributeName));
        assertEquals(newAttributeName, inheritedEnumAttribute.getName());
    }

    @Test
    public void testRenameEnumAttributeReferencedByEnumContent() throws CoreException {
        String newAttributeName = "foo";
        performRenameRefactoring(enumAttribute, newAttributeName);

        // Check for enum content reference update
        assertNull(enumContent.getEnumAttributeReference(ENUM_ATTRIBUTE_NAME));
        assertEquals(newAttributeName, enumContent.getEnumAttributeReference(newAttributeName).getName());
    }

    @Test
    public void testRenameEnumAttributeUsedInLiteralName() throws CoreException {
        IEnumType modelEnumType = newEnumType(ipsProject, "ModelEnumType");
        modelEnumType.setExtensible(false);

        IEnumAttribute enumAttribute = modelEnumType.newEnumAttribute();
        enumAttribute.setName("id");
        enumAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        enumAttribute.setIdentifier(true);
        enumAttribute.setUnique(true);
        enumAttribute.setUsedAsNameInFaktorIpsUi(true);

        IEnumLiteralNameAttribute literalNameAttribute = modelEnumType.newEnumLiteralNameAttribute();
        literalNameAttribute.setDefaultValueProviderAttribute(enumAttribute.getName());

        String newAttributeName = "newAttributeName";
        performRenameRefactoring(enumAttribute, newAttributeName);

        assertEquals(newAttributeName, literalNameAttribute.getDefaultValueProviderAttribute());
    }

}
