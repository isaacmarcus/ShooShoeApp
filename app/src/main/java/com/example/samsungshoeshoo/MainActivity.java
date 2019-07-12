package com.example.samsungshoeshoo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    // Mock http from laptop
    private String url="http://10.12.156.149:8100/shoes";
    private String postUrl="http://10.12.156.149:8100/location";

    // urls from RPI
//    private String url="http://10.12.0.19:8100/shoes"; // raspberry pi url
//    private String postUrl="http://10.12.0.19:8100/location";
    private RecyclerView recyclerView;
    private MyAdapter adapter;

    private List<ListItem> itemList;

    private String currentCategory = "favourites";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Build pull down to refresh view
        SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);

        // Add different colours to refresh loading
        pullToRefresh.setColorSchemeResources(android.R.color.holo_blue_dark,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // pull to refresh listener
        pullToRefresh.setOnRefreshListener(() -> {
            updateRecyclerView();
            pullToRefresh.setRefreshing(false);
        });


        // Pop up progress dialog to show loading database
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Database...");
        progressDialog.show();
        // Method called to build recycler view on opening application
        buildRecyclerView();
        progressDialog.dismiss();

        // on click listener for deploy button
        adapter.setOnItemClickListener(position -> deployItem(position));
    }

    public void deployItem(int position) {
        // Send Post Data
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deploying Shoe...");
        progressDialog.show();

        JSONObject postData = new JSONObject();
        try {
            // PUT postData required to be received by server in JSON Format
            postData.put("shelfIndex", itemList.get(position).getShelfId());

            // execute AsyncTask to send post data to http server as JSONObject in string format
            SendDeviceDetails sDD = new SendDeviceDetails();
            sDD.execute(postUrl, postData.toString());

            progressDialog.dismiss();
        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog.dismiss();
        }

        //TODO: Add if statement for if message received from post is successful

        // Remove item if successfully deployed
        itemList.remove(position);
        adapter.notifyItemRemoved(position);
    }

    public void buildRecyclerView() {
        //**
        //
        // getting recycler view from xml
        //**
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // initialize item list
        itemList = new ArrayList<>();

        //creating recycler view adapter
        adapter = new MyAdapter(this, itemList);

        // Get Database and update adapter
        updateRecyclerView();

        //setting adapter to recycler
        recyclerView.setAdapter(adapter);
    }

    // Method to update item list when refresh pull down is triggered
    public void updateRecyclerView () {
        recyclerView.getRecycledViewPool().clear();
        getData();
//        getDataLocal();
        adapter.notifyDataSetChanged();
    }

    // method for closing drawer with back button if it is open
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    // Listener for navigation items
//    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_favourites) {
            currentCategory = "favourites";
        } else if (id == R.id.nav_sneakers) {
            currentCategory = "sneaker";
        } else if (id == R.id.nav_formal) {
            currentCategory = "formal";
        } else if (id == R.id.nav_slippers) {
            currentCategory = "slipper";
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        updateRecyclerView();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        Toast.makeText(getApplicationContext(),currentCategory + " category selected.",Toast.LENGTH_SHORT).show();
        return true;
    }

    // method to parse Data and attach to List
    private void getData() {

        // Json Request
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,null, response -> {

            try {
                List<ListItem> currentItemList = new ArrayList<>();

                JSONArray jsonArray = response.getJSONArray("data");
                for(int i=0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    // Add jsonObject only if the occupied field is 1
                    if(jsonObject.getString("occ").equals("1")) {
                        ListItem item = new ListItem();
                        item.setShelfId(Integer.parseInt(jsonObject.getString("shelfIndex")));
                        item.setOcc(Integer.parseInt(jsonObject.getString("occ")));
                        item.setType(jsonObject.getString("type"));
                        item.setColour(jsonObject.getString("colour"));
                        item.setOwner(jsonObject.getString("owner"));
                        item.setImage(jsonObject.getString("img"));

                        // switch case to add based on current category selected
                        switch(currentCategory) {
                            case "favourites":
                                currentItemList.add(item);
                                break;
                            case "sneaker":
                                if(item.getType().equals("sneakers")) {
                                    currentItemList.add(item);
                                } else {
                                    break;
                                }
                            case "formal":
                                if(item.getType().equals("formal")) {
                                    currentItemList.add(item);
                                } else {
                                    break;
                                }
                            case "slipper":
                                if(item.getType().equals("slippers")) {
                                    currentItemList.add(item);
                                } else {
                                    break;
                                }
                            default:
                                break;
                        }

                    }

                }

                itemList.clear(); // empty itemList before putting items in it
                itemList.addAll(currentItemList);
                adapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
//                    progressDialog.dismiss();
            }
//                progressDialog.dismiss();
        }, error -> {
            Log.e("Volley",error.toString());
//                progressDialog.dismiss();
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);

    }


    // Method to mock database, while testing offline
    private void getDataLocal() {
        itemList.clear();
        itemList.add(
                new ListItem(
                        1, 1,
                        "sneaker",
                        "white",
                        "Johny",
                        "white_sneakers_m02"));


        itemList.add(
                new ListItem(
                        2, 1,
                        "sneaker",
                        "black",
                        "Chrissy",
                        "black_sneakers_m04"));

        itemList.add(
                new ListItem(
                        3, 1,
                        "sneaker",
                        "grey",
                        "Teagen",
                        "grey_sneakers_m02"));

        itemList.add(
                new ListItem(
                        1, 1,
                        "sneaker",
                        "red",
                        "Teagen",
                        "grey_sneakers_m02"));

        itemList.add(
                new ListItem(
                        1, 1,
                        "sneaker",
                        "blue",
                        "Teagen",
                        "grey_sneakers_m02"));

        itemList.add(
                new ListItem(
                        1, 1,
                        "sneaker",
                        "grey",
                        "Teagen",
                        "grey_sneakers_m02"));

        itemList.add(
                new ListItem(
                        1, 1,
                        "sneaker",
                        "grey",
                        "Teagen",
                        "grey_sneakers_m02"));
    }

}
