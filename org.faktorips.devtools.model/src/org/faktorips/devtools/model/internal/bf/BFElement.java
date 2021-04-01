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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.model.bf.BFElementType;
import org.faktorips.devtools.model.bf.IBFElement;
import org.faktorips.devtools.model.bf.IBusinessFunction;
import org.faktorips.devtools.model.bf.IControlFlow;
import org.faktorips.devtools.model.bf.Location;
import org.faktorips.devtools.model.bf.Size;
import org.faktorips.devtools.model.internal.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class BFElement extends AtomicIpsObjectPart implements IBFElement {

    private Location location;

    private BFElementType type;

    private Size size = new Size(100, 60);

    private List<String> incommingControlFlows = new ArrayList<String>();

    private List<String> outgoingControlFlows = new ArrayList<String>();

    public BFElement(IIpsObject parent, String id) {
        super(parent, id);
    }

    @Override
    public String getDisplayString() {
        return getName();
    }

    @Override
    public BFElementType getType() {
        return type;
    }

    void setType(BFElementType type) {
        // No event triggering since this method is only call at object creation time.
        this.type = type;
    }

    @Override
    public void addIncomingControlFlow(IControlFlow controlFlow) {
        if (incommingControlFlows.contains(controlFlow.getId())) {
            return;
        }
        incommingControlFlows.add(controlFlow.getId());
        objectHasChanged();
        controlFlow.setTarget(this);
    }

    @Override
    public List<IControlFlow> getIncomingControlFlow() {
        ArrayList<IControlFlow> inList = new ArrayList<IControlFlow>();
        List<IControlFlow> controlFlowList = getBusinessFunction().getControlFlows();
        for (IControlFlow controlFlow : controlFlowList) {
            for (String id : incommingControlFlows) {
                if (id.equals(controlFlow.getId())) {
                    inList.add(controlFlow);
                }
            }
        }
        return inList;
    }

    @Override
    public List<IControlFlow> getAllControlFlows() {
        List<IControlFlow> allEdges = new ArrayList<IControlFlow>();
        allEdges.addAll(getIncomingControlFlow());
        allEdges.addAll(getOutgoingControlFlow());
        return allEdges;
    }

    @Override
    public boolean removeIncomingControlFlow(IControlFlow controlFlow) {
        if (incommingControlFlows.remove(controlFlow.getId())) {
            objectHasChanged();
            controlFlow.setTarget(null);
            return true;
        }
        return false;
    }

    @Override
    public void removeAllIncommingControlFlows() {
        incommingControlFlows.clear();
    }

    @Override
    public void removeAllOutgoingControlFlows() {
        outgoingControlFlows.clear();
    }

    @Override
    public void addOutgoingControlFlow(IControlFlow controlFlow) {
        if (outgoingControlFlows.contains(controlFlow.getId())) {
            return;
        }
        outgoingControlFlows.add(controlFlow.getId());
        objectHasChanged();
        controlFlow.setSource(this);
    }

    @Override
    public List<IControlFlow> getOutgoingControlFlow() {
        ArrayList<IControlFlow> outList = new ArrayList<IControlFlow>();
        List<IControlFlow> controlFlowList = getBusinessFunction().getControlFlows();
        for (IControlFlow controlFlow : controlFlowList) {
            for (String id : outgoingControlFlows) {
                if (id.equals(controlFlow.getId())) {
                    outList.add(controlFlow);
                }
            }
        }
        return outList;
    }

    @Override
    public boolean removeOutgoingControlFlow(IControlFlow controlFlow) {
        if (outgoingControlFlows.remove(controlFlow.getId())) {
            objectHasChanged();
            controlFlow.setSource(null);
            return true;
        }
        return false;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void setLocation(Location location) {
        Location old = this.location;
        this.location = location;
        valueChanged(old, location);
    }

    @Override
    public Size getSize() {
        return size;
    }

    @Override
    public void setSize(Size size) {
        Size old = this.size;
        this.size = size;
        valueChanged(old, size);
    }

    @Override
    public void setName(String name) {
        String old = this.name;
        this.name = name;
        valueChanged(old, name);
    }

    @Override
    public IBusinessFunction getBusinessFunction() {
        return (IBusinessFunction)getParent();
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute(PROPERTY_NAME);
        type = BFElementType.getType(element.getAttribute(PROPERTY_TYPE));
        NodeList nl = element.getElementsByTagName("Location"); //$NON-NLS-1$
        for (int i = 0; i < nl.getLength(); i++) {
            Element posElement = (Element)nl.item(i);
            String xPos = posElement.getAttribute("xlocation"); //$NON-NLS-1$
            String yPos = posElement.getAttribute("ylocation"); //$NON-NLS-1$
            location = new Location(Integer.parseInt(xPos), Integer.parseInt(yPos));
        }
        nl = element.getElementsByTagName("Size"); //$NON-NLS-1$
        for (int i = 0; i < nl.getLength(); i++) {
            Element posElement = (Element)nl.item(i);
            String width = posElement.getAttribute("width"); //$NON-NLS-1$
            String height = posElement.getAttribute("height"); //$NON-NLS-1$
            size = new Size(Integer.parseInt(width), Integer.parseInt(height));
        }
        nl = element.getElementsByTagName("ControlFlow"); //$NON-NLS-1$
        incommingControlFlows.clear();
        outgoingControlFlows.clear();
        for (int i = 0; i < nl.getLength(); i++) {
            Element posElement = (Element)nl.item(i);
            String type = posElement.getAttribute("type"); //$NON-NLS-1$
            String controlFlowId = posElement.getAttribute("id"); //$NON-NLS-1$
            if (type.equals("in")) { //$NON-NLS-1$
                incommingControlFlows.add(controlFlowId);
            }
            if (type.equals("out")) { //$NON-NLS-1$
                outgoingControlFlows.add(controlFlowId);
            }
        }
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_TYPE, type.getId());
        Document doc = element.getOwnerDocument();

        Element locationEl = doc.createElement("Location"); //$NON-NLS-1$
        locationEl.setAttribute("xlocation", String.valueOf(getLocation().getX())); //$NON-NLS-1$
        locationEl.setAttribute("ylocation", String.valueOf(getLocation().getY())); //$NON-NLS-1$
        element.appendChild(locationEl);

        Element sizeEl = doc.createElement("Size"); //$NON-NLS-1$
        sizeEl.setAttribute("width", String.valueOf(getSize().getWidth())); //$NON-NLS-1$
        sizeEl.setAttribute("height", String.valueOf(getSize().getHeight())); //$NON-NLS-1$
        element.appendChild(sizeEl);

        for (String controlFlowId : outgoingControlFlows) {
            Element controlFlowEl = doc.createElement("ControlFlow"); //$NON-NLS-1$
            element.appendChild(controlFlowEl);
            controlFlowEl.setAttribute("type", "out"); //$NON-NLS-1$ //$NON-NLS-2$
            controlFlowEl.setAttribute("id", String.valueOf(controlFlowId)); //$NON-NLS-1$
        }

        for (String controlFlowId : incommingControlFlows) {
            Element controlFlowEl = doc.createElement("ControlFlow"); //$NON-NLS-1$
            element.appendChild(controlFlowEl);
            controlFlowEl.setAttribute("type", "in"); //$NON-NLS-1$ //$NON-NLS-2$
            controlFlowEl.setAttribute("id", String.valueOf(controlFlowId)); //$NON-NLS-1$
        }
    }

    /**
     * Can be used within subclass validation methods to validate the name of the business function
     * element. This method validates if the name has been specified and if the name is valid
     * according to java naming conventions.
     * 
     * @throws CoreException If an exception occurs during the course of validation.
     */
    protected final void validateName(MessageList msgList, IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(getName())) {
            msgList.add(new Message(MSGCODE_NAME_NOT_SPECIFIED, Messages.BFElement_nameNotSpecified, Message.ERROR,
                    this));
            return;
        }
        Message msg = getIpsProject().getNamingConventions().validateIfValidJavaIdentifier(getName(),
                Messages.BFElement_nameNotValid, this, ipsProject);
        if (msg != null) {
            msgList.add(msg);
            return;
        }
    }

    /**
     * Can be used within subclass validation methods to validate the name of the business function
     * element. This method checks if the name is equal to the not allowed values <em>execute</em>,
     * <em>start</em>, <em>end</em>.
     * 
     * @param name The name to be checked.
     * @param nameOfName The error message that is created by this method refers to the name. Here
     *            there is to specify how to call that name within the error message.
     * @param msgList The message list to which the message is appended.
     */
    protected final void validateNotAllowedNames(String name, String nameOfName, MessageList msgList) {
        String uncapName = StringUtils.uncapitalize(name);
        if (uncapName.equals("execute") || uncapName.equals("start") || uncapName.equals("end")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            String text = NLS.bind(Messages.BFElement_nameNotAllowed, name);
            msgList.add(new Message(MSGCODE_NAME_NOT_VALID, text, Message.ERROR, this));
        }
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        if (getType().equals(BFElementType.DECISION) || getType().equals(BFElementType.MERGE)
                || getType().equals(BFElementType.ACTION_INLINE) || getType().equals(BFElementType.PARAMETER)) {
            validateName(list, ipsProject);
            validateNotAllowedNames(getName(), "name", list); //$NON-NLS-1$
        }
    }

}
