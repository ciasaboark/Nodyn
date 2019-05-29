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

package io.phobotic.nodyn_app.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.cache.EmailImageCache;
import io.phobotic.nodyn_app.converter.AuditExcelConverter;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.audit.AuditDatabase;
import io.phobotic.nodyn_app.database.audit.model.Audit;
import io.phobotic.nodyn_app.database.audit.model.AuditDefinition;
import io.phobotic.nodyn_app.database.audit.model.AuditDetailRecord;
import io.phobotic.nodyn_app.database.exception.AssetNotFoundException;
import io.phobotic.nodyn_app.database.exception.AuditDefinitionNotFoundException;
import io.phobotic.nodyn_app.database.exception.ManufacturerNotFoundException;
import io.phobotic.nodyn_app.database.exception.ModelNotFoundException;
import io.phobotic.nodyn_app.database.exception.StatusNotFoundException;
import io.phobotic.nodyn_app.database.exception.UserNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Manufacturer;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.database.model.Status;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.email.Attachment;
import io.phobotic.nodyn_app.email.EmailRecipient;
import io.phobotic.nodyn_app.email.EmailSender;
import io.phobotic.nodyn_app.reporting.CustomEvents;

/**
 * Created by Jonathan Nelson on 1/17/18.
 */

public class AuditEmailService extends IntentService {
    private static final String TAG = AuditEmailService.class.getSimpleName();
    private static final int MODEL_THUMBNAIL_WIDTH = 48;
    private static final int MODEL_THUMBNAIL_HEIGHT = 48;
    private EmailImageCache imageCache;
    private AuditDatabase db;
    private SharedPreferences prefs;

    public AuditEmailService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        db = AuditDatabase.getInstance(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        imageCache = EmailImageCache.getInstance(this);

        // TODO: 1/17/18 let the database handle fetching only the headers that have not been sent
        List<Audit> auditHeaders = db.getAudits();
        Iterator<Audit> it = auditHeaders.iterator();
        while (it.hasNext()) {
            Audit a = it.next();
            if (a.isExtracted()) {
                it.remove();
            }
        }

        List<Audit> unsentAudits = new ArrayList<>();
        for (Audit header : auditHeaders) {
            Audit audit = db.getAudit(header.getId());

            //headers get inserted whenever an audit is started.  We need to make sure than only
            //+ audits with detail records are sent
            if (audit.getDetailRecords() == null || audit.getDetailRecords().isEmpty()) {
                audit.setExtracted(true);
                db.storeAudit(audit);
            } else {
                unsentAudits.add(audit);
            }
        }

        if (!unsentAudits.isEmpty()) {
            sendAuditEmails(unsentAudits);
        }

        pruneDatabase();
    }

    private void sendAuditEmails(List<Audit> unsentAudits) {
        for (Audit audit : unsentAudits) {
            try {
                sendAuditEmail(audit);
            } catch (Exception e) {
                Crashlytics.logException(e);
                Answers.getInstance().logCustom(new CustomEvent(CustomEvents.AUDIT_RESULTS_ERROR_EMAIL_NOT_SENT));
            }
        }
    }

    /**
     * Remove all audit records from the database that have been extracted
     */
    private void pruneDatabase() {
        db.pruneExtractedRecords();
    }

    private void sendAuditEmail(Audit audit) {
        updateModelImageCache(audit);
        List<EmailRecipient> recipients = new ArrayList<>();
        String addressesString = prefs.getString(getString(R.string.pref_key_equipment_managers_addresses),
                getString(R.string.pref_default_equipment_managers_addresses));
        String[] addresses = addressesString.split(",");
        for (String address : addresses) {
            recipients.add(new EmailRecipient(address));
        }

        AuditExcelConverter converter = new AuditExcelConverter(this, audit);
        File file = null;
        try {
            //go ahead and add the audit spreadsheet to the attachments
            file = converter.convert();
            List<Attachment> attachments = new ArrayList<>();
            Attachment attachment = new Attachment(file, "audit_results.xls");
            attachments.add(attachment);

            //build the email body html text
            String emailBody = getEmailBodyText(audit, attachments);

            AuditEmail auditEmail = new AuditEmail()
                    .setAudit(audit)
                    .setFile(file);
            sendEmail(emailBody, recipients, auditEmail, attachments);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Could not generate audit excel file: " + e.getMessage());
            Crashlytics.logException(e);
        }
    }

    private void updateModelImageCache(Audit audit) {
        Database db = Database.getInstance(this);
        List<Model> modelList = new ArrayList<>();
        for (int id : audit.getModelIDs()) {
            try {
                Model m = db.findModelByID(id);
                modelList.add(m);
            } catch (ModelNotFoundException e) {
                // TODO: 2/1/18
            }
        }

        imageCache.updateModelImageCache(modelList);
    }

