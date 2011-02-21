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

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IActionBFE;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.bf.commands.ChangeConstraintCommand;
import org.junit.Before;
import org.junit.Test;

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
        IActionBFE action = bf.newOpaqueAction(new Point(10, 10));
        command = new ChangeConstraintCommand(action, new Rectangle(100, 50, 200, 250));
        command.execute();
        assertEquals(new Point(100, 50), action.getLocation());
        assertEquals(new Dimension(200, 250), action.getSize());
    }

    @Test
    public void testUndoRedo() {
        IActionBFE action = bf.newOpaqueAction(new Point(10, 10));
        Point location = action.getLocation();
        Dimension size = action.getSize();
        command = new ChangeConstraintCommand(action, new Rectangle(100, 50, 200, 250));
        command.execute();
        assertEquals(new Point(100, 50), action.getLocation());
        assertEquals(new Dimension(200, 250), action.getSize());
        command.undo();
        assertEquals(location, action.getLocation());
        assertEquals(size, action.getSize());
        command.redo();
        assertEquals(new Point(100, 50), action.getLocation());
        assertEquals(new Dimension(200, 250), action.getSize());
    }
}
