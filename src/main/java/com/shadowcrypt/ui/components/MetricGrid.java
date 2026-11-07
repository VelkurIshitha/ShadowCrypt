package com.shadowcrypt.ui.components;

import com.shadowcrypt.ui.theme.Theme;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/** Two-column label/value grid for analysis metrics. */
public final class MetricGrid extends GridPane {

    private int row;

    public MetricGrid() {
        setHgap(28);
        setVgap(8);

        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(200);
        ColumnConstraints valueColumn = new ColumnConstraints();
        valueColumn.setHgrow(Priority.ALWAYS);
        getColumnConstraints().addAll(labelColumn, valueColumn);
    }

    public void addMetric(String label, String value) {
        Label name = new Label(label);
        name.getStyleClass().add(Theme.METRIC_LABEL);

        Label text = new Label(value);
        text.getStyleClass().add(Theme.METRIC_VALUE);
        text.setWrapText(true);

        add(name, 0, row);
        add(text, 1, row);
        row++;
    }

    public void clearMetrics() {
        getChildren().clear();
        row = 0;
    }
}
