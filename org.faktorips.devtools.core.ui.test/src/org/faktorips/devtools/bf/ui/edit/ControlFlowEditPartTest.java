/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.bf.ui.edit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.bf.edit.ControlFlowEditPart;
import org.faktorips.devtools.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.model.bf.IBusinessFunction;
import org.faktorips.devtools.model.bf.IControlFlow;
import org.faktorips.devtools.model.bf.Location;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;

/** @deprecated for removal since 21.6 */
@Deprecated
public class ControlFlowEditPartTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IBusinessFunction businessFunction;
    private IControlFlow controlFlow;
    private ControlFlowEditPart editPart;
    private boolean refreshChildrenCalled;
    private Shell shell;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
        businessFunction = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        controlFlow = businessFunction.newControlFlow();
        refreshChildrenCalled = false;
        ScrollingGraphicalViewer viewer = new ScrollingGraphicalViewer();
        shell = new Shell(Display.getCurrent());
        viewer.createControl(shell);
        ScalableFreeformRootEditPart root = new ScalableFreeformRootEditPart();
        root.setViewer(viewer);
        viewer.setRootEditPart(root);

        editPart = new ControlFlowEditPart() {

            @Override
            protected void refreshVisuals() {
                super.refreshVisuals();
                refreshChildrenCalled = true;
            }
        };
        editPart.setModel(controlFlow);
        editPart.setParent(root);
    }

    @Override
    public void tearDownExtension() {
        shell.dispose();
    }

    @Test
    public void testActivate() {
        editPart.activate();
        controlFlow.addBendpoint(0, new Location(1, 1));
        assertTrue(refreshChildrenCalled);
    }

    @Test
    public void testDeactivate() {
        editPart.activate();
        controlFlow.addBendpoint(0, new Location(1, 1));
        assertTrue(refreshChildrenCalled);

        refreshChildrenCalled = false;
        editPart.deactivate();
        controlFlow.addBendpoint(1, new Location(1, 1));
        assertFalse(refreshChildrenCalled);

    }
}
