/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.fl;

import java.util.Locale;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.IFunctionResolverFactory;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.fl.FunctionResolver;

/**
 * This AbstractProjectRelatedFunctionResolverFactory can be registered with the
 * <i>flfunctionResolverFactory</i> extension point. Subclasses resolves functions according to a
 * related {@link IIpsProject}. Therefore the Interface is enhanced and calling
 * {@link #newFunctionResolver(Locale)} is forbidden. Call the new method
 * {@link #newFunctionResolver(IIpsProject, Locale)} instead.
 * 
 * @author Peter Erzberger
 */
public abstract class AbstractProjectRelatedFunctionResolverFactory implements IFunctionResolverFactory {

    /**
     * Creates a new FunctionResolver with respect to the provided locale and the related project.
     * It is in the responsibility of the factory provider if the locale is considered.
     */
    public abstract FunctionResolver<JavaCodeFragment> newFunctionResolver(IIpsProject ipsProject, Locale locale);

    /**
     * This methods throws an {@link UnsupportedOperationException}, because the resolving of
     * functions is projected related. Call {@link #newFunctionResolver(IIpsProject, Locale)}
     * instead.
     * 
     */
    @Override
    public final FunctionResolver<JavaCodeFragment> newFunctionResolver(Locale locale) {
        throw new UnsupportedOperationException();
    }

}
