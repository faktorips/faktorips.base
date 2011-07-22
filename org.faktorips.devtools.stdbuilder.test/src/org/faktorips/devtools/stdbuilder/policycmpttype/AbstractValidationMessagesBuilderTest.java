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

package org.faktorips.devtools.stdbuilder.policycmpttype;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.model.IInternationalString;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;

public abstract class AbstractValidationMessagesBuilderTest {

    protected static final String ROOT_FOLDER = "rootFolder";
    protected static final String TEST_PACK = "test.pack";

    public AbstractValidationMessagesBuilderTest() {
        super();
    }

    protected DefaultBuilderSet mockBuilderSet() {
        DefaultBuilderSet builderSet = mock(DefaultBuilderSet.class);
        when(builderSet.getInternalPackage(TEST_PACK, "")).thenReturn(ROOT_FOLDER);
        when(builderSet.getLanguageUsedInGeneratedSourceCode()).thenReturn(Locale.ENGLISH);
        return builderSet;
    }

    protected IIpsPackageFragment mockPackageFragment() {
        IFile file = mock(IFile.class);

        IFolder derivedFolder = mock(IFolder.class);
        when(derivedFolder.getFile(any(IPath.class))).thenReturn(file);

        IIpsSrcFolderEntry ipsSrcFolderEntry = mock(IIpsSrcFolderEntry.class);
        when(ipsSrcFolderEntry.getOutputFolderForDerivedJavaFiles()).thenReturn(derivedFolder);
        when(ipsSrcFolderEntry.getBasePackageNameForDerivedJavaClasses()).thenReturn(TEST_PACK);

        IIpsPackageFragmentRoot root = mock(IIpsPackageFragmentRoot.class);
        when(root.getIpsObjectPathEntry()).thenReturn(ipsSrcFolderEntry);

        IIpsPackageFragment pack = mock(IIpsPackageFragment.class);
        when(pack.getRoot()).thenReturn(root);
        return pack;
    }

    protected IValidationRule mockValidationRule(IPolicyCmptType type) {
        IValidationRule validationRule = mock(IValidationRule.class);
        when(validationRule.getIpsObject()).thenReturn(type);
        IInternationalString msgText = mock(IInternationalString.class);
        when(validationRule.getMessageText()).thenReturn(msgText);
        return validationRule;
    }

}