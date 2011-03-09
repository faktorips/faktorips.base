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

package org.faktorips.devtools.stdbuilder.policycmpttype;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.changelistener.BeanChangeListenerSupportBuilder;
import org.faktorips.devtools.stdbuilder.changelistener.ChangeEventType;
import org.faktorips.devtools.stdbuilder.changelistener.IChangeListenerSupportBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.association.GenAssociation;
import org.faktorips.devtools.stdbuilder.policycmpttype.association.GenAssociationTo1;
import org.faktorips.devtools.stdbuilder.policycmpttype.association.GenAssociationToMany;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenChangeableAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenConstantAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenDerivedAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.method.GenPolicyCmptTypeMethod;
import org.faktorips.devtools.stdbuilder.productcmpttype.BaseProductCmptTypeBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.devtools.stdbuilder.productcmpttype.attribute.GenProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.type.GenType;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

/**
 * A generator for <code>IPolicyCmptType</code>s.
 * 
 * <p>
 * It provides access to generators for attributes, methods and associations of the policy component
 * type. Typically, when the generator is created, all the generators of its parts are also created,
 * except the ones in the super type hierarchy. These are created on demand since it is expected
 * that only a few of them will be overridden. It is necessary to provide an own generator instance
 * for those overridden parts in this generator and not to delegate to the generator of the super
 * class since otherwise it would not be possible to determine if code has to be generated with
 * respect to the super type.
 * 
 * @author Peter Erzberger
 */
public class GenPolicyCmptType extends GenType {

    private final List<GenPolicyCmptTypeAttribute> genPolicyCmptTypeAttributes = new ArrayList<GenPolicyCmptTypeAttribute>();

    private final List<GenAssociation> genAssociations = new ArrayList<GenAssociation>();

    private final List<GenValidationRule> genValidationRules = new ArrayList<GenValidationRule>();

    private final List<GenPolicyCmptTypeMethod> genPolicyCmptTypeMethods = new ArrayList<GenPolicyCmptTypeMethod>();

    private final IChangeListenerSupportBuilder changeListenerSupportBuilder;

    public GenPolicyCmptType(IPolicyCmptType policyCmptType, StandardBuilderSet builderSet) throws CoreException {
        super(policyCmptType, builderSet, new LocalizedStringsSet(GenPolicyCmptType.class));
        ArgumentCheck.notNull(policyCmptType, this);
        ArgumentCheck.notNull(builderSet, this);

        createGeneratorsForAttributes();
        createGeneratorsForMethods();
        createGeneratorsForValidationRules();
        createGeneratorsForAssociations();
        changeListenerSupportBuilder = new BeanChangeListenerSupportBuilder(this);
    }

    public IPolicyCmptType getPolicyCmptType() {
        return (IPolicyCmptType)getType();
    }

    private void createGeneratorsForMethods() throws CoreException {
        List<IMethod> methods = getPolicyCmptType().getMethods();
        for (IMethod method : methods) {
            if (method.isValid(getPolicyCmptType().getIpsProject())) {
                GenPolicyCmptTypeMethod generator = new GenPolicyCmptTypeMethod(this, method);
                genPolicyCmptTypeMethods.add(generator);
                getGeneratorsByPart().put(method, generator);
            }
        }
    }

    private void createGeneratorsForValidationRules() throws CoreException {
        List<IValidationRule> validationRules = getPolicyCmptType().getRules();
        for (IValidationRule validationRule : validationRules) {
            if (validationRule.isValid(getPolicyCmptType().getIpsProject())) {
                GenValidationRule generator = new GenValidationRule(this, validationRule);
                genValidationRules.add(generator);
                getGeneratorsByPart().put(validationRule, generator);
            }
        }
    }

    private void createGeneratorsForAttributes() throws CoreException {
        List<IPolicyCmptTypeAttribute> attrs = getPolicyCmptType().getPolicyCmptTypeAttributes();
        for (IPolicyCmptTypeAttribute attr : attrs) {
            if (attr.isValid(getPolicyCmptType().getIpsProject())) {
                GenPolicyCmptTypeAttribute generator = createGenerator(attr);
                genPolicyCmptTypeAttributes.add(generator);
                getGeneratorsByPart().put(attr, generator);
            }
        }
    }

