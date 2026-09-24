/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.enums.refactor;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.core.AbstractIpsRefactoringTest;
import org.faktorips.devtools.core.refactor.IpsRefactoringModificationSet;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.util.StringUtil;
import org.junit.jupiter.api.Test;

public class RenameEnumLiteralNameAttributeValueProcessorTest extends AbstractIpsRefactoringTest {

    @Test
    public void testRenameEnumLiteralNameAttributeValue() {
        performRenameRefactoring(enumLiteralNameAttributeValue, "bar");
        assertThat(enumLiteralNameAttributeValue.getStringValue(), is("bar"));
    }

    @Test
    public void testRenameEnumLiteralNameAttributeValue_PersistsAfterProbeRunIsUndone() throws Exception {
        RenameEnumLiteralNameAttributeValueProcessor processor = new RenameEnumLiteralNameAttributeValueProcessor(
                enumLiteralNameAttributeValue);
        processor.setNewName("bar");
        IProgressMonitor pm = new NullProgressMonitor();

        IpsRefactoringModificationSet probeRun = processor.refactorIpsModel(pm);
        probeRun.undo();

        processor.refactorIpsModel(pm);

        IEnumAttributeValue currentLiteralNameAttributeValue = enumLiteralNameAttributeValue.getEnumValue()
                .getEnumLiteralNameAttributeValue();
        IIpsSrcFile ipsSrcFile = currentLiteralNameAttributeValue.getIpsSrcFile();
        ipsSrcFile.save(pm);

        assertThat(currentLiteralNameAttributeValue.getStringValue(), is("bar"));
        String xml = StringUtil.readFromInputStream(ipsSrcFile.getContentFromEnclosingResource(),
                ipsProject.getXmlFileCharset());
        assertThat(xml, containsString("bar"));
    }

}
