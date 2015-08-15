package com.github.kaiwinter.jfx.tablecolumn.filter;

import java.io.IOException;
import java.net.URL;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Handles click events on the filter button of a {@link TableColumn}. On the first click event a filter dialog is instantiated and shown.
 * On following click events the filter dialog gets re-used.
 *
 * @param <S>
 *            the actual type
 */
public final class FilterEventHandler<S> implements EventHandler<ActionEvent> {

	private static final String FILTER_DIALOG_FXML = "filter.fxml";
	private static final String FILTER_DIALOG_CSS = FilterEventHandler.class.getResource("filter-dialog.css").toString();

	private final TableColumn<S, String> tableColumn;
	private final Button filterButton;

	private Stage filterStage;
	private FilterController<S, String> filterController;

	public FilterEventHandler(TableColumn<S, String> tableColumn, Button filterButton) {
		this.tableColumn = tableColumn;
		this.filterButton = filterButton;
	}

	@Override
	public void handle(ActionEvent event) {
		if (filterStage == null) {
			filterStage = createNewDialog();
		}

		Bounds bounds = filterButton.localToScreen(filterButton.getParent().getBoundsInParent());
		filterStage.setX(bounds.getMinX());
		filterStage.setY(bounds.getMaxY());
		filterController.show();
	}

	private Stage createNewDialog() {
		Stage dialog = new Stage(StageStyle.UNDECORATED);
		dialog.initOwner(tableColumn.getTableView().getScene().getWindow());

		try {
			// TODO KW: Do this on initialization time to show a potential missing resource early? This is a performance
			// trade-off. -> javadoc
			FXMLLoader fxmlLoader = new FXMLLoader();
			URL resource = getClass().getResource(FILTER_DIALOG_FXML);
			if (resource == null) {
				throw new RuntimeException("FXML file '" + FILTER_DIALOG_FXML + "' of the filter dialog not found!");
			}
			Parent filterDialogNode = fxmlLoader.load(resource.openStream());
			filterDialogNode.getStylesheets().add(FILTER_DIALOG_CSS);
			filterController = fxmlLoader.getController();
			filterController.setHandler(this);
			filterController.setColumn(tableColumn);
			filterController.setButton(filterButton);

			// Add listener to inform FilterController if Table View data changed
			tableColumn.getTableView().itemsProperty().addListener((observable, oldValue, newValue) -> {
				filterController.tableDataChanged();
			});

			Scene scene = new Scene(filterDialogNode);
			dialog.setScene(scene);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return dialog;
	}

	/**
	 * Sets whether the column is filtered. If a filter is active the CSS class of the filter button is changed to show a different icon.
	 *
	 * @param filterActive
	 *            if <code>true</code> the filter button's CSS class is changed to indicate the active filter, if <code>false</code> the
	 *            changed CSS class is removed
	 */
	public void setFilterActive(boolean filterActive) {
		if (filterActive && !filterButton.getStyleClass().contains("active")) {
			// Be sure to only add this once
			filterButton.getStyleClass().add("active");
		} else if (!filterActive && filterButton.getStyleClass().contains("active")) {
			filterButton.getStyleClass().remove("active");
		}
	}
}