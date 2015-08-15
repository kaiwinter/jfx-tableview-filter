package com.github.kaiwinter.jfx.tablecolumn.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Adds a filter button to the header of a {@link TableColumn} and provides helper methods.
 */
public final class FilterSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(FilterSupport.class.getSimpleName());

	private static final String FILTER_BUTTON_CSS = FilterEventHandler.class.getResource("style.css").toString();

	/**
	 * Adds the automatic column filter to the given {@link TableColumn}.
	 *
	 * @param column
	 *            the {@link TableColumn} which will receive the filter
	 */
	public static <S> void addFilter(TableColumn<S, String> column) {
		Button button = new Button();
		button.setFocusTraversable(false);
		button.getStylesheets().add(FILTER_BUTTON_CSS);
		button.setOnAction(new FilterEventHandler<S>(column, button));
		column.setGraphic(button);
	}

	/**
	 * Clear the items of the passed {@link TableView}. If a filter was added to a {@link TableColumn} by {@link #addFilter(TableColumn)}
	 * the item list gets wrapped in a {@link FilteredList} and a {@link SortedList}. This makes direct calls of clear() on the table's
	 * items fail. This method un-wraps the list and calls clear on the internal list. If the {@link TableView} doesn't contains a wrapped
	 * list clear() is called on the outer list.
	 *
	 * @param tableView
	 *            the {@link TableView} to clear
	 */
	public static void clearItems(TableView<?> tableView) {
		ObservableList<?> items = tableView.getItems();
		if (items instanceof SortedList) {
			if (((SortedList<?>) items).getSource() instanceof FilteredList) {
				FilteredList<?> filteredList = (FilteredList<?>) ((SortedList<?>) items).getSource();
				ObservableList<?> unwrappedList = filteredList.getSource();
				unwrappedList.clear();
			} else {
				LOGGER.warn("Tried to clear items of TableView which seems not to be managed by FilterSupport.");
			}
		} else {
			items.clear();
		}
	}

}
