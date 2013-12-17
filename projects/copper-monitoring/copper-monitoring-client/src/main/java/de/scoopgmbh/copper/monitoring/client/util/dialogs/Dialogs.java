// Code adapted from https://github.com/marcojakob/javafx-ui-sandbox

/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package de.scoopgmbh.copper.monitoring.client.util.dialogs;

import static de.scoopgmbh.copper.monitoring.client.util.dialogs.Dialogs.DialogResources.getIcon;
import static de.scoopgmbh.copper.monitoring.client.util.dialogs.Dialogs.DialogResources.getMessage;
import static de.scoopgmbh.copper.monitoring.client.util.dialogs.Dialogs.DialogResources.getString;
import static de.scoopgmbh.copper.monitoring.client.util.dialogs.Dialogs.DialogResponse.CLOSED;
import static de.scoopgmbh.copper.monitoring.client.util.dialogs.Dialogs.DialogResponse.OK;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Callback;

import com.sun.javafx.css.StyleManager;

/**
 * A class containing a number of pre-built JavaFX modal dialogs.
 * <p>
 * Note: This is a copy of the official OpenJFX UI Sandbox Control revision rt-9e5ef340d95f.
 * Changes are marked and described in the readme file.
 * 
 * @author OpenJFX Authors
 * @author Marco Jakob (http://edu.makery.ch)
 */
public class Dialogs {
	
	// !CHANGE START! use a separate css file
	private static final URL DIALOGS_CSS_URL = FXDialog.class.getResource("dialogs.css");
	// !CHANGE END!
    
    
    /***************************************************************************
     *                                                                         *
     * Public static support classes / enums                                   *
     *                                                                         *
     **************************************************************************/    
    
    /**
     * An enumeration used to specify the response provided by the user when
     * interacting with a dialog.
     */
    public static enum DialogResponse {
        /**
         * Used to represent that the user has selected the option corresponding 
         * with YES.
         */
        YES,
        
        /**
         * Used to represent that the user has selected the option corresponding 
         * with NO.
         */
        NO,
        
        /**
         * Used to represent that the user has selected the option corresponding 
         * with CANCEL.
         */
        CANCEL,
        
        /**
         * Used to represent that the user has selected the option corresponding 
         * with OK.
         */
        OK,
        
        /**
         * Used to represent that the user has selected the option corresponding 
         * with CLOSED.
         */
        CLOSED
    }
    
    /**
     * An enumeration used to specify which buttons to show to the user in a 
     * dialog.
     */
    public static enum DialogOptions {
        /**
         * Used to specify that two buttons should be shown, with default labels
         * specified as 'Yes' and 'No'.
         */
        YES_NO,
        
        /**
         * Used to specify that three buttons should be shown, with default labels
         * specified as 'Yes', 'No', and 'Cancel'.
         */
        YES_NO_CANCEL,
        
        /**
         * Used to specify that one button should be shown, with the default label
         * specified as 'Ok'.
         */
        OK,
        
        /**
         * Used to specify that two buttons should be shown, with default labels
         * specified as 'Ok' and 'Cancel'.
         */
        OK_CANCEL;
    }
    
    
    
    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/    
    
    private Dialogs() {
        // no-op as we don't want people creating instances of this class
    }
    
    
    
    /***************************************************************************
     *                                                                         *
     * Confirmation Dialogs                                                    *
     *                                                                         *
     **************************************************************************/    
    
    /**
     * Brings up a dialog with the options Yes, No and Cancel; with the title, 
     * <b>Select an Option</b>. 
     * 
     * @param owner
     * @param message
     * @return 
     */
    public static DialogResponse showConfirmDialog(final Stage owner, final String message) {
        return showConfirmDialog(owner, 
                                    message, 
                                    DialogType.CONFIRMATION.getDefaultMasthead());
    }
    
    public static DialogResponse showConfirmDialog(final Stage owner, final String message,
                                    final String masthead) {
        return showConfirmDialog(owner, 
                                    message, 
                                    masthead, 
                                    DialogType.CONFIRMATION.getDefaultTitle());
    }
    
    public static DialogResponse showConfirmDialog(final Stage owner, final String message,
                                    final String masthead, final String title) {
        return showConfirmDialog(owner, 
                                    message, 
                                    masthead, 
                                    title, 
                                    DialogType.CONFIRMATION.getDefaultOptions());
    }
    
    public static DialogResponse showConfirmDialog(final Stage owner, final String message,
                                    final String masthead, final String title, final DialogOptions options) {
        return showSimpleContentDialog(owner, 
                                    title,
                                    masthead, 
                                    message, 
                                    DialogType.CONFIRMATION,
                                    options);
    }
    
    

    /***************************************************************************
     *                                                                         *
     * Information Dialogs                                                     *
     *                                                                         *
     **************************************************************************/   
    
    public static void showInformationDialog(final Stage owner,
                                             final String message) {
        showInformationDialog(owner, 
                                    message, 
                                    DialogType.INFORMATION.getDefaultMasthead());
    }
    
    public static void showInformationDialog(final Stage owner, final String message,
                                             final String masthead){
        showInformationDialog(owner, 
                                    message, 
                                    masthead,
                                    DialogType.INFORMATION.getDefaultTitle());
    }
    
