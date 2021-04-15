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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.model.bf.BFElementType;
import org.faktorips.devtools.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.model.bf.IActionBFE;
import org.faktorips.devtools.model.bf.IBFElement;
import org.faktorips.devtools.model.bf.IControlFlow;
import org.faktorips.devtools.model.bf.IDecisionBFE;
import org.faktorips.devtools.model.bf.IMethodCallBFE;
import org.faktorips.devtools.model.bf.IParameterBFE;
import org.faktorips.devtools.model.bf.Location;
import org.faktorips.devtools.model.bf.Size;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.internal.dependency.IpsObjectDependency;
import org.faktorips.devtools.model.internal.ipsobject.BaseIpsObject;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.w3c.dom.Element;

/** @deprecated for removal since 21.6 */
@Deprecated
public class BusinessFunction extends BaseIpsObject implements org.faktorips.devtools.model.bf.IBusinessFunction {

    private final BFElementIpsObjectPartCollection<IBFElement> simpleElements;
    private final BFElementIpsObjectPartCollection<IActionBFE> actions;
    private final BFElementIpsObjectPartCollection<IDecisionBFE> decisions;
    private final BFElementIpsObjectPartCollection<IParameterBFE> parameters;
    private final IpsObjectPartCollection<IControlFlow> controlFlows;
    private Size parameterRectangleSize = new Size(100, 100);
    private Location parameterRectangleLocation = new Location(10, 10);

    public BusinessFunction(IIpsSrcFile file) {
        super(file);
        simpleElements = new BFElementIpsObjectPartCollection<>(this, BFElement.class, IBFElement.class,
                IBFElement.XML_TAG);
        actions = new BFElementIpsObjectPartCollection<>(this, ActionBFE.class, IActionBFE.class, IActionBFE.XML_TAG);
        decisions = new BFElementIpsObjectPartCollection<>(this, DecisionBFE.class, IDecisionBFE.class,
                IDecisionBFE.XML_TAG);
        parameters = new BFElementIpsObjectPartCollection<>(this, ParameterBFE.class, IParameterBFE.class,
                IParameterBFE.XML_TAG);
        controlFlows = new IpsObjectPartCollection<>(this, ControlFlow.class, IControlFlow.class,
                IControlFlow.XML_TAG);
    }

    @Override
    public Size getParameterRectangleSize() {
        return parameterRectangleSize;
    }

    @Override
    public void setParameterRectangleSize(Size parameterRectangleSize) {
        Size old = this.parameterRectangleSize;
        this.parameterRectangleSize = parameterRectangleSize;
        valueChanged(old, parameterRectangleSize);
    }

    @Override
    public IBFElement getStart() {
        for (IIpsObjectPart part : simpleElements.getParts()) {
            IBFElement element = (IBFElement)part;
            if (element.getType().equals(BFElementType.START)) {
                return element;
            }
        }
        return null;
    }

    @Override
    public IBFElement getEnd() {
        for (IIpsObjectPart part : simpleElements.getParts()) {
            IBFElement element = (IBFElement)part;
            if (element.getType().equals(BFElementType.END)) {
                return element;
            }
        }
        return null;
    }

    @Override
    public Location getParameterRectangleLocation() {
        return parameterRectangleLocation;
    }

    @Override
    public List<IParameterBFE> getParameterBFEs() {
        ArrayList<IParameterBFE> returnValue = new ArrayList<>();
        for (IIpsObjectPart parameterBFE : parameters.getParts()) {
            returnValue.add((IParameterBFE)parameterBFE);
        }
        return returnValue;
    }

    @Override
    public IParameterBFE getParameterBFE(String name) {
        for (IIpsObjectPart parameterBFE : parameters.getParts()) {
            if (parameterBFE.getName().equals(name)) {
                return (IParameterBFE)parameterBFE;
            }
        }
        return null;
    }

    @Override
    public IBFElement getBFElement(String id) {
        if (id == null) {
            return null;
        }
        IBFElement element = simpleElements.getPartById(id);
        if (element == null) {
            element = actions.getPartById(id);
        }
        if (element == null) {
            element = decisions.getPartById(id);
        }
        if (element == null) {
            element = parameters.getPartById(id);
        }
        return element;
    }

    @Override
    public IControlFlow newControlFlow() {
        return controlFlows.newPart();
    }

    @Override
    public IControlFlow getControlFlow(String id) {
        IIpsObjectPart[] parts = controlFlows.getParts();
        for (IIpsObjectPart ipsObjectPart : parts) {
            if (id.equals(ipsObjectPart.getId())) {
                return (IControlFlow)ipsObjectPart;
            }
        }
        return null;
    }

