package com.shadowcrypt.ui.theme;

import javafx.scene.Scene;
import javafx.scene.control.DialogPane;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Dark neutral theme. The stylesheet is written in Java and applied as a data URI,
 * so the project stays Java-only (no .css resource files).
 */
public final class Theme {

    public static final String ROOT = "sc-root";
    public static final String TITLE = "sc-title";
    public static final String SUBTITLE = "sc-subtitle";
    public static final String CARD = "sc-card";
    public static final String CARD_TITLE = "sc-card-title";
    public static final String MUTED = "sc-muted";
    public static final String PRIMARY_BUTTON = "sc-primary-button";
    public static final String SECONDARY_BUTTON = "sc-secondary-button";
    public static final String NAV_BUTTON = "sc-nav-button";
    public static final String NAV_BUTTON_ACTIVE = "sc-nav-button-active";
    public static final String BANNER = "sc-banner";
    public static final String BANNER_INFO = "sc-banner-info";
    public static final String BANNER_SUCCESS = "sc-banner-success";
    public static final String BANNER_WARNING = "sc-banner-warning";
    public static final String BANNER_ERROR = "sc-banner-error";
    public static final String WARNING_TEXT = "sc-warning-text";
    public static final String ERROR_TEXT = "sc-error-text";
    public static final String SUCCESS_TEXT = "sc-success-text";
    public static final String METRIC_LABEL = "sc-metric-label";
    public static final String METRIC_VALUE = "sc-metric-value";
    public static final String PREVIEW = "sc-preview";
    public static final String SIDEBAR = "sc-sidebar";
    public static final String CODE = "sc-code";

