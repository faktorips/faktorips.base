/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.builder.flidentifier.ast;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.util.TextRegion;

/**
 * The qualified node is a special node that always follows an {@link AssociationNode}. It
 * represents an identifier part that was qualified by the name of a product component. The
 * resulting {@link Datatype} will always be a subclass of {@link IType} or a
 * {@link ListOfTypeDatatype} with {@link IType} as basis type.
 * 
 * @author dirmeier
 */
public class QualifierNode extends IdentifierNode {

    private final IProductCmpt productCmpt;

    QualifierNode(IProductCmpt productCmpt, IType targetType, boolean listOfTypes, TextRegion textRegion) {
        super(targetType, listOfTypes, textRegion);
        this.productCmpt = productCmpt;
    }

    public String getRuntimeId() {
        return getProductCmpt().getRuntimeId();
    }

    public IProductCmpt getProductCmpt() {
        return productCmpt;
    }

}
