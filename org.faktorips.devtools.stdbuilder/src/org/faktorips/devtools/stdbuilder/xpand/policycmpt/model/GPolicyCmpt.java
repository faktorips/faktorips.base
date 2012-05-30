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
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelObject;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.PolicyXpandBuilder;
import org.faktorips.runtime.internal.AbstractConfigurableModelObject;
import org.faktorips.runtime.internal.AbstractModelObject;

public class GPolicyCmpt extends AbstractGeneratorModelObject {

    private ArrayList<GPolicyAttribute> attributes;

    public GPolicyCmpt(IPolicyCmptType policyCmptType, PolicyXpandBuilder policyBuilder) {
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

}
