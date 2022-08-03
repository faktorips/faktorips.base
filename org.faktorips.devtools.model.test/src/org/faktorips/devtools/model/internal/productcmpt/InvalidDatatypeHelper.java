/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.AbstractDatatypeHelper;

public class InvalidDatatypeHelper extends AbstractDatatypeHelper {

    public InvalidDatatypeHelper() {
        super(new InvalidDatatype());
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        return null;
    }

    @Override
    public JavaCodeFragment nullExpression() {
        return null;
    }

    @Override
    public JavaCodeFragment newInstance(String value) {
        return null;
    }

    @Override
    public String getJavaClassName() {
        return null;
    }

}
