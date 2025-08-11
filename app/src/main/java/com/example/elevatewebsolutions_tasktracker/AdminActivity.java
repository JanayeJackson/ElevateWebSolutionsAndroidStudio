package com.example.elevatewebsolutions_tasktracker;

import android.content.Context;
import android.content.Intent;

import com.example.elevatewebsolutions_tasktracker.databinding.ActivityLoginBinding;

public class AdminActivity {

    public static Intent addTaskIntentFactory(Context applicationContext, int loggedInUser) {
        Intent intent = new Intent(applicationContext, AdminActivity.class);
        intent.putExtra("loggedInUser", loggedInUser);
        return intent;
    }
}
