/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.lang.reflect.Method;
import java.net.URLClassLoader;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.ConversionMatrix;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IFormulaTestCase;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FlFunction;
import org.faktorips.fl.FunctionSignature;
import org.faktorips.fl.FunctionSignatureImpl;
import org.faktorips.runtime.ClassloaderRuntimeRepository;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.ITable;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;

/**
 * An adapter that adapts the retrieve of a table content to the FlFunction interfaces.<br>
 * This class resolves the access function by generating a compilation result which result is the
 * same value as the access function will be return in runtime by executing the access function.
 */
public class TableFunctionFormulaTestFlFunctionAdapter implements FlFunction {

    private ITableAccessFunction fct;
    private ExprCompiler compiler;
    private ITableContents tableContents;
    private IFormulaTestCase formulaTestCase;
    private String roleName;
    private IIpsProject ipsProject;

    public TableFunctionFormulaTestFlFunctionAdapter(ITableContents tableContents, ITableAccessFunction fct,
            IFormulaTestCase formulaTestCase, String roleName, IIpsProject ipsProject) {

        ArgumentCheck.notNull(fct);
        ArgumentCheck.notNull(tableContents);
        ArgumentCheck.notNull(roleName);
        ArgumentCheck.notNull(ipsProject);
        this.fct = fct;
        this.tableContents = tableContents;
        this.formulaTestCase = formulaTestCase;
        this.roleName = roleName;
        this.ipsProject = ipsProject;
    }

    @Override
    public CompilationResult compile(CompilationResult[] argResults) {
        try {
            Object result = getTableContentValue(argResults, ipsProject);

            // generate the code for the values inside the table content
            Datatype returnType = fct.getIpsProject().findDatatype(fct.getType());
            DatatypeHelper returnTypeHelper = fct.getIpsProject().findDatatypeHelper(returnType.getQualifiedName());
            JavaCodeFragment code = new JavaCodeFragment();
            if (result != null) {
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
            return new CompilationResultImpl(Message.newError(
                    "", Messages.TableAccessFunctionFlFunctionAdapter_msgErrorDuringCodeGeneration + fct.toString())); //$NON-NLS-1$
        }
    }

    @Override
    public void setCompiler(ExprCompiler compiler) {
        this.compiler = compiler;
    }

    @Override
    public ExprCompiler getCompiler() {
        return compiler;
    }

    @Override
    public String getDescription() {
        return fct.getDescription();
    }

    @Override
    public void setDescription(String description) {
        throw new RuntimeException("The adpater does not support setDescription()!"); //$NON-NLS-1$
    }

    @Override
    public Datatype getType() {
        try {
            return fct.getIpsProject().findValueDatatype(fct.getType());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return StringUtils.capitalize(roleName) + "." + fct.getAccessedColumn(); //$NON-NLS-1$
    }

    @Override
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

    @Override
    public boolean isSame(FunctionSignature fctSignature) {
        FunctionSignature thisFct = new FunctionSignatureImpl(getName(), getType(), getArgTypes());
        return thisFct.isSame(fctSignature);
    }

    @Override
    public boolean match(String name, Datatype[] argTypes) {
        FunctionSignature thisFct = new FunctionSignatureImpl(getName(), getType(), getArgTypes());
        return thisFct.match(name, argTypes);
    }

    @Override
    public boolean matchUsingConversion(String name, Datatype[] argTypes, ConversionMatrix matrix) {
        FunctionSignature thisFct = new FunctionSignatureImpl(getName(), getType(), getArgTypes());
        return thisFct.matchUsingConversion(name, argTypes, matrix);
    }

    /**
     * Returns <code>false</code>;
     */
    @Override
    public boolean hasVarArgs() {
        return false;
    }

    /**
     * Returns the corresponding table content.
     */
    private Object getTableContentValue(CompilationResult[] argResults, IIpsProject ipsProject) throws Exception {
        /*
         * create the classloader to run the table access function with and to create the runtime
         * repository
         */
        ClassLoader classLoaderForJavaProject = ipsProject.getClassLoaderForJavaProject();
        /*
         * accumulate the runtime classes from the current jvm, thus if a class exists in the
         * projects classpath and in the current jvm then the class from the current jvm will be
         * choosen, otherwise a ClassCastException will be thrown if the repository tries to
         * instantiate the class
         */
        ClassLoader classLoader = URLClassLoader.newInstance(((URLClassLoader)classLoaderForJavaProject).getURLs(),
                getClass().getClassLoader());
        IIpsArtefactBuilderSet ipsArtefactBuilderSet = ipsProject.getIpsArtefactBuilderSet();
        IRuntimeRepository repository = ClassloaderRuntimeRepository.create(ipsArtefactBuilderSet
                .getRuntimeRepositoryTocResourceName(tableContents.getIpsPackageFragment().getRoot()), classLoader);

        // search the table in the repository
        ITable table = repository.getTable(tableContents.getQualifiedName());
        if (table == null) {
            throw new RuntimeException(
                    "Table '" + tableContents.getQualifiedName() + "' doesn't exists in the repository!"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // find the correct getter method via reflections
        Class<?>[] argClasses = new Class[argResults.length];
        Object[] argValues = new Object[argResults.length];
        for (int i = 0; i < argResults.length; i++) {
            argValues[i] = ((FormulaTestCase)formulaTestCase).execute(argResults[i].getCodeFragment(), classLoader,
                    ipsProject);
            Class<?> runtimeClass = classLoader.loadClass(argResults[i].getDatatype().getJavaClassName());
            argClasses[i] = runtimeClass;
        }
        Method findRowMethod = table.getClass().getMethod("findRow", argClasses); //$NON-NLS-1$
        Object runtimeRow = findRowMethod.invoke(table, argValues);
        if (runtimeRow == null) {
            // no row found, therefore the result is null
            return null;
        }
        Method getColumnMethod = runtimeRow.getClass().getMethod(
                "get" + StringUtils.capitalize(fct.getAccessedColumn()), new Class[0]); //$NON-NLS-1$
        return getColumnMethod.invoke(runtimeRow, new Object[0]);
    }

}
