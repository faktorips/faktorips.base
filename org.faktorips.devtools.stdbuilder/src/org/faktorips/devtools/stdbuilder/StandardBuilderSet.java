package org.faktorips.devtools.stdbuilder;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.model.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.stdbuilder.backup.ProductCmptImplCuBuilder;
import org.faktorips.devtools.stdbuilder.backup.ProductCmptInterfaceCuBuilder;
import org.faktorips.devtools.stdbuilder.pctype.PolicyCmptTypeImplCuBuilder;
import org.faktorips.devtools.stdbuilder.pctype.PolicyCmptTypeInterfaceCuBuilder;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptGenerationCuBuilder;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptTocFileUpdateBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenImplCuBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenInterfaceCuBuilder;
import org.faktorips.devtools.stdbuilder.table.TableImplBuilder;
import org.faktorips.devtools.stdbuilder.table.TableRowBuilder;
import org.faktorips.devtools.stdbuilder.table.TableTocFileUpdateBuilder;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;

/**
 * A IpsArtefactBuilderSet implementation that assembles the standard FaktorIPS artefact builders.
 * 
 * @author Peter Erzberger
 */
public class StandardBuilderSet extends DefaultBuilderSet {

    private IIpsArtefactBuilder[] builders;
    private TableImplBuilder tableImplBuilder;

    /**
     * Overridden.
     */
    public IIpsArtefactBuilder[] getArtefactBuilders() {
        return builders;
    }

    /**
     * Overridden.
     */
    public boolean isSupportTableAccess() {
        return true;
    }

    /**
     * Overridden.
     */
    public CompilationResult getTableAccessCode(ITableAccessFunction fct, CompilationResult[] argResults) throws CoreException {
        Datatype returnType = fct.getIpsProject().findDatatype(fct.getType());
        JavaCodeFragment code = new JavaCodeFragment();
        CompilationResultImpl result = new CompilationResultImpl(code, returnType);
        code.appendClassName(tableImplBuilder.getQualifiedClassName(fct.getTableStructure().getIpsSrcFile()));
        code.append(".getInstance(");
        code.append(fct.getIpsProject().getCodeToGetTheRuntimeRepository());
        code.append(").findRow(");
        for (int i = 0; i < argResults.length; i++) {
            if (i>0) {
                code.append(", ");
            }
            code.append(argResults[i].getCodeFragment());
            result.addMessages(argResults[i].getMessages());
        }
        code.append(").get");
        code.append(StringUtils.capitalise(fct.findAccessedColumn().getName()));
        code.append("()");
        return result;
    }

