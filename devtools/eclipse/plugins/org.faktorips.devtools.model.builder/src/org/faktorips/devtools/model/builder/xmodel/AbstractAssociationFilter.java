/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.xmodel;

import org.faktorips.devtools.model.type.IAssociation;

public abstract class AbstractAssociationFilter {
    public AbstractAssociationFilter() {
    }

    public abstract boolean isValidAssociation(IAssociation association);
}
