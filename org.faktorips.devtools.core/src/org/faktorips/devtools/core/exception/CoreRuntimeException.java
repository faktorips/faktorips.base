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

package org.faktorips.devtools.core.exception;

import org.eclipse.core.runtime.CoreException;

/**
 * The {@link CoreRuntimeException} is intended to replace the {@link CoreException}. Checked
 * Exceptions should be removed from method signatures.
 * <p>
 * During the migration phase, to avoid CoreExceptions in client code, catch all
 * {@link CoreException}s thrown by framework/FIPS-Devtools code, create a
 * {@link CoreRuntimeException} and throw it instead. Use the
 * {@link CoreRuntimeException#CoreRuntimeException(CoreException)} constructor.
 * <p>
 * As the {@link CoreRuntimeException} is an (unchecked) {@link RuntimeException} it is not declared
 * by method signatures explicitly, which is what we want to avoid. If one of your methods throws a
 * {@link CoreRuntimeException}, document that it does and in which cases this happens.
 * 
 * @author Stefan Widmaier, FaktorZehn AG
 */
public class CoreRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -2861599541541109752L;

    public CoreRuntimeException(CoreException cause) {
        super(cause);
    }

    public CoreRuntimeException(String message, CoreException cause) {
        super(message, cause);
    }
}