    /**
     * Instantiates the artefact builders for this set.
     */
    public void initialize() throws CoreException {

        // policy component type builders
        ProductCmptInterfaceCuBuilder pcInterfaceBuilder = new ProductCmptInterfaceCuBuilder(this,
                KIND_PRODUCT_CMPT_INTERFACE);
        PolicyCmptTypeInterfaceCuBuilder policyCmptTypeInterfaceBuilder = new PolicyCmptTypeInterfaceCuBuilder(
                this, KIND_POLICY_CMPT_INTERFACE);
        policyCmptTypeInterfaceBuilder.setProductCmptInterfaceBuilder(pcInterfaceBuilder);
        pcInterfaceBuilder.setPolicyCmptTypeInterfaceBuilder(policyCmptTypeInterfaceBuilder);
        ProductCmptImplCuBuilder pcImplBuilder = new ProductCmptImplCuBuilder(this, KIND_PRODUCT_CMPT_IMPL);
        PolicyCmptTypeImplCuBuilder policyCmptTypeImplBuilder = new PolicyCmptTypeImplCuBuilder(
                this, KIND_POLICY_CMPT_IMPL);
        policyCmptTypeImplBuilder.setPolicyCmptTypeImplBuilder(policyCmptTypeImplBuilder);
        policyCmptTypeImplBuilder.setPolicyCmptTypeInterfaceBuilder(policyCmptTypeInterfaceBuilder);
        policyCmptTypeImplBuilder.setProductCmptImplBuilder(pcImplBuilder);
        policyCmptTypeImplBuilder.setProductCmptInterfaceBuilder(pcInterfaceBuilder);
        pcImplBuilder.setPolicyCmptTypeImplBuilder(policyCmptTypeImplBuilder);
        pcImplBuilder.setPolicyCmptTypeInterfaceBuilder(policyCmptTypeInterfaceBuilder);
        pcImplBuilder.setProductCmptInterfaceBuilder(pcInterfaceBuilder);
        
        ProductCmptGenInterfaceCuBuilder productCmptGenInterfaceBuilder = new ProductCmptGenInterfaceCuBuilder(this, DefaultBuilderSet.KIND_PRODUCT_CMPT_GENERATION_INTERFACE);
        ProductCmptGenImplCuBuilder productCmptGenImplBuilder = new ProductCmptGenImplCuBuilder(this, DefaultBuilderSet.KIND_PRODUCT_CMPT_GENERATION_IMPL);
        productCmptGenImplBuilder.setInterfaceBuilder(productCmptGenInterfaceBuilder);
        productCmptGenImplBuilder.setProductCmptTypeImplBuilder(pcImplBuilder);
        pcImplBuilder.setProductCmptGenImplBuilder(productCmptGenImplBuilder);
        productCmptGenInterfaceBuilder.setImplementationBuilder(productCmptGenImplBuilder);
        
        // product component builders.
        ProductCmptGenerationCuBuilder productCmptGenerationImplBuilder = new ProductCmptGenerationCuBuilder(
                this, KIND_PRODUCT_CMPT_GENERATION_IMPL);
        productCmptGenerationImplBuilder.setProductCmptImplBuilder(pcImplBuilder);
        IIpsArtefactBuilder productCmptContentCopyBuilder = new XmlContentFileCopyBuilder(
                IpsObjectType.PRODUCT_CMPT, this, KIND_PRODUCT_CMPT_CONTENT);
        ProductCmptTocFileUpdateBuilder productCmptTocUpdateBuilder = new ProductCmptTocFileUpdateBuilder(
                this, KIND_PRODUCT_CMPT_TOCENTRY);
        productCmptTocUpdateBuilder
                .setPolicyCmptTypeInterfaceBuilder(policyCmptTypeInterfaceBuilder);
        productCmptTocUpdateBuilder
                .setProductCmptGenerationBuilder(productCmptGenerationImplBuilder);
        TableTocFileUpdateBuilder tableContentTocUpdateBuilder = new TableTocFileUpdateBuilder(
                this, KIND_TABLE_TOCENTRY);
        tableContentTocUpdateBuilder.setTableImplBuilder(tableImplBuilder);

        // table structure builders
        tableImplBuilder = new TableImplBuilder(this, KIND_TABLE_IMPL);
        TableRowBuilder tableRowBuilder = new TableRowBuilder(this, KIND_TABLE_ROW);
        tableImplBuilder.setTableRowBuilder(tableRowBuilder);

        // table content builders
        IIpsArtefactBuilder tableContentCopyBuilder = new XmlContentFileCopyBuilder(
                IpsObjectType.TABLE_CONTENTS, this, KIND_TABLE_CONTENT);
        
        builders = new IIpsArtefactBuilder[] { 
                tableImplBuilder, 
                tableRowBuilder,
                productCmptGenInterfaceBuilder,
                productCmptGenImplBuilder,
                pcInterfaceBuilder, 
                pcImplBuilder, 
                policyCmptTypeImplBuilder,
                policyCmptTypeInterfaceBuilder, 
                productCmptGenerationImplBuilder,
                tableContentCopyBuilder, 
                productCmptContentCopyBuilder,
                productCmptTocUpdateBuilder, 
                tableContentTocUpdateBuilder };
    }
}
