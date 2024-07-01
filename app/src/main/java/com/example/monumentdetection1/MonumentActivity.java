package com.example.monumentdetection1;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;




public class MonumentActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MonumentAdapter monumentAdapter;
    private List<Monument> monumentList = new ArrayList<>();
    private FirebaseFirestore db;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monument);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        monumentAdapter = new MonumentAdapter(monumentList, this);
        recyclerView.setAdapter(monumentAdapter);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        loadMonuments();

        searchView = findViewById(R.id.searchView);
        setupSearchView();
    }

    private void loadMonuments() {
        db.collection("monuments")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Monument monument = document.toObject(Monument.class);
                            monumentList.add(monument);
                        }
                        monumentAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MonumentActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MonumentActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterMonuments(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterMonuments(newText);
                return false;
            }
        });
    }

    private void filterMonuments(String query) {
        List<Monument> filteredList = new ArrayList<>();
        for (Monument monument : monumentList) {
            if (monument.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(monument);
            }
        }
        monumentAdapter.updateList(filteredList);
    }
}