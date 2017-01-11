package mysite.com.dribbbleshow;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mysite.com.dribbbleshow.AppUtils.AppPermission;
import mysite.com.dribbbleshow.AppUtils.FileIO;

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
    private int responseLengthForImageLoad;

    // The life span of and item in the history folder
    private final long SHOT_LIFESPAN_MS = 24 * 3600000; // 24 hours in milliseconds

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
        mAdapter = new AdapterShots(shotList);
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

        checkInternetConnection();
    }

    @Override
    public void onRefresh() {
        loadShotsFromDisk();
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
                    loadShotsFromDisk();
                }
            });
            AppController.getInstance().addToRequestQueue(strReq, "string_req");
        } else {

            new AlertDialog(MainActivity.this, getString(R.string.no_connection));
            mayLoad = true;
            swipeRefreshLayout.setRefreshing(false);

            loadShotsFromDisk();
        }

    }

    public void requestShots(String url) {
        progressBar.setVisibility(View.VISIBLE);
        //Blocking screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        responseLengthForImageLoad = 0;

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

        responseLengthForImageLoad = response.length();
        //counter = 0;
        for (int i = 0; i < responseLengthForImageLoad; i++) {
            try {
                JSONObject item = response.getJSONObject(i);

                long id = item.isNull("id") ? 0L : item.getLong("id");
                String title = item.getString("title");
                String description = item.getString("description");
                Integer height = item.getInt("height");
                Integer width = item.getInt("width");

                JSONObject imagesJSONObj = new JSONObject(item.getString("images"));
                String hidpi = imagesJSONObj.getString("hidpi");
                String normal = imagesJSONObj.getString("normal");
                String teaser = imagesJSONObj.getString("teaser");

                Images images = new Images(hidpi, normal, teaser);

                Shot shot = new Shot(id, title, description, height, width, images);

                imageDownload(shot);

            } catch (JSONException e) {
                e.printStackTrace();
                responseLengthForImageLoad --;
            }
        }
    }

    private void imageDownload(final Shot shot) {
        //Image download
        if (shot.getShotBitmap() == null && shot.getImages() != null) {
            String imgUrl = shot.getImages().getAvailableUrl();
            if (imgUrl != null) {
                ImageLoader imageLoader = AppController.getInstance().getImageLoader();
                imageLoader.get(imgUrl, new ImageLoader.ImageListener() {

                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                        if (response.getBitmap() != null) {
                            shot.setShotBitmap(response.getBitmap());
                            addItemToList(shot);
                            mAdapter.notifyDataSetChanged();
                            saveShotItem(shot);
                            checkEnd();
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        checkEnd();
                    }

                });
            }
        }
    }

    synchronized void checkEnd(){
        responseLengthForImageLoad--;
        if(responseLengthForImageLoad < 1) {

            onStopRequest(null);

            //AppController.getInstance().showToast(MainActivity.this, "Page: "+(page-1) +"\n from server has finished");
        }
    }

    private void onStopRequest(String msg){
        if(msg!=null) {
            new AlertDialog(MainActivity.this, msg);
        }
        swipeRefreshLayout.setRefreshing(false);
        mayLoad = true;
        progressBar.setVisibility(View.GONE);

        //Unblocking screen
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }


    // load shot items from the Shots folder
    public void loadShotsFromDisk() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog(this, "Reading is DENIED");
            return;
        }

        List<File> files = FileIO.GetFiles(FileIO.GetShotsSubfolder());
        int all = files.size();
        int start = (pageDisk - 1) * perPage;
        int limit = perPage * pageDisk;

        if ((all > 0 && (all >= start))) {
            if (all < limit) {
                limit = all;
            }
            AppController.getInstance().showToast(MainActivity.this, getString(R.string.load_from_disk) + pageDisk);
            Shot shot;
            long currentTime;
            for (int i = start; i < limit; i++) {
                File file = files.get(i);
                currentTime = System.currentTimeMillis();
                if ((currentTime - file.lastModified()) < SHOT_LIFESPAN_MS) {
                    shot = getShotItemFromFile(file.getAbsolutePath());
                    if (null != shot) {
                        addItemToList(shot);
                    }
                } else {
                    // file has expired, remove from history
                    FileIO.Delete(FileIO.GetShotsSubfolder(), file.getName());
                }
            }

            pageDisk++;
        } else {
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

    @Nullable
    private Shot getShotItemFromFile(String file) {
        Shot shot = new Shot();
        shot.setFile(file);
        if (shot.deserialize()) {
            return shot;
        }
        return null;
    }

    private boolean saveShotItem(Shot shot) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {

            FileIO.CreateSubfolder(FileIO.GetShotsSubfolder());
            //shot.setFile(FileIO.GetShotsSubfolder() + "/" + shot.getId()+".shot");
            shot.setFile(FileIO.GetShotsSubfolder() + "/" + shot.hashCode() + ".shot");
            return shot.serialize();

        }
        return false;
    }

}
