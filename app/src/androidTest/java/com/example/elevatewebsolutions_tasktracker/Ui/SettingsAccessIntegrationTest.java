package com.example.elevatewebsolutions_tasktracker.Ui;

import android.app.Activity;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import com.example.elevatewebsolutions_tasktracker.MainActivity;
import com.example.elevatewebsolutions_tasktracker.R;
import com.example.elevatewebsolutions_tasktracker.SettingsActivity;
import com.example.elevatewebsolutions_tasktracker.auth.services.SessionManager;
import com.example.elevatewebsolutions_tasktracker.database.entities.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;

/**
 * Integration test: only admins can see SettingsActivity.
 * - Admin: Settings UI is shown.
 * - Non-admin: launching SettingsActivity redirects to MainActivity.
 */
@RunWith(AndroidJUnit4.class)
public class SettingsAccessIntegrationTest {

    private SessionManager sessionManager;

    @Before
    public void setUp() {
        sessionManager = new SessionManager(
                androidx.test.core.app.ApplicationProvider.getApplicationContext()
        );
        // start every test with a clean session
        sessionManager.destroySession();
    }

    @After
    public void tearDown() {
        sessionManager.destroySession();
    }

    private void signIn(boolean isAdmin) {
        // User(String username, String password, String title, boolean isAdmin)
        User u = new User(isAdmin ? "adminUser" : "memberUser",
                "pw",
                isAdmin ? "Admin" : "Member",
                isAdmin);
        sessionManager.createSession(u);
    }

    @Test
    public void admin_canOpenSettings_andSeeUI() {
        signIn(true); // admin

        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            // Verify something actually on Settings screen (matches your layout)
            onView(withId(R.id.titleSettingsTextView)).check(matches(isDisplayed()));
            onView(withId(R.id.addTaskButton)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void nonAdmin_redirectsToMainActivity() {
        signIn(false); // regular user

        try (ActivityScenario<SettingsActivity> ignored =
                     ActivityScenario.launch(SettingsActivity.class)) {

            // Which activity is RESUMED after launching Settings?
            final Activity[] resumed = new Activity[1];
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                for (Activity a : ActivityLifecycleMonitorRegistry.getInstance()
                        .getActivitiesInStage(Stage.RESUMED)) {
                    resumed[0] = a; // take the first resumed activity
                    break;
                }
            });

            assertTrue(
                    "Expected redirect to MainActivity but was: "
                            + (resumed[0] == null ? "null" : resumed[0].getClass().getName()),
                    resumed[0] instanceof MainActivity
            );
        }
    }
}
