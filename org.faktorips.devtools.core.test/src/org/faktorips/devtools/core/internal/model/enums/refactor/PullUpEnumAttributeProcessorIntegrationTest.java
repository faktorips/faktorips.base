/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.enums.refactor;

import static org.junit.Assert.assertFalse;
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

        performPullUpRefactoring(enumAttribute, superEnumType);

        // Check for no longer existing original enum attribute
        assertFalse(enumType.containsEnumAttribute(ENUM_ATTRIBUTE_NAME));

        // Check that enum attribute exists in target enum type
        assertTrue(enumType.containsEnumAttribute(ENUM_ATTRIBUTE_NAME));
    }

}
