/*
 * BEGIN FAKTORIPS GENERATOR INFORMATION SECTION
 * 
 * builder set: org.faktorips.devtools.stdbuilder.ipsstdbuilderset, Version: 3.0.0
 * 
 * END FAKTORIPS GENERATOR INFORMATION SECTION
 */
package org.faktorips.runtime.internal;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.model.annotation.IpsTableStructure;
import org.faktorips.runtime.model.table.TableStructureKind;
import org.faktorips.values.Decimal;

/**
 * Diese Klasse implementiert eine Read-Only In-Memory-Tabelle. Auf die Daten der Tabelle kann ueber
 * Finder-Methoden zugegriffen werden.
 * 
 * @generated
 */
@IpsTableStructure(name = "tables.TestTable", type = TableStructureKind.MULTIPLE_CONTENTS, columns = { "company",
        "gender", "rate" })
public class TestTable extends Table<TestTableRow> {

    /**
     * Erzeugt einen leeren Tabelleninhalt.
     * 
     * @generated
     */
    public TestTable() {
        super();
    }

    /**
     * Erzeugt einen neuen Tabelleninhalt mit den uebergebenen Zeilen. Die Liste mit den Zeilen wird
     * kopiert. Spaetere Aenderungen an dem Inhalt der Liste, aendern also nicht die erzeugte
     * Tabelle. Dieser Konstruktor ist vor allem fuer die Verwendung in JUnit Tests vorgesehen, um
     * beliebige Tabelleninhalte erzeugen zu koennen.
     * 
     * @generated
     */
    public TestTable(List<TestTableRow> content) {
        super();
        rows = new ArrayList<>(content);
        init();
    }

    /**
     * Diese Methode wird waehrend der Initialisierung verwendet. Sie fuegt eine neue Tabellenzeile
     * hinzu.
     * 
     * @generated
     */
    @Override
    protected void addRow(List<String> values, IRuntimeRepository productRepository) {
        String columnValue = values.get(0);
        String company = columnValue == null ? null : columnValue;
        columnValue = values.get(1);
        Integer gender = columnValue == null ? null
                : IpsStringUtils.isEmpty(columnValue) ? null : Integer.valueOf(columnValue);
        columnValue = values.get(2);
        Decimal rate = columnValue == null ? Decimal.NULL : Decimal.valueOf(columnValue);
        rows.add(new TestTableRow(company, gender, rate));
    }

    /**
     * Initialisiert die Maps dieser Tabelle. Diese werden von den Finder-Methoden dieser Klasse
     * verwendet.
     * 
     * @generated
     */
    @Override
    protected final void initKeyMaps() {
    }

    /**
     * Gibt die Instanz dieser Tabellenklasse zurueck.
     * 
     * @generated
     */
    public static final TestTable getInstance(IRuntimeRepository repository, String qualifiedTableName) {
        return (TestTable)repository.getTable(qualifiedTableName);
    }

}