/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.method;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.internal.dependency.DatatypeDependency;
import org.faktorips.devtools.model.internal.type.Messages;
import org.faktorips.devtools.model.internal.type.TypePart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.method.IBaseMethod;
import org.faktorips.devtools.model.method.IParameter;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.ITypePart;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of the published interface.
 * <p>
 * This implementation extends the {@link TypePart} and also should extend {@link BaseMethod}.
 * Because of the lack of multiple inheritance in Java there is a delegation to an instance of
 * method.
 * <p>
 * Especially the methods to read and create the xml use the delegation to keep the xml of the
 * {@link IType types} compatible to the previous releases.
 * 
 * @author Jan Ortmann
 */
public abstract class Method extends TypePart implements IMethod {

    private boolean abstractFlag = false;

    private final BaseMethod method;

    public Method(IType parent, String id) {
        super(parent, id);
        method = new BaseMethod(this, id);
    }

    @Override
    public void setName(String newName) {
        method.setName(newName);
    }

    @Override
    public String getName() {
        return method.getName();
    }

    @Override
    public boolean isAbstract() {
        return abstractFlag;
    }

    @Override
    public void setAbstract(boolean newValue) {
        boolean oldValue = isAbstract();
        abstractFlag = newValue;
        valueChanged(oldValue, newValue);
    }

    @Override
    public String getDatatype() {
        return method.getDatatype();
    }

    @Override
    public Datatype findDatatype(IIpsProject ipsProject) {
        return method.findDatatype(ipsProject);
    }

    @Override
    public void setDatatype(String newDatatype) {
        method.setDatatype(newDatatype);
    }

    @Override
    public int getJavaModifier() {
        return getModifier().getJavaModifier();
    }

    @Override
    public IMethod findOverridingMethod(IType typeToSearchFrom, IIpsProject ipsProject) {
        if (!typeToSearchFrom.isSubtypeOf(getType(), ipsProject)) {
            return null;
        }
        OverridingMethodFinder finder = new OverridingMethodFinder(ipsProject);
        finder.start(typeToSearchFrom);
        return finder.overridingMethod;
    }

    @Override
    public IMethod findOverriddenMethod(IIpsProject ipsProject) {
        OverridingMethodFinder finder = new OverridingMethodFinder(ipsProject);
        finder.start(getType());
        return finder.overridingMethod;
    }

    @Override
    public boolean isSameSignature(IBaseMethod other) {
        return method.isSameSignature(other);
    }

    @Override
    public boolean overrides(IMethod other) {
        if (this.equals(other)) {
            return false;
        }
        if (!isSameSignature(other)) {
            return false;
        }
        if (!getType().isSubtypeOf(other.getType(), other.getIpsProject())) {
            return false;
        }
        return true;
    }

    @Override
    public String getSignatureString() {
        return method.getSignatureString();
    }

    @Override
    public void initFromXml(Element element) {
        method.initFromXml(element);
        super.initFromXml(element);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        initAbstractFromXml(element);
    }

