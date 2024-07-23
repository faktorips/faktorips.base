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

import org.faktorips.runtime.internal.ProductComponent;

public class ProductBaseClassBuilderSetPropertyDef extends AbstractBaseClassBuilderSetPropertyDef {

    @Override
    protected Class<ProductComponent> getRequiredSuperClass() {
        return ProductComponent.class;
    }

}
