package com.example.elevatewebsolutions_tasktracker.database;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.elevatewebsolutions_tasktracker.MainActivity;
import com.example.elevatewebsolutions_tasktracker.database.entities.User;
import com.example.elevatewebsolutions_tasktracker.database.entities.Task;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
// import com.example.elevatewebsolutions_tasktracker.database.entities.Comment;

// TODO: Add Comment entity when implemented
@Database(entities = {User.class, Task.class}, version = 2, exportSchema = false)
public abstract class TaskManagerDatabase extends RoomDatabase {

    // Table names
    public static final String USER_TABLE = "user_table";
    public static final String TASK_TABLE = "task_table";
    public static final String COMMENT_TABLE = "comment_table";

    // Database name
    private static final String DATABASE_NAME = "TaskManager_database";

    // Singleton instance
    private static volatile TaskManagerDatabase INSTANCE;

    // Abstract methods to get DAOs
    public abstract UserDAO userDAO();
    public abstract TaskDAO taskDAO();
    // TODO: Add CommentDAO when Comment entity is implemented
    // public abstract CommentDAO commentDAO();

    //Number of threads repository will run on
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    /**
     * Singleton pattern implementation for database access
     * @param context Application context
     * @return TaskManagerDatabase instance
     */
    public static TaskManagerDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TaskManagerDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            TaskManagerDatabase.class,
                            DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .addCallback(addDefaultValues)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Adds Default values to the database in order to test functionality
     * Adds an admin, adds a user, and adds a couple of tasks
     */
    private static final RoomDatabase.Callback addDefaultValues = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Log.i(MainActivity.TAG, "Database CREATED!");
            databaseWriteExecutor.execute(() -> {
                UserDAO dao = INSTANCE.userDAO();
                TaskDAO taskDao = INSTANCE.taskDAO();
                dao.deleteAll();
                taskDao.deleteAllTasks();
                User admin = new User("admin1", "admin1", "admin");
                admin.setAdmin(true);
                dao.insert(admin);
                User testuser1 = new User("testuser1", "testuser1", "user");
                dao.insert(testuser1);
                Task testTask = new Task("Test Task 1", "This is a test task", "Open", testuser1.getId());
                taskDao.insert(testTask);
                //TODO: add test tasks
            });
        }
    };
}
