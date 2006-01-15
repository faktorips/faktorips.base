package org.faktorips.fl;

import junit.framework.TestCase;

import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.Message;

/**
 *
 */
public class CompilationResultImplTest extends TestCase {

    public void testSuccessfullFailed() {
        CompilationResultImpl result = new CompilationResultImpl("blabla", Datatype.STRING);
        assertTrue(result.successfull());
        assertFalse(result.failed());
        result.addMessage(Message.newInfo("1", "blabla"));
        assertTrue(result.successfull());
        assertFalse(result.failed());
        result.addMessage(Message.newError("1", "blabla"));
        assertFalse(result.successfull());
        assertTrue(result.failed());
    }
    
    public void testToString() {
        CompilationResultImpl result = new CompilationResultImpl("blabla", Datatype.STRING);
        result.toString();
        result = new CompilationResultImpl(Message.newError("1", "blabla"));
        result.toString();
    }

}