    /*
     * Info message string displayed in the masthead
     * Info icon 48x48 displayed in the masthead
     * "OK" button at the bottom.
     *
     * text and title strings are already translated strings.
     */
    public static void showInformationDialog(final Stage owner, final String message,
                                             final String masthead, final String title){
        showSimpleContentDialog(owner, 
                                    title,
                                    masthead, 
                                    message, 
                                    DialogType.INFORMATION,
                                    DialogType.INFORMATION.getDefaultOptions());
    }
    
    
    
    /***************************************************************************
     *                                                                         *
     * Warning Dialogs                                                         *
     *                                                                         *
     **************************************************************************/   
    
    /**
     * showWarningDialog - displays warning icon instead of "Java" logo icon
     *                     in the upper right corner of masthead.  Has masthead
     *                     and message that is displayed in the middle part
     *                     of the dialog.  No bullet is displayed.
     *
     *
     * @param  owner           - Component to parent the dialog to
     * @param  message         - question to display in the middle part
     *
     */
    public static DialogResponse showWarningDialog(final Stage owner, final String message) {
        return showWarningDialog(owner, 
                                message, 
                                DialogType.WARNING.getDefaultMasthead());
    }

    /**
     * showWarningDialog - displays warning icon instead of "Java" logo icon
     *                     in the upper right corner of masthead.  Has masthead
     *                     and message that is displayed in the middle part
     *                     of the dialog.  No bullet is displayed.
     *
     *
     * @param  owner           - Component to parent the dialog to
     * @param  masthead        - masthead in the top part of the dialog
     * @param  message         - question to display in the middle part
     *
     */
    public static DialogResponse showWarningDialog(final Stage owner, final String message,
                                        final String masthead) {
        return showWarningDialog(owner, 
                                message, 
                                masthead,
                                DialogType.WARNING.getDefaultTitle());
    }

    /**
     * showWarningDialog - displays warning icon instead of "Java" logo icon
     *                     in the upper right corner of masthead.  Has masthead
     *                     and message that is displayed in the middle part
     *                     of the dialog.  No bullet is displayed.
     *
     *
     * @param  owner           - Component to parent the dialog to
     * @param  masthead        - masthead in the top part of the dialog
     * @param  message         - question to display in the middle part
     * @param  title           - dialog title string from resource bundle
     *
     */
    public static DialogResponse showWarningDialog(final Stage owner, final String message,
                                        final String masthead, final String title) {
        return showWarningDialog(owner, 
                                message, 
                                masthead,
                                title,
                                DialogType.WARNING.getDefaultOptions());
    }
                                        
    public static DialogResponse showWarningDialog(final Stage owner, final String message,
                                        final String masthead, final String title,
                                        DialogOptions options) {
        return showSimpleContentDialog(owner, 
                                title,
                                masthead, 
                                message, 
                                DialogType.WARNING, 
                                options);
    }


    
    /***************************************************************************
     *                                                                         *
     * Exception / Error Dialogs                                               *
     *                                                                         *
     **************************************************************************/   

    public static DialogResponse showErrorDialog(final Stage owner, final String message) {
        return showErrorDialog(owner, 
                                message, 
                                DialogType.ERROR.getDefaultMasthead());
    }
    
    public static DialogResponse showErrorDialog(final Stage owner, final String message,
                                            final String masthead) {
        return showErrorDialog(owner, 
                                message, 
                                masthead,
                                masthead);
    }
    
    public static DialogResponse showErrorDialog(final Stage owner, final String message,
                                            final String masthead, final String title) {
        return showErrorDialog(owner, 
                                message, 
                                masthead,
                                title,
                                DialogType.ERROR.getDefaultOptions());
    }
    
    public static DialogResponse showErrorDialog(final Stage owner, final String message,
                                            final String masthead, final String title,
                                            DialogOptions options) {
        return showSimpleContentDialog(owner, 
                title,
                masthead, 
                message, 
                DialogType.ERROR, 
                options);
    }
    
    public static DialogResponse showErrorDialog(final Stage owner, final String message,
                                      final String masthead, final String title, 
                                      final Throwable throwable) {

        DialogTemplate<Void> template = new DialogTemplate<Void>(owner, title, masthead, null);
        template.setErrorContent(message, throwable);
        return showDialog(template);
    }
    
    
    
    /***************************************************************************
     *                                                                         *
     * User Input Dialogs                                                      *
     *                                                                         *
     **************************************************************************/  
    
    public static String showInputDialog(final Stage owner, final String message) {
        return showInputDialog(owner, message, "Masthead");
    }
    
    public static String showInputDialog(final Stage owner, final String message,
                                        final String masthead) {
        return showInputDialog(owner, message, masthead, "Title");
    }
    
    public static String showInputDialog(final Stage owner, final String message,
                                        final String masthead, final String title) {
        return showInputDialog(owner, message, masthead, title, null);
    }
    
	public static String showInputDialog(final Stage owner, final String message, 
			final String masthead, final String title, final String initialValue) {
		return showInputDialog(owner, message, masthead, title, initialValue, Collections.<String> emptyList());
	}

	public static Integer showIntInputDialog(final Stage owner, final String message, 
			final String masthead, final String title, final String initialValue) {
		String sVal = showInputDialog(owner, message, masthead, title, initialValue, Collections.<String> emptyList());
		if(sVal == null) return null;
		try {
			return Integer.valueOf(sVal);
		} catch (NumberFormatException e) {
			return null;
		}
	}

    public static <T> T showInputDialog(final Stage owner, final String message,
                                        final String masthead, final String title,
                                        final T initialValue, final T... choices) {
        return showInputDialog(owner, message, masthead, title, initialValue, Arrays.asList(choices));
    }
    
