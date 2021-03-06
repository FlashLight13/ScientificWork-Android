package stydying.algo.com.algostudying.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import stydying.algo.com.algostudying.R;
import stydying.algo.com.algostudying.data.entities.stats.User;
import stydying.algo.com.algostudying.data.entities.tasks.TaskGroup;
import stydying.algo.com.algostudying.events.BusProvider;
import stydying.algo.com.algostudying.events.OperationErrorEvent;
import stydying.algo.com.algostudying.events.OperationSuccessEvent;
import stydying.algo.com.algostudying.logic.TaskGroupsBusinessLogic;
import stydying.algo.com.algostudying.logic.creation.GameFieldCreationController;
import stydying.algo.com.algostudying.operations.LoadUsersOperation;
import stydying.algo.com.algostudying.operations.OperationProcessingService;
import stydying.algo.com.algostudying.ui.fragments.BaseFragment;
import stydying.algo.com.algostudying.ui.fragments.edit_user_tasks_fragments.EditTaskFragment;
import stydying.algo.com.algostudying.ui.fragments.edit_user_tasks_fragments.EditTaskGroupFragment;
import stydying.algo.com.algostudying.utils.BundleBuilder;

/**
 * Created by Anton on 29.02.2016.
 */
public class EditUserTasksActivity extends BaseActivity {

    @Bind(R.id.list)
    protected ListView listView;

    public static final int REQUEST_CODE = 1313;
    private static final String MODE_EXTRA
            = "stydying.algo.com.algostudying.ui.activities.EditUserTasksActivity.MODE_EXTRA";
    public static final String ID_EXTRA
            = "stydying.algo.com.algostudying.ui.activities.EditUserTasksActivity.ID_EXTRA";

    public enum Mode {
        NEW(null), TASK_GROUP(EditTaskGroupFragment.class), TASK(EditTaskFragment.class);

        private Class<? extends BaseFragment> fragmentClass;

        Mode(Class<? extends BaseFragment> fragmentClass) {
            this.fragmentClass = fragmentClass;
        }

        @Nullable
        BaseFragment fragment(long id) {
            try {
                if (fragmentClass == null) {
                    return null;
                }
                BaseFragment baseFragment = fragmentClass.newInstance();
                baseFragment.setArguments(new BundleBuilder().putLong(ID_EXTRA, id).build());
                return baseFragment;
            } catch (Exception e) {
                return null;
            }
        }
    }

    private Mode mode;
    private boolean isInProgress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_edit_user_tasks);
        this.mode = Mode.valueOf(getIntent().getStringExtra(MODE_EXTRA));
        Fragment fragment = mode.fragment(getTaskGroupId());
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.content, fragment, null).commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        loadUsers();
    }

    private long getTaskGroupId() {
        return getIntent().getLongExtra(ID_EXTRA, TaskGroup.DEFAULT_ID);
    }

    private void loadUsers() {
        OperationProcessingService.executeOperation(this, new LoadUsersOperation());
        setInProgress(true);
    }

    @Subscribe
    public void onSuccess(OperationSuccessEvent event) {
        if (event.isOperation(LoadUsersOperation.class)) {
            setInProgress(false);
            List<User> users = event.data();
            if (mode == Mode.NEW && (users == null || users.size() == 0)) {
                setInProgress(true);
                OperationProcessingService.executeOperation(this,
                        controller().setUsers(getSelectedUserIds()).getCreateOperation());
            }
            List<UserData> userDatas;
            if (users == null) {
                userDatas = new ArrayList<>();
            } else {
                userDatas = new ArrayList<>(users.size());
                for (User user : users) {
                    if (user.getType() == User.Type.STUDENT) {
                        userDatas.add(new UserData(
                                user,
                                TaskGroupsBusinessLogic.hasTaskGroup(user, getTaskGroupId())));
                    }
                }
            }
            listView.setAdapter(new UsersAdapter(this, userDatas));
        }
        if (controller().onSuccess(this, event)) {
            setInProgress(false);
            Snackbar.make(listView, R.string.message_world_created, Snackbar.LENGTH_SHORT).show();
            finish();
        }
    }

    public List<String> getSelectedUserIds() {
        List<String> result = new ArrayList<>();
        if (listView.getAdapter() != null) {
            for (UserData userData : ((UsersAdapter) listView.getAdapter()).getUsers()) {
                if (userData.isSelected) {
                    result.add(userData.user.getLogin());
                }
            }
        }
        return result;
    }

    @Subscribe
    public void onError(OperationErrorEvent event) {
        if (event.isOperation(LoadUsersOperation.class)) {
            setInProgress(false);
        }
        if (controller().onError(event)) {
            setInProgress(false);
            Snackbar.make(listView, R.string.message_world_not_created, Snackbar.LENGTH_SHORT).show();
        }
    }

    private GameFieldCreationController controller() {
        return GameFieldCreationController.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.bus().register(this);
    }

    @Override
    protected void onPause() {
        BusProvider.bus().unregister(this);
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (super.onOptionsItemSelected(item)) {
            return true;
        }
        setInProgress(true);
        if (mode != Mode.NEW) {
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (fragment.onOptionsItemSelected(item)) {
                    return true;
                }
            }
        } else {
            OperationProcessingService.executeOperation(this,
                    controller().setUsers(getSelectedUserIds()).getCreateOperation());
            return true;
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_edit_done);
        if (isInProgress) {
            item.setActionView(R.layout.v_action_progress);
            item.expandActionView();
        } else {
            item.setActionView(null);
            item.collapseActionView();
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void setInProgress(boolean isInProgress) {
        this.isInProgress = isInProgress;
        supportInvalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_done, menu);
        return true;
    }

    public static void startMe(@NonNull Activity activity, @NonNull Mode mode, long id) {
        Intent intent = new Intent(activity, EditUserTasksActivity.class);
        intent.putExtra(MODE_EXTRA, mode.name());
        intent.putExtra(ID_EXTRA, id);
        activity.startActivityForResult(intent, REQUEST_CODE);
    }

    public static final class UserData {
        public final User user;
        public boolean isSelected;

        public UserData(User user, boolean isSelected) {
            this.user = user;
            this.isSelected = isSelected;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public UserData setSelected(boolean selected) {
            isSelected = selected;
            return this;
        }
    }

    private static class UsersAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener {
        // TODO rework with CursorAdapter

        private List<UserData> users;
        private Context context;

        public UsersAdapter(Context context, List<UserData> users) {
            this.context = context;
            this.users = users;
        }

        @Override
        public int getCount() {
            return users.size();
        }

        @Override
        public UserData getItem(int position) {
            return users.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CheckBox view = (CheckBox) convertView;
            if (view == null) {
                view = new CheckBox(context);
                ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        context.getResources().getDimensionPixelSize(R.dimen.list_item_height_medium));
                int margin = context.getResources().getDimensionPixelSize(R.dimen.basic_list_item_sides_margin);
                layoutParams.setMargins(margin, 0, margin, 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    layoutParams.setMarginEnd(margin);
                    layoutParams.setMarginStart(margin);
                }
                view.setLayoutParams(layoutParams);
                view.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                view.setPadding(context.getResources().getDimensionPixelSize(R.dimen.list_item_height_medium), 0, 0, 0);
                view.setOnCheckedChangeListener(this);
            }

            UserData userData = getItem(position);
            view.setTag(R.integer.position_key, position);
            view.setChecked(userData.isSelected);
            view.setText(userData.user.getName());

            return view;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int position = (Integer) buttonView.getTag(R.integer.position_key);
            getItem(position).isSelected = isChecked;
        }

        public List<UserData> getUsers() {
            return users;
        }
    }
}
