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
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.IpsProductDefinitionPerspectiveFactory;

/**
 * This property tester defines two property tests to check active perspectives:
 * 
 * <li>
 * <em>isPerspectiveOpen</em> takes an perspective id as argument and checks whether this
 * perspective is active</li> <li>
 * <em>isProductDefinitionPerspectiveOpen</em> takes no argument and checks whether the ips product
 * definition perspective is open</li>
 * 
 * @author dirmeier
 */
public class IpsPerspectivePropertyTester extends PropertyTester {

    private static final Object PROPERTY_IS_PERSPECTIVE_OPEN = "isPerspectiveOpen"; //$NON-NLS-1$

    private static final Object PROPERTY_IS_PRODUCT_DEFINITION_PERSPECTIVE_OPEN = "isProductDefinitionPerspectiveOpen"; //$NON-NLS-1$

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (receiver instanceof IWorkbenchWindow) {
            final IWorkbenchWindow window = (IWorkbenchWindow)receiver;
            if (PROPERTY_IS_PERSPECTIVE_OPEN.equals(property)) {
                if (args.length != 1) {
                    throw new RuntimeException("invalid number of arguments in IpsPerspectivePropertyTester"); //$NON-NLS-1$
                }
                return isPerspectiveOpen(args[0], window);
            } else if (PROPERTY_IS_PRODUCT_DEFINITION_PERSPECTIVE_OPEN.equals(property)) {
                if (args.length != 0) {
                    throw new RuntimeException("invalid number of arguments in IpsPerspectivePropertyTester"); //$NON-NLS-1$
                }
                return isPerspectiveOpen(IpsProductDefinitionPerspectiveFactory.PRODUCTDEFINITIONPERSPECTIVE_ID, window);
            }
        }
        return false;
    }

    private boolean isPerspectiveOpen(Object id, final IWorkbenchWindow window) {
        IWorkbenchPage page = window.getActivePage();
        if (page != null) {
            IPerspectiveDescriptor persp = page.getPerspective();
            if (persp.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

}
