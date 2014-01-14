/*
 * Copyright 2002-2014 SCOOP Software GmbH
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
package org.copperengine.monitoring.client.ui.worklowinstancedetail.result;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;
import javafx.util.Callback;

import org.copperengine.monitoring.client.adapter.GuiCopperDataProvider;
import org.copperengine.monitoring.client.form.filter.FilterResultControllerBase;
import org.copperengine.monitoring.client.ui.worklowinstancedetail.filter.WorkflowInstanceDetailFilterModel;
import org.copperengine.monitoring.client.util.CodeMirrorFormatter;
import org.copperengine.monitoring.client.util.CodeMirrorFormatter.CodeFormatLanguage;
import org.copperengine.monitoring.core.debug.DisplayableNode;
import org.copperengine.monitoring.core.debug.NodeTyp;
import org.copperengine.monitoring.core.debug.StackFrame;

import com.google.common.base.Charsets;

public class WorkflowInstanceDetailResultController extends FilterResultControllerBase<WorkflowInstanceDetailFilterModel, WorkflowInstanceDetailResultModel>
        implements Initializable {
    private final GuiCopperDataProvider copperDataProvider;
    private final CodeMirrorFormatter codeMirrorFormatter;

    public WorkflowInstanceDetailResultController(GuiCopperDataProvider copperDataProvider, CodeMirrorFormatter codeMirrorFormatter) {
        super();
        this.copperDataProvider = copperDataProvider;
        this.codeMirrorFormatter = codeMirrorFormatter;
    }

    private class LazyTreeItem extends TreeItem<DisplayableNode> {
        private boolean hasLoadedChildren = false;

        public LazyTreeItem(DisplayableNode item) {
            super(item);
        }

        @Override
        public ObservableList<TreeItem<DisplayableNode>> getChildren() {
            if (!hasLoadedChildren) {
                loadChildren();
            }
            return super.getChildren();
        }

        @Override
        public boolean isLeaf() {
            if (!hasLoadedChildren) {
                loadChildren();
            }
            return super.getChildren().isEmpty();
        }

        private void loadChildren() {
            hasLoadedChildren = true;
            for (DisplayableNode displayableNode : getValue().getChildren()) {
                final LazyTreeItem node = new LazyTreeItem(displayableNode);
                super.getChildren().add(node);
            }

        }
    }

    @FXML
    // fx:id="sourceView"
    private WebView sourceView; // Value injected by FXMLLoader

    @FXML
    // fx:id="sourceView"
    private Button restart; // Value injected by FXMLLoader

    @FXML
    // fx:id="titleText"
    private TextField titleText; // Value injected by FXMLLoader

    @FXML
    // fx:id="treeView"
    private TreeView<DisplayableNode> treeView; // Value injected by FXMLLoader

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert restart != null : "fx:id=\"restart\" was not injected: check your FXML file 'WorkflowInstanceDetailResult.fxml'.";
        assert sourceView != null : "fx:id=\"sourceView\" was not injected: check your FXML file 'WorkflowInstanceDetailResult.fxml'.";
        assert titleText != null : "fx:id=\"titleText\" was not injected: check your FXML file 'WorkflowInstanceDetailResult.fxml'.";
        assert treeView != null : "fx:id=\"treeView\" was not injected: check your FXML file 'WorkflowInstanceDetailResult.fxml'.";

        final Image icon = new Image(getClass().getResourceAsStream("/org/copperengine/gui/icon/stackframe.png"));
        treeView.setRoot(new TreeItem<DisplayableNode>(null));
        treeView.setShowRoot(false);
        treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<DisplayableNode>>() {
            @Override
            public void changed(
                    ObservableValue<? extends TreeItem<DisplayableNode>> observable,
                    TreeItem<DisplayableNode> oldValue,
                    TreeItem<DisplayableNode> newValue) {
                TreeItem<DisplayableNode> currentItem = newValue;
                while (currentItem != null) {
                    if (currentItem.getValue() instanceof StackFrame) {
                        StackFrame sf = (StackFrame) currentItem.getValue();
                        byte[] sourceBytes = sf.getSourceCode();
                        String sourceCode = new String(sourceBytes, Charsets.UTF_8);

                        int line = sf.getLine() != null ? sf.getLine() : 0;
                        sourceView.getEngine().loadContent(codeMirrorFormatter.format(sourceCode, CodeFormatLanguage.JAVA, false, line));
                        return;
                    }
                    currentItem = currentItem.getParent();
                }
            }

        });
        treeView.setCellFactory(new Callback<TreeView<DisplayableNode>, TreeCell<DisplayableNode>>() {
            @Override
            public TreeCell<DisplayableNode> call(TreeView<DisplayableNode> param) {
                return new TreeCell<DisplayableNode>() {
                    @Override
                    protected void updateItem(final DisplayableNode node, boolean empty) {
                        super.updateItem(node, empty);
                        if (node != null) {
                            setText(node.getDisplayValue());
                            if (node.getTyp() == NodeTyp.STACKFRAME) {
                                setGraphic(new ImageView(icon));
                            }
                        }
                    }
                };
            }
        });

        restart.getStyleClass().add("copperActionButton");
        restart.setDisable(true);
    }

    @Override
    public URL getFxmlResource() {
        return getClass().getResource("WorkflowInstanceDetailResult.fxml");
    }

    @Override
    public void showFilteredResult(List<WorkflowInstanceDetailResultModel> filteredResult, final WorkflowInstanceDetailFilterModel usedFilter) {
        restart.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                copperDataProvider.restartInstance(usedFilter.workflowInstanceId.get(), usedFilter.selectedEngine.get().getId());
            }
        });
        restart.setDisable(false);

        titleText.setText(usedFilter.workflowInstanceId.get());

        final WorkflowInstanceDetailResultModel workflowInstanceDetailResultModel = filteredResult.get(0);
        List<StackFrame> stackList = workflowInstanceDetailResultModel.workflowClassMetaData.get().getWorkflowInstanceDetailedInfo().getStack();
        String source = stackList.isEmpty() ? "" : new String(stackList.get(0).getSourceCode());
        sourceView.getEngine().loadContent(codeMirrorFormatter.format(source, CodeFormatLanguage.JAVA, false));

        treeView.getRoot().getChildren().clear();
        for (DisplayableNode displayableNode : stackList) {
            final LazyTreeItem item = new LazyTreeItem(displayableNode);
            treeView.getRoot().getChildren().add(item);

        }

    }

    @Override
    public List<WorkflowInstanceDetailResultModel> applyFilterInBackgroundThread(WorkflowInstanceDetailFilterModel filter) {
        return Arrays.asList(copperDataProvider.getWorkflowDetails(filter));
    }

    @Override
    public boolean supportsClear() {
        return true;
    }

    @Override
    public void clear() {
        restart.setDisable(true);
    }
}
