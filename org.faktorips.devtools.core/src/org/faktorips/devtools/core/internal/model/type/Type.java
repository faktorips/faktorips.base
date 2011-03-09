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

package org.faktorips.devtools.core.internal.model.type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.ipsobject.BaseIpsObject;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.model.DatatypeDependency;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IDependencyDetail;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.ProductCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
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
    public IType findSupertype(IIpsProject ipsProject) throws CoreException {
        return (IType)ipsProject.findIpsObject(getIpsObjectType(), supertype);
    }

    @Override
    public boolean hasSupertype() {
        return StringUtils.isNotEmpty(supertype);
    }

    @Override
    public boolean hasExistingSupertype(IIpsProject ipsProject) throws CoreException {
        return findSupertype(ipsProject) != null;
    }

    @Override
    public void setSupertype(String newSupertype) {
        String oldSupertype = supertype;
        supertype = newSupertype;
        valueChanged(oldSupertype, newSupertype);
    }

    @Override
    public boolean isSubtypeOf(IType supertypeCandidate, IIpsProject ipsProject) throws CoreException {
        if (supertypeCandidate == null) {
            return false;
        }
        IType supertype = findSupertype(ipsProject);
        if (supertype == null) {
            return false;
        }
        if (supertypeCandidate.equals(supertype)) {
            return true;
        }
        IsSubtypeOfVisitor visitor = new IsSubtypeOfVisitor(ipsProject, supertypeCandidate);
        visitor.start(supertype);
        return visitor.isSubtype();
    }

    @Override
    public boolean isSubtypeOrSameType(IType candidate, IIpsProject project) throws CoreException {
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
        return new ArrayList<IAttribute>(getAttributesPartCollection().getBackingList());
    }

    @Override
    public IAttribute getAttribute(String name) {
        return getAttributesPartCollection().getPartByName(name);
    }

    @Override
    public List<IMethod> findAllMethods(IIpsProject ipsProject) throws CoreException {
        AllMethodsFinder finder = new AllMethodsFinder(ipsProject);
        finder.start(this);
        return finder.getMethodes();
    }

    @Override
    public List<IAttribute> findAllAttributes(IIpsProject ipsProject) throws CoreException {
        AllAttributeFinder finder = new AllAttributeFinder(ipsProject);
        finder.start(this);
        return finder.attributes;
    }

    @Override
    public List<IAssociation> findAllAssociations(IIpsProject ipsProject) throws CoreException {
        AllAssociationFinder finder = new AllAssociationFinder(ipsProject, true);
        finder.start(this);
        return finder.associations;
    }

    @Override
    public IAttribute findAttribute(String name, IIpsProject project) throws CoreException {
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
    public IAssociation findAssociation(String name, IIpsProject project) throws CoreException {
        AssociationFinder finder = new AssociationFinder(project, name);
        finder.start(this);
        return finder.association;
    }

    @Override
    public IAssociation findAssociationByRoleNamePlural(String roleNamePlural, IIpsProject ipsProject)
            throws CoreException {

        AssociationFinderPlural finder = new AssociationFinderPlural(ipsProject, roleNamePlural);
        finder.start(this);
        return finder.association;
    }

    @Override
    public List<IAssociation> findAssociationsForTargetAndAssociationType(String target,
            AssociationType associationType,
            IIpsProject project,
            boolean includeSupertypes) throws CoreException {

        if (target == null || associationType == null) {
            return new ArrayList<IAssociation>();
        }

        if (includeSupertypes) {
            AssociationTargetAndTypeFinder finder = new AssociationTargetAndTypeFinder(project, target, associationType);
            finder.start(this);
            return finder.getAssociationsFound();
        } else {
            List<IAssociation> result = new ArrayList<IAssociation>();
            List<IAssociation> associations = getAssociationsForTarget(target);
            for (IAssociation association : associations) {
                if (association.getAssociationType() == associationType) {
                    result.add(association);
                }
            }
            return result;
        }
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
        List<IAssociation> result = new ArrayList<IAssociation>();
        for (IAssociation association : getAssociationPartCollection()) {
            if (association.getTarget().equals(target)) {
                result.add(association);
            }
        }
        return result;
    }

    @Override
    public List<IAssociation> getAssociations() {
        return new ArrayList<IAssociation>(getAssociationPartCollection().getBackingList());
    }

    @Override
    public List<IAssociation> findAllNotDerivedAssociations() throws CoreException {
        NotDerivedAssociationCollector collector = new NotDerivedAssociationCollector(getIpsProject());
        collector.start(this);
        return collector.associations;
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
        return new ArrayList<IMethod>(getMethodPartCollection().getBackingList());
    }

    @Override
    public IMethod getMethod(String methodName, String[] datatypes) {
        if (datatypes == null) {
            datatypes = new String[0];
        }
        for (IMethod method : getMethodPartCollection()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            IParameter[] params = method.getParameters();
            if (params.length != datatypes.length) {
                continue;
            }
            boolean paramsOk = true;
            for (int i = 0; i < params.length; i++) {
                if (!params[i].getDatatype().equals(datatypes[i])) {
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
    public IMethod findMethod(String name, String[] datatypes, IIpsProject ipsProject) throws CoreException {
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
    public IMethod findMethod(String signature, IIpsProject ipsProject) throws CoreException {
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
    public List<IMethod> findOverrideMethodCandidates(boolean onlyNotImplementedAbstractMethods, IIpsProject ipsProject)
            throws CoreException {

        MethodOverrideCandidatesFinder finder = new MethodOverrideCandidatesFinder(ipsProject,
                onlyNotImplementedAbstractMethods);
        finder.start(findSupertype(ipsProject));
        return finder.candidates;
    }

    @Override
    public List<IMethod> overrideMethods(List<IMethod> methods) {
        List<IMethod> newMethods = new ArrayList<IMethod>(methods.size());
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
        supertype = element.getAttribute(PROPERTY_SUPERTYPE);
        abstractFlag = Boolean.valueOf(element.getAttribute(PROPERTY_ABSTRACT)).booleanValue();
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_SUPERTYPE, supertype);
        element.setAttribute(PROPERTY_ABSTRACT, "" + abstractFlag); //$NON-NLS-1$
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        DuplicatePropertyNameValidator duplicateValidator = createDuplicatePropertyNameValidator(ipsProject);
        duplicateValidator.start(this);
        duplicateValidator.addMessagesForDuplicates(list);
        if (hasSupertype()) {
            validateSupertype(list, ipsProject);
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
    }

    protected DuplicatePropertyNameValidator createDuplicatePropertyNameValidator(IIpsProject ipsProject) {
        return new DuplicatePropertyNameValidator(ipsProject);
    }

    private void validateSupertype(MessageList list, IIpsProject ipsProject) throws CoreException {
        IType supertypeObj = findSupertype(ipsProject);
        if (supertypeObj == null) {
            String text = NLS.bind(Messages.Type_msg_supertypeNotFound, supertype);
            list.add(new Message(MSGCODE_SUPERTYPE_NOT_FOUND, text, Message.ERROR, this, IType.PROPERTY_SUPERTYPE));
        } else {
            SupertypesCollector collector = new SupertypesCollector(ipsProject);
            collector.start(supertypeObj);
            if (collector.cycleDetected()) {
                String msg = Messages.Type_msg_cycleInTypeHierarchy;
                list.add(new Message(MSGCODE_CYCLE_IN_TYPE_HIERARCHY, msg.toString(), Message.ERROR, this,
                        IType.PROPERTY_SUPERTYPE));
            } else {
                for (IType supertype : collector.supertypes) {
                    MessageList superResult = supertype.validate(ipsProject);
                    if (!superResult.isEmpty()) {
                        if (superResult.getMessageByCode(IType.MSGCODE_SUPERTYPE_NOT_FOUND) != null) {
                            String text = Messages.Type_msg_TypeHierarchyInconsistent;
                            list.add(new Message(MSGCODE_INCONSISTENT_TYPE_HIERARCHY, text, Message.ERROR, this,
                                    PROPERTY_SUPERTYPE));
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Validation for {@link IPolicyCmptType#MSGCODE_MUST_OVERRIDE_ABSTRACT_METHOD}
     */
    private void validateIfAllAbstractMethodsAreImplemented(IIpsProject ipsProject, MessageList list)
            throws CoreException {

        List<IMethod> methods = findOverrideMethodCandidates(true, ipsProject);
        for (IMethod method : methods) {
            String text = NLS.bind(Messages.Type_msg_MustOverrideAbstractMethod, method.getName(), method.getType()
                    .getQualifiedName());
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
        return 0;
    }

    @Override
    public String getJavaClassName() {
        throw new RuntimeException("getJavaClassName is not supported by " + getClass()); //$NON-NLS-1$
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

    private static class SupertypesCollector extends TypeHierarchyVisitor {

        private List<IType> supertypes = new ArrayList<IType>();

        public SupertypesCollector(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IType currentType) throws CoreException {
            supertypes.add(currentType);
            return true;
        }

    }

    private class MethodOverrideCandidatesFinder extends TypeHierarchyVisitor {

        private List<IMethod> candidates = new ArrayList<IMethod>();

        private boolean onlyNotImplementedAbstractMethods;

        public MethodOverrideCandidatesFinder(IIpsProject ipsProject, boolean onlyNotImplementedAbstractMethods) {
            super(ipsProject);
            this.onlyNotImplementedAbstractMethods = onlyNotImplementedAbstractMethods;
        }

        @Override
        protected boolean visit(IType currentType) throws CoreException {
            List<IMethod> typeMethods = currentType.getMethods();
            for (IMethod method : typeMethods) {
                if (onlyNotImplementedAbstractMethods && !method.isAbstract()) {
                    continue;
                }
                IMethod overridingMethod = method.findOverridingMethod(Type.this, ipsProject);
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

    private static class AssociationFinder extends TypeHierarchyVisitor {

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

    private static class AssociationFinderPlural extends TypeHierarchyVisitor {

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

    /**
     * Finds all associations with the given target and association type in the type hierarchy.
     * 
     * @author Joerg Ortmann
     */
    private static class AssociationTargetAndTypeFinder extends TypeHierarchyVisitor {

        private String associationTarget;

        private AssociationType associationType;

        private List<IAssociation> associationsFound = new ArrayList<IAssociation>();

        public AssociationTargetAndTypeFinder(IIpsProject project, String associationTarget,
                AssociationType associationType) {

            super(project);
            this.associationTarget = associationTarget;
            this.associationType = associationType;
        }

        @Override
        protected boolean visit(IType currentType) {
            List<IAssociation> associations = currentType.getAssociationsForTarget(associationTarget);
            for (IAssociation association : associations) {
                if (association.getAssociationType() == associationType) {
                    associationsFound.add(association);
                }
            }
            // Always continue because we search for all matching association.
            return true;
        }

        /** Returns the associations which matches the given target and association type. */
        public List<IAssociation> getAssociationsFound() {
            return associationsFound;
        }

    }

    private static class AttributeFinder extends TypeHierarchyVisitor {

        private String attributeName;

        private IAttribute attribute;

        public AttributeFinder(IIpsProject ipsProject, String attrName) {
            super(ipsProject);
            attributeName = attrName;
        }

        @Override
        protected boolean visit(IType currentType) throws CoreException {
            attribute = currentType.getAttribute(attributeName);
            return attribute == null;
        }

    }

    private static class MethodFinderByNameAndParamtypes extends TypeHierarchyVisitor {

        private String methodName;

        private String[] datatypes;

        private IMethod method;

        public MethodFinderByNameAndParamtypes(IIpsProject ipsProject, String methodName, String[] datatypes) {
            super(ipsProject);
            this.methodName = methodName;
            this.datatypes = datatypes;
        }

        @Override
        protected boolean visit(IType currentType) throws CoreException {
            method = currentType.getMethod(methodName, datatypes);
            return method == null;
        }

    }

    private static class MethodFinderBySignature extends TypeHierarchyVisitor {

        private String signature;

        private IMethod method;

        public MethodFinderBySignature(IIpsProject ipsProject, String signature) {
            super(ipsProject);
            this.signature = signature;
        }

        @Override
        protected boolean visit(IType currentType) throws CoreException {
            method = currentType.getMethod(signature);
            return method == null;
        }

    }

    private static class AllMethodsFinder extends TypeHierarchyVisitor {

        private List<IMethod> methods;

        private Set<String> methodSignatures;

        public AllMethodsFinder(IIpsProject ipsProject) {
            super(ipsProject);
            methods = new ArrayList<IMethod>();
            methodSignatures = new HashSet<String>();
        }

        @Override
        protected boolean visit(IType currentType) throws CoreException {
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

    private static class AllAttributeFinder extends TypeHierarchyVisitor {

        private List<IAttribute> attributes;

        private Set<String> attributeNames;

        public AllAttributeFinder(IIpsProject ipsProject) {
            super(ipsProject);
            attributes = new ArrayList<IAttribute>();
            attributeNames = new HashSet<String>();
        }

        @Override
        protected boolean visit(IType currentType) throws CoreException {
            List<? extends IAttribute> lattributes = currentType.getAttributes();
            List<IAttribute> attributesToAdd = new ArrayList<IAttribute>();
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

    private static class AllAssociationFinder extends TypeHierarchyVisitor {

        private final List<IAssociation> associations;
        private final boolean superTypeFirst;

        public AllAssociationFinder(IIpsProject ipsProject, boolean superTypeFirst) {
            super(ipsProject);
            this.superTypeFirst = superTypeFirst;
            associations = new ArrayList<IAssociation>();
        }

        @Override
        protected boolean visit(IType currentType) throws CoreException {
            List<? extends IAssociation> lassociations = currentType.getAssociations();
            if (superTypeFirst) {
                // Place supertype associations before subtype associations.
                associations.addAll(0, lassociations);
            } else {
                associations.addAll(lassociations);
            }

            return true;
        }

    }

    private class DerivedUnionsSpecifiedValidator extends TypeHierarchyVisitor {

        private MessageList msgList;

        private List<IAssociation> candidateSubsets = new ArrayList<IAssociation>(0);

        public DerivedUnionsSpecifiedValidator(MessageList msgList, IIpsProject ipsProject) {
            super(ipsProject);
            this.msgList = msgList;
        }

        @Override
        protected boolean visit(IType currentType) throws CoreException {
            List<? extends IAssociation> associations = currentType.getAssociations();
            for (IAssociation association : associations) {
                candidateSubsets.add(association);
            }
            for (IAssociation association : associations) {
                if (association.isDerivedUnion()) {
                    if (!isSubsetted(association)) {
                        String text = NLS.bind(Messages.Type_msg_MustImplementDerivedUnion, association.getName(),
                                association.getType().getQualifiedName());
                        msgList.add(new Message(IType.MSGCODE_MUST_SPECIFY_DERIVED_UNION, text, Message.ERROR,
                                Type.this, IType.PROPERTY_ABSTRACT));
                    }
                } else if (association instanceof IPolicyCmptTypeAssociation) {
                    /*
                     * special check for policy component type associations with type detail to
                     * master, if this association is a inverse of a derived union then we need to
                     * check either the class is abstract or an inverse implementation of the
                     * derived union exists
                     */
                    IPolicyCmptTypeAssociation policyCmptTypeAssociation = (IPolicyCmptTypeAssociation)association;
                    if (!policyCmptTypeAssociation.isInverseOfDerivedUnion()) {
                        continue;
                    }

                    /*
                     * now check if there is another detail to master which is the inverse of a
                     * subset derived union
                     */
                    if (!isInverseSubsetted(policyCmptTypeAssociation)) {
                        String text = NLS.bind(Messages.Type_msg_MustImplementInverseDerivedUnion,
                                association.getName(), association.getType().getQualifiedName());
                        msgList.add(new Message(IType.MSGCODE_MUST_SPECIFY_INVERSE_OF_DERIVED_UNION, text,
                                Message.ERROR, Type.this, IType.PROPERTY_ABSTRACT));
                    }
                }
            }
            return true;
        }

        private boolean isSubsetted(IAssociation derivedUnion) throws CoreException {
            for (IAssociation candidate : candidateSubsets) {
                if (derivedUnion == candidate.findSubsettedDerivedUnion(ipsProject)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isInverseSubsetted(IPolicyCmptTypeAssociation inverseOfDerivedUnion) throws CoreException {
            IPolicyCmptTypeAssociation derivedUnion = inverseOfDerivedUnion.findInverseAssociation(getIpsProject());
            if (derivedUnion == null) {
                // must be an error
                return false;
            }
            /*
             * now check if one of the candidate is the inverse of the implementation of the derived
             * union
             */
            for (IAssociation candidate : candidateSubsets) {
                if (!(candidate instanceof IPolicyCmptTypeAssociation)) {
                    continue;
                }
                IPolicyCmptTypeAssociation policyCmptTypeAssociation = (IPolicyCmptTypeAssociation)candidate;
                if (!policyCmptTypeAssociation.isCompositionDetailToMaster()) {
                    continue;
                }
                IPolicyCmptTypeAssociation inverseAssociationOfCandidate = policyCmptTypeAssociation
                        .findInverseAssociation(getIpsProject());
                if (inverseAssociationOfCandidate == null) {
                    continue;
                }
                // test if the inverse is the subset of the derived union
                if (inverseAssociationOfCandidate.getSubsettedDerivedUnion().equals(derivedUnion.getName())) {
                    return true;
                }
                // TODO FIPS-85
                // if (ipsProject.getProperties().isUnsafeInverseAssociations()) {
                // if (inverseAssociationOfCandidate.equals(derivedUnion)) {
                // return true;
                // }
                // }
            }
            return false;
        }
    }

    private static class IsSubtypeOfVisitor extends TypeHierarchyVisitor {

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
        protected boolean visit(IType currentType) throws CoreException {
            if (currentType == supertypeCandidate) {
                subtype = true;
                return false;
            }
            return true;
        }

    }

    private static class NotDerivedAssociationCollector extends ProductCmptTypeHierarchyVisitor {

        public NotDerivedAssociationCollector(IIpsProject ipsProject) {
            super(ipsProject);
        }

        private List<IAssociation> associations = new ArrayList<IAssociation>();

        @Override
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            List<? extends IAssociation> typeAssociations = currentType.getAssociations();
            int index = 0;
            for (IAssociation association : typeAssociations) {
                /*
                 * To get the associations of the root type of the supertype hierarchy first, put in
                 * the list at first, but with unchanged order for all associations found in one
                 * type ...
                 */
                if (!association.isDerived()) {
                    associations.add(index, association);
                    index++;
                }
            }
            return true;
        }

    }

}
