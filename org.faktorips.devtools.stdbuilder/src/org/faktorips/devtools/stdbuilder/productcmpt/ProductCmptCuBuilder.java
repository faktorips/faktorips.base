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
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.model.builder.naming.BuilderAspect;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductCmptClass;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.ProductComponent;

/**
 * Generates special runtime classes for product components. These classes are themselves subclasses
 * of the normally generated {@link ProductComponent} classes. Their sole purpose is to provide the
 * compiled to java source code for all formula expressions contained in the product component.
 * 
 * Accordingly only for product components that contain formulas (and entered expressions) such Java
 * compilation units are generated.
 * 
 */
public class ProductCmptCuBuilder extends AbstractProductCuBuilder<IProductCmpt> {

    public ProductCmptCuBuilder(StandardBuilderSet builderSet) {
        super(builderSet, ProductCmptCuBuilder.class);
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
     * public MotorPolicyPk0(IRuntimeRepository repository, String id, String kindId, String versionId) {
     *     super(repository, id, kindId, versionId);
     * }
     * </pre>
     */
    @Override
    protected void buildConstructor(JavaCodeFragmentBuilder codeBuilder) {
        String javaDoc = getLocalizedText(CONSTRUCTOR_JAVADOC);
        //
        String className = getUnqualifiedClassName();
        String[] argNames = new String[] { "repository", "id", "kindId", "versionId" }; //$NON-NLS-1$
        String[] argClassNames = new String[] { "IRuntimeRepository", "String", "String", "String" };
        JavaCodeFragment body = new JavaCodeFragment("super(repository, id, kindId, versionId);"); //$NON-NLS-1$
        codeBuilder.addImport(IRuntimeRepository.class);
        codeBuilder.method(Modifier.PUBLIC, null, className, argNames, argClassNames, body, javaDoc);
    }

    @Override
    protected void getGeneratedJavaTypesThis(IIpsObject ipsObject, IPackageFragment fragment, List<IType> javaTypes)
            throws CoreException {
        IProductCmpt currentProductCmpt = (IProductCmpt)ipsObject;
        IIpsSrcFile productCmptSrcFile = getVirtualIpsSrcFile(currentProductCmpt);
        String typeName = getUnqualifiedClassName(productCmptSrcFile);
        IType type = getJavaType(fragment, typeName);
        javaTypes.add(type);
    }

    @Override
    protected IIpsSrcFile getVirtualIpsSrcFile(IProductCmpt productCmpt) {
        String name = getUnchangedJavaSrcFilePrefix(productCmpt.getIpsSrcFile());
        name = productCmpt.getIpsProject().getProductCmptNamingStrategy().getJavaClassIdentifier(name);
        return productCmpt.getProductCmpt().getIpsSrcFile().getIpsPackageFragment()
                .getIpsSrcFile(IpsObjectType.PRODUCT_CMPT.getFileName(name));
    }

    @Override
    protected String getSuperClassQualifiedClassName(IProductCmpt productCmpt) {
        return getBuilderSet().getModelNode(productCmpt.findProductCmptType(getIpsProject()), XProductCmptClass.class)
                .getQualifiedName(BuilderAspect.IMPLEMENTATION);
    }

}
