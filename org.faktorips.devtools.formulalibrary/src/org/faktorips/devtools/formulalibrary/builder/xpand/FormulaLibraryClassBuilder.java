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

package org.faktorips.devtools.formulalibrary.builder.xpand;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.formulalibrary.builder.xpand.model.XFormulaLibraryClass;
import org.faktorips.devtools.formulalibrary.internal.model.FormulaLibraryIpsObjectType;
import org.faktorips.devtools.formulalibrary.model.IFormulaLibrary;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.XpandBuilder;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Class to generate the {@link IFormulaLibrary}
 * 
 * @author frank
 */
public class FormulaLibraryClassBuilder extends XpandBuilder<XFormulaLibraryClass> {

    public static final String NAME = "FormulaLibraryClassBuilder"; //$NON-NLS-1$

    private MultiStatus buildStatus;

    public FormulaLibraryClassBuilder(StandardBuilderSet builderSet, GeneratorModelContext modelContext,
            ModelService modelService) {
        super(builderSet, modelContext, modelService, new LocalizedStringsSet(FormulaLibraryClassBuilder.class));
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return FormulaLibraryIpsObjectType.getInstance().equals(ipsSrcFile.getIpsObjectType());
    }

    @Override
    protected String getTemplate() {
        return "org::faktorips::devtools::formulalibrary::builder::xpand::template::FormulaLibrary::main"; //$NON-NLS-1$
    }

    @Override
    protected Class<XFormulaLibraryClass> getGeneratorModelNodeClass() {
        return XFormulaLibraryClass.class;
    }

    @Override
    public boolean isGenerateingArtifactsFor(IIpsObjectPartContainer ipsObjectPartContainer) {
        try {
            if (isBuilderFor(ipsObjectPartContainer.getIpsSrcFile())) {
                return true;
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return false;
    }

    @Override
    protected IIpsObject getSupportedIpsObject(IIpsObjectPartContainer ipsObjectPartContainer) {
        return ipsObjectPartContainer.getIpsObject();
    }

    @Override
    protected boolean isBuildingPublishedSourceFile() {
        return false;
    }

    @Override
    protected boolean generatesInterface() {
        return false;
    }

    @Override
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException {
        super.beforeBuild(ipsSrcFile, status);
        buildStatus = status;
    }

    public MultiStatus getBuildStatus() {
        return buildStatus;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
