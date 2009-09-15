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

package org.faktorips.devtools.stdbuilder.policycmpttype;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
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
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenChangeableAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenConstantAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenDerivedAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.method.GenMethod;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.devtools.stdbuilder.productcmpttype.attribute.GenProdAttribute;
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
 * </p>
 * 
 * @author Peter Erzberger
 */
public class GenPolicyCmptType extends GenType {

    private final Map<IIpsObjectPart, GenPolicyCmptTypePart> generatorsByPart = new HashMap<IIpsObjectPart, GenPolicyCmptTypePart>();
    private final List<GenAttribute> genAttributes = new ArrayList<GenAttribute>();
    private final List<GenAssociation> genAssociations = new ArrayList<GenAssociation>();
    private final List<GenValidationRule> genValidationRules = new ArrayList<GenValidationRule>();
    private final List<GenMethod> genMethods = new ArrayList<GenMethod>();
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
        IMethod[] methods = getPolicyCmptType().getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].isValid()) {
                GenMethod generator = new GenMethod(this, methods[i]);
                genMethods.add(generator);
                generatorsByPart.put(methods[i], generator);
            }
        }
    }

    private void createGeneratorsForValidationRules() throws CoreException {
        IValidationRule[] validationRules = getPolicyCmptType().getRules();
        for (int i = 0; i < validationRules.length; i++) {
            if (validationRules[i].isValid()) {
                GenValidationRule generator = new GenValidationRule(this, validationRules[i]);
                genValidationRules.add(generator);
                generatorsByPart.put(validationRules[i], generator);
            }
        }
    }

    private void createGeneratorsForAttributes() throws CoreException {
        IPolicyCmptTypeAttribute[] attrs = getPolicyCmptType().getPolicyCmptTypeAttributes();
        for (int i = 0; i < attrs.length; i++) {
            if (attrs[i].isValid()) {
                GenAttribute generator = createGenerator(attrs[i]);
                genAttributes.add(generator);
                generatorsByPart.put(attrs[i], generator);
            }
        }
    }

    private void createGeneratorsForAssociations() throws CoreException {
        IPolicyCmptType type = getPolicyCmptType();
        IPolicyCmptTypeAssociation[] ass = type.getPolicyCmptTypeAssociations();
        for (int i = 0; i < ass.length; i++) {
            if (ass[i].isValid()) {
                GenAssociation generator = createGenerator(ass[i]);
                genAssociations.add(generator);
                generatorsByPart.put(ass[i], generator);
            }
        }
    }

    public List<GenAttribute> getGenAttributes() {
        return genAttributes;
    }

    private GenAttribute createGenerator(IPolicyCmptTypeAttribute a) throws CoreException {
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

    public GenMethod getGenerator(IMethod method) throws CoreException {
        GenMethod generator = (GenMethod)generatorsByPart.get(method);
        if (generator == null && method.isValid()) {
            generator = new GenMethod(this, method);
            genMethods.add(generator);
            generatorsByPart.put(method, generator);
        }
        return generator;
    }

    public GenAttribute getGenerator(IPolicyCmptTypeAttribute a) throws CoreException {
        GenAttribute generator = (GenAttribute)generatorsByPart.get(a);
        if (generator == null && a.isValid()) {
            generator = createGenerator(a);
            genAttributes.add(generator);
            generatorsByPart.put(a, generator);
        }
        return generator;
    }

    public GenValidationRule getGenerator(IValidationRule a) {
        return (GenValidationRule)generatorsByPart.get(a);
    }

    public GenAssociation getGenerator(IPolicyCmptTypeAssociation a) throws CoreException {
        GenAssociation generator = (GenAssociation)generatorsByPart.get(a);
        if (null == generator && a.isValid()) {
            generator = createGenerator(a);
            genAssociations.add(generator);
            generatorsByPart.put(a, generator);
        }
        return generator;
    }

    /**
     * Returns the unqualified name for Java class generated by this builder for the given ips
     * source file.
     * 
     * @param ipsSrcFile the ips source file
     * @return the qualified class name
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

    private boolean isGenerateChangeListenerSupport() {
        return getBuilderSet().getConfig().getPropertyValueAsBoolean(
                StandardBuilderSet.CONFIG_PROPERTY_GENERATE_CHANGELISTENER).booleanValue();
    }

    protected GenProdAttribute getGenerator(IProductCmptTypeAttribute a) throws CoreException {
        return getBuilderSet().getGenerator(getProductCmptType()).getGenerator(a);
    }

    public IProductCmptType getProductCmptType() throws CoreException {
        return getPolicyCmptType().findProductCmptType(getPolicyCmptType().getIpsProject());
    }

    public GenProductCmptType getGenProductCmptType() throws CoreException {
        IIpsProject ipsProject = getBuilderSet().getIpsProject();
        return getBuilderSet().getGenerator(getPolicyCmptType().findProductCmptType(ipsProject));
    }

    public String getPolicyCmptTypeName() throws CoreException {
        return StringUtils.capitalize(getPolicyCmptType().getName());
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public IPolicy createPolicy()
     * </pre>
     */
    public void generateSignatureCreatePolicyCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String returnType = getQualifiedName(true);
        String methodName = getMethodNameCreatePolicyCmpt();
        methodsBuilder.signature(Modifier.PUBLIC, returnType, methodName, new String[0], new String[0]);
    }

    /**
     * Returns the method name to create the concrete policy component class, e.g.
     * createMotorPolicy.
     */
    public String getMethodNameCreatePolicyCmpt() throws CoreException {
        String policyCmptConceptName = getPolicyCmptTypeName();
        return getLocalizedText("METHOD_CREATE_POLICY_CMPT_NAME", policyCmptConceptName);
    }

    public void generateChangeListenerConstants(JavaCodeFragmentBuilder builder) {
        if (isGenerateChangeListenerSupport()) {
            changeListenerSupportBuilder.generateChangeListenerConstants(builder);
        }
    }

    public void generateChangeListenerMethods(JavaCodeFragmentBuilder methodsBuilder,
            String parentModelObjectName,
            boolean generateParentNotification) {
        if (isGenerateChangeListenerSupport()) {
            changeListenerSupportBuilder.generateChangeListenerMethods(methodsBuilder, parentModelObjectName,
                    generateParentNotification);
        }
    }

    public String getNotificationSupportInterfaceName() {
        if (isGenerateChangeListenerSupport()) {
            return changeListenerSupportBuilder.getNotificationSupportInterfaceName();
        }
        return null;
    }

}
