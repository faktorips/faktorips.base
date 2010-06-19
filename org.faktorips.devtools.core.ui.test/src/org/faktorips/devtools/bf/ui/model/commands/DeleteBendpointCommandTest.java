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

import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.geometry.Point;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.bf.commands.BendpointCommand;
import org.faktorips.devtools.core.ui.bf.commands.DeleteBendpointCommand;

public class DeleteBendpointCommandTest extends AbstractIpsPluginTest {

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
        command = new DeleteBendpointCommand(0, cf);
    }

    public void testExecute() {
        try {
            command.execute();
            fail(); // since no bend point has been added
        } catch (IndexOutOfBoundsException e) {
        }

        cf.addBendpoint(0, new AbsoluteBendpoint(10, 10));
        assertEquals(1, cf.getBendpoints().size());
        assertEquals(new Point(10, 10), cf.getBendpoints().get(0));
        command.execute();
        assertTrue(cf.getBendpoints().isEmpty());
    }
}
