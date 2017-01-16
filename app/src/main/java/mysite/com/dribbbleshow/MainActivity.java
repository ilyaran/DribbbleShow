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

import java.util.ArrayList;
import java.util.List;

import mysite.com.dribbbleshow.model.api.ApiClient;
import mysite.com.dribbbleshow.model.api.ApiInterface;
import mysite.com.dribbbleshow.model.dto.ShotDTO;
import mysite.com.dribbbleshow.other.AppPermission;
import mysite.com.dribbbleshow.adapter.ShotsAdapter;
import mysite.com.dribbbleshow.other.AlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final int perPage = 8;

    private static int page = 1;
    private static int pageCache = 1;

    private static final String url = "https://api.dribbble.com/v1/shots?per_page=" + perPage + "&list=attachments&list=debuts&list=playoffs&list=rebounds&list=teams&sort=recent&page=";
    private List<ShotDTO> shotList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ShotsAdapter mAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;
    private static boolean mayLoad = true;
    private LinearLayout progressBar;
    List<ShotDTO> fromRealm;
    //OkHttpClient okHttpClient;
    ApiInterface apiService;
    Call<List<ShotDTO>> call;

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
        mAdapter = new ShotsAdapter(this, shotList);
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

        apiService = ApiClient.getClientDribbble().create(ApiInterface.class);

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

            request();

            AppController.getInstance().showToast(MainActivity.this, getString(R.string.load_from_server) + page);

            page++;

        } else {
            mayLoad = true;
            swipeRefreshLayout.setRefreshing(false);

            new AlertDialog(MainActivity.this, getString(R.string.no_connection));
            loadShotsFromRealm();
        }

    }

    private void request() {
        progressBar.setVisibility(View.VISIBLE);
        //Blocking screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        call = apiService.getShots(perPage, page);
        call.enqueue(new Callback<List<ShotDTO>>() {
            @Override
            public void onResponse(Call<List<ShotDTO>> call, Response<List<ShotDTO>> response) {
                if (response.isSuccessful()) {
                    List<ShotDTO> responseShotList = response.body();

                    if (responseShotList != null && !responseShotList.isEmpty()) {

                        mayLoad = true;

                        swipeRefreshLayout.setRefreshing(false);

                        shotList.addAll(responseShotList);

                        mAdapter.notifyDataSetChanged();

                        onStopRequest(null);

                    }
                } else {
                    onStopRequest("Error");
                }
            }

            @Override
            public void onFailure(Call<List<ShotDTO>> call, Throwable t) {
                t.printStackTrace();
                onStopRequest("Error");
            }
        });
    }


    /*public void checkInternetConnection() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog(this, "Internet DENIED");
            return;
        }
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {

            Request request = new Request.Builder()
                    .url("http://www.google.com/")
                    .build();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mayLoad = true;
                    swipeRefreshLayout.setRefreshing(false);

                    new AlertDialog(MainActivity.this, getString(R.string.no_connection));
                    loadShotsFromRealm();
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }else {
                        // Read data on the worker thread
                        final String responseData = response.body().string();

                        // Run view-related code back on the main thread
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                requestShots(url + page);
                                AppController.getInstance().showToast(MainActivity.this, getString(R.string.load_from_server) + page);
                                page++;
                            }
                        });
                    }
                }
            });

        } else {
            mayLoad = true;
            swipeRefreshLayout.setRefreshing(false);

            new AlertDialog(MainActivity.this, getString(R.string.no_connection));
            loadShotsFromRealm();
        }

    }

    public void requestShots(String url) {
        progressBar.setVisibility(View.VISIBLE);
        //Blocking screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        Request request = new Request.Builder()
                .header("Authorization", "Bearer bc3b4c9cc1dedb6598585d845b417541464ea447f4bd66a486dc1f32566c9c0e")
                .url(url)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onStopRequest("Error");
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }else {

                    // Read data on the worker thread
                    final String responseData = response.body().string();

                    // Run view-related code back on the main thread
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONArray jsonArray;
                            try {
                                jsonArray = new JSONArray(responseData);
                                getItems(jsonArray);
                            } catch (JSONException e) {
                                onStopRequest("Error");
                            }
                        }
                    });
                }
            }
        });

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
    }*/

    private void onStopRequest(String msg) {
        //Unblocking screen
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        if (msg != null) {
            new AlertDialog(MainActivity.this, msg);
        }
        swipeRefreshLayout.setRefreshing(false);
        mayLoad = true;
        progressBar.setVisibility(View.GONE);

        //save on DB
        AppController.getInstance().addList(shotList);
    }

    // load shot items from the Shots folder
    public void loadShotsFromRealm() {

        int all = fromRealm.size();
        int start = (pageCache - 1) * perPage;
        int limit = perPage * pageCache;
        if ((all > 0 && (all > start))) {
            if (all < limit) {
                limit = all;
            }
            AppController.getInstance().showToast(MainActivity.this, getString(R.string.load_from_disk) + pageCache);
            for (int i = start; i < limit; i++) {
                addItemToList(fromRealm.get(i));
            }
            pageCache++;
        } else {
            AppController.getInstance().showToast(MainActivity.this, getString(R.string.end_disk));
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private void addItemToList(ShotDTO shot) {
        if (shotList.isEmpty()) {
            shotList.add(shot);
        } else {
            shotList.add(0, shot); // insert before the first
        }
        mAdapter.notifyItemInserted(0);
    }
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
        //realm.close();
    }



}
