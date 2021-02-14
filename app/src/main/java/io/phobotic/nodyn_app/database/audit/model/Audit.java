/*
 * Copyright (c) 2020 Jonathan Nelson <ciasaboark@gmail.com>
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

package io.phobotic.nodyn_app.database.audit.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jonathan Nelson on 2020-02-18.
 */
public class Audit {
    private AuditHeader header;
    private List<AuditDetail> details = new ArrayList<>();

    public Audit(AuditHeader header) {
        this.header = header;
    }

    public void setDetails(List<AuditDetail> details) {
        this.details = details;
    }

    public AuditHeader getHeader() {
        return header;
    }

    public List<AuditDetail> getDetails() {
        return details;
    }
}
