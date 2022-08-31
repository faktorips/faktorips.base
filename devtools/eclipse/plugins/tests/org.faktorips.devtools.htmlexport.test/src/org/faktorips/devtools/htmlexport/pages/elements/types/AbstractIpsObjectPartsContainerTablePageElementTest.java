/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;
import org.faktorips.devtools.model.internal.DefaultVersion;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IVersionControlledElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class AbstractIpsObjectPartsContainerTablePageElementTest {

    private static final String SINCE_VERSION = "Since Version";

    @Mock
    private DocumentationContext context;

    @Mock
    private IIpsObjectPartContainer part;

    @Mock
    private IVersionControlledElement versionControlledElement;

    @Captor
    private ArgumentCaptor<TableRowPageElement> tableRowCaptor;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractIpsObjectPartsContainerTablePageElement<IIpsObjectPartContainer> pageElement;

    private List<IIpsObjectPartContainer> objectParts;

    @Before
    public void setUp() {
        objectParts = new ArrayList<>();
        doReturn(objectParts).when(pageElement).getObjectParts();
        doReturn(context).when(pageElement).getContext();
        when(context.getMessage(HtmlExportMessages.TablePageElement_headlineSince)).thenReturn(SINCE_VERSION);
    }

    @Test
    public void testGetHeadline_noSinceVersion() throws Exception {
        objectParts.add(part);
        doReturn(new ArrayList<String>()).when(pageElement).getHeadlineWithIpsObjectPart();

        List<String> headline = pageElement.getHeadline();

        assertThat(headline, not(hasItem(SINCE_VERSION)));
    }

    @Test
    public void testGetHeadline_sinceVersion() throws Exception {
        objectParts.add(versionControlledElement);
        doReturn(new ArrayList<String>()).when(pageElement).getHeadlineWithIpsObjectPart();

        List<String> headline = pageElement.getHeadline();

        assertThat(headline, hasItem(SINCE_VERSION));
    }

    @Test
    public void testGetRow() throws Exception {
        objectParts.add(versionControlledElement);
        doReturn(new ArrayList<IPageElement>()).when(pageElement).createRowWithIpsObjectPart(versionControlledElement);
        doReturn(new DefaultVersion("1.2.3")).when(versionControlledElement).getSinceVersion();

        List<IPageElement> result = pageElement.getRow(versionControlledElement);

        assertEquals(1, result.size());
        IPageElement textElement = new TextPageElement("1.2.3", context);
        assertThat(result, hasItem(textElement));
    }

    @Test
    public void testGetRow_noVersion() throws Exception {
        objectParts.add(part);
        doReturn(new ArrayList<IPageElement>()).when(pageElement).createRowWithIpsObjectPart(part);

        List<IPageElement> result = pageElement.getRow(part);

        assertEquals(0, result.size());
    }

}
