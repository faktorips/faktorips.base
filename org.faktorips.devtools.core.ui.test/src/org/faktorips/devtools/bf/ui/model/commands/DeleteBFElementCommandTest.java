/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.bf.ui.model.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.draw2d.geometry.Point;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IActionBFE;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.bf.commands.DeleteBFElementCommand;
import org.junit.Before;
import org.junit.Test;

public class DeleteBFElementCommandTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IBusinessFunction bf;
    private DeleteBFElementCommand command;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
        bf = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf");
    }

    @Test
    public void testExecute() {
        IActionBFE action = bf.newOpaqueAction(new Point(10, 10));
        command = new DeleteBFElementCommand(bf, action);
        assertEquals(1, bf.getBFElements().size());
        command.execute();
        assertEquals(0, bf.getBFElements().size());

        action = bf.newOpaqueAction(new Point(10, 10));
        IControlFlow inCf = bf.newControlFlow();
        action.addIncomingControlFlow(inCf);
        IControlFlow outCf = bf.newControlFlow();
        action.addOutgoingControlFlow(outCf);
        command = new DeleteBFElementCommand(bf, action);
        command.execute();
        assertEquals(0, bf.getBFElements().size());
        assertTrue(action.getAllControlFlows().isEmpty());
    }

    @Test
    public void testUndoRedo() {
        IActionBFE action = bf.newOpaqueAction(new Point(10, 10));
        command = new DeleteBFElementCommand(bf, action);
        assertEquals(1, bf.getBFElements().size());
        command.execute();
        assertEquals(0, bf.getBFElements().size());
        command.undo();
        assertEquals(1, bf.getBFElements().size());
        command.redo();
        assertEquals(0, bf.getBFElements().size());
    }
}
