/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.type;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.util.DatatypeComparator;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.internal.dependency.DatatypeDependency;
import org.faktorips.devtools.model.internal.dependency.IpsObjectDependency;
import org.faktorips.devtools.model.internal.ipsobject.BaseIpsObject;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.model.internal.method.Method;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.method.IParameter;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.ITypeHierarchy;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Element;

/**
 * Implementation of the published interface.
 * 
 * @author Jan Ortmann
 */
public abstract class Type extends BaseIpsObject implements IType {

    private String supertype = ""; //$NON-NLS-1$

    private boolean abstractFlag;

    public Type(IIpsSrcFile file) {
        super(file);
    }

    @Override
    public boolean isAbstract() {
        return abstractFlag;
    }

    @Override
    public void setAbstract(boolean newValue) {
        boolean oldValue = abstractFlag;
        abstractFlag = newValue;
        valueChanged(oldValue, newValue);
    }

    @Override
    public String getSupertype() {
        return supertype;
    }

    @Override
    public IType findSupertype(IIpsProject ipsProject) {
        return (IType)ipsProject.findIpsObject(getIpsObjectType(), supertype);
    }

    @Override
    public boolean hasSupertype() {
        return IpsStringUtils.isNotEmpty(supertype);
    }

    @Override
    public boolean hasExistingSupertype(IIpsProject ipsProject) {
        return findSupertype(ipsProject) != null;
    }

    @Override
    public void setSupertype(String newSupertype) {
        String oldSupertype = supertype;
        supertype = newSupertype;
        valueChanged(oldSupertype, newSupertype);
    }

    @Override
    public boolean isSubtypeOf(IType supertypeCandidate, IIpsProject ipsProject) {
        if (supertypeCandidate == null) {
            return false;
        }
        IType foundSupertype = findSupertype(ipsProject);
        if (foundSupertype == null) {
            return false;
        }
        if (supertypeCandidate.equals(foundSupertype)) {
            return true;
        }
        IsSubtypeOfVisitor visitor = new IsSubtypeOfVisitor(ipsProject, supertypeCandidate);
        visitor.start(foundSupertype);
        return visitor.isSubtype();
    }

    @Override
    public boolean isSubtypeOrSameType(IType candidate, IIpsProject project) {
        if (equals(candidate)) {
            return true;
        }
        return isSubtypeOf(candidate, project);
    }

    protected abstract IpsObjectPartCollection<? extends IAttribute> getAttributesPartCollection();

    protected abstract IpsObjectPartCollection<? extends IAssociation> getAssociationPartCollection();

    protected abstract IpsObjectPartCollection<? extends IMethod> getMethodPartCollection();

    @Override
    public List<IAttribute> getAttributes() {
        return new ArrayList<>(getAttributesPartCollection().getBackingList());
    }

    @Override
    public IAttribute getAttribute(String name) {
        return getAttributesPartCollection().getPartByName(name);
    }

    @Override
    public List<IMethod> findAllMethods(IIpsProject ipsProject) {
        AllMethodsFinder finder = new AllMethodsFinder(ipsProject);
        finder.start(this);
        return finder.getMethodes();
    }

    @Override
    public List<IAttribute> findAllAttributes(IIpsProject ipsProject) {
        AllAttributeFinder finder = new AllAttributeFinder(ipsProject);
        finder.start(this);
        return finder.attributes;
    }

    @Override
    public List<IAssociation> findAllAssociations(IIpsProject ipsProject) {
        AllAssociationFinder finder = new AllAssociationFinder(ipsProject, true);
        finder.start(this);
        return finder.getAssociationsFound();
    }

    @Override
    public IAttribute findAttribute(String name, IIpsProject project) {
        AttributeFinder finder = new AttributeFinder(project, name);
        finder.start(this);
        return finder.attribute;
    }

    @Override
    public IAttribute newAttribute() {
        return getAttributesPartCollection().newPart();
    }

    @Override
    public int getNumOfAttributes() {
        return getAttributesPartCollection().size();
    }

    @Override
    public int[] moveAttributes(int[] indexes, boolean up) {
        return getAttributesPartCollection().moveParts(indexes, up);
    }

