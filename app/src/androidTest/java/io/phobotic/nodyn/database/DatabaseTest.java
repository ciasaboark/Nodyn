/*
 * Copyright (c) 2017 Jonathan Nelson <ciasaboark@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.phobotic.nodyn.database;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.FullDataModel;

/**
 * Created by Jonathan Nelson on 7/13/17.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class DatabaseTest {
    Database db;
    Context instrumentationCtx;


    @Before
    public void setUp() throws Exception {
        instrumentationCtx = InstrumentationRegistry.getContext();
        db = Database.getInstance(instrumentationCtx);
    }

    @Test
    public void testInsertAsset() {
        Asset asset = new Asset()
                .setId(1)
                .setName("1");
        List<Asset> assetList = new ArrayList<>();
        assetList.add(asset);
        FullDataModel model = new FullDataModel()
                .setAssets(assetList);

        db.updateModel(model);

        List<Asset> insertedAssets = db.getAssets();
        assert insertedAssets.contains(asset) : "Unable to pull asset from database after " +
                "updating full data model";
    }

}