    public static <T> T showInputDialog(final Stage owner, final String message,
                                        final String masthead, final String title,
                                        final T initialValue, final List<T> choices) {
        DialogTemplate<T> template = new DialogTemplate<T>(owner, title, masthead, null);
        template.setInputContent(message, initialValue, choices);
        return showUserInputDialog(template);
    }
    
    /***************************************************************************
     *                                                                         *
     * Custom Content Dialog                                                   *
     *                                                                         *
     **************************************************************************/  

    //Provided Pane is inserted in the content panel. Provided callback is added to buttons' onAction handler.
    public static <T> DialogResponse showCustomDialog(final Stage owner, final Pane customContentPanel, final String masthead, final String title, DialogOptions options, Callback<java.lang.Void, java.lang.Void> callback) {
        DialogTemplate<T> template = new DialogTemplate<T>(owner, customContentPanel, title, masthead, options); //DialogType.CUSTOM.defaultOptions);
        template.setCustomContent(customContentPanel);
        template.setCustomCallback(callback);
        return showCustomDialog(template);
	}

	
    
    
    /***************************************************************************
     *                                                                         *
     * Private API                                                             *
     *                                                                         *
     **************************************************************************/  
    
    // NOT PUBLIC API
    static enum DialogType {
        ERROR(DialogOptions.OK, "error48.image") {
            @Override public String getDefaultMasthead() { return "Error"; }  
        },
        COPPER(DialogOptions.OK, "copper48.image") {
            @Override public String getDefaultMasthead() { return "Copper"; }
        },
        INFORMATION(DialogOptions.OK, "info48.image") {
            @Override public String getDefaultMasthead() { return "Message"; }
        },
        WARNING(DialogOptions.OK, "warning48.image") {
            @Override public String getDefaultMasthead() { return "Warning"; }
        },
        CONFIRMATION(DialogOptions.YES_NO_CANCEL, "confirm48.image") {
            @Override public String getDefaultMasthead() { return "Select an Option"; }
        },
        INPUT(DialogOptions.OK_CANCEL, "confirm48.image") {
            @Override public String getDefaultMasthead() { return "Select an Option"; }
        },
        CUSTOM(DialogOptions.OK, "info48.image") {
            @Override public String getDefaultMasthead() { return "Message"; }
        };
        
        private final DialogOptions defaultOptions;
        private final String imageResource;
        
        DialogType(DialogOptions defaultOptions, String imageResource) {
            this.defaultOptions = defaultOptions;
            this.imageResource = imageResource;
        }
        
        public ImageView getImage() {
            return getIcon(imageResource);
        }

        public String getDefaultTitle() {
            return getDefaultMasthead();
        }
        
        public abstract String getDefaultMasthead();

        public DialogOptions getDefaultOptions() {
            return defaultOptions;
        }
    }
    
    private static DialogResponse showSimpleContentDialog(final Stage owner,
                                        final String title, final String masthead, 
                                        final String message, DialogType dialogType,
                                        final DialogOptions options) {
        DialogTemplate<Void> template = new DialogTemplate<Void>(owner, title, masthead, options);
        template.setSimpleContent(message, dialogType);
        return showDialog(template);
    }
    
    private static DialogResponse showDialog(DialogTemplate<?> template) {
        try {
            template.getDialog().centerOnScreen();
            template.show();
            return template.getResponse();
        } catch (Throwable e) {
            return CLOSED;
        }
    }
    
    private static <T> T showUserInputDialog(DialogTemplate<T> template) {
    	// !CHANGE START! return null if user did not click ok
		template.getDialog().centerOnScreen();
		template.show();
		if (template.getResponse() == OK) {
			return template.getInputResponse();
		} else {
			return null;
		}
		// !CHANGE END!
    }

	private static DialogResponse showCustomDialog(DialogTemplate<?> template) {
		try {
			//template.options = DialogType.CUSTOM.defaultOptions;
			template.getDialog().centerOnScreen();
			template.show();
	        return template.getResponse();
		} catch (Throwable e) {
			return CLOSED;
		}
//		if (template.getResponse() == OK) {
//			return template.getInputResponse();
//		} else {
//			return null;
//		}
	}    
    /**
     * 
     * @param <T> The type for user input
     */
    private static class DialogTemplate<T> {
        private static enum DialogStyle {
            SIMPLE,
            ERROR,
            INPUT,
            CUSTOM;
        }

        private FXDialog dialog;
        private VBox contentPane;

        private DialogType dialogType = DialogType.INFORMATION;
        private final DialogOptions options;
        private DialogResponse userResponse = DialogResponse.CLOSED;

        private DialogStyle style;

        // for user input dialogs (textfield / choicebox / combobox)
        private T initialInputValue;
        private List<T> inputChoices;
        // !CHANGE START! change to property so we can use binding
        private Property<T> userInputResponse;
        // !CHANGE END!


        // masthead
        private String mastheadString;
        private BorderPane mastheadPanel;
        private ImageView mastheadIcon;
        private UITextArea mastheadTextArea;

        // center
        private Pane centerPanel;
        private String contentString = null;

        // Buttons
        private ObservableList<Button> buttons;
        private static final String okBtnStr = "common.ok.btn";
        private static final String yesBtnStr = "common.yes.btn";
        private static final String noBtnStr = "common.no.btn";
        private static final String cancelBtnStr = "common.cancel.btn";
        private static final String detailBtnStr = "common.detail.button";

