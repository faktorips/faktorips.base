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

package org.faktorips.devtools.bf.ui.model.commands;

import org.eclipse.draw2d.geometry.Point;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.bf.IDecisionBFE;
import org.faktorips.devtools.core.model.bf.IParameterBFE;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.ui.bf.commands.ConnectionCommand;

public class ConnectionCommandTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private ConnectionCommand command;
    private IBusinessFunction bf;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
        command = new ConnectionCommand(false);
        bf = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf");
        command.setBusinessFunction(bf);

    }

    public void testCanExecute() throws Exception {
        assertFalse(command.canExecute());

        IBFElement source = bf.newStart(new Point(10, 10));
        IControlFlow out = bf.newControlFlow();
        source.addOutgoingControlFlow(out);
        command.setSource(source);
        assertFalse(command.canExecute());

        bf = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf1");
        command = new ConnectionCommand(false);
        source = bf.newEnd(new Point(10, 10));
        command.setSource(source);
        assertFalse(command.canExecute());

        bf = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf2");
        command = new ConnectionCommand(false);
        source = bf.newMerge(new Point(10, 10));
        out = bf.newControlFlow();
        source.addOutgoingControlFlow(out);
        command.setSource(source);
        assertFalse(command.canExecute());

        bf = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf21");
        command = new ConnectionCommand(false);
        source = bf.newOpaqueAction(new Point(10, 10));
        out = bf.newControlFlow();
        source.addOutgoingControlFlow(out);
        command.setSource(source);
        assertFalse(command.canExecute());

        source.delete();
        source = bf.newBusinessFunctionCallAction(new Point(10, 10));
        source.addOutgoingControlFlow(out);
        command.setSource(source);
        assertFalse(command.canExecute());

        source.delete();
        source = bf.newMethodCallAction(new Point(10, 10));
        source.addOutgoingControlFlow(out);
        command.setSource(source);
        assertFalse(command.canExecute());

        bf = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf3");
        command = new ConnectionCommand(false);
        source = bf.newOpaqueAction(new Point(100, 100));
        command.setSource(source);
        command.setTarget(source);
        assertFalse(command.canExecute());

        bf = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf4");
        command = new ConnectionCommand(false);
        IBFElement target = bf.newDecision(new Point(100, 100));
        IControlFlow in = bf.newControlFlow();
        target.addIncomingControlFlow(in);
        source = bf.newOpaqueAction(new Point(10, 10));
        command.setSource(source);
        command.setTarget(target);
        assertFalse(command.canExecute());

        bf = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf41");
        command = new ConnectionCommand(false);
        target = bf.newMethodCallDecision(new Point(100, 100));
        in = bf.newControlFlow();
        target.addIncomingControlFlow(in);
        source = bf.newOpaqueAction(new Point(10, 10));
        command.setSource(source);
        command.setTarget(target);
        assertFalse(command.canExecute());

        bf = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf5");
        command = new ConnectionCommand(false);
        source = bf.newOpaqueAction(new Point(100, 100));
        command.setSource(source);
        out = bf.newControlFlow();
        source.addOutgoingControlFlow(out);
        target = bf.newOpaqueAction(new Point(200, 200));
        command.setTarget(target);
        assertFalse(command.canExecute());

        bf = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf6");
        command = new ConnectionCommand(false);
        source = bf.newOpaqueAction(new Point(100, 100));
        command.setSource(source);
        target = bf.newOpaqueAction(new Point(200, 200));
        in = bf.newControlFlow();
        target.addIncomingControlFlow(in);
        command.setTarget(target);
        assertFalse(command.canExecute());
    }

    public void testExecute() {
        IBFElement source = bf.newOpaqueAction(new Point(10, 10));
        IBFElement target = bf.newOpaqueAction(new Point(100, 100));
        command.setSource(source);
        command.setTarget(target);
        assertEquals(0, bf.getControlFlows().size());
        command.execute();
        assertEquals(1, bf.getControlFlows().size());
        assertEquals(bf.getControlFlows().get(0), source.getOutgoingControlFlow().get(0));
        assertEquals(target, source.getOutgoingControlFlow().get(0).getTarget());
    }

    public void testSetDefaultConditionValueForBooleanDecisionSourceNode() throws Exception {
        // by default a new decision is created with the datatype set to boolean
        IDecisionBFE source = bf.newDecision(new Point(10, 10));
        IBFElement target = bf.newOpaqueAction(new Point(10, 10));
        command.setSource(source);
        command.setTarget(target);
        command.execute();
        assertEquals("true", command.getControlFlow().getConditionValue());

        source = bf.newMethodCallDecision(new Point(10, 10));
        source.setTarget("aPolicy");
        source.setExecutableMethodName("aMethod");
        IParameterBFE param = bf.newParameter();
        param.setDatatype("aPolicy");
        param.setName("aPolicy");
        IPolicyCmptType aPolicy = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "aPolicy");
        IMethod method = aPolicy.newMethod();
        method.setDatatype(Datatype.BOOLEAN.getQualifiedName());
        method.setName("aMethod");
        target = bf.newOpaqueAction(new Point(10, 10));
        command.setSource(source);
        command.setTarget(target);
        command.execute();
        assertEquals("true", command.getControlFlow().getConditionValue());

        source = bf.newDecision(new Point(10, 10));
        target = bf.newOpaqueAction(new Point(10, 10));
        IControlFlow cf = bf.newControlFlow();
        cf.setConditionValue(Boolean.TRUE.toString());
        source.addOutgoingControlFlow(cf);
        command.setSource(source);
        command.setTarget(target);
        command.execute();
        assertEquals("false", command.getControlFlow().getConditionValue());

        source = bf.newDecision(new Point(10, 10));
        target = bf.newOpaqueAction(new Point(10, 10));
        cf = bf.newControlFlow();
        cf.setConditionValue(Boolean.FALSE.toString());
        source.addOutgoingControlFlow(cf);
        command.setSource(source);
        command.setTarget(target);
        command.execute();
        assertEquals("true", command.getControlFlow().getConditionValue());

        source = bf.newDecision(new Point(10, 10));
        target = bf.newOpaqueAction(new Point(10, 10));
        cf = bf.newControlFlow();
        cf.setConditionValue(Boolean.TRUE.toString());
        source.addOutgoingControlFlow(cf);
        cf = bf.newControlFlow();
        cf.setConditionValue(Boolean.FALSE.toString());
        source.addOutgoingControlFlow(cf);
        command.setSource(source);
        command.setTarget(target);
        command.execute();
        assertEquals("", command.getControlFlow().getConditionValue());
    }

    public void testExecuteReconnect() {
        command = new ConnectionCommand(true);
        command.setBusinessFunction(bf);
        IBFElement source = bf.newOpaqueAction(new Point(10, 10));
        IBFElement target = bf.newOpaqueAction(new Point(100, 100));

        IControlFlow cf = bf.newControlFlow();
        cf.setSource(source);
        cf.setTarget(target);
        command.setControlFlow(cf);

        // reconnect target
        IBFElement newtarget = bf.newOpaqueAction(new Point(100, 100));
        command.setTarget(newtarget);
        command.setSource(null);
        command.execute();
        assertEquals(newtarget, cf.getTarget());
        assertEquals(source, cf.getSource());
        assertTrue(target.getIncomingControlFlow().isEmpty());

        // reconnect source
        cf.setSource(source);
        cf.setTarget(target);
        IBFElement newSource = bf.newOpaqueAction(new Point(100, 100));
        command.setSource(newSource);
        command.setTarget(null);
        command.execute();
        assertEquals(newSource, cf.getSource());
        assertEquals(target, cf.getTarget());
        assertTrue(target.getOutgoingControlFlow().isEmpty());
    }

    public void testUndoRedo() {
        IBFElement source = bf.newOpaqueAction(new Point(10, 10));
        IBFElement target = bf.newOpaqueAction(new Point(100, 100));
        command.setSource(source);
        command.setTarget(target);
        assertEquals(0, bf.getControlFlows().size());
        command.execute();
        assertEquals(1, bf.getControlFlows().size());
        assertEquals(bf.getControlFlows().get(0), source.getOutgoingControlFlow().get(0));
        assertEquals(target, source.getOutgoingControlFlow().get(0).getTarget());
        command.undo();
        assertTrue(bf.getControlFlows().isEmpty());
        assertTrue(source.getOutgoingControlFlow().isEmpty());
        assertTrue(target.getIncomingControlFlow().isEmpty());
        command.redo();
        assertEquals(1, bf.getControlFlows().size());
        assertEquals(bf.getControlFlows().get(0), source.getOutgoingControlFlow().get(0));
        assertEquals(target, source.getOutgoingControlFlow().get(0).getTarget());
    }

    public void testExecuteWithBooleanDecisionSourceNode() {
        IDecisionBFE decision = bf.newDecision(new Point(10, 10));
        decision.setName("decision");
        decision.setDatatype(Datatype.BOOLEAN.getQualifiedName());
        IBFElement action1 = bf.newOpaqueAction(new Point(10, 10));
        action1.setName("action1");
        IBFElement action2 = bf.newOpaqueAction(new Point(10, 10));
        action2.setName("action2");

        command.setSource(decision);
        command.setTarget(action1);
        command.execute();
        assertEquals(Boolean.TRUE.toString(), command.getControlFlow().getConditionValue());

        command.setSource(decision);
        command.setTarget(action2);
        command.execute();
        assertEquals(Boolean.FALSE.toString(), command.getControlFlow().getConditionValue());

        command = new ConnectionCommand(false);
        command.setBusinessFunction(bf);

        decision.removeAllOutgoingControlFlows();
        action1.removeAllIncommingControlFlows();
        action2.removeAllIncommingControlFlows();

        IControlFlow cf = bf.newControlFlow();
        cf.setSource(decision);
        cf.setTarget(action1);
        cf.setConditionValue(Boolean.TRUE.toString());

        command.setSource(decision);
        command.setTarget(action1);
        command.execute();
        assertEquals(Boolean.FALSE.toString(), command.getControlFlow().getConditionValue());
    }

}
