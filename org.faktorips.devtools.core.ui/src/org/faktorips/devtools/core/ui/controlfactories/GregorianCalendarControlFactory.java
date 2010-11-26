/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controlfactories;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.DateISOStringFormat;
import org.faktorips.devtools.core.ui.controller.fields.FormattingTextField;
import org.faktorips.devtools.core.ui.controls.DateControl;
import org.faktorips.devtools.core.ui.table.FormattingTextCellEditor;
import org.faktorips.devtools.core.ui.table.GridTableViewerTraversalStrategy;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.TableViewerTraversalStrategy;
import org.faktorips.devtools.core.ui.table.TextCellEditor;

/**
 * A factory for edit fields/controls for the data type Date or gregorian calendar respectively.
 * 
 * @author Stefan Widmaier
 * @since 3.2
 */
public class GregorianCalendarControlFactory extends ValueDatatypeControlFactory {

    public GregorianCalendarControlFactory() {
        super();
    }

    @Override
    public boolean isFactoryFor(ValueDatatype datatype) {
        return Datatype.GREGORIAN_CALENDAR.equals(datatype);
    }

    @Override
    public EditField createEditField(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {

        DateControl dateControl = new DateControl(parent, toolkit);
        FormattingTextField formatField = new FormattingTextField(dateControl.getTextControl(),
                new DateISOStringFormat());
        return formatField;
    }

    @Override
    public Control createControl(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {
        Text text = toolkit.createText(parent, SWT.NONE);
        return text;
    }

    protected Button createButton(UIToolkit toolkit, Composite calendarComposite) {
        GridData buttonGridData = new GridData(SWT.FILL, SWT.FILL, false, false);
        Button button = toolkit.createButton(calendarComposite, ""); //$NON-NLS-1$
        button.setLayoutData(buttonGridData);
        button.setImage(IpsUIPlugin.getImageHandling().getSharedImage("Calendar.png", true)); //$NON-NLS-1$
        return button;
    }

    protected Composite createComposite(Composite parent) {

        // Composite calendarComposite = createComposite(parent);
        // Text text = (Text)createControl(toolkit, calendarComposite, datatype, valueSet,
        // ipsProject);
        // GridData textGridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        // text.setLayoutData(textGridData);
        // Button button = createButton(toolkit, calendarComposite);

        // Composite calendarComposite = toolkit.createGridComposite(parent, 2, false, true);
        Composite calendarComposite = new Composite(parent, SWT.NONE);
        GridData compositeGridData = new GridData(SWT.FILL, SWT.FILL, false, false);
        calendarComposite.setLayoutData(compositeGridData);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginTop = 2;
        gridLayout.marginBottom = 2;
        gridLayout.marginLeft = 1;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        calendarComposite.setLayout(gridLayout);
        return calendarComposite;
    }

    // private void createListeners(final FormattingTextField formatField, final Button button) {
    // button.addSelectionListener(new SelectionListener() {
    // @Override
    // public void widgetSelected(SelectionEvent e) {
    // showClendarShell(formatField, button);
    // }
    //
    // @Override
    // public void widgetDefaultSelected(SelectionEvent e) {
    // // nothing to do
    // }
    // });
    // }

    // Was sagt Jan eigentlich zu einem Button mit Kalenderanzeige?

    // TODO Regexp localabhängig machen, sonst kann man doch wieder ungültige Zeichen eingeben.
    // Wie soll das gehen? -> Sample Value: Format formatieren lassen und String-Chars auf
    // contains() prüfen.
    // -> langsam, doof zu programmieren
    // -> man ist nie sicher alle Sonderzeichen erwischt zu haben. zB E26 bei Double

    // ValidierungsMeldungen haben anderes Format als angezeigt

    /*
     * Diskrepanz zwischen angezeigtem Wert und Modell-wert: Wenn man Tippt, wird nicht immer das
     * Modell geändert. Das liegt daran, dass der Inhalt nicht immer geparst werden kann. Dann wird
     * null ins Modell geschrieben. Falls dort schon Null stand, ändert sich aus Sicht FIPS nichts
     * und somit gibt es auch kein Dirty *.
     * 
     * Unterbinden kann man dies nur auf zwei Wegen:
     * 
     * - man macht ein Control, das immer eine gültige Eingabe erzwingt.
     * 
     * - man lässt alles ins Modell schreiben und validieren.
     * 
     * Ersteres ist schwer zu implementieren, zweiteres benötigt unterschiedliche Methoden für
     * parsable und getValue. :/
     */

    /*
     * GregorianCalendarField:
     * 
     * Wird benötigt um validFrom/to(als GregorianCalendar) per UIController/PropertyMapping direkt
     * in eine ProdCmpt zu schreiben.
     * 
     * Leider gibt es dabei keine Formatunterstützung wie beim FormattingEditField
     * 
     * -> vielleicht sollte man das GregorCalFormat parametrisieren, damit es auch einen
     * GregorianCalendar, statt eines ISO Date Strings zurückgeben kann.
     * 
     * -> Leider würde dann in der alternativen GUI der KalenderButton fehlen...
     * 
     * -> Button+Text auslagern in eigenes Control zB TextButtonControl
     * 
     * Offene Punkte dann:
     * 
     * - wer instanziiert das FormattingEditField?
     * 
     * - Auf welchem Weg soll das Datum ins Modell gelangen (ISO-String bzw. GregCal)? ->
     * Text#setText() mit medium DateFormat funktioniert, ist aber nicht schön. Alternativen?
     * Problem ist natürlich, dass es zwei Formate gibt ...
     */

    /**
     * @deprecated use
     *             {@link #createTableCellEditor(UIToolkit, ValueDatatype, IValueSet, TableViewer, int, IIpsProject)}
     *             instead.
     */
    @Deprecated
    @Override
    public IpsCellEditor createCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {

        return createTableCellEditor(toolkit, dataType, valueSet, tableViewer, columnIndex, ipsProject);
    }

    /**
     * Creates a {@link TextCellEditor} containing a {@link Text} control and configures it with a
     * {@link TableViewerTraversalStrategy}.
     */
    @Override
    public IpsCellEditor createTableCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {

        IpsCellEditor cellEditor = createTextCellEditor(toolkit, dataType, valueSet, tableViewer.getTable(), ipsProject);
        TableViewerTraversalStrategy strat = new TableViewerTraversalStrategy(cellEditor, tableViewer, columnIndex);
        strat.setRowCreating(true);
        cellEditor.setTraversalStrategy(strat);
        return cellEditor;
    }

    /**
     * Creates a {@link TextCellEditor} containing a {@link Text} control and configures it with a
     * {@link GridTableViewerTraversalStrategy}.
     */
    @Override
    public IpsCellEditor createGridTableCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            GridTableViewer gridViewer,
            int columnIndex,
            IIpsProject ipsProject) {

        IpsCellEditor cellEditor = createTextCellEditor(toolkit, dataType, valueSet, gridViewer.getGrid(), ipsProject);
        cellEditor.setTraversalStrategy(new GridTableViewerTraversalStrategy(cellEditor, gridViewer, columnIndex));
        return cellEditor;
    }

    private IpsCellEditor createTextCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            Composite parent,
            IIpsProject ipsProject) {

        Text textControl = (Text)createControl(toolkit, parent, dataType, valueSet, ipsProject);
        DateISOStringFormat format = new DateISOStringFormat();
        IpsCellEditor tableCellEditor = new FormattingTextCellEditor(textControl, format);
        return tableCellEditor;
    }

    @Override
    public IpsCellEditor createGridTreeCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            GridTreeViewer gridViewer,
            int columnIndex,
            IIpsProject ipsProject) {

        IpsCellEditor cellEditor = createTextCellEditor(toolkit, dataType, valueSet, gridViewer.getGrid(), ipsProject);
        return cellEditor;
    }

}
