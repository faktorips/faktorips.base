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

package org.faktorips.devtools.stdbuilder.productcmpt;

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.devtools.core.builder.AbstractArtefactBuilder;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptImplClassBuilder;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptBuilder extends AbstractArtefactBuilder {

    private String kindId;
    private MultiStatus buildStatus;
    private ProductCmptGenerationCuBuilder productCmptGenerationBuilder;
    
    // builders needed
    private ProductCmptImplClassBuilder productCmptImplBuilder;
    private ProductCmptGenImplClassBuilder productCmptGenImplBuilder;

    /**
     * 
     */
    public ProductCmptBuilder(IIpsArtefactBuilderSet builderSet, String kindId) {
        super(builderSet);
        this.kindId = kindId;
        
    }

    public void setProductCmptImplBuilder(ProductCmptImplClassBuilder builder) {
        this.productCmptImplBuilder = builder;
    }

    public void setProductCmptGenImplBuilder(ProductCmptGenImplClassBuilder builder) {
        this.productCmptGenImplBuilder = builder;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "ProductCmptBuilder";
    }

    public void beforeBuildProcess(IIpsProject project, int buildKind) throws CoreException {
        super.beforeBuildProcess(project, buildKind);
        productCmptGenerationBuilder = newProductCmptGenerationCuBuilder();
        productCmptGenerationBuilder.beforeBuildProcess(project, buildKind);
    }
    
    public void afterBuildProcess(IIpsProject project, int buildKind) throws CoreException {
        super.afterBuildProcess(project, buildKind);
        productCmptGenerationBuilder.afterBuildProcess(project, buildKind);
        productCmptGenerationBuilder = null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return IpsObjectType.PRODUCT_CMPT.equals(ipsSrcFile.getIpsObjectType());
    }

    /**
     * {@inheritDoc}
     */
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException {
        super.beforeBuild(ipsSrcFile, status);
        buildStatus = status;
    }

    /**
     * {@inheritDoc}
     */
    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
        if (!productCmpt.containsFormula()) {
            return;
        }
        if (productCmpt.findProductCmptType()==null) {
            // if the type can't be found, nothing an be generated.
            return;
        }
        IIpsObjectGeneration[] generations = productCmpt.getGenerations();
        for (int i = 0; i < generations.length; i++) {
            build((IProductCmptGeneration)generations[i]);
        }
    }
    
    private void build(IProductCmptGeneration generation) throws CoreException {
        IIpsSrcFile ipsSrcFile = getVirtualIpsSrcFile(generation);
        productCmptGenerationBuilder.setProductCmptGeneration(generation);
        productCmptGenerationBuilder.beforeBuild(ipsSrcFile, buildStatus);
        productCmptGenerationBuilder.build(ipsSrcFile);
        productCmptGenerationBuilder.afterBuild(ipsSrcFile);
    }
    
    private ProductCmptGenerationCuBuilder newProductCmptGenerationCuBuilder(){
        ProductCmptGenerationCuBuilder genBuilder = new ProductCmptGenerationCuBuilder(getBuilderSet(), kindId);
        genBuilder.setProductCmptGenImplBuilder(productCmptGenImplBuilder);
        genBuilder.setProductCmptImplBuilder(productCmptImplBuilder);
        return genBuilder;
    }
    
    private IIpsSrcFile getVirtualIpsSrcFile(IProductCmptGeneration generation) throws CoreException {
        GregorianCalendar validFrom = generation.getValidFrom();
        int month = validFrom.get(GregorianCalendar.MONTH) + 1;
        int date = validFrom.get(GregorianCalendar.DATE);
        String name = generation.getProductCmpt().getName() + " " + 
                + validFrom.get(GregorianCalendar.YEAR)
                + (month<10?"0"+month:""+month)
                + (date<10?"0"+date:""+date);
        name = generation.getIpsProject().getProductCmptNamingStrategy().getJavaClassIdentifier(name);
        return generation.getProductCmpt().getIpsSrcFile().getIpsPackageFragment().getIpsSrcFile(IpsObjectType.PRODUCT_CMPT.getFileName(name));
    }
    
    public String getQualifiedClassName(IProductCmptGeneration generation) throws CoreException {
        ProductCmptGenerationCuBuilder builder = newProductCmptGenerationCuBuilder();
        builder.setProductCmptGeneration(generation);
        IIpsSrcFile file = getVirtualIpsSrcFile(generation);
        return builder.getQualifiedClassName(file);
    }

    /**
     * {@inheritDoc}
     */
    public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {
        // TODO Auto-generated method stub

    }

}
