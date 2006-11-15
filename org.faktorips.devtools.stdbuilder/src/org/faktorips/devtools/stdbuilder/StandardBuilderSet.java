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

package org.faktorips.devtools.stdbuilder;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.AbstractParameterIdentifierResolver;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.internal.model.TableStructureEnumDatatypeAdapter;
import org.faktorips.devtools.core.model.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.core.model.IParameterIdentifierResolver;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.stdbuilder.formulatest.FormulaTestBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.BasePolicyCmptTypeBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptBuilder;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptXMLBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.table.TableImplBuilder;
import org.faktorips.devtools.stdbuilder.table.TableRowBuilder;
import org.faktorips.devtools.stdbuilder.testcase.TestCaseBuilder;
import org.faktorips.devtools.stdbuilder.testcasetype.TestCaseTypeClassBuilder;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.runtime.TableFunctionExecution;
import org.faktorips.runtime.internal.MethodNames;

/**
 * A IpsArtefactBuilderSet implementation that assembles the standard FaktorIPS artefact builders.
 * 
 * @author Peter Erzberger
 */
public class StandardBuilderSet extends DefaultBuilderSet {

    private IIpsArtefactBuilder[] builders;
    private TableImplBuilder tableImplBuilder;
    private TableRowBuilder tableRowBuilder;
    private PolicyCmptInterfaceBuilder policyCmptInterfaceBuilder;

