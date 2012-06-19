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

package org.faktorips.devtools.stdbuilder.xpand.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;

public abstract class XAssociation extends AbstractGeneratorModelNode {

    public XAssociation(IAssociation association, GeneratorModelContext context, ModelService modelService) {
        super(association, context, modelService);
    }

    @Override
    public IAssociation getIpsObjectPartContainer() {
        return (IAssociation)super.getIpsObjectPartContainer();
    }

    public IAssociation getAssociation() {
        return getIpsObjectPartContainer();
    }

    public String getName(boolean plural) {
        if (plural) {
            return getAssociation().getTargetRolePlural();
        } else {
            return getAssociation().getTargetRoleSingular();
        }
    }

    public String getMemberVarName() {
        return getJavaNamingConvention().getMemberVarName(getName(isOnetoMany()));
    }

    public String getGetterMethodName() {
        return getGetterMethodName(isOnetoMany());
    }

    public String getGetterMethodName(boolean toMany) {
        return getJavaNamingConvention().getGetterMethodName(getName(toMany));
    }

    public String getSetterMethodName() {
        return getJavaNamingConvention().getSetterMethodName(getName());
    }

    /**
     * Returns true if this association is a one to many association and false if it is one to one.
     * 
     * @return true fro one to many and false for one to one associations
     */
    public boolean isOnetoMany() {
        return getAssociation().is1ToMany();
    }

    public boolean isDerived() {
        return getAssociation().isDerived();
    }

    protected IType getTarget() {
        try {
            return getAssociation().findTarget(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public String getTargetClassName() {
        IType target = getTarget();
        XClass modelNode = getModelNode(target, getTargetModelNodeType());
        // TODO FIPS-1059
        return addImport(modelNode.getQualifiedName(BuilderAspect.INTERFACE));
    }

    protected abstract Class<? extends XClass> getTargetModelNodeType();

}
