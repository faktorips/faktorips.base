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
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
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
        return modelNode.getSimpleName(BuilderAspect.INTERFACE);
    }

    public String getMethodNameGetTargetGeneration() {
        IType target = getTargetType();
        XProductCmptGenerationClass modelNode = getModelNode(target, XProductCmptGenerationClass.class);
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
            matchingSingularName = StringUtils.capitalize(getNameOfMatchingAssociation());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return getJavaNamingConvention().getGetterMethodName("CardinalityFor" + matchingSingularName);
    }

    public String getNameOfMatchingAssociation() throws CoreException {
        return getAssociation().findMatchingPolicyCmptTypeAssociation(getIpsProject()).getTargetRoleSingular();
    }

    public boolean hasMatchingAssociation() {
        try {
            return getAssociation().constrainsPolicyCmptTypeAssociation(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Returns the key used to localize java doc. The parameter gives the prefix for the key
     * specifying the scope of generated code. This method adds "ONE" or "MANY" depending on the
     * kind of the association to differ between one to many and one to one associations.
     * 
     * @param prefix The prefix defining the scope of the generated code, for example
     *            "METHOD_GET_CMPT"
     * @return The key used to localize the java doc for example "METHOD_GET_CMPT_ONE" or
     *         "METHOD_GET_CMPT_MANY"
     */
    public String getJavadocKey(String prefix) {
        return prefix + (isOneToMany() ? "_MANY" : "_ONE");
    }

}
