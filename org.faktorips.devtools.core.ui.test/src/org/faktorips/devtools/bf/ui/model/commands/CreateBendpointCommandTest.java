/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.bf.ui.model.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.draw2d.geometry.Point;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.bf.commands.BendpointCommand;
import org.faktorips.devtools.core.ui.bf.commands.CreateBendpointCommand;
import org.junit.Before;
import org.junit.Test;

public class CreateBendpointCommandTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private BendpointCommand command;
    private IBusinessFunction bf;
    private IControlFlow cf;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
        bf = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf");
        cf = bf.newControlFlow();
        command = new CreateBendpointCommand(0, new Point(10, 10), cf);
    }

    @Test
    public void testExecute() {
        assertTrue(cf.getBendpoints().isEmpty());
        command.execute();
        assertEquals(1, cf.getBendpoints().size());
        assertEquals(new Point(10, 10), cf.getBendpoints().get(0));
    }

    @Test
    public void testRedo() {
        command.execute();
        assertEquals(1, cf.getBendpoints().size());
        assertEquals(new Point(10, 10), cf.getBendpoints().get(0));
        command.undo();
        assertTrue(cf.getBendpoints().isEmpty());
        command.redo();
        assertEquals(1, cf.getBendpoints().size());
        assertEquals(new Point(10, 10), cf.getBendpoints().get(0));

    }

    @Test
    public void testUndo() {
        command.execute();
        assertEquals(1, cf.getBendpoints().size());
        assertEquals(new Point(10, 10), cf.getBendpoints().get(0));
        command.undo();
        assertTrue(cf.getBendpoints().isEmpty());
    }

}
