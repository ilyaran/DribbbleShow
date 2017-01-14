package mysite.com.dribbbleshow;


import android.app.Activity;
import android.app.Application;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

public class AppController extends Application {

    public static final String TAG = AppController.class.getSimpleName();

    public static String ROOT_SUBFOLDER = "DribbbleRootSubfolder";
    public static String SHOTS_SUBFOLDER = "Shots";
    // The life span of and item in the history folder
    public static final long SHOT_LIFESPAN_MS = 24 * 3600000; // 24 hours in milliseconds

    private static AppController mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    Realm realm;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        // Realm DB
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().build();
        Realm.deleteRealm(config);
        Realm.setDefaultConfiguration(config);

        realm = Realm.getDefaultInstance();
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public void addList(final List<Shot> shotList) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for(Shot shot : shotList) {
                    realm.copyToRealm(shot);
                }
            }
        });
    }

    public List<Shot> findAll() {
        Realm realm = Realm.getDefaultInstance();
        long currentTime = System.currentTimeMillis();
        final RealmResults<Shot> shotList = realm.where(Shot.class)
                .findAllSorted("created", Sort.DESCENDING);
        return shotList;
    }

    public void deleteAll() {
        long currentTime = System.currentTimeMillis();

        final RealmResults<Shot> shotList = realm.where(Shot.class)
                .lessThan("created", currentTime - SHOT_LIFESPAN_MS).findAll();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                shotList.deleteAllFromRealm();
            }
        });
    }

    // Show Custom Toast
    public void showToast(Activity context, String msg) {
        LayoutInflater inflater =
                (LayoutInflater)
                        context.getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) context.findViewById(R.id.custom_toast_container));

        TextView text = (TextView) layout.findViewById(R.id.toastTextView);
        text.setText(msg);

        Toast toast = new Toast(context.getApplicationContext());
        //toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    //**************** Volley
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }


    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.mRequestQueue,
                    new LruBitmapCache());
        }
        return this.mImageLoader;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }


}
