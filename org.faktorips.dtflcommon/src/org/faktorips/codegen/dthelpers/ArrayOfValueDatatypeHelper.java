/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.dthelpers;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.ArrayOfValueDatatype;
import org.faktorips.datatype.Datatype;

/**
 * A helper class for {@link ArrayOfValueDatatype}. ValueOf and newInstance expressions are not
 * supported by this helper. A call to these method returns a {@code "null"} fragment.
 * 
 * @author Peter Erzberger
 */
public class ArrayOfValueDatatypeHelper extends AbstractDatatypeHelper {

    public ArrayOfValueDatatypeHelper() {
        super();
    }

    public ArrayOfValueDatatypeHelper(Datatype datatype) {
        super(datatype);
    }

    /**
     * Returns a "null" fragment.
     * 
     * {@inheritDoc}
     */
    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        return nullExpression();
    }

    /**
     * Returns a "null" fragment.
     * 
     * {@inheritDoc}
     */
    public JavaCodeFragment newInstance(String value) {
        return nullExpression();
    }

}