    @Override
    public IAssociation findAssociation(String name, IIpsProject project) {
        AssociationFinder finder = new AssociationFinder(project, name);
        finder.start(this);
        return finder.association;
    }

    @Override
    public IAssociation findAssociationByRoleNamePlural(String roleNamePlural, IIpsProject ipsProject) {
        AssociationFinderPlural finder = new AssociationFinderPlural(ipsProject, roleNamePlural);
        finder.start(this);
        return finder.association;
    }

    @Override
    public List<IAssociation> findAssociationsForTargetAndAssociationType(String target,
            AssociationType associationType,
            IIpsProject project,
            boolean includeSupertypes) {

        if (target == null || associationType == null) {
            return new ArrayList<>();
        }

        if (includeSupertypes) {
            AssociationTargetAndTypeFinder finder = new AssociationTargetAndTypeFinder(project, target,
                    associationType);
            finder.start(this);
            return finder.getAssociationsFound();
        } else {
            return findAssociationsForTargetAndAssociationTypeInternal(target, associationType, project);
        }
    }

    /**
     * @param project the project used to find the associations
     * @throws IpsException in case of an exception while finding a type or association
     */
    protected List<IAssociation> findAssociationsForTargetAndAssociationTypeInternal(String target,
            AssociationType associationType,
            IIpsProject project) {
        List<IAssociation> result = new ArrayList<>();
        List<IAssociation> associations = getAssociationsForTarget(target);
        for (IAssociation association : associations) {
            if (association.getAssociationType() == associationType) {
                result.add(association);
            }
        }
        return result;
    }

    @Override
    public IAssociation getAssociation(String name) {
        return getAssociationPartCollection().getPartByName(name);
    }

    @Override
    public IAssociation getAssociationByRoleNamePlural(String roleNamePlural) {
        if (roleNamePlural == null) {
            return null;
        }
        for (IAssociation association : getAssociationPartCollection()) {
            if (roleNamePlural.equals(association.getTargetRolePlural())) {
                return association;
            }
        }
        return null;
    }

    @Override
    public List<IAssociation> getAssociationsForTarget(String target) {
        List<IAssociation> result = new ArrayList<>();
        for (IAssociation association : getAssociationPartCollection()) {
            if (association.getTarget().equals(target)) {
                result.add(association);
            }
        }
        return result;
    }

    @Override
    public List<IAssociation> getAssociations() {
        return new ArrayList<>(getAssociationPartCollection().getBackingList());
    }

    @Override
    public List<IAssociation> getAssociations(AssociationType... types) {
        List<IAssociation> associations = new ArrayList<>();
        List<IAssociation> findAssociations = this.getAssociations();
        for (IAssociation association : findAssociations) {
            if (Arrays.asList(types).contains(association.getAssociationType())) {
                associations.add(association);
            }
        }
        return associations;
    }

    @Override
    public int getNumOfAssociations() {
        return getAssociationPartCollection().size();
    }

    @Override
    public int[] moveAssociations(int[] indexes, boolean up) {
        return getAssociationPartCollection().moveParts(indexes, up);
    }

    @Override
    public IAssociation newAssociation() {
        return getAssociationPartCollection().newPart();
    }

    @Override
    public IMethod newMethod() {
        return getMethodPartCollection().newPart();
    }

    @Override
    public List<IMethod> getMethods() {
        return new ArrayList<>(getMethodPartCollection().getBackingList());
    }

    @Override
    public IMethod getMethod(String methodName, final String[] datatypes) {
        String[] myDatatypes = datatypes;
        if (myDatatypes == null) {
            myDatatypes = new String[0];
        }
        for (IMethod method : getMethodPartCollection()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            IParameter[] params = method.getParameters();
            if (params.length != myDatatypes.length) {
                continue;
            }
            boolean paramsOk = true;
            for (int i = 0; i < params.length; i++) {
                if (!params[i].getDatatype().equals(myDatatypes[i])) {
                    paramsOk = false;
                    break;
                }
            }
            if (paramsOk) {
                return method;
            }
        }
        return null;
    }

