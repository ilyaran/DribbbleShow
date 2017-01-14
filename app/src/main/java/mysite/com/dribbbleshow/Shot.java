package mysite.com.dribbbleshow;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class Shot extends RealmObject {

    @Ignore public static final String TAG = AppController.class.getSimpleName();

    private long id;
    private String title;
    private String description;
    private String hidpi;
    private String normal;
    private String teaser;
    private long created;

    public Shot() {}

    public Shot(long id, String title, String description, String hidpi, String normal, String teaser) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.hidpi = hidpi;
        this.normal = normal;
        this.teaser = teaser;
        this.created = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getHidpi() {
        return hidpi;
    }

    public String getNormal() {
        return normal;
    }

    public String getTeaser() {
        return teaser;
    }

    public String getAvailableUrl() {
        if(hidpi!=null){
            return hidpi;
        }else if(normal!=null){
            return normal;
        }
        return teaser;
    }


    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTeaser(String teaser) {
        this.teaser = teaser;
    }

    public void setNormal(String normal) {
        this.normal = normal;
    }

    public void setHidpi(String hidpi) {
        this.hidpi = hidpi;
    }

    public long getCreated() {
        return created;
    }


}
