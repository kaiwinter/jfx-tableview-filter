package com.github.kaiwinter.jfx.tablecolumn.filter.predicate;

import java.util.function.Predicate;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;

/**
 * {@link Predicate} implementation to make a substring match of a <code>filterText</code> against a {@link String} value. In addition the
 * operators '>' and '<' can be used to filter for open ranges.
 */
public final class InStringExpressionPredicate implements Predicate<String> {

	private final String filterText;

	public InStringExpressionPredicate(String filterText) {
		this.filterText = filterText.trim();
	}

	@Override
	public boolean test(String value) {
		if (filterText.length() == 0) {
			return true;
		}

		// get operator
		char expression = filterText.charAt(0);

		if ('>' == expression) {
			String filterWithoutExpression = filterText.substring(1).trim();
			return value.toLowerCase().compareTo(filterWithoutExpression.toLowerCase()) > 0;
		} else if ('<' == expression) {
			String filterWithoutExpression = filterText.substring(1).trim();
			return value.toLowerCase().compareTo(filterWithoutExpression.toLowerCase()) < 0;
		}
		return value.toLowerCase().contains(filterText.toLowerCase());
	}

	/**
	 * Uses the {@link InStringExpressionPredicate} to filter a {@link TableColumn} of content type {@link String}.
	 *
	 * @param <S>
	 *            The type of the content in all cells in this TableColumn
	 */
	public static class InStringTableColumnExpressionPredicate<S> implements Predicate<S> {

		private final TableColumn<S, String> tableColumn;
		private final InStringExpressionPredicate expressionPredicate;

		public InStringTableColumnExpressionPredicate(TableColumn<S, String> tableColumn, String filterText) {
			this.tableColumn = tableColumn;
			this.expressionPredicate = new InStringExpressionPredicate(filterText);
		}

		@Override
		public boolean test(S value) {
			CellDataFeatures<S, String> cellData = new CellDataFeatures<>(null, null, value);
			String stringValue = tableColumn.getCellValueFactory().call(cellData).getValue();
			return expressionPredicate.test(stringValue);
		}

	}
}
