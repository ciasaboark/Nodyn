/*
 * Copyright (c) 2018 Jonathan Nelson <ciasaboark@gmail.com>
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

package io.phobotic.nodyn_app.database.model;

/**
 * Created by Jonathan Nelson on 7/8/17.
 */

public class Category {
    private int id = -1;
    private String name;
    private String categoryType;
    private int count;
    private boolean acceptance;
    private String eulaText;
    private boolean useDefaultEula;

    public int getId() {
        return id;
    }

    public Category setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Category setName(String name) {
        this.name = name;
        return this;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public Category setCategoryType(String categoryType) {
        this.categoryType = categoryType;
        return this;
    }

    public int getCount() {
        return count;
    }

    public Category setCount(int count) {
        this.count = count;
        return this;
    }

    public boolean isAcceptance() {
        return acceptance;
    }

    public Category setAcceptance(boolean acceptance) {
        this.acceptance = acceptance;
        return this;
    }

    public String getEulaText() {
        return eulaText;
    }

    public Category setEulaText(String eulaText) {
        this.eulaText = eulaText;
        return this;
    }

    public boolean isUseDefaultEula() {
        return useDefaultEula;
    }

    public Category setUseDefaultEula(boolean useDefaultEula) {
        this.useDefaultEula = useDefaultEula;
        return this;
    }

    public class Columns {
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String CATEGORY_TYPE = "category_type";
        public static final String COUNT = "count";
        public static final String ACCEPTANCE = "acceptance";
        public static final String EULA_TEXT = "eula_text";
        public static final String USE_DEFAULT_EULA = "eula_default";
    }
}
