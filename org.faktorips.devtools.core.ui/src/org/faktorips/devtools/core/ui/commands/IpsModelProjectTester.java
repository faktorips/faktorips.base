/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.expressions.PropertyTester;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * This property tester is able to check if an {@link IIpsProject} is a modeling project.
 * 
 * @author noschinski2
 */
public class IpsModelProjectTester extends PropertyTester {

    public static final String PROPERTY_MODEL_IPS_PROJECT = "isModelIpsProject"; //$NON-NLS-1$

    public IpsModelProjectTester() {
        super();
    }

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

        if (!(receiver instanceof IIpsProject)) {
            return false;
        }

        if (property.equals(PROPERTY_MODEL_IPS_PROJECT)) {
            IIpsProject project = (IIpsProject)receiver;
            return project.isModelProject();
        }
        return false;
    }

}
