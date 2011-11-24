/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.ipsobject;

/**
 * Implements the <em>Null-Object</em> pattern for {@link QualifiedNameType}.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 * 
 * @see QualifiedNameType
 */
public class NullQualifiedNameType extends QualifiedNameType {

    private static final long serialVersionUID = -7931101082356961576L;

    public NullQualifiedNameType() {
        super("", new NullIpsObjectType()); //$NON-NLS-1$
    }

    private static class NullIpsObjectType extends IpsObjectType {

        protected NullIpsObjectType() {
            super("", "", "", "", "", false, false, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        }

    }

}