    private String getEmailBodyText(Audit audit, List<Attachment> attachments) throws IOException {
        String rawHtml = getUnformattedHtml();
        String modelRowsHtml = buildModelRowsHtml(audit, attachments);

        Database db = Database.getInstance(this);
        String user;
        if (audit.getUserID() == -1) {
            user = getString(R.string.user_authentication_not_required);
        } else {
            try {
                User u = db.findUserByID(audit.getUserID());
                user = u.getName();
            } catch (UserNotFoundException e) {
                user = String.format(getString(R.string.unknown_user), audit.getUserID());
            }
        }

        DateFormat df = SimpleDateFormat.getDateTimeInstance();
        String beginDate = df.format(new Date(audit.getBegin()));
        String endDate = df.format(new Date(audit.getEnd()));

        boolean notesIncluded = false;
        for (AuditDetailRecord r : audit.getDetailRecords()) {
            //undamaged and not_audited are the default audit statuses
            boolean customStatus = r.getStatus() != AuditDetailRecord.Status.UNDAMAGED
                    && r.getStatus() != AuditDetailRecord.Status.NOT_AUDITED;

            //filter out the unaudited assets, since they have notes added automatically
            boolean noteAdded = r.getStatus() != AuditDetailRecord.Status.NOT_AUDITED &&
                    r.getNotes() != null && r.getNotes().length() > 0;
            if (customStatus || noteAdded) {
                notesIncluded = true;
                break;
            }
        }

        String notes = "";
        if (notesIncluded) {
            notes = "Auditor notes are included in the attached spreadsheet";
        }

        String appImage = "cid:app_icon_196.png";

        String auditDefinitonName;
        String definitionLastAuditDate = "";
        AuditDatabase auditDatabase = AuditDatabase.getInstance(this);
        if (audit.getDefinedAuditID() == -1) {
            auditDefinitonName = getString(R.string.custom_audit);
        } else {
            try {
                AuditDefinition definition = auditDatabase.findAuditDefinitionByID(audit.getDefinedAuditID());
                auditDefinitonName = definition.getName();

                // TODO: 2/11/18 the definition last audit timestamp will probably refer to this audit.  It would be nice if we could include a timestamp for the most recent audit prior to this one.
            } catch (AuditDefinitionNotFoundException e) {
                auditDefinitonName = String.format(getString(R.string.unknown_audit_definition), audit.getDefinedAuditID());
            }
        }


        StringBuilder statusBuilder = new StringBuilder();
        String prefix = "";
        for (Integer i : audit.getStatusIDs()) {
            try {
                Status s = db.findStatusByID(i);
                statusBuilder.append(prefix + s.getName());
                prefix = ", ";
            } catch (StatusNotFoundException e) {

            }
        }
        String statuses = statusBuilder.toString();

        String bodyText = String.format(rawHtml, appImage, auditDefinitonName, definitionLastAuditDate,
                user, beginDate, endDate, statuses, notes, modelRowsHtml);

        return bodyText;
    }

    private void sendEmail(String body, List<EmailRecipient> recipients, AuditEmail auditEmail, List<Attachment> attachments) {
        addAssetAsAttachment(attachments, "app_icon_96.png");
//        addAssetAsAttachment(attachments, "devices_generic_48.png");

        Log.d(TAG, "Sending audit email for audit id " + auditEmail.audit.getId());

        EmailSender sender = new EmailSender(this)
                .setBody(body)
                .setSubject("Asset Audit")
                .setRecipientList(recipients)
                .withAttachments(attachments)
                .setFailedListener(new EmailSender.EmailStatusListener() {
                    @Override
                    public void onEmailSendResult(@Nullable String message, @Nullable Object tag) {
                        Log.e(TAG, "Audit results send email failed with message: " + message);
                        Answers.getInstance().logCustom(new CustomEvent(CustomEvents.AUDIT_RESULTS_ERROR_EMAIL_NOT_SENT));

                        if (tag instanceof AuditEmail) {
                            //delete the temporary file even if the email was not sent
                            File file = ((AuditEmail) tag).getFile();
                            boolean deleted = file.delete();
                            Log.d(TAG, "Audit temp file " + file.getName() + " was " + (deleted ? "" : "not ") + " deleted");
                        }
                    }
                }, auditEmail)
                .setSuccessListener(new EmailSender.EmailStatusListener() {
                    @Override
                    public void onEmailSendResult(@Nullable String message, @Nullable Object tag) {
                        Log.d(TAG, "Audit results send email succeeded with message: " + message);
                        Answers.getInstance().logCustom(new CustomEvent(CustomEvents.AUDIT_RESULTS_EMAIL_SENT));
                        if (tag instanceof AuditEmail) {
                            Audit audit = ((AuditEmail) tag).getAudit();
                            audit.setExtracted(true);
                            db.storeAudit(audit);

                            //delete the temporary file
                            File file = ((AuditEmail) tag).getFile();
                            boolean deleted = file.delete();
                            Log.d(TAG, "Audit temp file " + file.getName() + " was " + (deleted ? "" : "not ") + " deleted");
                        }
                    }
                }, auditEmail)
                .send();
    }

