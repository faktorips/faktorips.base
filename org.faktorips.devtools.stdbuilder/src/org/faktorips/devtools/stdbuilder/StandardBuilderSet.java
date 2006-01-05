package org.faktorips.devtools.stdbuilder;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.model.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.stdbuilder.pctype.PolicyCmptTypeImplCuBuilder;
import org.faktorips.devtools.stdbuilder.pctype.PolicyCmptTypeInterfaceCuBuilder;
import org.faktorips.devtools.stdbuilder.pctype.ProductCmptImplCuBuilder;
import org.faktorips.devtools.stdbuilder.pctype.ProductCmptInterfaceCuBuilder;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptGenerationCuBuilder;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptTocFileUpdateBuilder;
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
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsArtefactBuilderSet#getArtefactBuilders()
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
     * 
     * @see org.faktorips.devtools.core.model.IIpsArtefactBuilderSet#initialize()
     */
    public void initialize() throws CoreException {

        tableImplBuilder = new TableImplBuilder(this, KIND_TABLE_IMPL);
        TableRowBuilder tableRowBuilder = new TableRowBuilder(this, KIND_TABLE_ROW);
        tableImplBuilder.setTableRowBuilder(tableRowBuilder);

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
        ProductCmptGenerationCuBuilder productCmptGenerationImplBuilder = new ProductCmptGenerationCuBuilder(
                this, KIND_PRODUCT_CMPT_GENERATION_IMPL);
        productCmptGenerationImplBuilder.setProductCmptImplBuilder(pcImplBuilder);
        IIpsArtefactBuilder tableContentCopyBuilder = new XmlContentFileCopyBuilder(
                IpsObjectType.TABLE_CONTENTS, this, KIND_TABLE_CONTENT);
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

        builders = new IIpsArtefactBuilder[] { tableImplBuilder, tableRowBuilder,
                pcInterfaceBuilder, pcImplBuilder, policyCmptTypeImplBuilder,
                policyCmptTypeInterfaceBuilder, productCmptGenerationImplBuilder,
                tableContentCopyBuilder, productCmptContentCopyBuilder,
                productCmptTocUpdateBuilder, tableContentTocUpdateBuilder };
    }
}
