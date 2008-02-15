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

package org.faktorips.devtools.core;

import java.util.Locale;

import org.faktorips.fl.FunctionResolver;

/**
 * FunctionResolverFactories can be registered with the <i>flfunctionResolverFactory</i> extension point. 
 * The function resolvers of the registered factories augment the set of availabe formula language functions. 
 * 
 * @author Peter Erzberger
 */
public interface IFunctionResolverFactory {

    /**
     * Creates a new FunctionResolver with respect to the provided local. It is in the responsibility of the
     * factory provider if the locale is considered.
     */
    public FunctionResolver newFunctionResolver(Locale locale);
    
}
