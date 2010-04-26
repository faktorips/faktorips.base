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

package org.faktorips.devtools.bf.ui.edit;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.bf.edit.ActionEditPart;
import org.faktorips.devtools.core.ui.bf.edit.BusinessFunctionEditPart;
import org.faktorips.devtools.core.ui.bf.edit.BusinessFunctionEditPartFactory;
import org.faktorips.devtools.core.ui.bf.edit.ControlFlowEditPart;
import org.faktorips.devtools.core.ui.bf.edit.DecisionEditPart;
import org.faktorips.devtools.core.ui.bf.edit.EndEditPart;
import org.faktorips.devtools.core.ui.bf.edit.MergeEditPart;
import org.faktorips.devtools.core.ui.bf.edit.StartEditPart;

public class BusinessFunctionEditPartFactoryTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
    }

    public void testCreateEditPart() throws Exception {
        BusinessFunctionEditPartFactory factory = new BusinessFunctionEditPartFactory();
        IBusinessFunction bf = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        EditPart editPart = factory.createEditPart(null, bf);
        assertTrue(editPart instanceof BusinessFunctionEditPart);

        editPart = factory.createEditPart(null, bf.newControlFlow());
        assertTrue(editPart instanceof ControlFlowEditPart);

        editPart = factory.createEditPart(null, bf.newDecision(new Point(1, 1)));
        assertTrue(editPart instanceof DecisionEditPart);

        editPart = factory.createEditPart(null, bf.newMethodCallAction(new Point(1, 1)));
        assertTrue(editPart instanceof ActionEditPart);

        editPart = factory.createEditPart(null, bf.newOpaqueAction(new Point(1, 1)));
        assertTrue(editPart instanceof ActionEditPart);

        editPart = factory.createEditPart(null, bf.newBusinessFunctionCallAction(new Point(1, 1)));
        assertTrue(editPart instanceof ActionEditPart);

        editPart = factory.createEditPart(null, bf.newParameter());
        assertNull(editPart);

        editPart = factory.createEditPart(null, bf.newStart(new Point(1, 1)));
        assertTrue(editPart instanceof StartEditPart);

        editPart = factory.createEditPart(null, bf.newEnd(new Point(1, 1)));
        assertTrue(editPart instanceof EndEditPart);

        editPart = factory.createEditPart(null, bf.newMerge(new Point(1, 1)));
        assertTrue(editPart instanceof MergeEditPart);

        try {
            editPart = factory.createEditPart(null, null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            editPart = factory.createEditPart(null, newPolicyCmptType(ipsProject, "pctype"));
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

}
