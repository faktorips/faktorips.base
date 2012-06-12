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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.naming.JavaPackageStructure;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.type.IType;

public abstract class XClass extends AbstractGeneratorModelNode {

    public XClass(IIpsObjectPartContainer ipsObjectPartContainer, GeneratorModelContext context,
            ModelService modelService) {
        super(ipsObjectPartContainer, context, modelService);
    }

    public String getFileName() {
        return getImplClassNaming().getRelativeJavaFile(getIpsObjectPartContainer().getIpsSrcFile()).toOSString();
    }

    @Override
    public IType getIpsObjectPartContainer() {
        return (IType)super.getIpsObjectPartContainer();
    }

    public IType getType() {
        return getIpsObjectPartContainer();
    }

    public String getSimpleName() {
        return getImplClassNaming().getUnqualifiedClassName(getType().getIpsSrcFile());
    }

    public String getQualifiedName() {
        return getImplClassNaming().getQualifiedClassName(getType());
    }

    public String getPackageName() {
        return JavaPackageStructure.getPackageName(getType().getIpsSrcFile(), false, true);
    }

    public String getSuperclassName() {
        try {
            if (getType().hasSupertype()) {
                IType superType = getType().findSupertype(getIpsProject());
                if (superType != null) {
                    return addImport(getImplClassNaming().getQualifiedClassName(superType));
                } else {
                    return "";
                }
            } else {
                return getBaseSuperclassName();
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    protected abstract String getBaseSuperclassName();

    public boolean isImplementsInterface() {
        return !getImplementedInterface().isEmpty();
    }

    public List<String> getImplementedInterface() {
        ArrayList<String> list = new ArrayList<String>();
        addImport(getInterfaceNaming().getQualifiedClassName(getType()));
        return list;
    }

}
