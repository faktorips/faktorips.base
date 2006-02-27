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

	public static String AbstractParameterIdentifierResolver_msgResolverMustBeSet;

	public static String AbstractParameterIdentifierResolver_msgDatatypeCanNotBeResolved;

	public static String AbstractParameterIdentifierResolver_3;

	public static String AbstractParameterIdentifierResolver_4;

	public static String AbstractParameterIdentifierResolver_msgErrorParameterDatatypeResolving;

	public static String AbstractParameterIdentifierResolver_6;

	public static String AbstractParameterIdentifierResolver_7;

	public static String AbstractParameterIdentifierResolver_msgErrorDuringEnumDatatypeResolving;

	public static String AbstractParameterIdentifierResolver_msgErrorRetrievingAttribute;

	public static String AbstractParameterIdentifierResolver_11;

	public static String AbstractParameterIdentifierResolver_12;

	public static String AbstractParameterIdentifierResolver_msgErrorNoAttribute;

	public static String AbstractParameterIdentifierResolver_14;

	public static String AbstractParameterIdentifierResolver_15;

	public static String AbstractParameterIdentifierResolver_msgErrorNoDatatypeForAttribute;

	public static String AbstractParameterIdentifierResolver_17;

	public static String AbstractParameterIdentifierResolver_18;

	public static String AbstractParameterIdentifierResolver_msgErrorAttributeDatatypeResolving;

	public static String AbstractParameterIdentifierResolver_21;

	public static String AbstractParameterIdentifierResolver_22;

	public static String JetJavaSourceFileBuilder_name;

	public static String IpsBuilder_msgErrorExceptionDuringBuild;

	public static String IpsBuilder_5;

	public static String IpsBuilder_6;

	public static String IpsBuilder_msgFullBuildResults;

	public static String IpsBuilder_msgIncrementalBuildResults;
}