    @Override
    public List<IControlFlow> getControlFlows() {
        return new ArrayList<>(controlFlows.getBackingList());
    }

    @Override
    public IBFElement newEnd(Location location) {
        BFElement element = (BFElement)simpleElements.newBFElement(location, BFElementType.END);
        element.setSize(new Size(30, 30));
        return element;
    }

    @Override
    public IBFElement newMerge(Location location) {
        return simpleElements.newBFElement(location, BFElementType.MERGE);
    }

    @Override
    public IBFElement newStart(Location location) {
        BFElement element = (BFElement)simpleElements.newBFElement(location, BFElementType.START);
        element.setSize(new Size(30, 30));
        return element;
    }

    @Override
    public IActionBFE newOpaqueAction(Location location) {
        ActionBFE element = (ActionBFE)actions.newBFElement(location, BFElementType.ACTION_INLINE);
        return element;
    }

    @Override
    public IActionBFE newMethodCallAction(Location location) {
        ActionBFE element = (ActionBFE)actions.newBFElement(location, BFElementType.ACTION_METHODCALL);
        return element;
    }

    @Override
    public IActionBFE newBusinessFunctionCallAction(Location location) {
        ActionBFE element = (ActionBFE)actions.newBFElement(location, BFElementType.ACTION_BUSINESSFUNCTIONCALL);
        return element;
    }

    @Override
    public IDecisionBFE newDecision(Location location) {
        DecisionBFE element = (DecisionBFE)decisions.newBFElement(location, BFElementType.DECISION);
        return element;
    }

    // TODO testing
    @Override
    public IDecisionBFE newMethodCallDecision(Location location) {
        DecisionBFE element = (DecisionBFE)decisions.newBFElement(location, BFElementType.DECISION_METHODCALL);
        return element;
    }

    @Override
    public IParameterBFE newParameter() {
        ParameterBFE element = (ParameterBFE)parameters.newBFElement(null, BFElementType.PARAMETER);
        return element;
    }

    @Override
    public List<IBFElement> getBFElements() {
        int size = simpleElements.size() + actions.size() + decisions.size() + parameters.size();
        List<IBFElement> nodeList = new ArrayList<>(size);
        nodeList.addAll(simpleElements.getBackingList());
        nodeList.addAll(actions.getBackingList());
        nodeList.addAll(decisions.getBackingList());
        nodeList.addAll(parameters.getBackingList());
        return nodeList;
    }

    @Override
    public IpsObjectType getIpsObjectType() {
        return BusinessFunctionIpsObjectType.getInstance();
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        int width = Integer.parseInt(element.getAttribute("parameterRectangleWidth")); //$NON-NLS-1$
        int height = Integer.parseInt(element.getAttribute("parameterRectangleHeight")); //$NON-NLS-1$
        parameterRectangleSize = new Size(width, height);
        int xLocation = Integer.parseInt(element.getAttribute("parameterRectangleX")); //$NON-NLS-1$
        int yLocation = Integer.parseInt(element.getAttribute("parameterRectangleY")); //$NON-NLS-1$
        parameterRectangleLocation = new Location(xLocation, yLocation);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute("parameterRectangleWidth", String.valueOf(getParameterRectangleSize().getWidth())); //$NON-NLS-1$
        element.setAttribute("parameterRectangleHeight", String.valueOf(getParameterRectangleSize().getHeight())); //$NON-NLS-1$
        element.setAttribute("parameterRectangleX", String.valueOf(getParameterRectangleLocation().getX())); //$NON-NLS-1$
        element.setAttribute("parameterRectangleY", String.valueOf(getParameterRectangleLocation().getY())); //$NON-NLS-1$
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        validateBFElementsConnected(list);
        validateOnlyOneElementAllowed(list, BFElementType.START, MSGCODE_START_SINGLE_OCCURRENCE);
        validateOnlyOneElementAllowed(list, BFElementType.END, MSGCODE_END_SINGLE_OCCURRENCE);
        validateBFElementNameCollision(list);
        list.add(new Message(MSGCODE_DEPRECATED, Messages.BusinessFunction_deprecated, Message.WARNING, this));
    }

