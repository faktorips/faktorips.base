package org.faktorips.devtools.core.internal.model.bf;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class BFElement extends IpsObjectPart implements IBFElement {

    Point location;
    private Dimension size = new Dimension(100, 50);
    BFElementType type;

    private List<Integer> incommingControlFlows = new ArrayList<Integer>();
    private List<Integer> outgoingControlFlows = new ArrayList<Integer>();


    public BFElement(IIpsObject parent, int id){
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
        //no event triggering since this method is only call at object creation time
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
                if(id.equals(controlFlow.getId())){
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
                if(id.equals(controlFlow.getId())){
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
        NodeList nl = element.getElementsByTagName("Location");
        for (int i = 0; i < nl.getLength(); i++) {
            Element posElement = (Element)nl.item(i);
            String xPos = posElement.getAttribute("xlocation");
            String yPos = posElement.getAttribute("ylocation");
            this.location = new Point(Integer.parseInt(xPos), Integer.parseInt(yPos)); 
        }
        nl = element.getElementsByTagName("Size");
        for (int i = 0; i < nl.getLength(); i++) {
            Element posElement = (Element)nl.item(i);
            String width = posElement.getAttribute("width");
            String height = posElement.getAttribute("height");
            this.size = new Dimension(Integer.parseInt(width), Integer.parseInt(height)); 
        }
        nl = element.getElementsByTagName("ControlFlow");
        incommingControlFlows.clear();
        outgoingControlFlows.clear();
        for (int i = 0; i < nl.getLength(); i++) {
            Element posElement = (Element)nl.item(i);
            String type = posElement.getAttribute("type");
            String controlFlowId = posElement.getAttribute("id");
            if(type.equals("in")){
                incommingControlFlows.add(Integer.valueOf(controlFlowId));
            }
            if(type.equals("out")){
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
        
        Element locationEl = doc.createElement("Location");
        locationEl.setAttribute("xlocation", String.valueOf(getLocation().x));
        locationEl.setAttribute("ylocation", String.valueOf(getLocation().y));
        element.appendChild(locationEl);

        Element sizeEl = doc.createElement("Size");
        sizeEl.setAttribute("width", String.valueOf(getSize().width));
        sizeEl.setAttribute("height", String.valueOf(getSize().height));
        element.appendChild(sizeEl);
        
        for (Integer controlFlowId: this.outgoingControlFlows) {
            Element controlFlowEl = doc.createElement("ControlFlow");
            element.appendChild(controlFlowEl);
            controlFlowEl.setAttribute("type", "out");
            controlFlowEl.setAttribute("id", String.valueOf(controlFlowId));
        }

        for (Integer controlFlowId: this.incommingControlFlows) {
            Element controlFlowEl = doc.createElement("ControlFlow");
            element.appendChild(controlFlowEl);
            controlFlowEl.setAttribute("type", "in");
            controlFlowEl.setAttribute("id", String.valueOf(controlFlowId));
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
    protected void reAddPart(IIpsObjectPart part) {
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
}
