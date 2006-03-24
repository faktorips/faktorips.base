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
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptImplClassBuilder;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptBuilder extends AbstractArtefactBuilder {

    private String kindId;
    private MultiStatus buildStatus;
    
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

    /**
     * {@inheritDoc}
     */
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return IpsObjectType.PRODUCT_CMPT.equals(ipsSrcFile.getIpsObjectType());
    }
    
    /**
     * {@inheritDoc}
     */
    public void beforeBuildProcess(int buildKind) throws CoreException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    public void afterBuildProcess(int buildKind) throws CoreException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException {
        buildStatus = status;
    }

    /**
     * {@inheritDoc}
     */
    public void afterBuild(IIpsSrcFile ipsSrcFile) throws CoreException {
        // nothing to do
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
        ProductCmptGenerationCuBuilder genBuilder = newProductCmptGenerationCuBuilder(generation);
        genBuilder.beforeBuild(ipsSrcFile, buildStatus);
        genBuilder.build(ipsSrcFile);
        genBuilder.afterBuild(ipsSrcFile);
    }
    
    private ProductCmptGenerationCuBuilder newProductCmptGenerationCuBuilder(IProductCmptGeneration generation) throws CoreException {
        ProductCmptGenerationCuBuilder genBuilder = new ProductCmptGenerationCuBuilder(
                generation, getBuilderSet(), kindId);
        genBuilder.setProductCmptGenImplBuilder(productCmptGenImplBuilder);
        genBuilder.setProductCmptImplBuilder(productCmptImplBuilder);
        return genBuilder;
    }
    
    private IIpsSrcFile getVirtualIpsSrcFile(IProductCmptGeneration generation) throws CoreException {
        IProductCmptType type = generation.getProductCmpt().findProductCmptType();
        String name = generation.getProductCmpt().getName();
        name = generation.getIpsProject().getProductCmptNamingStratgey().getJavaClassIdentifier(name);
        GregorianCalendar validFrom = generation.getValidFrom();
        int month = validFrom.get(GregorianCalendar.MONTH) + 1;
        int date = validFrom.get(GregorianCalendar.DATE);
        name = name
                + validFrom.get(GregorianCalendar.YEAR)
                + (month<10?"0"+month:""+month)
                + (date<10?"0"+date:""+date);
        return type.getIpsSrcFile().getIpsPackageFragment().getIpsSrcFile(IpsObjectType.PRODUCT_CMPT.getFileName(name));
    }
    
    public String getQualifiedClassName(IProductCmptGeneration generation) throws CoreException {
        ProductCmptGenerationCuBuilder builder = newProductCmptGenerationCuBuilder(generation);
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
