/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.internal.fl.TableAccessFunctionFlFunctionAdapter;
import org.faktorips.devtools.model.internal.tablestructure.TableAccessFunction;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.IIndex;
import org.faktorips.devtools.model.tablestructure.IKeyItem;
import org.faktorips.devtools.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TableAccessFunctionFlFunctionAdapterTest {

    private static final String MY_DATATYPE = "myDatatype";

    private static final String TABLE_CONTENTS_NAME = "TableContentsName";

    private static final String REFERENCED_NAME = "ReferencedName";

    @Mock
    private IIpsProject ipsProject1;

    @Mock
    private IIpsProject ipsProject2;

    @Mock
    private IIndex key;

    @Mock
    private IColumn column;

    @Mock
    private ValueDatatype datatype1;

    @Mock
    private ValueDatatype datatype2;

    private ITableAccessFunction fct;

    private TableAccessFunctionFlFunctionAdapter tableAccessFunctionFlFunctionAdapter;

    @Before
    public void setUp() {
        ITableStructure tableStructure = mock(ITableStructure.class);
        when(tableStructure.getIpsProject()).thenReturn(ipsProject2);
        when(key.getTableStructure()).thenReturn(tableStructure);
        IKeyItem keyItem = mock(IKeyItem.class);
        when(key.getKeyItems()).thenReturn(new IKeyItem[] { keyItem });
        when(keyItem.getDatatype()).thenReturn(MY_DATATYPE);
        when(ipsProject1.findValueDatatype(MY_DATATYPE)).thenReturn(datatype1);
        when(ipsProject2.findValueDatatype(MY_DATATYPE)).thenReturn(datatype2);

        fct = new TableAccessFunction(key, column);
        tableAccessFunctionFlFunctionAdapter = new TableAccessFunctionFlFunctionAdapter(TABLE_CONTENTS_NAME, fct,
                REFERENCED_NAME, ipsProject1);

    }

    @Test
    public void testGetArgTypes_datatypesInDifferentProjects() throws Exception {
        Datatype[] argTypes = tableAccessFunctionFlFunctionAdapter.getArgTypes();

        assertEquals(1, argTypes.length);
        assertEquals(datatype1, argTypes[0]);
    }

}
