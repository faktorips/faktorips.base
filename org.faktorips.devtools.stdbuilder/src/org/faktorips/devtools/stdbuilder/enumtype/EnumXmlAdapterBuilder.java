/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.enumtype;

import java.lang.reflect.Modifier;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.TypeSection;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
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

    public EnumTypeBuilder enumTypeBuilder;

    public EnumXmlAdapterBuilder(DefaultBuilderSet builderSet, EnumTypeBuilder enumTypeBuilder) {
        super(builderSet, new LocalizedStringsSet(EnumXmlAdapterBuilder.class));
        this.enumTypeBuilder = enumTypeBuilder;
    }

    @Override
    public boolean buildsDerivedArtefacts() {
        return true;
    }

    /** Returns the enum type for that code is being generated. */
    private IEnumType getEnumType() {
        return (IEnumType)getIpsObject();
    }

    @Override
    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        if (!((StandardBuilderSet)getBuilderSet()).isGenerateJaxbSupport()) {
            return;
        }
        super.build(ipsSrcFile);
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        if (ipsSrcFile.getIpsObjectType().equals(IpsObjectType.ENUM_TYPE) && ipsSrcFile.exists()) {
            IEnumType enumType = (IEnumType)ipsSrcFile.getIpsObject();
            return !enumType.isContainingValues() && !enumType.isAbstract();
        }
        return false;
    }

    @Override
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        return super.getUnqualifiedClassName(ipsSrcFile) + "XmlAdapter"; //$NON-NLS-1$
    }

    @Override
    public String getUnqualifiedClassName() throws CoreException {
        return super.getUnqualifiedClassName() + "XmlAdapter"; //$NON-NLS-1$
    }

    @Override
    protected void generateCodeForJavatype() throws CoreException {
        TypeSection mainSection = getMainTypeSection();
        mainSection.getJavaDocForTypeBuilder().javaDoc(
                getLocalizedText(getEnumType(), "CLASS_JAVADOC", getEnumType().getQualifiedName()));
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
        superClassName.append(enumTypeBuilder.getQualifiedClassName(getEnumType()));
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
        builder.javaDoc(getLocalizedText(getEnumType(), "FIELD_REPOSITORY_JAVADOC"));
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
        builder.javaDoc(getLocalizedText(getEnumType(), "CONSTRUCTOR_JAVADOC"));
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
    private void generateMethodMarshal(JavaCodeFragmentBuilder builder, DatatypeHelper datatypeHelper)
            throws CoreException {

        JavaCodeFragment body = new JavaCodeFragment();
        body.appendln("if(value == null)"); //$NON-NLS-1$
        body.appendOpenBracket();
        body.append("return null;"); //$NON-NLS-1$
        body.appendCloseBracket();
        body.append("return value."); //$NON-NLS-1$
        body.append(enumTypeBuilder.getMethodNameGetIdentifierAttribute(getEnumType(), getIpsProject()));
        body.append("();"); //$NON-NLS-1$

        builder.javaDoc(getLocalizedText(getEnumType(), "METHOD_MARSHAL_JAVADOC"));
        appendOverrideAnnotation(builder, false);
        builder.method(Modifier.PUBLIC, datatypeHelper.getJavaClassName(), "marshal", new String[] { "value" }, //$NON-NLS-1$ //$NON-NLS-2$
                new String[] { enumTypeBuilder.getQualifiedClassName(getEnumType()) }, body, null);
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
    private void generateMethodUnMarshal(JavaCodeFragmentBuilder builder, DatatypeHelper datatypeHelper)
            throws CoreException {

        JavaCodeFragment body = new JavaCodeFragment();
        body.appendln("if(id == null)"); //$NON-NLS-1$
        body.appendOpenBracket();
        body.append("return null;"); //$NON-NLS-1$
        body.appendCloseBracket();
        body.append("return "); //$NON-NLS-1$
        body.append("repository.getEnumValue("); //$NON-NLS-1$
        body.appendClassName(enumTypeBuilder.getQualifiedClassName(getEnumType()));
        body.append(".class, "); //$NON-NLS-1$
        body.append("id);"); //$NON-NLS-1$

        builder.javaDoc(getLocalizedText(getEnumType(), "METHOD_UNMARSHAL_JAVADOC"));
        appendOverrideAnnotation(builder, false);
        builder.method(Modifier.PUBLIC, enumTypeBuilder.getQualifiedClassName(getEnumType()), "unmarshal", //$NON-NLS-1$
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

}
