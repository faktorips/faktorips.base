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

package org.faktorips.devtools.formulalibrary.builder.xpand.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.method.IBaseMethod;
import org.faktorips.devtools.formulalibrary.internal.model.FormulaLibrary;
import org.faktorips.devtools.formulalibrary.model.IFormulaFunction;
import org.faktorips.devtools.formulalibrary.model.IFormulaLibrary;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XClass;

/**
 * Generate the class for {@link IFormulaLibrary}
 * 
 * @author frank
 */
public class XFormulaLibraryClass extends XClass {

    public XFormulaLibraryClass(IIpsObject ipsObject, GeneratorModelContext context, ModelService modelService) {
        super(ipsObject, context, modelService);
    }

    @Override
    public FormulaLibrary getIpsObjectPartContainer() {
        return (FormulaLibrary)super.getIpsObjectPartContainer();
    }

    @Override
    public boolean isValidForCodeGeneration() {
        try {
            return getIpsObjectPartContainer().isValid(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public String getImplClassName() {
        return addImport(getSimpleName(BuilderAspect.IMPLEMENTATION));
    }

    @Override
    protected String getBaseSuperclassName() {
        return StringUtils.EMPTY;
    }

    @Override
    public LinkedHashSet<String> getExtendedInterfaces() {
        return new LinkedHashSet<String>();
    }

    @Override
    protected LinkedHashSet<String> getExtendedOrImplementedInterfaces() {
        return new LinkedHashSet<String>();
    }

    @Override
    public LinkedHashSet<String> getImplementedInterfaces() {
        return new LinkedHashSet<String>();
    }

    /**
     * Returns the methods from {@link IFormulaFunction}
     */
    private List<IBaseMethod> getFormulaFunctionMethods() {
        List<IBaseMethod> methods = new ArrayList<IBaseMethod>();
        List<IFormulaFunction> formulaFunctions = getIpsObjectPartContainer().getFormulaFunctions();
        for (IFormulaFunction iFormulaFunction : formulaFunctions) {
            methods.add(iFormulaFunction.getFormulaMethod());
        }
        return methods;
    }

    /**
     * Returns a Set with {@link XFormulaMethod}
     */
    public Set<XFormulaMethod> getMethods() {
        if (isCached(XFormulaMethod.class)) {
            return getCachedObjects(XFormulaMethod.class);
        } else {
            Set<XFormulaMethod> nodesForParts = initNodesForParts(getFormulaFunctionMethods(), XFormulaMethod.class);
            putToCache(nodesForParts);
            return nodesForParts;
        }
    }
}
