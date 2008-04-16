/***************************************************************************************************
 *  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.  *  * Alle Rechte vorbehalten.  *  *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,  * Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der  * Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community)  * genutzt werden, die Bestandteil der Auslieferung ist und auch
 * unter  *   http://www.faktorips.org/legal/cl-v01.html  * eingesehen werden kann.  *  *
 * Mitwirkende:  *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  *  
 **************************************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.AbstractProductCmptTypeBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenChangeableAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.attribute.GenProdAttribute;
import org.faktorips.util.LocalizedStringsSet;

/**
 * 
 * 
 * @author Jan Ortmann, Daniel Hohenberger
 */
public abstract class BaseProductCmptTypeBuilder extends AbstractProductCmptTypeBuilder {

    private Map generatorsByPart = new HashMap();
    private List genAttributes = new ArrayList();
    private List genProdAttributes = new ArrayList();

    /**
     * @param packageStructure
     * @param kindId
     * @param localizedStringsSet
     */
    public BaseProductCmptTypeBuilder(IIpsArtefactBuilderSet builderSet, String kindId,
            LocalizedStringsSet localizedStringsSet) {
        super(builderSet, kindId, localizedStringsSet);
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
        generatorsByPart.clear();

        createGeneratorsForAttributes();
        createGeneratorsForProdAttributes();
    }

    private void createGeneratorsForAttributes() throws CoreException {
        LocalizedStringsSet stringsSet = new LocalizedStringsSet(GenAttribute.class);
        IPolicyCmptType type = getPcType();
        if (type != null) {
            IPolicyCmptTypeAttribute[] attrs = type.getPolicyCmptTypeAttributes();
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
    }

    private void createGeneratorsForProdAttributes() throws CoreException {
        LocalizedStringsSet stringsSet = new LocalizedStringsSet(GenProdAttribute.class);
        IProductCmptType type = getProductCmptType();
        if (type != null) {
            IProductCmptTypeAttribute[] attrs = type.getProductCmptTypeAttributes();
            for (int i = 0; i < attrs.length; i++) {
                if (attrs[i].isValid()) {
                    GenProdAttribute generator = createGenerator(attrs[i], stringsSet);
                    if (generator != null) {
                        genProdAttributes.add(generator);
                        generatorsByPart.put(attrs[i], generator);
                    }
                }
            }
        }
    }

    protected GenAttribute getGenerator(IPolicyCmptTypeAttribute a) {
        return (GenAttribute)generatorsByPart.get(a);
    }

    protected GenProdAttribute getGenerator(IProductCmptTypeAttribute a) {
        return (GenProdAttribute)generatorsByPart.get(a);
    }

    protected abstract GenAttribute createGenerator(IPolicyCmptTypeAttribute a, LocalizedStringsSet localizedStringsSet)
            throws CoreException;

    protected abstract GenProdAttribute createGenerator(IProductCmptTypeAttribute a,
            LocalizedStringsSet localizedStringsSet) throws CoreException;

    protected Iterator getGenProdAttributes() {
        return genProdAttributes.iterator();
    }

    /**
     * This method is called from the abstract builder if the policy component attribute is valid
     * and therefore code can be generated.
     * <p>
     * 
     * @param attribute The attribute sourcecode should be generated for.
     * @param datatypeHelper The datatype code generation helper for the attribute's datatype.
     * @param fieldsBuilder The code fragment builder to build the member variabales section.
     * @param methodsBuilder The code fragment builder to build the method section.
     */
    protected void generateCodeForPolicyCmptTypeAttribute(IPolicyCmptTypeAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        GenChangeableAttribute generator = (GenChangeableAttribute)getGenerator(a);
        if (generator != null) {
            generator.generateCodeForProductCmptType(generatesInterface());
        }
    }

    /**
     * This method is called from the abstract builder if the product component attribute is valid
     * and therefore code can be generated.
     * <p>
     * 
     * @param attribute The attribute sourcecode should be generated for.
     * @param datatypeHelper The datatype code generation helper for the attribute's datatype.
     * @param fieldsBuilder The code fragment builder to build the member variabales section.
     * @param methodsBuilder The code fragment builder to build the method section.
     */
    // TODO werden datatypeHelper und die builder noch gebraucht?
    protected void generateCodeForProductCmptTypeAttribute(org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder,
            JavaCodeFragmentBuilder constantBuilder) throws CoreException {

        GenProdAttribute generator = (GenProdAttribute)getGenerator(attribute);
        if (generator != null) {
            generator.generate(generatesInterface());
        }
    }

    /**
     * Generates the code for a method defined in the model. This includes formula signature
     * definitions.
     */
    //FIXME remove
    protected final void generateCodeForModelMethod(IProductCmptTypeMethod method,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException{};

}
