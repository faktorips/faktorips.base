/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpt;

import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AContainer;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AResource.AResourceType;
import org.faktorips.devtools.model.builder.AbstractArtefactBuilder;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

/**
 * 
 */
public class ProductCmptBuilder extends AbstractArtefactBuilder {

    private MultiStatus buildStatus;
    private ProductCmptGenerationCuBuilder generationBuilder;
    private ProductCmptCuBuilder productCmptCuBuilder;

    public ProductCmptBuilder(StandardBuilderSet builderSet) {
        super(builderSet);
        productCmptCuBuilder = new ProductCmptCuBuilder(builderSet);
        generationBuilder = new ProductCmptGenerationCuBuilder(builderSet, productCmptCuBuilder);
    }

    @Override
    public StandardBuilderSet getBuilderSet() {
        return (StandardBuilderSet)super.getBuilderSet();
    }

    public AbstractProductCuBuilder<IProductCmptGeneration> getGenerationBuilder() {
        return generationBuilder;
    }

    @Override
    public String getName() {
        return "ProductCmptBuilder"; //$NON-NLS-1$
    }

    @Override
    public void beforeBuildProcess(IIpsProject project, ABuildKind buildKind) {
        super.beforeBuildProcess(project, buildKind);
        productCmptCuBuilder.beforeBuildProcess(project, buildKind);
        generationBuilder.beforeBuildProcess(project, buildKind);
    }

    @Override
    public void afterBuildProcess(IIpsProject project, ABuildKind buildKind) {
        super.afterBuildProcess(project, buildKind);
        productCmptCuBuilder.afterBuildProcess(project, buildKind);
        generationBuilder.afterBuildProcess(project, buildKind);
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.PRODUCT_CMPT.equals(ipsSrcFile.getIpsObjectType());
    }

    @Override
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) {
        super.beforeBuild(ipsSrcFile, status);
        buildStatus = status;
    }

    @Override
    public void build(IIpsSrcFile ipsSrcFile) {
        IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
        if (productCmpt.isValid(getIpsProject())) {
            if (requiresJavaCompilationUnit(productCmpt)) {
                build(productCmpt);
            }
            List<IProductCmptGeneration> generations = productCmpt.getProductCmptGenerations();
            for (IProductCmptGeneration generation : generations) {
                if (requiresJavaCompilationUnit(generation)) {
                    build(generation);
                }
            }
        }
    }

    private void build(IProductCmpt productCmpt) {
        productCmptCuBuilder.callBuildProcess(productCmpt, buildStatus);
    }

    private void build(IProductCmptGeneration generation) {
        generationBuilder.callBuildProcess(generation, buildStatus);
    }

    public String getImplementationClass(IProductCmpt productCmpt) {
        return productCmptCuBuilder.getImplementationClass(productCmpt);
    }

    public String getImplementationClass(IProductCmptGeneration generation) {
        return generationBuilder.getImplementationClass(generation);
    }

    public String getQualifiedClassName(IProductCmptGeneration generation) {
        return generationBuilder.getQualifiedClassName(generation);
    }

    private boolean requiresJavaCompilationUnit(IPropertyValueContainer container) {
        if (!isContainingAvailableFormula(container)
                || (container.findProductCmptType(container.getIpsProject()) == null)) {
            return false;
        }
        return true;
    }

    @Override
    public void delete(IIpsSrcFile deletedFile) {
        // the problem here, is that the file is deleted and so we can't access the generations.
        // so we can get the exact file names, as the generation's valid from is part of the file
        // name
        // instead we delete all file that start with the common prefix.
        String prefix = generationBuilder.getJavaSrcFilePrefix(deletedFile);
        // get a file handle in the
        AFile file = generationBuilder.getJavaFile(deletedFile);
        // target folder
        AContainer folder = file.getParent();
        // now delete all files that start with the common
        SortedSet<? extends AResource> members = folder == null ? Collections.emptySortedSet() : folder.getMembers();
        // prefix
        for (AResource member : members) {
            if (member.getType() == AResourceType.FILE && member.getName().startsWith(prefix)) {
                member.delete(null);
            }
        }
    }

    @Override
    public boolean buildsDerivedArtefacts() {
        return true;
    }

    @Override
    public boolean isBuildingInternalArtifacts() {
        return getBuilderSet().isGeneratePublishedInterfaces();
    }

    /**
     * Returns <code>true</code> if there is at least one formula that has an entered expression.
     * Returns <code>false</code> if there is no formula or if every formula has no entered
     * expression.
     * 
     * @param container The product component or product component generation that may contain the
     *            formulas
     * @return <code>true</code> for at least one available formula
     */
    public boolean isContainingAvailableFormula(IPropertyValueContainer container) {
        AbstractProductCuBuilder<? extends IPropertyValueContainer> cuBuilder = getCuBuilderFor(container);
        return cuBuilder.isContainingAvailableFormula(container);
    }

    private AbstractProductCuBuilder<? extends IPropertyValueContainer> getCuBuilderFor(
            IPropertyValueContainer container) {
        if (container instanceof IProductCmpt) {
            return productCmptCuBuilder;
        } else {
            return generationBuilder;
        }
    }

}
