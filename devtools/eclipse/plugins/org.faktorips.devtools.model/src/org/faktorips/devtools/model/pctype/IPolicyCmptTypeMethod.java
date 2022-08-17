/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.pctype;

import org.faktorips.devtools.model.type.IMethod;

/**
 * An {@link IMethod} that is part of an {@link IPolicyCmptType}.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 */
public interface IPolicyCmptTypeMethod extends IMethod {

    /**
     * Returns the {@link IPolicyCmptType} this {@link IPolicyCmptTypeMethod} belongs to.
     */
    IPolicyCmptType getPolicyCmptType();

}
