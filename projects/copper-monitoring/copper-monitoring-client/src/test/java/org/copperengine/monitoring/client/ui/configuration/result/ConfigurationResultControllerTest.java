/*
 * Copyright 2002-2015 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.copperengine.monitoring.client.ui.configuration.result;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.copperengine.monitoring.client.ui.configuration.result.ConfigurationResultController;
import org.copperengine.monitoring.core.model.ConfigurationInfo;
import org.junit.Assert;
import org.junit.Test;

public class ConfigurationResultControllerTest {

    @Test
    public void test_FindByDate() throws Exception {
        List<ConfigurationInfo> configurationInfos = new ArrayList<ConfigurationInfo>();
        configurationInfos.add(new ConfigurationInfo(new Date(4),null,null,null));
        configurationInfos.add(new ConfigurationInfo(new Date(3),null,null,null));
        configurationInfos.add(new ConfigurationInfo(new Date(2),null,null,null));
        configurationInfos.add(new ConfigurationInfo(new Date(1),null,null,null));
        configurationInfos.add(new ConfigurationInfo(new Date(0),null,null,null));

        ConfigurationInfo result = new ConfigurationResultController(null,null).findByDate(configurationInfos,new Date(1));
        Assert.assertEquals(configurationInfos.get(3).getTimeStamp().getTime(),result.getTimeStamp().getTime());
    }

    @Test
    public void test_FindByDate_gap() throws Exception {
        List<ConfigurationInfo> configurationInfos = new ArrayList<ConfigurationInfo>();
        configurationInfos.add(new ConfigurationInfo(new Date(9),null,null,null));
        configurationInfos.add(new ConfigurationInfo(new Date(7),null,null,null));
        configurationInfos.add(new ConfigurationInfo(new Date(2),null,null,null));
        configurationInfos.add(new ConfigurationInfo(new Date(1),null,null,null));
        configurationInfos.add(new ConfigurationInfo(new Date(0),null,null,null));

        ConfigurationInfo result = new ConfigurationResultController(null,null).findByDate(configurationInfos,new Date(6));
        Assert.assertEquals(configurationInfos.get(2).getTimeStamp().getTime(),result.getTimeStamp().getTime());
    }

    @Test
    public void test_FindByDate_first() throws Exception {
        List<ConfigurationInfo> configurationInfos = new ArrayList<ConfigurationInfo>();
        configurationInfos.add(new ConfigurationInfo(new Date(9),null,null,null));
        configurationInfos.add(new ConfigurationInfo(new Date(7),null,null,null));
        configurationInfos.add(new ConfigurationInfo(new Date(2),null,null,null));
        configurationInfos.add(new ConfigurationInfo(new Date(1),null,null,null));
        configurationInfos.add(new ConfigurationInfo(new Date(0),null,null,null));

        ConfigurationInfo result = new ConfigurationResultController(null,null).findByDate(configurationInfos,new Date(9));
        Assert.assertEquals(configurationInfos.get(0).getTimeStamp().getTime(),result.getTimeStamp().getTime());
    }
}
