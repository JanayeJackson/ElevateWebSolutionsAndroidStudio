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
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;

import com.example.elevatewebsolutions_tasktracker.database.TaskManagerRepository;
import com.example.elevatewebsolutions_tasktracker.database.entities.Task;
import com.example.elevatewebsolutions_tasktracker.database.entities.User;
import com.example.elevatewebsolutions_tasktracker.databinding.ActivityMainBinding;

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

        repository = TaskManagerRepository.getRepository(getApplication());  //get repository instance
        loginUser(savedInstanceState); //login user and get user object

        if(loggedInUser == -1){ //if user is not logged in, redirect to login activity
            Intent intent = LoginActivity.loginIntentFactory(getApplicationContext());
            startActivity(intent);
        }

        updateSharedPreference(); //update shared preference with logged in user id

        binding.taskDisplayTextView.setMovementMethod(new ScrollingMovementMethod()); //make text view scrollable

        //Insert test tasks for demonstration purposes
        //Task testTask = new Task("Test Task", "This is a test task description.", "open", loggedInUser);
        //repository.insertTask(testTask);
    }

    /**
     * Creates the table rows for displaying tasks.
     * This method checks if the user is an admin or a regular user.
     * If the user is an admin, it fetches all tasks from the repository.
     * If the user is a regular user, it fetches tasks specific to that user.
     */
    @SuppressLint("SetTextI18n")
    private void createTableRows() {
        if(user.getAdmin() == true){ //if user is an admin, get all tasks from the repository
            repository.getAllTasks().removeObservers(this);
            repository.getAllTasks().observe(this, this::handleTasks);
        }else { //if user is a regular user, get tasks specific to that user from the repository
            repository.getAllTasksByUserId(loggedInUser).removeObservers(this);
            repository.getAllTasksByUserId(loggedInUser).observe(this, this::handleTasks);
        }
        //if user is an admin get add task button from binding
        if(user.getAdmin() == true){
            Button addTaskButton = binding.addTaskButton;
            addTaskButton.setVisibility(View.VISIBLE);
            addTaskButton.setOnClickListener(view -> {
                //Intent intent = AddTaskActivity.addTaskIntentFactory(getApplicationContext(), user.getId());
                //startActivity(intent);
            });
        }
    }

    /**
     * This method creates a header row and adds each task as a new row in the table.
     * If the list of tasks is empty, it displays a message indicating that there are no tasks available.
     * If the user is an admin, it also adds a delete button for each task.
     * @param tasks List of tasks to be displayed in the table.
     */
    @SuppressLint("SetTextI18n")
    private void handleTasks(List<Task> tasks) {
        table = binding.taskDisplayTableLayout; //get table layout from binding
        table.removeAllViews(); //remove all views from the table layout

        //If tasks is not empty, clear table and create new header row and add each task as a new row
        if (tasks != null && !tasks.isEmpty()) {
            TableRow headerRow = getHeaderRow();
            table.addView(headerRow);

            // Iterate through the list of tasks and add each task as a new row in the table
            for (Task task : tasks) {
                TableRow row = addTableRow(task);
                table.addView(row);
            }

            binding.taskDisplayTextView.setText("Tasks for " + user.getUsername() + ":");
        } else {
            binding.taskDisplayTextView.setText("No tasks available.");
        }
    }

    /**
     * Creates a header row for the task table.
     * This row contains the column titles for task name, task status, and edit task.
     * If the user is an admin, it also includes a column for deleting tasks.
     * ConstraintLayout parameters are set to position the header row below the TextView displaying the user's tasks.
     * @return A TableRow that serves as the header for the task table.
     */
    @SuppressLint("SetTextI18n")
    @NonNull
    private TableRow getHeaderRow() {
        TableRow headerRow = new TableRow(this);
        TextView taskNameHeader = new TextView(this);
        TextView taskDescriptionHeader = new TextView(this);
        TextView editTaskHeader = new TextView(this);

        // Set text and layout parameters for the header TextViews
        taskNameHeader.setText("Task Name");
        taskNameHeader.setPadding(16, 16, 16, 16);
        taskNameHeader.setGravity(Gravity.START);

        taskDescriptionHeader.setText("Task Status");
        taskDescriptionHeader.setPadding(16, 16, 16, 16);
        taskDescriptionHeader.setGravity(Gravity.START);

        editTaskHeader.setText("Edit Task");
        editTaskHeader.setPadding(16, 16, 16, 16);
        editTaskHeader.setGravity(Gravity.START);

        // Add the TextViews to the header row
        headerRow.addView(taskNameHeader);
        headerRow.addView(taskDescriptionHeader);
        headerRow.addView(editTaskHeader);

        // If the user is an admin, add a delete task header
        if(user.getAdmin() == true){
            TextView deleteHeader = new TextView(this);
            deleteHeader.setText("Delete Task");
            deleteHeader.setPadding(16, 16, 16, 16);
            deleteHeader.setGravity(Gravity.START);
            headerRow.addView(deleteHeader);
        }

        // Set the layout parameters for the header row and the table
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        params.topToBottom = R.id.taskDisplayTextView; // Align header row below the TextView
        params.topMargin = 24;
        headerRow.setLayoutParams(params);
        table.setLayoutParams(params);

        return headerRow;
    }

    /**
     * Adds a new TableRow to the TableLayout for displaying a task.
     * @param task the Task object to be displayed in the row.
     * @return A TableRow containing TextViews for the task name and status, and buttons for editing and deleting the task.
     */
    @NonNull
    private TableRow addTableRow(Task task) {
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        // Create TextViews for task name and status
        TextView taskNameTextView = new TextView(this);
        taskNameTextView.setText(task.getTitle());
        taskNameTextView.setPadding(16, 16, 16, 16);
        taskNameTextView.setGravity(Gravity.START);

        TextView taskStatusTextView = new TextView(this);
        taskStatusTextView.setText(task.getStatus());
        taskStatusTextView.setPadding(16, 16, 16, 16);
        taskStatusTextView.setGravity(Gravity.START);

        // Create an Edit button for the task
        Button editButton = getEditButton(task);

        // Add the TextViews and button to the row
        row.addView(taskNameTextView);
        row.addView(taskStatusTextView);
        row.addView(editButton);

        // If the user is an admin, add a delete button for the task
        if(user.getAdmin() == true){
            Button deleteButton = getDeleteButton(task);
            row.addView(deleteButton);
        }

        return row;
    }

    /**
     * Creates an edit button for each task.
     * @param task the Task object for which the edit button is created.
     * @return A Button that, when clicked, will start the EditTaskActivity to edit the task.
     */
    private Button getEditButton(Task task) {
        Button editButton = new Button(this);
        editButton.setText("Edit");
        editButton.setOnClickListener(view -> {
            //Intent intent = EditTaskActivity.editTaskIntentFactory(getApplicationContext(), task.getTaskId());
            //startActivity(intent);
        });
        return editButton;
    }

    /**
     * Creates a delete button for each task.
     * @param task the Task object for which the delete button is created.
     * @return A Button that, when long-clicked, will prompt the user to confirm deletion of the task.
     */
    @NonNull
    private Button getDeleteButton(Task task) {
        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");
        deleteButton.setOnLongClickListener(view -> {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
            alertBuilder.setMessage("Are you sure you want to delete this task?");
            alertBuilder.setPositiveButton("Delete", (dialog, which) -> {
                repository.deleteTask(task);
            });
            alertBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            alertBuilder.create().show();
            return true;
        });
        return deleteButton;
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
                createTableRows();
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