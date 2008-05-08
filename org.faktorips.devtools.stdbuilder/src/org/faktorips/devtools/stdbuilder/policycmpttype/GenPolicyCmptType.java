/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.policycmpttype.association.GenAssociation;
import org.faktorips.devtools.stdbuilder.policycmpttype.association.GenAssociationTo1;
import org.faktorips.devtools.stdbuilder.policycmpttype.association.GenAssociationToMany;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenChangeableAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenConstantAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenDerivedAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.attribute.GenProdAttribute;
import org.faktorips.devtools.stdbuilder.type.GenType;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.runtime.internal.ModelObjectChangedEvent;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

public class GenPolicyCmptType extends GenType {

    private Map generatorsByPart = new HashMap();
    private List genAttributes = new ArrayList();
    private List genAssociations = new ArrayList();
    private List genValidationRules = new ArrayList();
    private List genMethods = new ArrayList();

    /**
     * @param policyCmptType
     * @param builder
     * @throws CoreException
     */
    public GenPolicyCmptType(IPolicyCmptType policyCmptType, StandardBuilderSet builderSet,
            LocalizedStringsSet stringsSet) throws CoreException {
        super(policyCmptType, builderSet, stringsSet);
        ArgumentCheck.notNull(policyCmptType, this);
        ArgumentCheck.notNull(builderSet, this);

        createGeneratorsForAttributes();
        createGeneratorsForMethods();
        createGeneratorsForValidationRules();
        createGeneratorsForAssociations();
    }

    public IPolicyCmptType getPolicyCmptType() {
        return (IPolicyCmptType)getType();
    }

    private void createGeneratorsForMethods() throws CoreException {
        LocalizedStringsSet stringsSet = new LocalizedStringsSet(GenAttribute.class);
        IMethod[] methods = getPolicyCmptType().getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].isValid()) {
                GenMethod generator = new GenMethod(this, methods[i], stringsSet);
                if (generator != null) {
                    genMethods.add(generator);
                    generatorsByPart.put(methods[i], generator);
                }
            }
        }
    }

    private void createGeneratorsForValidationRules() throws CoreException {
        LocalizedStringsSet stringsSet = new LocalizedStringsSet(GenValidationRule.class);
        IValidationRule[] validationRules = getPolicyCmptType().getRules();
        for (int i = 0; i < validationRules.length; i++) {
            if (validationRules[i].isValid()) {
                GenValidationRule generator = new GenValidationRule(this, validationRules[i], stringsSet);
                if (generator != null) {
                    genValidationRules.add(generator);
                    generatorsByPart.put(validationRules[i], generator);
                }
            }
        }
    }

    private void createGeneratorsForAttributes() throws CoreException {
        LocalizedStringsSet stringsSet = new LocalizedStringsSet(GenAttribute.class);
        IPolicyCmptTypeAttribute[] attrs = getPolicyCmptType().getPolicyCmptTypeAttributes();
        for (int i = 0; i < attrs.length; i++) {
            if (attrs[i].isValid()) {
                GenAttribute generator = createGenerator(attrs[i], stringsSet);
                if (generator != null) {
                    genAttributes.add(generator);
                    generatorsByPart.put(attrs[i], generator);
                }
            }
        }
    }

    public Iterator getGenAttributes() {
        return genAttributes.iterator();
    }

    private GenAttribute createGenerator(IPolicyCmptTypeAttribute a, LocalizedStringsSet stringsSet)
            throws CoreException {
        if (a.isDerived()) {
            return new GenDerivedAttribute(this, a, stringsSet);
        }
        if (a.isChangeable()) {
            return new GenChangeableAttribute(this, a, stringsSet);
        }
        return new GenConstantAttribute(this, a, stringsSet);
    }

    private void createGeneratorsForAssociations() throws CoreException {
        LocalizedStringsSet stringsSet = new LocalizedStringsSet(GenAssociation.class);
        IPolicyCmptType type = getPolicyCmptType();
        IPolicyCmptTypeAssociation[] ass = type.getPolicyCmptTypeAssociations();
        for (int i = 0; i < ass.length; i++) {
            if (ass[i].isValid()) {
                GenAssociation generator = createGenerator(ass[i], stringsSet);
                if (generator != null) {
                    genAssociations.add(generator);
                    generatorsByPart.put(ass[i], generator);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    private GenAssociation createGenerator(IPolicyCmptTypeAssociation association, LocalizedStringsSet stringsSet)
            throws CoreException {
        if (association.is1ToMany()) {
            return new GenAssociationToMany(this, association, stringsSet);
        }
        return new GenAssociationTo1(this, association, stringsSet);
    }

    public GenMethod getGenerator(IMethod a) {
        return (GenMethod)generatorsByPart.get(a);
    }

    public GenAttribute getGenerator(IPolicyCmptTypeAttribute a) throws CoreException {
        GenAttribute generator = (GenAttribute)generatorsByPart.get(a);
        if (generator != null) {
            return generator;
        }
        // if the attributes policy component type is not this type but one in the super type
        // hierarchy of this type
        if (!a.getPolicyCmptType().equals(getPolicyCmptType())) {
            GenPolicyCmptType superTypeGenerator = getBuilderSet().getGenerator(a.getPolicyCmptType());
            return superTypeGenerator.getGenerator(a);

        }
        return null;
    }

    public GenValidationRule getGenerator(IValidationRule a) {
        return (GenValidationRule)generatorsByPart.get(a);
    }

    public GenAssociation getGenerator(IPolicyCmptTypeAssociation a) {
        GenAssociation generator = (GenAssociation)generatorsByPart.get(a);
        if (null == generator) {
            try {
                generator = createGenerator(a, new LocalizedStringsSet(GenAssociation.class));
            } catch (CoreException e) {
                return null;
            }
            if (generator != null) {
                genAssociations.add(generator);
                generatorsByPart.put(a, generator);
            }
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

    public void generateChangeListenerSupport(JavaCodeFragmentBuilder methodsBuilder,
            String eventClassName,
            String eventConstant,
            String fieldName,
            String paramName) {
        if (isGenerateChangeListenerSupport()) {
            methodsBuilder.appendln("if (" + MethodNames.EXISTS_CHANGE_LISTENER_TO_BE_INFORMED + "()) {");
            methodsBuilder.append(MethodNames.NOTIFIY_CHANGE_LISTENERS + "(new ");
            methodsBuilder.appendClassName(ModelObjectChangedEvent.class);
            methodsBuilder.append("(this, ");
            methodsBuilder.appendClassName(eventClassName);
            methodsBuilder.append('.');
            methodsBuilder.append(eventConstant);
            methodsBuilder.append(", ");
            methodsBuilder.appendQuoted(fieldName);
            if (paramName != null) {
                methodsBuilder.append(", ");
                methodsBuilder.append(paramName);
            }
            methodsBuilder.appendln("));");
            methodsBuilder.appendln("}");
        }
    }

    private boolean isGenerateChangeListenerSupport() {
        return getBuilderSet().isGenerateChangeListener();
    }

    protected GenProdAttribute getGenerator(IProductCmptTypeAttribute a) throws CoreException {
        return getBuilderSet().getGenerator(getProductCmptType()).getGenerator(a);
    }

    public IProductCmptType getProductCmptType() throws CoreException {
        return getPolicyCmptType().findProductCmptType(getPolicyCmptType().getIpsProject());
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
    public void generateSignatureCreatePolicyCmpt(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
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

}
