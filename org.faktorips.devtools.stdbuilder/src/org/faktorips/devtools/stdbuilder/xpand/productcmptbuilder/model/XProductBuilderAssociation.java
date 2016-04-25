/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xpand.productcmptbuilder.model;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductAssociation;

public class XProductBuilderAssociation extends XProductAssociation {

    public XProductBuilderAssociation(IProductCmptTypeAssociation association, GeneratorModelContext context,
            ModelService modelService) {
        super(association, context, modelService);
    }

    public XProductBuilder getTargetBuilderClass() {
        return getModelNode(getTargetProductCmptClass().getType(), XProductBuilder.class);
    }

    /**
     * Returns whether this association is marked as changing over time (<code>true</code>) or as
     * static (<code>false</code>).
     */
    public boolean isChangingOverTime() {
        return getAssociation().isChangingOverTime();
    }

    /**
     * Checks if override is needed for the association setter. In case an overwritten exists,
     * override is not needed if the overwritten association is a derived union or has an abstract
     * target. Else, an association setter is already generated in super class, thus override is
     * needed.
     * 
     * @return true if '@Override is needed for the association, false is not.
     */
    @Override
    public boolean isNeedOverrideForConstrainNewChildMethod() {
        if (isConstrain()) {
            XProductAssociation superAsso = getSuperAssociationWithSameName();
            if (!superAsso.isDerivedUnion() && !superAsso.isAbstractTarget()) {
                return true;
            } else {
                return getSuperAssociationWithSameName().isNeedOverrideForConstrainNewChildMethod();
            }
        } else {
            return false;
        }
    }

    public boolean isOverwrittenTargetNotAbstract() {
        return !getSuperAssociationWithSameName().isAbstractTarget();
    }

    public String getMethodName() {
        return StringUtils.uncapitalize(getName());
    }

    // public String getMethodNameCreateDefaultAssociation() {
    // return "createDefault" + StringUtils.capitalize(getName());
    // }
}
