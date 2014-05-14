/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.tablecontents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SingleTableContentsValidatorTest {

    @Mock
    private IpsProject ipsProject;

    @Mock
    private TableStructure tableStructure;

    @Mock
    private IIpsSrcFile contentsSrcFile1;
    @Mock
    private IIpsSrcFile contentsSrcFile2;

    private SingleTableContentsValidator validator;

    @Before
    public void setup() {
        validator = new SingleTableContentsValidator(tableStructure);
        when(tableStructure.getIpsProject()).thenReturn(ipsProject);
    }

    @Test
    public void testValidateIfPossible() {
        setUpContentSrcFiles(contentsSrcFile1, contentsSrcFile2);
        MessageList messageList = new SingleTableContentsValidator(tableStructure).validateIfPossible();

        assertEquals(1, messageList.size());
    }

    @Test
    public void testValidateIfPossible_TableStructureIsNull() {
        MessageList messageList = new SingleTableContentsValidator(null).validateIfPossible();

        assertNotNull(messageList);
        assertEquals(0, messageList.size());
    }

    @Test
    public void testCanValidate() {
        assertTrue(validator.canValidate());
    }

    @Test
    public void testCanValidate_TableStructureIsNull() {
        assertFalse(new SingleTableContentsValidator(null).canValidate());
    }

    @Test
    public void testValidateAndAppendMessages_SingleContent_ToManyContents() {
        setUpContentSrcFiles(contentsSrcFile1, contentsSrcFile2);

        validateAndAssertMessageCount(1);
    }

    @Test
    public void testValidateAndAppendMessages_SingleContent_NoContents() {
        setUpContentSrcFiles();

        validateAndAssertMessageCount(0);
    }

    @Test
    public void testValidateAndAppendMessages_SingleContent_SingleContents() {
        setUpContentSrcFiles(contentsSrcFile1);

        validateAndAssertMessageCount(0);
    }

    @Test
    public void testValidateAndAppendMessages_MultiContent_TooManyContents() {
        when(tableStructure.isMultipleContentsAllowed()).thenReturn(true);
        setUpContentSrcFiles(contentsSrcFile1, contentsSrcFile2);

        validateAndAssertMessageCount(0);
    }

    @Test
    public void testValidateAndAppendMessages_MultiContent_NoContents() {
        when(tableStructure.isMultipleContentsAllowed()).thenReturn(true);
        setUpContentSrcFiles();

        validateAndAssertMessageCount(0);
    }

    @Test
    public void testValidateAndAppendMessages_MultiContent_SingleContents() {
        when(tableStructure.isMultipleContentsAllowed()).thenReturn(true);
        setUpContentSrcFiles(contentsSrcFile1);

        validateAndAssertMessageCount(0);
    }

    @Test
    public void testValidateAndAppendMessages_AssertCorrectErrorMessage() {
        when(tableStructure.getName()).thenReturn("tableStructure");
        when(contentsSrcFile1.getIpsObjectName()).thenReturn("contentsSrcFile1");
        when(contentsSrcFile2.getIpsObjectName()).thenReturn("contentsSrcFile2");
        setUpContentSrcFiles(contentsSrcFile1, contentsSrcFile2);

        MessageList messageList = new MessageList();
        validator.validateAndAppendMessages(messageList);

        assertEquals(1, messageList.size());
        assertEquals(getExpectedMessage(), messageList.getMessage(0));
    }

    @Test
    public void testForbidsAdditionalContents_multiContentStructure() {
        setUpContentSrcFiles(contentsSrcFile1, contentsSrcFile2);
        when(tableStructure.isMultipleContentsAllowed()).thenReturn(true);

        assertFalse(validator.forbidsAdditionalContents());
    }

    @Test
    public void testForbidsAdditionalContents_singleContentStructure_noContents() {
        setUpContentSrcFiles();

        assertFalse(validator.forbidsAdditionalContents());
    }

    @Test
    public void testForbidsAdditionalContents_singleContentStructure_oneContents() {
        setUpContentSrcFiles(contentsSrcFile1);

        assertTrue(validator.forbidsAdditionalContents());
    }

    @Test
    public void testForbidsAdditionalContents_singleContentStructure_multipleContents() {
        setUpContentSrcFiles(contentsSrcFile1, contentsSrcFile2);

        assertTrue(validator.forbidsAdditionalContents());
    }

    @Test
    public void testForbidsAdditionalContents_nullStructure() {
        validator = new SingleTableContentsValidator(null);

        assertTrue(validator.forbidsAdditionalContents());
    }

    private void setUpContentSrcFiles(IIpsSrcFile... srcFileArray) {
        when(ipsProject.findAllTableContentsSrcFiles(tableStructure)).thenReturn(createList(srcFileArray));
    }

    private Message getExpectedMessage() {
        String text = NLS.bind(Messages.TableContents_msgTooManyContentsForSingleTableStructure,
                tableStructure.getName(), "[" + contentsSrcFile1 + ", " + contentsSrcFile2 + "]");
        Message expectedMessage = new Message(ITableContents.MSGCODE_TOO_MANY_CONTENTS_FOR_SINGLETABLESTRUCTURE, text,
                Message.ERROR, tableStructure.getName(), ITableContents.PROPERTY_TABLESTRUCTURE);
        return expectedMessage;
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
