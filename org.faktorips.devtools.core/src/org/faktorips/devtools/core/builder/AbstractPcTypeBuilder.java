package org.faktorips.devtools.core.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

/**
 * Abstract base class that can be used for builders generating Java sourcecode for a policy
 * component type.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractPcTypeBuilder extends JavaSourceFileBuilder {

    private Map containerRelationToSubRelationMap;

    public AbstractPcTypeBuilder(IJavaPackageStructure packageStructure, String kindId,
            LocalizedStringsSet stringsSet) {
        super(packageStructure, kindId, stringsSet);
    }

    /**
     * Returns the policy component type this builder builds an artefact for.
     */
    public IPolicyCmptType getPcType() {
        return (IPolicyCmptType)getIpsObject();
    }

    /**
     * Overridden.
     */
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return ipsSrcFile.getIpsObjectType().equals(IpsObjectType.POLICY_CMPT_TYPE);
    }

    /**
     * Overridden.
     */
    public void afterBuild(IIpsSrcFile ipsSrcFile) throws CoreException {
        super.afterBuild(ipsSrcFile);
        containerRelationToSubRelationMap = null;
    }

    /**
     * Overridden.
     * 
     * Calls the generateInternal() method and addes the package and import declarations to the
     * content.
     */
    public String generate() throws CoreException {
        assertConditionsBeforeGenerating();
        StringBuffer content = new StringBuffer();
        content.append("package ");
        content.append(getPackage(getIpsSrcFile()));
        content.append(';');
        content.append(StringUtil.getSystemLineSeparator());
        content.append(StringUtil.getSystemLineSeparator());
        JavaCodeFragment code = generateCodeForJavatype();
        content.append(code.getImportDeclaration().toString());
        content.append(StringUtil.getSystemLineSeparator());
        content.append(StringUtil.getSystemLineSeparator());
        content.append(code.getSourcecode());
        return content.toString();
    }

    /*
     * Generates the sourcecode of the generated Java class or interface.
     */
    private JavaCodeFragment generateCodeForJavatype() throws CoreException {
        JavaCodeFragmentBuilder codeBuilder = new JavaCodeFragmentBuilder();
        generateTypeJavadoc(codeBuilder);
        if (generatesInterface()) {
            codeBuilder.interfaceBegin(getUnqualifiedClassName(), getExtendedInterfaces());
        } else {
            codeBuilder.classBegin(getClassModifier(), getUnqualifiedClassName(), getSuperclass(),
                getExtendedInterfaces());
        }
        JavaCodeFragmentBuilder memberVarCodeBuilder = new JavaCodeFragmentBuilder();
        JavaCodeFragmentBuilder methodCodeBuilder = new JavaCodeFragmentBuilder();

        generateCodeForAttributes(memberVarCodeBuilder, methodCodeBuilder);
        generateCodeForRelations(memberVarCodeBuilder, methodCodeBuilder);
        generateOther(memberVarCodeBuilder, methodCodeBuilder);

        codeBuilder.append(memberVarCodeBuilder.getFragment());
        generateConstructors(codeBuilder);
        codeBuilder.append(methodCodeBuilder.getFragment());

        codeBuilder.classEnd();
        return codeBuilder.getFragment();
    }

    /**
     * This method can be overridden to check conditions before the actual code generation starts. A
     * runtime exception should be thrown if the conditions are not fullfilled. This default
     * implementation is empty.
     */
    protected void assertConditionsBeforeGenerating() {
    }

    /**
     * Generates the Javadoc for the Java class or interface.
     * 
     * @param builder The builder to use to generate the Javadoc via it's javadoc method.
     */
    protected abstract void generateTypeJavadoc(JavaCodeFragmentBuilder builder) throws CoreException;

    /**
     * A hook to generate code that is not based on attributes, relations, rules and
     * methods.
     */
    protected abstract void generateOther(
    		JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    /**
     * Returns true if an interface is generated, false if a class is generated.
     */
    protected abstract boolean generatesInterface();

    protected abstract void generateConstructors(JavaCodeFragmentBuilder builder)
            throws CoreException;

    /**
     * Returns the qualified name of the superclass or <code>null</code> if the class being
     * generated is not derived from a class or is an interface.
     */
    protected abstract String getSuperclass() throws CoreException;

    /**
     * Returns the qualified name of the interfaces the generated class or interface extends.
     * Returns an empty array if no interfaces are extended
     */
    protected abstract String[] getExtendedInterfaces() throws CoreException;

    protected final void generateCodeForAttributes(JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        IAttribute[] attributes = getPcType().getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            IAttribute a = attributes[i];
            if (!a.validate().containsErrorMsg()) {
                try {
                    Datatype datatype = a.getIpsProject().findDatatype(a.getDatatype());
                    DatatypeHelper helper = a.getIpsProject().getDatatypeHelper(datatype);
                    if (helper == null) {
                        throw new CoreException(new IpsStatus("No datatype helper found for datatype " + datatype));            
                    }
                    generateCodeForAttribute(a, helper, memberVarsBuilder, methodsBuilder);
                } catch (Exception e) {

                    throw new CoreException(new IpsStatus(IStatus.ERROR,
                            "Error building attribute " + attributes[i].getName() + " of "
                                    + getQualifiedClassName(getIpsObject().getIpsSrcFile()), e));
                }
            }
        }
    }

    /**
     * This method is called from the build attributes method if the attribute is valid and
     * therefore code can be generated.
     * 
     * @param attribute The attribute sourcecode should be generated for.
     * @param datatypeHelper The datatype code generation helper for the attribute's datatype.
     * @param memberVarsBuilder The code fragment builder to build the memeber variabales section.
     * @param memberVarsBuilder The code fragment builder to build the method section.
     */
    protected abstract void generateCodeForAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    private void generateCodeForRelations(JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        createContainerRelationToSubRelationsMap();
        IRelation[] relations = getPcType().getRelations();

        for (int i = 0; i < relations.length; i++) {
            if (!relations[i].validate().containsErrorMsg()) {

                try {
                    generateCodeForRelation(relations[i], memberVarsBuilder, methodsBuilder);
                } catch (Exception e) {
                    throw new CoreException(new IpsStatus(IStatus.ERROR, "Error building relation "
                            + relations[i].getName() + " of "
                            + getQualifiedClassName(getIpsObject().getIpsSrcFile()), e));
                }
            }
        }

        for (Iterator it = containerRelationToSubRelationMap.keySet().iterator(); it.hasNext();) {
            IRelation containerRelation = (IRelation)it.next();
            List subRelationList = (List)containerRelationToSubRelationMap.get(containerRelation);
            IRelation[] subRelations = (IRelation[])subRelationList
                    .toArray(new IRelation[subRelationList.size()]);
            try {
                generateCodeForContainerRelations(containerRelation, subRelations,
                    memberVarsBuilder, methodsBuilder);
            } catch (Exception e) {
                throw new CoreException(new IpsStatus(IStatus.ERROR,
                        "Error building container relation " + containerRelation.getName() + " of "
                                + getQualifiedClassName(getIpsObject().getIpsSrcFile()), e));
            }

        }
    }

    private void createContainerRelationToSubRelationsMap() throws CoreException {
        IRelation[] relations = getPcType().getRelations();
        Map containerRelationNameToSubRelationMap = new HashMap();

        for (int i = 0; i < relations.length; i++) {
            if (!relations[i].validate().containsErrorMsg()) {

                if (relations[i].hasContainerRelation()) {
                    addToContainerRelationMap(containerRelationNameToSubRelationMap, relations[i],
                        relations[i].getContainerRelation());
                } else {
                    IRelation reverseComposition = relations[i]
                            .findContainerRelationOfTypeReverseComposition();
                    if (reverseComposition != null) {
                        addToContainerRelationMap(containerRelationNameToSubRelationMap,
                            relations[i], reverseComposition.getName());
                    }
                }
            }
        }

        containerRelationToSubRelationMap = new HashMap(containerRelationNameToSubRelationMap
                .size());
        for (Iterator it = containerRelationNameToSubRelationMap.keySet().iterator(); it.hasNext();) {
            String name = (String)it.next();
            List subRelations = (List)containerRelationNameToSubRelationMap.get(name);
            IRelation containerRelation = findContainerRelation(getPcType(), name);
            containerRelationToSubRelationMap.put(containerRelation, subRelations);
        }
    }

    private IRelation findContainerRelation(IPolicyCmptType pcType, String containerRelationName)
            throws CoreException {
        if (StringUtils.isEmpty(containerRelationName)) {
            return null;
        }
        IRelation containerRelation = pcType.getRelation(containerRelationName);
        if (containerRelation != null) {
            return containerRelation;
        }
        IPolicyCmptType supertype = pcType.findSupertype();
        if (supertype == null) {
            return null;
        }
        return findContainerRelation(supertype, containerRelationName);
    }

    /**
     * This method can be called within implementations of the generateCodeForRelation() method to
     * distinguish relations that are part of the container relations hold by the policy component
     * type instance of this builder. Calls to this method outside the generateCodeForRelation()
     * might cause a runtime exception.
     * 
     * @param relation the relation to check
     */
    protected boolean isContainerRelation(IRelation relation) {
        return containerRelationToSubRelationMap.containsKey(relation);
    }

    private void addToContainerRelationMap(Map containerRelationToSubRelationMap,
            IRelation rel,
            String containerRelationName) {
        List subRelations = (List)containerRelationToSubRelationMap.get(containerRelationName);
        if (subRelations == null) {
            subRelations = new ArrayList();
        }
        subRelations.add(rel);
        containerRelationToSubRelationMap.put(containerRelationName, subRelations);
    }

    /**
     * Subclasses may provide an implementation generating methods and attributes based on the
     * provided IRelation. This method is called for every IRelation instance assigned to the
     * ProductCmptType object hold by this builder.
     * 
     * @param relation the relation source code should be generated for
     * @param memberVarsBuilder the code fragment builder to build the memeber variabales section.
     * @param memberVarsBuilder the code fragment builder to build the method section.
     * @throws Exception implementations of this method don't have to take care about rising checked
     *             exceptions. An exception that had been thrown leads to an interruption of the
     *             current build cycle of this builder. Alternatively it is possible to catch an
     *             exception and log it by means of the addToBuildStatus() method of the super
     *             class.
     * @see JavaSourceFileBuilder#addToBuildStatus(CoreException)
     * @see JavaSourceFileBuilder#addToBuildStatus(IStatus)
     */
    protected abstract void generateCodeForRelation(IRelation relation,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;

    /**
     * The generateCodeForRelation() method is called for each relation assigned to the policy
     * component type instance hold by an instance of this class. This method is called for each
     * group of relations that have the same container relation. A group is provided to this method
     * as an array of relations. This method is called the first time after the method
     * generateCodeForRelation() was called for all relation instances of the policy component type
     * instance.
     * 
     * @param containerRelation the container relation that is common for the relations in the group
     * @param subRelations a group of relation instances that have the same container relation
     * @param memberVarsBuilder the code fragment builder to build the memeber variabales section.
     * @param memberVarsBuilder the code fragment builder to build the method section.
     * @throws Exception implementations of this method don't have to take care about rising checked
     *             exceptions. An exception that had been thrown leads to an interruption of the
     *             current build cycle of this builder. Alternatively it is possible to catch an
     *             exception and log it by means of the addToBuildStatus() methods of the super
     *             class.
     */
    protected abstract void generateCodeForContainerRelations(IRelation containerRelation,
            IRelation[] subRelations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception;

    /**
     * Returns the class modifier.
     * 
     * @see java.lang.reflect.Modifier
     */
    protected int getClassModifier() throws CoreException {
        return getPcType().isAbstract() ? java.lang.reflect.Modifier.PUBLIC
                | java.lang.reflect.Modifier.ABSTRACT : java.lang.reflect.Modifier.PUBLIC;
    }

}
