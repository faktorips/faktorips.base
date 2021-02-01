/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.bf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.bf.BFElementType;
import org.faktorips.devtools.model.bf.IBFElement;
import org.faktorips.devtools.model.bf.IBusinessFunction;
import org.faktorips.devtools.model.bf.IControlFlow;
import org.faktorips.devtools.model.bf.IDecisionBFE;
import org.faktorips.devtools.model.bf.Location;
import org.faktorips.devtools.model.internal.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ControlFlow extends AtomicIpsObjectPart implements IControlFlow {

    private String conditionValue = ""; //$NON-NLS-1$

    private String targetId;

    private String sourceId;

    private List<Location> bendpoints = new ArrayList<Location>();

    public ControlFlow(IIpsObject parent, String id) {
        super(parent, id);
    }

    @Override
    public List<Location> getBendpoints() {
        return Collections.unmodifiableList(bendpoints);
    }

    @Override
    public void setBendpoint(int index, Location bendpoint) {
        if (bendpoint == null || bendpoints.contains(bendpoint)) {
            return;
        }
        bendpoints.set(index, bendpoint);
        objectHasChanged();
    }

    @Override
    public void addBendpoint(int index, Location bendpoint) {
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
            bendpoints.add(new Location(locationX, locationY));
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
        for (Location bendpoint : bendpoints) {
            Element bendpointEl = doc.createElement("Bendpoint"); //$NON-NLS-1$
            bendpointEl.setAttribute("locationX", String.valueOf(bendpoint.getX())); //$NON-NLS-1$
            bendpointEl.setAttribute("locationY", String.valueOf(bendpoint.getY())); //$NON-NLS-1$
            element.appendChild(bendpointEl);
        }
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(IControlFlow.XML_TAG);
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
                String text = NLS.bind(Messages.ControlFlow_duplicateControlFlowValue,
                        new String[] { decision.getName(), getConditionValue() });
                msgList.add(new Message(MSGCODE_DUBLICATE_VALUES, text, Message.ERROR, this));
            }
        }
    }

}