    private static final String CSS = """
            .root {
                -fx-font-family: "Segoe UI", "Inter", sans-serif;
                -fx-font-size: 13px;
                -fx-background-color: #0f141a;
            }
            .label { -fx-text-fill: #e6edf3; }
            .sc-title { -fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f0f6fc; }
            .sc-subtitle { -fx-font-size: 12px; -fx-text-fill: #8b98a5; }
            .sc-muted { -fx-text-fill: #8b98a5; }
            .sc-card {
                -fx-background-color: #161d26;
                -fx-background-radius: 10;
                -fx-border-radius: 10;
                -fx-border-color: #243040;
                -fx-border-width: 1;
                -fx-padding: 16;
            }
            .sc-card-title { -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f0f6fc; }
            .sc-sidebar {
                -fx-background-color: #0b1015;
                -fx-border-color: transparent #1c2530 transparent transparent;
                -fx-border-width: 1;
            }
            .sc-nav-button {
                -fx-background-color: transparent;
                -fx-border-color: transparent;
                -fx-text-fill: #8b98a5;
                -fx-alignment: center-left;
                -fx-padding: 10 14;
                -fx-background-radius: 8;
                -fx-border-radius: 8;
            }
            .sc-nav-button:hover { -fx-background-color: #141c25; -fx-text-fill: #e6edf3; }
            .sc-nav-button-active {
                -fx-background-color: #17212c;
                -fx-text-fill: #f0f6fc;
                -fx-font-weight: bold;
                -fx-border-color: #2f81f7;
                -fx-border-width: 0 0 0 3;
            }
            .text-field, .password-field, .text-area {
                -fx-background-color: #0d1218;
                -fx-text-fill: #e6edf3;
                -fx-prompt-text-fill: #5f6f80;
                -fx-highlight-fill: #2f81f7;
                -fx-background-radius: 6;
                -fx-border-radius: 6;
                -fx-border-color: #243040;
                -fx-border-width: 1;
            }
            .text-field:focused, .password-field:focused, .text-area:focused {
                -fx-border-color: #2f81f7;
            }
            .text-area .content { -fx-background-color: #0d1218; }
            .button {
                -fx-background-color: #212b36;
                -fx-text-fill: #e6edf3;
                -fx-background-radius: 6;
                -fx-border-radius: 6;
                -fx-border-color: #2f3b4b;
                -fx-border-width: 1;
                -fx-padding: 8 14;
                -fx-cursor: hand;
            }
            .button:hover { -fx-background-color: #2a3644; }
            .button:disabled { -fx-opacity: 0.55; }
            .sc-primary-button {
                -fx-background-color: #2f81f7;
                -fx-border-color: #2f81f7;
                -fx-text-fill: white;
                -fx-font-weight: bold;
            }
            .sc-primary-button:hover { -fx-background-color: #4f9cf9; }
            .sc-secondary-button {
                -fx-background-color: #1b2530;
                -fx-border-color: #2f3b4b;
                -fx-text-fill: #e6edf3;
            }
            .toggle-button {
                -fx-background-color: #0d1218;
                -fx-text-fill: #8b98a5;
                -fx-background-radius: 6;
                -fx-border-radius: 6;
                -fx-border-color: #243040;
                -fx-padding: 7 16;
                -fx-cursor: hand;
            }
            .toggle-button:selected {
                -fx-background-color: #17212c;
                -fx-text-fill: #f0f6fc;
                -fx-border-color: #2f81f7;
                -fx-font-weight: bold;
            }
            .sc-banner {
                -fx-padding: 10 12;
                -fx-background-radius: 8;
                -fx-border-radius: 8;
                -fx-border-width: 1;
            }
            .sc-banner-info { -fx-background-color: #101f2d; -fx-border-color: #1f4f7a; }
            .sc-banner-success { -fx-background-color: #0f2117; -fx-border-color: #2ea043; }
            .sc-banner-warning { -fx-background-color: #241d0e; -fx-border-color: #d29922; }
            .sc-banner-error { -fx-background-color: #2a1215; -fx-border-color: #f85149; }
            .sc-banner-success .label { -fx-text-fill: #7ee787; }
            .sc-banner-warning .label { -fx-text-fill: #e3b341; }
            .sc-banner-error .label { -fx-text-fill: #ff7b72; }
            .sc-banner-info .label { -fx-text-fill: #79c0ff; }
            .sc-warning-text { -fx-text-fill: #e3b341; }
            .sc-error-text { -fx-text-fill: #ff7b72; }
            .sc-success-text { -fx-text-fill: #7ee787; }
            .sc-metric-label { -fx-text-fill: #8b98a5; }
            .sc-metric-value { -fx-text-fill: #f0f6fc; -fx-font-weight: bold; }
            .sc-preview {
                -fx-background-color: #0d1218;
                -fx-background-radius: 8;
                -fx-border-radius: 8;
                -fx-border-color: #243040;
                -fx-border-width: 1;
                -fx-padding: 6;
            }
            .sc-busy-overlay { -fx-background-color: rgba(15, 20, 26, 0.75); }
            .sc-busy-box {
                -fx-background-color: #161d26;
                -fx-background-radius: 10;
                -fx-border-radius: 10;
                -fx-border-color: #243040;
                -fx-border-width: 1;
                -fx-padding: 20;
            }
            .sc-code {
                -fx-font-family: "Consolas", "Courier New", monospace;
                -fx-font-size: 11px;
                -fx-text-fill: #a5d6ff;
            }
            .scroll-pane { -fx-background-color: transparent; }
            .scroll-pane > .viewport { -fx-background-color: transparent; }
            .progress-bar > .bar { -fx-background-color: #2f81f7; -fx-background-insets: 0; -fx-background-radius: 4; }
            .progress-bar > .track { -fx-background-color: #0d1218; -fx-background-radius: 4; }
            .progress-indicator { -fx-progress-color: #2f81f7; }
            .separator .line { -fx-border-color: #243040; -fx-border-width: 1 0 0 0; }
            .tooltip { -fx-background-color: #1c2530; -fx-text-fill: #e6edf3; }
            """;

    private static final String STYLESHEET = "data:text/css;base64,"
            + Base64.getEncoder().encodeToString(CSS.getBytes(StandardCharsets.UTF_8));

    private Theme() {
    }

    public static void apply(Scene scene) {
        scene.getStylesheets().add(STYLESHEET);
    }

    public static void applyTo(DialogPane dialogPane) {
        dialogPane.getStylesheets().add(STYLESHEET);
    }
}
