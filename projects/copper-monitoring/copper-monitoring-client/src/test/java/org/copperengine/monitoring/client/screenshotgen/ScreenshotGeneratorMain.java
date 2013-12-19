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
package org.copperengine.monitoring.client.screenshotgen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;

import org.copperengine.monitoring.client.adapter.GuiCopperDataProvider;
import org.copperengine.monitoring.client.form.BorderPaneShowFormStrategie;
import org.copperengine.monitoring.client.screenshotgen.view.fixture.ApplicationFixture;
import org.copperengine.monitoring.client.screenshotgen.view.fixture.ScreenshotPageBase;
import org.copperengine.monitoring.client.screenshotgen.view.fixture.TestDataProvider;
import org.copperengine.monitoring.client.screenshotgen.view.fixture.TestFormContext;
import org.copperengine.monitoring.client.ui.settings.AuditralColorMapping;
import org.copperengine.monitoring.client.ui.settings.SettingsModel;
import org.copperengine.monitoring.client.util.MessageProvider;
import org.jemmy.fx.SceneDock;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

public class ScreenshotGeneratorMain {

    public static final int SHORT_WAIT_TIME = 100;
    public static final int LONG_WAIT_TIME = SHORT_WAIT_TIME * 10;
    public static final String OUTPUT_FOLDER = "screenshots";

    public static void main(String[] args) {
        new ScreenshotGeneratorMain().run();
    }

    void run() {
        deleteOutputFolder();
        new Thread() {
            @Override
            public void run() {
                ApplicationFixture.launchWorkaround();
            }
        }.start();
        try {
            Thread.sleep(SHORT_WAIT_TIME);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        scene = new SceneDock();

        ArrayList<ScreenshotPageBase> tests = new ArrayList<ScreenshotPageBase>();
        try {
            for (ClassInfo classInfo : ClassPath.from(getClass().getClassLoader()).getTopLevelClassesRecursive(ScreenshotGeneratorMain.class.getPackage().getName())) {
                if (ScreenshotPageBase.class.isAssignableFrom(classInfo.load()) && !ScreenshotPageBase.class.equals(classInfo.load())) {
                    try {
                        final ScreenshotPageBase test = (ScreenshotPageBase) classInfo.load().newInstance();
                        tests.add(test);
                    } catch (InstantiationException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (final ScreenshotPageBase screenshotPageBase : tests) {
            before(screenshotPageBase);
            writeScreenshotTo(new File(OUTPUT_FOLDER + "/" + screenshotPageBase.getTitle() + ".png"));
        }

    }

    public void before(final ScreenshotPageBase integrationtestBase) {
        final TestDataProvider testDataProvider = new TestDataProvider();
        final GuiCopperDataProvider guiCopperDataProvider = new GuiCopperDataProvider(testDataProvider);

        integrationtestBase.setTestDataProvider(testDataProvider);
        integrationtestBase.setScene(scene);

        runInGuithreadAndWait(new Runnable() {
            @Override
            public void run() {
                SettingsModel defaultSettings = new SettingsModel();
                AuditralColorMapping newItem = new AuditralColorMapping();
                newItem.color.setValue(Color.rgb(255, 128, 128));
                newItem.loglevelRegEx.setValue("1");
                defaultSettings.auditralColorMappings.add(newItem);

                TestFormContext testFormContext = new TestFormContext(
                        ApplicationFixture.getPane(),
                        guiCopperDataProvider,
                        new MessageProvider(ResourceBundle.getBundle("org.copperengine.gui.message")),
                        defaultSettings);
                integrationtestBase.initGui(ApplicationFixture.getPane(), testFormContext);
            }
        });
        try {
            Thread.sleep(integrationtestBase.getWaitForInitGuiMs()); // wait for Background worker
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public static void runInGuithreadAndWait(Runnable run) {
        FutureTask<Void> futureTask = new FutureTask<Void>(run, null);
        Platform.runLater(futureTask);
        try {
            futureTask.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static BorderPaneShowFormStrategie getShowFormStrategy() {
        return new BorderPaneShowFormStrategie(ApplicationFixture.getPane());
    }

    public void after() {
        runInGuithreadAndWait(new Runnable() {
            @Override
            public void run() {
                ApplicationFixture.getPane().getChildren().clear();
            }
        });
    }

    public void writeScreenshotTo(File file) {
        FutureTask<WritableImage> task = new FutureTask<WritableImage>(new Callable<WritableImage>() {
            @Override
            public WritableImage call() throws Exception {
                WritableImage image = scene.wrap().getControl().snapshot(null);
                return image;
            }
        });
        Platform.runLater(task);

        WritableImage image;
        try {
            image = task.get();
        } catch (InterruptedException e1) {
            throw new RuntimeException(e1);
        } catch (ExecutionException e1) {
            throw new RuntimeException(e1);
        } // wait for completition, blocking the thread if needed

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };

    protected SceneDock scene;

    public void deleteOutputFolder() {
        File folder = new File(OUTPUT_FOLDER);
        if (folder.exists() && folder.getAbsoluteFile().getParent() != null) {// dont delete complete drive
            deleteReportFolderInternal(folder);
        }
    }

    private void deleteReportFolderInternal(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { // some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteReportFolderInternal(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    private String getRelativePath(File base, File childname) {
        String basePath;
        String childPath;
        basePath = base.getParentFile().toURI().toString();
        childPath = childname.toURI().toString();
        return childPath.substring(basePath.length());
    }

}
