/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xpand.productcmpt;

import org.faktorips.devtools.stdbuilder.xpand.AbstractAssociationAnnGen;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductAssociation;

public class ProductCmptAssociationAnnGen extends AbstractAssociationAnnGen {

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode ipsElement) {
        return ipsElement instanceof XProductAssociation;
    }

}
