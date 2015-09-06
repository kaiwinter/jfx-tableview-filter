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
	 *            the {@link TableColumn} which will receive the filter, not <code>null</code>
	 */
	public static <S> void addFilter(TableColumn<S, String> column) {
		if (column == null) {
			throw new IllegalArgumentException("TableColumn must not be null");
		}

		Button button = new Button();
		button.setFocusTraversable(false);
		button.getStylesheets().add(FILTER_BUTTON_CSS);
		button.setOnAction(new FilterEventHandler<S>(column, button));
		column.setGraphic(button);
	}

	/**
	 * Returns the items of the passed {@link TableView}. If a filter was added to a {@link TableColumn} by {@link #addFilter(TableColumn)}
	 * the item list is wrapped in a {@link FilteredList} and a {@link SortedList}. This makes direct calls on the list which is returned by
	 * <code>getItems()</code> on the {@link TableView} fail. This method un-wraps the list and returns the underlying
	 * {@link ObservableList}.
	 *
	 * @param tableView
	 *            the {@link TableView} to clear, not <code>null</code>
	 * @return the underlying {@link ObservableList} of the given <code>tableView</code>
	 */
	public static <S> ObservableList<? extends S> getItems(TableView<S> tableView) {
		if (tableView == null) {
			throw new IllegalArgumentException("TableView must not be null");
		}

		ObservableList<S> items = tableView.getItems();
		return getUnwrappedList(items);
	}

	/**
	 * Returns the items of the passed {@link TableView}. If a filter was added to a {@link TableColumn} by {@link #addFilter(TableColumn)}
	 * the item list is wrapped in a {@link FilteredList} and a {@link SortedList}. This makes direct calls on the list which is returned by
	 * <code>getItems()</code> on the {@link TableView} fail. This method un-wraps the list and returns the underlying
	 * {@link ObservableList}.
	 *
	 * @param wrappedList
	 *            the item list of a {@link TableView}
	 * @return the underlying {@link ObservableList} of the given wrapped list or the passed list if it is not wrapped
	 */
	public static <S> ObservableList<? extends S> getUnwrappedList(ObservableList<S> wrappedList) {
		if (wrappedList instanceof SortedList) {
			if (((SortedList<S>) wrappedList).getSource() instanceof FilteredList) {
				FilteredList<? extends S> filteredList = (FilteredList<? extends S>) ((SortedList<? extends S>) wrappedList).getSource();
				ObservableList<? extends S> unwrappedList = filteredList.getSource();
				return unwrappedList;
			} else {
				LOGGER.warn("Cannot unwrap List: outer list is of type {} instead of FilteredList", wrappedList.getClass().getSimpleName());
			}
		} else {
			LOGGER.warn("Cannot unwrap List: outer list is of type {} instead of SortedList", wrappedList.getClass().getSimpleName());
		}

		return wrappedList;
	}

}
