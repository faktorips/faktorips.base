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

package org.faktorips.devtools.stdbuilder.xpand.policycmpt.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.productcmpttype.BaseProductCmptTypeBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelObject;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.PolicyCmptImplClassBuilder;
import org.faktorips.runtime.INotificationSupport;
import org.faktorips.runtime.internal.AbstractConfigurableModelObject;
import org.faktorips.runtime.internal.AbstractModelObject;

public class GPolicyCmpt extends AbstractGeneratorModelObject {

    private ArrayList<GPolicyAttribute> attributes;

    public GPolicyCmpt(IPolicyCmptType policyCmptType, PolicyCmptImplClassBuilder policyBuilder) {
        super(policyCmptType, policyBuilder);
    }

    @Override
    public IPolicyCmptType getIpsObjectPartContainer() {
        return (IPolicyCmptType)super.getIpsObjectPartContainer();
    }

    /**
     * @return Returns the policyCmptType.
     */
    public IPolicyCmptType getPolicyCmptType() {
        return getIpsObjectPartContainer();
    }

    public String getFileName() {
        try {
            return getBuilder().getRelativeJavaFile(getBuilder().getIpsSrcFile()).toOSString();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public String getSimpleName() {
        try {
            return getBuilder().getUnqualifiedClassName();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public String getQualifiedName() {
        try {
            return getBuilder().getQualifiedClassName();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public String getPackageName() {
        try {
            return getBuilder().getPackage();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public boolean hasSupertype() {
        return getPolicyCmptType().hasSupertype();
    }

    public boolean isConfigured() {
        return getPolicyCmptType().isConfigurableByProductCmptType();
    }

    // TODO refactor
    public String getProductCmptClassName() {
        try {
            GenProductCmptType generator = getBuilder().getBuilderSet().getGenerator(
                    getPolicyCmptType().findProductCmptType(getIpsObjectPartContainer().getIpsProject()));
            return addImport(generator.getQualifiedName(true));
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public boolean isAggregateRoot() {
        try {
            return getPolicyCmptType().isAggregateRoot();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public boolean isImplementsInterface() {
        return !getImplementedInterface().isEmpty();
    }

    public List<String> getImplementedInterface() {
        ArrayList<String> list = new ArrayList<String>();
        if (isGeneratePropertyChange() && !hasSupertype()) {
            list.add(addImport(INotificationSupport.class));
        }
        return list;
    }

    public String getSuperclassName() {
        try {
            if (getPolicyCmptType().hasSupertype()) {
                IType superType = getPolicyCmptType().findSupertype(getBuilder().getIpsProject());
                return addImport(getBuilder().getQualifiedClassName(superType));
            } else {
                if (isConfigured()) {
                    return addImport(AbstractConfigurableModelObject.class);
                } else {
                    return addImport(AbstractModelObject.class);
                }
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public List<GPolicyAttribute> getAttributes() {
        if (attributes == null) {
            attributes = new ArrayList<GPolicyAttribute>();
            for (IPolicyCmptTypeAttribute attribute : getPolicyCmptType().getPolicyCmptTypeAttributes()) {
                attributes.add(new GPolicyAttribute(this, attribute, getBuilder()));
            }
        }
        return attributes;
    }

    public boolean isGeneratePropertyChange() {
        return getBuilder().getBuilderSet().getConfig()
                .getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_CHANGELISTENER).booleanValue();
    }

    /**
     * Searches and returns the Java type generated for the <tt>IProductCmptType</tt> configuring
     * the <tt>IPolicyCmptType</tt> this generator is configured for.
     * <p>
     * Returns <tt>null</tt> if the <tt>IProductCmptType</tt> cannot be found.
     * 
     * @param forInterface Flag indicating whether to search for the published interface of the
     *            <tt>IProductCmptType</tt> (<tt>true</tt>) or for it's implementation (
     *            <tt>false</tt>).
     * 
     * @throws CoreException If an error occurs while searching for the <tt>IProductCmptType</tt>.
     */
    public org.eclipse.jdt.core.IType findGeneratedJavaTypeForProductCmptType(boolean forInterface)
            throws CoreException {
        BaseProductCmptTypeBuilder productCmptTypeBuilder = forInterface ? getBuilderSet()
                .getProductCmptInterfaceBuilder() : getBuilderSet().getProductCmptImplClassBuilder();

        IProductCmptType productCmptType = getPolicyCmptType().findProductCmptType(getPolicyCmptType().getIpsProject());
        if (productCmptType == null) {
            return null;
        }
        return productCmptTypeBuilder.getGeneratedJavaTypes(productCmptType).get(0);
    }

}
