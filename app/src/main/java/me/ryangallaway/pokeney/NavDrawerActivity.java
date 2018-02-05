package me.ryangallaway.pokeney;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.service.autofill.Dataset;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NavDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseUser currentUser;
    private DatabaseReference db;
    private ListView pokeListView;
    private ArrayAdapter<Poke> adapter;
    private ArrayList<Poke> pokeList;
    private ValueEventListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // build UI elements
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pokeList = new ArrayList<>();
        pokeListView = findViewById(R.id.pokeList);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pokeList);
        pokeListView.setAdapter(adapter);

        // build navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // get user currently logged in (From Firebase)
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // get database reference and populate Pokes list
        db = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onStart() {
        super.onStart();

        // listen for new pokes
        ValueEventListener pokeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get Poke and update ListView
                pokeList.clear();
                for (DataSnapshot pokes: dataSnapshot.child("pokes").getChildren()) {
                    Poke poke = pokes.getValue(Poke.class);
                    if (poke != null) {
                        pokeList.add(poke);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Pokeney", "loadPost:onCancelled", databaseError.toException());
            }
        };
        db.addValueEventListener(pokeListener);
        listener = pokeListener;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (listener != null) {
            db.removeEventListener(listener);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav_drawer, menu);

        TextView fullName = findViewById(R.id.navbarFullName);
        TextView email = findViewById(R.id.navbarEmail);
        fullName.setText(currentUser.getDisplayName());
        email.setText(currentUser.getEmail());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        if (id == R.id.nav_signout) {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(NavDrawerActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
                            Intent signInIntent = new Intent(NavDrawerActivity.this, SignInActivity.class);
                            startActivity(signInIntent);
                            finish();
                        }
                    });
        }
        return true;
    }

    public void onAddButtonPressed(View v) {
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
        Poke newPoke = new Poke(currentUser.getDisplayName(), dateFormat.format(new Date()));
        Map<String, Object> pokeUpdate = new HashMap<>();
        pokeUpdate.put("/pokes/" + newPoke.id, newPoke);
        db.updateChildren(pokeUpdate);
    }
}
