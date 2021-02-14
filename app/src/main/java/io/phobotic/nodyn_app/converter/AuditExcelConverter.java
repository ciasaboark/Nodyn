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

package io.phobotic.nodyn_app.converter;

import android.content.Context;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.audit.model.Audit;
import io.phobotic.nodyn_app.database.audit.model.AuditHeader;
import io.phobotic.nodyn_app.database.audit.model.AuditDetail;
import io.phobotic.nodyn_app.database.exception.AssetNotFoundException;
import io.phobotic.nodyn_app.database.exception.ManufacturerNotFoundException;
import io.phobotic.nodyn_app.database.exception.ModelNotFoundException;
import io.phobotic.nodyn_app.database.exception.StatusNotFoundException;
import io.phobotic.nodyn_app.database.exception.UserNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Manufacturer;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.database.model.Status;
import io.phobotic.nodyn_app.database.model.User;

/**
 * Created by Jonathan Nelson on 1/17/18.
 */

public class AuditExcelConverter {
    private static final String FILE_NAME_UNFORMATTED = "audit-%s-%s.xls";
    private static final String TAG = AuditExcelConverter.class.getSimpleName();
    private static final short borderStyle = HSSFCellStyle.BORDER_THIN;
    private static final String[] FIELDS = {
            "Manufacturer",
            "Model",
            "Asset Tag",
            "Serial No",
            "Asset Status",
            "Timestamp",
            "Audit Results",
            "Currently Assigned to",
            "Notes"
    };
    private final Context context;
    private final Database db;
    private Audit audit;
    private CellStyle headerStyle;
    private CellStyle recordStyle;
    private CellStyle missedAuditStyle;
    private Workbook wb;
    private Sheet sheet;
    private int[] columnMaxCharacters = new int[FIELDS.length];

    private Map<String, Integer> locationLookups;

    public AuditExcelConverter(Context context, Audit audit) {
        this.context = context;
        this.audit = audit;
        this.db = Database.getInstance(context);
        init();
    }

    private void init() {
        wb = new HSSFWorkbook();
        sheet = wb.createSheet("Location Lookups");
        initStyles();
    }

