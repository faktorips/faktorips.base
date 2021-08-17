/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.dthelpers;

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;

/** Abstract base class for helpers for time data types. */
public abstract class AbstractTimeHelper extends AbstractDatatypeHelper {

    public AbstractTimeHelper() {
        super();
    }

    public AbstractTimeHelper(Datatype d) {
        super(d);
    }

    @Override
    public JavaCodeFragment newInstance(String value) {
        if (StringUtils.isEmpty(value)) {
            return nullExpression();
        }
        StringBuilder sb = new StringBuilder();
        sb.append('"').append(value).append('"');
        return valueOfExpression(sb.toString());
    }

}
