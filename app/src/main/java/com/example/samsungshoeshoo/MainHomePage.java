package com.example.samsungshoeshoo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainHomePage extends AppCompatActivity {

    // Mock http from laptop
    private String url="http://10.12.156.149:8100/shoes";
    private String postUrl="http://10.12.156.149:8100/location";

    // urls from RPI
//    private String url="http://10.12.0.19:8100/shoes"; // raspberry pi url
//    private String postUrl="http://10.12.0.19:8100/location";

    private RecyclerView favouritesRecyclerView;
    private MyAdapter favouritesAdapter;
    private List<ListItem> favouritesItemList;

    private RecyclerView extraRecyclerView;
    private MyAdapter extraAdapter;
    private List<ListItem> extraItemList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // gets rid of top notification bar
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.homepage_main);

        // Build pull down to refresh view
        SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefreshHome);

        // Add different colours to refresh loading
        pullToRefresh.setColorSchemeResources(android.R.color.holo_blue_dark,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // pull to refresh listener
        pullToRefresh.setOnRefreshListener(() -> {
            updatePage();
            pullToRefresh.setRefreshing(false);
        });

        // Pop up progress dialog to show loading database
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Database...");
        progressDialog.show();
        // Method called to build recycler view on opening application
        buildRecyclerViews();
        progressDialog.dismiss();

        // on click listeners for deploy buttons
        favouritesAdapter.setOnItemClickListener(position -> deployItem(position, favouritesAdapter, favouritesItemList));
        extraAdapter.setOnItemClickListener(position -> deployItem(position, extraAdapter, extraItemList));
    }

    public void deployItem(int position, MyAdapter adapter, List<ListItem> itemList) {
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

    private void updatePage() {
        getDataLocal(favouritesItemList);
        getDataLocal(extraItemList);
        favouritesAdapter.notifyDataSetChanged();
        extraAdapter.notifyDataSetChanged();
    }

    public void buildRecyclerViews() {
        //**
        //
        // getting recycler view from xml
        //**
        favouritesRecyclerView = findViewById(R.id.favouritesRecyclerView);
        favouritesRecyclerView.setHasFixedSize(true);
        extraRecyclerView = findViewById(R.id.extraRecyclerView);
        extraRecyclerView.setHasFixedSize(true);

        // creating and setting horizontal linear layout managers for the recycler view
        favouritesRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        extraRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));

        // initialize item list
        favouritesItemList = new ArrayList<>();
        extraItemList = new ArrayList<>();

        //creating recycler view adapter
        favouritesAdapter = new MyAdapter(this, favouritesItemList);
        extraAdapter = new MyAdapter(this, extraItemList);

        // Get Database and update adapter
        updatePage();

        //setting adapter to recycler
        favouritesRecyclerView.setAdapter(favouritesAdapter);
        extraRecyclerView.setAdapter(extraAdapter);
    }

    // Method to mock database, while testing offline
    private void getDataLocal(List<ListItem> itemList) {
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
