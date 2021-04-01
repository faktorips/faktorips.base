/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.bf.ui.model.commands;

import static org.junit.Assert.assertEquals;

import org.eclipse.draw2d.geometry.Rectangle;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.bf.commands.ChangeConstraintCommand;
import org.faktorips.devtools.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.model.bf.IActionBFE;
import org.faktorips.devtools.model.bf.IBusinessFunction;
import org.faktorips.devtools.model.bf.Location;
import org.faktorips.devtools.model.bf.Size;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;

/** @deprecated for removal since 21.6 */
@Deprecated
public class ChangeConstraintCommandTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IBusinessFunction bf;
    private ChangeConstraintCommand command;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
        bf = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf");
    }

    @Test
    public void testExecute() {
        IActionBFE action = bf.newOpaqueAction(new Location(10, 10));
        command = new ChangeConstraintCommand(action, new Rectangle(100, 50, 200, 250));
        command.execute();
        assertEquals(new Location(100, 50), action.getLocation());
        assertEquals(new Size(200, 250), action.getSize());
    }

    @Test
    public void testUndoRedo() {
        IActionBFE action = bf.newOpaqueAction(new Location(10, 10));
        Location location = action.getLocation();
        Size size = action.getSize();
        command = new ChangeConstraintCommand(action, new Rectangle(100, 50, 200, 250));
        command.execute();
        assertEquals(new Location(100, 50), action.getLocation());
        assertEquals(new Size(200, 250), action.getSize());
        command.undo();
        assertEquals(location, action.getLocation());
        assertEquals(size, action.getSize());
        command.redo();
        assertEquals(new Location(100, 50), action.getLocation());
        assertEquals(new Size(200, 250), action.getSize());
    }
}
