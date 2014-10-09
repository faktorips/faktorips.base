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
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.naming.JavaClassNaming;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.util.ArgumentCheck;

/**
 * Generates the compilation unit that represents the product component. Note that only for product
 * component's that contain a config element of type formula a Java compilation unit is generated.
 * This is necessary as the formula is compiled into Java sourcecode and this Java sourcecode is
 * placed in the compilation unit generated for a product component's generation.
 * 
 */
public class ProductCmptCuBuilder extends AbstractProductCuBuilder<IProductCmpt> {

    // the product component sourcecode is generated for.
    private IProductCmpt productCmpt;

    public ProductCmptCuBuilder(StandardBuilderSet builderSet) {
        super(builderSet, ProductCmptCuBuilder.class);
    }

    @Override
    void setProperty(IProductCmpt propertyContainer) {
        setProductCmpt(productCmpt);
    }

    private void setProductCmpt(IProductCmpt productCmpt) {
        ArgumentCheck.notNull(productCmpt);
        this.productCmpt = productCmpt;
    }

    @Override
    IProductCmpt getProperty() {
        return productCmpt;
    }

    @Override
    IFormula[] getFormulas() {
        return productCmpt.getFormulas();
    }

    @Override
    String getSuperClassQualifiedClassName() throws CoreException {
        IProductCmptType pcType = productCmpt.findProductCmptType(getIpsProject());
        return getProductCmptImplBuilder().getQualifiedClassName(pcType.getIpsSrcFile());
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.PRODUCT_CMPT.equals(ipsSrcFile.getIpsObjectType());
    }

    /**
     * Generates the constructor.
     * <p>
     * Example:
     * <p>
     * 
     * <pre>
     * public MotorPolicyPk0(IRuntimeRepository repository, String id, String kindId, String versionId) {
     *     super(repository, id, kindId, versionId);
     * }
     * </pre>
     */
    @Override
    void buildConstructor(JavaCodeFragmentBuilder codeBuilder) {
        String javaDoc = getLocalizedText(CONSTRUCTOR_JAVADOC);
        try {
            //
            String className = getUnqualifiedClassName();
            String[] argNames = new String[] { "repository", "id", "kindId", "versionId" }; //$NON-NLS-1$
            String[] argClassNames = new String[] { "IRuntimeRepository", "String", "String", "String" };
            JavaCodeFragment body = new JavaCodeFragment("super(repository, id, kindId, versionId);"); //$NON-NLS-1$
            codeBuilder.addImport(IRuntimeRepository.class.getClass());
            codeBuilder.method(Modifier.PUBLIC, null, className, argNames, argClassNames, body, javaDoc);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    protected void getGeneratedJavaTypesThis(IIpsObject ipsObject, IPackageFragment fragment, List<IType> javaTypes) {
        IProductCmpt currentProductCmpt = (IProductCmpt)ipsObject;
        IIpsSrcFile productCmptSrcFile = getVirtualIpsSrcFile(currentProductCmpt);
        try {
            String typeName = getUnqualifiedClassName(productCmptSrcFile);
            ICompilationUnit compilationUnit = fragment.getCompilationUnit(typeName + JavaClassNaming.JAVA_EXTENSION);
            javaTypes.add(compilationUnit.getType(typeName));
        } catch (CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    IIpsSrcFile getVirtualIpsSrcFile(IProductCmpt productCmpt) {
        String name = getUnchangedJavaSrcFilePrefix(productCmpt.getIpsSrcFile());
        name = productCmpt.getIpsProject().getProductCmptNamingStrategy().getJavaClassIdentifier(name);
        return productCmpt.getProductCmpt().getIpsSrcFile().getIpsPackageFragment()
                .getIpsSrcFile(IpsObjectType.PRODUCT_CMPT.getFileName(name));
    }

}