        // This is used in the exception dialog only.
        private Throwable throwable = null;    

        // Visual indication of security level alert - either high or medium.
        // Located in the lower left corner at the bottom of the dialog.
        private static final String SECURITY_ALERT_HIGH = "security.alert.high.image";
        private static final String SECURITY_ALERT_LOW  = "security.alert.low.image";
        private ImageView securityIcon;

        // These are for security dialog only.
        private String[] alertStrs;
        private String[] infoStrs;
        
        //Custom panel
        private Pane customContentPanel;
		private Callback<Void, Void> callback;



        /***************************************************************************
         *                                                                         *
         * Constructors                                                            *
         *                                                                         *
         **************************************************************************/    

        DialogTemplate(Stage owner, String title, String masthead, DialogOptions options) {
            this.dialog = new FXDialog(title, owner, true);

            this.contentPane = new VBox();
            this.dialog.setContentPane(contentPane);

            this.mastheadString = masthead;
            this.options = options;
        }

        public void setCustomCallback(Callback<Void, Void> callback) {
        	this.callback = callback;
		}

		DialogTemplate(Stage owner, Pane customContent, String title, String masthead, DialogOptions options) {
        	this(owner, title, masthead, options);
        	this.customContentPanel = customContent;
        }




        /***************************************************************************
         *                                                                         *
         * Dialog construction API                                                 *
         *                                                                         *
         **************************************************************************/

        void setSimpleContent(String contentString, DialogType dialogType) {
            setSimpleContent(contentString, dialogType, null, true);
        }

        void setSimpleContent(String contentString, DialogType dialogType,
                              String infoString, boolean useWarningIcon) {
            this.style = DialogStyle.SIMPLE;
            this.contentString = contentString;

            this.dialogType = dialogType == null ? DialogType.WARNING : dialogType;
            if (infoString != null) {
                String[] strs = { infoString };
                if (useWarningIcon) {
                    this.alertStrs = strs;
                } else {
                    this.infoStrs = strs;
                }
            }

            contentPane.getChildren().add(createMasthead());
            contentPane.getChildren().add(createCenterPanel());

            Pane bottomPanel = createBottomPanel();
            if (bottomPanel != null) {
                contentPane.getChildren().add(bottomPanel);
            }

            dialog.setResizable(false);
        }

        void setErrorContent(String contentString, Throwable throwable) {
            this.style = DialogStyle.ERROR;
            this.contentString = contentString;
            this.throwable = throwable;

            this.dialogType = DialogType.ERROR;

            contentPane.getChildren().add(createMasthead());
            contentPane.getChildren().add(createCenterPanel());

            Pane bottomPanel = createBottomPanel();
            if (bottomPanel != null && bottomPanel.getChildren().size() > 0) {
                contentPane.getChildren().add(bottomPanel);
            }

            dialog.setResizable(false);
        }

        void setInputContent(String message, T initialValue, List<T> choices) {
            this.style = DialogStyle.INPUT;
            this.dialogType = DialogType.COPPER;
            this.contentString = message;
            this.initialInputValue = initialValue;
            this.inputChoices = choices;

            contentPane.setPrefWidth(240);
            contentPane.getChildren().add(createMasthead());
            contentPane.getChildren().add(createCenterPanel());

            Pane bottomPanel = createBottomPanel();
            if (bottomPanel != null) {
                contentPane.getChildren().add(bottomPanel);
            }

            dialog.setResizable(false);
        }
        
        void setCustomContent(Pane customContent){
        	this.style = DialogStyle.CUSTOM;
        	this.customContentPanel = customContent;

        	contentPane.getChildren().add(createMasthead());
            contentPane.getChildren().add(createCenterPanel());

            Pane bottomPanel = createBottomPanel();
            if (bottomPanel != null) {
                contentPane.getChildren().add(bottomPanel);
            }

            dialog.setResizable(false);
        }



        /***************************************************************************
         *                                                                         *
         * 'Public' API                                                            *
         *                                                                         *
         **************************************************************************/

        public FXDialog getDialog() {
            return dialog;
        }

        public void show() {
            dialog.showAndWait();
        }

        public void hide() {
            dialog.hide();
        }

        /**
         * gets the response from the user.
         * @return the response
         */
        public DialogResponse getResponse() {
            return userResponse;
        }

        public T getInputResponse() {
        	// !CHANGE START!
        	if (userInputResponse != null) {
        		return userInputResponse.getValue();
        	}
        	return null;
        	// !CHANGE END!
        }



        /***************************************************************************
         *                                                                         *
         * Implementation                                                          *
         *                                                                         *
         **************************************************************************/

        /*
         * top part of the dialog contains short informative message, and either
         * an icon, or the text is displayed over a watermark image
         */
        private Pane createMasthead() {
            mastheadPanel = new BorderPane();
            mastheadPanel.getStyleClass().add("top-panel");
            mastheadPanel.setPrefWidth(-1);

            // Create panel with text area and icon or just a background image:
            // Create topPanel's components.  UITextArea determines
            // the size of the dialog by defining the number of columns
            // based on font size.
            mastheadTextArea = new UITextArea(-1);
            mastheadTextArea.setText(mastheadString);
//            mastheadTextArea = new UITextArea(mastheadString);
            mastheadTextArea.setAlignment(Pos.CENTER_LEFT);
            mastheadTextArea.getStyleClass().add("masthead-label-1");

            VBox mastheadVBox = new VBox();
            mastheadVBox.setAlignment(Pos.CENTER_LEFT);
            mastheadVBox.getChildren().add(mastheadTextArea);
//            mastheadVBox.setPrefWidth(MAIN_TEXT_WIDTH);

            mastheadPanel.setLeft(mastheadVBox);
            BorderPane.setAlignment(mastheadVBox, Pos.CENTER_LEFT);
            mastheadIcon = dialogType == null ? getIcon("java48.image") : dialogType.getImage();
            mastheadPanel.setRight(mastheadIcon);

            return mastheadPanel;
        }

