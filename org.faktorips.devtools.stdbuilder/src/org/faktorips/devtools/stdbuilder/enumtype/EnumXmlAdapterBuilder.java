/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.enumtype;

import java.lang.reflect.Modifier;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.TypeSection;
import org.faktorips.devtools.core.builder.naming.DefaultJavaClassNameProvider;
import org.faktorips.devtools.core.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xmodel.GeneratorConfig;
import org.faktorips.devtools.stdbuilder.xmodel.enumtype.XEnumType;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.util.LocalizedStringsSet;

/**
 * A builder for JAXB XmlAdapters. XmlAdapters are generated for Faktor-IPS enumerations that defer
 * their content to a Faktor-IPS enumeration content. These contents can only be accessed through
 * the {@link IRuntimeRepository}. This is the responsibility of the generated XmlAdapter.
 * 
 * @author Peter Kuntz
 */
public class EnumXmlAdapterBuilder extends DefaultJavaSourceFileBuilder {

    private final IJavaClassNameProvider javaClassNamingProvider;

    public EnumXmlAdapterBuilder(StandardBuilderSet builderSet) {
        super(builderSet, new LocalizedStringsSet(EnumXmlAdapterBuilder.class));
        javaClassNamingProvider = new DefaultJavaClassNameProvider(builderSet.isGeneratePublishedInterfaces()) {
            @Override
            public String getImplClassName(IIpsSrcFile ipsSrcFile) {
                return ipsSrcFile.getIpsProject().getJavaNamingConvention()
                        .getImplementationClassName(ipsSrcFile.getIpsObjectName() + "XmlAdapter"); //$NON-NLS-1$
            }

            @Override
            public boolean isImplClassInternalArtifact() {
                return isBuildingInternalArtifacts();
            }

        };
    }

    @Override
    public StandardBuilderSet getBuilderSet() {
        return (StandardBuilderSet)super.getBuilderSet();
    }