    @Override
    public IMethod findMethod(String name, String[] datatypes, IIpsProject ipsProject) {
        MethodFinderByNameAndParamtypes finder = new MethodFinderByNameAndParamtypes(ipsProject, name, datatypes);
        finder.start(this);
        return finder.method;
    }

    @Override
    public IMethod getMethod(String signature) {
        for (IMethod method : getMethodPartCollection()) {
            if (method.getSignatureString().equals(signature)) {
                return method;
            }
        }
        return null;
    }

    @Override
    public IMethod findMethod(String signature, IIpsProject ipsProject) {
        MethodFinderBySignature finder = new MethodFinderBySignature(ipsProject, signature);
        finder.start(this);
        return finder.method;
    }

    @Override
    public int getNumOfMethods() {
        return getMethodPartCollection().size();
    }

    @Override
    public int[] moveMethods(int[] indexes, boolean up) {
        return getMethodPartCollection().moveParts(indexes, up);
    }

    @Override
    public List<IMethod> findOverrideMethodCandidates(boolean onlyNotImplementedAbstractMethods,
            IIpsProject ipsProject) {

        MethodOverrideCandidatesFinder finder = new MethodOverrideCandidatesFinder(ipsProject,
                onlyNotImplementedAbstractMethods);
        finder.start(findSupertype(ipsProject));
        return finder.candidates;
    }

    @Override
    public List<IMethod> overrideMethods(List<IMethod> methods) {
        List<IMethod> newMethods = new ArrayList<>(methods.size());
        for (IMethod method : methods) {
            IMethod override = newMethod();
            override.setModifier(method.getModifier());
            override.setAbstract(false);
            override.setDatatype(method.getDatatype());
            override.setName(method.getName());
            IParameter[] params = method.getParameters();
            for (IParameter param : params) {
                IParameter newParam = override.newParameter();
                newParam.setName(param.getName());
                newParam.setDatatype(param.getDatatype());
            }
            newMethods.add(override);
        }
        return newMethods;
    }

    @Override
    public List<IAttribute> findOverrideAttributeCandidates(IIpsProject ipsProject) {
        IType foundSupertype = findSupertype(ipsProject);

        if (foundSupertype == null) {
            // no supertype, no candidates :-)
            return new ArrayList<>();
        }

        // for easy finding attributes by name put them in a map with the name as key
        Map<String, IAttribute> toExclude = new HashMap<>();
        for (IAttribute attribute : getAttributesPartCollection()) {
            if (attribute.isOverwrite()) {
                toExclude.put(attribute.getName(), attribute);
            }
        }

        // find all overwrite-candidates
        List<IAttribute> candidates = getSupertypeHierarchy().getAllAttributes(foundSupertype);
        List<IAttribute> result = new ArrayList<>();
        for (IAttribute candidate : candidates) {
            if (!toExclude.containsKey(candidate.getName())) {
                result.add(candidate);
            }
        }

        return result;
    }

    @Override
    public List<IAssociation> findConstrainableAssociationCandidates(IIpsProject ipsProject) {
        ConstrainableAssociationFinder finder = new ConstrainableAssociationFinder(false, ipsProject);
        finder.start(this);
        return finder.getAssociationsFound();
    }

    @Override
    public List<IAttribute> overrideAttributes(List<? extends IAttribute> attributes) {
        List<IAttribute> newAttributes = new ArrayList<>(attributes.size());
        for (IAttribute attribute : attributes) {
            IAttribute override = createAttributeIfNeccessary(attribute);
            override.setOverwrite(true);
            newAttributes.add(override);
        }
        return newAttributes;
    }

    /**
     * Checks whether an attribute with the given attribute's name already exists in this type and
     * creates a new one otherwise. Returns the existent or newly created attribute. Copies all
     * properties of the overridden attribute to the new one, if a new one needs to be created.
     */
    private IAttribute createAttributeIfNeccessary(IAttribute attribute) {
        IAttribute override = getAttribute(attribute.getName());
        if (override == null) {
            override = newAttribute();
            override.copyFromWithoutLabelAndDescription(attribute);
        }
        return override;
    }

    @Override
    public ITypeHierarchy getSupertypeHierarchy() {
        return TypeHierarchy.getSupertypeHierarchy(this);
    }

