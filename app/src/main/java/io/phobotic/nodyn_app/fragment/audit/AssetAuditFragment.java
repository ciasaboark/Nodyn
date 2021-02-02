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

package io.phobotic.nodyn_app.fragment.audit;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.audit.AuditDatabase;
import io.phobotic.nodyn_app.database.audit.model.Audit;
import io.phobotic.nodyn_app.database.audit.model.AuditHeader;
import io.phobotic.nodyn_app.database.audit.model.AuditDefinition;
import io.phobotic.nodyn_app.database.audit.model.AuditDetail;
import io.phobotic.nodyn_app.database.exception.AssetNotFoundException;
import io.phobotic.nodyn_app.database.exception.ModelNotFoundException;
import io.phobotic.nodyn_app.database.exception.StatusNotFoundException;
import io.phobotic.nodyn_app.database.exception.UserNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.database.model.Status;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.helper.GridColumnHelper;
import io.phobotic.nodyn_app.helper.MediaHelper;
import io.phobotic.nodyn_app.helper.TextHelper;
import io.phobotic.nodyn_app.list.adapter.AuditedAssetRecyclerViewAdapter;
import io.phobotic.nodyn_app.list.adapter.UnauditedAssetRecyclerViewAdapter;
import io.phobotic.nodyn_app.view.ScanInputView;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class AssetAuditFragment extends Fragment implements DialogInterface.OnDismissListener {
    private static final String TAG = AssetAuditFragment.class.getSimpleName();
    private static final String ARG_AUDIT = "audit";
    private static final String ARG_USER = "user";
    private static final String ARG_AUDIT_DEFINITION = "audit_definition";
    private static final String AUDIT_HELP_SHOWCASE_ID_PREFIX = "audit_showcase_prefix-";
    private static final String ITEM_HELP_SHOWCASE_ID_PREFIX = "item_showcase_prefix-";
    private static final int MIN_CARD_HEIGHT = 200;
    private static final int MAX_CARD_HEIGHT = 900;
    private String showcaseID;
    private View rootView;
    private AuditDefinition auditDefinition;
    private Audit audit;
    private User user;
    private List<Asset> unscannedAssets;
    private List<AuditDetail> detailRecords = new ArrayList<>();
    private RecyclerView auditedAssetsRecyclerView;
    private RecyclerView unscannedAssetsRecyclerView;
    private View unscannedAssetsWrapper;
    private long auditStart = System.currentTimeMillis();
    private AuditStatusListener listener;
    private View error;
    private View listHolder;
    private boolean listExpaned = false;
    //    private View headerDetails;
//    private TextView detailIntro;
//    private TextView detailModels;
//    private TextView detailStatuses;
    private TextView title;
    //    private TextView detailBlind;
    private ExtendedFloatingActionButton submitButton;
    private ScanInputView input;
    private View expandListWrapper;
    private boolean hasShownFirstCardShowcase = false;
    private View spacer;
    private View helpButton;

    public static AssetAuditFragment newInstance(AuditDefinition auditDefinition, AuditHeader audit, User user) {
        AssetAuditFragment f = new AssetAuditFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_AUDIT_DEFINITION, auditDefinition);
        bundle.putSerializable(ARG_AUDIT, audit);
        bundle.putSerializable(ARG_USER, user);
        f.setArguments(bundle);
        return f;
    }

    public AssetAuditFragment() {
        // Required empty public constructor
    }

    public AssetAuditFragment setListener(AuditStatusListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            auditDefinition = (AuditDefinition) getArguments().getSerializable(ARG_AUDIT_DEFINITION);
            AuditHeader header = (AuditHeader) getArguments().getSerializable(ARG_AUDIT);
            audit = new Audit(header);
            user = (User) getArguments().getSerializable(ARG_USER);
        }

        //we want the showcase overlay to display once for each user that runs an audit.  So long
        //+ as we were passed a user we can use that user ID as the showcaseID
        if (user != null) {
            showcaseID = AUDIT_HELP_SHOWCASE_ID_PREFIX + "user-" + user.getId();
        } else {
            //if we were not provided a user then we will have to use the audit definition ID
            showcaseID = AUDIT_HELP_SHOWCASE_ID_PREFIX + "audit-" + auditDefinition.getId();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_asset_audit, container, false);
        setHasOptionsMenu(true);
        init();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_audit, menu);
        MenuItem i = menu.findItem(R.id.action_help);
        helpButton = i.getActionView();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean consumed = false;

        Log.d(TAG, item.toString());
        switch (item.getItemId()) {
            case R.id.action_help:
                showHelp();
                consumed = true;
                break;
        }

        return consumed;
    }

    private void init() {
        findViews();
        initButtons();
        initInput();
        initDetails();

        buildUnscannedAssetList();
        initLists();
        hideUnscannedAssetsIfNeeded();
        initDragHandle();
        final ViewTreeObserver observer = unscannedAssetsWrapper.getViewTreeObserver();

        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                showFab();
                try {
                    unscannedAssetsWrapper.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } catch (Exception e) {
                    Log.d(TAG, "Unable to remove layout listener");
                }
            }
        });
    }

    private void animateUnscannedCard() {
        if (!auditDefinition.isBlindAudit()) {
            int startHeight = unscannedAssetsWrapper.getHeight();
            int endHeight = (int) (startHeight * 1.3);
            ValueAnimator anim = ValueAnimator.ofInt(startHeight, endHeight, startHeight);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    Integer value = (Integer) animation.getAnimatedValue();
                    unscannedAssetsWrapper.getLayoutParams().height = value.intValue();
                    unscannedAssetsWrapper.requestLayout();
                }
            });

            anim.setDuration(2000);
            anim.setRepeatCount(1);
            anim.setStartDelay(1000);
            anim.setInterpolator(new BounceInterpolator());
            anim.start();
        } else {
            showFab();
        }
    }

    private void showFab() {
        submitButton.setVisibility(View.VISIBLE);
        submitButton.show();
        showShowcaseIfNeeded();
    }

    private void initDragHandle() {
        expandListWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ViewGroup.LayoutParams params = unscannedAssetsWrapper.getLayoutParams();
                int fromHeight = params.height;
                int toHeight;
                if (listExpaned) {
                    toHeight = 100;
                } else {
                    toHeight = 800;
                }

                listExpaned = !listExpaned;

                ValueAnimator animator = ValueAnimator.ofInt(fromHeight, toHeight);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int h = (int) animation.getAnimatedValue();
                        params.height = h;
                        unscannedAssetsWrapper.setLayoutParams(params);
                    }
                });
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ImageView iv = rootView.findViewById(R.id.chevron);
                        if (listExpaned) {
                            iv.setImageDrawable(getContext().getResources().getDrawable(R.drawable.chevron_down));
                        } else {
                            iv.setImageDrawable(getContext().getResources().getDrawable(R.drawable.chevron_up));
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                animator.setDuration(300);
                animator.setInterpolator(new AccelerateInterpolator());
                animator.start();

            }
        });
    }

    private void showShowcaseIfNeeded() {
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), showcaseID);

        sequence.setConfig(config);

