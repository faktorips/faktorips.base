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

import java.lang.reflect.Method;
import java.net.URLClassLoader;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.ConversionMatrix;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsProject;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.product.IFormulaTestCase;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FlFunction;
import org.faktorips.fl.FunctionSignature;
import org.faktorips.fl.FunctionSignatureImpl;
import org.faktorips.runtime.ClassloaderRuntimeRepository;
import org.faktorips.runtime.ITable;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;

/**
 * An adapter that adapts the retrieve of a table content to the FlFunction interfaces.<br>
 * This class resolves the access function by generating a compilation result which result
 * is the same value as the access function will be return in runtime by executing the access
 * function.
 */
public class TableFunctionFormulaTestFlFunctionAdapter implements FlFunction {

    private ITableAccessFunction fct;
    private ExprCompiler compiler;
    private ITableContents tableContents;
    private IFormulaTestCase formulaTestCase;
    private String roleName;
    
    /**
     *
     */
    public TableFunctionFormulaTestFlFunctionAdapter(ITableContents tableContents, ITableAccessFunction fct,
            IFormulaTestCase formulaTestCase, String roleName) {
        ArgumentCheck.notNull(fct);
        ArgumentCheck.notNull(tableContents);
        ArgumentCheck.notNull(roleName);
        this.fct = fct;
        this.tableContents = tableContents;
        this.formulaTestCase = formulaTestCase;
        this.roleName = roleName;
    }
        
    /**
     * {@inheritDoc}
     */
    public CompilationResult compile(CompilationResult[] argResults) {
        try {
            Object result = getTableContentValue(argResults);
            
            // generate the code for the values inside the table content
            Datatype returnType = fct.getIpsProject().findDatatype(fct.getType());
            DatatypeHelper returnTypeHelper = fct.getIpsProject().findDatatypeHelper(returnType.getQualifiedName());
            JavaCodeFragment code = new JavaCodeFragment();
            if (result != null){
                code.append("("); //$NON-NLS-1$
                code.append(returnTypeHelper.newInstance(result.toString()));
                code.append(")"); //$NON-NLS-1$
            } else {
                code.append(returnTypeHelper.newInstanceFromExpression(null));
            }
            
            CompilationResultImpl compilationResultImpl = new CompilationResultImpl(code, returnType);
            compilationResultImpl.addAllIdentifierUsed(argResults);
            return compilationResultImpl;
        } catch (Exception e) {
            IpsPlugin.log(e);
            return new CompilationResultImpl(Message.newError("", Messages.TableAccessFunctionFlFunctionAdapter_msgErrorDuringCodeGeneration + fct.toString())); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setCompiler(ExprCompiler compiler) {
        this.compiler = compiler;
    }

    /**
     * {@inheritDoc}
     */
    public ExprCompiler getCompiler() {
        return compiler;
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return fct.getDescription();
    }

    /**
     * {@inheritDoc}
     */
    public void setDescription(String description) {
        throw new RuntimeException("The adpater does not support setDescription()!"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public Datatype getType() {
        try {
            return fct.getIpsProject().findValueDatatype(fct.getType());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
		return StringUtils.capitalise(roleName) + "." + fct.getAccessedColumn(); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public boolean isSame(FunctionSignature fctSignature) {
        FunctionSignature thisFct = new FunctionSignatureImpl(getName(), getType(), getArgTypes());
        return thisFct.isSame(fctSignature);
    }

    /**
     * {@inheritDoc}
     */
    public boolean match(String name, Datatype[] argTypes) {
        FunctionSignature thisFct = new FunctionSignatureImpl(getName(), getType(), getArgTypes());
        return thisFct.match(name, argTypes);
    }

    /**
     * {@inheritDoc}
     */
    public boolean matchUsingConversion(String name, Datatype[] argTypes, ConversionMatrix matrix) {
        FunctionSignature thisFct = new FunctionSignatureImpl(getName(), getType(), getArgTypes());
        return thisFct.matchUsingConversion(name, argTypes, matrix);
    }

    /**
     * {@inheritDoc}
     * Returns <code>false</code>;
     */
	public boolean hasVarArgs() {
		return false;
	}

    /*
     * Returns the corresponding table content.
     */
    private Object getTableContentValue(CompilationResult[] argResults) throws Exception{
        ITableStructure tableStructure = tableContents.findTableStructure();
        
        IIpsProject ipsProject = tableContents.getIpsProject();
        
        // create the classloader to run the table access function with
        ClassLoader runtimeClassLoader = ((IpsProject)ipsProject).getClassLoaderProviderForJavaProject().getClassLoader();
        // make sure that the correct runtime classes from the current jvm will be used
        ClassLoader classLoader = URLClassLoader.newInstance(((URLClassLoader)runtimeClassLoader).getURLs(), getClass().getClassLoader());

        IIpsArtefactBuilderSet ipsArtefactBuilderSet = ipsProject.getIpsArtefactBuilderSet();
        ClassloaderRuntimeRepository repository = ClassloaderRuntimeRepository.create(ipsArtefactBuilderSet.getRuntimeRepositoryTocResourceName(tableStructure.getIpsPackageFragment().getRoot()), classLoader);
        ITable table = repository.getTable(tableContents.getQualifiedName());
        
        // find the correct getter method via reflections
        Class[] argClasses = new Class[argResults.length];
        Object[] argValues = new Object[argResults.length];
        for (int i = 0; i < argResults.length; i++) {
            argValues[i] = ((FormulaTestCase)formulaTestCase).execute(argResults[i].getCodeFragment(), classLoader);
            Class runtimeClass = classLoader.loadClass(argResults[i].getDatatype().getJavaClassName());
            argClasses[i] = runtimeClass;
        }
        
        Method findRowMethod = table.getClass().getMethod("findRow", argClasses); //$NON-NLS-1$
        Object runtimeRow = findRowMethod.invoke(table, argValues);
        if (runtimeRow == null){
            // no row found, therefore the result is null
            return null;
        }
        Method getColumnMethod = runtimeRow.getClass().getMethod("get" + StringUtils.capitalise(fct.getAccessedColumn()), new Class[0]); //$NON-NLS-1$
        return getColumnMethod.invoke(runtimeRow, new Object[0]);
    }
}
