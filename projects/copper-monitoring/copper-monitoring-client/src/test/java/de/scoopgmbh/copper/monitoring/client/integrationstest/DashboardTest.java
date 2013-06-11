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
package de.scoopgmbh.copper.monitoring.client.integrationstest;


import javafx.scene.layout.BorderPane;

import org.jemmy.fx.control.LabeledDock;
import org.jemmy.fx.control.TabPaneDock;
import org.jemmy.fx.control.TextInputControlDock;

import de.scoopgmbh.copper.monitoring.client.integrationstest.fixture.IntegrationtestBase;
import de.scoopgmbh.copper.monitoring.client.integrationstest.fixture.TestFormContext;



public class DashboardTest extends IntegrationtestBase{
	
//	public static class CellInfo{
//		String row;
//		String column;
//		
//		public CellInfo(String row, String column) {
//			super();
//			this.row = row;
//			this.column = column;
//		}
//		public String getRow() {
//			return row;
//		}
//		public void setRow(String row) {
//			this.row = row;
//		}
//		public String getColumn() {
//			return column;
//		}
//		public void setColumn(String column) {
//			this.column = column;
//		}
//		
//	}
//	
//	
//	private void executeGuiAction(Action action) {
//		QueueExecutor.EXECUTOR.execute(Environment.getEnvironment(), true, action);
//	}
//	
//	
//	public void select(final String col, final String row){
//		executeGuiAction(new Action() {
//			@Override
//			public void run(Object... parameters) throws Exception {
//				table.getSelectionManager().selectSingleCell(Integer.valueOf(col), Integer.valueOf(row));
//			}
//		});
//	}
//
//    public CellInfo selectLeftWithKey() {
//    	scene.keyboard().pushKey(KeyboardButtons.LEFT);
//    	return new CellInfo(
//    	    	""+table.getSelectionManager().getMinSelectedCellRow(), 
//    	    	""+table.getSelectionManager().getMinSelectedCellColumn());
//    }
//    
//    public CellInfo selectRightWithKey() {
//    	scene.keyboard().pushKey(KeyboardButtons.RIGHT);
//    	return new CellInfo(
//    	    	""+table.getSelectionManager().getMinSelectedCellRow(), 
//    	    	""+table.getSelectionManager().getMinSelectedCellColumn());
//    }
//    
//    public CellInfo selectUpWithKey() {
//    	scene.keyboard().pushKey(KeyboardButtons.UP);
//    	return new CellInfo(
//    	    	""+table.getSelectionManager().getMinSelectedCellRow(), 
//    	    	""+table.getSelectionManager().getMinSelectedCellColumn());
//    }
//    
//    public CellInfo selectDownWithKey() {
//    	scene.keyboard().pushKey(KeyboardButtons.DOWN);
//    	return new CellInfo(
//    	    	""+table.getSelectionManager().getMinSelectedCellRow(), 
//    	    	""+table.getSelectionManager().getMinSelectedCellColumn());
//    }
//	
//	
//	@Before
//	public void before(){
//		new Thread(){
//    		@Override
//    		public void run() {
////    			ApplicationFixture application = new ApplicationFixture();
//    			ApplicationFixture.launchWorkaround();
////    			try {
////					application.start(new Stage());
////				} catch (Exception e) {
////					throw new RuntimeException(e);
////				}
//    		}
//    	}.start();
//    	
//    	scene = new SceneDock();
//    	table = ApplicationFixture.getTable();
//	}
//	SceneDock scene;
//	Table2<TeilUeberwachung> table;
//
//	
//    @AfterClass
//    public static void after(){
//		try {
//			// Set your page url in this string. For eg, I m using URL for Google Search engine
//			String file = new File(System.getProperty("concordion.output.dir")).getAbsolutePath()+ "\\" +
//			TableTest.class.getName().replace(".", "\\").replaceAll("Test", "") + ".html";
//			java.awt.Desktop.getDesktop().open(new File(file));
//		} catch (java.io.IOException e) {
//			e.printStackTrace();
//		}
//    }
	
	
	public void bla(){
		
	}

	@Override
	public void initGui(BorderPane pane, TestFormContext testFormContext) {
		testFormContext.createDashboardForm().show();

	}
	
	public int setPoolSize(String size){
		TextInputControlDock textInput = new TextInputControlDock(scene.asParent(),"nummerNew");
		textInput.type(size);
		LabeledDock setButton = new LabeledDock(scene.asParent(),"nummerbutton");
		setButton.mouse().click();
		return testDataProvider.numberOfThreads;
	}
	
	public int setBatcherPoolSize(String size){
		TextInputControlDock textInput = new TextInputControlDock(scene.asParent(),"batcherNewNum");
		textInput.type(size);
		LabeledDock setButton = new LabeledDock(scene.asParent(),"batcherNumSet");
		setButton.mouse().click();
		return testDataProvider.numerOfThreadsBatcher;
	}
	
	public int setPriority(String size){
		TextInputControlDock textInput = new TextInputControlDock(scene.asParent(),"prioNew");
		textInput.type(size);
		LabeledDock setButton = new LabeledDock(scene.asParent(),"prioButton");
		setButton.mouse().click();
		return testDataProvider.threadPriority;
	}
	
	public int getPoolsTabsCount(){
		TabPaneDock tabpane = new TabPaneDock(scene.asParent(),"pools");
		return tabpane.getTabs().size();
	}

}