/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.bf.ui.edit;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.bf.edit.BusinessFunctionEditPart;
import org.faktorips.devtools.core.ui.bf.edit.ParameterEditPart;

public class BusinessFunctionEditPartTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IBusinessFunction businessFunction;
    private BusinessFunctionEditPart editPart;
    private boolean refreshChildrenCalled;

    private Shell shell;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        shell = new Shell(Display.getCurrent());

        ipsProject = newIpsProject("TestProject");
        businessFunction = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        refreshChildrenCalled = false;
        ScrollingGraphicalViewer viewer = new ScrollingGraphicalViewer();
        viewer.createControl(shell);
        ScalableFreeformRootEditPart root = new ScalableFreeformRootEditPart();
        root.setViewer(viewer);
        viewer.setRootEditPart(root);

        editPart = new BusinessFunctionEditPart() {

            @Override
            public void refreshChildren() {
                super.refreshChildren();
                refreshChildrenCalled = true;
            }
        };
        editPart.setParent(root);
        editPart.setModel(businessFunction);
    }

    @Override
    public void tearDownExtension() {
        shell.dispose();
    }

    public void testActivate() {
        editPart.activate();
        businessFunction.newControlFlow();
        assertTrue(refreshChildrenCalled);
    }

    public void testDeactivate() {
        editPart.activate();
        businessFunction.newControlFlow();
        assertTrue(refreshChildrenCalled);

        editPart.deactivate();
        refreshChildrenCalled = false;
        businessFunction.newControlFlow();
        assertFalse(refreshChildrenCalled);
    }

    public void testContentsChanged() throws Exception {
        editPart.activate();
        IControlFlow cf = businessFunction.newControlFlow();
        assertTrue(refreshChildrenCalled);

        refreshChildrenCalled = false;
        cf.delete();
        assertTrue(refreshChildrenCalled);

        refreshChildrenCalled = false;
        IBusinessFunction bf2 = (IBusinessFunction)newIpsObject(ipsProject,
                BusinessFunctionIpsObjectType.getInstance(), "bf2");
        bf2.newControlFlow();
        assertFalse(refreshChildrenCalled);
    }

    @SuppressWarnings("unchecked")
    public void testRefreshChildren() {
        List<EditPart> childs = editPart.getChildren();
        assertEquals(0, childs.size());

        // creation of the ParameterEditPart
        editPart.refreshChildren();
        childs = editPart.getChildren();
        assertEquals(1, childs.size());
        assertTrue(childs.get(0) instanceof ParameterEditPart);

        // removal and new creation of the ParameterEditPart
        editPart.refreshChildren();
        childs = editPart.getChildren();
        assertEquals(1, childs.size());
        assertTrue(childs.get(0) instanceof ParameterEditPart);
    }
}
