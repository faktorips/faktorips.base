/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel;

import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;

public class GenericGeneratorModelNode extends AbstractGeneratorModelNode {

    /**
     * This is an empty implementation of {@link AbstractGeneratorModelNode}.
     * 
     */
    public GenericGeneratorModelNode(IIpsObjectPartContainer ipsObjectPartContainer, GeneratorModelContext context,
            ModelService modelService) {
        super(ipsObjectPartContainer, context, modelService);
    }

}