//        sequence.addSequenceItem(null, "Asset audit", "Welcome to the asset audit", "Next");

        int seq = -1;
        boolean animateCard = false;
        if (!auditDefinition.isBlindAudit()) {
            animateCard = true;
            String remainingText = getString(R.string.audit_showcase_unscaned);
            sequence.addSequenceItem(unscannedAssetsRecyclerView, remainingText, getString(R.string.next).toUpperCase());
            seq++;
        }

        String submitText = getString(R.string.audit_showcase_submit);
        sequence.addSequenceItem(submitButton, submitText, getString(R.string.next).toUpperCase());
        seq++;


        //the help button may be hidden within the overflow menu
        if (helpButton != null) {
            String helpText = getString(R.string.audit_showcase_help);
            sequence.addSequenceItem(helpButton, helpText, getString(R.string.got_it).toUpperCase());
            seq++;
        }


        final boolean finalAnimateCard = animateCard;
        sequence.setOnItemShownListener(new MaterialShowcaseSequence.OnSequenceItemShownListener() {
            @Override
            public void onShow(MaterialShowcaseView materialShowcaseView, int i) {
                if (i == 0 && finalAnimateCard) {
                    animateUnscannedCard();
                }
            }
        });

        sequence.start();
    }

    private void showHelp() {
        ConstraintLayout v = (ConstraintLayout) getLayoutInflater().inflate(R.layout.dialog_audit_help, null);
        TextView detailIntro = v.findViewById(R.id.details_intro);
        TextView detailModels = v.findViewById(R.id.details_models);
        TextView detailStatuses = v.findViewById(R.id.details_statuses);
        TextView detailBlind = v.findViewById(R.id.details_blind);
        TextView manualLink = v.findViewById(R.id.manual_link);
        manualLink.setMovementMethod(LinkMovementMethod.getInstance());

        initIntroDescription(detailIntro);
        initModelsDescription(detailModels);
        initStatusesDescription(detailStatuses);
        initBlindDescription(detailBlind);

        AlertDialog d = new MaterialAlertDialogBuilder(getContext(), R.style.Widgets_Dialog)
                .setTitle("Audit Help")
                .setView(v)
                .setIcon(R.drawable.ic_help_circle_outline_white_36dp)
                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        d.show();
    }

    private void initButtons() {
        //submit FAB will be show later
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmSubmitAuditResults();
            }
        });
        submitButton.setVisibility(View.GONE);
        submitButton.hide();
    }

    private void initIntroDescription(TextView tv) {
        String intro;
        if (auditDefinition.getId() == -1) {
            intro = getString(R.string.audit_detail_description_custom);
        } else {
            intro = getString(R.string.audit_detail_description_definition);
            DateFormat df = DateFormat.getDateInstance();

            //add in the detail line for the date this audit was created.
            Date createdDate = new Date(auditDefinition.getCreateTimestamp());
            String createdDateString = df.format(createdDate);

            //add in the last time this audit has been completed
            String lastAuditString = null;
            if (auditDefinition.getLastAuditTimestamp() == -1) {
                //this definition has never been used to complete an audit
                lastAuditString = getString(R.string.audit_detail_description_definition_no_previous_audit);
            } else {
                Date lastAuditDate = new Date(auditDefinition.getLastAuditTimestamp());
                DateFormat dtf = DateFormat.getDateTimeInstance();
                String lastAuditDateString = dtf.format(lastAuditDate);
                lastAuditString = String.format(getString(R.string.audit_detail_description_definition_previous_audit_date), lastAuditDateString);
            }

            intro = String.format(intro, createdDateString, lastAuditString);
        }

        tv.setText(intro);
    }

    private void initDetails() {
        initTitle();
    }

    private void addShowListener(final AlertDialog d) {
        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                final Button positiveButton = d.getButton(DialogInterface.BUTTON_POSITIVE);
                final Button negativeButton = d.getButton(DialogInterface.BUTTON_NEGATIVE);
                final Button neutralButton = d.getButton(DialogInterface.BUTTON_NEUTRAL);
                addPostDelay(positiveButton);
                addPostDelay(negativeButton);
                addPostDelay(neutralButton);
            }
        });
    }

    private void addPostDelay(final Button b) {
        if (b != null) {
            b.setEnabled(false);
            Handler h = new Handler();
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (b != null) {
                        b.setEnabled(true);
                    }
                }
            };
            h.postDelayed(r, 500);
        }
    }

    private void buildUnscannedAssetList() {
        Database db = Database.getInstance(getContext());

        //get a list of all assets then filter out the models that were not selected
        unscannedAssets = db.getAssets();

        filterOutModels();
        filterMetaStatus();
        removeUnselectedStatuses();

        //sort the list by make, then model, then tag
        Collections.sort(unscannedAssets, new Comparator<Asset>() {
            @Override
            public int compare(Asset o1, Asset o2) {
                int c = ((Integer) o1.getManufacturerID()).compareTo(o2.getManufacturerID());
                if (c == 0) {
                    c = ((Integer) o1.getModelID()).compareTo(o2.getModelID());
                }

                if (c == 0) {
                    c = o1.getTag().compareTo(o2.getTag());
                }

                return c;
            }
        });
    }

    private void removeUnselectedStatuses() {
        //remove assets that do not match the asset status requirements
        List<Integer> allowedStatusIDs = audit.getHeader().getStatusIDs();
        Iterator<Asset> it = unscannedAssets.iterator();
        while (it.hasNext()) {
            Asset a = it.next();
            if (allowedStatusIDs.contains(a.getStatusID())) {
                Log.d(TAG, "Allowing asset status id " + a.getStatusID());
            } else {
                Log.d(TAG, "Removing asset with status id " + a.getStatusID());
                it.remove();
            }
        }
    }

    private void filterMetaStatus() {
        //filter the asset list by the meta status (all, assigned, unassigned)
        String metaStatus = auditDefinition.getMetaStatus();
        if (metaStatus != null) {
            switch (metaStatus) {
                case "ALL":
                    //don't filter out any assets
                    break;
                case "ASSIGNED":
                    applyAssignedFilter();
                    break;
                case "UNASSIGNED":
                    applyUnassignedFilter();
                    break;
            }
        }
    }

    /**
     * Remove all assets from the unscanned assets list that are not assigned to a user
     */
    private void applyAssignedFilter() {
        Iterator<Asset> it = unscannedAssets.iterator();
        while (it.hasNext()) {
            Asset a = it.next();
            if (a.getAssignedToID() == -1) {
                it.remove();
            }
        }
    }

    /**
     * Remove all assets from the unscanned assets list that are assigned to a user
     */
    private void applyUnassignedFilter() {
        Iterator<Asset> it = unscannedAssets.iterator();
        while (it.hasNext()) {
            Asset a = it.next();
            if (a.getAssignedToID() != -1) {
                it.remove();
            }
        }
    }

    /**
     * Remove all assets from the unscanned assets list if they are not one of the selected
     * asset models
     */
    private void filterOutModels() {
        Iterator<Asset> it = unscannedAssets.iterator();
        while (it.hasNext()) {
            Asset a = it.next();
            boolean removeAsset = true;
            int modelID = a.getModelID();
            if (audit.getHeader().getModelIDs().contains(modelID)) {
                removeAsset = false;
            }

            if (removeAsset) {
                it.remove();
            }
        }
    }

    private void initLists() {
        auditedAssetsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        GridColumnHelper gridColumnHelper = new GridColumnHelper(getContext(), R.layout.view_unaudited_asset);
        unscannedAssetsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), gridColumnHelper.calculateNoOfColumns()));
        updateLists();

    }

    private void hideUnscannedAssetsIfNeeded() {
        if (auditDefinition.isBlindAudit()) {
            unscannedAssetsWrapper.setVisibility(View.GONE);
            spacer.setVisibility(View.GONE);
        }

    }

    private void initModelsDescription(TextView tv) {
        String modelsText;

        if (auditDefinition.isAuditAllModels()) {
            modelsText = getString(R.string.audit_detail_models_all);
        } else {
            modelsText = getString(R.string.audit_detail_models_list);
        }

        String modelListString = getModelsListString();

        modelsText = String.format(modelsText, modelListString);
        tv.setText(Html.fromHtml(modelsText));
    }

    private void initStatusesDescription(TextView tv) {
        String statusText = null;
        if (auditDefinition.isAuditAllStatuses()) {
            statusText = getString(R.string.audit_detail_statuses_all);
        } else {
            Database db = Database.getInstance(getContext());
            StringBuilder sb = new StringBuilder();
            for (Integer i : auditDefinition.getRequiredStatusIDs()) {
                try {
                    Status s = db.findStatusByID(i);
                    sb.append("&#8226; ");
                    sb.append(s.getName());
                    sb.append("<br/>");
                } catch (StatusNotFoundException e) {
                    // TODO: 2/4/18
                }
            }

            statusText = getString(R.string.audit_detail_statuses_list);
            statusText = String.format(statusText, sb.toString());
        }

        tv.setText(Html.fromHtml(statusText));
    }

    private void animateDownUnscannedCard() {
        int height = unscannedAssetsWrapper.getHeight();
        if (height > MIN_CARD_HEIGHT) {
            ValueAnimator anim = ValueAnimator.ofInt(height, MIN_CARD_HEIGHT);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    Integer value = (Integer) animation.getAnimatedValue();
                    unscannedAssetsWrapper.getLayoutParams().height = value.intValue();
                    unscannedAssetsWrapper.requestLayout();
                }
            });

            anim.setDuration(500);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            anim.start();
        }
    }

    private void initBlindDescription(TextView tv) {
        String blindText;
        if (auditDefinition.isBlindAudit()) {
            blindText = getString(R.string.audit_detail_description_blind_on);
        } else {
            blindText = getString(R.string.audit_detail_description_blind_off);
        }

        TextHelper.setTextOrHide(tv, tv, blindText);
    }

    private void findViews() {
        title = rootView.findViewById(R.id.title);

        auditedAssetsRecyclerView = rootView.findViewById(R.id.audited_assets_list);
        unscannedAssetsRecyclerView = rootView.findViewById(R.id.unscanned_assets_list);
        unscannedAssetsWrapper = rootView.findViewById(R.id.unscanned_assets_wrapper);
        error = rootView.findViewById(R.id.error);
        listHolder = rootView.findViewById(R.id.list_holder);
        submitButton = rootView.findViewById(R.id.submit_button);
        input = rootView.findViewById(R.id.input);
        expandListWrapper = rootView.findViewById(R.id.drag_handle_wrapper);
        spacer = rootView.findViewById(R.id.spacer);
    }

    /**
     * Return true if the asset was filtered out because of the meta status filter.
     *
     * @param a
     * @return
     */
    private boolean isAssetIncludedInMetaStatusFilter(Asset a) {
        boolean assetFiltered = false;
        switch (auditDefinition.getMetaStatus()) {
            case "ALL":
                break;
            case "ASSIGNED":
                if (a.getAssignedToID() == -1) assetFiltered = true;
                break;
            case "UNASSIGNED":
                if (a.getAssignedToID() != -1) assetFiltered = true;
                break;
        }

        return assetFiltered;
    }

    private void initInput() {
        input.setListener(new ScanInputView.OnTextInputListener() {
            @Override
            public void onTextInputFinished(String text) {
                handleInput(text);
            }

            @Override
            public void onTextInputBegin() {
                //nothing to do here
            }
        });
    }

    private String getUnexpectedMetaStatusReasonText(Asset a) {
        String expected = "";
        switch (auditDefinition.getMetaStatus()) {
            case "ASSIGNED":
                expected = getString(R.string.audit_meta_status_assigned);
                break;
            case "UNASSIGNED":
                expected = getString(R.string.audit_meta_status_unassigned);
                break;
            default:
                String err = String.format("Only expected statuses of 'ASSIGNED' or 'UNASSIGNED', " +
                        "found status of %s", auditDefinition.getMetaStatus());
                Log.d(TAG, err);
                FirebaseCrashlytics.getInstance().recordException(new Exception(err));
        }

        String reason = "";
        if (a.getAssignedToID() == -1) {
            reason = getString(R.string.audit_meta_status_unassigned);
        } else {
            reason = getString(R.string.audit_meta_status_assigned_to_user);
            Database db = Database.getInstance(getContext());
            try {
                User u = db.findUserByID(a.getAssignedToID());
                reason = String.format(reason, u.getName());
            } catch (UserNotFoundException e) {
                reason = String.format(reason, "unknown user");
            }
        }

        String line = getString(R.string.audit_unexpected_status_meta_status);
        line = String.format(line, expected, reason);

        return line;
    }

    private String getUnexpectedStatusReasonText(Asset a) {
        String expectedStatusesString = getStatusesString();
        String statusText = getString(R.string.audit_unexpected_status_statuses);
        String assetStatus = "UNKNOWN";
        try {
            Database db = Database.getInstance(getContext());
            Status s = db.findStatusByID(a.getStatusID());
            assetStatus = s.getName();
        } catch (StatusNotFoundException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        statusText = String.format(statusText, assetStatus, expectedStatusesString);
        return statusText;
    }

    private String getStatusesString() {
        StringBuilder sb = new StringBuilder("[");
        String prefix = "";
        Database db = Database.getInstance(getContext());
        for (Integer id : audit.getHeader().getStatusIDs()) {
            try {
                Status s = db.findStatusByID(id);
                sb.append(prefix);
                sb.append(s.getName());
                prefix = ", ";
            } catch (StatusNotFoundException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }

        sb.append("]");
        return sb.toString();
    }

    private void confirmSubmitAuditResults() {
        View v = null;
        boolean requirePause = false;

        if (unscannedAssets.isEmpty()) {
            v = View.inflate(getContext(), R.layout.view_audit_submit_dialog, null);
        } else if (detailRecords.isEmpty()) {
            v = View.inflate(getContext(), R.layout.view_audit_submit_empty_dialog, null);
            requirePause = true;
        } else {
            v = View.inflate(getContext(), R.layout.view_audit_submit_unfinished_dialog, null);
            requirePause = true;
        }

        final AlertDialog d = new MaterialAlertDialogBuilder(getContext(), R.style.Widgets_Dialog)
                .setView(v)
                .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        submitAuditResults();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing to do here
                    }
                })
                .create();
        final TextView waitTextView = v.findViewById(R.id.wait);

        if (requirePause) {
            d.setOnShowListener(new DialogInterface.OnShowListener() {
                final long WAIT_MS = 7000;

                @Override
                public void onShow(DialogInterface dialog) {
                    final Button b = d.getButton(DialogInterface.BUTTON_POSITIVE);
                    b.setEnabled(false);
                    if (waitTextView != null) {
                        setWaitText(WAIT_MS);
                    }

                    CountDownTimer timer = new CountDownTimer(WAIT_MS, 300) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            if (waitTextView != null) {
                                setWaitText(millisUntilFinished);
                            }
                        }

                        @Override
                        public void onFinish() {
                            if (b != null) b.setEnabled(true);
                            if (waitTextView != null) {
                                waitTextView.setVisibility(View.INVISIBLE);
                            }
                        }
                    };
                    timer.start();
                }

                private void setWaitText(long millisUntilFinished) {
                    String waitText = getString(R.string.please_wait_timer);
                    int seconds = Math.round((float) millisUntilFinished / 1000);
                    waitText = String.format(waitText, seconds);
                    waitTextView.setText(waitText);
                }
            });
        }
        d.show();
    }

    private void initTitle() {
        String titleString = getString(R.string.audit_detail_title);
        if (auditDefinition.getName() != null) {
            titleString = auditDefinition.getName();
        }

        title.setText(titleString);
    }

    private void handleInput(String text) {
        Database db = Database.getInstance(getContext());
        try {
            final Asset a = db.findAssetByTag(text);

            //if the asset scanned was not one of the models we were looking for then show an error
            if (detailRecordsContains(a)) {
                //if we have already scanned that asset then show an error
                showAlreadyScannedDialog();
            } else if (!unscannedAssets.contains(a)) {

                //if the asset is one of the models we were looking for we can give the auditor the option to add it to the audit
                if (auditDefinition.getRequiredModelIDs().contains(a.getModelID()) || auditDefinition.isAuditAllModels()) {
                    MediaHelper.playSoundEffect(getContext(), R.raw.original_sound__error_bleep_5);
                    showUnexpectedAssetDialog(a);
                } else {
                    //otherwise just notify the auditor that an unexpected asset was scanned
                    showUnwantedAssetDialog();
                }
            } else {
                //otherwise shift the assets to the other list

                //to prevent having to use notifyDatasetChanged we need to get the index of the asset
                //+ that was removed
                int index = -1;
                for (int i = 0; i < unscannedAssets.size(); i++) {
                    Asset tempAsset = unscannedAssets.get(i);
                    if (tempAsset.equals(a)) {
                        unscannedAssets.remove(i);
                        index = i;
                        break;
                    }
                }
                unscannedAssetsRecyclerView.getAdapter().notifyItemRemoved(index);

                animateDownUnscannedCard();

                addAuditedAsset(a, AuditDetail.Status.UNDAMAGED);
            }

        } catch (AssetNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "Unable to find asset for scanned input string: '" + text + "'");
            FirebaseCrashlytics.getInstance().recordException(e);

            AlertDialog d = new MaterialAlertDialogBuilder(getContext(), R.style.Widgets_Dialog)
                    .setTitle(getResources().getString(R.string.asset_scan_list_unknown_asset_title))
                    .setView(R.layout.view_unknown_asset)
                    .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .create();
            d.setOnDismissListener(this);
            addShowListener(d);

            MediaHelper.playSoundEffect(getContext(), R.raw.original_sound__error_bleep_5);
            d.show();
        }
    }

    private void showAlreadyScannedDialog() {
        final AlertDialog d = new MaterialAlertDialogBuilder(getContext(), R.style.Widgets_Dialog)
                .setTitle("Asset already scanned")
                .setMessage("This asset has already been scanned")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing to do here

                    }
                }).create();
        d.setOnDismissListener(this);
        addShowListener(d);

        MediaHelper.playSoundEffect(getContext(), R.raw.original_sound__error_bleep_5);
        d.show();
    }

    private void showUnwantedAssetDialog() {
        AlertDialog d = new MaterialAlertDialogBuilder(getContext(), R.style.Widgets_Dialog)
                .setTitle("Unexpected asset")
                .setMessage("This asset is not one of the models selected for auditing.  Please scan another asset")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing to do here
                    }
                })
                .create();
        d.setOnDismissListener(this);
        addShowListener(d);

        MediaHelper.playSoundEffect(getContext(), R.raw.original_sound__error_bleep_5);
        d.show();
    }

    private String getModelsListString() {
        Database db = Database.getInstance(getContext());
        List<Model> models = new ArrayList<>();
        if (auditDefinition.isAuditAllModels()) {
            models = db.getModels();
        } else {
            for (Integer i : auditDefinition.getRequiredModelIDs()) {
                try {
                    Model m = db.findModelByID(i);
                    models.add(m);
                } catch (ModelNotFoundException e) {
                    // TODO: 2/4/18
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Model m : models) {
            sb.append("&#8226; ");
            sb.append(m.getName());
            sb.append("<br/>");
        }

        return sb.toString();
    }

    private void showUnexpectedAssetDialog(final Asset a) {
        View v = View.inflate(getContext(), R.layout.view_audit_unexpected_scan, null);
        TextView tv = v.findViewById(R.id.statuses);

        //give a summary reason why this asset was not expected.  This should be either because
        //+ it was filtered out by meta status (assigned/unassigned), or because the asset's
        //+ status label was not one of the ones selected
        String reasonText;
        if (isAssetIncludedInMetaStatusFilter(a)) {
            reasonText = getUnexpectedMetaStatusReasonText(a);
        } else {
            reasonText = getUnexpectedStatusReasonText(a);
        }

        tv.setText(reasonText);

        AlertDialog d = new MaterialAlertDialogBuilder(getContext(), R.style.Widgets_Dialog)
                .setView(v)
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addAuditedAsset(a, AuditDetail.Status.UNEXPECTED);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing to do here
                    }
                })
                .create();
        d.setOnDismissListener(this);
        addShowListener(d);
        d.show();
    }

    private void addAuditedAsset(Asset a, AuditDetail.Status status) {
        MediaHelper.playSoundEffect(getContext(), R.raw.n_audioman__blip);
        AuditDetail record = new AuditDetail(null, audit.getHeader().getId(), a.getId(), System.currentTimeMillis(), status, null, false);
        detailRecords.add(record);
        auditedAssetsRecyclerView.getAdapter().notifyItemInserted(detailRecords.size() - 1);
        auditedAssetsRecyclerView.smoothScrollToPosition(detailRecords.size() - 1);
        showHideIntro();

        if (detailRecords.size() == 1 && !hasShownFirstCardShowcase) {
            int pos = ((LinearLayoutManager) auditedAssetsRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            View v = auditedAssetsRecyclerView.getLayoutManager().findViewByPosition(0);
            View v2 = auditedAssetsRecyclerView.getChildAt(0);
            Log.d(TAG, "foo");
//            final ImageButton editButton = (ImageButton) v.findViewById(R.id.edit_button);
//            editButton.performClick();
//
//            ShowcaseConfig config = new ShowcaseConfig();
//            MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), showcaseID);
//            config.setDelay(500);
//            sequence.setConfig(config);
//
//            String editButtonText = getString(R.string.audit_showcase_edit_button);
//            sequence.addSequenceItem(editButton, editButtonText, getString(R.string.next).toUpperCase());
//
//
//            sequence.setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
//                @Override
//                public void onDismiss(MaterialShowcaseView materialShowcaseView, int i) {
//                    editButton.performClick();
//                }
//            });
//
//            sequence.start();
        }
    }

    private void updateLists() {
        AuditedAssetRecyclerViewAdapter auditedAdapter = new AuditedAssetRecyclerViewAdapter(
                getContext(), detailRecords);
        auditedAdapter.setOnAuditRemovedListener(new AuditedAssetRecyclerViewAdapter.OnAuditRemovedListener() {
            @Override
            public void onAssetRemoved(@NotNull AuditDetail record) {
                detailRecords.remove(record);

                try {
                    Database db = Database.getInstance(getContext());
                    Asset a = db.findAssetByID(record.getAssetID());
                    unscannedAssets.add(0, a);
                    unscannedAssetsRecyclerView.getAdapter().notifyItemInserted(0);
                    unscannedAssetsRecyclerView.smoothScrollToPosition(0);

                } catch (AssetNotFoundException e) {
                    Log.e(TAG, "Could not find asset matching asset ID " + record.getAssetID()
                            + " from deleted asset audit detail record.  This asset will not be " +
                            "added back to list of unaudited assets");
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

                showHideIntro();
            }
        });

        auditedAssetsRecyclerView.swapAdapter(auditedAdapter,
                false);
        auditedAssetsRecyclerView.getAdapter().notifyDataSetChanged();
        auditedAssetsRecyclerView.smoothScrollToPosition(0);

        UnauditedAssetRecyclerViewAdapter unscannedAdapter = new UnauditedAssetRecyclerViewAdapter(
                getContext(), unscannedAssets);
        unscannedAssetsRecyclerView.swapAdapter(unscannedAdapter,
                false);
        unscannedAssetsRecyclerView.getAdapter().notifyDataSetChanged();


        showHideIntro();
    }

    private void submitAuditResults() {
        for (Asset asset : unscannedAssets) {
            AuditDetail record = new AuditDetail(null, audit.getHeader().getId(), asset.getId(),
                    System.currentTimeMillis(), AuditDetail.Status.NOT_AUDITED,
                    "Asset was not found during audit", false);
            detailRecords.add(record);
        }

        audit.getHeader().setEnd(System.currentTimeMillis());
        audit.getHeader().setCompleted(true);
        audit.setDetails(detailRecords);

        if (listener != null) {
            listener.onAuditComplete(audit);
        }
    }

    private boolean detailRecordsContains(Asset a) {
        boolean containsAsset = false;

        for (AuditDetail record : detailRecords) {
            if (record.getAssetID() == a.getId()) {
                containsAsset = true;
                break;
            }
        }

        return containsAsset;
    }

    private void showHideIntro() {
        if (detailRecords.isEmpty()) {
            error.setVisibility(View.VISIBLE);
            listHolder.setVisibility(View.GONE);
        } else {
            error.setVisibility(View.GONE);
            listHolder.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        input.focus();

        final Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                if (input != null) {
                    input.focus();
                }
            }
        };
        handler.postDelayed(r, 100);
    }
}
