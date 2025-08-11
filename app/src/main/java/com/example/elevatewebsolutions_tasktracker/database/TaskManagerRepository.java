package com.example.elevatewebsolutions_tasktracker.database;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.elevatewebsolutions_tasktracker.MainActivity;
import com.example.elevatewebsolutions_tasktracker.database.entities.Task;
import com.example.elevatewebsolutions_tasktracker.database.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TaskManagerRepository {

    private final TaskDAO taskDAO;
    private final UserDAO userDao;
    private LiveData<List<Task>> alltasks;

    private static TaskManagerRepository repository;

    /**
     * Creates single instance of Database that can be used to update database ensuring
     * there are no conflicts or collisions.
     * @param application
     */
    private TaskManagerRepository(Application application) {
        TaskManagerDatabase db = TaskManagerDatabase.getDatabase(application);
        this.taskDAO = db.taskDAO();
        this.userDao = db.userDAO();
        this.alltasks = this.taskDAO.getAllTasks();
    }

    /**
     * Checks to see if there is an active repository, if so returns that repository
     * Else waits for an instance of the repository to be created using the databseWriteExecutor
     * Uses try and catch to attempt to return created repository, if error occurs exception is thrown
     * @param application
     * @return instance of repository
     */
    public static TaskManagerRepository getRepository(Application application){
        if (repository != null){
            return repository;
        }
        Future<TaskManagerRepository> future = TaskManagerDatabase.databaseWriteExecutor.submit(
                new Callable<TaskManagerRepository>() {
                    @Override
                    public TaskManagerRepository call() throws Exception {
                        return new TaskManagerRepository(application);
                    }
                }
        );
        try{
            return future.get();
        }catch(InterruptedException | ExecutionException e){
            Log.d(MainActivity.TAG, "Problem getting TaskManagerRepository, thread error");
        }
        return null;
    }

    /**
     * Gets all tasks in the task database
     * @return a LiveData list of all tasks
     */
    public LiveData<List<Task>> getAllTasks(){
        return  taskDAO.getAllTasks();
    }

    /**
     * Inserts a task into the task database
     * @param task
     */
    public void insertTask(Task task) {
        TaskManagerDatabase.databaseWriteExecutor.execute(() -> {
            taskDAO.insert(task);
        });
    }

    /**
     * Inserts a user into the user database
     * @param user
     */
    public void insertUser(User... user) {
        TaskManagerDatabase.databaseWriteExecutor.execute(() -> {
            userDao.insert(user);
        });
    }

    /**
     * Gets user properties using username
     * @param username
     * @return a LiveData object of a User
     */
    public LiveData<User> getUserByUserName(String username) {
        return userDao.getUserByUsername(username);
    }

    /**
     * Gets user properties using user ID
     * @param id
     * @return a LiveData object of a User
     */
    public LiveData<User> getUserByUserId(int id) {
        return userDao.getUserByUserId(id);
    }

    /**
     * Gets a list of tasks by the ID of the user that is loggedIn
     * @param loggedInUserId
     * @return a LivedData object of all tasks associated with current user
     */
    public LiveData<List<Task>>getAllTasksByUserId(int loggedInUserId){
        return taskDAO.getTasksByUserId(loggedInUserId);
    }

    public void deleteTask(Task task) {
        TaskManagerDatabase.databaseWriteExecutor.execute(() -> {
            taskDAO.delete(task);
        });
    }
}