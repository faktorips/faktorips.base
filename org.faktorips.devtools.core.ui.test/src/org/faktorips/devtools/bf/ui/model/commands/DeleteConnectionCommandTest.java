/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.bf.ui.model.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.draw2d.geometry.Point;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IActionBFE;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.bf.commands.DeleteConnectionCommand;
import org.junit.Before;
import org.junit.Test;

public class DeleteConnectionCommandTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IBusinessFunction bf;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
        bf = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf");
    }

    @Test
    public void testCanExecute() {
        IControlFlow cf = bf.newControlFlow();
        DeleteConnectionCommand command = new DeleteConnectionCommand(bf, cf);
        assertTrue(command.canExecute());
    }

    @Test
    public void testExecute() {
        IActionBFE source = bf.newOpaqueAction(new Point(10, 10));
        IActionBFE target = bf.newOpaqueAction(new Point(10, 10));
        IControlFlow cf = bf.newControlFlow();
        cf.setSource(source);
        cf.setTarget(target);
        assertEquals(source, cf.getSource());
        assertEquals(target, cf.getTarget());
        DeleteConnectionCommand command = new DeleteConnectionCommand(bf, cf);
        command.execute();
        assertNull(cf.getSource());
        assertNull(cf.getTarget());
        assertTrue(cf.isDeleted());
    }

    @Test
    public void testRedoUndo() {
        IActionBFE source = bf.newOpaqueAction(new Point(10, 10));
        IActionBFE target = bf.newOpaqueAction(new Point(10, 10));
        IControlFlow cf = bf.newControlFlow();
        String cfId = cf.getId();
        cf.setSource(source);
        cf.setTarget(target);
        DeleteConnectionCommand command = new DeleteConnectionCommand(bf, cf);
        command.execute();
        assertNull(cf.getSource());
        assertNull(cf.getTarget());
        assertTrue(cf.isDeleted());

        command.undo();
        cf = bf.getControlFlow(cfId);
        assertEquals(source, cf.getSource());
        assertEquals(target, cf.getTarget());
        assertFalse(cf.isDeleted());
    }

}
