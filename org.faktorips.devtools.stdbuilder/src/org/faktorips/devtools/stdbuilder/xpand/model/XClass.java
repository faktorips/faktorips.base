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

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.type.IType;

public abstract class XClass extends AbstractGeneratorModelNode {

    public XClass(IIpsObjectPartContainer ipsObjectPartContainer, GeneratorModelContext context,
            ModelService modelService) {
        super(ipsObjectPartContainer, context, modelService);
    }

    public String getFileName() {
        return getModelContext().getRelativeJavaFile(getIpsObjectPartContainer().getIpsSrcFile()).toOSString();
    }

    @Override
    public IType getIpsObjectPartContainer() {
        return (IType)super.getIpsObjectPartContainer();
    }

    public IType getIType() {
        return getIpsObjectPartContainer();
    }

    public String getSimpleName() {
        return getModelContext().getUnqualifiedClassName(getIType());
    }

    public String getQualifiedName() {
        return getModelContext().getQualifiedClassName(getIType());
    }

    public String getPackageName() {
        return getModelContext().getPackage(getIType());
    }
}
