package org.faktorips.devtools.core.internal.model.bf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.faktorips.devtools.core.internal.model.ipsobject.BaseIpsObject;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IActionBFE;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.bf.IDecisionBFE;
import org.faktorips.devtools.core.model.bf.IParameterBFE;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

public class BusinessFunction extends BaseIpsObject implements IBusinessFunction {

    private BFElementIpsObjectPartCollection simpleElements;
    private BFElementIpsObjectPartCollection actions;
    private BFElementIpsObjectPartCollection decisions;
    private BFElementIpsObjectPartCollection parameters;
    private BFElementIpsObjectPartCollection controlFlows;
    private Dimension parameterRectangleSize = new Dimension(100, 100);
    private Point parameterRectangleLocation = new Point(10, 10);

    public BusinessFunction(IIpsSrcFile file) {
        super(file);
        simpleElements = new BFElementIpsObjectPartCollection(this, BFElement.class, IBFElement.class,
                IBFElement.XML_TAG);
        actions = new BFElementIpsObjectPartCollection(this, ActionBFE.class, IActionBFE.class, IActionBFE.XML_TAG);
        decisions = new BFElementIpsObjectPartCollection(this, DecisionBFE.class, IDecisionBFE.class,
                IDecisionBFE.XML_TAG);
        parameters = new BFElementIpsObjectPartCollection(this, ParameterBFE.class, IParameterBFE.class,
                IParameterBFE.XML_TAG);
        controlFlows = new BFElementIpsObjectPartCollection(this, ControlFlow.class, IControlFlow.class,
                IControlFlow.XML_TAG);
    }

    public Dimension getParameterRectangleSize() {
        return parameterRectangleSize;
    }

    public void setParameterRectangleSize(Dimension parameterRectangleSize) {
        Dimension old = this.parameterRectangleSize;
        this.parameterRectangleSize = parameterRectangleSize;
        valueChanged(old, parameterRectangleSize);
    }

    // TODO test
    public IBFElement getStart() {
        for (IIpsObjectPart part : simpleElements.getParts()) {
            IBFElement element = (IBFElement)part;
            if (element.getType().equals(BFElementType.START)) {
                return element;
            }
        }
        return null;
    }

    // TODO test
    public IBFElement getEnd() {
        for (IIpsObjectPart part : simpleElements.getParts()) {
            IBFElement element = (IBFElement)part;
            if (element.getType().equals(BFElementType.START)) {
                return element;
            }
        }
        return null;
    }

    public Point getParameterRectangleLocation() {
        return parameterRectangleLocation;
    }

    public List<IParameterBFE> getParameterBFEs() {
        ArrayList<IParameterBFE> returnValue = new ArrayList<IParameterBFE>();
        for (IIpsObjectPart parameterBFE : parameters.getParts()) {
            returnValue.add((IParameterBFE)parameterBFE);
        }
        return returnValue;
    }

    public IParameterBFE getParameterBFE(String name) {
        for (IIpsObjectPart parameterBFE : parameters.getParts()) {
            if (parameterBFE.getName().equals(name)) {
                return (IParameterBFE)parameterBFE;
            }
        }
        return null;
    }

    public IBFElement getBFElement(Integer id) {
        if (id == null) {
            return null;
        }
        IBFElement element = (IBFElement)simpleElements.getPartById(id);
        if (element == null) {
            element = (IBFElement)actions.getPartById(id);
        }
        if (element == null) {
            element = (IBFElement)decisions.getPartById(id);
        }
        if (element == null) {
            element = (IBFElement)parameters.getPartById(id);
        }
        return element;
    }

    public IControlFlow newControlFlow() {
        return (IControlFlow)controlFlows.newPart();
    }

