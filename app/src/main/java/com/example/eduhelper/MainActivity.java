package com.example.eduhelper;

import android.content.Intent;
import android.net.Uri;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerView;
    private List<ClassItem> classList;
    private ClassAdapter adapter;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private void showAdminDetailsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_admin_details, null);

        // References
        EditText gmailEditText = dialogView.findViewById(R.id.gmailText);
        View copyBtn = dialogView.findViewById(R.id.copyButton);
        View telegramLink = dialogView.findViewById(R.id.telegramLink);

        gmailEditText.setText("dhyeyp254@gmail.com");

        // Copy to clipboard
        copyBtn.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("email", gmailEditText.getText().toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Copied to clipboard!", Toast.LENGTH_SHORT).show();
        });

        // Telegram link click
        telegramLink.setOnClickListener(v -> {
            String url = "https://t.me/dhyeye";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        new AlertDialog.Builder(this)
                .setTitle("Admin Contact")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }


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

        // Toolbar setup
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer setup
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // RecyclerView setup
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
                            String extrasLink = obj.optString("extrasUrl", "");


                            classList.add(new ClassItem(className, notesLink, qpLink, videoLink, extrasLink));
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
        String url = "https://api.sheetbest.com/sheets/290ba29e-69fd-46e3-b713-40ad8c899acf";

        JSONObject body = new JSONObject();
        try {
            body.put("name", name);
            body.put("contact", contact);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "JSON creation error", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("SHEET_SUCCESS", "Response: " + response);
                    Toast.makeText(this, "Meeting stored in Sheet!", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e("SHEET_ERROR", "Error posting to Sheet: " + error.toString());
                    Toast.makeText(this, "Sheet may still be updated. Please verify.", Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return body.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
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

        } else if (id == R.id.nav_make_app) {
            showAdminDetailsDialog();
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
