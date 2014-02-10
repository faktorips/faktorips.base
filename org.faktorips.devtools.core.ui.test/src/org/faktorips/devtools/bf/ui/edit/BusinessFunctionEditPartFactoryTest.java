/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.bf.ui.edit;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
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
import org.junit.Before;
import org.junit.Test;

public class BusinessFunctionEditPartFactoryTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
    }

    @Test
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
