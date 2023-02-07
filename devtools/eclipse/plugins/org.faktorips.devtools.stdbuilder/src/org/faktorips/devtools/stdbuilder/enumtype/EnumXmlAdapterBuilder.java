/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.enumtype;

import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.util.LocalizedStringsSet;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.builder.TypeSection;
import org.faktorips.devtools.model.builder.java.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.model.builder.java.JavaSourceFileBuilder;
import org.faktorips.devtools.model.builder.naming.DefaultJavaClassNameProvider;
import org.faktorips.devtools.model.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.model.builder.settings.JaxbSupportVariant;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xmodel.GeneratorConfig;
import org.faktorips.devtools.stdbuilder.xmodel.enumtype.XEnumType;
import org.faktorips.runtime.IRuntimeRepository;

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
    public void build(IIpsSrcFile ipsSrcFile) {
        if (JaxbSupportVariant.None == GeneratorConfig.forIpsSrcFile(ipsSrcFile).getJaxbSupport()) {
            return;
        }
        AFile javaFile = getJavaFile(ipsSrcFile);
        if (javaFile.exists()) {
            Charset charset = ipsSrcFile.getIpsProject().getProject().getDefaultCharset();
            String oldJavaFileContentsStr = getJavaFileContents(javaFile, charset);
            if (oldJavaFileContentsStr.contains('@' + JavaSourceFileBuilder.ANNOTATION_GENERATED)) {
                setMergeEnabled(true);
            }
        }
        super.build(ipsSrcFile);
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        if (ipsSrcFile.getIpsObjectType().equals(IpsObjectType.ENUM_TYPE) && ipsSrcFile.exists()) {
            IEnumType enumType = (IEnumType)ipsSrcFile.getIpsObject();
            return enumType.isExtensible() && !enumType.isAbstract();
        }
        return false;
    }

    @Override
    protected void generateCodeForJavatype() {
        TypeSection mainSection = getMainTypeSection();
        mainSection.getJavaDocForTypeBuilder()
                .javaDoc(getLocalizedText("CLASS_JAVADOC", getEnumType().getQualifiedName()),
                        JavaSourceFileBuilder.ANNOTATION_GENERATED);
        mainSection.setClass(true);
        mainSection.setEnum(false);
        mainSection.setClassModifier(Modifier.PUBLIC);
        mainSection.setUnqualifiedName(getUnqualifiedClassName(getEnumType().getIpsSrcFile()));
        IEnumAttribute idAttribute = getEnumType().findIdentiferAttribute(getIpsProject());
        if (idAttribute == null || !idAttribute.isValid(getIpsProject())) {
            return;
        }

        ValueDatatype idAttributeDatatype = idAttribute.findDatatype(getIpsProject());
        if (idAttributeDatatype.isPrimitive()) {
            idAttributeDatatype = idAttributeDatatype.getWrapperType();
        }

        DatatypeHelper datatypeHelper = getIpsProject().getDatatypeHelper(idAttributeDatatype);

        StringBuilder superClassName = new StringBuilder();
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
        builder.javaDoc(getLocalizedText("FIELD_REPOSITORY_JAVADOC"), JavaSourceFileBuilder.ANNOTATION_GENERATED);
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
    private void generateConstructor(JavaCodeFragmentBuilder builder) {
        builder.javaDoc(getLocalizedText("CONSTRUCTOR_JAVADOC"), JavaSourceFileBuilder.ANNOTATION_GENERATED);
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

        builder.method(Modifier.PUBLIC, datatypeHelper.getJavaClassName(), "marshal", new String[] { "value" }, //$NON-NLS-1$ //$NON-NLS-2$
                new String[] { getEnumModelNode().getQualifiedClassName() }, body,
                getLocalizedText("METHOD_MARSHAL_JAVADOC"), new String[] { JavaSourceFileBuilder.ANNOTATION_GENERATED },
                new String[] { JavaSourceFileBuilder.ANNOTATION_OVERRIDE });
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
     *      return repository.getExistingEnumValue(AnEnum.class, id);
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
        body.append("repository.getExistingEnumValue("); //$NON-NLS-1$
        body.appendClassName(getEnumModelNode().getQualifiedClassName());
        body.append(".class, "); //$NON-NLS-1$
        body.append("id);"); //$NON-NLS-1$

        String throwsJavadoc = "throws IllegalArgumentException "
                + getLocalizedText("METHOD_UNMARSHAL_JAVADOC_ILLEGALARGUMENTEXCEPTION");
        builder.method(Modifier.PUBLIC, getEnumModelNode().getQualifiedClassName(), "unmarshal", //$NON-NLS-1$
                new String[] { "id" }, new String[] { datatypeHelper.getJavaClassName() }, body,
                getLocalizedText("METHOD_UNMARSHAL_JAVADOC"),
                new String[] { throwsJavadoc, JavaSourceFileBuilder.ANNOTATION_GENERATED },
                new String[] { JavaSourceFileBuilder.ANNOTATION_OVERRIDE });

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
