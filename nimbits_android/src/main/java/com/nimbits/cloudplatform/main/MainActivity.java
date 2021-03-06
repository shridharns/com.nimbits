package com.nimbits.cloudplatform.main;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.nimbits.cloudplatform.Nimbits;
import com.nimbits.cloudplatform.R;
import com.nimbits.cloudplatform.ToastHelper;
import com.nimbits.cloudplatform.alerts.AlertSettingActivity;
import com.nimbits.cloudplatform.client.enums.EntityType;
import com.nimbits.cloudplatform.client.enums.Parameters;
import com.nimbits.cloudplatform.client.model.entity.Entity;
import com.nimbits.cloudplatform.main.async.LoadMainTask;
import com.nimbits.cloudplatform.main.async.delete.DeleteEntityDialogFragment;
import com.nimbits.cloudplatform.main.async.delete.DeleteEntityTask;
import com.nimbits.cloudplatform.settings.PointSettingsActivity;
import com.nimbits.cloudplatform.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * User: benjamin
 * Date: 12/27/12
 * Time: 7:52 PM
 */
public class MainActivity extends Activity implements
        EntityListAdapter.ExpandListener,
        EntityListAdapter.EntityClickedListener,
        AddEntityDialog.EntitySelectedDialogListener,
        DeleteEntityDialogFragment.PositiveButtonClickHandler,
        SettingOptionsDialog.OptionSelectedListener

{

    private Context context;


    private EntityListFragment entityListFragment;



    private final Timer timer = new Timer();
    private TextView titleView;

    private ProgressBar progressBar;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        setContentView(R.layout.main_activity);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        progressBar.setVisibility(View.VISIBLE);
        titleView = (TextView) findViewById(R.id.entity_name);
        titleView.setText(Nimbits.session.getEmail().getValue());
        initNewlyStartedActivity(getIntent());
        if (Nimbits.currentEntity == null) {
            Nimbits.currentEntity = Nimbits.session;
        }

        ImageView expand = (ImageView) findViewById(R.id.image_expand);
        expand.setVisibility(View.GONE);
        loadTree(true);

    }
    @Override
    public void onResume() {
        super.onResume();
        //  surface.resume();
    }
    private void initNewlyStartedActivity(Intent intent)  {


        MainToolBar toolbar = new MainToolBar();
        toolbar.initToolbar();

        //initGPS();

        boolean refresh = intent.getBooleanExtra(Parameters.refresh.getText(), false);
        if (Nimbits.tree == null || Nimbits.tree.isEmpty()) {
            loadTree(refresh);
        }

        Entity entity = (Entity) intent.getSerializableExtra(Parameters.entity.name());
        if (entity != null) {
            // stack.setDisplayedChild(stackLocationMap.get(entity));
            // singleEntityViewing.initSingleEntityView();
        }

    }

    private void loadTree(boolean refresh) {
        final LoadMainTask task = new LoadMainTask();
        task.setListener(new LoadMainTask.LoadListener() {

            @Override
            public void onSuccess(List<Entity> response) {
                try {
                updateProgressBar(0);
                List<Entity> current = getChildEntities(response);

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                entityListFragment = new EntityListFragment(context, current);
                String fragmentTag =  ("EntityList");
                fragmentTransaction.replace(R.id.main_frame, entityListFragment, fragmentTag);
                fragmentTransaction.commit();
                progressBar.setVisibility(View.GONE);
                }
                catch (IllegalStateException ex) {
                    return;

                }

            }

            @Override
            public void onProgress(int progress) {
                updateProgressBar(progress);
            }


        });
        task.execute(refresh);
    }

    private List<Entity> getChildEntities(List<Entity> response) {
        List<Entity> current = new ArrayList<Entity>();
        for (Entity entity : response) {
            if (entity.getParent().equals(Nimbits.currentEntity.getKey()) && ! entity.getEntityType().equals(EntityType.user) && entity.getEntityType().isTreeGridItem()) {

                current.add(entity);
            }
        }
        return current;
    }

    private void updateProgressBar(int progress) {
        ProgressBar bar = (ProgressBar) findViewById(R.id.progressBar);
        bar.setMax(100);
        bar.setProgress(progress);

    }



    @Override
    public void onEntityExpand(Entity entity) {
        showEntity(entity);
    }



    //    @SuppressWarnings("unchecked")