    /**
     * {@inheritDoc}
     */
    public IIpsArtefactBuilder[] getArtefactBuilders() {
        return builders;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSupportTableAccess() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public CompilationResult getTableAccessCode(ITableContents tableContents, ITableAccessFunction fct, CompilationResult[] argResults) throws CoreException {
        Datatype returnType = fct.getIpsProject().findDatatype(fct.getType());
        DatatypeHelper returnTypeHelper = fct.getIpsProject().findDatatypeHelper(returnType.getQualifiedName());
        JavaCodeFragment code = new JavaCodeFragment();
        ITableStructure tableStructure = fct.getTableStructure();
        code.append("((");
        code.appendClassName(returnType.getJavaClassName());
        code.append(")");
        code.append("(new ");
        code.appendClassName(TableFunctionExecution.class);
        code.append("()");
        code.appendOpenBracket();
        code.append("public Object execute()");
        code.appendOpenBracket();
        code.appendClassName(tableRowBuilder.getQualifiedClassName(tableStructure.getIpsSrcFile()));
        code.append(" row = ");
        CompilationResultImpl result = new CompilationResultImpl(code, returnType);
        result.addAllIdentifierUsed(argResults);
        code.appendClassName(tableImplBuilder.getQualifiedClassName(tableStructure.getIpsSrcFile()));
        // create get instance method by using the qualified name of the table content
        code.append(".getInstance(" + MethodNames.GET_REPOSITORY + "(), \"" + tableContents.getQualifiedName()
                + "\").findRow(");
        // TODO pk: findRow is not correct in general
        for (int i = 0; i < argResults.length; i++) {
            if (i>0) {
                code.append(", ");
            }
            code.append(argResults[i].getCodeFragment());
            result.addMessages(argResults[i].getMessages());
        }
        code.append(");");
        code.appendln();
        code.append("if(row != null)");
        code.appendOpenBracket();
        code.append("return row.get");
        code.append(StringUtils.capitalise(fct.findAccessedColumn().getName()));
        code.append("();");
        code.appendCloseBracket();
        code.append("return ");
        code.append(returnTypeHelper.nullExpression());
        code.append(';');
        code.appendCloseBracket();
        code.appendCloseBracket();
        code.append(").execute())");
        return result;
    }
    
    public IParameterIdentifierResolver getFlParameterIdentifierResolver() {
        return new AbstractParameterIdentifierResolver(){

            protected String getParameterAttributGetterName(IAttribute attribute, Datatype datatype) {
                return policyCmptInterfaceBuilder.getMethodNameGetPropertyValue(attribute, datatype);    
            }
            
        };
    }

    public boolean isSupportFlIdentifierResolver() {
        return true;
    }

    /**
     * Instantiates the artefact builders for this set. The following configuration properties of the
     * provided IIpsArtefactBuilderSetConfig are considered by this builder set.
     * 
     * Property: generateChangeListener, type=boolean, values = ("true", "false")
     * 
     */
    public void initialize(IIpsArtefactBuilderSetConfig config) throws CoreException {

        //configuration properties
        Boolean generateChangeListener = config.getBooleanPropertyValue(BasePolicyCmptTypeBuilder.CONFIG_PROPERTY_GENERATE_CHANGELISTENER);
        
        
        // create policy component type builders
        PolicyCmptImplClassBuilder policyCmptImplClassBuilder = new PolicyCmptImplClassBuilder(
                this, KIND_POLICY_CMPT_IMPL, generateChangeListener);
        policyCmptInterfaceBuilder = new PolicyCmptInterfaceBuilder(
                this, KIND_POLICY_CMPT_INTERFACE, generateChangeListener);
        
        
        // create product component type builders
        ProductCmptInterfaceBuilder productCmptInterfaceBuilder = new ProductCmptInterfaceBuilder(this,
                KIND_PRODUCT_CMPT_INTERFACE);
        ProductCmptImplClassBuilder productCmptImplClassBuilder = new ProductCmptImplClassBuilder(this, KIND_PRODUCT_CMPT_IMPL);
        ProductCmptGenInterfaceBuilder productCmptGenInterfaceBuilder = new ProductCmptGenInterfaceBuilder(this, DefaultBuilderSet.KIND_PRODUCT_CMPT_GENERATION_INTERFACE);
        ProductCmptGenImplClassBuilder productCmptGenImplClassBuilder = new ProductCmptGenImplClassBuilder(this, DefaultBuilderSet.KIND_PRODUCT_CMPT_GENERATION_IMPL);

        // product component builders
        ProductCmptBuilder productCmptGenerationImplBuilder = new ProductCmptBuilder(
                this, KIND_PRODUCT_CMPT_GENERATION_IMPL);
        IIpsArtefactBuilder productCmptContentCopyBuilder = new ProductCmptXMLBuilder(
                IpsObjectType.PRODUCT_CMPT, this, KIND_PRODUCT_CMPT_CONTENT);
        
        // table structure builders
        tableImplBuilder = new TableImplBuilder(this, KIND_TABLE_IMPL);
        tableRowBuilder = new TableRowBuilder(this, KIND_TABLE_ROW);
        tableImplBuilder.setTableRowBuilder(tableRowBuilder);

        // table content builders
        IIpsArtefactBuilder tableContentCopyBuilder = new XmlContentFileCopyBuilder(
                IpsObjectType.TABLE_CONTENTS, this, KIND_TABLE_CONTENT);

        // test case type builders
        TestCaseTypeClassBuilder testCaseTypeClassBuilder = new TestCaseTypeClassBuilder(this, KIND_TEST_CASE_TYPE_CLASS);
        
        // test case builder
        TestCaseBuilder testCaseBuilder = new TestCaseBuilder(this);

        // formula test builder
        FormulaTestBuilder formulaTestBuilder = new FormulaTestBuilder(this);

        // toc file builder
        TocFileBuilder tocFileBuilder = new TocFileBuilder(this);
        
        //
        // wire up the builders
        //
        
        // policy component type builders
        policyCmptImplClassBuilder.setInterfaceBuilder(policyCmptInterfaceBuilder);
        policyCmptInterfaceBuilder.setProductCmptInterfaceBuilder(productCmptInterfaceBuilder);
        policyCmptInterfaceBuilder.setProductCmptGenInterfaceBuilder(productCmptGenInterfaceBuilder);
        policyCmptImplClassBuilder.setProductCmptGenInterfaceBuilder(productCmptGenInterfaceBuilder);
        policyCmptImplClassBuilder.setProductCmptGenImplBuilder(productCmptGenImplClassBuilder);
        policyCmptImplClassBuilder.setProductCmptInterfaceBuilder(productCmptInterfaceBuilder);
        
        // product component type builders
        productCmptInterfaceBuilder.setPolicyCmptTypeInterfaceBuilder(policyCmptInterfaceBuilder);
        productCmptInterfaceBuilder.setProductCmptGenInterfaceBuilder(productCmptGenInterfaceBuilder);

        productCmptImplClassBuilder.setInterfaceBuilder(productCmptInterfaceBuilder);
        productCmptImplClassBuilder.setProductCmptGenInterfaceBuilder(productCmptGenInterfaceBuilder);
        productCmptImplClassBuilder.setPolicyCmptImplClassBuilder(policyCmptImplClassBuilder);
        
        productCmptGenInterfaceBuilder.setProductCmptTypeInterfaceBuilder(productCmptInterfaceBuilder);

        productCmptGenImplClassBuilder.setInterfaceBuilder(productCmptGenInterfaceBuilder);
        productCmptGenImplClassBuilder.setProductCmptTypeImplBuilder(productCmptImplClassBuilder);
        productCmptGenImplClassBuilder.setProductCmptTypeInterfaceBuilder(productCmptInterfaceBuilder);
        productCmptGenImplClassBuilder.setPolicyCmptTypeImplBuilder(policyCmptImplClassBuilder);
        productCmptGenImplClassBuilder.setTableImplBuilder(tableImplBuilder);
        
        // product component builders.
        productCmptGenerationImplBuilder.setProductCmptImplBuilder(productCmptImplClassBuilder);
        productCmptGenerationImplBuilder.setProductCmptGenImplBuilder(productCmptGenImplClassBuilder);
        
        // test case builder
        testCaseBuilder.setJavaSourceFileBuilder(policyCmptImplClassBuilder);
        
        // toc file builders
        tocFileBuilder.setProductCmptTypeImplClassBuilder(productCmptImplClassBuilder);
        tocFileBuilder.setProductCmptBuilder(productCmptGenerationImplBuilder);
        tocFileBuilder.setProductCmptGenImplClassBuilder(productCmptGenImplClassBuilder);
        tocFileBuilder.setTableImplBuilder(tableImplBuilder);
        tocFileBuilder.setTestCaseTypeClassBuilder(testCaseTypeClassBuilder);
        tocFileBuilder.setTestCaseBuilder(testCaseBuilder);
        tocFileBuilder.setFormulaTestBuilder(formulaTestBuilder);
        
        builders = new IIpsArtefactBuilder[] { 
                tableImplBuilder, 
                tableRowBuilder,
                productCmptGenInterfaceBuilder,
                productCmptGenImplClassBuilder,
                productCmptInterfaceBuilder, 
                productCmptImplClassBuilder, 
                policyCmptImplClassBuilder,
                policyCmptInterfaceBuilder, 
                productCmptGenerationImplBuilder,
                tableContentCopyBuilder, 
                productCmptContentCopyBuilder,
                testCaseTypeClassBuilder,
                testCaseBuilder,
                formulaTestBuilder,
                tocFileBuilder };
    }
    
    /**
     * For testing purposes.
     */
    public IIpsArtefactBuilder getBuilder(Class builderClass) {
        for (int i = 0; i < builders.length; i++) {
            if (builders[i].getClass().equals(builderClass)) {
                return builders[i];            }
        }
        throw new RuntimeException("No builder of class " + builderClass + " defined.");
    }

    /**
     * Does nothing.
     * {@inheritDoc}
     */
    public void clean() throws CoreException {
    }
    
    /**
     * {@inheritDoc}
     */
    public DatatypeHelper getDatatypeHelperForTableBasedEnum(TableStructureEnumDatatypeAdapter datatype) {
        return new TableStructureEnumDatatypeHelper(datatype);
    }
}
