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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CategoryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    // Mock http from laptop
//    private String url="http://10.12.156.149:8100/shoes";
//    private String postUrl="http://10.12.156.149:8100/location";

    // urls from RPI
    private String url="http://10.12.0.18:8100/shoes"; // raspberry pi url
    private String postUrl="http://10.12.0.18:8100/location";
    private RecyclerView recyclerView;
    private MyAdapter adapter;

    private List<ListItem> itemList;

    private String currentCategory = "Favourites";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // getting bundle from previous activity
        Bundle bundle = getIntent().getExtras();
        currentCategory = bundle.getString("category");

        // Set up items that need to be found by ID
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        toolbar.setSubtitle("");

        //TODO: Implement colour sorting through floating action button
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

        // Action Bar Drawer toggle for side pull
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
        adapter.setOnItemClickListener(position -> deployItem(position, "deploy"));
        adapter.setOnDeleteClickListener(position -> deployItem(position, "delete"));
    }

    public void deployItem(int position, String action) {
        // Send Post Data
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deploying Shoe...");
        progressDialog.show();

        JSONObject postData = new JSONObject();
//        String deployResponse;
        try {

            if (action.equals("deploy")) {
                // PUT postData required to be received by server in JSON Format
                postData.put("shelfIndex", itemList.get(position).getShelfId());
                postData.put("deploy", true);
            } else if (action.equals("delete")) {
                // PUT postData required to be received by server in JSON Format
                postData.put("shelfIndex", itemList.get(position).getShelfId());
                postData.put("deploy", false);
            }

            // execute async Task to send post data to http server as JSONObject in string format
            SendDeviceDetails sDD = new SendDeviceDetails();
            JSONObject jsonResponse = new JSONObject(sDD.execute(postUrl, postData.toString()).get());
            String deployResponse = String.valueOf(jsonResponse.getBoolean("success"));
            progressDialog.dismiss();

            if (deployResponse.equals("true")) {
                if (action.equals("deploy")) {
                    Toast.makeText(this, "Shoe being deployed...", Toast.LENGTH_SHORT).show();
                } else if (action.equals("delete")) {
                    Toast.makeText(this, "Shoe removed from database...", Toast.LENGTH_SHORT).show();
                }
                itemList.remove(position);
                adapter.notifyItemRemoved(position);
            } else if (deployResponse.equals("false")) {
                Toast.makeText(this, "Error, please try again later", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog.dismiss();
        } catch (InterruptedException e) {
            e.printStackTrace();
            progressDialog.dismiss();
        } catch (ExecutionException e) {
            e.printStackTrace();
            progressDialog.dismiss();
        }
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
        final TextView currentCategoryTV = findViewById(R.id.categoryTextView);
        currentCategoryTV.setText("Your " + currentCategory); // change title on page
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

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    // Listener for navigation items
//    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_favourites) {
            currentCategory = "Favourites";
        } else if (id == R.id.nav_sneakers) {
            currentCategory = "Sneakers";
        } else if (id == R.id.nav_formal) {
            currentCategory = "Formals";
        } else if (id == R.id.nav_slippers) {
            currentCategory = "Slippers";
        } else if (id == R.id.nav_sandals) {
            currentCategory = "Sandals";
        }
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }
        updateRecyclerView();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // method to parse Data and attach to List
    private void getData(){

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
                        item.setDate(MainHomePage.daysAgo(jsonObject.getString("timeStored")));

                        // switch case to add based on current category selected
                        switch(currentCategory) {
                            case "Favourites":
                                currentItemList.add(item);
                                Collections.sort(
                                        currentItemList,
                                        (item1, item2) -> item1.getDate() - item2.getDate());
                                break;
                            case "Sneakers":
                                if(item.getType().equals("sneakers")) {
                                    currentItemList.add(item);
                                } else {
                                    break;
                                }
                            case "Formals":
                                if(item.getType().equals("formal")) {
                                    currentItemList.add(item);
                                } else {
                                    break;
                                }
                            case "Slippers":
                                if(item.getType().equals("slippers")) {
                                    currentItemList.add(item);
                                } else {
                                    break;
                                }
                            case "Sandals":
                                if(item.getType().equals("sandals")) {
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
            }
        }, error -> {
            Log.e("Volley",error.toString());
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
                        "white_sneakers_m02",
                        MainHomePage.daysAgo("Tue Jul 23 13:30:42 2019")));


        itemList.add(
                new ListItem(
                        2, 1,
                        "sneaker",
                        "black",
                        "Chrissy",
                        "black_sneakers_m04",
                        MainHomePage.daysAgo("Thu Jul 25 13:30:42 2019")));

        itemList.add(
                new ListItem(
                        3, 1,
                        "sneaker",
                        "grey",
                        "Teagen",
                        "grey_sneakers_m02",
                        MainHomePage.daysAgo("Thu Jul 25 13:30:42 2019")));
    }

}

