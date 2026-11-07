package com.shadowcrypt.ui.components;

import com.shadowcrypt.ui.theme.Theme;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.awt.image.BufferedImage;

/** Image thumbnail with a caption, used by encryption, decryption, and analysis. */
public final class ImagePreviewPane extends VBox {

    private static final double PREVIEW_WIDTH = 280;
    private static final double PREVIEW_HEIGHT = 190;

    private final ImageView imageView = new ImageView();
    private final Label captionLabel = new Label("No image selected");
    private final Label placeholder = new Label("—");

    public ImagePreviewPane(String title) {
        setSpacing(6);
        setAlignment(Pos.TOP_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add(Theme.MUTED);

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setFitWidth(PREVIEW_WIDTH);
        imageView.setFitHeight(PREVIEW_HEIGHT);

        placeholder.getStyleClass().add(Theme.MUTED);

        StackPane frame = new StackPane(placeholder, imageView);
        frame.getStyleClass().add(Theme.PREVIEW);
        frame.setMinSize(PREVIEW_WIDTH + 12, PREVIEW_HEIGHT + 12);
        frame.setPrefSize(PREVIEW_WIDTH + 12, PREVIEW_HEIGHT + 12);

        captionLabel.getStyleClass().add(Theme.SUBTITLE);
        captionLabel.setWrapText(true);

        getChildren().addAll(titleLabel, frame, captionLabel);
    }

    public void setImage(BufferedImage image, String caption) {
        imageView.setImage(ImageConverter.toFxImage(image));
        placeholder.setVisible(false);
        captionLabel.setText(caption);
    }

    public void clear(String caption) {
        imageView.setImage(null);
        placeholder.setVisible(true);
        captionLabel.setText(caption);
    }
}
