package com.github.kaiwinter.jfx.tablecolumn.filter;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.util.Callback;

/**
 * The controller of the filter dialog. It accesses the {@link TableView} via the {@link TableColumn} of this filter to get and modify the
 * underlying items.
 *
 * @param <S>
 * @param <T>
 */
public final class FilterController<S, T> {

	@FXML
	private TextField filterTf;

	@FXML
	private ListView<String> filterLv;

	private TableColumn<S, String> tableColumn;

	private FilterEventHandler<S> eventHandler;

	private ObservableList<S> items;
	private FilteredList<S> filteredList;
	private SortedList<S> sortedList;

	@FXML
	private void initialize() {
		// On double click, select clicked item
		filterLv.setOnMouseClicked(mouseEvent -> {
			if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
				String selectedItem = filterLv.getSelectionModel().getSelectedItem();
				filterTf.setText(selectedItem);
				filter();
			}
		});
	}

	/**
	 * Initializes the {@link FilterController} with the column.
	 *
	 * @param tableColumn
	 *            the {@link TableColumn}
	 */
	public void setColumn(TableColumn<S, String> tableColumn) {
		this.tableColumn = tableColumn;
		initializeLists();
	}

	/**
	 * Initialize List, FilteredList and SortedList
	 */
	private void initializeLists() {
		items = tableColumn.getTableView().getItems();
		wrapObservableList();

		if (!(items instanceof SortedList)) {
			throw new IllegalArgumentException("Something went wrong, unexpected List implementation in TableView");
		}

		sortedList = (SortedList<S>) items;
		filteredList = (FilteredList<S>) sortedList.getSource();
		items = (ObservableList<S>) filteredList.getSource();

		sortedList.comparatorProperty().bind(tableColumn.getTableView().comparatorProperty());
		tableColumn.getTableView().setItems(sortedList);
	}

	/**
	 * Replaces the {@link ObservableList} in the {@link TableColumn} by a {@link FilteredList} wrapped in a {@link SortedList} which is
	 * necessary for sorting and filtering. It is only done once for a {@link TableView}.
	 */
	private void wrapObservableList() {
		items = tableColumn.getTableView().getItems();
		if (!(items instanceof SortedList)) {
			FilteredList<S> filteredListInt = items.filtered(null);
			SortedList<S> sortedInt = filteredListInt.sorted();
			tableColumn.getTableView().setItems(sortedInt);

			items = sortedInt;
		}
	}

	@FXML
	public void filter() {
		String filterText = filterTf.getText();
		Predicate<S> predicate;
		boolean filterActive;
		if (filterText.isEmpty()) {
			filterActive = false;
			predicate = null;
		} else {
			filterActive = true;
			predicate = value -> {
				CellDataFeatures<S, String> cellData = new CellDataFeatures<>(null, null, value);

				String stringValue = tableColumn.getCellValueFactory().call(cellData).getValue();
				return stringValue.toLowerCase().contains(filterText.toLowerCase());
			};
		}
		eventHandler.setFilterActive(filterActive);

		ExtendedPredicate<S> predicateExt = (ExtendedPredicate<S>) filteredList.getPredicate();
		if (predicateExt == null) {
			predicateExt = new ExtendedPredicate<>();
		}
		predicateExt.addPredicate(tableColumn, predicate);

		// Have to be copied for the framework to see a change
		predicateExt = new ExtendedPredicate<S>(predicateExt);
		filteredList.setPredicate(predicateExt);

		hide();
	}

	@FXML
	public void clear() {
		filterTf.clear();
		filter();
		eventHandler.setFilterActive(false);
	}

	/**
	 * Shows the filter dialog.
	 */
	public void show() {
		setFilteredItemsInFilterListView();
		filterTf.requestFocus();
		filterLv.getSelectionModel().clearSelection();
	}

	/**
	 * Hides the filter dialog.
	 */
	@FXML
	public void hide() {
		eventHandler.hidePopup();
	}

	/**
	 * Initializes the {@link ListView} of the filter dialog with the unique values of the {@link TableColumn}. The {@link ListView} is
	 * sorted by {@link String#CASE_INSENSITIVE_ORDER} order. The {@link ListView} itself gets filtered if the user enters text in the
	 * {@link TextField}.
	 */
	private void setFilteredItemsInFilterListView() {
		// Initialize list view with unique values of this column
		Set<String> uniqueValues = getUniqueValuesFromColumn(filteredList);

		FilteredList<String> filteredUniqueValues = new FilteredList<>(FXCollections.observableArrayList(uniqueValues));
		SortedList<String> sortedUniqueValues = filteredUniqueValues.sorted(String.CASE_INSENSITIVE_ORDER);
		filterLv.setItems(sortedUniqueValues);

		// Filter list view if text is entered
		ChangeListener<String> listener = (ChangeListener<String>) (observable, oldValue, newValue) -> {
			filteredUniqueValues.setPredicate(t -> {
				if (newValue == null || newValue.isEmpty()) {
					return true;
				}
				return t.toLowerCase().contains(newValue.toLowerCase());
			});
		};
		filterTf.textProperty().addListener(listener);

		// fire event to filter the list
		listener.changed(filterTf.textProperty(), null, filterTf.getText());
	}

	/**
	 * Returns all unique values of the column. A second {@link FilteredList} on the underlying items is used. The filter {@link Predicate}
	 * for this {@link TableColumn} of the second {@link FilteredList} is removed to get not only the unique values from the already
	 * filtered column.
	 *
	 * @return unique values of the column
	 */
	private Set<String> getUniqueValuesFromColumn(FilteredList<S> filteredList) {
		ExtendedPredicate<S> predicate = (ExtendedPredicate<S>) filteredList.getPredicate();

		FilteredList<S> filteredListWithoutFilterOfThisColumn;
		if (predicate == null) {
			filteredListWithoutFilterOfThisColumn = filteredList;
		} else {
			// Remove Predicate of this column
			ExtendedPredicate<S> predicateCopy = new ExtendedPredicate<>(predicate);
			predicateCopy.addPredicate(tableColumn, null);
			filteredListWithoutFilterOfThisColumn = items.filtered(predicateCopy);
		}

		Set<String> uniqueItems = filteredListWithoutFilterOfThisColumn //
				.stream() //
				.map(p -> getStringValueOfColumn(p)) //
				.collect(Collectors.toSet());
		return uniqueItems;
	}

	private String getStringValueOfColumn(S item) {
		Callback<CellDataFeatures<S, String>, ObservableValue<String>> cellValueFactory = tableColumn.getCellValueFactory();
		CellDataFeatures<S, String> param = new CellDataFeatures<>(null, tableColumn, item);
		String value = cellValueFactory.call(param).getValue();

		return value;
	}

	/**
	 * Refreshes the {@link FilterController}'s internal data from the underlying {@link TableColumn}.
	 */
	public void tableDataChanged() {
		Predicate<? super S> predicate = filteredList.getPredicate();
		initializeLists();
		filteredList.setPredicate(predicate);
	}

	public void setHandler(FilterEventHandler<S> eventHandler) {
		this.eventHandler = eventHandler;
	}

}
