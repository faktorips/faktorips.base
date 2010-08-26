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

import org.eclipse.draw2d.geometry.Point;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.bf.commands.BendpointCommand;
import org.faktorips.devtools.core.ui.bf.commands.CreateBendpointCommand;

public class CreateBendpointCommandTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private BendpointCommand command;
    private IBusinessFunction bf;
    private IControlFlow cf;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
        bf = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf");
        cf = bf.newControlFlow();
        command = new CreateBendpointCommand(0, new Point(10, 10), cf);
    }

    public void testExecute() {
        assertTrue(cf.getBendpoints().isEmpty());
        command.execute();
        assertEquals(1, cf.getBendpoints().size());
        assertEquals(new Point(10, 10), cf.getBendpoints().get(0));
    }

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

    public void testUndo() {
        command.execute();
        assertEquals(1, cf.getBendpoints().size());
        assertEquals(new Point(10, 10), cf.getBendpoints().get(0));
        command.undo();
        assertTrue(cf.getBendpoints().isEmpty());
    }

}
