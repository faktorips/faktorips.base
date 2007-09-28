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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.BaseIpsObject;
import org.faktorips.devtools.core.internal.model.IpsObjectPartCollection;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
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

    private String supertype = "";

    private boolean abstractFlag;
    
    protected IpsObjectPartCollection methods;

    /**
     * @param file
     */
    public Type(IIpsSrcFile file) {
        super(file);
        methods = createCollectionForMethods();
    }

    /**
     * Faktory method to create the collection holding the methods.
     */
    protected abstract IpsObjectPartCollection createCollectionForMethods();
    
    protected Iterator getIteratorForMethods(){
        return methods.iterator();
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
    public IMethod[] findOverrideMethodCandidates(boolean onlyAbstractMethods, IIpsProject project) throws CoreException {
        MethodOverrideCandidatesFinder finder = new MethodOverrideCandidatesFinder(project, onlyAbstractMethods);
        finder.start(this);
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
            if (thisMethod.isSame(method)) {
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
        element.setAttribute(PROPERTY_ABSTRACT, "" + abstractFlag);
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
    }

    private void validateSupertype(MessageList list, IIpsProject ipsProject) throws CoreException {
        IType supertypeObj = findSupertype(ipsProject);
        if (supertypeObj==null) {
            String text = "The supertype " + supertype + " can't be found";
            list.add(new Message(MSGCODE_SUPERTYPE_NOT_FOUND, text, Message.ERROR, this, IType.PROPERTY_SUPERTYPE));
        } else {
            SupertypesCollector collector = new SupertypesCollector(ipsProject);
            collector.start(supertypeObj);
            if (collector.cycleDetected()) {
                String msg = "Cycle detected in type hierarchy.";
                list.add(new Message(MSGCODE_CYCLE_IN_TYPE_HIERARCHY, msg.toString(), Message.ERROR, this, IType.PROPERTY_SUPERTYPE));
            } else {
                for (Iterator it=collector.supertypes.iterator(); it.hasNext(); ) {
                    IType supertype = (IType)it.next();
                    MessageList superResult = supertype.validate();
                    if (!superResult.isEmpty()) {
                        if (superResult.getMessageByCode(IType.MSGCODE_SUPERTYPE_NOT_FOUND)!=null) {
                            String text = "The type's hierarchy is inconsistent.";
                            list.add(new Message(MSGCODE_INCONSISTENT_TYPE_HIERARCHY, text, Message.ERROR, this, PROPERTY_SUPERTYPE));
                            return;
                        }
                    }
                }
            }
        }
    }
    
    public void dependsOn(Set result) throws CoreException {
        // TODO v2 - add dependencies for method parameters
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
        private boolean onlyAbstractMethods;
        
        public MethodOverrideCandidatesFinder(IIpsProject ipsProject, boolean onlyAbstractMethods) {
            super(ipsProject);
            this.onlyAbstractMethods = onlyAbstractMethods;
        }
        
        public IMethod[] getCandidates() {
            return (IMethod[])candidates.toArray(new IMethod[candidates.size()]);
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IType currentType) throws CoreException {
            IMethod[] supertypeMethods = currentType.getMethods();
            for (int i = 0; i < supertypeMethods.length; i++) {
                if (!hasSameMethod(supertypeMethods[i])) {
                    if (!onlyAbstractMethods || supertypeMethods[i].isAbstract()) {
                        // candidate found, but it might be already in the list
                        if (!sameMethodAlreadyInCandidateList(supertypeMethods[i], candidates)) {
                            candidates.add(supertypeMethods[i]);
                        }
                    }
                }
            }
            return true;
        }

        private boolean sameMethodAlreadyInCandidateList(IMethod method, List candidates) {
            for (Iterator it = candidates.iterator(); it.hasNext();) {
                IMethod candidate = (IMethod)it.next();
                if (method.isSame(candidate)) {
                    return true;
                }
            }
            return false;
        }

    }
}
