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
package de.scoopgmbh.copper.monitoring.client.doc.view.load;


import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import de.scoopgmbh.copper.monitoring.client.doc.view.fixture.FilterAbleFormFixture;
import de.scoopgmbh.copper.monitoring.client.doc.view.fixture.IntegrationtestBase;
import de.scoopgmbh.copper.monitoring.client.doc.view.fixture.TestFormContext;



public class CopperLoad extends IntegrationtestBase{
	
	@Override
	public void initGui(BorderPane pane, TestFormContext testFormContext) {
		testFormContext.createEngineLoadForm().show();
		final FilterAbleFormFixture filterAbleFormFixture= new FilterAbleFormFixture(scene);
		for (int i=0;i<10;i++){
			final int ifinal = i;
			new Thread(){
				@Override
				public void run() {
					try {
						Thread.sleep(1010*ifinal);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							filterAbleFormFixture.refresh();
						}
					});
				};
			}.start();
		}
	}

	@Override
	public String getTitle() {
		return "copper load";
	}
	
	@Override
	public long getWaitForInitGuiMs(){
		return super.getWaitForInitGuiMs()*15;
	}

}