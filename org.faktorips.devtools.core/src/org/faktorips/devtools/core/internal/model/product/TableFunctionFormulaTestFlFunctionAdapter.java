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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.ConversionMatrix;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.product.IFormulaTestCase;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IKeyItem;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FlFunction;
import org.faktorips.fl.FunctionSignature;
import org.faktorips.fl.FunctionSignatureImpl;
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

    /**
     *
     */
    public TableFunctionFormulaTestFlFunctionAdapter(ITableContents tableContents, ITableAccessFunction fct,
            IFormulaTestCase formulaTestCase) {
        ArgumentCheck.notNull(fct);
        ArgumentCheck.notNull(tableContents);
        this.fct = fct;
        this.tableContents = tableContents;
        this.formulaTestCase = formulaTestCase;
    }

    /**
     * Overridden.
     */
    public CompilationResult compile(CompilationResult[] argResults) {
        try {
            // first check if the table supports access
            IIpsArtefactBuilderSet builderSet = fct.getIpsProject().getIpsArtefactBuilderSet();
            if (!builderSet.isSupportTableAccess()) {
                return new CompilationResultImpl(Message.newError("", Messages.TableAccessFunctionFlFunctionAdapter_msgNoTableAccess)); //$NON-NLS-1$
            }
            return getTableContentValue(tableContents, fct, argResults);
        } catch (Exception e) {
            IpsPlugin.log(e);
            return new CompilationResultImpl(Message.newError("", Messages.TableAccessFunctionFlFunctionAdapter_msgErrorDuringCodeGeneration + fct.toString())); //$NON-NLS-1$
        }
    }

    /*
     * Returns <code>true</code> if the given row matches the given unique key values
     */
    private boolean matchesUniqueKey(List keyColumsIdxs, List datatypesKeyColumns, CompilationResult[] args, IRow row) throws Exception{
        if (keyColumsIdxs.size() == 0){
            return false;
        }
        for (int i = 0; i < keyColumsIdxs.size(); i++) {
            String value = row.getValue(((Integer)keyColumsIdxs.get(i)).intValue());
            // get the index of the argument
            int intIdxArg = -1;
            for (int j = 0; j < args.length; j++) {
                String argDatatype = args[i].getDatatype().getName();
                for (int k = 0; k < datatypesKeyColumns.size(); k++) {
                    String datatypeOfKey = (String) datatypesKeyColumns.get(k);
                    if (argDatatype.equals(datatypeOfKey)){
                        intIdxArg = k;
                        break;
                    }
                }
                if (intIdxArg>=0){
                    break;
                }
            }       
            
            if (intIdxArg == -1){
                // wrong datatype of key and args
                return false;
            }
            // evaluates the result of the given argument (compilation result), to compare it with
            // the value inside the table column
            // e.g. the given compilation result could be something like "new Integer((new Integer(1)).intValue() + 1)",
            // therefore the corresponding code fragment will first executed to get the result, in this case 2
            Object result = formulaTestCase.execute(args[intIdxArg].getCodeFragment());
            if (result == null || !(""+value).equals(result.toString())) { //$NON-NLS-1$
                return false;
            }
        }
        return true;
    }
    
    /*
     * Returns the table index of the given column name
     */
    private int getIndexOfColumn(IColumn[] columnDefs, String columnName){
        for (int i = 0; i < columnDefs.length; i++) {
            if (columnDefs[i].getName().equals(columnName)) {
                return i;
            }
        }
        return -1;
    }
    
    /*
     * Returns the compilation result corresponding to the table access funtion. The executing of
     * the returned compilation result will be the same as the table access function provides in
     * runtime. 
     * Remark: for the formula test executing we don't use the generated formula and
     * repository content to evaluate the result, because if the builder is not running or no
     * repositories are generated then we couldn't calculate the formula for preview. Therfore the model
     * content will used to get the content of the table and generate the compilation result.
     */
    private CompilationResult getTableContentValue(ITableContents tableContents, ITableAccessFunction fct, CompilationResult[] argResults) throws Exception {
        Datatype returnType = fct.getIpsProject().findDatatype(fct.getType());
        
        // first read the table structure to evaluate the value column which is accessed by the table access function
        // and evaluate the unique key columns
        tableContents.findGenerationEffectiveOn(IpsPlugin.getDefault().getIpsPreferences().getWorkingDate());
        ITableStructure tableStructure = tableContents.findTableStructure();
        IColumn[] columnDefs = tableStructure.getColumns();
        List keyIdxs = new ArrayList();
        List keyDatatype = new ArrayList();
        int valueColumnIdx = -1;
        
        IUniqueKey[] keys = tableStructure.getUniqueKeys();
        
        // determine the value column
        // check if the column is the value column by comparing the accessed column name,
        // e.g. table.value() indicates that the column with the name "value" is the column
        // containing the value
        valueColumnIdx = getIndexOfColumn(columnDefs, fct.getAccessedColumn());
        
        // get the key idexes which specifiec the row
        //   iterate all unique keys
        String[] paramFctDatatypes = fct.getArgTypes();
        for (int i = 0; i < keys.length; i++) {
            // iterate all columns specified the key
            // to check if the function input parameter datatype matches the matches the 
            keyIdxs.clear();
            IKeyItem[] keyItems = keys[i].getKeyItems();
            for (int j = 0; j < keyItems.length; j++) {
                // check if key column matches the function in param datatyes
                if(keyItems[j].getDatatype().equals(paramFctDatatypes[j])){
                    // the datatype is the same add the index of the column to the key column index list
                    int idx = getIndexOfColumn(columnDefs, keyItems[j].getName());
                    if (idx == -1){
                        // abort because column could not be determined
                        break;
                    }
                    keyIdxs.add(new Integer(idx));
                    keyDatatype.add(keyItems[j].getDatatype());
                } else {
                    break;
                }
            }
            if (keyIdxs.size() == paramFctDatatypes.length && keyIdxs.size() == keyDatatype.size()){
                // stop because the key was sucessfully found by the parameter signature (same datatypes in same order)
                break;
            }
        }

        // continue only if the value column could be determined and the functions parameter types
        // equals the size of the key column indexes the
        // key was sucessfully evaluated, otherwise the access fct or the key is wrong!
        String resultValueInTable = null;
        if (valueColumnIdx >= 0 && keyIdxs.size() == paramFctDatatypes.length && keyIdxs.size() == keyDatatype.size()){
            // find the content by the unique keys
            ITableContentsGeneration generation = (ITableContentsGeneration) tableContents.getGenerations()[0];
            IRow[] rows = generation.getRows();
            for (int i = 0; i < rows.length; i++) {
                if (matchesUniqueKey(keyIdxs, keyDatatype, argResults, rows[i])){
                    resultValueInTable = rows[i].getValue(valueColumnIdx);
                    break;
                }
            }
        }
        
        // generate the code for the values inside the table content
        DatatypeHelper returnTypeHelper = fct.getIpsProject().findDatatypeHelper(returnType.getQualifiedName());
        JavaCodeFragment code = new JavaCodeFragment();
        if (resultValueInTable != null){
            code.append("("); //$NON-NLS-1$
            code.append(returnTypeHelper.newInstance(resultValueInTable));
            code.append(")"); //$NON-NLS-1$
        } else {
            code.append(returnTypeHelper.newInstanceFromExpression(null));
        }
        CompilationResultImpl result = new CompilationResultImpl(code, returnType);
        return result;
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
		return tableContents.getName() + "." + fct.getAccessedColumn(); //$NON-NLS-1$
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