    @Override
    public ITypeHierarchy getSubtypeHierarchy() {
        return TypeHierarchy.getSubtypeHierarchy(this);
    }

    @Override
    public boolean hasSameMethod(IMethod method) {
        return getMatchingMethod(method) != null;
    }

    @Override
    public IMethod getMatchingMethod(IMethod method) {
        for (IMethod thisMethod : getMethodPartCollection()) {
            if (thisMethod.isSameSignature(method)) {
                return thisMethod;
            }
        }
        return null;
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        supertype = XmlUtil.getAttributeOrEmptyString(element, PROPERTY_SUPERTYPE);
        abstractFlag = XmlUtil.getBooleanAttributeOrFalse(element, PROPERTY_ABSTRACT);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        if (IpsStringUtils.isNotEmpty(supertype)) {
            element.setAttribute(PROPERTY_SUPERTYPE, supertype);
        }
        if (abstractFlag) {
            element.setAttribute(PROPERTY_ABSTRACT, "" + abstractFlag); //$NON-NLS-1$
        }
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        super.validateThis(list, ipsProject);
        DuplicatePropertyNameValidator duplicateValidator = createDuplicatePropertyNameValidator(ipsProject);
        duplicateValidator.start(this);
        duplicateValidator.addMessagesForDuplicates(this, list);
        if (hasSupertype()) {
            list.add(TypeValidations.validateTypeHierachy(this, ipsProject));
        }
        if (!isAbstract()) {
            validateIfAllAbstractMethodsAreImplemented(getIpsProject(), list);
            IIpsProjectProperties props = getIpsProject().getReadOnlyProperties();
            if (props.isDerivedUnionIsImplementedRuleEnabled()) {
                DerivedUnionsSpecifiedValidator validator = new DerivedUnionsSpecifiedValidator(list, ipsProject);
                validator.start(this);
            }
            for (IMethod method : getMethodPartCollection()) {
                if (method.isAbstract()) {
                    String text = Messages.Type_msg_AbstractMissmatch;
                    list.add(new Message(MSGCODE_ABSTRACT_MISSING, text, Message.ERROR, this, PROPERTY_ABSTRACT));
                    break;
                }
            }
        }

        validateAbstractAttributes(list, ipsProject);
    }

    public void validateAbstractAttributes(MessageList list, IIpsProject ipsProject) {
        if (!isAbstract()) {
            findAllAttributes(ipsProject).stream().filter(attribute -> !attribute.isOfType(getQualifiedNameType()))
                    .forEach(attribute -> {
                        new AttributeAbstractDatatypeValidator(attribute, this, ipsProject)
                                .validateNotAbstractDatatype(list);
                    });
        }
    }

    protected abstract DuplicatePropertyNameValidator createDuplicatePropertyNameValidator(IIpsProject ipsProject);

