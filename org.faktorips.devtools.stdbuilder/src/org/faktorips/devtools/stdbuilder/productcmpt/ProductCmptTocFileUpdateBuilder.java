package org.faktorips.devtools.stdbuilder.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.stdbuilder.AbstractTocFileUpdateBuilder;
import org.faktorips.devtools.stdbuilder.pctype.PolicyCmptTypeInterfaceCuBuilder;
import org.faktorips.runtime.TocEntry;

public class ProductCmptTocFileUpdateBuilder extends AbstractTocFileUpdateBuilder {

    private PolicyCmptTypeInterfaceCuBuilder policyCmptTypeInterfaceBuilder;
    private ProductCmptGenerationCuBuilder productCmptGenerationBuilder;

    /**
     * @param structure
     */
    public ProductCmptTocFileUpdateBuilder(IJavaPackageStructure structure, String kind) {
        super(structure, kind);
    }

    public void setPolicyCmptTypeInterfaceBuilder(PolicyCmptTypeInterfaceCuBuilder policyCmptTypeInterfaceBuilder) {
        this.policyCmptTypeInterfaceBuilder = policyCmptTypeInterfaceBuilder;
    }

    public void setProductCmptGenerationBuilder(ProductCmptGenerationCuBuilder productCmptGenerationBuilder) {
        this.productCmptGenerationBuilder = productCmptGenerationBuilder;
    }

    protected void checkIfDependOnBuildersSet() throws IllegalStateException {
        String builderName = null;

        if (policyCmptTypeInterfaceBuilder == null) {
            builderName = PolicyCmptTypeInterfaceCuBuilder.class.getName();
        }

        if(productCmptGenerationBuilder == null) {
            builderName = ProductCmptGenerationCuBuilder.class.getName();
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
    protected TocEntry createTocEntry(IIpsObject ipsObject) throws CoreException {

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
        TocEntry entry = TocEntry.createProductCmptTocEntry(productCmpt.getQualifiedName(),
            xmlResourceName, productCmptGenerationBuilder.getQualifiedClassName(productCmpt
                    .getIpsSrcFile()), policyCmptTypeInterfaceBuilder.getQualifiedClassName(pcType
                    .getIpsSrcFile()), null);
        return entry;
    }

    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.PRODUCT_CMPT.equals(ipsSrcFile.getIpsObjectType());
    }
}
