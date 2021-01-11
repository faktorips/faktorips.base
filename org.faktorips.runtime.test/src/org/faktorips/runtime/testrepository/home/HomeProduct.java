/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.testrepository.home;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.testrepository.PnCProduct;

/**
 * @author Jan Ortmann
 */
public class HomeProduct extends PnCProduct {

    public HomeProduct(IRuntimeRepository registry, String id, String productKindId, String versionId) {
        super(registry, id, productKindId, versionId);
    }

    @Override
    public IConfigurableModelObject createPolicyComponent() {
        return new HomePolicy(this);
    }

}