    @Override
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return javaClassNamingProvider;
    }

    @Override
    public boolean buildsDerivedArtefacts() {
        return false;
    }

    /** Returns the enum type for that code is being generated. */
    private IEnumType getEnumType() {
        return (IEnumType)getIpsObject();
    }

    @Override
    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        if (!GeneratorConfig.forIpsSrcFile(ipsSrcFile).isGenerateJaxbSupport()) {
            return;
        }
        super.build(ipsSrcFile);
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        if (ipsSrcFile.getIpsObjectType().equals(IpsObjectType.ENUM_TYPE) && ipsSrcFile.exists()) {
            IEnumType enumType = (IEnumType)ipsSrcFile.getIpsObject();
            return enumType.isExtensible() && !enumType.isAbstract();
        }
        return false;
    }

    @Override
    protected void generateCodeForJavatype() throws CoreException {
        TypeSection mainSection = getMainTypeSection();
        mainSection.getJavaDocForTypeBuilder()
                .javaDoc(getLocalizedText("CLASS_JAVADOC", getEnumType().getQualifiedName()));
        mainSection.setClass(true);
        mainSection.setEnum(false);
        mainSection.setClassModifier(Modifier.PUBLIC);
        mainSection.setUnqualifiedName(getUnqualifiedClassName(getEnumType().getIpsSrcFile()));
        IEnumAttribute idAttribute = getEnumType().findIdentiferAttribute(getIpsProject());
        if (idAttribute == null || !idAttribute.isValid(getIpsProject())) {
            return;
        }
        DatatypeHelper datatypeHelper = getIpsProject().getDatatypeHelper(idAttribute.findDatatype(getIpsProject()));

        StringBuffer superClassName = new StringBuffer();
        superClassName.append("javax.xml.bind.annotation.adapters.XmlAdapter"); //$NON-NLS-1$
        superClassName.append("<"); //$NON-NLS-1$
        superClassName.append(datatypeHelper.getJavaClassName());
        superClassName.append(", "); //$NON-NLS-1$
        superClassName.append(getEnumModelNode().getQualifiedClassName());
        superClassName.append(">"); //$NON-NLS-1$
        mainSection.setSuperClass(superClassName.toString());

        generateConstructor(mainSection.getConstructorBuilder());
        generateMethodMarshal(mainSection.getMethodBuilder(), datatypeHelper);
        generateMethodUnMarshal(mainSection.getMethodBuilder(), datatypeHelper);
        generateFieldRepository(mainSection.getMemberVarBuilder());
    }

    /**
     * <pre>
     * [Javadoc]
     *      private IRuntimeRepository repository;
     * </pre>
     */
    private void generateFieldRepository(JavaCodeFragmentBuilder builder) {
        builder.javaDoc(getLocalizedText("FIELD_REPOSITORY_JAVADOC"));
        builder.varDeclaration(Modifier.PRIVATE, IRuntimeRepository.class, "repository"); //$NON-NLS-1$
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     *      public AnEnumXmlAdapter(IRuntimeRepository repository) {
     *          this.repository = repository;
     *      }
     * </pre>
     */
    private void generateConstructor(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.javaDoc(getLocalizedText("CONSTRUCTOR_JAVADOC"));
        builder.methodBegin(Modifier.PUBLIC, null, getUnqualifiedClassName(getEnumType().getIpsSrcFile()),
                new String[] { "repository" }, new Class[] { IRuntimeRepository.class }); //$NON-NLS-1$
        builder.append(new JavaCodeFragment("this.repository = repository;")); //$NON-NLS-1$
        builder.methodEnd();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     *   public String marshal(AnEnum value) {
     *      if (value == null) {
     *          return null;
     *      }
     *      return value.getId();
     *   }
     * </pre>
     */
    private void generateMethodMarshal(JavaCodeFragmentBuilder builder, DatatypeHelper datatypeHelper) {

        JavaCodeFragment body = new JavaCodeFragment();
        body.appendln("if(value == null)"); //$NON-NLS-1$
        body.appendOpenBracket();
        body.append("return null;"); //$NON-NLS-1$
        body.appendCloseBracket();
        body.append("return value."); //$NON-NLS-1$
        body.append(getEnumModelNode().getIdentifierAttribute().getMethodNameGetter());
        body.append("();"); //$NON-NLS-1$

        builder.javaDoc(getLocalizedText("METHOD_MARSHAL_JAVADOC"));
        appendOverrideAnnotation(builder, false);
        builder.method(Modifier.PUBLIC, datatypeHelper.getJavaClassName(), "marshal", new String[] { "value" }, //$NON-NLS-1$ //$NON-NLS-2$
                new String[] { getEnumModelNode().getQualifiedClassName() }, body, null);
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     *  public AnEnum unmarshal(String id) {
     *      if (id == null) {
     *          return null;
     *      }
     *      return repository.getEnumValue(AnEnum.class, id);
     *  }
     * </pre>
     */
    private void generateMethodUnMarshal(JavaCodeFragmentBuilder builder, DatatypeHelper datatypeHelper) {

        JavaCodeFragment body = new JavaCodeFragment();
        body.appendln("if(id == null)"); //$NON-NLS-1$
        body.appendOpenBracket();
        body.append("return null;"); //$NON-NLS-1$
        body.appendCloseBracket();
        body.append("return "); //$NON-NLS-1$
        body.append("repository.getEnumValue("); //$NON-NLS-1$
        body.appendClassName(getEnumModelNode().getQualifiedClassName());
        body.append(".class, "); //$NON-NLS-1$
        body.append("id);"); //$NON-NLS-1$

        builder.javaDoc(getLocalizedText("METHOD_UNMARSHAL_JAVADOC"));
        appendOverrideAnnotation(builder, false);
        builder.method(Modifier.PUBLIC, getEnumModelNode().getQualifiedClassName(), "unmarshal", //$NON-NLS-1$
                new String[] { "id" }, new String[] { datatypeHelper.getJavaClassName() }, body, null); //$NON-NLS-1$

    }

    @Override
    protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements,
            IIpsObjectPartContainer ipsObjectPartContainer) {
        // nothing to do
    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        return false;
    }

    @Override
    protected boolean generatesInterface() {
        return false;
    }

    /**
     * 
     * {@inheritDoc}
     * <p>
     * The files generated by the {@link EnumXmlAdapterBuilder} considers all files to be internal.
     */
    @Override
    public boolean isBuildingInternalArtifacts() {
        return true;
    }

    private XEnumType getEnumModelNode() {
        return getBuilderSet().getModelNode(getEnumType(), XEnumType.class);
    }
}