    public IControlFlow getControlFlow(int id) {
        IIpsObjectPart[] parts = controlFlows.getParts();
        for (IIpsObjectPart ipsObjectPart : parts) {
            if (id == ipsObjectPart.getId()) {
                return (IControlFlow)ipsObjectPart;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<IControlFlow> getControlFlows() {
        IIpsObjectPart[] controlFlowParts = controlFlows.getParts();
        ArrayList<IControlFlow> controlFlowList = new ArrayList<IControlFlow>(controlFlowParts.length);
        controlFlowList.addAll((Collection<? extends IControlFlow>)Arrays.asList(controlFlowParts));
        return controlFlowList;
    }

    public IBFElement newSimpleBFElement(BFElementType type, Point location) {
        if (!(BFElementType.END == type || BFElementType.START == type || BFElementType.MERGE == type)) {
            throw new IllegalArgumentException("Only BFElement of the types: " + BFElementType.START + ", "
                    + BFElementType.END + ", " + BFElementType.MERGE + " can be created with this method.");
        }
        BFElement element = (BFElement)simpleElements.newBFElement(location, type);
        return element;
    }

    public IActionBFE newOpaqueAction(Point location) {
        ActionBFE element = (ActionBFE)actions.newBFElement(location, BFElementType.ACTION_INLINE);
        return element;
    }

    public IActionBFE newMethodCallAction(Point location) {
        ActionBFE element = (ActionBFE)actions.newBFElement(location, BFElementType.ACTION_METHODCALL);
        return element;
    }

    public IActionBFE newBusinessFunctionCallAction(Point location) {
        ActionBFE element = (ActionBFE)actions.newBFElement(location, BFElementType.ACTION_BUSINESSFUNCTIONCALL);
        return element;
    }

    public IDecisionBFE newDecision(Point location) {
        DecisionBFE element = (DecisionBFE)decisions.newBFElement(location, BFElementType.DECISION);
        return element;
    }

    public IParameterBFE newParameter() {
        ParameterBFE element = (ParameterBFE)parameters.newBFElement(null, BFElementType.PARAMETER);
        return element;
    }

    // TODO test
    @SuppressWarnings("unchecked")
    public List<IBFElement> getBFElementsWithoutParameters() {

        List<IBFElement> nodeList = new ArrayList<IBFElement>();
        IIpsObjectPart[] bFParts = simpleElements.getParts();
        nodeList.addAll((Collection<? extends IBFElement>)Arrays.asList(bFParts));
        bFParts = actions.getParts();
        nodeList.addAll((Collection<? extends IBFElement>)Arrays.asList(bFParts));
        bFParts = decisions.getParts();
        nodeList.addAll((Collection<? extends IBFElement>)Arrays.asList(bFParts));
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    public List<IBFElement> getBFElements() {

        List<IBFElement> nodeList = new ArrayList<IBFElement>();
        IIpsObjectPart[] bFParts = simpleElements.getParts();
        nodeList.addAll((Collection<? extends IBFElement>)Arrays.asList(bFParts));
        bFParts = actions.getParts();
        nodeList.addAll((Collection<? extends IBFElement>)Arrays.asList(bFParts));
        bFParts = decisions.getParts();
        nodeList.addAll((Collection<? extends IBFElement>)Arrays.asList(bFParts));
        bFParts = parameters.getParts();
        nodeList.addAll((Collection<? extends IBFElement>)Arrays.asList(bFParts));
        return nodeList;
    }

    public IpsObjectType getIpsObjectType() {
        return BusinessFunctionIpsObjectType.getInstance();
    }

    @Override
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        int width = Integer.parseInt(element.getAttribute("parameterRectangleWidth"));
        int height = Integer.parseInt(element.getAttribute("parameterRectangleHeight"));
        parameterRectangleSize = new Dimension(width, height);
        int xLocation = Integer.parseInt(element.getAttribute("parameterRectangleX"));
        int yLocation = Integer.parseInt(element.getAttribute("parameterRectangleY"));
        parameterRectangleLocation = new Point(xLocation, yLocation);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute("parameterRectangleWidth", String.valueOf(getParameterRectangleSize().width));
        element.setAttribute("parameterRectangleHeight", String.valueOf(getParameterRectangleSize().height));
        element.setAttribute("parameterRectangleX", String.valueOf(getParameterRectangleLocation().x));
        element.setAttribute("parameterRectangleY", String.valueOf(getParameterRectangleLocation().y));
    }

    private static class BFElementIpsObjectPartCollection extends IpsObjectPartCollection {

        @SuppressWarnings("unchecked")
        public BFElementIpsObjectPartCollection(BaseIpsObject ipsObject, Class partsClazz, Class publishedInterface,
                String xmlTag) {
            super(ipsObject, partsClazz, publishedInterface, xmlTag);
        }

        public IBFElement newBFElement(final Point location, final BFElementType type) {
            IpsObjectPartInitializer initializer = new IpsObjectPartInitializer() {

                public void initialize(IpsObjectPart part) {
                    BFElement element = (BFElement)part;
                    element.location = location;
                    element.type = type;
                }
            };
            return (BFElement)newPart(initializer);
        }
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

        List<IBFElement> elements = getBFElements();
        if (elements.isEmpty()) {
            return;
        }
        List<IParameterBFE> parameters = getParameterBFEs();
        // if only parameters are defined there is nothing to do here
        if (elements.size() == parameters.size()) {
            return;
        }
        boolean startOrEndMissing = false;
        if (getStart() == null) {
            list.add(new Message("code", "The start of this business function has to be defined", Message.ERROR, this));
            startOrEndMissing = true;
        }
        if (getEnd() == null) {
            list.add(new Message("code", "The end of this business function has to be defined", Message.ERROR, this));
            startOrEndMissing = true;
        }
        if (startOrEndMissing) {
            return;
        }

        elements = getBFElementsWithoutParameters();
        ArrayList<IBFElement> successfullyCheckedForStart = new ArrayList<IBFElement>(elements.size());
        for (IBFElement element : elements) {
            traceToStart(element, successfullyCheckedForStart, new ArrayList<IBFElement>(elements.size()));
        }

        ArrayList<IBFElement> successfullyCheckedForEnd = new ArrayList<IBFElement>(elements.size());
        for (IBFElement element : elements) {
            traceToEnd(element, successfullyCheckedForEnd, new ArrayList<IBFElement>(elements.size()));
        }

        for (IBFElement element : elements) {
            if (element.getType().equals(BFElementType.START)) {
                continue;
            }
            if (!successfullyCheckedForStart.contains(element)) {
                list.add(new Message("code", "This element: " + element.getDisplayString()
                        + " is not directly or indirectly connected to the start of this business function.",
                        Message.ERROR, element));
            }
        }

        for (IBFElement element : elements) {
            if (element.getType().equals(BFElementType.END)) {
                continue;
            }
            if (!successfullyCheckedForEnd.contains(element)) {
                list.add(new Message("code", "This element: " + element.getDisplayString()
                        + " is not directly or indirectly connected to the end of this business function.",
                        Message.ERROR, element));
            }
        }
    }

    private void traceToStart(IBFElement current, List<IBFElement> successfullyChecked, List<IBFElement> currentTrace) {
        if(current == null ){
            return;
        }
        if (BFElementType.START.equals(current.getType())) {
            for (IBFElement element : currentTrace) {
                if (!successfullyChecked.contains(element)) {
                    successfullyChecked.add(element);
                }
            }
            return;
        }
        if (currentTrace.contains(current)) {
            return;
        }
        List<IControlFlow> in = current.getIncomingControlFlow();

        if (in.size() == 1) {
            IBFElement source = in.get(0).getSource();
            if(addIfPredecessorValid(source, current, successfullyChecked, currentTrace)){
                return;
            }
            traceToStart(source, successfullyChecked, currentTrace);
        }
        for (IControlFlow controlFlow : in) {
            IBFElement source = controlFlow.getSource();
            if(addIfPredecessorValid(source, current, successfullyChecked, currentTrace)){
                continue;
            }
            ArrayList<IBFElement> newTrace = new ArrayList<IBFElement>(currentTrace.size() + 10);
            newTrace.addAll(currentTrace);
            traceToStart(source, successfullyChecked, newTrace);
        }
    }

    private boolean addIfPredecessorValid(IBFElement source, IBFElement current, List<IBFElement> successfullyChecked, List<IBFElement> currentTrace){
        if (successfullyChecked.contains(source)) {
            if (current != null && !successfullyChecked.contains(current)) {
                successfullyChecked.add(current);
            }
            return true;
        }
        currentTrace.add(current);
        return false;
    }
    
    private void traceToEnd(IBFElement current, List<IBFElement> successfullyChecked, List<IBFElement> currentTrace) {
        if(current == null){
            return;
        }
        if (BFElementType.END.equals(current.getType())) {
            for (IBFElement element : currentTrace) {
                if (!successfullyChecked.contains(element)) {
                    successfullyChecked.add(element);
                }
            }
            return;
        }
        if (currentTrace.contains(current)) {
            return;
        }
        List<IControlFlow> in = current.getOutgoingControlFlow();
        if (in.size() == 1) {
            IBFElement target = in.get(0).getTarget();
            if(addIfPredecessorValid(target, current, successfullyChecked, currentTrace)){
                return;
            }
            traceToEnd(target, successfullyChecked, currentTrace);
        }
        for (IControlFlow controlFlow : in) {
            IBFElement target = controlFlow.getTarget();
            if(addIfPredecessorValid(target, current, successfullyChecked, currentTrace)){
                continue;
            }
            ArrayList<IBFElement> newTrace = new ArrayList<IBFElement>(currentTrace.size() + 10);
            newTrace.addAll(currentTrace);
            traceToEnd(target, successfullyChecked, newTrace);
        }
    }

}
