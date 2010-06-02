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

package org.faktorips.devtools.core.internal.model.bf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.Bendpoint;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.bf.IDecisionBFE;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ControlFlow extends IpsObjectPart implements IControlFlow {

    private String conditionValue = ""; //$NON-NLS-1$

    private String targetId;

    private String sourceId;

    private List<Bendpoint> bendpoints = new ArrayList<Bendpoint>();

    public ControlFlow(IIpsObject parent, String id) {
        super(parent, id);
    }

    @Override
    public List<Bendpoint> getBendpoints() {
        return Collections.unmodifiableList(bendpoints);
    }

    @Override
    public void setBendpoint(int index, Bendpoint bendpoint) {
        if (bendpoint == null || bendpoints.contains(bendpoint)) {
            return;
        }
        bendpoints.set(index, bendpoint);
        objectHasChanged();
    }

    @Override
    public void addBendpoint(int index, Bendpoint bendpoint) {
        if (bendpoint == null || bendpoints.contains(bendpoint)) {
            return;
        }
        bendpoints.add(index, bendpoint);
        objectHasChanged();
    }

    @Override
    public void removeBendpoint(int index) {
        if (bendpoints.remove(index) != null) {
            objectHasChanged();
        }
    }

    @Override
    public String getConditionValue() {
        return conditionValue;
    }

    // TODO test
    @Override
    public void setConditionValue(String value) {
        String old = conditionValue;
        conditionValue = value;
        valueChanged(old, conditionValue);
    }

    @Override
    public IBusinessFunction getBusinessFunction() {
        return (IBusinessFunction)getParent();
    }

    @Override
    public IBFElement getTarget() {
        return getBusinessFunction().getBFElement(targetId);
    }

    @Override
    public void setTarget(IBFElement target) {
        if (targetId == null && target == null) {
            return;
        }
        if (targetId != null && target != null && targetId.equals(target.getId())) {
            return;
        }
        if (getTarget() != null) {
            getTarget().removeIncomingControlFlow(this);
        }
        targetId = (target == null) ? null : target.getId();
        objectHasChanged();
        if (getTarget() != null) {
            getTarget().addIncomingControlFlow(this);
        }
    }

    @Override
    public IBFElement getSource() {
        return getBusinessFunction().getBFElement(sourceId);
    }

    @Override
    public void setSource(IBFElement source) {
        if (sourceId == null && source == null) {
            return;
        }
        if (sourceId != null && source != null && sourceId.equals(source.getId())) {
            return;
        }
        if (getSource() != null) {
            getSource().removeOutgoingControlFlow(this);
        }
        sourceId = (source == null) ? null : source.getId();
        objectHasChanged();
        if (getSource() != null) {
            getSource().addOutgoingControlFlow(this);
        }
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute(PROPERTY_NAME);
        String sourceValue = element.getAttribute(PROPERTY_SOURCE);
        sourceId = StringUtils.isEmpty(sourceValue) ? null : sourceValue;
        String targetValue = element.getAttribute(PROPERTY_TARGET);
        targetId = StringUtils.isEmpty(targetValue) ? null : targetValue;
        // TODO test
        conditionValue = element.getAttribute(PROPERTY_CONDITION_VALUE);
        NodeList nl = element.getElementsByTagName("Bendpoint"); //$NON-NLS-1$
        bendpoints.clear();
        for (int i = 0; i < nl.getLength(); i++) {
            Element bendpointEl = (Element)nl.item(i);
            int locationX = Integer.parseInt(bendpointEl.getAttribute("locationX")); //$NON-NLS-1$
            int locationY = Integer.parseInt(bendpointEl.getAttribute("locationY")); //$NON-NLS-1$
            bendpoints.add(new AbsoluteBendpoint(locationX, locationY));
        }
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_SOURCE, sourceId == null ? "" : sourceId); //$NON-NLS-1$
        element.setAttribute(PROPERTY_TARGET, targetId == null ? "" : targetId); //$NON-NLS-1$
        // TODO test
        element.setAttribute(PROPERTY_CONDITION_VALUE, conditionValue);
        Document doc = element.getOwnerDocument();
        for (Bendpoint bendpoint : bendpoints) {
            Element bendpointEl = doc.createElement("Bendpoint"); //$NON-NLS-1$
            Point location = bendpoint.getLocation();
            bendpointEl.setAttribute("locationX", String.valueOf(location.x)); //$NON-NLS-1$
            bendpointEl.setAttribute("locationY", String.valueOf(location.y)); //$NON-NLS-1$
            element.appendChild(bendpointEl);
        }
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(IControlFlow.XML_TAG);
    }

    @Override
    public IIpsElement[] getChildren() {
        return new IIpsElement[0];
    }

    @Override
    protected IIpsObjectPart newPart(Element xmlTag, String id) {
        return null;
    }

    @Override
    protected void addPart(IIpsObjectPart part) {
        // Nothing to do.
    }

    @Override
    protected void reinitPartCollections() {
        // Nothing to do.
    }

    @Override
    protected void removePart(IIpsObjectPart part) {
        // Nothing to do.
    }

    @Override
    @SuppressWarnings("unchecked")
    public IIpsObjectPart newPart(Class partType) {
        return null;
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        IBFElement source = getSource();
        if (source != null
                && (source.getType().equals(BFElementType.DECISION) || source.getType().equals(
                        BFElementType.DECISION_METHODCALL))) {
            if (StringUtils.isEmpty(getConditionValue())) {
                list.add(new Message(MSGCODE_VALUE_NOT_SPECIFIED, Messages.ControlFlow_valueMustBeSpecified,
                        Message.ERROR, this));
                return;
            }
            DecisionBFE decisionSource = (DecisionBFE)source;
            ValueDatatype datatype = decisionSource.findDatatype(ipsProject);
            if (datatype != null) {
                if (!datatype.isParsable(getConditionValue())) {
                    String text = NLS.bind(Messages.ControlFlow_valueNotValid, new String[] { getConditionValue(),
                            datatype.getQualifiedName() });
                    list.add(new Message(MSGCODE_VALUE_NOT_VALID, text, Message.ERROR, this));
                }
            }
            validateDublicateValues(decisionSource, list);
        }
    }

    private void validateDublicateValues(IDecisionBFE decision, MessageList msgList) {
        List<IControlFlow> cfs = decision.getOutgoingControlFlow();
        for (IControlFlow controlFlow : cfs) {
            if (controlFlow == this) {
                continue;
            }
            if (StringUtils.isEmpty(controlFlow.getConditionValue()) || StringUtils.isEmpty(getConditionValue())) {
                continue;
            }
            if (controlFlow.getConditionValue().equals(getConditionValue())) {
                String text = NLS.bind(Messages.ControlFlow_duplicateControlFlowValue, new String[] {
                        decision.getName(), getConditionValue() });
                msgList.add(new Message(MSGCODE_DUBLICATE_VALUES, text, Message.ERROR, this));
            }
        }
    }

}