    /**
     * Validation for {@link #MSGCODE_MUST_OVERRIDE_ABSTRACT_METHOD}
     */
    private void validateIfAllAbstractMethodsAreImplemented(IIpsProject ipsProject, MessageList list) {

        List<IMethod> methods = findOverrideMethodCandidates(true, ipsProject);
        for (IMethod method : methods) {
            String text = MessageFormat.format(Messages.Type_msg_MustOverrideAbstractMethod, method.getName(),
                    method.getType().getQualifiedName());
            list.add(new Message(MSGCODE_MUST_OVERRIDE_ABSTRACT_METHOD, text, Message.ERROR, this));
        }
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isValueDatatype() {
        return false;
    }

    @Override
    public boolean isEnum() {
        return false;
    }

    @Override
    public int compareTo(Datatype o) {
        return DatatypeComparator.doCompare(this, o);
    }

    @Override
    public boolean hasNullObject() {
        return false;
    }

    /**
     * Collects the dependencies of this type. Subclasses need to call this method in their
     * dependsOn() methods. The provided parameter must not be <code>null</code>.
     */
    protected void dependsOn(Set<IDependency> dependencies, Map<IDependency, List<IDependencyDetail>> details) {
        if (hasSupertype()) {
            IDependency dependency = IpsObjectDependency.createSubtypeDependency(getQualifiedNameType(),
                    new QualifiedNameType(getSupertype(), getIpsObjectType()));
            dependencies.add(dependency);
            addDetails(details, dependency, this, PROPERTY_SUPERTYPE);
        }
        addQualifiedNameTypesForRelationTargets(dependencies, details);
        addAttributeDatatypeDependencies(dependencies, details);
        addMethodDatatypeDependencies(dependencies, details);
    }

    private void addMethodDatatypeDependencies(Set<IDependency> dependencies,
            Map<IDependency, List<IDependencyDetail>> details) {
        for (IMethod method2 : getMethodPartCollection()) {
            Method method = (Method)method2;
            method.dependsOn(dependencies, details);
        }
    }

    private void addAttributeDatatypeDependencies(Set<IDependency> dependencies,
            Map<IDependency, List<IDependencyDetail>> details) {

        for (IAttribute attribute : getAttributesPartCollection()) {
            String datatype = attribute.getDatatype();
            IDependency dependency = new DatatypeDependency(getQualifiedNameType(), datatype);
            dependencies.add(dependency);
            addDetails(details, dependency, attribute, IAttribute.PROPERTY_DATATYPE);
        }
    }

    private void addQualifiedNameTypesForRelationTargets(Set<IDependency> dependencies,
            Map<IDependency, List<IDependencyDetail>> details) {

        for (IAssociation relation : getAssociationPartCollection()) {
            String targetQName = relation.getTarget();
            /*
             * an additional condition "&& this.isAggregateRoot()" will _not_ be helpful, because
             * this method is called recursively for the detail and so on. But this detail is not an
             * aggregate root and the recursion will terminate too early.
             */
            if (relation.getAssociationType().equals(AssociationType.COMPOSITION_MASTER_TO_DETAIL)) {
                IDependency dependency = IpsObjectDependency.createCompostionMasterDetailDependency(
                        getQualifiedNameType(), new QualifiedNameType(targetQName, getIpsObjectType()));

                dependencies.add(dependency);
                addDetails(details, dependency, relation, IAssociation.PROPERTY_TARGET);
            } else {
                IDependency dependency = IpsObjectDependency.createReferenceDependency(getQualifiedNameType(),
                        new QualifiedNameType(targetQName, getIpsObjectType()));
                dependencies.add(dependency);
                addDetails(details, dependency, relation, IAssociation.PROPERTY_TARGET);
            }
        }
    }

    protected void checkDerivedUnionIsImplemented(IAssociation association,
            List<IAssociation> candidateSubsets,
            MessageList msgList) {
        if (association.isDerivedUnion()) {
            if (!isSubsetted(association, candidateSubsets)) {
                String text = MessageFormat.format(Messages.Type_msg_MustImplementDerivedUnion, association.getName(),
                        association.getType().getQualifiedName());
                msgList.add(new Message(IType.MSGCODE_MUST_SPECIFY_DERIVED_UNION, text, Message.ERROR, Type.this,
                        IType.PROPERTY_ABSTRACT));
            }
        }
    }

    private boolean isSubsetted(IAssociation derivedUnion, List<IAssociation> candidateSubsets) {
        for (IAssociation candidate : candidateSubsets) {
            if (derivedUnion == candidate.findSubsettedDerivedUnion(getIpsProject())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<IType> findSubtypes(boolean transitive, boolean includeSelf, IIpsProject project) {
        TypeHierarchy subtypeHierarchy = TypeHierarchy.getSubtypeHierarchy(this, project);
        return getSubtypesInternal(transitive, includeSelf, subtypeHierarchy);
    }

    @Override
    public List<IType> searchSubtypes(boolean transitive, boolean includingSelf) {
        TypeHierarchy subtypeHierarchy = TypeHierarchy.getSubtypeHierarchy(this);
        return getSubtypesInternal(transitive, includingSelf, subtypeHierarchy);
    }

    private List<IType> getSubtypesInternal(boolean transitive, boolean includingSelf, TypeHierarchy subtypeHierarchy) {
        List<IType> result;
        if (transitive) {
            result = subtypeHierarchy.getAllSubtypes(this);
        } else {
            result = subtypeHierarchy.getSubtypes(this);
        }
        if (includingSelf) {
            result.add(this);
        }
        return result;
    }

    @Override
    public IAssociation constrainAssociation(IAssociation associationToConstrain, IType targetType) {
        IAssociation association = createAssociationIfNeccessary(associationToConstrain);
        association.setTarget(targetType.getQualifiedName());
        association.setConstrain(true);
        return association;
    }

    /**
     * Checks whether an association with the given association's name already exists in this type
     * and creates a new one otherwise. Returns the existent or newly created association. Copies
     * all properties of the overridden association to the new one, if a new one needs to be
     * created.
     */
    private IAssociation createAssociationIfNeccessary(IAssociation associationToConstrain) {
        IAssociation association = getAssociation(associationToConstrain.getName());
        if (association == null) {
            association = newAssociation();
            association.copyFromWithoutLabelAndDescription(associationToConstrain);
        }
        return association;
    }

    private class MethodOverrideCandidatesFinder extends TypeHierarchyVisitor<IType> {

        private List<IMethod> candidates = new ArrayList<>();

        private boolean onlyNotImplementedAbstractMethods;

        public MethodOverrideCandidatesFinder(IIpsProject ipsProject, boolean onlyNotImplementedAbstractMethods) {
            super(ipsProject);
            this.onlyNotImplementedAbstractMethods = onlyNotImplementedAbstractMethods;
        }

        @Override
        protected boolean visit(IType currentType) {
            List<IMethod> typeMethods = currentType.getMethods();
            for (IMethod method : typeMethods) {
                if (onlyNotImplementedAbstractMethods && !method.isAbstract()) {
                    continue;
                }
                IMethod overridingMethod = method.findOverridingMethod(Type.this, getIpsProject());
                if (overridingMethod != null && overridingMethod.getType() == Type.this) {
                    continue;
                }
                if (overridingMethod == null || (!onlyNotImplementedAbstractMethods)) {
                    // candidate found, but it might be already in the list
                    if (!sameMethodAlreadyInCandidateList(method, candidates)) {
                        candidates.add(method);
                    }
                }
            }
            return true;
        }

        private boolean sameMethodAlreadyInCandidateList(IMethod method, List<IMethod> candidates) {
            for (IMethod candidate : candidates) {
                if (method.isSameSignature(candidate)) {
                    return true;
                }
            }
            return false;
        }

    }

    private static class AssociationFinder extends TypeHierarchyVisitor<IType> {

        private String associationName;

        private IAssociation association = null;

        public AssociationFinder(IIpsProject project, String associationName) {
            super(project);
            this.associationName = associationName;
        }

        @Override
        protected boolean visit(IType currentType) {
            association = currentType.getAssociation(associationName);
            return association == null;
        }

    }

    private static class AssociationFinderPlural extends TypeHierarchyVisitor<IType> {

        private String associationName;

        private IAssociation association = null;

        public AssociationFinderPlural(IIpsProject project, String associationName) {
            super(project);
            this.associationName = associationName;
        }

        @Override
        protected boolean visit(IType currentType) {
            association = currentType.getAssociationByRoleNamePlural(associationName);
            return association == null;
        }

    }

    protected abstract static class AbstractAssociationFinder<T extends IAssociation>
            extends TypeHierarchyVisitor<IType> {

        private List<T> associationsFound = new ArrayList<>();
        private final boolean superTypeFirst;

        public AbstractAssociationFinder(boolean superTypeFirst, IIpsProject ipsProject) {
            super(ipsProject);
            this.superTypeFirst = superTypeFirst;
        }

        @Override
        protected boolean visit(IType currentType) {
            List<T> associations = getAssociations(currentType);
            int index;
            if (superTypeFirst) {
                index = 0;
            } else {
                index = associationsFound.size();
            }
            for (T association : associations) {
                if (isAssociationWanted(association)) {
                    associationsFound.add(index, association);
                    index++;
                }
            }
            // Always continue because we search for all matching association.
            return true;
        }

        /**
         * This method returns <code>true</code> if the association specified by the parameter
         * should be added to the list of found associations.
         * <p>
         * Subclasses may implement this method to include or exclude some associations.
         * 
         * @param association The association that should currently be added
         * @return <code>true</code> to add the association, <code>false</code> to ignore.
         */
        protected boolean isAssociationWanted(IAssociation association) {
            return !isConstrainAlreadyAdded(association);
        }

        /**
         * Returns the list of associations that from the current type. For example if you want to
         * have all associations of the current type, your implementation calls
         * {@link IType#getAssociations()}.
         * 
         * @param currentType The type of which you have to return the associations from.
         * @return The list of associations of the current type
         */
        protected abstract List<T> getAssociations(IType currentType);

        /**
         * Prevent more general associations from being added, which also applies to the constrained
         * association itself (as it is the most general). The goal actually is to only find/add the
         * most specific association of a hierarchy of constrained and constraining associations.
         * Finding the original constrained association of a constraining association is easy, in
         * contrast to the other way round.
         */
        private boolean isConstrainAlreadyAdded(IAssociation association) {
            for (IAssociation alreadyAdded : getAssociationsFound()) {
                if (alreadyAdded.isConstrain() && alreadyAdded.getName().equals(association.getName())) {
                    return true;
                }
            }
            return false;
        }

        /** Returns the associations which matches the given target and association type. */
        public List<T> getAssociationsFound() {
            return associationsFound;
        }

    }

    /**
     * Finds all associations with the given target and association type in the type hierarchy.
     * 
     * @author Joerg Ortmann
     */
    private static class AssociationTargetAndTypeFinder extends AbstractAssociationFinder<IAssociation> {

        private String associationTarget;

        private AssociationType associationType;

        public AssociationTargetAndTypeFinder(IIpsProject project, String associationTarget,
                AssociationType associationType) {
            super(false, project);
            this.associationTarget = associationTarget;
            this.associationType = associationType;
        }

        @Override
        protected List<IAssociation> getAssociations(IType currentType) {
            return ((Type)currentType).findAssociationsForTargetAndAssociationTypeInternal(associationTarget,
                    associationType, getIpsProject());
        }

    }

    private static class AllAssociationFinder extends AbstractAssociationFinder<IAssociation> {

        public AllAssociationFinder(IIpsProject ipsProject, boolean superTypeFirst) {
            super(superTypeFirst, ipsProject);
        }

        @Override
        protected List<IAssociation> getAssociations(IType currentType) {
            return currentType.getAssociations();
        }

    }

    private static class AttributeFinder extends TypeHierarchyVisitor<IType> {

        private String attributeName;

        private IAttribute attribute;

        public AttributeFinder(IIpsProject ipsProject, String attrName) {
            super(ipsProject);
            attributeName = attrName;
        }

        @Override
        protected boolean visit(IType currentType) {
            attribute = currentType.getAttribute(attributeName);
            return attribute == null;
        }

    }

    private static class MethodFinderByNameAndParamtypes extends TypeHierarchyVisitor<IType> {

        private String methodName;

        private String[] datatypes;

        private IMethod method;

        public MethodFinderByNameAndParamtypes(IIpsProject ipsProject, String methodName, String[] datatypes) {
            super(ipsProject);
            this.methodName = methodName;
            this.datatypes = datatypes;
        }

        @Override
        protected boolean visit(IType currentType) {
            method = currentType.getMethod(methodName, datatypes);
            return method == null;
        }

    }

    private static class MethodFinderBySignature extends TypeHierarchyVisitor<IType> {

        private String signature;

        private IMethod method;

        public MethodFinderBySignature(IIpsProject ipsProject, String signature) {
            super(ipsProject);
            this.signature = signature;
        }

        @Override
        protected boolean visit(IType currentType) {
            method = currentType.getMethod(signature);
            return method == null;
        }

    }

    private static class AllMethodsFinder extends TypeHierarchyVisitor<IType> {

        private List<IMethod> methods;

        private Set<String> methodSignatures;

        public AllMethodsFinder(IIpsProject ipsProject) {
            super(ipsProject);
            methods = new ArrayList<>();
            methodSignatures = new HashSet<>();
        }

        @Override
        protected boolean visit(IType currentType) {
            // Considers overridden methods.
            for (IMethod method : currentType.getMethods()) {
                if (!methodSignatures.contains(method.getSignatureString())) {
                    methods.add(method);
                    methodSignatures.add(method.getSignatureString());
                }
            }
            return true;
        }

        private List<IMethod> getMethodes() {
            return methods;
        }

    }

    private static class AllAttributeFinder extends TypeHierarchyVisitor<IType> {

        private List<IAttribute> attributes;

        private Set<String> attributeNames;

        public AllAttributeFinder(IIpsProject ipsProject) {
            super(ipsProject);
            attributes = new ArrayList<>();
            attributeNames = new HashSet<>();
        }

        @Override
        protected boolean visit(IType currentType) {
            List<? extends IAttribute> lattributes = currentType.getAttributes();
            List<IAttribute> attributesToAdd = new ArrayList<>();
            // Considers overridden attributes.
            for (IAttribute attribute : lattributes) {
                if (!attributeNames.contains(attribute.getName())) {
                    attributesToAdd.add(attribute);
                    attributeNames.add(attribute.getName());
                }
            }
            // Place supertype attributes before subtype attributes.
            attributes.addAll(0, attributesToAdd);
            return true;
        }

    }

    private class DerivedUnionsSpecifiedValidator extends TypeHierarchyVisitor<IType> {

        private MessageList msgList;

        private List<IAssociation> candidateSubsets = new ArrayList<>(0);

        public DerivedUnionsSpecifiedValidator(MessageList msgList, IIpsProject ipsProject) {
            super(ipsProject);
            this.msgList = msgList;
        }

        @Override
        protected boolean visit(IType currentType) {
            List<? extends IAssociation> associations = currentType.getAssociations();
            for (IAssociation association : associations) {
                candidateSubsets.add(association);
            }
            for (IAssociation association : associations) {
                checkDerivedUnionIsImplemented(association, candidateSubsets, msgList);
            }
            return true;
        }

    }

    private static class IsSubtypeOfVisitor extends TypeHierarchyVisitor<IType> {

        private IType supertypeCandidate;

        private boolean subtype = false;

        public IsSubtypeOfVisitor(IIpsProject ipsProject, IType supertypeCandidate) {
            super(ipsProject);
            ArgumentCheck.notNull(supertypeCandidate);
            this.supertypeCandidate = supertypeCandidate;
        }

        boolean isSubtype() {
            return subtype;
        }

        @Override
        protected boolean visit(IType currentType) {
            if (currentType == supertypeCandidate) {
                subtype = true;
                return false;
            }
            return true;
        }

    }

    /**
     * Finds all associations that could be constrained by this type.
     * 
     */
    private static class ConstrainableAssociationFinder extends AbstractAssociationFinder<IAssociation> {

        private Set<String> alreadyConstrained = new HashSet<>();

        public ConstrainableAssociationFinder(boolean superTypeFirst, IIpsProject ipsProject) {
            super(superTypeFirst, ipsProject);
        }

        @Override
        public void start(IType basetype) {
            addConstrainingAssociations(basetype.getAssociations());
            super.start(basetype);
        }

        private void addConstrainingAssociations(List<? extends IAssociation> associations) {
            for (IAssociation association : associations) {
                if (association.isConstrain()) {
                    alreadyConstrained.add(association.getName());
                }
            }
        }

        @Override
        protected boolean isAssociationWanted(IAssociation association) {
            return super.isAssociationWanted(association) && !isDerivedUnionOrSubset(association)
                    && !isDetailToMaster(association) && !isAlreadyConstrained(association);
        }

        private boolean isDerivedUnionOrSubset(IAssociation association) {
            return association.isDerivedUnion() || association.isSubsetOfADerivedUnion();
        }

        private boolean isDetailToMaster(IAssociation association) {
            return AssociationType.COMPOSITION_DETAIL_TO_MASTER.equals(association.getAssociationType());
        }

        private boolean isAlreadyConstrained(IAssociation association) {
            return alreadyConstrained.contains(association.getName());
        }

        @Override
        protected List<IAssociation> getAssociations(IType currentType) {
            return currentType.getAssociations();
        }

    }

}
