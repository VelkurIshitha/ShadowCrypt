package com.shadowcrypt.app;

import com.shadowcrypt.config.AppConstants;
import com.shadowcrypt.service.AnalysisService;
import com.shadowcrypt.service.DecryptionService;
import com.shadowcrypt.service.EncryptionService;
import com.shadowcrypt.service.KeyService;
import com.shadowcrypt.ui.main.AppState;
import com.shadowcrypt.ui.main.MainWindow;
import com.shadowcrypt.ui.theme.Theme;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** JavaFX application entry point. */
public class ShadowCryptApplication extends Application {

    @Override
    public void start(Stage stage) {
        AppState state = new AppState();
        KeyService keyService = new KeyService();
        EncryptionService encryptionService = new EncryptionService();
        DecryptionService decryptionService = new DecryptionService();
        AnalysisService analysisService = new AnalysisService();

        MainWindow mainWindow = new MainWindow(
                state, keyService, encryptionService, decryptionService, analysisService);

        Scene scene = new Scene(mainWindow.root(), AppConstants.WINDOW_WIDTH, AppConstants.WINDOW_HEIGHT);
        Theme.apply(scene);

        stage.setTitle(AppConstants.APP_NAME);
        stage.setMinWidth(AppConstants.MIN_WINDOW_WIDTH);
        stage.setMinHeight(AppConstants.MIN_WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.show();
    }
}
