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

import java.lang.reflect.Modifier;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.naming.JavaClassNaming;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.ProductCmptGenerationClassBuilder;
import org.faktorips.util.ArgumentCheck;

/**
 * Generates the compilation unit that represents the product component generation. Note that only
 * for product component's that contain a config element of type formula a Java compilation unit is
 * generated. This is necessary as the formula is compiled into Java sourcecode and this Java
 * sourcecode is placed in the compilation unit generated for a product component's generation.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptGenerationCuBuilder extends AbstractProductCuBuilder<IProductCmptGeneration> {

    // the product component generation sourcecode is generated for.
    private IProductCmptGeneration generation;

    // builders needed
    private ProductCmptGenerationClassBuilder productCmptGenImplBuilder;
    private ProductCmptCuBuilder productCmptCuBuilder;

    public ProductCmptGenerationCuBuilder(StandardBuilderSet builderSet, ProductCmptCuBuilder productCmptCuBuilder) {
        super(builderSet, ProductCmptGenerationCuBuilder.class);
        this.productCmptCuBuilder = productCmptCuBuilder;

    }

    IFile getGeneratedJavaFile(IProductCmptGeneration gen) throws CoreException {
        IIpsSrcFile ipsSrcFile = getVirtualIpsSrcFile(gen);
        return getJavaFile(ipsSrcFile);
    }

    private void setProductCmptGeneration(IProductCmptGeneration generation) {
        ArgumentCheck.notNull(generation);
        this.generation = generation;
    }

    @Override
    public IFormula[] getFormulas() {
        return generation.getFormulas();
    }

    @Override
    String getSuperClassQualifiedClassName() throws CoreException {
        IProductCmptType pcType = generation.getProductCmpt().findProductCmptType(getIpsProject());
        return productCmptGenImplBuilder.getQualifiedClassName(pcType.getIpsSrcFile());
    }

    public void setProductCmptGenImplBuilder(ProductCmptGenerationClassBuilder builder) {
        productCmptGenImplBuilder = builder;
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.PRODUCT_CMPT.equals(ipsSrcFile.getIpsObjectType());
    }

    public IProductCmptType getProductCmptType() {
        try {
            return generation.findProductCmptType(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Generates the constructor.
     * <p>
     * Example:
     * <p>
     * 
     * <pre>
     * public MotorPolicyPk0(RuntimeRepository repository, String qName, Class policyComponentType) {
     *     super(registry, qName, policyComponentType);
     * }
     * </pre>
     */
    @Override
    void buildConstructor(JavaCodeFragmentBuilder codeBuilder) {
        Locale language = getLanguageUsedInGeneratedSourceCode();
        String genName = getChangesInTimeNamingConvention(generation).getGenerationConceptNameSingular(language);
        String javaDoc = getLocalizedText(AbstractProductCuBuilder.CONSTRUCTOR_JAVADOC, genName);
        try {
            String className = getUnqualifiedClassName();
            String[] argNames = new String[] { "productCmpt" }; //$NON-NLS-1$
            String qualifiedClassName = getImplementationClassProductCmpt(generation.getProductCmpt());
            String[] argClassNames = new String[] { qualifiedClassName };
            JavaCodeFragment body = new JavaCodeFragment("super(productCmpt);"); //$NON-NLS-1$
            codeBuilder.method(Modifier.PUBLIC, null, className, argNames, argClassNames, body, javaDoc);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
    }

    String getImplementationClassProductCmpt(IProductCmpt productCmpt) throws CoreException {
        if (productCmpt.isContainingAvailableFormula() && getBuilderSet().getFormulaCompiling().isCompileToSubclass()) {
            return productCmptCuBuilder.getQualifiedClassName(productCmpt);
        } else {
            return getProductCmptImplBuilder().getQualifiedClassName(productCmpt.findProductCmptType(getIpsProject()));
        }
    }

    @Override
    protected void getGeneratedJavaTypesThis(IIpsObject ipsObject, IPackageFragment fragment, List<IType> javaTypes) {
        IProductCmpt productCmpt = (IProductCmpt)ipsObject;
        for (IIpsObjectGeneration currentGeneration : productCmpt.getGenerations()) {
            IIpsSrcFile generationSrcFile = getVirtualIpsSrcFile((IProductCmptGeneration)currentGeneration);
            try {
                String typeName = getUnqualifiedClassName(generationSrcFile);
                ICompilationUnit compilationUnit = fragment.getCompilationUnit(typeName
                        + JavaClassNaming.JAVA_EXTENSION);
                javaTypes.add(compilationUnit.getType(typeName));
            } catch (CoreException e) {
                throw new CoreRuntimeException(e.getMessage(), e);
            }
        }
    }

    @Override
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

    @Override
    void setProperty(IProductCmptGeneration propertyContainer) {
        setProductCmptGeneration(propertyContainer);
    }

    @Override
    IProductCmptGeneration getProperty() {
        return generation;
    }

}
