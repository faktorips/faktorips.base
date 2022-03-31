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

import org.faktorips.abstracttest.core.AbstractIpsRefactoringTest;
import org.junit.Test;

public class RenameEnumLiteralNameAttributeValueProcessorTest extends AbstractIpsRefactoringTest {

    @Test
    public void testRenameEnumLiteralNameAttributeValue() {
        performRenameRefactoring(enumLiteralNameAttributeValue, "bar");
        assertEquals("bar", enumLiteralNameAttributeValue.getStringValue());
    }

}
