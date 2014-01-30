/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategyFactory;
import org.faktorips.util.ArgumentCheck;

/**
 * Factory for {@link DateBasedProductCmptNamingStrategyFactory}.
 * 
 * @author Jan Ortmann
 */
public class DateBasedProductCmptNamingStrategyFactory implements IProductCmptNamingStrategyFactory {

    @Override
    public String getExtensionId() {
        return new DateBasedProductCmptNamingStrategy().getExtensionId();
    }

    @Override
    public IProductCmptNamingStrategy newProductCmptNamingStrategy(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);
        DateBasedProductCmptNamingStrategy newStrategy = new DateBasedProductCmptNamingStrategy();
        newStrategy.setIpsProject(ipsProject);
        return newStrategy;
    }

}
