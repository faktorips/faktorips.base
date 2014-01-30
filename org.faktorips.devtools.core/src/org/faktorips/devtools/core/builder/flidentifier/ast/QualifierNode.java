/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder.flidentifier.ast;

import org.eclipse.jface.text.Region;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.type.IType;

/**
 * The qualified node is a special node that always follows an {@link AssociationNode}. It
 * represents an identifier part that was qualified by the name of a product component. The
 * resulting {@link Datatype} will always be a subclass of {@link IType} or a
 * {@link ListOfTypeDatatype} with {@link IType} as basis type.
 * 
 * @author dirmeier
 */
public class QualifierNode extends IdentifierNode {

    private final String runtimeId;

    private final IProductCmpt productCmpt;

    QualifierNode(IProductCmpt productCmpt, String runtimeId, IType targetType, boolean listOfTypes, Region region) {
        super(targetType, listOfTypes, region);
        this.productCmpt = productCmpt;
        this.runtimeId = runtimeId;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public IProductCmpt getProductCmpt() {
        return productCmpt;
    }
}
