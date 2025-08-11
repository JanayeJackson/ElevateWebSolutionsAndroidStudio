package com.example.elevatewebsolutions_tasktracker;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;

import com.example.elevatewebsolutions_tasktracker.database.TaskManagerRepository;
import com.example.elevatewebsolutions_tasktracker.database.entities.Task;
import com.example.elevatewebsolutions_tasktracker.database.entities.User;
import com.example.elevatewebsolutions_tasktracker.databinding.ActivityMainBinding;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "TASK_MANAGER";
    private static final String MAIN_ACTIVITY_USER_ID = "com.example.elevatewebsolutions_tasktracker.MAIN_ACTIVITY_USER_ID";
    static final String SHARED_PREFERENCE_USERID_KEY = "com.example.elevatewebsolutions_tasktracker.SHARED_PREFERENCE_USERID_KEY";

    private static final String SAVED_INSTANCE_STATE_USERID_KEY = "com.example.elevatewebsolutions_tasktracker.SAVED_INSTANCE_STATE_USERID_KEY";

    private static final int LOGGED_OUT = -1;
    private ActivityMainBinding binding;
    private TaskManagerRepository repository;

    private TableLayout table;

    private int loggedInUser = -1;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = TaskManagerRepository.getRepository(getApplication());
        loginUser(savedInstanceState);

        if(loggedInUser == -1){
            Intent intent = LoginActivity.loginIntentFactory(getApplicationContext());
            startActivity(intent);
        }

        updateSharedPreference();

        binding.taskDisplayTextView.setMovementMethod(new ScrollingMovementMethod());
        createTableRows();

        //Insert test tasks for demonstration purposes
        //Task testTask = new Task("Test Task", "This is a test task description.", "open", loggedInUser);
        //repository.insertTask(testTask);
    }

    @SuppressLint("SetTextI18n")
    private void createTableRows() {
        table = binding.taskDisplayTableLayout;
        table.removeAllViews();
        repository.getAllTasksByUserId(loggedInUser).observe(this, tasks -> {
            if (tasks != null && !tasks.isEmpty()) {
                TableRow headerRow = getHeaderRow();
                table.addView(headerRow);

                for (Task task : tasks) {
                    TableRow row = addTableRow(task);
                    table.addView(row);
                }
                binding.taskDisplayTextView.setText("Tasks for " + user.getUsername() + ":");
            } else {
                binding.taskDisplayTextView.setText("No tasks available.");
            }
        });



    }

    @NonNull
    private TableRow getHeaderRow() {
        TableRow headerRow = new TableRow(this);
        TextView taskNameHeader = new TextView(this);
        TextView taskDescriptionHeader = new TextView(this);

        taskNameHeader.setText("Task Name");
        taskNameHeader.setPadding(16, 16, 16, 16);
        taskNameHeader.setGravity(Gravity.START);
        taskDescriptionHeader.setText("Task Description");
        taskDescriptionHeader.setPadding(16, 16, 16, 16);
        taskDescriptionHeader.setGravity(Gravity.START);

        headerRow.addView(taskNameHeader);
        headerRow.addView(taskDescriptionHeader);
        if(user.getAdmin() == true){
            TextView deleteHeader = new TextView(this);
            deleteHeader.setText("Delete Task");
            deleteHeader.setPadding(16, 16, 16, 16);
            deleteHeader.setGravity(Gravity.START);
            headerRow.addView(deleteHeader);
        }

        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        params.topToBottom = R.id.taskDisplayTextView;           // below the table// align right edge
        params.topMargin = 24;
        headerRow.setLayoutParams(params);
        table.setLayoutParams(params);

        return headerRow;
    }

    @NonNull
    private TableRow addTableRow(Task task) {
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView taskNameTextView = new TextView(this);
        taskNameTextView.setText(task.getTitle());
        taskNameTextView.setPadding(16, 16, 16, 16);
        taskNameTextView.setGravity(Gravity.START);

        TextView taskDescriptionTextView = new TextView(this);
        taskDescriptionTextView.setText(task.getDescription());
        taskDescriptionTextView.setPadding(16, 16, 16, 16);
        taskDescriptionTextView.setGravity(Gravity.START);

        row.addView(taskNameTextView);
        row.addView(taskDescriptionTextView);

        if(user.getAdmin() == true){
            Button deleteButton = getDeleteButton(task);
            row.addView(deleteButton);
        }

        return row;
    }

    @NonNull
    private Button getDeleteButton(Task task) {
        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");
        deleteButton.setOnLongClickListener(view -> {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
            alertBuilder.setMessage("Are you sure you want to delete this task?");
            alertBuilder.setPositiveButton("Delete", (dialog, which) -> {
                repository.deleteTask(task);
                createTableRows();
            });
            alertBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            alertBuilder.create().show();
            return true;
        });
        return deleteButton;
    }

    private void updateDisplay() {
    }

    private void loginUser(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        loggedInUser = sharedPreferences.getInt(getString(R.string.preference_userid_key), LOGGED_OUT);

        if(loggedInUser == LOGGED_OUT && savedInstanceState != null && savedInstanceState.containsKey(SAVED_INSTANCE_STATE_USERID_KEY)){
            loggedInUser = savedInstanceState.getInt(SAVED_INSTANCE_STATE_USERID_KEY, LOGGED_OUT);
        }
        if(loggedInUser == LOGGED_OUT){
            loggedInUser = getIntent().getIntExtra(MAIN_ACTIVITY_USER_ID, LOGGED_OUT);
        }
        if(loggedInUser == LOGGED_OUT){
            return;
        }
        LiveData<User> userObserver = repository.getUserByUserId(loggedInUser);
        userObserver.observe(this, user -> {
            this.user = user;
            if(user != null){
                invalidateOptionsMenu();
            }else{
                logout();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_INSTANCE_STATE_USERID_KEY, loggedInUser);
        updateSharedPreference();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.logoutMenuItem);
        item.setVisible(true);
        if(user == null){
            return false;
        }
        item.setTitle(user.getUsername());

        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                showLogoutDialog();
                return false;
            }
        });

        return true;
    }
    private void showLogoutDialog(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
        final AlertDialog alertDialog = alertBuilder.create();

        alertBuilder.setMessage("Logout?");

        alertBuilder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                logout();
            }
        });

        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });

        alertBuilder.create().show();
    }

    private void logout(){
        loggedInUser = LOGGED_OUT;
        updateSharedPreference();
        getIntent().putExtra(MAIN_ACTIVITY_USER_ID, LOGGED_OUT);

        startActivity(LoginActivity.loginIntentFactory(getApplicationContext()));
    }

    private void updateSharedPreference(){
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedPrefEditor = sharedPreferences.edit();
        sharedPrefEditor.putInt(getString(R.string.preference_userid_key), loggedInUser);
        sharedPrefEditor.apply();
    }

    static Intent mainActivityIntentFactory(Context context, int userId){
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(MAIN_ACTIVITY_USER_ID, userId);
        return intent;
    }

}