/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.type;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Abstract base class for the generators for <tt>IPolicyCmptTypeAttribute</tt>s and
 * <tt>IProductCmptTypeAttribute</tt>s.
 * 
 * @author Alexander Weickmann
 */
public abstract class GenAttribute extends GenTypePart {

    private final IAttribute attribute;

    private final DatatypeHelper datatypeHelper;

    protected GenAttribute(GenType genType, IAttribute attribute, LocalizedStringsSet localizedStringSet) {
        super(genType, attribute, localizedStringSet);
        this.attribute = attribute;
        try {
            datatypeHelper = attribute.getIpsProject().findDatatypeHelper(attribute.getDatatype());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
        if (datatypeHelper == null) {
            throw new NullPointerException("No datatype helper found for " + attribute);
        }
    }

    public IAttribute getAttribute() {
        return attribute;
    }

    public final DatatypeHelper getDatatypeHelper() {
        return datatypeHelper;
    }

    public final String getMemberVarName() {
        return getJavaNamingConvention().getMemberVarName(attribute.getName());
    }

    public final ValueDatatype getDatatype() {
        return (ValueDatatype)getDatatypeHelper().getDatatype();
    }

    public final String getJavaClassName() {
        return getDatatypeHelper().getJavaClassName();
    }

    public final boolean isPublished() {
        return getAttribute().getModifier().isPublished();
    }

    public final boolean isOverwrite() {
        return getAttribute().isOverwrite();
    }

    public final String getGetterMethodName() {
        return getJavaNamingConvention().getGetterMethodName(getAttribute().getName(), getDatatype());
    }

    public final String getSetterMethodName() {
        return getJavaNamingConvention().getSetterMethodName(getAttribute().getName(), getDatatype());
    }

    /**
     * Convenience method that can be used by subclasses to add the setter method to the generated
     * Java elements.
     * 
     * @param javaElements The list to add the setter method to.
     * @param generatedJavaType The generated Java type to which the method belongs.
     * 
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    protected final void addSetterMethodToGeneratedJavaElements(List<IJavaElement> javaElements, IType generatedJavaType) {
        ArgumentCheck.notNull(new Object[] { javaElements, generatedJavaType });
        IMethod setterMethod = generatedJavaType.getMethod(getSetterMethodName(),
                new String[] { getJavaTypeSignature(getDatatype(), false) });
        javaElements.add(setterMethod);
    }

    /**
     * Convenience method that can be used by subclasses to add the getter method to the generated
     * Java elements.
     * 
     * @param javaElements The list to add the getter method to.
     * @param generatedJavaType The generated Java type to which the method belongs.
     * 
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    protected final void addGetterMethodToGeneratedJavaElements(List<IJavaElement> javaElements, IType generatedJavaType) {
        ArgumentCheck.notNull(new Object[] { javaElements, generatedJavaType });
        IMethod getterMethod = generatedJavaType.getMethod(getGetterMethodName(), new String[] {});
        javaElements.add(getterMethod);
    }

    /**
     * Convenience method that can be used by subclasses to add the member variable to the generated
     * Java elements.
     * 
     * @param javaElements The list to add the member variable to.
     * @param generatedJavaType The generated Java type to which the member variable belongs.
     * 
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    protected final void addMemberVarToGeneratedJavaElements(List<IJavaElement> javaElements, IType generatedJavaType) {
        ArgumentCheck.notNull(new Object[] { javaElements, generatedJavaType });
        IField memberVar = generatedJavaType.getField(getMemberVarName());
        javaElements.add(memberVar);
    }

    /**
     * Convenience method that can be used by subclasses to add the attribute name constant to the
     * generated Java elements.
     * 
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public final static String PROPERTY_PREMIUM = &quot;premium&quot;;
     * </pre>
     */
    protected void generateAttributeNameConstant(JavaCodeFragmentBuilder builder) {
        if (getAttribute().isOverwrite()) {
            return;
        }
        appendLocalizedJavaDoc("FIELD_PROPERTY_NAME", getAttribute().getName(), builder);
        builder.append("public final static ");
        builder.appendClassName(String.class);
        builder.append(' ');
        builder.append(getStaticConstantPropertyName());
        builder.append(" = ");
        builder.appendQuoted(getAttribute().getName());
        builder.appendln(";");
    }

    public String getStaticConstantPropertyName() {
        return getLocalizedText("FIELD_PROPERTY_NAME", StringUtils.upperCase(getAttribute().getName()));
    }
}
