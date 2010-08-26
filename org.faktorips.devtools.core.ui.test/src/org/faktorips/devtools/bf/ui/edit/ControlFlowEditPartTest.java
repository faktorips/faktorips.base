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

package org.faktorips.devtools.bf.ui.edit;

import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.bf.edit.ControlFlowEditPart;

public class ControlFlowEditPartTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IBusinessFunction businessFunction;
    private IControlFlow controlFlow;
    private ControlFlowEditPart editPart;
    private boolean refreshChildrenCalled;
    private Shell shell;

    @Override
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

    public void testActivate() {
        editPart.activate();
        controlFlow.addBendpoint(0, new AbsoluteBendpoint(1, 1));
        assertTrue(refreshChildrenCalled);
    }

    public void testDeactivate() {
        editPart.activate();
        controlFlow.addBendpoint(0, new AbsoluteBendpoint(1, 1));
        assertTrue(refreshChildrenCalled);

        refreshChildrenCalled = false;
        editPart.deactivate();
        controlFlow.addBendpoint(1, new AbsoluteBendpoint(1, 1));
        assertFalse(refreshChildrenCalled);

    }
}
