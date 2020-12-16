/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype;

import org.faktorips.devtools.core.internal.model.type.Method;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IType;

/**
 * Implementation of {@link IPolicyCmptTypeMethod}, please see the interface for more details.
 * 
 * @author Alexander Weickmann
 */
public class PolicyCmptTypeMethod extends Method implements IPolicyCmptTypeMethod {

    public PolicyCmptTypeMethod(IType parent, String id) {
        super(parent, id);
    }

    @Override
    public IPolicyCmptType getPolicyCmptType() {
        return (IPolicyCmptType)getIpsObject();
    }
}
