package mysite.com.dribbbleshow;


public class Images {

    private String hidpi;
    private String normal;
    private String teaser;

    public Images(String hidpi, String normal, String teaser) {
        this.hidpi = hidpi;
        this.normal = normal;
        this.teaser = teaser;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Images)) return false;

        Images images = (Images) o;

        if (getHidpi() != null ? !getHidpi().equals(images.getHidpi()) : images.getHidpi() != null)
            return false;
        if (getNormal() != null ? !getNormal().equals(images.getNormal()) : images.getNormal() != null)
            return false;
        return getTeaser() != null ? getTeaser().equals(images.getTeaser()) : images.getTeaser() == null;

    }

    @Override
    public int hashCode() {
        int result = getHidpi() != null ? getHidpi().hashCode() : 0;
        result = 31 * result + (getNormal() != null ? getNormal().hashCode() : 0);
        result = 31 * result + (getTeaser() != null ? getTeaser().hashCode() : 0);
        return result;
    }

    public String getAvailableUrl() {
        if(hidpi!=null){
            return hidpi;
        }else if(normal!=null){
            return normal;
        }
        return teaser;
    }
}