//    private void loadCurrentValues() {
//        final View current = stack.getCurrentView();
//
//        if (current != null) {
//            ListView entityList = (ListView) current.findViewById(R.id.entity_list_view);
//
//            if (entityList != null && entityList.getAdapter() != null) {
//                Log.v("nimbits", "running loadcurrent" + entityList.getChildCount());
//
//                Log.v("nimbits", String.valueOf(entityList.getAdapter().getCount()));
//                for (int i = 0; i < entityList.getChildCount(); i++) {
//
//                    Entity entity = (Entity) entityList.getAdapter().getItem(i);
//                    final View view = entityList.getChildAt(i);
//
//                    final TextView valueText = (TextView) view.findViewById(R.id.value);
//                    final TextView note = (TextView) view.findViewById(R.id.note);
//                    final ImageView status = (ImageView) view.findViewById(R.id.entity_image);
//                    final TextView timestamp = (TextView) view.findViewById(R.id.timestamp);
//                    Log.v("nimbits", "got field" + valueText.getText());
//
//                    if (entity.getEntityType().equals(EntityType.point)) {
//                        // TextView valueEntry = (TextView) findViewById(R.id.current_value);
//
//
//                        LoadValueTask.getInstance(new LoadValueTask.LoadValueTaskListener() {
//                            @Override
//                            public void onSuccess(Value response) {
//                                PointViewHelper.setViews(response, valueText, timestamp, note, status, SimpleValue.getEmptyInstance());
//
//                            }
//                        }).execute(entity);
//                    } else {
//
//                        valueText.setVisibility(View.GONE);
//                    }
//
//
//                }
//            }
//        }
//
//    }
//

    @Override
    public void onSettingOptionSelected(SettingOption option, Entity entity) {

        Intent intent;
        switch (option) {
            case application:
                intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                break;
            case point:
                intent = new Intent(getApplicationContext(), PointSettingsActivity.class);
                intent.putExtra(Parameters.entity.getText(), entity);

                startActivity(intent);
                finish();


            case alert:
                intent = new Intent(getApplicationContext(), AlertSettingActivity.class);
                intent.putExtra(Parameters.entity.getText(), entity);

                startActivity(intent);
                finish();
                break;
        }

    }

    @Override
    public void onPositiveDeleteEntityButtonClicked() {

        DeleteEntityTask.getInstance(new DeleteEntityTask.DeleteEntityTaskListener() {
            @Override
            public void onSuccess() {
                goBack();
            }
        });

    }

    @Override
    public void onBackPressed() {
        goBack();

    }

    @Override
    public void onEntityClicked(Entity entity) {

        switch (entity.getEntityType()) {


            case point:
                Nimbits.currentEntity = entity;
                Intent intent = new Intent(getBaseContext(), PointActivity.class);
                startActivity(intent);
                finish();
                break;
            case user:
                break;
            case category:
                showEntity(entity);
                break;
            case subscription:
                break;

            case calculation:
                break;

            case summary:
                break;

            case accessKey:
                break;
        }
//
//
    }
    //
//    //new entity type selected event
    @Override
    public void onEntityTypeSelected(EntityType type, Entity parent) {


        switch (type) {
            case point:
                Intent intent = new Intent(getApplicationContext(), NewEntityActivity.class);
                intent.putExtra(Parameters.type.name(), type.name());
                intent.putExtra(Parameters.parent.name(), parent);
                startActivity(intent);
                finish();
                break;

        }
    }


    private class MainToolBar {



        private void initRefreshButton() {
            ImageButton refresh = (ImageButton) findViewById(R.id.refresh_button);
            refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    loadTree(true);
                }
            });
        }

        private void initDeleteButton() {
            ImageButton delete = (ImageButton) findViewById(R.id.delete_button);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (! Nimbits.currentEntity.getEntityType().equals(EntityType.user)) {
                        DeleteEntityDialogFragment delete = new DeleteEntityDialogFragment(getBaseContext());
                        delete.show(getFragmentManager(), "Confirm");
                    }
                    else {
                        ToastHelper.show(getApplicationContext(), "Select the object you'd like to delete first, you're on the top level.");
                    }

                }
            });
        }

        private void initAddButton() {
            ImageButton add = (ImageButton) findViewById(R.id.new_entity_button);
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    AddEntityDialog dialog = new AddEntityDialog(Nimbits.currentEntity);
                    dialog.show(getFragmentManager(), "Create Entity");


                }
            });
        }

        private void initToolbar() {
            initDeleteButton();
            initAddButton();
            initRefreshButton();


            initSettingsButton();
            initHomeButton();
        }

        private void initSettingsButton() {


            ImageButton button = (ImageButton) findViewById(R.id.settings_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    setCurrentEntity();
                    SettingOptionsDialog dialog = new SettingOptionsDialog(Nimbits.currentEntity);
                    dialog.show(getFragmentManager(), "Setting Options");


                }
            });
        }

        private void initHomeButton() {

            ImageButton button = (ImageButton) findViewById(R.id.home_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    showEntity(Nimbits.session);
                    //  stack.setDisplayedChild(homeIndex);

                }
            });
        }


    }

    private void showEntity(Entity entity) {
        Nimbits.currentEntity =entity;
        entityListFragment.refresh(getChildEntities(Nimbits.tree));
        titleView.setText(Nimbits.currentEntity.getName().getValue());
    }

    private void goBack() {
        if (Nimbits.currentEntity == null) {
            showEntity(Nimbits.session);
        }
        else if (! Nimbits.currentEntity.getEntityType().equals(EntityType.user)) {
            showEntity(Nimbits.getParentEntity());

        }
    }


}
