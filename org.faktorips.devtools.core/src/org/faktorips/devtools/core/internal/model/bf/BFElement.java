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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class BFElement extends IpsObjectPart implements IBFElement {

    Point location;
    private Dimension size = new Dimension(100, 60);
    BFElementType type;

    private List<Integer> incommingControlFlows = new ArrayList<Integer>();
    private List<Integer> outgoingControlFlows = new ArrayList<Integer>();

    public BFElement(IIpsObject parent, int id) {
        super(parent, id);
    }

    @Override
    public void delete() {
        super.delete();
    }

    public String getDisplayString() {
        return getName();
    }

    public BFElementType getType() {
        return type;
    }

    void setType(BFElementType type) {
        // no event triggering since this method is only call at object creation time
        this.type = type;
    }

    public void addIncomingControlFlow(IControlFlow controlFlow) {
        if (incommingControlFlows.contains(controlFlow.getId())) {
            return;
        }
        incommingControlFlows.add(controlFlow.getId());
        objectHasChanged();
        controlFlow.setTarget(this);
    }

    public List<IControlFlow> getIncomingControlFlow() {
        ArrayList<IControlFlow> inList = new ArrayList<IControlFlow>();
        List<IControlFlow> controlFlowList = getBusinessFunction().getControlFlows();
        for (IControlFlow controlFlow : controlFlowList) {
            for (Integer id : incommingControlFlows) {
                if (id.equals(controlFlow.getId())) {
                    inList.add(controlFlow);
                }
            }
        }
        return inList;
    }

    public List<IControlFlow> getAllControlFlows() {
        List<IControlFlow> allEdges = new ArrayList<IControlFlow>();
        allEdges.addAll(getIncomingControlFlow());
        allEdges.addAll(getOutgoingControlFlow());
        return allEdges;
    }

    public boolean removeIncomingControlFlow(IControlFlow controlFlow) {
        if (incommingControlFlows.remove((Integer)controlFlow.getId())) {
            objectHasChanged();
            controlFlow.setTarget(null);
            return true;
        }
        return false;
    }

    public void removeAllIncommingControlFlows(){
        incommingControlFlows.clear();
    }
    
    public void removeAllOutgoingControlFlows(){
        outgoingControlFlows.clear();
    }
    
    public void addOutgoingControlFlow(IControlFlow controlFlow) {
        if (outgoingControlFlows.contains(controlFlow.getId())) {
            return;
        }
        outgoingControlFlows.add(controlFlow.getId());
        objectHasChanged();
        controlFlow.setSource(this);
    }

    public List<IControlFlow> getOutgoingControlFlow() {
        ArrayList<IControlFlow> outList = new ArrayList<IControlFlow>();
        List<IControlFlow> controlFlowList = getBusinessFunction().getControlFlows();
        for (IControlFlow controlFlow : controlFlowList) {
            for (Integer id : outgoingControlFlows) {
                if (id.equals(controlFlow.getId())) {
                    outList.add(controlFlow);
                }
            }
        }
        return outList;
    }

    public boolean removeOutgoingControlFlow(IControlFlow controlFlow) {
        if (outgoingControlFlows.remove((Integer)controlFlow.getId())) {
            objectHasChanged();
            controlFlow.setSource(null);
            return true;
        }
        return false;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        Point old = this.location;
        this.location = location;
        valueChanged(old, location);
    }

    public Dimension getSize() {
        return size;
    }

    public void setSize(Dimension size) {
        Dimension old = this.size;
        this.size = size;
        valueChanged(old, size);
    }

    public void setName(String name) {
        String old = this.name;
        this.name = name;
        valueChanged(old, name);
    }

    public IBusinessFunction getBusinessFunction() {
        return (IBusinessFunction)getParent();
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute(PROPERTY_NAME);
        type = BFElementType.getType(element.getAttribute(PROPERTY_TYPE));
        NodeList nl = element.getElementsByTagName("Location"); //$NON-NLS-1$
        for (int i = 0; i < nl.getLength(); i++) {
            Element posElement = (Element)nl.item(i);
            String xPos = posElement.getAttribute("xlocation"); //$NON-NLS-1$
            String yPos = posElement.getAttribute("ylocation"); //$NON-NLS-1$
            this.location = new Point(Integer.parseInt(xPos), Integer.parseInt(yPos));
        }
        nl = element.getElementsByTagName("Size"); //$NON-NLS-1$
        for (int i = 0; i < nl.getLength(); i++) {
            Element posElement = (Element)nl.item(i);
            String width = posElement.getAttribute("width"); //$NON-NLS-1$
            String height = posElement.getAttribute("height"); //$NON-NLS-1$
            this.size = new Dimension(Integer.parseInt(width), Integer.parseInt(height));
        }
        nl = element.getElementsByTagName("ControlFlow"); //$NON-NLS-1$
        incommingControlFlows.clear();
        outgoingControlFlows.clear();
        for (int i = 0; i < nl.getLength(); i++) {
            Element posElement = (Element)nl.item(i);
            String type = posElement.getAttribute("type"); //$NON-NLS-1$
            String controlFlowId = posElement.getAttribute("id"); //$NON-NLS-1$
            if (type.equals("in")) { //$NON-NLS-1$
                incommingControlFlows.add(Integer.valueOf(controlFlowId));
            }
            if (type.equals("out")) { //$NON-NLS-1$
                outgoingControlFlows.add(Integer.valueOf(controlFlowId));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_TYPE, type.getId());
        Document doc = element.getOwnerDocument();

        Element locationEl = doc.createElement("Location"); //$NON-NLS-1$
        locationEl.setAttribute("xlocation", String.valueOf(getLocation().x)); //$NON-NLS-1$
        locationEl.setAttribute("ylocation", String.valueOf(getLocation().y)); //$NON-NLS-1$
        element.appendChild(locationEl);

        Element sizeEl = doc.createElement("Size"); //$NON-NLS-1$
        sizeEl.setAttribute("width", String.valueOf(getSize().width)); //$NON-NLS-1$
        sizeEl.setAttribute("height", String.valueOf(getSize().height)); //$NON-NLS-1$
        element.appendChild(sizeEl);

        for (Integer controlFlowId : this.outgoingControlFlows) {
            Element controlFlowEl = doc.createElement("ControlFlow"); //$NON-NLS-1$
            element.appendChild(controlFlowEl);
            controlFlowEl.setAttribute("type", "out"); //$NON-NLS-1$ //$NON-NLS-2$
            controlFlowEl.setAttribute("id", String.valueOf(controlFlowId)); //$NON-NLS-1$
        }

        for (Integer controlFlowId : this.incommingControlFlows) {
            Element controlFlowEl = doc.createElement("ControlFlow"); //$NON-NLS-1$
            element.appendChild(controlFlowEl);
            controlFlowEl.setAttribute("type", "in"); //$NON-NLS-1$ //$NON-NLS-2$
            controlFlowEl.setAttribute("id", String.valueOf(controlFlowId)); //$NON-NLS-1$
        }
    }

    @Override
    public IIpsElement[] getChildren() {
        return new IIpsElement[0];
    }

    @Override
    protected IIpsObjectPart newPart(Element xmlTag, int id) {
        return null;
    }

    @Override
    protected void addPart(IIpsObjectPart part) {
    }

    @Override
    protected void reinitPartCollections() {
    }

    @Override
    protected void removePart(IIpsObjectPart part) {

    }

    @SuppressWarnings("unchecked")
    public IIpsObjectPart newPart(Class partType) {
        return null;
    }

    public Image getImage() {
        // TODO image handling
        return null;
    }

    /**
     * Can be used within subclass validation methods to validate the name of the business function
     * element. This method validates if the name has been specified and if the name is valid
     * according to java naming conventions.
     * 
     * @throws CoreException if an exception occurs during the course of validation
     */
    protected final void validateName(MessageList msgList, IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(getName())) {
            msgList
                    .add(new Message(MSGCODE_NAME_NOT_SPECIFIED, Messages.getString("BFElement.nameNotSpecified"), Message.ERROR, this)); //$NON-NLS-1$
            return;
        }
        Message msg = getIpsProject().getNamingConventions().validateIfValidJavaIdentifier(getName(),
                Messages.getString("BFElement.nameNotValid"), this, ipsProject); //$NON-NLS-1$
        if (msg != null) {
            msgList.add(msg);
            return;
        }
    }

    /**
     * Can be used within subclass validation methods to validate the name of the business function
     * element. This method checks if the name is equal to the not allowed values <i>execute</i>,
     * <i>start</i>, <i>end</i>.
     * 
     * @param name the name to be checked
     * @param nameOfName the error message that is created by this method refers to the name. Here
     *            there is to specify how to call that name within the error message
     * @param msgList the message list to which the message is appended
     * 
     * @throws CoreException if an exception occurs during the course of validation
     */
    protected final void validateNotAllowedNames(String name, String nameOfName, MessageList msgList) {
        String uncapName = StringUtils.uncapitalize(name);
        if (uncapName.equals("execute") || uncapName.equals("start") || uncapName.equals("end")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            String text = NLS.bind(Messages.getString("BFElement.nameNotAllowed"), name); //$NON-NLS-1$ //$NON-NLS-2$
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
