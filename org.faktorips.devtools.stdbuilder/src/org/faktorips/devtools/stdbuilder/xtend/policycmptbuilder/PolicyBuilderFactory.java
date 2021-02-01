package org.faktorips.devtools.stdbuilder.xtend.policycmptbuilder;

/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.stdbuilder.IIpsArtefactBuilderFactory;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

public class PolicyBuilderFactory implements IIpsArtefactBuilderFactory {

    @Override
    public IIpsArtefactBuilder createBuilder(StandardBuilderSet builderSet) {
        return new PolicyCmptClassBuilderBuilder(builderSet, builderSet.getGeneratorModelContext(),
                builderSet.getModelService());
    }

}
