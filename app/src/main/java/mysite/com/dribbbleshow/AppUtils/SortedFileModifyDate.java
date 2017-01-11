package mysite.com.dribbbleshow.AppUtils;


import java.io.File;

@SuppressWarnings("rawtypes")
public class SortedFileModifyDate implements Comparable {
    private File mFile = null;

    public SortedFileModifyDate(File value) {
        this.mFile = value;
    }

    public File GetFile()
    {
        return(mFile);
    }

    @Override
    public int compareTo(Object o) {
        SortedFileModifyDate obj = (SortedFileModifyDate) o;
        if (obj == null) {
            return -1;
        }
        else if (mFile.lastModified() < obj.mFile.lastModified()) {
            return -1;
        }
        else if (mFile.lastModified() > obj.mFile.lastModified()) {
            return 1;
        }
        else {
            return 0;
        }
    }

    @Override
    public String toString() {
        String result = "";
        if (mFile != null) {
            result = mFile.getName();
        }
        return (result);
    }
}

