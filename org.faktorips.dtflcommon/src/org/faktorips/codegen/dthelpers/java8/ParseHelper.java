/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.dthelpers.java8;

import org.faktorips.codegen.JavaCodeFragment;

class ParseHelper {

    private ParseHelper() {
        // Utility class that should not be instantiated
    }

    /**
     * Creates a new code fragment that parses the given expression using the the parse method of
     * the given class.
     */
    public static JavaCodeFragment parse(String expression, String className) {
        JavaCodeFragment code = new JavaCodeFragment();
        code.appendClassName(className);
        code.append(".parse("); //$NON-NLS-1$
        code.append(expression);
        code.append(')');
        return code;
    }
}