    private String getUnformattedHtml() throws IOException {
        InputStream is = getAssets().open("audit_results_email.html");
        int size = is.available();

        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();

        String htmlString = new String(buffer);
        return htmlString;
    }

    private String buildModelRowsHtml(Audit audit, List<Attachment> attachments) {
        String modelRowUnformatted =
                "<tr>" +
                        "<td>" +
                        "<div class=\"image\">" +
                        "<img style=\"width: 48px; height: 48px; max-width: 48px; max-height: 48px;\" src=\"%s\"/>" +
                        "</div>" +
                        "</td>" +
                        "<td>%s</td>" +
                        "<td>%s</td>" +
                        "<td>%d</td>" +
                        "<td>%d</td>" +
                        "<td>%d</td>" +
                        "</tr>";
        StringBuilder sb = new StringBuilder();

        List<Integer> modelIDs = audit.getModelIDs();
        if (modelIDs == null) modelIDs = new ArrayList<>();

        Map<Integer, Integer> modelTotalCount = new HashMap<>();
        Map<Integer, Integer> modelFoundCount = new HashMap<>();
        Map<Integer, Integer> modelMissingCount = new HashMap<>();

        Database db = Database.getInstance(this);
        List<AuditDetailRecord> details = audit.getDetailRecords();

        if (details != null) {
            for (AuditDetailRecord record : details) {
                int assetID = record.getAssetID();
                try {
                    Asset asset = db.findAssetByID(assetID);
                    int modelID = asset.getModelID();
                    Integer total = modelTotalCount.get(modelID);
                    if (total == null) total = 0;
                    modelTotalCount.put(modelID, ++total);

                    if (record.getStatus() == AuditDetailRecord.Status.NOT_AUDITED) {
                        Integer missing = modelMissingCount.get(modelID);
                        if (missing == null) missing = 0;
                        modelMissingCount.put(modelID, ++missing);
                    } else {
                        Integer found = modelFoundCount.get(modelID);
                        if (found == null) found = 0;
                        modelFoundCount.put(modelID, ++found);
                    }
                } catch (AssetNotFoundException e) {
                    Crashlytics.logException(e);
                }
            }
        }

        for (Integer modelID : modelIDs) {
            try {
                Model model = db.findModelByID(modelID);
                int manufacturerID = model.getManufacturerID();
                try {
                    Manufacturer manufacturer = db.findManufacturerByID(manufacturerID);
                    Integer total = modelTotalCount.get(modelID);
                    if (total == null) total = 0;

                    Integer found = modelFoundCount.get(modelID);
                    if (found == null) found = 0;

                    Integer missing = modelMissingCount.get(modelID);
                    if (missing == null) missing = 0;

                    //if we were able to build a cached thumbnail for this model then use it,
                    //+ otherwise fallback to an empty string
                    // TODO: 2/3/18 the imagecache should probably not default to using a fallback image.  Let that be handled somewhere else
                    String modelImageSrc = imageCache.getCachedImage(String.valueOf(model.getId()));
                    if (modelImageSrc == null) {
                        modelImageSrc = "";
                    } else {
                        //add this image as an attachment
                        addCachedFileAsAttachment(attachments, modelImageSrc, imageCache);
                        modelImageSrc = "cid:" + modelImageSrc;
                    }

                    String rowHtml = String.format(modelRowUnformatted, modelImageSrc,
                            manufacturer.getName(), model.getName(), total, found, missing);
                    sb.append(rowHtml);
                } catch (ManufacturerNotFoundException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Unable to find manufacturer with ID " + manufacturerID + ", was this model" +
                            "deleted after audit was completed?");
                    Crashlytics.logException(e);
                }

            } catch (ModelNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "Unable to find model with ID " + modelID + ", was this model" +
                        "deleted after audit was completed?");
                Crashlytics.logException(e);
            }
        }

        return sb.toString();
    }

    private void addAssetAsAttachment(List<Attachment> attachments, String assetName) {
        File f = new File(String.format("%s/%s", getCacheDir(), assetName));
        if (!f.exists()) try {

            InputStream is = getAssets().open(assetName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();


            FileOutputStream fos = new FileOutputStream(f);
            fos.write(buffer);
            fos.close();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        Attachment attachment = new Attachment(f, assetName, assetName);
        attachment.setInline(true);
        attachments.add(attachment);
    }

    private void addCachedFileAsAttachment(List<Attachment> attachments, String filename,
                                           EmailImageCache cache) {
        File f = cache.getFileForFilename(filename);
        Attachment attachment = new Attachment(f, filename + ".png", filename);
        attachment.setInline(true);
        attachment.setContentType("image/png");
        attachments.add(attachment);
    }


    private class AuditEmail {
        private Audit audit;
        private File file;

        public Audit getAudit() {
            return audit;
        }

        public AuditEmail setAudit(Audit audit) {
            this.audit = audit;
            return this;
        }

        public File getFile() {
            return file;
        }

        public AuditEmail setFile(File file) {
            this.file = file;
            return this;
        }
    }

}
