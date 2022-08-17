/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.type.AssociationKind;
import org.faktorips.runtime.model.type.PolicyAssociation;

/**
 * Implementation of {@link IVisitorSupport} for {@link IModelObject IModelObjects} not generated
 * implementing {@link IVisitorSupport}.
 *
 * @implNote This implementation uses generic access via the {@link IpsModel}, so generating your
 *               classes with visitor support will be more efficient if you have access to the
 *               project's generator settings.
 * @since 21.6
 */
public class GenericVisitorSupport implements IVisitorSupport {

    private final IModelObject modelObject;

    public GenericVisitorSupport(IModelObject modelObject) {
        this.modelObject = modelObject;
    }

    @Override
    public boolean accept(IModelObjectVisitor visitor) {
        if (!visitor.visit(modelObject)) {
            return false;
        }
        IpsModel.getPolicyCmptType(modelObject).getAssociations().stream()
                .filter(GenericVisitorSupport::shouldFollow)
                .flatMap(a -> a.getTargetObjects(modelObject).stream())
                .map(IVisitorSupport::orGenericVisitorSupport)
                .forEach(c -> c.accept(visitor));
        return true;
    }

    private static boolean shouldFollow(PolicyAssociation association) {
        return !association.isDerivedUnion() && AssociationKind.Composition == association.getAssociationKind();
    }

}
