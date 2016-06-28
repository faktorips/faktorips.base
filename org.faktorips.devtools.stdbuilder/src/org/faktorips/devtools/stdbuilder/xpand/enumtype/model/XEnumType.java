/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xpand.enumtype.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.ComplianceCheck;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XClass;

public class XEnumType extends XClass {

    /** The builder configuration property name that indicates whether to use Java 5 enum types. */
    private static final String USE_JAVA_ENUM_TYPES_CONFIG_PROPERTY = "useJavaEnumTypes"; //$NON-NLS-1$

    public XEnumType(IEnumType enumtype, GeneratorModelContext context, ModelService modelService) {
        super(enumtype, context, modelService);
    }

    @Override
    public boolean isValidForCodeGeneration() {
        try {
            return getIpsObjectPartContainer().isValid(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    protected String getBaseSuperclassName() {
        // TODO
        return "";
    }

    @Override
    public LinkedHashSet<String> getExtendedInterfaces() {
        // TODO
        return new LinkedHashSet<String>();
    }

    @Override
    protected LinkedHashSet<String> getExtendedOrImplementedInterfaces() {
        // TODO Auto-generated method stub
        return new LinkedHashSet<String>();
    }

    @Override
    public LinkedHashSet<String> getImplementedInterfaces() {
        // TODO Auto-generated method stub
        return new LinkedHashSet<String>();
    }

    public IEnumType getEnumType() {
        return (IEnumType)getIpsObjectPartContainer().getIpsObject();
    }

    public String getQualifiedIpsObjectName() {
        return getEnumType().getQualifiedName();
    }

    public List<XEnumAttribute> getAttributeModelNodes(boolean includeSupertypeCopies, boolean includeLiteralName) {
        List<IEnumAttribute> enumAttributes;
        if (includeSupertypeCopies) {
            enumAttributes = getEnumType().getEnumAttributesIncludeSupertypeCopies(includeLiteralName);
        } else {
            enumAttributes = getEnumType().getEnumAttributes(includeLiteralName);
        }
        return new ArrayList<XEnumAttribute>(initNodesForParts(enumAttributes, XEnumAttribute.class));
    }

    public List<XEnumAttribute> getAllAttributeModelNodes() {
        return getAttributeModelNodes(true, true);
    }

    public String getEnumContentQualifiedName() {
        return getEnumType().getEnumContentName();
    }

    public boolean isExtensible() {
        return getEnumType().isExtensible();
    }

    /**
     * Returns whether to generate an enum.
     */
    protected boolean isEnum() {
        return isInterface() ? false : getEnumType().isInextensibleEnum();
    }

    /**
     * Returns whether to generate a class.
     */
    protected boolean isClass() {
        return isInterface() ? false : getEnumType().isExtensible();
    }

    /**
     * Returns whether to generate an interface.
     */
    protected boolean isInterface() {
        return getEnumType().isAbstract();
    }

    /**
     * Returns <code>true</code> if Java 5 enums are available.
     */
    protected boolean isJava5EnumsAvailable() {
        return ComplianceCheck.isComplianceLevelAtLeast5(getIpsProject())
                && getIpsProject().getIpsArtefactBuilderSet().getConfig()
                .getPropertyValueAsBoolean(USE_JAVA_ENUM_TYPES_CONFIG_PROPERTY);
    }

    /* This method is public because it is used in enumXmlAdapterBuilder */
    public String getMethodNameGetIdentifierAttribute() {
        return getIdentifierAttribute().getMethodNameGetter();
    }

    public XEnumAttribute getIdentifierAttribute() {
        return getModelNode(getEnumType().findIdentiferAttribute(getIpsProject()), XEnumAttribute.class);
    }
}
