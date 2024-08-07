/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.propertydef;

import org.faktorips.runtime.internal.AbstractModelObject;

public class PolicyBaseClassBuilderSetPropertyDef extends AbstractBaseClassBuilderSetPropertyDef {

    @Override
    protected Class<AbstractModelObject> getRequiredSuperClass() {
        return AbstractModelObject.class;
    }

}