        private Pane createCenterPanel() {
            centerPanel = new VBox();
            centerPanel.getStyleClass().add("center-panel");

            BorderPane contentPanel = new BorderPane();
            contentPanel.getStyleClass().add("center-content-panel");
            VBox.setVgrow(contentPanel, Priority.ALWAYS);

            Node content = createCenterContent();
            if (content != null) {
                contentPanel.setCenter(content);
                contentPanel.setPadding(new Insets(0, 0, 12, 0));
            }

            FlowPane buttonsPanel = new FlowPane(6, 0) {
                @Override protected void layoutChildren() {
                    /*
                    * According to UI guidelines, all buttons should have the same length.
                    * This function is to define the longest button in the array of buttons
                    * and set all buttons in array to be the length of the longest button.
                    */
                    // Find out the longest button...
                    double widest = 50;
                    for (int i = 0; i < buttons.size(); i++) {
                        Button btn = buttons.get(i);
                        if (btn == null) continue;
                        widest = Math.max(widest, btn.prefWidth(-1));
                    }

                    // ...and set all buttons to be this width
                    for (int i = 0; i < buttons.size(); i++) {
                        Button btn = buttons.get(i);
                        if (btn == null) continue;
                        btn.setPrefWidth(btn.isVisible() ? widest : 0);
                    }
                    
                    super.layoutChildren();
                }
            };
            buttonsPanel.getStyleClass().add("button-bar");

            // Create buttons from okBtnStr and cancelBtnStr strings.
            buttonsPanel.getChildren().addAll(createButtons());

            if (contentPanel.getChildren().size() > 0) {
                centerPanel.getChildren().add(contentPanel);
            }

            BorderPane bottomPanel = new BorderPane();
            bottomPanel.getStyleClass().add("center-bottom-panel");
            bottomPanel.setRight(buttonsPanel);
            centerPanel.getChildren().add(bottomPanel);

            return centerPanel;
        }

        @SuppressWarnings("unchecked")
		private Node createCenterContent() {
        	// !CHANGE START!
            if (style == DialogStyle.SIMPLE || style == DialogStyle.ERROR) {
                if (contentString != null) {
                    UITextArea ta = new UITextArea(contentString);
                    ta.getStyleClass().add("center-content-area");
                    ta.setAlignment(Pos.TOP_LEFT);
                    return ta;
                }
            } else if (style == DialogStyle.INPUT) {
                Control inputControl = null;
                userInputResponse = new SimpleObjectProperty<T>();
                if (inputChoices == null || inputChoices.isEmpty()) {
                    // no input constraints, so use a TextField
                    final TextField textField = new TextField();
                    textField.setPrefWidth(66);
                    userInputResponse.bind((ObservableValue<T>) textField.textProperty());
                    textField.setOnAction(new EventHandler<ActionEvent>() {
                        @Override public void handle(ActionEvent t) {
                        	userResponse = DialogResponse.OK;
                            hide();
                        }
                    });
                    if (initialInputValue != null) {
                        textField.setText(initialInputValue.toString());
                    }
                    inputControl = textField;
                } else {
                    // input method will be constrained to the given choices
//                    ChangeListener<T> changeListener = new ChangeListener<T>() {
//                        @Override public void changed(ObservableValue<? extends T> ov, T t, T t1) {
//                            userInputResponse = t1;
//                        }
//                    };

                    if (inputChoices.size() > 10) {
                        // use ComboBox
                        ComboBox<T> comboBox = new ComboBox<T>();
                        comboBox.getItems().addAll(inputChoices);
                        comboBox.getSelectionModel().select(initialInputValue);
                        userInputResponse.bind(comboBox.valueProperty());
//                        comboBox.getSelectionModel().selectedItemProperty().addListener(changeListener);
                        inputControl = comboBox;
                    } else {
                        // use ChoiceBox
                        ChoiceBox<T> choiceBox = new ChoiceBox<T>();
                        choiceBox.getItems().addAll(inputChoices);
                        choiceBox.getSelectionModel().select(initialInputValue);
//                        choiceBox.getSelectionModel().selectedItemProperty().addListener(changeListener);
                        userInputResponse.bind(choiceBox.valueProperty());
                        inputControl = choiceBox;
                    }
                    // !CHANGE END!
                }

                HBox hbox = new HBox(10);

                if (contentString != null && ! contentString.isEmpty()) {
                    Label label = new Label(contentString);
                    hbox.getChildren().add(label);
                }

                if (inputControl != null) {
                    hbox.getChildren().add(inputControl);
                }

                return hbox;
            } else if(style == DialogStyle.CUSTOM){
            	return customContentPanel;
            }

            return null;
        }

