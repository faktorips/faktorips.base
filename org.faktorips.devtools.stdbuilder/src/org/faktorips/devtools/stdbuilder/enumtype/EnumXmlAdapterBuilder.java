/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.enumtype;

import java.lang.reflect.Modifier;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.TypeSection;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.util.LocalizedStringsSet;

/**
 * A builder for JAXB XmlAdapters. XmlAdapters are generated for Faktor-IPS enumerations that defer their content
 * to a Faktor-IPS enumeration content. These contents can only be accessed through the {@link IRuntimeRepository}.
 * This is the responsibility of the generated XmlAdapter.
 * 
 * @author Peter Kuntz
 */
public class EnumXmlAdapterBuilder extends DefaultJavaSourceFileBuilder {

    /** The package id identifiying the builder */
    public final static String PACKAGE_STRUCTURE_KIND_ID = "EnumXmlAdapterBuilder.enumtype.stdbuilder.devtools.faktorips.org"; //$NON-NLS-1$

    public EnumTypeBuilder enumTypeBuilder;
    
    public EnumXmlAdapterBuilder(IIpsArtefactBuilderSet builderSet, EnumTypeBuilder enumTypeBuilder) {
        super(builderSet, PACKAGE_STRUCTURE_KIND_ID, new LocalizedStringsSet(EnumXmlAdapterBuilder.class));
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

    /**
     * {@inheritDoc}
     */
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
    protected void generateCodeForJavatype() throws CoreException {
        TypeSection mainSection = getMainTypeSection();
        appendLocalizedJavaDoc("CLASS", getUnqualifiedClassName(), getEnumType(), mainSection.getJavaDocForTypeBuilder());
        mainSection.setClass(true);
        mainSection.setEnum(false);
        mainSection.setClassModifier(Modifier.PUBLIC);
        mainSection.setUnqualifiedName(getUnqualifiedClassName(getEnumType().getIpsSrcFile()));
        IEnumAttribute idAttribute = getEnumType().findIsIdentiferAttribute(getIpsProject());
        if(idAttribute == null || !idAttribute.isValid()){
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
    private void generateFieldRepository(JavaCodeFragmentBuilder builder){
        appendLocalizedJavaDoc("FIELD_REPOSITORY", getEnumType(), builder);
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
        appendLocalizedJavaDoc("CONSTRUCTOR", getEnumType(), builder);
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
        body.append(enumTypeBuilder.getMethodNameOfIdentifierAttribute(getEnumType(), getIpsProject()));
        body.append("();"); //$NON-NLS-1$
        
        
        appendLocalizedJavaDoc("METHOD_MARSHAL", getEnumType(), builder);
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

        appendLocalizedJavaDoc("METHOD_UNMARSHAL", getEnumType(), builder);
        builder.method(Modifier.PUBLIC, enumTypeBuilder.getQualifiedClassName(getEnumType()), "unmarshal", //$NON-NLS-1$
                new String[] { "id" }, new String[] { datatypeHelper.getJavaClassName() }, body, null); //$NON-NLS-1$

    }
}
