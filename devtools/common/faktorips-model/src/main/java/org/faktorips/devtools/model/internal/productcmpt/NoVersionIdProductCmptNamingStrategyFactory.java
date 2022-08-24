/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategyFactory;
import org.faktorips.util.ArgumentCheck;

/**
 * Factory for {@link NoVersionIdProductCmptNamingStrategy}.
 * 
 * @author Jan Ortmann
 */
public class NoVersionIdProductCmptNamingStrategyFactory implements IProductCmptNamingStrategyFactory {

    @Override
    public String getExtensionId() {
        return new NoVersionIdProductCmptNamingStrategy().getExtensionId();
    }

    @Override
    public IProductCmptNamingStrategy newProductCmptNamingStrategy(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);
        NoVersionIdProductCmptNamingStrategy newStrategy = new NoVersionIdProductCmptNamingStrategy();
        newStrategy.setIpsProject(ipsProject);
        return newStrategy;
    }

}
