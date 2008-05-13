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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.AbstractPcTypeBuilder;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.policycmpttype.association.GenAssociation;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.message.MessageList;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class BasePolicyCmptTypeBuilder extends AbstractPcTypeBuilder {

    private Map generatorsByPart = new HashMap();
    private List genAttributes = new ArrayList();
    private List genProdAttributes = new ArrayList();
    private List genAssociations = new ArrayList();

    private boolean generateChangeListenerSupport;

    public BasePolicyCmptTypeBuilder(IIpsArtefactBuilderSet builderSet, String kindId, LocalizedStringsSet stringsSet,
            boolean generateChangeListenerSupport) throws CoreException {
        super(builderSet, kindId, stringsSet);
        this.generateChangeListenerSupport = generateChangeListenerSupport;
    }

    /**
     * {@inheritDoc}
     */
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException {
        super.beforeBuild(ipsSrcFile, status);
        initPartGenerators();
    }

    private void initPartGenerators() throws CoreException {
        genAttributes.clear();
        genProdAttributes.clear();
        genAssociations.clear();
        generatorsByPart.clear();
    }

    public boolean isGenerateChangeListenerSupport() {
        return generateChangeListenerSupport;
    }

    /**
     * This validation is necessary because otherwise a java class file is created with a wrong java
     * class name this causes jmerge to throw an exception
     */
    protected boolean hasValidProductCmptTypeName() throws CoreException {
        IProductCmptType type = getProductCmptType();
        MessageList msgList = type.validate(getIpsProject());
        return !msgList.getMessagesFor(type, IProductCmptType.PROPERTY_NAME).containsErrorMsg();
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForAttribute(IPolicyCmptTypeAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder constantBuilder,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        if (attribute.isProductRelevant() && getProductCmptType() == null) {
            return;
        }
        GenAttribute generator = ((StandardBuilderSet)getBuilderSet()).getGenerator(getPcType())
                .getGenerator(attribute);
        if (generator != null) {
            generator.generate(generatesInterface(), getIpsProject(), getMainTypeSection());
        }
    }

    protected void generateCodeForMethodDefinedInModel(IMethod method, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {

        GenMethod generator = ((StandardBuilderSet)getBuilderSet()).getGenerator(getPcType()).getGenerator(method);
        if (generator != null) {
            generator.generate(generatesInterface(), getIpsProject(), getMainTypeSection());
        }
    }

    protected void generateCodeForValidationRule(IValidationRule validationRule) throws CoreException {
        GenValidationRule generator = ((StandardBuilderSet)getBuilderSet()).getGenerator(getPcType()).getGenerator(
                validationRule);
        if (generator != null) {
            generator.generate(generatesInterface(), getIpsProject(), getMainTypeSection());
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForAssociation(IPolicyCmptTypeAssociation association,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        GenAssociation generator = ((StandardBuilderSet)getBuilderSet()).getGenerator(getPcType()).getGenerator(
                association);
        generator.generate(generatesInterface(), getIpsProject(), getMainTypeSection());
    }

    boolean isFirstDependantTypeInHierarchy(IPolicyCmptType type) throws CoreException {
        if (!type.isDependantType()) {
            return false;
        }
        IPolicyCmptType supertype = (IPolicyCmptType)type.findSupertype(getIpsProject());
        if (supertype == null) {
            return true;
        }
        return !supertype.isDependantType();
    }

    protected boolean isUseTypesafeCollections() {
        return ((StandardBuilderSet)getBuilderSet()).isUseTypesafeCollections();
    }

    /**
     * Returns the GenProductCmptType for this builder.
     */
    public GenProductCmptType getGenProductCmptType() throws CoreException {
        return ((StandardBuilderSet)getBuilderSet()).getGenerator(getProductCmptType());
    }
}
