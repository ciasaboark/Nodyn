/*
 * Copyright (c) 2019 Jonathan Nelson <ciasaboark@gmail.com>
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

package io.phobotic.nodyn_app.preference;

/**
 * Created by Jonathan Nelson on 2/23/19.
 */
public class SettingsPage {
    private int curPage;
    private int totalPages;
    private String jsonFragment;
    private boolean isCompressed;
    private int versionCode;

    public SettingsPage(int versionCode, int curPage, int totalPages, String jsonFragment, boolean isCompressed) {
        this.versionCode = versionCode;
        this.curPage = curPage;
        this.totalPages = totalPages;
        this.jsonFragment = jsonFragment;
        this.isCompressed = isCompressed;
    }

    public int getCurPage() {
        return curPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public String getJsonFragment() {
        return jsonFragment;
    }

    public boolean isCompressed() {
        return isCompressed;
    }

    public int getVersionCode() {
        return versionCode;
    }
}
