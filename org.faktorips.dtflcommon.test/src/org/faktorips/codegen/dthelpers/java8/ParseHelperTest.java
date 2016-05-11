package org.faktorips.codegen.dthelpers.java8;

import static org.junit.Assert.assertEquals;

import org.faktorips.codegen.JavaCodeFragment;
import org.junit.Test;

public class ParseHelperTest {

    @Test
    public void testParse() throws Exception {
        JavaCodeFragment fragment = ParseHelper.parse("expression", "className");
        assertEquals("className.parse(expression)", fragment.getSourcecode());
    }

}
