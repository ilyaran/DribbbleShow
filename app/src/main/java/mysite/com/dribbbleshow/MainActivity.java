package mysite.com.dribbbleshow;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mysite.com.dribbbleshow.AppUtils.AppPermission;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final int perPage = 8;
    private String url = "https://api.dribbble.com/v1/shots?per_page=" + perPage + "&list=attachments&list=debuts&list=playoffs&list=rebounds&list=teams&sort=recent&page=";
    private List<Shot> shotList = new ArrayList<>();
    private RecyclerView recyclerView;
    private AdapterShots mAdapter;
    private int page = 1;
    private int pageDisk = 1;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean mayLoad = true;
    private LinearLayout progressBar;
    List<Shot> fromRealm;

    private AppPermission permission;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        permission.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = new AppPermission(this, 1234);
            permission.askPermissions();
        }

        progressBar = (LinearLayout) findViewById(R.id.progressBar);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new AdapterShots(this,shotList);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        gridLayoutManager.setSpanCount(1);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) { //check for scroll down

                    int visibleItemCount = gridLayoutManager.getChildCount();
                    int totalItemCount = gridLayoutManager.getItemCount();
                    int pastVisiblesItems = gridLayoutManager.findFirstVisibleItemPosition();

                    if (mayLoad) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {

                            mayLoad = false;
                            swipeRefreshLayout.setRefreshing(true);

                            checkInternetConnection();
                        }
                    }
                }
            }
        });

        AppController.getInstance().deleteAll();
        fromRealm = AppController.getInstance().findAll();

        checkInternetConnection();
    }

    @Override
    public void onRefresh() {
        loadShotsFromRealm();
    }

    public void checkInternetConnection() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog(this, "Internet DENIED");
            return;
        }
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            StringRequest strReq = new StringRequest(Request.Method.GET,
                    "http://www.google.com/", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    requestShots(url + page);
                    AppController.getInstance().showToast(MainActivity.this, getString(R.string.load_from_server) + page);
                    page++;
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    mayLoad = true;
                    swipeRefreshLayout.setRefreshing(false);

                    new AlertDialog(MainActivity.this, getString(R.string.no_connection));
                    loadShotsFromRealm();
                }
            });
            AppController.getInstance().addToRequestQueue(strReq, "string_req");
        } else {

            new AlertDialog(MainActivity.this, getString(R.string.no_connection));
            mayLoad = true;
            swipeRefreshLayout.setRefreshing(false);

            loadShotsFromRealm();
        }

    }

    public void requestShots(String url) {
        progressBar.setVisibility(View.VISIBLE);
        //Blocking screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray jsonArray;
                        try {
                            jsonArray = new JSONArray(response);
                            getItems(jsonArray);
                        } catch (JSONException e) {
                            onStopRequest("Error");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onStopRequest("Error");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer bc3b4c9cc1dedb6598585d845b417541464ea447f4bd66a486dc1f32566c9c0e");
                return headers;
            }
        };

        AppController.getInstance().addToRequestQueue(stringRequest, "string_req");

    }

    private void getItems(JSONArray response) {

        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject item = response.getJSONObject(i);

                long id = item.isNull("id") ? 0L : item.getLong("id");
                String title = item.getString("title");
                String description = item.getString("description");

                JSONObject imagesJSONObj = new JSONObject(item.getString("images"));
                String hidpi = imagesJSONObj.getString("hidpi");
                String normal = imagesJSONObj.getString("normal");
                String teaser = imagesJSONObj.getString("teaser");

                Shot shot = new Shot(id, title, description, hidpi, normal, teaser);

                shotList.add(shot);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        mAdapter.notifyDataSetChanged();
        onStopRequest(null);
    }

    private void onStopRequest(String msg){
        //Unblocking screen
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        if(msg!=null) {
            new AlertDialog(MainActivity.this, msg);
        }
        swipeRefreshLayout.setRefreshing(false);
        mayLoad = true;
        progressBar.setVisibility(View.GONE);

        AppController.getInstance().addList(shotList);
    }

    // load shot items from the Shots folder
    public void loadShotsFromRealm() {

        int all = fromRealm.size();
        int start = (pageDisk - 1) * perPage;
        int limit = perPage * pageDisk;
        if ((all > 0 && (all >= start))) {
            if (all < limit) {
                limit = all;
            }
            AppController.getInstance().showToast(MainActivity.this, getString(R.string.load_from_disk) + pageDisk);
            for (int i = start; i < limit; i++) {
                addItemToList(fromRealm.get(i));
            }
            pageDisk++;
        }else {
            AppController.getInstance().showToast(MainActivity.this, getString(R.string.end_disk));
        }
        swipeRefreshLayout.setRefreshing(false);

    }

    private void addItemToList(Shot shot) {
        if (shotList.isEmpty()) {
            shotList.add(shot);
        } else {
            shotList.add(0, shot); // insert before the first
        }
        mAdapter.notifyItemInserted(0);
    }

    @Override
    protected void onStop() {
        AppController.getInstance().realm.close();
        super.onStop();
    }


}
