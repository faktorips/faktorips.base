/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IllegalRepositoryModificationException;
import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.XmlAbstractTestCase;
import org.faktorips.runtime.internal.toc.TableContentTocEntry;
import org.faktorips.values.Decimal;
import org.faktorips.values.DefaultInternationalString;
import org.faktorips.values.InternationalString;
import org.faktorips.values.LocalizedString;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 *
 * @author Peter Erzberger
 */
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class TableTest extends XmlAbstractTestCase {

    private TestTable table;

    @Mock
    private TableContentTocEntry tocEntry;

    @Before
    public void setUp() throws Exception {
        table = new TestTable();
        when(tocEntry.getIpsObjectId()).thenReturn(getClass().getName());
    }

    @Test
    public void testInitFromXmlViaSax() throws Exception {
        String className = getClass().getSimpleName();
        String resourceName = className + ".xml";
        InputStream is = getClass().getResourceAsStream(resourceName);
        if (is == null) {
            throw new RuntimeException("Can't find resource " + resourceName);
        }

        table.initFromXml(is, null, tocEntry.getIpsObjectId());

        TestTableRow row = table.rows.get(0);
        assertNull(row.getCompany());
        assertEquals(Integer.valueOf("0"), row.getGender());
        assertEquals(Decimal.valueOf("0.1"), row.getRate());

        row = table.rows.get(1);
        assertEquals("KQV", row.getCompany());
        assertEquals(Integer.valueOf("1"), row.getGender());
        assertEquals(Decimal.valueOf("0.15"), row.getRate());

        row = table.rows.get(5);
        assertEquals("BBV", row.getCompany());
        assertEquals(Integer.valueOf("1"), row.getGender());
        assertEquals(Decimal.valueOf("0.35"), row.getRate());

        assertEquals("TestBeschreibung", table.getDescription(Locale.GERMAN));
        assertEquals("TestDescription", table.getDescription(Locale.ENGLISH));
        assertEquals(StringUtils.EMPTY, table.getDescription(Locale.FRENCH));

        assertEquals(getClass().getName(), table.getName());
    }

    @Test
    public void testSetDescription_WithLocaleAndString() {
        IRuntimeRepository repo = new InMemoryRuntimeRepository();
        assertThat(table.getDescription(Locale.ENGLISH), is(""));

        table.setDescription(Locale.ENGLISH, "Updated English Description", repo);

        assertThat(table.getDescription(Locale.ENGLISH), is("Updated English Description"));
    }

    @Test
    public void testSetDescription_WithInternationalString() {
        IRuntimeRepository repo = new InMemoryRuntimeRepository();

        InternationalString newDesc = new DefaultInternationalString(
                List.of(new LocalizedString(Locale.ENGLISH, "Desc EN"), new LocalizedString(Locale.GERMAN, "Desc DE")),
                Locale.ENGLISH);

        table.setDescription(newDesc, repo);

        assertThat(table.getDescription(Locale.ENGLISH), is("Desc EN"));
        assertThat(table.getDescription(Locale.GERMAN), is("Desc DE"));
    }

    @Test(expected = IllegalRepositoryModificationException.class)
    public void testSetDescription_ReadOnlyRepositoryThrows() {
        IRuntimeRepository readOnlyRepo = mock(IRuntimeRepository.class);
        when(readOnlyRepo.isModifiable()).thenReturn(false);
        table.setDescription(Locale.ENGLISH, "New Desc", readOnlyRepo);
    }
}
