package de.scoopgmbh.copper.monitoring.client.integrationstest;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
import org.mockito.Mockito;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import de.scoopgmbh.copper.monitoring.client.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.monitoring.client.form.BorderPaneShowFormStrategie;
import de.scoopgmbh.copper.monitoring.client.integrationstest.fixture.ApplicationFixture;
import de.scoopgmbh.copper.monitoring.client.integrationstest.fixture.IntegrationtestBase;
import de.scoopgmbh.copper.monitoring.client.integrationstest.fixture.TestDataProvider;
import de.scoopgmbh.copper.monitoring.client.integrationstest.fixture.TestFormContext;
import de.scoopgmbh.copper.monitoring.client.ui.settings.SettingsModel;
import de.scoopgmbh.copper.monitoring.client.util.MessageProvider;

public class IntegrationTestMain {
	
	private static final int SHORT_WAIT_TIME = 100;

	
	public static void main(String[] args) {
		new IntegrationTestMain().run();
	}
	
	void run(){
		System.setProperty("concordion.output.dir", "report");
		deleteReportFolder(new File(System.getProperty("concordion.output.dir")));
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
    	
		ArrayList<IntegrationtestBase> tests = new ArrayList<IntegrationtestBase>();
		try {
			for (ClassInfo classInfo: ClassPath.from(getClass().getClassLoader()).getTopLevelClassesRecursive("de.scoopgmbh.copper.monitoring.client.integrationstest")){
				if (IntegrationtestBase.class.isAssignableFrom(classInfo.load()) && !IntegrationtestBase.class.equals(classInfo.load())){
					try {
						final IntegrationtestBase test = (IntegrationtestBase) classInfo.load().newInstance();
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
		
		
		ConcordionBuilder concordionBuilder = new ConcordionBuilder();
		extension.addTo(concordionBuilder);
		
		for (final IntegrationtestBase integrationtestBase: tests){
			ResultSummary resultSummary;
			try {
				before(integrationtestBase);
				resultSummary = concordionBuilder.build().process(integrationtestBase);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			resultSummary.print(System.out, this);
			resultSummary.assertIsSatisfied(this);
			after();
		}
		
		File summary = writeSummary(tests);
		afterAll(summary);
		// resultSummary.getFailureCount()
	}


	public void before(final IntegrationtestBase integrationtestBase){
		final TestDataProvider testDataProvider = new TestDataProvider();
		final GuiCopperDataProvider guiCopperDataProvider = new GuiCopperDataProvider(testDataProvider);
    	
		integrationtestBase.setTestDataProvider(testDataProvider);
		integrationtestBase.setScene(scene);
		
    	runInGuithreadAndWait(new Runnable() {
			@Override
			public void run() {
				TestFormContext testFormContext = new TestFormContext(
						ApplicationFixture.getPane(),
						guiCopperDataProvider, 
						new MessageProvider(ResourceBundle.getBundle("de.scoopgmbh.copper.gui.message")), 
						Mockito.mock(SettingsModel.class));
				integrationtestBase.initGui(ApplicationFixture.getPane(),testFormContext);
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

	void afterAll(final File summeryFile){
		runInGuithreadAndWait(new Runnable() {
			@Override
			public void run() {
				Stage stage = new Stage();
				stage.setHeight(800);
				stage.setWidth(1024);
				final BorderPane borderPane = new BorderPane();
				final WebView webView = new WebView();
				
				try {
					webView.getEngine().load(
							summeryFile.toURI().toURL().toExternalForm());
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
				borderPane.setCenter(webView);
				final Button button = new Button("open report folder");
				button.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						try {
							Desktop.getDesktop().open(summeryFile.getParentFile());
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
				
				ApplicationFixture.getStage().close();

			}
			
		});
		try {
			Thread.sleep(Long.MAX_VALUE);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		

	}
	
	
	
	private String getGeneratedHtmlPath(final IntegrationtestBase integrationtestBase) {
		String name = integrationtestBase.getClass().getName().replace(".", "/");
		if (name.endsWith("Test")){
			name = name.substring(0, name.length()-"Test".length());
		}
		final String reportfile = new File(System.getProperty("concordion.output.dir")).getAbsolutePath()+"/"+
		name+".html";
		return reportfile;
	}
	
	public void after(){
		runInGuithreadAndWait(new Runnable() {
			@Override
			public void run() {
				ApplicationFixture.getPane().getChildren().clear();
			}
		});
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
	
	
	public File writeSummary(List<IntegrationtestBase> tests){
		File newTextFile = new File(new File(System.getProperty("concordion.output.dir")).getAbsolutePath()+"/de/scoopgmbh/copper/monitoring/client/integrationstest/index.html");
		
		URL url = Resources.getResource("de/scoopgmbh/copper/monitoring/client/integrationstest/index.html");
		String file;
		try {
			file = Resources.toString(url, Charsets.UTF_8);
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		StringBuilder list = new StringBuilder();
		list.append("<ul>");
		for (IntegrationtestBase test:tests){

			String relative = newTextFile.toURI().relativize(new File(getGeneratedHtmlPath(test)).toURI()).getPath();
				
			list.append("<li>");
			list.append("<a href=\"");
			list.append(relative);
			list.append("\"> ");
			list.append(test.getClass().getSimpleName());
			list.append("</a>");
			list.append("</li>");
			
			
		}
		list.append("</ul>");
		file = file.replace("<!-- $testlist -->", list.toString());
        
        try {
			FileWriter fw = new FileWriter(newTextFile);
			fw.write(file);
			fw.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
        return newTextFile;
	}
}