        private List<Button> createButtons() {
            buttons = FXCollections.observableArrayList();

            if (style == DialogStyle.INPUT) {
                buttons.addAll(createButton(okBtnStr, DialogResponse.OK, true, false),
                                createButton(cancelBtnStr, DialogResponse.CANCEL, false, true));
            } else {
                if (DialogType.ERROR == dialogType && throwable != null) {
                    // we've got an error dialog, which has 'OK' and 'Details..' buttons
                    buttons.addAll(createButton(okBtnStr, DialogResponse.OK, true, false));

                    Button detailsBtn = new Button((detailBtnStr == null) ? "" : getMessage(detailBtnStr));
                    detailsBtn.setOnAction(new EventHandler<ActionEvent>() {
                        @Override public void handle(ActionEvent ae) {
                            new ExceptionDialog(dialog, throwable).show();
                        }
                    });
                    buttons.add(detailsBtn);
                } else if (options == DialogOptions.OK) {
                    buttons.addAll(createButton(okBtnStr, DialogResponse.OK, true, false));
                } else if (options == DialogOptions.OK_CANCEL) {
                    buttons.addAll(createButton(okBtnStr, DialogResponse.OK, true, false),
                                createButton(cancelBtnStr, DialogResponse.CANCEL, false, true));
                } else if (options == DialogOptions.YES_NO) {
                    buttons.addAll(createButton(yesBtnStr, DialogResponse.YES, true, false),
                                createButton(noBtnStr, DialogResponse.NO, false, true));
                } else if (options == DialogOptions.YES_NO_CANCEL) {
                    buttons.addAll(createButton(yesBtnStr, DialogResponse.YES, true, false),
                                createButton(noBtnStr, DialogResponse.NO, false, true),
                                createButton(cancelBtnStr, DialogResponse.CANCEL, false, false));
                }
            }

            return buttons;
        }

