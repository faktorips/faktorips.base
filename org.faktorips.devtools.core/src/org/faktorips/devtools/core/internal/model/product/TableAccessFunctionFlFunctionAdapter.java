/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.product;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ConversionMatrix;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FlFunction;
import org.faktorips.fl.FunctionSignature;
import org.faktorips.fl.FunctionSignatureImpl;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;

/**
 * An adapter that adapts a table access function to the FlFunction interfaces.
 * 
 * @author Jan Ortmann
 */
public class TableAccessFunctionFlFunctionAdapter implements FlFunction {

    private ITableAccessFunction fct;
    private ExprCompiler compiler;
    
    public TableAccessFunctionFlFunctionAdapter(ITableAccessFunction fct) {
        ArgumentCheck.notNull(fct);
        this.fct = fct;
    }

    /**
     * Overridden.
     */
    public CompilationResult compile(CompilationResult[] argResults) {
        try {
            IIpsArtefactBuilderSet builderSet = fct.getIpsProject().getArtefactBuilderSet();
            if (!builderSet.isSupportTableAccess()) {
                return new CompilationResultImpl(Message.newError("", Messages.TableAccessFunctionFlFunctionAdapter_msgNoTableAccess)); //$NON-NLS-1$
            }
            return builderSet.getTableAccessCode(fct, argResults);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return new CompilationResultImpl(Message.newError("", Messages.TableAccessFunctionFlFunctionAdapter_msgErrorDuringCodeGeneration + fct.toString())); //$NON-NLS-1$
        }
    }

    public void setCompiler(ExprCompiler compiler) {
        this.compiler = compiler;
    }

    public ExprCompiler getCompiler() {
        return compiler;
    }

    public String getDescription() {
        return fct.getDescription();
    }

    public void setDescription(String description) {
        throw new RuntimeException("The adpater does not support setDescription()!"); //$NON-NLS-1$
    }

    public Datatype getType() {
        try {
            return fct.getIpsProject().findValueDatatype(fct.getType());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return fct.getName();
    }

    public Datatype[] getArgTypes() {
        IIpsProject project = fct.getIpsProject();
        String[] argTypes = fct.getArgTypes();
        Datatype[] types = new Datatype[argTypes.length];
        for (int i = 0; i < argTypes.length; i++) {
            try {
                types[i] = project.findValueDatatype(argTypes[i]);
            } catch (CoreException e) {
                throw new RuntimeException("Error searching for datatype " + argTypes[i], e); //$NON-NLS-1$
            }
        }
        return types;
    }

    public boolean isSame(FunctionSignature fctSignature) {
        FunctionSignature thisFct = new FunctionSignatureImpl(getName(), getType(), getArgTypes());
        return thisFct.isSame(fctSignature);
    }

    public boolean match(String name, Datatype[] argTypes) {
        FunctionSignature thisFct = new FunctionSignatureImpl(getName(), getType(), getArgTypes());
        return thisFct.match(name, argTypes);
    }

    public boolean matchUsingConversion(String name, Datatype[] argTypes, ConversionMatrix matrix) {
        FunctionSignature thisFct = new FunctionSignatureImpl(getName(), getType(), getArgTypes());
        return thisFct.matchUsingConversion(name, argTypes, matrix);
    }

    /**
     * Returns false;
     */
	public boolean hasVarArgs() {
		return false;
	}

}