    private void initStyles() {
        Font headerFont = wb.createFont();
        headerFont.setFontHeightInPoints((short) 11);
        headerFont.setColor(IndexedColors.BLACK.getIndex());
        headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

        headerStyle = wb.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        headerStyle.setAlignment(CellStyle.ALIGN_CENTER);
        headerStyle.setFont(headerFont);
        headerStyle.setBorderBottom(borderStyle);
        headerStyle.setBorderTop(borderStyle);
        headerStyle.setBorderRight(borderStyle);
        headerStyle.setBorderLeft(borderStyle);
        headerStyle.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.index);
        headerStyle.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.index);
        headerStyle.setRightBorderColor(IndexedColors.GREY_40_PERCENT.index);
        headerStyle.setTopBorderColor(IndexedColors.GREY_40_PERCENT.index);


        recordStyle = wb.createCellStyle();
        recordStyle.setBorderBottom(borderStyle);
        recordStyle.setBorderTop(borderStyle);
        recordStyle.setBorderRight(borderStyle);
        recordStyle.setBorderLeft(borderStyle);
        recordStyle.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.index);
        recordStyle.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.index);
        recordStyle.setRightBorderColor(IndexedColors.GREY_40_PERCENT.index);
        recordStyle.setTopBorderColor(IndexedColors.GREY_40_PERCENT.index);

        missedAuditStyle = wb.createCellStyle();
        missedAuditStyle.setBorderBottom(borderStyle);
        missedAuditStyle.setBorderTop(borderStyle);
        missedAuditStyle.setBorderRight(borderStyle);
        missedAuditStyle.setBorderLeft(borderStyle);
        missedAuditStyle.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.index);
        missedAuditStyle.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.index);
        missedAuditStyle.setRightBorderColor(IndexedColors.GREY_40_PERCENT.index);
        missedAuditStyle.setTopBorderColor(IndexedColors.GREY_40_PERCENT.index);
        missedAuditStyle.setFillForegroundColor(IndexedColors.CORAL.index);
        missedAuditStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
    }

    public File convert() throws IOException {
        File file = null;

        writeHeaders();
        writeRows();
        resizeColumns();

        String username = "unknown";
        try {
            User user = db.findUserByID(audit.getHeader().getUserID());
            username = user.getName();
        } catch (UserNotFoundException e) {

        }

        String timestamp = String.valueOf(audit.getHeader().getBegin());
        String filename = String.format(FILE_NAME_UNFORMATTED, username, timestamp);

        File dir = context.getFilesDir();

        file = new File(dir, "/excel/" + filename);
        try {
            File parent = file.getParentFile();
            parent.mkdirs();
            FileOutputStream fos = new FileOutputStream(file);
            wb.write(fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }

        return file;
    }

    private void writeHeaders() {
        Row row = sheet.createRow(0);
        for (int col = 0; col < FIELDS.length; col++) {
            Cell cell = row.createCell(col);
            cell.setCellStyle(headerStyle);
            cell.setCellValue(FIELDS[col]);

            updateCellMaxLength(col, cell.getStringCellValue());
        }

    }

    private void writeRows() {
        int rowNum = 1;
        List<AuditDetail> records = sortRecords();

        for (AuditDetail record : records) {
            Row row = sheet.createRow(rowNum);
            writeRecordRow(row, record);
            rowNum++;
        }
    }

    private void resizeColumns() {
        for (int column = 0; column < FIELDS.length; column++) {
            int columnChars = columnMaxCharacters[column];
            int width = columnChars * 250;  //serious magic number time.  Who knows why this value works?
            if (width != 0) {
                sheet.setColumnWidth(column, width);
            }
        }
    }

    private void updateCellMaxLength(int column, String value) {
        if (value == null) return;

        int maxLength = columnMaxCharacters[column];
        int strLength = value.length();
        if (strLength > maxLength) {
            columnMaxCharacters[column] = value.length();
        }
    }

    private List<AuditDetail> sortRecords() {
        List<AuditDetail> records = audit.getDetails();
        if (records == null) records = new ArrayList<>();

        Collections.sort(records, new Comparator<AuditDetail>() {
            @Override
            public int compare(AuditDetail r1, AuditDetail r2) {
                return ((Long) r1.getTimestamp()).compareTo(r2.getTimestamp());
            }
        });

        return records;
    }

    private void writeRecordRow(Row row, AuditDetail record) {
        if (record.getStatus().equals(AuditDetail.Status.NOT_AUDITED)) {
            writeRecord(row, record, missedAuditStyle);
        } else {
            writeRecord(row, record, recordStyle);
        }
    }

    private void writeRecord(Row row, AuditDetail record, CellStyle style) {
        Asset asset = null;
        try {
            asset = db.findAssetByID(record.getAssetID());
        } catch (AssetNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "unable to find asset with ID " + record.getAssetID() + ".  Was this asset deleted from backend since audit was performed");
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        for (int col = 0; col < FIELDS.length; col++) {
            String model = "Unknown";
            String manufacturer = "Unknown";
            String status = "Unknown";

            try {
                Model mod = db.findModelByID(asset.getModelID());
                model = mod.getName();
                Manufacturer man = db.findManufacturerByID(mod.getManufacturerID());
                manufacturer = man.getName();
                Status s = db.findStatusByID(asset.getStatusID());
                status = s.getName();
            } catch (ModelNotFoundException | ManufacturerNotFoundException |
                    StatusNotFoundException e) {
                Log.e(TAG, "Unable to find asset model, manufacturer, or status");
            }

            Cell cell = row.createCell(col);
            String cellValue = "";
            switch (col) {
                case 0:
                    cellValue = manufacturer;
                    break;
                case 1:
                    cellValue = model;
                    break;
                case 2:
                    String assetName = "";
                    if (asset != null) {
                        assetName = asset.getTag();
                    }
                    cellValue = assetName;
                    break;
                case 3:
                    cellValue = asset.getSerial();
                    break;
                case 4:
                    cellValue = status;
                    break;
                case 5:
                    DateFormat df = SimpleDateFormat.getDateTimeInstance();
                    Date d = new Date(record.getTimestamp());
                    String dateString = df.format(d);
                    cellValue = dateString;
                    break;
                case 6:
                    String auditResult = "UNKNOWN";
                    if (record.getStatus() != null) {
                        auditResult = record.getStatus().toString();
                    }
                    cellValue = auditResult;
                    break;
                case 7:
                    String assignedTo = "";
                    int userID = asset.getAssignedToID();
                    if (userID != -1) {
                        try {
                            Database db = Database.getInstance(context);
                            User u = db.findUserByID(userID);
                            assignedTo = u.getName();
                        } catch (UserNotFoundException e) {
                            assignedTo = "Unknown user";
                        }
                    }
                    break;
                case 8:
                    cellValue = record.getNotes() == null ? "" : record.getNotes();
                    break;
            }
            cell.setCellValue(cellValue);
            cell.setCellStyle(style);

            updateCellMaxLength(col, cellValue);
        }
    }
}