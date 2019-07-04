/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.enums.refactor;

import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsRefactoringTest;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.junit.Test;

public class PullUpEnumAttributeProcessorIntegrationTest extends AbstractIpsRefactoringTest {

    @Test
    public void testPullUpEnumAttribute() throws CoreException {
        IEnumType superEnumType = newEnumType(ipsProject, "SuperEnumType");
        superEnumType.setAbstract(true);
        enumType.setSuperEnumType(superEnumType.getQualifiedName());

        IEnumType siblingEnumType = newEnumType(ipsProject, "SiblingEnumType");
        siblingEnumType.setSuperEnumType(superEnumType.getQualifiedName());

        performPullUpRefactoring(enumAttribute, superEnumType);

        // Check that the original enum attribute is marked as inherited
        assertTrue(enumAttribute.isInherited());

        // Check that enum attribute exists in target enum type
        assertTrue(superEnumType.containsEnumAttribute(ENUM_ATTRIBUTE_NAME));

        // Check that the pulled up attribute is inherited by other sub types of target enum type
        assertTrue(siblingEnumType.getEnumAttributeIncludeSupertypeCopies(ENUM_ATTRIBUTE_NAME).isInherited());
    }

}