    private void createGeneratorsForAssociations() throws CoreException {
        IPolicyCmptType type = getPolicyCmptType();
        List<IPolicyCmptTypeAssociation> ass = type.getPolicyCmptTypeAssociations();
        for (IPolicyCmptTypeAssociation as : ass) {
            if (as.isValid(getPolicyCmptType().getIpsProject())) {
                GenAssociation generator = createGenerator(as);
                genAssociations.add(generator);
                getGeneratorsByPart().put(as, generator);
            }
        }
    }

    public List<GenPolicyCmptTypeAttribute> getGenAttributes() {
        return genPolicyCmptTypeAttributes;
    }

    private GenPolicyCmptTypeAttribute createGenerator(IPolicyCmptTypeAttribute a) throws CoreException {
        if (a.isDerived()) {
            return new GenDerivedAttribute(this, a);
        }
        if (a.isChangeable()) {
            return new GenChangeableAttribute(this, a);
        }
        return new GenConstantAttribute(this, a);
    }

    private GenAssociation createGenerator(IPolicyCmptTypeAssociation association) throws CoreException {
        if (association.is1ToMany()) {
            return new GenAssociationToMany(this, association);
        }
        return new GenAssociationTo1(this, association);
    }

    public GenPolicyCmptTypeMethod getGenerator(IMethod method) throws CoreException {
        GenPolicyCmptTypeMethod generator = (GenPolicyCmptTypeMethod)getGeneratorsByPart().get(method);
        if (generator == null && method.isValid(getPolicyCmptType().getIpsProject())) {
            generator = new GenPolicyCmptTypeMethod(this, method);
            genPolicyCmptTypeMethods.add(generator);
            getGeneratorsByPart().put(method, generator);
        }
        return generator;
    }

    public GenPolicyCmptTypeAttribute getGenerator(IPolicyCmptTypeAttribute a) throws CoreException {
        GenPolicyCmptTypeAttribute generator = (GenPolicyCmptTypeAttribute)getGeneratorsByPart().get(a);
        if (generator == null && a.isValid(getPolicyCmptType().getIpsProject())) {
            generator = createGenerator(a);
            genPolicyCmptTypeAttributes.add(generator);
            getGeneratorsByPart().put(a, generator);
        }
        return generator;
    }

    public GenValidationRule getGenerator(IValidationRule a) {
        return (GenValidationRule)getGeneratorsByPart().get(a);
    }

    public GenAssociation getGenerator(IPolicyCmptTypeAssociation a) throws CoreException {
        GenAssociation generator = (GenAssociation)getGeneratorsByPart().get(a);
        if (null == generator && a.isValid(getPolicyCmptType().getIpsProject())) {
            generator = createGenerator(a);
            genAssociations.add(generator);
            getGeneratorsByPart().put(a, generator);
        }
        return generator;
    }

    /**
     * Returns the unqualified name for Java class generated by this builder for the given ips
     * source file.
     * 
     * @throws CoreException is delegated from calls to other methods
     */
    @Override
    public String getUnqualifiedClassName(boolean forInterface) throws CoreException {
        if (forInterface) {
            return getBuilderSet().getJavaNamingConvention().getPublishedInterfaceName(getPolicyCmptType().getName());
        }
        return StringUtil.getFilenameWithoutExtension(getPolicyCmptType().getName());
    }

    /**
     * Returns the method name to initialize the policy component with the default data from the
     * product component.
     */
    public String getMethodNameInitialize() {
        return "initialize";
    }

    public void generateChangeListenerSupportBeforeChange(JavaCodeFragmentBuilder methodsBuilder,
            ChangeEventType eventType,
            String fieldType,
            String fieldName,
            String paramName,
            String fieldNameConstant) {
        if (isGenerateChangeListenerSupport()) {
            changeListenerSupportBuilder.generateChangeListenerSupportBeforeChange(methodsBuilder, eventType,
                    fieldType, fieldName, paramName, fieldNameConstant);
        }
    }

    public void generateChangeListenerSupportAfterChange(JavaCodeFragmentBuilder methodsBuilder,
            ChangeEventType eventType,
            String fieldType,
            String fieldName,
            String paramName,
            String fieldNameConstant) {
        if (isGenerateChangeListenerSupport()) {
            changeListenerSupportBuilder.generateChangeListenerSupportAfterChange(methodsBuilder, eventType, fieldType,
                    fieldName, paramName, fieldNameConstant);
        }
    }

