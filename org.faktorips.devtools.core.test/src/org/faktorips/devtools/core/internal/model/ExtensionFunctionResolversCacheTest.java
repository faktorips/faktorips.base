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

package org.faktorips.devtools.core.internal.model;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.IFunctionResolverFactory;
import org.faktorips.devtools.core.builder.ExtendedExprCompiler;
import org.faktorips.devtools.core.fl.AbstractProjectRelatedFunctionResolverFactory;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectProperties;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.fl.FunctionResolver;
import org.junit.Before;
import org.junit.Test;

public class ExtensionFunctionResolversCacheTest {

    private ExtensionFunctionResolversCache extensionFunctionResolversCache;
    private ExtendedExprCompiler compiler;
    private IFunctionResolverFactory<JavaCodeFragment> resolverFactory;
    private AbstractProjectRelatedFunctionResolverFactory<JavaCodeFragment> projectRelatedResolverFactory;
    private List<IFunctionResolverFactory<JavaCodeFragment>> resolverFactories;
    private FunctionResolver<JavaCodeFragment> resolver;
    private FunctionResolver<JavaCodeFragment> projectRelatedResolver;
    private IIpsProject ipsProject;

    @Before
    public void setUp() {
        ipsProject = mock(IIpsProject.class);
        IIpsModel ipsModel = mock(IIpsModel.class);
        when(ipsProject.getIpsModel()).thenReturn(ipsModel);
        compiler = mock(ExtendedExprCompiler.class);

        setUpResolverFactories();
        setUpProjectProperties();
        setUpResolvers();

        extensionFunctionResolversCache = new ExtensionFunctionResolversCache(ipsProject, resolverFactories);
    }

    @SuppressWarnings("unchecked")
    protected void setUpResolverFactories() {
        resolverFactory = mock(IFunctionResolverFactory.class);
        projectRelatedResolverFactory = mock(AbstractProjectRelatedFunctionResolverFactory.class);

        resolverFactories = new ArrayList<IFunctionResolverFactory<JavaCodeFragment>>();
        resolverFactories.add(resolverFactory);
        resolverFactories.add(projectRelatedResolverFactory);
    }

    protected void setUpProjectProperties() {
        IpsProjectProperties projectProperties = mock(IpsProjectProperties.class);
        when(ipsProject.getReadOnlyProperties()).thenReturn(projectProperties);
        when(projectProperties.isActive(resolverFactory)).thenReturn(true);
        when(projectProperties.isActive(projectRelatedResolverFactory)).thenReturn(true);
    }

    protected void setUpResolvers() {
        @SuppressWarnings("unchecked")
        FunctionResolver<JavaCodeFragment> mockResolver = mock(FunctionResolver.class);
        resolver = mockResolver;
        @SuppressWarnings("unchecked")
        FunctionResolver<JavaCodeFragment> mockProjectRelatedResolver = mock(FunctionResolver.class);
        projectRelatedResolver = mockProjectRelatedResolver;
        when(resolverFactory.newFunctionResolver(any(Locale.class))).thenReturn(resolver);
        when(projectRelatedResolverFactory.newFunctionResolver(eq(ipsProject), any(Locale.class))).thenReturn(
                projectRelatedResolver);
    }

    @Test
    public void testAddFunctionResolvers() {
        extensionFunctionResolversCache.addExtensionFunctionResolversToCompiler(compiler);
        verify(compiler).add(resolver);
        verify(compiler).add(projectRelatedResolver);
    }

    @Test
    public void testAddCachedFunctionResolvers() {
        extensionFunctionResolversCache.addExtensionFunctionResolversToCompiler(compiler);
        extensionFunctionResolversCache.addExtensionFunctionResolversToCompiler(compiler);

        verify(resolverFactory).newFunctionResolver(any(Locale.class));
        verify(projectRelatedResolverFactory).newFunctionResolver(eq(ipsProject), any(Locale.class));
    }

    @Test
    public void testAddNewFunctionResolvers() {
        extensionFunctionResolversCache.addExtensionFunctionResolversToCompiler(compiler);
        extensionFunctionResolversCache.clearCache();
        extensionFunctionResolversCache.addExtensionFunctionResolversToCompiler(compiler);

        verify(resolverFactory, times(2)).newFunctionResolver(any(Locale.class));
        verify(projectRelatedResolverFactory, times(2)).newFunctionResolver(eq(ipsProject), any(Locale.class));
    }

}
