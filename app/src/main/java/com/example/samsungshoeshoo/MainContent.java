//package com.example.samsungshoeshoo;
//
//import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class MainContent extends AppCompatActivity {
//
//    private RecyclerView recyclerView;
//    MyAdapter adapter;
//
//    private List<ListItem> itemList;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.content_main);
//
//        // getting recycler view from xml
//        recyclerView = findViewById(R.id.recyclerView);
//        recyclerView.setHasFixedSize(true);
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        // initialize item list
//        itemList = new ArrayList<>();
//
//
//        // add items to list, to be changed to add based on JSON
//        itemList.add(
//                new ListItem(
//                        1,
//                        "Old Skool Vans",
//                        "White Sneaker",
//                        4.5,
//                        80,
//                        R.drawable.white_sneakers_m02));
//
//        itemList.add(
//                new ListItem(
//                        1,
//                        "Converse Chuck Taylor",
//                        "Black Sneaker",
//                        4.7,
//                        75,
//                        R.drawable.black_sneakers_m04));
//
//        itemList.add(
//                new ListItem(
//                        1,
//                        "Nike Shoe",
//                        "Grey Sneaker",
//                        3.2,
//                        60,
//                        R.drawable.grey_sneakers_m02));
//
//        //creating recycler view adapter
//        MyAdapter adapter = new MyAdapter(this, itemList);
//
//        //setting adapter to recycler
//        recyclerView.setAdapter(adapter);
//
//    }
//
//}
