/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.ipsobject.BaseIpsObject;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
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
    
    protected IpsObjectPartCollection methods;
    protected IpsObjectPartCollection associations;
    protected IpsObjectPartCollection attributes;
    
    public Type(IIpsSrcFile file) {
        super(file);
        attributes = createCollectionForAttributes();
        associations = createCollectionForAssociations();
        methods = createCollectionForMethods();
    }

    /**
     * Factory method to create the collection holding the methods.
     */
    protected abstract IpsObjectPartCollection createCollectionForMethods();
    
    /**
     * Factory method to create the collection holding the associations.
     */
    protected abstract IpsObjectPartCollection createCollectionForAssociations();

    /**
     * Factory method to create the collection holding the attributes.
     */
    protected abstract IpsObjectPartCollection createCollectionForAttributes();

    protected Iterator getIteratorForMethods(){
        return methods.iterator();
    }
    
    protected Iterator getIteratorForAssociations(){
        return associations.iterator();
    }

    protected Iterator getIteratorForAttributes(){
        return attributes.iterator();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAbstract() {
        return abstractFlag;
    }

    /**
     * {@inheritDoc}
     */
    public void setAbstract(boolean newValue) {
        boolean oldValue = abstractFlag;
        abstractFlag = newValue;
        valueChanged(oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    public String getSupertype() {
        return supertype;
    }
    
    /**
     * {@inheritDoc}
     */
    public IType findSupertype(IIpsProject ipsProject) throws CoreException {
        return (IType)ipsProject.findIpsObject(getIpsObjectType(), supertype);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasSupertype() {
        return StringUtils.isNotEmpty(supertype);
    }

    /**
     * {@inheritDoc}
     */
    public void setSupertype(String newSupertype) {
        String oldSupertype = supertype;
        supertype = newSupertype;
        valueChanged(oldSupertype, newSupertype);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isSubtypeOf(IType supertypeCandidate, IIpsProject project) throws CoreException {
        if (supertypeCandidate==null) {
            return false;
        }
        IType supertype = findSupertype(project);
        if (supertype==null) {
            return false;
        }
        if (supertypeCandidate.equals(supertype)) {
            return true;
        }
        return supertype.isSubtypeOf(supertypeCandidate, project);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSubtypeOrSameType(IType candidate, IIpsProject project) throws CoreException {
        if (this.equals(candidate)) {
            return true;
        }
        return isSubtypeOf(candidate, project);
    }

    /**
     * {@inheritDoc}
     */
    public IAttribute[] getAttributes() {
        return (IAttribute[])attributes.toArray(new IAttribute[attributes.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public IAttribute getAttribute(String name) {
        return (IAttribute)attributes.getPartByName(name);
    }
    
    /**
     * {@inheritDoc}
     */
    public IAttribute[] findAllAttributes() throws CoreException {
        AllAttributeFinder finder = new AllAttributeFinder(getIpsProject());
        finder.start(this);
        return finder.getAttributes();
    }

    /**
     * {@inheritDoc}
     */
    public IAttribute findAttribute(String name, IIpsProject project) throws CoreException {
        AttributeFinder finder = new AttributeFinder(project, name);
        finder.start(this);
        return finder.attribute;
    }

    /**
     * {@inheritDoc}
     */
    public IAttribute newAttribute() {
        return (IAttribute)attributes.newPart();
    }

    /**
     * {@inheritDoc}
     */
    public int getNumOfAttributes() {
        return attributes.size();
    }

    /**
     * {@inheritDoc}
     */
    public int[] moveAttributes(int[] indexes, boolean up) {
        return attributes.moveParts(indexes, up);
    }

    /**
     * {@inheritDoc}
     */
    public IAssociation findAssociation(String name, IIpsProject project) throws CoreException {
        AssociationFinder finder = new AssociationFinder(project, name);
        finder.start(this);
        return finder.association;
    }

    /**
     * {@inheritDoc}
     */
    public IAssociation getAssociation(String name) {
        return (IAssociation)associations.getPartByName(name);
    }

    /**
     * {@inheritDoc}
     */
    public IAssociation[] getAssociationsForTarget(String target) {
        List result = new ArrayList();
        for (Iterator it = associations.iterator(); it.hasNext();) {
            IAssociation association = (IAssociation)it.next();
            if (association.getTarget().equals(target)) {
                result.add(association);
            }
        }
        return (IAssociation[])result.toArray(new IAssociation[result.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public IAssociation[] getAssociations() {
        return (IAssociation[])associations.toArray(new IAssociation[associations.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public int getNumOfAssociations() {
        return associations.size();
    }

    /**
     * {@inheritDoc}
     */
    public int[] moveAssociations(int[] indexes, boolean up) {
        return associations.moveParts(indexes, up);
    }

    /**
     * {@inheritDoc}
     */
    public IAssociation newAssociation() {
        return (IAssociation)associations.newPart();
    }

    /**
     * {@inheritDoc}
     */
    public IMethod newMethod() {
        return (IMethod)methods.newPart();
    }
    
    /**
     * {@inheritDoc}
     */
    public IMethod[] getMethods() {
        return (IMethod[])methods.toArray(new IMethod[methods.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public IMethod getMethod(String methodName, String[] datatypes) {
        if (datatypes==null) {
            datatypes = new String[0];
        }
        for (Iterator it=methods.iterator(); it.hasNext(); ) {
            IMethod method = (IMethod)it.next();
            if (!method.getName().equals(methodName)) {
                continue;
            }
            IParameter[] params = method.getParameters();
            if (params.length!=datatypes.length) {
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

    /**
     * {@inheritDoc}
     */
    public IMethod findMethod(String name, String[] datatypes, IIpsProject ipsProject) throws CoreException {
        MethodFinderByNameAndParamtypes finder = new MethodFinderByNameAndParamtypes(ipsProject, name, datatypes);
        finder.start(this);
        return finder.method;
    }

    /**
     * {@inheritDoc}
     */
    public IMethod getMethod(String signature) {
        for (Iterator it=methods.iterator(); it.hasNext(); ) {
            IMethod method = (IMethod)it.next();
            if (method.getSignatureString().equals(signature)) {
                return method;
            }
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    public IMethod findMethod(String signature, IIpsProject ipsProject) throws CoreException {
        MethodFinderBySignature finder = new MethodFinderBySignature(ipsProject, signature);
        finder.start(this);
        return finder.method;
    }

    /**
     * {@inheritDoc}
     */
    public int getNumOfMethods() {
        return methods.size();
    }

    /**
     * {@inheritDoc}
     */
    public int[] moveMethods(int[] indexes, boolean up) {
        return methods.moveParts(indexes, up);
    }
    
    /**
     * {@inheritDoc}
     */
    public IMethod[] findOverrideMethodCandidates(boolean onlyNotImplementedAbstractMethods, IIpsProject ipsProject) throws CoreException {
        MethodOverrideCandidatesFinder finder = new MethodOverrideCandidatesFinder(ipsProject, onlyNotImplementedAbstractMethods);
        finder.start(findSupertype(ipsProject));
        return finder.getCandidates();
    }
    
    /**
     * {@inheritDoc}
     */
    public IMethod[] overrideMethods(IMethod[] methods) {
        IMethod[] newMethods = new IMethod[methods.length];
        for (int i = 0; i < methods.length; i++) {
            IMethod override = newMethod();
            override.setModifier(methods[i].getModifier());
            override.setAbstract(false);
            override.setDatatype(methods[i].getDatatype());
            override.setName(methods[i].getName());
            IParameter[] params = methods[i].getParameters();
            for (int j = 0; j < params.length; j++) {
                IParameter newParam = override.newParameter();
                newParam.setName(params[j].getName());
                newParam.setDatatype(params[j].getDatatype());
            }
            newMethods[i] = override;
        }
        return newMethods;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasSameMethod(IMethod method) {
        return getMatchingMethod(method) != null;
    }

    /**
     * {@inheritDoc}
     */
    public IMethod getMatchingMethod(IMethod method) {
        for (Iterator it = this.methods.iterator(); it.hasNext();) {
            IMethod thisMethod = (IMethod)it.next();
            if (thisMethod.overrides(method)) {
                return thisMethod;
            }
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        supertype = element.getAttribute(PROPERTY_SUPERTYPE);
        abstractFlag = Boolean.valueOf(element.getAttribute(PROPERTY_ABSTRACT)).booleanValue();
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_SUPERTYPE, supertype);
        element.setAttribute(PROPERTY_ABSTRACT, "" + abstractFlag); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);
        IIpsProject ipsProject = getIpsProject();
        if (hasSupertype()) {
            validateSupertype(list, ipsProject);
        }
        if (!isAbstract()) {
            validateIfAllAbstractMethodsAreImplemented(getIpsProject(), list);
            IIpsProjectProperties props = getIpsProject().getProperties();
            if (props.isDerivedUnionIsImplementedRuleEnabled()) {
                DerivedUnionsSpecifiedValidator validator = new DerivedUnionsSpecifiedValidator(list, ipsProject);
                validator.start(this);
            }
            IMethod[] methods = getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].isAbstract()) {
                    String text = Messages.Type_msg_AbstractMissmatch;
                    list.add(new Message(MSGCODE_ABSTRACT_MISSING, text, Message.ERROR, this, PROPERTY_ABSTRACT)); //$NON-NLS-1$
                    break;
                }
            }
        }
        
    }

    private void validateSupertype(MessageList list, IIpsProject ipsProject) throws CoreException {
        IType supertypeObj = findSupertype(ipsProject);
        if (supertypeObj==null) {
            String text = NLS.bind(Messages.Type_msg_supertypeNotFound, supertype);
            list.add(new Message(MSGCODE_SUPERTYPE_NOT_FOUND, text, Message.ERROR, this, IType.PROPERTY_SUPERTYPE));
        } else {
            SupertypesCollector collector = new SupertypesCollector(ipsProject);
            collector.start(supertypeObj);
            if (collector.cycleDetected()) {
                String msg = Messages.Type_msg_cycleInTypeHierarchy;
                list.add(new Message(MSGCODE_CYCLE_IN_TYPE_HIERARCHY, msg.toString(), Message.ERROR, this, IType.PROPERTY_SUPERTYPE));
            } else {
                for (Iterator it=collector.supertypes.iterator(); it.hasNext(); ) {
                    IType supertype = (IType)it.next();
                    MessageList superResult = supertype.validate();
                    if (!superResult.isEmpty()) {
                        if (superResult.getMessageByCode(IType.MSGCODE_SUPERTYPE_NOT_FOUND)!=null) {
                            String text = Messages.Type_msg_TypeHierarchyInconsistent;
                            list.add(new Message(MSGCODE_INCONSISTENT_TYPE_HIERARCHY, text, Message.ERROR, this, PROPERTY_SUPERTYPE));
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

        IMethod[] methods = findOverrideMethodCandidates(true, ipsProject);
        for (int i = 0; i < methods.length; i++) {
            String text = NLS.bind(Messages.Type_msg_MustOverrideAbstractMethod, methods[i].getName(),
                    methods[i].getType().getQualifiedName());
            list.add(new Message(MSGCODE_MUST_OVERRIDE_ABSTRACT_METHOD, text, Message.ERROR, this));
        }
    }

    /**
     * {@inheritDoc}
     */
    // Implementation of the Datatype interface.
    public boolean isVoid() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    // Implementation of the Datatype interface.
    public boolean isPrimitive() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    // Implementation of the Datatype interface.
    public boolean isValueDatatype() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    // Implementation of the Datatype interface.
    public int compareTo(Object o) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    // Implementation of the Datatype interface.
    public String getJavaClassName() {
        throw new RuntimeException("getJavaClassName is not supported by " + getClass()); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    // Implementation of the Datatype interface.
    public boolean hasNullObject() {
        return false;
    }

    public void dependsOn(Set result) throws CoreException {
        // TODO v2 - dependeny - add dependencies for method parameters
//      private void addQualifiedNameTypesForFormulaParameters(Set qualifiedNameTypes) throws CoreException {
//          IAttribute[] attributes = getAttributes();
//          IIpsProject ipsProject = getIpsProject();
//          for (int i = 0; i < attributes.length; i++) {
//              if (ConfigElementType.FORMULA.equals(attributes[i].getConfigElementType())) {
//                  Parameter[] parameters = attributes[i].getFormulaParameters();
//                  for (int j = 0; j < parameters.length; j++) {
//                      String datatypeId = parameters[j].getDatatype();
//                      Datatype datatype = ipsProject.findDatatype(datatypeId);
//                      if (datatype instanceof ValueDatatype) {
//                          // no dependency
//                      } else if (datatype instanceof IIpsObject) {
//                          IIpsObject ipsObject = (IIpsObject)datatype;
//                          qualifiedNameTypes.add(ipsObject.getQualifiedNameType());
//                      } else {
//                          for (int k = 0; k < IpsObjectType.ALL_TYPES.length; k++) {
//                              if (IpsObjectType.ALL_TYPES[k].isDatatype()) {
//                                  qualifiedNameTypes.add(new QualifiedNameType(datatypeId, IpsObjectType.ALL_TYPES[k]));
//                              }
//                          }
//                      }
//                  }
//              }
//          }
//      }
        
    }
    
    class SupertypesCollector extends TypeHierarchyVisitor {

        private List supertypes = new ArrayList();
        
        public SupertypesCollector(IIpsProject ipsProject) {
            super(ipsProject);
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IType currentType) throws CoreException {
            supertypes.add(currentType);
            return true;
        }
    }

    class MethodOverrideCandidatesFinder extends TypeHierarchyVisitor {

        private List candidates = new ArrayList();
        private boolean onlyNotImplementedAbstractMethods;
        
        public MethodOverrideCandidatesFinder(IIpsProject ipsProject, boolean onlyNotImplementedAbstractMethods) {
            super(ipsProject);
            this.onlyNotImplementedAbstractMethods = onlyNotImplementedAbstractMethods;
        }
        
        public IMethod[] getCandidates() {
            return (IMethod[])candidates.toArray(new IMethod[candidates.size()]);
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IType currentType) throws CoreException {
            IMethod[] typeMethods = currentType.getMethods();
            for (int i = 0; i < typeMethods.length; i++) {
                if (onlyNotImplementedAbstractMethods && !typeMethods[i].isAbstract()) {
                    continue;
                }
                IMethod overridingMethod = typeMethods[i].findOverridingMethod(Type.this, ipsProject);
                if (overridingMethod!=null && overridingMethod.getType()==Type.this) {
                    continue;
                }
                if (overridingMethod==null || (!onlyNotImplementedAbstractMethods)) {
                    // candidate found, but it might be already in the list
                    if (!sameMethodAlreadyInCandidateList(typeMethods[i], candidates)) {
                        candidates.add(typeMethods[i]);
                    }
                }
            }
            return true;
        }

        private boolean sameMethodAlreadyInCandidateList(IMethod method, List candidates) {
            for (Iterator it = candidates.iterator(); it.hasNext();) {
                IMethod candidate = (IMethod)it.next();
                if (method.overrides(candidate)) {
                    return true;
                }
            }
            return false;
        }

    }
    
    class AssociationFinder extends TypeHierarchyVisitor {

        private String associationName;
        private IAssociation association = null;
        
        public AssociationFinder(IIpsProject project, String associationName) {
            super(project);
            this.associationName = associationName;
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IType currentType) {
            association = currentType.getAssociation(associationName);
            return association==null;
        }
        
    }
    
    private static class AttributeFinder extends TypeHierarchyVisitor {

        private String attributeName;
        private IAttribute attribute;
        
        public AttributeFinder(IIpsProject ipsProject, String attrName) {
            super(ipsProject);
            this.attributeName = attrName;
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IType currentType) throws CoreException {
            attribute = currentType.getAttribute(attributeName);
            return attribute==null;
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

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IType currentType) throws CoreException {
            method = currentType.getMethod(methodName, datatypes);
            return method==null;
        }
    }

    private static class MethodFinderBySignature extends TypeHierarchyVisitor {

        private String signature;
        private IMethod method;
        
        public MethodFinderBySignature(IIpsProject ipsProject, String signature) {
            super(ipsProject);
            this.signature = signature;
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IType currentType) throws CoreException {
            method = currentType.getMethod(signature);
            return method==null;
        }
    }

    private static class AllAttributeFinder extends TypeHierarchyVisitor {

        private List attributes;
        private Set attributeNames;
        
        public AllAttributeFinder(IIpsProject ipsProject) {
            super(ipsProject);
            attributes = new ArrayList();
            attributeNames = new HashSet();
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IType currentType) throws CoreException {
            IAttribute[] lattributes = currentType.getAttributes();
            //considers overridden attributes
            for (int i = 0; i < lattributes.length; i++) {
                if(!attributeNames.contains(lattributes[i].getName())){
                    attributeNames.add(lattributes[i].getName());
                    attributes.add(lattributes[i]);
                }
            }
            return true;
        }
        
        private IAttribute[] getAttributes(){
            return (IAttribute[])attributes.toArray(new IAttribute[attributes.size()]);
        }
    }

    private class DerivedUnionsSpecifiedValidator extends TypeHierarchyVisitor {

        private MessageList msgList;
        private List candidateSubsets = new ArrayList(0);

        public DerivedUnionsSpecifiedValidator(MessageList msgList, IIpsProject ipsProject) {
            super(ipsProject);
            this.msgList = msgList;
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IType currentType) throws CoreException {
            IAssociation[] associations = currentType.getAssociations();
            for (int i = 0; i < associations.length; i++) {
                candidateSubsets.add(associations[i]);
            }
            for (int i = 0; i < associations.length; i++) {
                if (associations[i].isDerivedUnion()) {
                    if (!isSubsetted(associations[i])) {
                        String text = NLS.bind(Messages.Type_msg_MustImplementDerivedUnion,
                                associations[i].getName(), associations[i].getType().getQualifiedName());
                        msgList.add(new Message(IType.MSGCODE_MUST_SPECIFY_DERIVED_UNION, text,
                                Message.ERROR, Type.this, IType.PROPERTY_ABSTRACT));
                    }
                }
            }
            return true;
        }

        private boolean isSubsetted(IAssociation derivedUnion) throws CoreException {
            for (Iterator it = candidateSubsets.iterator(); it.hasNext();) {
                IAssociation candidate = (IAssociation)it.next();
                if (derivedUnion == candidate.findSubsettedDerivedUnion(ipsProject)) {
                    return true;
                }
            }
            return false;
        }

    }
}
