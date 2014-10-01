/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpt;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.devtools.core.builder.AbstractArtefactBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.ProductCmptClassBuilder;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.ProductCmptGenerationClassBuilder;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptBuilder extends AbstractArtefactBuilder {

    private MultiStatus buildStatus;
    private ProductCmptGenerationCuBuilder generationBuilder;
    private ProductCmptCuBuilder productCmptBuilder;

    public ProductCmptBuilder(StandardBuilderSet builderSet) {
        super(builderSet);
        generationBuilder = new ProductCmptGenerationCuBuilder(builderSet, this);
        productCmptBuilder = new ProductCmptCuBuilder(builderSet, this);
    }

    @Override
    public StandardBuilderSet getBuilderSet() {
        return (StandardBuilderSet)super.getBuilderSet();
    }

    public void setProductCmptImplBuilder(ProductCmptClassBuilder builder) {
        productCmptBuilder.setProductCmptImplBuilder(builder);
        generationBuilder.setProductCmptImplBuilder(builder);
    }

    public void setProductCmptGenImplBuilder(ProductCmptGenerationClassBuilder builder) {
        generationBuilder.setProductCmptGenImplBuilder(builder);
    }

    public ProductCmptGenerationCuBuilder getGenerationBuilder() {
        return generationBuilder;
    }

    @Override
    public String getName() {
        return "ProductCmptBuilder"; //$NON-NLS-1$
    }

    @Override
    public void beforeBuildProcess(IIpsProject project, int buildKind) throws CoreException {
        super.beforeBuildProcess(project, buildKind);
        productCmptBuilder.beforeBuildProcess(project, buildKind);
        generationBuilder.beforeBuildProcess(project, buildKind);
    }

    @Override
    public void afterBuildProcess(IIpsProject project, int buildKind) throws CoreException {
        super.afterBuildProcess(project, buildKind);
        productCmptBuilder.afterBuildProcess(project, buildKind);
        generationBuilder.afterBuildProcess(project, buildKind);
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return IpsObjectType.PRODUCT_CMPT.equals(ipsSrcFile.getIpsObjectType());
    }

    @Override
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException {
        super.beforeBuild(ipsSrcFile, status);
        buildStatus = status;
    }

    @Override
    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
        if (productCmpt.isValid(getIpsProject())) {
            if (mustFileBeBuild(productCmpt)) {
                build(productCmpt);
            }
            IIpsObjectGeneration[] generations = productCmpt.getGenerationsOrderedByValidDate();
            for (IIpsObjectGeneration generation : generations) {
                if (mustFileBeBuild((IProductCmptGeneration)generation)) {
                    build((IProductCmptGeneration)generation);
                }
            }
        }
    }

    private void build(IProductCmpt productCmpt) throws CoreException {
        IIpsSrcFile ipsSrcFile = getVirtualIpsSrcFile(productCmpt);
        productCmptBuilder.setProductCmpt(productCmpt);
        productCmptBuilder.beforeBuild(ipsSrcFile, buildStatus);
        if (getBuilderSet().getFormulaCompiling().isCompileToSubclass()) {
            productCmptBuilder.build(ipsSrcFile);
        }
        productCmptBuilder.afterBuild(ipsSrcFile);
    }

    private void build(IProductCmptGeneration generation) throws CoreException {
        IIpsSrcFile ipsSrcFile = getVirtualIpsSrcFile(generation);
        generationBuilder.setProductCmptGeneration(generation);
        generationBuilder.beforeBuild(ipsSrcFile, buildStatus);
        if (getBuilderSet().getFormulaCompiling().isCompileToSubclass()) {
            generationBuilder.build(ipsSrcFile);
        }
        generationBuilder.afterBuild(ipsSrcFile);
    }

    public String getQualifiedClassName(IProductCmptGeneration generation) throws CoreException {
        generationBuilder.setProductCmptGeneration(generation);
        IIpsSrcFile file = getVirtualIpsSrcFile(generation);
        return generationBuilder.getQualifiedClassName(file);
    }

    public String getQualifiedClassName(IProductCmpt productCmpt) throws CoreException {
        productCmptBuilder.setProductCmpt(productCmpt);
        IIpsSrcFile file = getVirtualIpsSrcFile(productCmpt);
        return productCmptBuilder.getQualifiedClassName(file);
    }

    /**
     * Returns the Java sourcefile that is generated for the given generation or <code>null</code>
     * if no sourcefile is generated, because the product component doesn't contain a formula.
     */
    public IFile getGeneratedJavaFile(IProductCmptGeneration gen) throws CoreException {
        if (!mustFileBeBuild(gen)) {
            return null;
        }
        generationBuilder.setProductCmptGeneration(gen);
        return generationBuilder.getJavaFile(getVirtualIpsSrcFile(gen));
    }

    private boolean mustFileBeBuild(IProductCmptGeneration productCmptGeneration) throws CoreException {
        if (!productCmptGeneration.isContainingAvailableFormula()) {
            return false;
        }
        if (productCmptGeneration.findProductCmptType(productCmptGeneration.getIpsProject()) == null) {
            return false;
        }
        return true;
    }

    private boolean mustFileBeBuild(IProductCmpt productCmpt) throws CoreException {
        if (!productCmpt.isContainingAvailableFormula()) {
            return false;
        }
        if (productCmpt.findProductCmptType(productCmpt.getIpsProject()) == null) {
            return false;
        }
        return true;
    }

    @Override
    public void delete(IIpsSrcFile deletedFile) throws CoreException {
        // the problem here, is that the file is deleted and so we can't access the generations.
        // so we can get the exact file names, as the generation's valid from is part of the file
        // name
        // instead we delete all file that start with the common prefix.
        String prefix = getJavaSrcFilePrefix(deletedFile);
        // get a file handle in the
        IFile file = generationBuilder.getJavaFile(deletedFile);
        // target folder
        IContainer folder = file.getParent();
        // now delete all files that start with the common
        IResource[] members = folder.members();
        // prefix
        for (IResource member : members) {
            if (member.getType() == IResource.FILE && member.getName().startsWith(prefix)) {
                member.delete(true, null);
            }
        }
    }

    /**
     * Constructs a virtual ips source file. the name is derived from the product component and the
     * generation's valid from date. This is done to use the superclass' mechanism to derive the (to
     * be generated) Java sourcefile for a given ips src file.
     */
    IIpsSrcFile getVirtualIpsSrcFile(IProductCmptGeneration generation) {
        GregorianCalendar validFrom = generation.getValidFrom();
        int month = validFrom.get(Calendar.MONTH) + 1;
        int date = validFrom.get(Calendar.DATE);
        String name = getUnchangedJavaSrcFilePrefix(generation.getIpsSrcFile()) + validFrom.get(Calendar.YEAR)
                + (month < 10 ? "0" + month : "" + month) //$NON-NLS-1$ //$NON-NLS-2$
                + (date < 10 ? "0" + date : "" + date); //$NON-NLS-1$ //$NON-NLS-2$
        name = generation.getIpsProject().getProductCmptNamingStrategy().getJavaClassIdentifier(name);
        return generation.getProductCmpt().getIpsSrcFile().getIpsPackageFragment()
                .getIpsSrcFile(IpsObjectType.PRODUCT_CMPT.getFileName(name));
    }

    IIpsSrcFile getVirtualIpsSrcFile(IProductCmpt productCmpt) {
        String name = getUnchangedJavaSrcFilePrefix(productCmpt.getIpsSrcFile());
        name = productCmpt.getIpsProject().getProductCmptNamingStrategy().getJavaClassIdentifier(name);
        return productCmpt.getProductCmpt().getIpsSrcFile().getIpsPackageFragment()
                .getIpsSrcFile(IpsObjectType.PRODUCT_CMPT.getFileName(name));
    }

    /**
     * Returns the prefix that is common to the Java source file for all generations.
     */
    private String getJavaSrcFilePrefix(IIpsSrcFile file) {
        return file.getIpsProject().getProductCmptNamingStrategy()
                .getJavaClassIdentifier(getUnchangedJavaSrcFilePrefix(file));
    }

    /**
     * Returns the prefix that is common to the Java source file for all generations before the
     * project's naming strategy is applied to replace characters that aren't allowed in Java class
     * names.
     */
    private String getUnchangedJavaSrcFilePrefix(IIpsSrcFile file) {
        return file.getQualifiedNameType().getUnqualifiedName() + ' ';
    }

    @Override
    public boolean buildsDerivedArtefacts() {
        return true;
    }

    @Override
    public boolean isBuildingInternalArtifacts() {
        return getBuilderSet().isGeneratePublishedInterfaces();
    }

}
