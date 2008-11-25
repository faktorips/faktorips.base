package org.faktorips.devtools.core.internal.model.bf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.Bendpoint;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ControlFlow extends IpsObjectPart implements IControlFlow {

    private Integer targetId;
    private Integer sourceId;
    private List<Bendpoint> bendpoints = new ArrayList<Bendpoint>();

    
    public ControlFlow(IIpsObject parent, int id) {
        super(parent, id);
    }

    public List<Bendpoint> getBendpoints() {
        return Collections.unmodifiableList(bendpoints);
    }

    public void setBendpoint(int index, Bendpoint bendpoint){
        if (bendpoint == null || bendpoints.contains(bendpoint)) {
            return;
        }
        bendpoints.set(index, bendpoint);
        objectHasChanged();
    }
    
    public void addBendpoint(int index, Bendpoint bendpoint) {
        if (bendpoint == null || bendpoints.contains(bendpoint)) {
            return;
        }
        bendpoints.add(index, bendpoint);
        objectHasChanged();
    }

    public void removeBendpoint(int index) {
        if (bendpoints.remove(index) != null) {
            objectHasChanged();
        }
    }

    public IBusinessFunction getBusinessFunction(){
        return (IBusinessFunction)getParent();
    }
    
    public IBFElement getTarget() {
        return getBusinessFunction().getBFElement(targetId);
    }

    public void setTarget(IBFElement target) {
        if(this.targetId == null && target == null){
            return;
        }
        if (this.targetId != null && target != null && this.targetId.equals(target.getId())) {
            return;
        }
        if(getTarget() != null){
            getTarget().removeIncomingControlFlow(this);
        }
        this.targetId = (target == null) ? null : target.getId();
        objectHasChanged();
        if(getTarget() != null){
            getTarget().addIncomingControlFlow(this);
        }
    }

    public IBFElement getSource() {
        return getBusinessFunction().getBFElement(sourceId);
    }

    public void setSource(IBFElement source) {
        if(this.sourceId == null && source == null){
            return;
        }
        if (this.sourceId != null && source != null && this.sourceId.equals(source.getId())) {
            return;
        }
        if(getSource() != null){
            getSource().removeOutgoingControlFlow(this);
        }
        this.sourceId = (source == null) ? null : source.getId();
        objectHasChanged();
        if(getSource() != null){
            getSource().addOutgoingControlFlow(this);
        }
    }
    
    @Override
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute(PROPERTY_NAME);
        String sourceValue = element.getAttribute(PROPERTY_SOURCE);
        sourceId = sourceValue.isEmpty() ? null : Integer.parseInt(sourceValue);
        String targetValue = element.getAttribute(PROPERTY_TARGET);
        targetId = targetValue.isEmpty() ? null : Integer.parseInt(targetValue);
        NodeList nl = element.getElementsByTagName("Bendpoint");
        bendpoints.clear();
        for (int i = 0; i < nl.getLength(); i++) {
            Element bendpointEl = (Element)nl.item(i);
            int locationX = Integer.parseInt(bendpointEl.getAttribute("locationX"));
            int locationY = Integer.parseInt(bendpointEl.getAttribute("locationY"));
            bendpoints.add(new AbsoluteBendpoint(locationX, locationY));
        }
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_SOURCE, sourceId == null ? "" : String.valueOf(sourceId));
        element.setAttribute(PROPERTY_TARGET, targetId == null ? "" : String.valueOf(targetId));
        Document doc = element.getOwnerDocument();
        for (Bendpoint bendpoint : this.bendpoints) {
            Element bendpointEl = doc.createElement("Bendpoint");
            Point location = bendpoint.getLocation();
            bendpointEl.setAttribute("locationX", String.valueOf(location.x));
            bendpointEl.setAttribute("locationY", String.valueOf(location.y));
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

    //TODO image access 
    public Image getImage() {
        return null;
    }
}
