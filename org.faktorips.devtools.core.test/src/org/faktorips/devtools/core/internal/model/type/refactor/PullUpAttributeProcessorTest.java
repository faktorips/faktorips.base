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

package org.faktorips.devtools.core.internal.model.type.refactor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsRefactoringTest;
import org.junit.Test;

public class PullUpAttributeProcessorTest extends AbstractIpsRefactoringTest {

    @Test
    public void testPullUpPolicyCmptTypeAttribute() throws CoreException {
        performPullUpRefactoring(policyCmptTypeAttribute, superPolicyCmptType);

        // Check that attribute no longer exists in original type
        assertNull(policyCmptType.getAttribute(policyCmptTypeAttribute.getName()));

        // Check that attribute exists in target type
        assertNotNull(superPolicyCmptType.getAttribute(policyCmptTypeAttribute.getName()));
    }

    @Test
    public void testPullUpProductCmptTypeAttribute() throws CoreException {
        performPullUpRefactoring(productCmptTypeAttribute, superProductCmptType);

        // Check that attribute no longer exists in original type
        assertNull(productCmptType.getAttribute(productCmptTypeAttribute.getName()));

        // Check that attribute exists in target type
        assertNotNull(superProductCmptType.getAttribute(productCmptTypeAttribute.getName()));
    }

}