        private Button createButton(final String extLabel, final DialogResponse response, 
                final boolean isDefault, final boolean isCancel) {
            Button btn = new Button((extLabel == null) ? "" : getMessage(extLabel));
            btn.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent ae) {
                    userResponse = response;

                    //If callback provided for custom dialog - call it. 
                    if(callback != null){ callback.call(null);}
                    
                    // hide the dialog.  We'll return from the dialog,
                    // and who ever called it will retrieve user's answer
                    // and will dispose of the dialog after that.
                    hide();
                }
            });
            btn.setDefaultButton(isDefault);
            btn.setCancelButton(isCancel);

            return btn;
        }

        /*
         * bottom panel contains icon indicating the security alert level,
         * two bullets with most significant security warnings,
         * link label - to view more details about security warnings.
         */
        private Pane createBottomPanel() {
            if (alertStrs == null && infoStrs == null) return null;

            HBox bottomPanel = new HBox();
            bottomPanel.getStyleClass().add("bottom-panel");

            // Icon 32x32 pixels with indication of secutiry alert - high/low

            // If there are no messages in securityAlerts, show
            // SECURITY_ALERT_LOW icon in the lower left corner of
            // security dialog.
            String imageFile = SECURITY_ALERT_HIGH;

            if (alertStrs == null || alertStrs.length == 0) {
                imageFile = SECURITY_ALERT_LOW;
            }
            securityIcon = getIcon(imageFile);

            // Add icon to the bottom panel.
            bottomPanel.getChildren().add(securityIcon);

            // If there are no alerts (alertStrs is null, or length is 0),
            // then we should show only first message from infoStrs.
            // this is how it will work for security dialog...
            int textAreaWidth = 333;
            UITextArea bulletText = new UITextArea(textAreaWidth);
            bulletText.getStyleClass().add("bottom-text");

            if ((alertStrs == null || alertStrs.length == 0)
                && infoStrs != null && infoStrs.length != 0) {
                // If there are no alerts, use first string from the infoStrs.
                bulletText.setText((infoStrs[0] != null) ? infoStrs[0] : " ");
            } else if (alertStrs != null && alertStrs.length != 0) {
                // If there are any alerts, use first string from alertStrs.
                bulletText.setText((alertStrs[0] != null) ? alertStrs[0] : " ");
            }

            bottomPanel.getChildren().add(bulletText);

    //        if (moreInfoLbl != null) {
    //            bottomPanel.getChildren().add(moreInfoLbl);
    //        }

            return bottomPanel;
        }
    }
    
    
    
    private static class UITextArea extends Label {
        double preferred_width = 360;

        /**
         * Creates a new instance of UITextArea
         */
        public UITextArea(String text) {
            setText(text);
            init();
        }

        /** 
         * Creates a new instance of UITextArea with specified preferred width.
         * This is used by the dialog UI template.
         */
        public UITextArea(double my_width) {
            preferred_width = my_width;
            init();
        }

        private void init() {
            setPrefWidth(preferred_width);
            setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
            setWrapText(true);
        }
    }

    
    
    /**
     * Heavyweight dialog implementation
     */
    private static class FXDialog extends Stage {
        private BorderPane root;
        private RootPane decoratedRoot;
        private HBox windowBtns;
        private Button minButton;
        private Button maxButton;
        private Rectangle resizeCorner;
        private double mouseDragOffsetX = 0;
        private double mouseDragOffsetY = 0;
        protected Label titleLabel;

        private static final int HEADER_HEIGHT = 28;

        FXDialog(String title) {
            this(title, null, false);
        }

        FXDialog(String title, Window owner, boolean modal) {
            this(title, owner, modal, StageStyle.TRANSPARENT);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
		FXDialog(String title, Window owner, boolean modal, StageStyle stageStyle) {
            super(stageStyle);

            setTitle(title);

            if (owner != null) {
                initOwner(owner);
            }

            if (modal) {
                initModality(Modality.WINDOW_MODAL);
            }

            resizableProperty().addListener(new InvalidationListener() {
                @Override public void invalidated(Observable valueModel) {
                    resizeCorner.setVisible(isResizable());
                    maxButton.setVisible(isResizable());

                    if (isResizable()) {
                        windowBtns.getChildren().add(1, maxButton);
                    } else {
                        windowBtns.getChildren().remove(maxButton);
                    }
                }
            });

            root = new BorderPane();

            Scene scene;
            if (stageStyle == StageStyle.DECORATED) {
                scene = new Scene(root);
                // !CHANGE START!
                scene.getStylesheets().addAll(DIALOGS_CSS_URL.toExternalForm());
                // !CHANGE END!
                setScene(scene);
                return;
            }

            // *** The rest is for adding window decorations ***

            decoratedRoot = new RootPane() {
                @Override protected void layoutChildren() {
                    super.layoutChildren();
                    if (resizeCorner != null) {
                        resizeCorner.relocate(getWidth() - 20, getHeight() - 20);
                    }
                }
            };
            decoratedRoot.getChildren().add(root);
            scene = new Scene(decoratedRoot);
            // !CHANGE START!
			String css = (String) AccessController.doPrivileged(new PrivilegedAction() {
                @Override public Object run() {
                    return DIALOGS_CSS_URL.toExternalForm();
                }
            });
            scene.getStylesheets().addAll(css);
            // !CHANGE END!
            scene.setFill(Color.TRANSPARENT);
            setScene(scene);

            decoratedRoot.getStyleClass().addAll("dialog", "decorated-root");

            focusedProperty().addListener(new InvalidationListener() {
                @Override public void invalidated(Observable valueModel) {
                    decoratedRoot.pseudoClassStateChanged("active");
                }
            });

            ToolBar toolBar = new ToolBar();
            toolBar.getStyleClass().add("window-header");
            toolBar.setPrefHeight(HEADER_HEIGHT);
            toolBar.setMinHeight(HEADER_HEIGHT);
            toolBar.setMaxHeight(HEADER_HEIGHT);

            // add window dragging
            toolBar.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent event) {
                    mouseDragOffsetX = event.getSceneX();
                    mouseDragOffsetY = event.getSceneY();
                }
            });
            toolBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent event) {
                    setX(event.getScreenX() - mouseDragOffsetX);
                    setY(event.getScreenY() - mouseDragOffsetY);
                }
            });

            titleLabel = new Label();
            titleLabel.getStyleClass().add("window-title");
            titleLabel.setText(getTitle());

            titleProperty().addListener(new InvalidationListener() {
                @Override public void invalidated(Observable valueModel) {
                    titleLabel.setText(getTitle());
                }
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // add close min max
            Button closeButton = createWindowButton("close");
            closeButton.setOnAction(new EventHandler() {
                @Override public void handle(Event event) {
                    FXDialog.this.hide();
                }
            });
            minButton = createWindowButton("minimize");
            minButton.setOnAction(new EventHandler() {
                @Override public void handle(Event event) {
                    setIconified(!isIconified());
                }
            });

            maxButton = createWindowButton("maximize");
            maxButton.setOnAction(new EventHandler() {
                private double restoreX;
                private double restoreY;
                private double restoreW;
                private double restoreH;

                @Override public void handle(Event event) {
                    Screen screen = Screen.getPrimary(); // todo something more sensible
                    double minX = screen.getVisualBounds().getMinX();
                    double minY = screen.getVisualBounds().getMinY();
                    double maxW = screen.getVisualBounds().getWidth();
                    double maxH = screen.getVisualBounds().getHeight();

                    if (restoreW == 0 || getX() != minX || getY() != minY || getWidth() != maxW || getHeight() != maxH) {
                        restoreX = getX();
                        restoreY = getY();
                        restoreW = getWidth();
                        restoreH = getHeight();
                        setX(minX);
                        setY(minY);
                        setWidth(maxW);
                        setHeight(maxH);
                    } else {
                        setX(restoreX);
                        setY(restoreY);
                        setWidth(restoreW);
                        setHeight(restoreH);
                    }
                }
            });

            windowBtns = new HBox(3);
            windowBtns.getStyleClass().add("window-buttons");
            windowBtns.getChildren().addAll(minButton, maxButton, closeButton);

            toolBar.getItems().addAll(titleLabel, spacer, windowBtns);
            root.setTop(toolBar);

            resizeCorner = new Rectangle(10, 10);
            resizeCorner.getStyleClass().add("window-resize-corner");

            // add window resizing
            EventHandler<MouseEvent> resizeHandler = new EventHandler<MouseEvent>() {
                private double width;
                private double height;
                private Point2D dragAnchor;

                @Override public void handle(MouseEvent event) {
                    EventType type = event.getEventType();

                    if (type == MouseEvent.MOUSE_PRESSED) {
                        width = getWidth();
                        height = getHeight();
                        dragAnchor = new Point2D(event.getSceneX(), event.getSceneY());
                    } else if (type == MouseEvent.MOUSE_DRAGGED) {
                        setWidth(Math.max(decoratedRoot.minWidth(-1),   width  + (event.getSceneX() - dragAnchor.getX())));
                        setHeight(Math.max(decoratedRoot.minHeight(-1), height + (event.getSceneY() - dragAnchor.getY())));
                    }
                }
            };
            resizeCorner.setOnMousePressed(resizeHandler);
            resizeCorner.setOnMouseDragged(resizeHandler);

            resizeCorner.setManaged(false);
            decoratedRoot.getChildren().add(resizeCorner);
        }

        void setContentPane(Pane pane) {
            if (pane.getId() == null) {
                pane.getStyleClass().add("content-pane");
            }
            root.setCenter(pane);
        }

//        public void setIconifiable(boolean iconifiable) {
//            minButton.setVisible(iconifiable);
//        }
        
        private Button createWindowButton(String name) {
            StackPane graphic = new StackPane();
            graphic.getStyleClass().setAll("graphic");
            
            Button button = new Button();
            button.getStyleClass().setAll("window-button");
            button.getStyleClass().add("window-"+name+"-button");
            button.setGraphic(graphic);
            button.setMinSize(17, 17);
            button.setPrefSize(17, 17);
            return button;
        }
        

        
        private static class RootPane extends StackPane {
            /*******************************************************************
             *                                                                 *
             * Stylesheet Handling                                             *
             *                                                                 *
             *******************************************************************/

        	// !CHANGE START!
            private static final long PSEUDO_CLASS_ACTIVE_MASK = 
                    StyleManager.getInstance().getPseudoclassMask("active");
            // !CHANGE END!

            @SuppressWarnings("deprecation")
			@Override public long impl_getPseudoClassState() {
                long mask = super.impl_getPseudoClassState();
                if (getScene().getWindow().isFocused()) {
                    mask |= PSEUDO_CLASS_ACTIVE_MASK;
                }
                return mask;
            }

            @SuppressWarnings("deprecation")
			private void pseudoClassStateChanged(String pseudoClass) {
                impl_pseudoClassStateChanged(pseudoClass);
            }
        }
    }
    
    private static class ExceptionDialog extends FXDialog {
        public ExceptionDialog(Stage parent, Throwable throwable) {
            super(getMessage("exception.dialog.title"));

            initModality(Modality.APPLICATION_MODAL);
            
            // --- initComponents
            VBox contentPanel = new VBox();
            contentPanel.getStyleClass().add("more-info-dialog");

            contentPanel.setPrefSize(800, 600);

            if (throwable != null) {
                BorderPane labelPanel = new BorderPane();

                Label label = new Label(getString("exception.dialog.label"));
                labelPanel.setLeft(label);

                contentPanel.getChildren().add(labelPanel);

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                throwable.printStackTrace(pw);
                TextArea text = new TextArea(sw.toString());
                text.setEditable(false);
                text.setWrapText(true);
                text.setPrefWidth(60 * 8);
                text.setPrefHeight(20 * 12);

                VBox.setVgrow(text, Priority.ALWAYS);
                contentPanel.getChildren().add(text);
            }
            
            // --- getBtnPanel
            // This panel contains right-aligned "Close" button.  It should
            // dismiss the dialog and dispose of it.
            HBox btnPanel = new HBox();
            btnPanel.getStyleClass().add("button-panel");

            Button dismissBtn = new Button(getMessage("common.close.btn"));
            dismissBtn.setPrefWidth(80);
            dismissBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    hide();
                }
            });

            dismissBtn.setDefaultButton(true);
            btnPanel.getChildren().add(dismissBtn);
            contentPanel.getChildren().add(btnPanel);
            // --- getBtnPanel

            setContentPane(contentPanel);
            // --- initComponents
        }
    }
    
    
    
    static class DialogResources {
        // Localization strings.
        private final static ResourceBundle dialogsResourceBundle = 
                ResourceBundle.getBundle("de.scoopgmbh.copper.monitoring.client.util.dialogs.resources.dialog-resources");

        /**
         * Method to get an internationalized string from the deployment resource.
         */
        static String getMessage(String key) {
            try {
                return dialogsResourceBundle.getString(key);
            } catch (MissingResourceException ex) {
                // Do not trace this exception, because the key could be
                // an already translated string.
                System.out.println("Failed to get string for key '" + key + "'");
                return key;
            }
        }

        /**
        * Returns a string from the resources
        */
        static String getString(String key) {
            try {
                return dialogsResourceBundle.getString(key);
            } catch (MissingResourceException mre) {
                // Do not trace this exception, because the key could be
                // an already translated string.
                System.out.println("Failed to get string for key '" + key + "'");
                return key;
            }
        }

        /**
        * Returns a string from a resource, substituting argument 1
        */
        static String getString(String key, Object... args) {
            return MessageFormat.format(getString(key), args);
        }

        /**
         * Returns an <code>ImageView</code> given an image file name or resource name
         */
        static public ImageView getIcon(final String key) {
            try {
                return AccessController.doPrivileged(
                    new PrivilegedExceptionAction<ImageView>()   {
                        @Override public ImageView run() {
                            String resourceName = getString(key);
                            URL url = DialogResources.class.getResource(resourceName);
                            if (url == null) {
                                System.out.println("Can't create ImageView for key '" + key + 
                                        "', which has resource name '" + resourceName + 
                                        "' and URL 'null'");
                                return null;
                            }
                            return getIcon(url);
                        }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }

        static public ImageView getIcon(URL url) {
            return new ImageView(new Image(url.toString()));
        }
    }
}