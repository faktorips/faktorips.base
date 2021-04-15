/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.testrepository.motor;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.testrepository.PnCProduct;

/**
 * @author Jan Ortmann
 */
public class MotorProduct extends PnCProduct {

    public MotorProduct(IRuntimeRepository registry, String id, String productKindId, String versionId) {
        super(registry, id, productKindId, versionId);
    }

    protected ProductComponentGeneration createGeneration() {
        return new MotorProductGen(this);
    }

    @Override
    public IConfigurableModelObject createPolicyComponent() {
        return new MotorPolicy(this);
    }

}
