/*
 * Copyright 2002-2015 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.copperengine.monitoring.client.form.filter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import org.copperengine.monitoring.client.form.FxmlController;
import org.copperengine.monitoring.client.util.MessageProvider;

import com.sun.javafx.runtime.VersionInfo;
import com.sun.javafx.scene.control.behavior.TableCellBehavior;

/**
 * @param <F>Filtermodel
 * @param <R>Resultmodel
 */
public abstract class FilterResultControllerBase<F, R> implements FilterResultController<F, R>, FxmlController {

    private final List<TableView<?>> tableViews = new ArrayList<TableView<?>>();
    private final Map<TableView<?>, ObservableList<?>> originalItemsMap = new HashMap<TableView<?>, ObservableList<?>>(); 

    public <M> HBox createTableControls(final TableView<M> tableView) {        
        this.tableViews.add(tableView);

        if (tableView.getContextMenu() == null) {
            tableView.setContextMenu(new ContextMenu());
        }
        final MenuItem copyMenuItem = new MenuItem("copy table");
        final MenuItem copyCellMenuItem = new MenuItem("copy cell");
        fillContextMenu(tableView, copyMenuItem, copyCellMenuItem);
        tableView.contextMenuProperty().addListener(new ChangeListener<ContextMenu>() {
            @Override
            public void changed(ObservableValue<? extends ContextMenu> observable, ContextMenu oldValue, ContextMenu
                    newValue) {
                if (newValue != null) {
                    fillContextMenu(tableView, copyMenuItem, copyCellMenuItem);
                }
            }
        });
        copyMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                copyTable(tableView);
            }
        });
        copyCellMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                copyTableCell(tableView);
            }
        });

        HBox pane = new HBox();
        BorderPane.setMargin(pane, new Insets(3));
        pane.setSpacing(3);
        pane.setAlignment(Pos.CENTER_LEFT);

        ObservableList<String> columnOptions = FXCollections.observableArrayList("<ANY COLUMN>");
        for(TableColumn<M, ?> column : tableView.getColumns()) {
            columnOptions.add(column.getText());
        }        
        final ComboBox<String> columnField = new ComboBox<String>(columnOptions);
        columnField.setPromptText("column");
        columnField.getSelectionModel().select(0);
        
        final TextField filterField = new TextField();
        filterField.setPromptText("filter");
        
        final CheckBox regExp = new CheckBox("RegExp");
        final Runnable filterAction = new Runnable() {
            @Override
            public void run() {
                applyFilter(columnField.getSelectionModel().getSelectedIndex()-1, tableView, filterField.getText(), regExp.isSelected());
            }
        };        
        
        columnField.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                filterAction.run();
            }
        });
        filterField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                filterAction.run();
            }
        });
        regExp.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                filterAction.run();
            }
        });
        
        HBox.setHgrow(filterField, Priority.ALWAYS);
        final Label count = new Label("count: 0");
        tableView.itemsProperty().addListener(new ChangeListener<ObservableList<M>>() {
            @Override
            public void changed(ObservableValue<? extends ObservableList<M>> observable,
                                ObservableList<M> oldValue,
                                ObservableList<M> newValue) {
                if (newValue != null) {
                    count.setText("count: " + String.valueOf(newValue.size()));
                }
            }
        });
        pane.getChildren().add(columnField);
        pane.getChildren().add(filterField);
        pane.getChildren().add(regExp);
        pane.getChildren().add(new Separator(Orientation.VERTICAL));
        pane.getChildren().add(count);

        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        return pane;
    }

    protected<M> void setOriginalItems(TableView<M> tableView, ObservableList<M> items) {
        tableView.setItems(items);
        originalItemsMap.put(tableView, items);
    }
    
    protected<M> ObservableList<M> getOriginalItems(TableView<M> tableView) {
        @SuppressWarnings("unchecked")
        ObservableList<M> origItems = (ObservableList<M>)originalItemsMap.get(tableView);
        if(origItems == null) {
            origItems = FXCollections.observableArrayList();
            originalItemsMap.put(tableView, origItems);
        }
        return origItems;
    }
    
    private<M> void applyFilter(int columnIndex, TableView<M> tableView, String filterText, boolean isRegExp) {
        ObservableList<M> origItems = getOriginalItems(tableView);
        ObservableList<M> filteredItems;
        if(filterText == null || filterText.length() == 0) {            
            filteredItems = origItems;
        } else {
            filteredItems = FXCollections.observableArrayList();
            if(!origItems.isEmpty()) {
                ObservableList<TableColumn<M, ?>> tableColumns = tableView.getColumns();
                ObservableList<TableColumn<M, ?>> columns = tableColumns;
                if(columnIndex >= 0 && columnIndex < tableColumns.size()) {
                    columns = FXCollections.observableArrayList();
                    columns.add(tableColumns.get(columnIndex));
                }
                for(M item : origItems) {
                    for(TableColumn<M, ?> column : columns) {
                        Object cell = column.getCellData(item);
                        if (cell != null && cell.toString() != null) {
                            String val = cell.toString();
                            boolean matches = isRegExp ? val.matches(filterText) : val.contains(filterText) ;
                            if(matches) {
                                filteredItems.add(item);
                                break;
                            }
                        }
                    }
                }
            }            
        }
        tableView.setItems(filteredItems);
    }

    private <M> void fillContextMenu(final TableView<M> tableView, final MenuItem copyMenuItem, MenuItem copyCellMenuItem) {
        tableView.getContextMenu().getItems().add(new SeparatorMenuItem());
        tableView.getContextMenu().getItems().add(copyMenuItem);
        tableView.getContextMenu().getItems().add(copyCellMenuItem);
    }

    private void copyTable(final TableView<?> tableView) {
        StringBuilder clipboardString = new StringBuilder();
        for (int row = 0; row < tableView.getItems().size(); row++) {
            for (int column = 0; column < tableView.getColumns().size(); column++) {
                Object cell = tableView.getColumns().get(column).getCellData(row);
                clipboardString.append(cell);
                clipboardString.append("\t");
            }
            clipboardString.append("\n");
        }
        final ClipboardContent content = new ClipboardContent();
        content.putString(clipboardString.toString());
        Clipboard.getSystemClipboard().setContent(content);
    }

    private void copyTableCell(final TableView<?> tableView) {
        StringBuilder clipboardString = new StringBuilder();
        for (TablePosition<?, ?> tablePosition : tableView.getSelectionModel().getSelectedCells()) {
            Object cell = tableView.getColumns().get(tablePosition.getColumn()).getCellData(tablePosition.getRow());
            clipboardString.append(cell);
        }
        final ClipboardContent content = new ClipboardContent();
        content.putString(clipboardString.toString());
        Clipboard.getSystemClipboard().setContent(content);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void onClose() {
        // workaround for javafx memoryleaks RT-25652, RT-32087
        if (VersionInfo.getRuntimeVersion().startsWith("2")){
            for (TableView tableView : tableViews) {
                tableView.getFocusModel().focus(null);
                Class tcbClass = TableCellBehavior.class;
                try {
                    Method anchorMethod = tcbClass.getDeclaredMethod("setAnchor", TableView.class, TablePosition.class);
                    anchorMethod.setAccessible(true);
                    anchorMethod.invoke(null, tableView, null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                tableView.setOnMouseClicked(null);
                tableView.setSelectionModel(null);
                tableView.getColumns().clear();
                tableView.setItems(FXCollections.observableArrayList());
                tableView = null;
            }
            tableViews.clear();
        }

    }

    @Override
    public boolean supportsClear() {
        return false;
    }

    @Override
    public void clear() {
        // default empty implementation
    }

    @Override
    public List<? extends Node> getContributedButtons(MessageProvider messageProvider) {
        return Collections.emptyList();
    }
}
