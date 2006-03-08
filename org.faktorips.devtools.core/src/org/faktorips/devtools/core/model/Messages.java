/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.model.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String EnumValueSet_msgValueNotInEnumeration;

	public static String EnumValueSet_msgNotAnEnumValueset;

	public static String EnumValueSet_msgValueNotParsable;

	public static String EnumValueSet_msgDuplicateValue;

	public static String Range_msgValueNotInRange;

	public static String Range_msgTypeOfValuesetNotMatching;

	public static String Range_msgNoStepDefinedInSubset;

	public static String Range_msgStepMismatch;

	public static String Range_msgLowerBoundViolation;

	public static String Range_msgUpperBoundViolation;

	public static String Range_msgValueNotParsable;

	public static String Range_msgValueNotComparable;

	public static String Range_msgUnknownDatatype;

	public static String Range_msgLowerboundGreaterUpperbound;

	public static String Range_msgPropertyValueNotParsable;

	public static String IpsObjectType_nameBusinessFunction;

	public static String IpsObjectType_namePolicyClass;

	public static String IpsObjectType_nameProductClass;

	public static String IpsObjectType_nameTableStructure;

	public static String IpsObjectType_nameProductComponent;

	public static String IpsObjectType_nameTableContents;
}
