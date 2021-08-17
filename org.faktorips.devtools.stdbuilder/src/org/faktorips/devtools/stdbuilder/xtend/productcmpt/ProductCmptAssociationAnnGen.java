/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.productcmpt;

import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.XDerivedUnionAssociation;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductAssociation;
import org.faktorips.devtools.stdbuilder.xtend.association.AbstractAssociationAnnGen;

public class ProductCmptAssociationAnnGen extends AbstractAssociationAnnGen {

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode node) {
        return node instanceof XProductAssociation || node instanceof XDerivedUnionAssociation;
    }

}
