/*
 * Copyright 2002-2013 SCOOP Software GmbH
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
package de.scoopgmbh.copper.monitoring.client.util;

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.util.Callback;

public class TableColumnHelper {
	
	public static interface ColumnStringAccessor<T>{
		public Property<String> getProperty(T row);
	}
	public static interface ColumnBooleanAccessor<T>{
		public Property<Boolean> getProperty(T row);
	}
	
	/**
	 * T Tableobject
	 * Column Display Type
	 */
	public static <T> void setupEdiableStringColumn(TableColumn<T,String> column, final  ColumnStringAccessor<T> propertyAcessor){
		column.getTableView().setEditable(true);
		column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<T,String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(CellDataFeatures<T, String> param) {
				return propertyAcessor.getProperty(param.getValue());
			}
		});
		column.setEditable(true);
		column.setOnEditCommit(new EventHandler<CellEditEvent<T, String>>() {
			@Override
			public void handle(CellEditEvent<T, String> t) {
				propertyAcessor.getProperty(t.getRowValue()).setValue(t.getNewValue());
			}
		});
		column.setCellFactory(new Callback<TableColumn<T,String>, TableCell<T,String>>() {
			@Override
			public TableCell<T, String> call(TableColumn<T, String> param) {
				return new EditingCell<T>();
			}
		});
	}
	
	public static <T> void setupEdiableBooleanColumn(TableColumn<T,Boolean> column, final  ColumnBooleanAccessor<T> propertyAcessor){
		column.getTableView().setEditable(true);
		column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<T,Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<T, Boolean> param) {
				return propertyAcessor.getProperty(param.getValue());
			}
		});
		column.setOnEditCommit(new EventHandler<CellEditEvent<T, Boolean>>() {
			@Override
			public void handle(CellEditEvent<T, Boolean> t) {
				propertyAcessor.getProperty(t.getRowValue()).setValue(t.getNewValue());
			}
		});
		column.setCellFactory(CheckBoxTableCell.forTableColumn(column));
		column.setEditable(true);
	}

    public static class EditingCell<T>  extends TableCell<T, String> {
 
        private TextField textField;
 
        public EditingCell() {
        }
 
        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.selectAll();
            }
        }
 
        @Override
        public void cancelEdit() {
            super.cancelEdit();
 
            setText((String) getItem());
            setGraphic(null);
        }
 
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
 
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
        }
 
        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
            textField.focusedProperty().addListener(new ChangeListener<Boolean>(){
                @Override
                public void changed(ObservableValue<? extends Boolean> arg0, 
                    Boolean arg1, Boolean arg2) {
                        if (!arg2) {
                            commitEdit(textField.getText());
                        }
                }
            });
        }
 
        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }

}
