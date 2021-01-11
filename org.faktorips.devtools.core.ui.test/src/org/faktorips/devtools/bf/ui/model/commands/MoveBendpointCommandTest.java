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
import static org.junit.Assert.fail;

import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.geometry.Point;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.bf.commands.BendpointCommand;
import org.faktorips.devtools.core.ui.bf.commands.MoveBendpointCommand;
import org.junit.Before;
import org.junit.Test;

public class MoveBendpointCommandTest extends AbstractIpsPluginTest {

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
        command = new MoveBendpointCommand(0, new Point(100, 100), cf);
    }

    @Test
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
        assertEquals(new Point(100, 100), cf.getBendpoints().get(0));
    }
}
