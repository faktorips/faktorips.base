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

import java.lang.reflect.Modifier;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.model.builder.naming.BuilderAspect;
import org.faktorips.devtools.model.builder.xmodel.GeneratorConfig;
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductCmptGenerationClass;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.runtime.internal.ProductComponentGeneration;

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

    private ProductCmptCuBuilder productCmptCuBuilder;

    public ProductCmptGenerationCuBuilder(StandardBuilderSet builderSet, ProductCmptCuBuilder productCmptCuBuilder) {
        super(builderSet, ProductCmptGenerationCuBuilder.class);
        this.productCmptCuBuilder = productCmptCuBuilder;
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.PRODUCT_CMPT.equals(ipsSrcFile.getIpsObjectType());
    }

    /**
     * Generates the constructor.
     * <p>
     * Example:
     *
     * <pre>
     * public MotorPolicyPk0(RuntimeRepository repository, String qName, Class policyComponentType) {
     *     super(registry, qName, policyComponentType);
     * }
     * </pre>
     */
    @Override
    protected void buildConstructor(JavaCodeFragmentBuilder codeBuilder) {
        GeneratorConfig generatorConfig = GeneratorConfig.forIpsSrcFile(getIpsSrcFile());
        Locale language = generatorConfig.getLanguageUsedInGeneratedSourceCode();
        String genName = generatorConfig.getChangesOverTimeNamingConvention()
                .getGenerationConceptNameSingular(language);
        String javaDoc = getLocalizedText(AbstractProductCuBuilder.CONSTRUCTOR_JAVADOC, genName);
        String className = getUnqualifiedClassName();
        String[] argNames = { "productCmpt" }; //$NON-NLS-1$
        String qualifiedClassName = productCmptCuBuilder
                .getImplementationClass(getPropertyValueContainer().getProductCmpt());
        String[] argClassNames = { qualifiedClassName };
        JavaCodeFragment body = new JavaCodeFragment("super(productCmpt);"); //$NON-NLS-1$
        codeBuilder.method(Modifier.PUBLIC, null, className, argNames, argClassNames, body, javaDoc);
    }

    @Override
    protected void getGeneratedJavaTypesThis(IIpsObject ipsObject, IPackageFragment fragment, List<IType> javaTypes)
            throws CoreException {
        IProductCmpt productCmpt = (IProductCmpt)ipsObject;
        for (IIpsObjectGeneration currentGeneration : productCmpt.getGenerations()) {
            IIpsSrcFile generationSrcFile = getVirtualIpsSrcFile((IProductCmptGeneration)currentGeneration);
            String typeName = getUnqualifiedClassName(generationSrcFile);
            IType type = getJavaType(fragment, typeName);
            javaTypes.add(type);
        }
    }

    @Override
    protected IIpsSrcFile getVirtualIpsSrcFile(IProductCmptGeneration generation) {
        GregorianCalendar validFrom = generation.getValidFrom();
        int month = validFrom.get(Calendar.MONTH) + 1;
        int date = validFrom.get(Calendar.DATE);
        String name = getUnchangedJavaSrcFilePrefix(generation.getIpsSrcFile()) + ' ' + validFrom.get(Calendar.YEAR)
                + (month < 10 ? "0" + month : "" + month) //$NON-NLS-1$ //$NON-NLS-2$
                + (date < 10 ? "0" + date : "" + date); //$NON-NLS-1$ //$NON-NLS-2$
        name = generation.getIpsProject().getProductCmptNamingStrategy().getJavaClassIdentifier(name);
        return generation.getProductCmpt().getIpsSrcFile().getIpsPackageFragment()
                .getIpsSrcFile(IpsObjectType.PRODUCT_CMPT.getFileName(name));
    }

    @Override
    protected String getSuperClassQualifiedClassName(IProductCmptGeneration productCmptGeneration) {
        return getBuilderSet().getModelNode(productCmptGeneration.findProductCmptType(getIpsProject()),
                XProductCmptGenerationClass.class).getQualifiedName(BuilderAspect.IMPLEMENTATION);
    }

}
