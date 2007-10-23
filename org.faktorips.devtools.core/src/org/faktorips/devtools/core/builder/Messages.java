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

package org.faktorips.devtools.core.builder;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.builder.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String AbstractParameterIdentifierResolver_msgExceptionWhileResolvingIdentifierAtThis;

    public static String AbstractParameterIdentifierResolver_msgResolverMustBeSet;

	public static String AbstractParameterIdentifierResolver_msgDatatypeCanNotBeResolved;

	public static String AbstractParameterIdentifierResolver_msgErrorParameterDatatypeResolving;

	public static String AbstractParameterIdentifierResolver_msgErrorDuringEnumDatatypeResolving;

	public static String AbstractParameterIdentifierResolver_msgErrorRetrievingAttribute;

	public static String AbstractParameterIdentifierResolver_msgErrorNoAttribute;

	public static String AbstractParameterIdentifierResolver_msgErrorNoDatatypeForAttribute;

    public static String AbstractParameterIdentifierResolver_msgNoDatatypeForProductCmptTypeAttribute;

    public static String IpsBuilder_ipsSrcFileNotParsable;

    public static String IpsBuilder_subTaskMissingDerivedFolders;

	public static String JetJavaSourceFileBuilder_name;

	public static String IpsBuilder_msgBuildResults;

	public static String IpsBuilder_msgInvalidProperties;

    public static String IpsBuilder_validatingProject;

    public static String IpsBuilder_preparingBuild;

    public static String IpsBuilder_startFullBuild;

    public static String IpsBuilder_startIncrementalBuild;

    public static String IpsBuilder_finishBuild;

    public static String IpsBuilder_deleting;

    public static String IpsBuilder_building;

    public static String AbstractParameterIdentifierResolver_msgAttributeMissing;
}
