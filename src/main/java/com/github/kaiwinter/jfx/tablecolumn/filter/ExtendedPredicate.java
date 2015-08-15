package com.github.kaiwinter.jfx.tablecolumn.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * An instance of this class is attached to a {@link FilteredList} to filter the whole data of a {@link TableView} if one or more columns
 * are restricted by a filter.
 *
 * The {@link Predicate}s are stored per {@link TableColumn} in this class. This is necessary because column filters have to be identifiable
 * in order to modify them afterwards. This is also why the {@link Predicate}s can't just be accumulated by it's <code>and</code> method.
 *
 * @param <S>
 *            the type
 */
public final class ExtendedPredicate<S> implements Predicate<S> {

	private Map<TableColumn<S, String>, Predicate<S>> tableColumn2Predicate = new HashMap<>();

	public ExtendedPredicate() {
	}

	/**
	 * Copies a {@link ExtendedPredicate}. It is necessary to place a new instance of this class in the {@link FilteredList} after an
	 * {@link Predicate} was added otherwise the {@link FilteredList} won't update.
	 * 
	 * @param extendedPredicate
	 */
	public ExtendedPredicate(ExtendedPredicate<S> extendedPredicate) {
		this.tableColumn2Predicate = new HashMap<>(extendedPredicate.tableColumn2Predicate);
	}

	@Override
	public boolean test(S t) {
		for (Predicate<S> predicate : tableColumn2Predicate.values()) {
			if (predicate == null) {
				continue;
			}
			if (!predicate.test(t)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param tableColumn
	 *            the {@link TableColumn} of the {@link Predicate}. Needed to identify an existing {@link Predicate} to allow later
	 *            modifications
	 * @param predicate
	 *            the {@link Predicate} to add to the filter
	 */
	public void addPredicate(TableColumn<S, String> tableColumn, Predicate<S> predicate) {
		tableColumn2Predicate.put(tableColumn, predicate);
	}
}