    public boolean isGenerateChangeListenerSupport() {
        return getBuilderSet().getConfig()
                .getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_CHANGELISTENER).booleanValue();
    }

    public GenProductCmptTypeAttribute getGenerator(IProductCmptTypeAttribute a) throws CoreException {
        return getBuilderSet().getGenerator(findProductCmptType()).getGenerator(a);
    }

    public IProductCmptType findProductCmptType() throws CoreException {
        return getPolicyCmptType().findProductCmptType(getPolicyCmptType().getIpsProject());
    }

    public GenProductCmptType getGenProductCmptType() throws CoreException {
        return getBuilderSet().getGenerator(findProductCmptType());
    }

    public String getPolicyCmptTypeName() {
        return StringUtils.capitalize(getPolicyCmptType().getName());
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public IPolicy createPolicy()
     * </pre>
     */
    public void generateSignatureCreatePolicyCmpt(JavaCodeFragmentBuilder methodsBuilder) {
        String returnType = getQualifiedName(true);
        String methodName = getMethodNameCreatePolicyCmpt();
        methodsBuilder.signature(Modifier.PUBLIC, returnType, methodName, new String[0], new String[0]);
    }

    /**
     * Returns the method name to create the concrete policy component class, e.g.
     * createMotorPolicy.
     */
    public String getMethodNameCreatePolicyCmpt() {
        String policyCmptConceptName = getPolicyCmptTypeName();
        return getLocalizedText("METHOD_CREATE_POLICY_CMPT_NAME", policyCmptConceptName);
    }

    public void generateChangeListenerConstants(JavaCodeFragmentBuilder builder) {
        if (isGenerateChangeListenerSupport()) {
            changeListenerSupportBuilder.generateChangeListenerConstants(builder);
        }
    }

    public void generateChangeListenerMethods(JavaCodeFragmentBuilder methodsBuilder,
            List<IPolicyCmptTypeAssociation> detailToMasterAssociations,
            boolean createPropertyChangeListenerMethods) throws CoreException {
        if (isGenerateChangeListenerSupport()) {
            String[] parentObjectFieldNames = new String[detailToMasterAssociations.size()];
            for (int i = 0; i < detailToMasterAssociations.size(); i++) {
                String parentObjectFieldName = getGenerator(detailToMasterAssociations.get(i))
                        .getFieldNameForAssociation();
                parentObjectFieldNames[i] = parentObjectFieldName;
            }
            changeListenerSupportBuilder.generateChangeListenerMethods(methodsBuilder, parentObjectFieldNames,
                    createPropertyChangeListenerMethods);
        }
    }

    public String getNotificationSupportInterfaceName() {
        if (isGenerateChangeListenerSupport()) {
            return changeListenerSupportBuilder.getNotificationSupportInterfaceName();
        }
        return null;
    }

    @Override
    protected void getGeneratedJavaElementsForType(List<IJavaElement> javaElements,
            IType generatedJavaType,
            boolean forInterface) {

        if (!(getPolicyCmptType().isAbstract()) && getPolicyCmptType().isConfigurableByProductCmptType()) {
            try {
                IType javaTypeProductCmpt = findGeneratedJavaTypeForProductCmptType(forInterface);
                if (javaTypeProductCmpt != null) {
                    org.eclipse.jdt.core.IMethod createPolicyCmptMethod = javaTypeProductCmpt.getMethod(
                            getMethodNameCreatePolicyCmpt(), new String[] {});
                    javaElements.add(createPolicyCmptMethod);
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Searches and returns the Java type generated for the <tt>IProductCmptType</tt> configuring
     * the <tt>IPolicyCmptType</tt> this generator is configured for.
     * <p>
     * Returns <tt>null</tt> if the <tt>IProductCmptType</tt> cannot be found.
     * 
     * @param forInterface Flag indicating whether to search for the published interface of the
     *            <tt>IProductCmptType</tt> (<tt>true</tt>) or for it's implementation (
     *            <tt>false</tt>).
     * 
     * @throws CoreException If an error occurs while searching for the <tt>IProductCmptType</tt>.
     */
    public IType findGeneratedJavaTypeForProductCmptType(boolean forInterface) throws CoreException {
        BaseProductCmptTypeBuilder productCmptTypeBuilder = forInterface ? getBuilderSet()
                .getProductCmptInterfaceBuilder() : getBuilderSet().getProductCmptImplClassBuilder();

        IProductCmptType productCmptType = getPolicyCmptType().findProductCmptType(getPolicyCmptType().getIpsProject());
        if (productCmptType == null) {
            return null;
        }
        return productCmptTypeBuilder.getGeneratedJavaTypes(productCmptType).get(0);
    }

}
