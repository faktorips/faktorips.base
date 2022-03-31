/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Locale;

import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.pctype.IValidationRuleMessageText;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

public abstract class AbstractValidationMessagesBuilderTest {

    private static final String TEST_VALIDATION_MESSAGES = "test.validation-messages";

    protected static final String ROOT_FOLDER = "rootFolder";
    protected static final String TEST_PACK = "test.pack";

    public AbstractValidationMessagesBuilderTest() {
        super();
    }

    protected StandardBuilderSet mockBuilderSet() {
        StandardBuilderSet builderSet = mock(StandardBuilderSet.class);
        when(builderSet.getLanguageUsedInGeneratedSourceCode()).thenReturn(Locale.ENGLISH);
        when(builderSet.getValidationMessageBundleBaseName(any(IIpsSrcFolderEntry.class))).thenReturn("test");
        return builderSet;
    }

    protected IIpsPackageFragment mockPackageFragment() {
        AFile file = mock(AFile.class);

        AFolder derivedFolder = mock(AFolder.class);
        when(derivedFolder.getFile(any(Path.class))).thenReturn(file);

        IIpsSrcFolderEntry ipsSrcFolderEntry = mock(IIpsSrcFolderEntry.class);
        when(ipsSrcFolderEntry.getOutputFolderForDerivedJavaFiles()).thenReturn(derivedFolder);
        when(ipsSrcFolderEntry.getBasePackageNameForDerivedJavaClasses()).thenReturn(TEST_PACK);
        when(ipsSrcFolderEntry.getValidationMessagesBundle()).thenReturn(TEST_VALIDATION_MESSAGES);

        IIpsPackageFragmentRoot root = mock(IIpsPackageFragmentRoot.class);
        when(root.getIpsObjectPathEntry()).thenReturn(ipsSrcFolderEntry);

        IIpsPackageFragment pack = mock(IIpsPackageFragment.class);
        when(pack.getRoot()).thenReturn(root);
        return pack;
    }

    protected IValidationRule mockValidationRule(IPolicyCmptType type) {
        IValidationRule validationRule = mock(IValidationRule.class);
        when(validationRule.getIpsObject()).thenReturn(type);
        IValidationRuleMessageText msgText = mock(IValidationRuleMessageText.class);
        when(validationRule.getMessageText()).thenReturn(msgText);
        return validationRule;
    }

}