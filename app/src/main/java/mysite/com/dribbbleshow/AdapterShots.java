package mysite.com.dribbbleshow;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class AdapterShots extends RecyclerView.Adapter<AdapterShots.HolderShot> {

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

    public AdapterShots(List<Shot> shotList) {
        this.shotList = shotList;
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

        if (shot.getShotBitmap() != null) {
            if (shot.getTitle() != null) {
                holder.title.setText(shot.getTitle());
            }
            if (shot.getDescription() != null) {
                holder.description.setText(fromHtml(shot.getDescription()));
            }

            holder.shotImageView.setImageBitmap(shot.getShotBitmap());
        }

    }

    @Override
    public int getItemCount() {
        return shotList.size();
    }
}
