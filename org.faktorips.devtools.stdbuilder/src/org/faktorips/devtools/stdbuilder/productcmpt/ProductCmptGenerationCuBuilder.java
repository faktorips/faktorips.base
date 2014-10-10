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
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.util.ArgumentCheck;

/**
 * Generates special runtime classes for product component generations. These classes are themselves
 * subclasses of the normally generated {@link ProductComponentGeneration} classes. Their sole
 * purpose is to provide the compiled to java source code for all formula expressions contained in
 * the product component generation.
 * 
 * Accordingly only for product component generations that contain formulas (and entered
 * expressions) such Java compilation units are generated.
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
            return productCmptCuBuilder.getQualifiedClassNameOfProductCmpt(productCmpt);
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
