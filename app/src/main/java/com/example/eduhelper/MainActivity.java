package com.example.eduhelper;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerView;
    private List<ClassItem> classList;
    private ClassAdapter adapter;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Setup ActionBarDrawerToggle (hamburger)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Setup RecyclerView
        recyclerView = findViewById(R.id.classRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        classList = new ArrayList<>();
        adapter = new ClassAdapter(this, classList);
        recyclerView.setAdapter(adapter);

        fetchClassData();
    }

    private void fetchClassData() {
        String url = "https://api.sheetbest.com/sheets/bcbbcbac-a5d0-4fd9-8021-02e3bf4f8da0";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    classList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String className = obj.optString("className", "Unknown Class");
                            String notesLink = obj.optString("notesUrl", "");
                            String qpLink = obj.optString("questionsUrl", "");
                            String videoLink = obj.optString("videoUrl", "");

                            classList.add(new ClassItem(className, notesLink, qpLink, videoLink));
                        } catch (Exception e) {
                            Log.e("JSON_PARSE_ERROR", "Error parsing JSON at index " + i, e);
                        }
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> {
                    Log.e("VOLLEY_ERROR", "Error fetching data", error);
                    Toast.makeText(this, "Failed to load data.", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(request);
    }

    // ========================== BOOK A MEETING ==========================

    public void showMeetingDialog(View v) {
        View dialogView = getLayoutInflater().inflate(R.layout.book_meeting_dialog, null);
        EditText nameInput = dialogView.findViewById(R.id.meetingName);
        EditText contactInput = dialogView.findViewById(R.id.meetingContact);

        new AlertDialog.Builder(this)
                .setTitle("Book a Meeting")
                .setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String contact = contactInput.getText().toString().trim();

                    if (!name.isEmpty() && !contact.isEmpty()) {
                        sendToSheet(name, contact);
                        sendToTelegram(name, contact);
                    } else {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendToSheet(String name, String contact) {
        String url = "https://api.sheetbest.com/sheets/a5b08e9f-6482-46b4-ad1e-d855af1765bc";

        JSONObject body = new JSONObject();
        try {
            body.put("name", name);
            body.put("contact", contact);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> Toast.makeText(this, "Meeting stored in Sheet!", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this, "Failed to store in Sheet", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void sendToTelegram(String name, String contact) {
        String BOT_TOKEN = "7732270849:AAGEWA6H6b6mr6UkXk5y2DP-fENs9OuXCLc";
        String CHAT_ID = "1123135015";

        String message = "📥 New Meeting Request:\n👤 Name: " + name + "\n📞 Contact: " + contact;
        String url = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage"
                + "?chat_id=" + CHAT_ID + "&text=" + android.net.Uri.encode(message);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> Log.d("TELEGRAM", "Telegram message sent!"),
                error -> Log.e("TELEGRAM_ERROR", "Failed to send Telegram message", error)
        );

        Volley.newRequestQueue(this).add(request);
    }

    // ========================== SIDEBAR NAVIGATION ==========================

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_teachers) {
            Toast.makeText(this, "Teachers list coming soon!", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_book_meeting) {
            showMeetingDialog(null);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
