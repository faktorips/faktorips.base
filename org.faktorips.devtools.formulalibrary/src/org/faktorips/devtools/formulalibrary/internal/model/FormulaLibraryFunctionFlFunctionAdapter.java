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

package org.faktorips.devtools.formulalibrary.internal.model;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.GenericBuilderKindId;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.productcmpt.AbstractFlFunctionAdapter;
import org.faktorips.devtools.core.model.method.IBaseMethod;
import org.faktorips.devtools.formulalibrary.builder.xpand.FormulaLibraryClassBuilder;
import org.faktorips.devtools.formulalibrary.model.IFormulaFunction;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.FlFunction;

/**
 * An adapter that adapts an {@link IFormulaFunction} to the {@link FlFunction} interface.
 * 
 * @author dicker
 */
public class FormulaLibraryFunctionFlFunctionAdapter extends AbstractFlFunctionAdapter {

    private final IBaseMethod formulaMethod;
    private String functionDescription;

    public FormulaLibraryFunctionFlFunctionAdapter(IBaseMethod formulaMethod, String functionDescription) {
        super(formulaMethod.getIpsProject());
        this.formulaMethod = formulaMethod;
        this.functionDescription = functionDescription;
    }

    @Override
    public CompilationResult compile(CompilationResult[] argResults) {
        JavaCodeFragment javaCodeFragment = new JavaCodeFragment();
        javaCodeFragment.append(getJavaNameOfFormulaLibrary() + "." + formulaMethod.getName()); //$NON-NLS-1$
        javaCodeFragment.append("("); //$NON-NLS-1$
        for (int i = 0; i < argResults.length; i++) {
            if (i > 0) {
                javaCodeFragment.append(", "); //$NON-NLS-1$
            }
            CompilationResult compilationResult = argResults[i];
            javaCodeFragment.append(compilationResult.getCodeFragment());
        }
        javaCodeFragment.append(")"); //$NON-NLS-1$
        CompilationResult result = new CompilationResultImpl(javaCodeFragment, getType());
        return result;
    }

    private String getJavaNameOfFormulaLibrary() {
        FormulaLibraryClassBuilder builder = getIpsProject().getIpsArtefactBuilderSet().getBuilderById(
                new GenericBuilderKindId(FormulaLibraryClassBuilder.NAME), FormulaLibraryClassBuilder.class);

        try {
            return builder.getQualifiedClassName(formulaMethod.getIpsObject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    public String getDescription() {
        return functionDescription;
    }

    @Override
    public Datatype getType() {
        try {
            return formulaMethod.findDatatype(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return formulaMethod.getIpsObject().getQualifiedName() + "." + formulaMethod.getName(); //$NON-NLS-1$
    }

    @Override
    public Datatype[] getArgTypes() {
        List<Datatype> datatypes = formulaMethod.getParameterDatatypes();
        return datatypes.toArray(new Datatype[datatypes.size()]);
    }

}