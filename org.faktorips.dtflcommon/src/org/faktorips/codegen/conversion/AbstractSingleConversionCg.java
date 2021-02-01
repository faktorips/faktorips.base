/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.conversion;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.SingleConversionCg;
import org.faktorips.datatype.Datatype;

/**
 * A ConversionGenerator that ...
 */
public abstract class AbstractSingleConversionCg implements SingleConversionCg<JavaCodeFragment> {

    private Datatype from;
    private Datatype to;

    /**
     * Creates a new ConversionGenerator that converts from Datatype from to Datatype to.
     */
    public AbstractSingleConversionCg(Datatype from, Datatype to) {
        this.from = from;
        this.to = to;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.codegen.SingleConversionCg#getFrom()
     */
    @Override
    public Datatype getFrom() {
        return from;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.codegen.SingleConversionCg#getTo()
     */
    @Override
    public Datatype getTo() {
        return to;
    }

}
