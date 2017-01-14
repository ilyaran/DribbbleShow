package mysite.com.dribbbleshow;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class AdapterShots extends RecyclerView.Adapter<AdapterShots.HolderShot> {

    Context mContext;
    private List<Shot> shotList;

    public class HolderShot extends RecyclerView.ViewHolder {
        public ImageView shotImageView;
        public TextView title, description;

        public HolderShot(View view) {
            super(view);
            shotImageView = (ImageView) view.findViewById(R.id.shotImageView);
            title = (TextView) view.findViewById(R.id.title);
            description = (TextView) view.findViewById(R.id.description);
        }
    }

    public AdapterShots(Context context, List<Shot> shotList) {
        this.shotList = shotList;
        mContext = context;
    }

    @Override
    public HolderShot onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_view_shot, parent, false);

        return new HolderShot(itemView);
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    @Override
    public void onBindViewHolder(final HolderShot holder, int position) {
        final Shot shot = shotList.get(position);

        String imgUrl = shot.getAvailableUrl();
        if (imgUrl != null) {

            if (shot.getTitle() != null) {
                holder.title.setText(shot.getTitle());
            }
            if (shot.getDescription() != null) {
                holder.description.setText(fromHtml(shot.getDescription()));
            }

            ImageLoader imageLoader = AppController.getInstance().getImageLoader();
            imageLoader.get(imgUrl, new ImageLoader.ImageListener() {

                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                    if (response.getBitmap() != null) {
                        holder.shotImageView.setImageBitmap(response.getBitmap());
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {}

            });

        }

    }

    @Override
    public int getItemCount() {
        return shotList.size();
    }
}
