/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.runtime.Message;
import org.junit.Test;

/**
 *
 */
public class CompilationResultImplTest {

    @Test
    public void testSuccessfullFailed() {
        AbstractCompilationResult<JavaCodeFragment> result = new CompilationResultImpl("blabla", Datatype.STRING);
        assertTrue(result.successfull());
        assertFalse(result.failed());
        result.addMessage(Message.newInfo("1", "blabla"));
        assertTrue(result.successfull());
        assertFalse(result.failed());
        result.addMessage(Message.newError("1", "blabla"));
        assertFalse(result.successfull());
        assertTrue(result.failed());
    }

    @Test
    public void testToString() {
        AbstractCompilationResult<JavaCodeFragment> result = new CompilationResultImpl("blabla", Datatype.STRING);
        result.toString();
        result = new CompilationResultImpl(Message.newError("1", "blabla"));
        result.toString();
    }

}