    // TODO testing Decision_MethodCall
    private void validateBFElementNameCollision(MessageList msgList) {
        Map<String, List<IBFElement>> elements = new HashMap<>();

        for (IBFElement element : getBFElements()) {
            String key;
            if (element.getType().equals(BFElementType.START) || element.getType().equals(BFElementType.END)
                    || element.getType().equals(BFElementType.PARAMETER)) {
                continue;
            } else if (element.getType().equals(BFElementType.ACTION_METHODCALL)) {
                key = ((IActionBFE)element).getExecutableMethodName();
            } else if (element.getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)) {
                key = ((IActionBFE)element).getTarget();
            } else if (element.getType().equals(BFElementType.DECISION_METHODCALL)) {
                key = ((IDecisionBFE)element).getExecutableMethodName();
            } else {
                // decision, merge, inline action,
                key = element.getName();
            }
            List<IBFElement> list = getValue(elements, key);
            list.add(element);
        }

        for (Entry<String, List<IBFElement>> entry : elements.entrySet()) {
            String key = entry.getKey();
            List<IBFElement> list = entry.getValue();
            if (list.size() > 1) {
                for (IBFElement element : list) {
                    if (!(checkIfOnlyMethodCallActions(list) || checkIfOnlyBusinessFunctionCallActions(list)
                            || checkIfOnlyMethodCallDecisions(list))) {
                        String text = NLS.bind(Messages.BusinessFunction_duplicateNames, key);
                        msgList.add(new Message(MSGCODE_ELEMENT_NAME_COLLISION, text, Message.ERROR, element));
                    }
                }
            }
        }
    }

    private boolean checkIfOnlyMethodCallActions(List<IBFElement> list) {
        return checkIfOnlyOneTypeInList(list, BFElementType.ACTION_METHODCALL);
    }

    private boolean checkIfOnlyMethodCallDecisions(List<IBFElement> list) {
        return checkIfOnlyOneTypeInList(list, BFElementType.DECISION_METHODCALL);
    }

    private boolean checkIfOnlyBusinessFunctionCallActions(List<IBFElement> list) {
        return checkIfOnlyOneTypeInList(list, BFElementType.ACTION_BUSINESSFUNCTIONCALL);
    }

    private boolean checkIfOnlyOneTypeInList(List<IBFElement> list, BFElementType type) {
        for (IBFElement element : list) {
            if (!element.getType().equals(type)) {
                return false;
            }
        }
        return true;
    }

    private List<IBFElement> getValue(Map<String, List<IBFElement>> elements, String key) {
        List<IBFElement> list = elements.computeIfAbsent(key, $ -> new ArrayList<>());
        return list;
    }

    private void validateOnlyOneElementAllowed(MessageList msgList, BFElementType type, String msgCode) {
        List<IBFElement> startElements = new ArrayList<>();
        for (IBFElement element : simpleElements) {
            if (element.getType().equals(type)) {
                startElements.add(element);
            }
        }
        if (startElements.size() > 1) {
            String text = NLS.bind(Messages.BusinessFunction_elementOnlyOnce, type.getName());
            for (IBFElement element : startElements) {
                msgList.add(new Message(msgCode, text, Message.ERROR, element));
            }
        }
    }

    private void validateBFElementsConnected(MessageList list) {
        boolean startOrEndMissing = false;
        if (getStart() == null) {
            list.add(new Message(MSGCODE_START_DEFINITION_MISSING, Messages.BusinessFunction_startMissing,
                    Message.ERROR, this));
            startOrEndMissing = true;
        }
        if (getEnd() == null) {
            list.add(new Message(MSGCODE_END_DEFINITION_MISSING, Messages.BusinessFunction_endMissing, Message.ERROR,
                    this));
            startOrEndMissing = true;
        }
        if (startOrEndMissing) {
            return;
        }

        List<IBFElement> elements = getBFElementsWithoutParameters();
        ArrayList<IBFElement> successfullyCheckedForStart = new ArrayList<>(elements.size());
        for (IBFElement element : elements) {
            traceToStart(element, successfullyCheckedForStart, new ArrayList<IBFElement>(elements.size()));
        }

        ArrayList<IBFElement> successfullyCheckedForEnd = new ArrayList<>(elements.size());
        for (IBFElement element : elements) {
            traceToEnd(element, successfullyCheckedForEnd, new ArrayList<IBFElement>(elements.size()));
        }

        for (IBFElement element : elements) {
            if (element.getType().equals(BFElementType.START)) {
                continue;
            }
            if (!successfullyCheckedForStart.contains(element)) {
                String text = NLS.bind(Messages.BusinessFunction_elementNotConnectedWithStart,
                        element.getDisplayString());
                list.add(new Message(MSGCODE_NOT_CONNECTED_WITH_START, text, Message.ERROR, element));
            }
        }

        for (IBFElement element : elements) {
            if (element.getType().equals(BFElementType.END)) {
                continue;
            }
            if (!successfullyCheckedForEnd.contains(element)) {
                String text = NLS.bind(Messages.BusinessFunction_elementNotConnectedWithEnd,
                        element.getDisplayString());
                list.add(new Message(MSGCODE_NOT_CONNECTED_WITH_END, text, Message.ERROR, element));
            }
        }
    }

    private void traceToStart(IBFElement current, List<IBFElement> successfullyChecked, List<IBFElement> currentTrace) {
        if (current == null) {
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
            if (addIfPredecessorValid(source, current, successfullyChecked, currentTrace)) {
                return;
            }
            traceToStart(source, successfullyChecked, currentTrace);
        }
        for (IControlFlow controlFlow : in) {
            IBFElement source = controlFlow.getSource();
            if (addIfPredecessorValid(source, current, successfullyChecked, currentTrace)) {
                continue;
            }
            ArrayList<IBFElement> newTrace = new ArrayList<>(currentTrace.size() + 10);
            newTrace.addAll(currentTrace);
            traceToStart(source, successfullyChecked, newTrace);
        }
    }

    private boolean addIfPredecessorValid(IBFElement source,
            IBFElement current,
            List<IBFElement> successfullyChecked,
            List<IBFElement> currentTrace) {

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
        if (current == null) {
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
            if (addIfPredecessorValid(target, current, successfullyChecked, currentTrace)) {
                return;
            }
            traceToEnd(target, successfullyChecked, currentTrace);
        }
        for (IControlFlow controlFlow : in) {
            IBFElement target = controlFlow.getTarget();
            if (addIfPredecessorValid(target, current, successfullyChecked, currentTrace)) {
                continue;
            }
            ArrayList<IBFElement> newTrace = new ArrayList<>(currentTrace.size() + 10);
            newTrace.addAll(currentTrace);
            traceToEnd(target, successfullyChecked, newTrace);
        }
    }

    @Override
    protected IDependency[] dependsOn(Map<IDependency, List<IDependencyDetail>> details) {
        List<IDependency> dependencies = new ArrayList<>();
        for (IIpsObjectPart part : actions.getParts()) {
            IActionBFE action = (IActionBFE)part;
            if (action.getType() == BFElementType.ACTION_BUSINESSFUNCTIONCALL) {
                IDependency dependency = IpsObjectDependency.createReferenceDependency(getQualifiedNameType(),
                        new QualifiedNameType(action.getTarget(), BusinessFunctionIpsObjectType.getInstance()));
                dependencies.add(dependency);
                addDetails(details, dependency, action, IMethodCallBFE.PROPERTY_TARGET);
                continue;
            }
            if (action.getType() == BFElementType.ACTION_METHODCALL) {
                IParameterBFE param = action.getParameter();
                if (param != null) {
                    String datatype = param.getDatatype();
                    if (datatype != null) {
                        IDependency dependency = IpsObjectDependency.createReferenceDependency(getQualifiedNameType(),
                                new QualifiedNameType(datatype, IpsObjectType.POLICY_CMPT_TYPE));
                        dependencies.add(dependency);
                        addDetails(details, dependency, param, IParameterBFE.PROPERTY_DATATYPE);
                    }
                }
            }
        }
        return dependencies.toArray(new IDependency[dependencies.size()]);
    }

    @Override
    public List<IBFElement> getBFElementsWithoutParameters() {
        int size = simpleElements.size() + actions.size() + decisions.size();
        List<IBFElement> nodeList = new ArrayList<>(size);
        nodeList.addAll(simpleElements.getBackingList());
        nodeList.addAll(actions.getBackingList());
        nodeList.addAll(decisions.getBackingList());
        return nodeList;
    }

    private static class BFElementIpsObjectPartCollection<T extends IBFElement> extends IpsObjectPartCollection<T> {

        public BFElementIpsObjectPartCollection(BaseIpsObject ipsObject, Class<? extends T> partsClazz,
                Class<T> publishedInterface, String xmlTag) {
            super(ipsObject, partsClazz, publishedInterface, xmlTag);
        }

        public IBFElement newBFElement(final Location location, final BFElementType type) {
            IpsObjectPartInitializer<T> initializer = part -> {
                ((BFElement)part).setLocation(location);
                ((BFElement)part).setType(type);
            };

            return newPart(initializer);
        }

    }

}
