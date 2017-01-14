package mysite.com.dribbbleshow;


import android.app.Activity;
import android.app.Application;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

public class AppController extends Application {

    public static final String TAG = AppController.class.getSimpleName();

    // The life span of and item in the history folder
    public static final long SHOT_LIFESPAN_MS = 72*3600000; // milliseconds

    private static AppController mInstance;
    /*private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;*/

    Realm realm;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        // Realm DB
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().build();
        //Realm.deleteRealm(config);
        Realm.setDefaultConfiguration(config);

        realm = Realm.getDefaultInstance();
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    // Realm DB
    public void addList(final List<Shot> shotList) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for(Shot shot : shotList) {
                    realm.copyToRealmOrUpdate(shot);
                }
            }
        });
    }

    public List<Shot> findAll() {
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
    // End Realm DB

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


}
