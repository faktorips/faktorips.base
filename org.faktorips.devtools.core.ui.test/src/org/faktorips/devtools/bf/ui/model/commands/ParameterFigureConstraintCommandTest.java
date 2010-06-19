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

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.bf.commands.ParameterFigureConstraintCommand;

public class ParameterFigureConstraintCommandTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IBusinessFunction bf;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
        bf = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf");
    }

    public void testExecute() {
        bf.setParameterRectangleSize(new Dimension(0, 0));
        Rectangle bounds = new Rectangle(-1, -1, 30, 40);
        ParameterFigureConstraintCommand command = new ParameterFigureConstraintCommand(bf, bounds);
        command.execute();
        assertEquals(new Dimension(30, 40), bf.getParameterRectangleSize());
    }

    public void testUndo() {
        bf.setParameterRectangleSize(new Dimension(0, 0));
        Rectangle bounds = new Rectangle(-1, -1, 30, 40);
        ParameterFigureConstraintCommand command = new ParameterFigureConstraintCommand(bf, bounds);
        command.execute();
        command.undo();
        assertEquals(new Dimension(0, 0), bf.getParameterRectangleSize());
    }

}
