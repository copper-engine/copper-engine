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
package de.scoopgmbh.copper.monitoring.client.integrationstest.fixture;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.imageio.ImageIO;

import org.concordion.api.ResultSummary;
import org.concordion.api.extension.ConcordionExtension;
import org.concordion.api.extension.Extension;
import org.concordion.ext.ScreenshotExtension;
import org.concordion.ext.ScreenshotTaker;
import org.concordion.internal.ConcordionBuilder;
import org.jemmy.fx.SceneDock;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import de.scoopgmbh.copper.monitoring.client.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.monitoring.client.form.BorderPaneShowFormStrategie;
import de.scoopgmbh.copper.monitoring.client.ui.settings.SettingsModel;
import de.scoopgmbh.copper.monitoring.client.util.MessageProvider;

/** Basic testcase for Concordion */
public abstract class IntegrationtestBase {
	private static final int SHORT_WAIT_TIME = 100;

	private static boolean firstTest=true;
	

	@BeforeClass
	public static void basicSetUp() {
		System.out.println(firstTest);
		if (firstTest){
			System.setProperty("concordion.output.dir", "report");
			deleteReportFolder(new File(System.getProperty("concordion.output.dir")));
		}
		firstTest=false;
	}

	
	@Before
	public void before(){
		Assume.assumeTrue( Desktop.isDesktopSupported() );
		new Thread(){
    		@Override
    		public void run() {
    			ApplicationFixture.launchWorkaround();
    		}
    	}.start();
    	try {
    		Thread.sleep(100);
    	} catch (InterruptedException e) {
    		throw new RuntimeException(e);
    	}
    	scene = new SceneDock();
    	
    	
    	testDataProvider = new TestDataProvider();
		final GuiCopperDataProvider guiCopperDataProvider = new GuiCopperDataProvider(testDataProvider);
    	
    	runInGuithreadAndWait(new Runnable() {
			@Override
			public void run() {
				TestFormContext testFormContext = new TestFormContext(
						ApplicationFixture.getPane(),
						guiCopperDataProvider, 
						new MessageProvider(ResourceBundle.getBundle("de.scoopgmbh.copper.gui.message")), 
						Mockito.mock(SettingsModel.class));
				initGui(ApplicationFixture.getPane(),testFormContext);
			}
		});	
    	try {
			Thread.sleep(SHORT_WAIT_TIME); //wait for Background worker
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void runInGuithreadAndWait(Runnable run){
    	FutureTask<Void> futureTask = new FutureTask<Void>(run,null);
    	Platform.runLater(futureTask);
    	try {
			futureTask.get();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static BorderPaneShowFormStrategie getShowFormStrategy(){
		return new BorderPaneShowFormStrategie(ApplicationFixture.getPane());
	}
	
	public abstract void initGui(BorderPane pane, TestFormContext testFormContext);
	
	
	@After
	public void after(){
		runInGuithreadAndWait(new Runnable() {
			@Override
			public void run() {
				ApplicationFixture.getPane().getChildren().clear();
			}
		});
		final Object outer = this;
		runInGuithreadAndWait(new Runnable() {
			@Override
			public void run() {
				Stage stage = new Stage();
				stage.setHeight(800);
				stage.setWidth(1024);
				final BorderPane borderPane = new BorderPane();
				final WebView webView = new WebView();
				
				String name = outer.getClass().getName().replace(".", "/");
				if (name.endsWith("Test")){
					name = name.substring(0, name.length()-"Test".length());
				}
				final String reportfile = new File(System.getProperty("concordion.output.dir")).getAbsolutePath()+"/"+
				name+".html";
				try {
					webView.getEngine().load(
							new File(reportfile).toURI().toURL().toExternalForm());
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
				borderPane.setCenter(webView);
				final Button button = new Button("open report folder");
				button.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						try {
							Desktop.getDesktop().open(new File(reportfile).getParentFile());
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				});
				HBox buttonbox= new HBox();
				buttonbox.getChildren().add(button);
				borderPane.setBottom(buttonbox);
				stage.setScene(new Scene(borderPane));
				stage.show();
				stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
					@Override
					public void handle(WindowEvent event) {
						System.exit(0);
					}
				});
			}
			
		});
		try {
			Thread.sleep(Long.MAX_VALUE);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	private ScreenshotTaker camera = new ScreenshotTaker() {

		@Override
		public int writeScreenshotTo(final OutputStream outputStream) throws IOException {
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
				ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", outputStream);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			return (int) image.getWidth();
		}

		@Override
		public String getFileExtension() {
			return "png";
		}

	};

	@Extension
	public ConcordionExtension extension = new ScreenshotExtension().setScreenshotTaker(camera).setScreenshotOnAssertionSuccess(false)
			.setScreenshotOnThrowable(false).setMaxWidth(400);

	protected SceneDock scene;

	protected TestDataProvider testDataProvider;

	@Test
	public void executeConcordionTest() throws Throwable {
		ConcordionBuilder concordionBuilder = new ConcordionBuilder();
		extension.addTo(concordionBuilder);
		ResultSummary resultSummary = concordionBuilder.build().process(this);
		resultSummary.print(System.out, this);
		resultSummary.assertIsSatisfied(this);
		// resultSummary.getFailureCount()
	}

	public static void deleteReportFolder(File folder) {
		if (folder.exists() && (folder.toString().contains("tmpTestOutput") || folder.toString().contains("report"))) {
			File[] files = folder.listFiles();
			if (files != null) { // some JVMs return null for empty dirs
				for (File f : files) {
					if (f.isDirectory()) {
						deleteReportFolder(f);
					} else {
						f.delete();
					}
				}
			}
			folder.delete();
		}
	}

}