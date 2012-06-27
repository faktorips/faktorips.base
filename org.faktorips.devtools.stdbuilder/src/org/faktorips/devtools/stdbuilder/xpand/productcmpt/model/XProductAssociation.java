/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.productcmpt.model;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XAssociation;
import org.faktorips.devtools.stdbuilder.xpand.model.XClass;

public class XProductAssociation extends XAssociation {

    public XProductAssociation(IProductCmptTypeAssociation association, GeneratorModelContext context,
            ModelService modelService) {
        super(association, context, modelService);
    }

    @Override
    public IProductCmptTypeAssociation getAssociation() {
        return (IProductCmptTypeAssociation)super.getAssociation();
    }

    public String getTargetClassGenerationName() {
        IType target = getTargetType();
        XClass modelNode = getModelNode(target, XProductCmptGenerationClass.class);
        // TODO FIPS-1059
        return addImport(modelNode.getQualifiedName(BuilderAspect.INTERFACE));
    }

    public String getMethodNameGetTargetGeneration() {
        IType target = getTargetType();
        XProductCmptClass modelNode = getModelNode(target, XProductCmptClass.class);
        return modelNode.getMethodNameGetProductComponentGeneration();
    }

    public String getMethodNameGetLinksFor() {
        return getMethodNameGetLinksFor(isOneToMany());
    }

    public String getMethodNameGetLinkFor() {
        return getMethodNameGetLinksFor(false);
    }

    private String getMethodNameGetLinksFor(boolean plural) {
        return getJavaNamingConvention().getMultiValueGetterMethodName(
                "Link" + (plural ? "s" : "") + "For" + StringUtils.capitalize(getName(plural)));
    }

    public String getMethodNameGetCardinalityFor() {
        String matchingSingularName;
        try {
            matchingSingularName = StringUtils.capitalize(getAssociation().findMatchingPolicyCmptTypeAssociation(
                    getIpsProject()).getTargetRoleSingular());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return getJavaNamingConvention().getGetterMethodName("CardinalityFor" + matchingSingularName);
    }

    public boolean hasMatchingAssociation() {
        try {
            return getAssociation().constrainsPolicyCmptTypeAssociation(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

}
