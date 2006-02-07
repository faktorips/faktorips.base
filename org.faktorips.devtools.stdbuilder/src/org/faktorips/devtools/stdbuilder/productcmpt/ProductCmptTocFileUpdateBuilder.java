package org.faktorips.devtools.stdbuilder.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.stdbuilder.AbstractTocFileUpdateBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptInterfaceBuilder;
import org.faktorips.runtime.TocEntryGeneration;
import org.faktorips.runtime.TocEntryObject;
import org.faktorips.runtime.internal.DateTime;

public class ProductCmptTocFileUpdateBuilder extends AbstractTocFileUpdateBuilder {

    private PolicyCmptInterfaceBuilder policyCmptTypeInterfaceBuilder;
    private ProductCmptImplClassBuilder productCmptTypeImplClassBuilder;
    private ProductCmptGenImplClassBuilder productCmptGenImplClassBuilder;
    private ProductCmptBuilder productCmptBuilder;
    
    /**
     * @param structure
     */
    public ProductCmptTocFileUpdateBuilder(IJavaPackageStructure structure, String kind) {
        super(structure, kind);
    }

    public void setPolicyCmptTypeInterfaceBuilder(PolicyCmptInterfaceBuilder policyCmptTypeInterfaceBuilder) {
        this.policyCmptTypeInterfaceBuilder = policyCmptTypeInterfaceBuilder;
    }

    public void setProductCmptTypeImplClassBuilder(ProductCmptImplClassBuilder builder) {
        this.productCmptTypeImplClassBuilder = builder;
    }
    
    public void setProductCmptGenImplClassBuilder(ProductCmptGenImplClassBuilder builder) {
        this.productCmptGenImplClassBuilder = builder;
    }

    public void setProductCmptBuilder(ProductCmptBuilder builder) {
        productCmptBuilder = builder;
    }

    protected void checkIfDependOnBuildersSet() throws IllegalStateException {
        String builderName = null;

        if (policyCmptTypeInterfaceBuilder == null) {
            builderName = PolicyCmptInterfaceBuilder.class.getName();
        }

        if (builderName != null) {
            throw new IllegalStateException(
                    "One of the builders this builder depends on is not set: " + builderName);
        }
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.stdbuilder.AbstractTocFileUpdateBuilder#createTocEntry(org.faktorips.devtools.core.model.IIpsObject)
     */
    protected TocEntryObject createTocEntry(IIpsObject ipsObject) throws CoreException {

        IProductCmpt productCmpt = (IProductCmpt)ipsObject;
        if (productCmpt.getNumOfGenerations() == 0) {
            return null;
        }
        IPolicyCmptType pcType = productCmpt.findPolicyCmptType();
        if (pcType == null) {
            return null;
        }
        
        String packageString = getPackageStructure().getPackage(kind, productCmpt.getIpsSrcFile()).replace('.', '/');
        String xmlResourceName = packageString + '/' + productCmpt.getName() + ".xml";
        TocEntryObject entry = TocEntryObject.createProductCmptTocEntry(productCmpt.getQualifiedName(),
            xmlResourceName, 
            productCmptTypeImplClassBuilder.getQualifiedClassName(pcType.getIpsSrcFile()), 
            policyCmptTypeInterfaceBuilder.getQualifiedClassName(pcType.getIpsSrcFile()));
        IIpsObjectGeneration[] generations = productCmpt.getGenerations();
        TocEntryGeneration[] genEntries = new TocEntryGeneration[generations.length];
        for (int i = 0; i < generations.length; i++) {
            DateTime validFrom = DateTime.createDateOnly(generations[i].getValidFrom());
            IProductCmptGeneration gen = (IProductCmptGeneration)generations[i];
            String generationClassName;
            if (gen.getProductCmpt().containsFormula()) {
                generationClassName = productCmptBuilder.getQualifiedClassName((IProductCmptGeneration)generations[i]);
            } else {
                generationClassName = productCmptGenImplClassBuilder.getQualifiedClassName(gen.getProductCmpt().findProductCmptType());
            }
            genEntries[i] = new TocEntryGeneration(entry, validFrom, generationClassName, xmlResourceName); 
        }
        entry.setGenerationEntries(genEntries);
        return entry;
    }

    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.PRODUCT_CMPT.equals(ipsSrcFile.getIpsObjectType());
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "TOC-File-Builder";
    }
}
