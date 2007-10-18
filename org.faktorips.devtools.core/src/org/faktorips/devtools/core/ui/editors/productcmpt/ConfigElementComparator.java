/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IConfigElement;

/**
 * Comparator for comparing <code>IConfigElement</code>s. A config element is considered to 
 * be less than another if it underlying attribute is defined in a policy component type 
 * which is a supertype of the policy component type defining the attribute for the other 
 * <code>IConfigElement</code>.
 * <p>
 * If both <code>IConfigElement</code>s are defined by attributes of the same policy component
 * type, the <code>IConfigElement</code> is considered to be less which is listed first by this
 * policy component type.
 * <p>
 * If an attribute or policy component type can not be found or a <code>CoreException</code>
 * occurs, both elements are considered to be equal.
 * <p>
 * Example:
 * <p>
 * Policy Component Type "CollisionCoverage" defines an attribute, called "deduction".<br>
 * Policy Component Type "Coverage" defines another attribute, "sumInsured".<br>
 * Coverage is the supertype for CollisionCoverage.<br>
 * The IConfigElement based on sumInsured will be considered to be less than the IConfigElement
 * based on the attribute sumInsured. 
 * 
 * @author Thorsten Guenther
 */
public class ConfigElementComparator implements Comparator {

	/**
	 * {@inheritDoc}
	 */
	public int compare(Object o1, Object o2) {
		try {
			IPolicyCmptTypeAttribute a1 = ((IConfigElement)o1).findPcTypeAttribute();
			IPolicyCmptTypeAttribute a2 = ((IConfigElement)o2).findPcTypeAttribute();
			
			if (a1 == null || a2 == null) {
				return 0;
			}
			
			IPolicyCmptType t1 = (IPolicyCmptType)a1.getParent();
			IPolicyCmptType t2 = (IPolicyCmptType)a2.getParent();

			if (t1 == null || t2 == null) {
				return 0;
			}
			
			if (t1.getSupertypeHierarchy().isSupertypeOf(t1, t2) || t2.getSupertypeHierarchy().isSupertypeOf(t1, t2)) {
				return -1;
			}
			else if (t1.getQualifiedName().equals(t2.getQualifiedName())){
				List attrs = Arrays.asList(t1.getPolicyCmptTypeAttributes()); 
			    return attrs.indexOf(a1) - attrs.indexOf(a2);
			}
			else {
				return 1;
			}
		} catch (CoreException e) {
			IpsPlugin.log(e);
		}
		return 0;
	}
}
