/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.tablecontents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.internal.tablestructure.TableStructure;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SingleTableContentsValidatorTest {

    @Mock
    private IpsProject ipsProjectBase;
    @Mock
    private IpsProject ipsProjectA;

    @Mock
    private TableStructure tableStructure;

    @Mock
    private IIpsSrcFile contentsSrcFile1;
    @Mock
    private IIpsSrcFile contentsSrcFile2;

    private SingleTableContentsValidator validator;

    @Before
    public void setup() {
        validator = new SingleTableContentsValidator(ipsProjectBase, tableStructure);
        when(tableStructure.getName()).thenReturn("tableStructure");
    }

    @Test
    public void testValidateIfPossible() {
        setUpContentSrcFiles(ipsProjectBase, contentsSrcFile1, contentsSrcFile2);
        MessageList messageList = new SingleTableContentsValidator(ipsProjectBase, tableStructure).validateIfPossible();

        assertEquals(1, messageList.size());
    }

    @Test
    public void testValidateIfPossible_TableStructureIsNull() {
        MessageList messageList = new SingleTableContentsValidator(ipsProjectBase, null).validateIfPossible();

        assertNotNull(messageList);
        assertEquals(0, messageList.size());
    }

    @Test
    public void testCanValidate() {
        assertTrue(validator.canValidate());
    }

    @Test
    public void testCanValidate_TableStructureIsNull() {
        assertFalse(new SingleTableContentsValidator(ipsProjectBase, null).canValidate());
    }

    @Test
    public void testValidateAndAppendMessages_SingleContent_TooManyContents() {
        setUpContentSrcFiles(ipsProjectBase, contentsSrcFile1, contentsSrcFile2);

        validateAndAssertMessageCount(1);
    }

    @Test
    public void testValidateAndAppendMessages_SingleContent_NoContents() {
        setUpContentSrcFiles(ipsProjectBase);

        validateAndAssertMessageCount(0);
    }

    @Test
    public void testValidateAndAppendMessages_SingleContent_SingleContents() {
        setUpContentSrcFiles(ipsProjectBase, contentsSrcFile1);

        validateAndAssertMessageCount(0);
    }

    @Test
    public void testValidateAndAppendMessages_MultiContent_TooManyContents() {
        when(tableStructure.isMultipleContentsAllowed()).thenReturn(true);
        setUpContentSrcFiles(ipsProjectBase, contentsSrcFile1, contentsSrcFile2);

        validateAndAssertMessageCount(0);
    }

    @Test
    public void testValidateAndAppendMessages_MultiContent_NoContents() {
        when(tableStructure.isMultipleContentsAllowed()).thenReturn(true);
        setUpContentSrcFiles(ipsProjectBase);

        validateAndAssertMessageCount(0);
    }

    @Test
    public void testValidateAndAppendMessages_MultiContent_SingleContents() {
        when(tableStructure.isMultipleContentsAllowed()).thenReturn(true);
        setUpContentSrcFiles(ipsProjectBase, contentsSrcFile1);

        validateAndAssertMessageCount(0);
    }

    @Test
    public void testValidateAndAppendMessages_AssertCorrectErrorMessage() {
        when(tableStructure.getName()).thenReturn("tableStructure");
        setUpContentSrcFiles(ipsProjectBase, contentsSrcFile1, contentsSrcFile2);

        MessageList messageList = new MessageList();
        validator.validateAndAppendMessages(messageList);

        assertEquals(1, messageList.size());
        assertEquals(getExpectedMessage(), messageList.getMessage(0));
    }

    @Test
    public void testForbidsAdditionalContents_multiContentStructure() {
        setUpContentSrcFiles(ipsProjectBase, contentsSrcFile1, contentsSrcFile2);
        when(tableStructure.isMultipleContentsAllowed()).thenReturn(true);

        assertFalse(validator.forbidsAdditionalContents());
    }

    @Test
    public void testForbidsAdditionalContents_singleContentStructure_noContents() {
        setUpContentSrcFiles(ipsProjectBase);

        assertFalse(validator.forbidsAdditionalContents());
    }

    @Test
    public void testForbidsAdditionalContents_singleContentStructure_oneContents() {
        setUpContentSrcFiles(ipsProjectBase, contentsSrcFile1);

        assertTrue(validator.forbidsAdditionalContents());
    }

    @Test
    public void testForbidsAdditionalContents_singleContentStructure_multipleContents() {
        setUpContentSrcFiles(ipsProjectBase, contentsSrcFile1, contentsSrcFile2);

        assertTrue(validator.forbidsAdditionalContents());
    }

    @Test
    public void testForbidsAdditionalContents_nullStructure() {
        validator = new SingleTableContentsValidator(ipsProjectBase, null);

        assertTrue(validator.forbidsAdditionalContents());
    }

    @Test
    public void testValidateIfPossible_contentInDifferentProject() {
        setUpContentSrcFiles(ipsProjectA, contentsSrcFile1);
        validator = new SingleTableContentsValidator(ipsProjectA, tableStructure);

        assertTrue(validator.forbidsAdditionalContents());
    }

    @Test
    public void testValidateIfPossible_ignoresContentInDifferentProject() {
        setUpContentSrcFiles(ipsProjectA, contentsSrcFile1);
        validator = new SingleTableContentsValidator(ipsProjectBase, tableStructure);

        assertFalse(validator.forbidsAdditionalContents());
    }

    @Test(expected = AssertionFailedException.class)
    public void testConstructor_projectNull() {
        validator = new SingleTableContentsValidator(null, tableStructure);
    }

    private void setUpContentSrcFiles(IIpsProject ipsProject, IIpsSrcFile... srcFileArray) {
        when(ipsProject.findAllTableContentsSrcFiles(tableStructure)).thenReturn(createList(srcFileArray));
    }

    private Message getExpectedMessage() {
        String text = NLS.bind(Messages.TableContents_msgTooManyContentsForSingleTableStructure,
                tableStructure.getName());
        return new Message(ITableContents.MSGCODE_TOO_MANY_CONTENTS_FOR_SINGLETABLESTRUCTURE, text,
                Message.ERROR, tableStructure.getName(), ITableContents.PROPERTY_TABLESTRUCTURE);
    }

    private void validateAndAssertMessageCount(int messageCount) {
        MessageList messageList = new MessageList();
        validator.validateAndAppendMessages(messageList);
        assertEquals(messageCount, messageList.size());
    }

    private List<IIpsSrcFile> createList(IIpsSrcFile... srcFileArray) {
        return Arrays.asList(srcFileArray);
    }

}
