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
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.bf.commands.CreateBFElementCommand;
import org.junit.Before;
import org.junit.Test;

public class CreateBFElementCommandTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IBusinessFunction bf;
    private CreateBFElementCommand command;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
        bf = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf");
        command = new CreateBFElementCommand(BFElementType.ACTION_BUSINESSFUNCTIONCALL, bf, new Point(10, 10));
    }

    @Test
    public void testExecute() {
        assertTrue(bf.getBFElements().isEmpty());
        command.execute();
        assertEquals(1, bf.getBFElements().size());
    }

    @Test
    public void testRedo() {
        assertTrue(bf.getBFElements().isEmpty());
        command.execute();
        assertEquals(1, bf.getBFElements().size());
        command.undo();
        assertTrue(bf.getBFElements().isEmpty());
        command.redo();
        assertEquals(1, bf.getBFElements().size());
    }

    @Test
    public void testUndo() {
        assertTrue(bf.getBFElements().isEmpty());
        command.execute();
        assertEquals(1, bf.getBFElements().size());
        command.undo();
        assertTrue(bf.getBFElements().isEmpty());
    }

}
