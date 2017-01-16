package mysite.com.dribbbleshow.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.List;

import mysite.com.dribbbleshow.R;
import mysite.com.dribbbleshow.model.dto.ShotDTO;

public class ShotsAdapter extends RecyclerView.Adapter<ShotsAdapter.HolderShot> {

    Context mContext;
    private List<ShotDTO> shotList;

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

    public ShotsAdapter(Context context, List<ShotDTO> shotList) {
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
        final ShotDTO shot = shotList.get(position);
        if (shot.getImages() != null) {
            String imgUrl = shot.getImages().getAvailableUrl();
            if (imgUrl != null) {

                if (shot.getTitle() != null) {
                    holder.title.setText(shot.getTitle());
                }

                if (shot.getDescription() != null) {
                    holder.description.setText(fromHtml(shot.getDescription()));
                }

                Glide.with(mContext)
                        .load(imgUrl)
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                holder.shotImageView.setImageBitmap(resource);
                            }
                        });

            }
        }
    }

    @Override
    public int getItemCount() {
        return shotList.size();
    }
}