    private void initAbstractFromXml(Element element) {
        String abstractString = element.getAttribute(PROPERTY_ABSTRACT);
        if (abstractString != null) {
            abstractFlag = Boolean.valueOf(abstractString).booleanValue();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * This implementation calls {@link IBaseMethod#toXml(Document)} in order to keep the xml of the
     * {@link IType types} compatible to previous releases. The properties and parts, which are
     * delegated to the {@link #method} are already within the returned element.
     */
    @Override
    protected Element createElement(Document doc) {
        return method.toXml(doc);
    }

    @Override
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_ABSTRACT, Boolean.toString(isAbstract()));
    }

    @Override
    protected void validateThis(MessageList result, IIpsProject ipsProject) {
        super.validateThis(result, ipsProject);
        result.add(method.validate(ipsProject));
        if (isAbstract() && !getType().isAbstract()) {
            result.add(new Message(
                    "", MessageFormat.format(Messages.TypeMethod_msg_abstractMethodError, getName()), Message.ERROR, //$NON-NLS-1$
                    this,
                    PROPERTY_ABSTRACT));
        }
        if (validateDuplicateMethodInSameType(result)) {
            validateOverriddenMethod(result, ipsProject);
        }
    }

    private void validateOverriddenMethod(MessageList list, IIpsProject ipsProject) {
        IMethod overridden = findOverriddenMethod(ipsProject);
        if (overridden == null) {
            return;
        }
        validateModifierOfOverriddenMethod(list, overridden);
        validateReturnTypeOfOverriddenMethod(list, overridden, ipsProject);
    }

    private void validateModifierOfOverriddenMethod(MessageList list, IMethod overridden) {
        if (!getModifier().equals(overridden.getModifier())) {
            list.add(Message.newError(MSGCODE_MODIFIER_NOT_EQUAL,
                    MessageFormat.format(Messages.TypeMethod_msg_modifierOverriddenNotEqual,
                            overridden.getModifier().getId()),
                    this, ITypePart.PROPERTY_MODIFIER));
        }
    }

    private void validateReturnTypeOfOverriddenMethod(MessageList list, IMethod overridden, IIpsProject ipsProject) {
        Datatype returnType = findDatatype(ipsProject);
        if (returnType == null) {
            return;
        }
        Datatype overriddenReturnType = overridden.findDatatype(ipsProject);
        if (!returnType.equals(overriddenReturnType)) {
            String text = MessageFormat.format(Messages.TypeMethod_incompatbileReturnType, overridden.getType()
                    .getUnqualifiedName(), overridden.getSignatureString());
            Message msg = Message.newError(MSGCODE_RETURN_TYPE_IS_INCOMPATIBLE, text, this,
                    IBaseMethod.PROPERTY_DATATYPE);
            list.add(msg);
        }
    }

    private boolean validateDuplicateMethodInSameType(MessageList msgList) {
        List<IMethod> methods = getType().getMethods();
        String thisSignature = getSignatureString();
        for (IBaseMethod formulaMethod : methods) {
            if (this.equals(formulaMethod)) {
                continue;
            }
            if (formulaMethod.getSignatureString().equals(thisSignature)) {
                msgList.add(new Message(MSGCODE_DUBLICATE_SIGNATURE, Messages.TypeMethod_duplicateSignature,
                        Message.ERROR, this));
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getType().getQualifiedName());
        builder.append(": "); //$NON-NLS-1$
        builder.append(method.toString());
        return builder.toString();
    }

    public void dependsOn(Set<IDependency> dependencies, Map<IDependency, List<IDependencyDetail>> details) {
        IDependency dependency = new DatatypeDependency(getType().getQualifiedNameType(), getDatatype());
        dependencies.add(dependency);
        addDetails(details, dependency, this, PROPERTY_DATATYPE);
        for (IParameter parameter : getParameters()) {
            dependency = new DatatypeDependency(getType().getQualifiedNameType(), parameter.getDatatype());
            dependencies.add(dependency);
            addDetails(details, dependency, parameter, IParameter.PROPERTY_DATATYPE);
        }
    }

    @Override
    public IParameter[] getParameters() {
        return method.getParameters();
    }

    @Override
    public String[] getParameterNames() {
        return method.getParameterNames();
    }

    @Override
    public List<Datatype> getParameterDatatypes() {
        return method.getParameterDatatypes();
    }

    @Override
    public int getNumOfParameters() {
        return method.getNumOfParameters();
    }

    @Override
    public IParameter newParameter() {
        return method.newParameter();
    }

    @Override
    public IParameter newParameter(String datatype, String name) {
        return method.newParameter(datatype, name);
    }

    @Override
    public int[] moveParameters(int[] indices, boolean up) {
        return method.moveParameters(indices, up);
    }

    class OverridingMethodFinder extends TypeHierarchyVisitor<IType> {

        private IMethod overridingMethod;

        public OverridingMethodFinder(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IType currentType) {
            IMethod match = currentType.getMatchingMethod(Method.this);
            if (match != null && match != Method.this) {
                overridingMethod = match;
                return false;
            }
            return true;
        }

    }
}
