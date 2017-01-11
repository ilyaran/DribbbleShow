package mysite.com.dribbbleshow;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;


public class Shot {

    public static final String TAG = AppController.class.getSimpleName();

    private Bitmap shotBitmap = null;
    private static final long serialVersionUID = 1L;
    private static final int NO_IMAGE = -1;
    private String mStorageFile = null;
    private File mFile = null;

    private long id;
    private String title;
    private String description;
    private Integer height;
    private Integer width;

    private Images images;

    public Shot() {}

    public Shot(long id, String title, String description, Integer height, Integer width, Images images) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.height = height;
        this.width = width;
        this.images = images;
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

    public Integer getHeight() {
        return height;
    }

    public Integer getWidth() {
        return width;
    }

    public Images getImages() {
        return images;
    }

    public Bitmap getShotBitmap() {
        return shotBitmap;
    }

    public void setShotBitmap(Bitmap shotBitmap) {
        this.shotBitmap = shotBitmap;
    }

    public void setFile(String file) {
        mStorageFile = file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shot)) return false;

        Shot shot = (Shot) o;

        if (getId() != shot.getId()) return false;
        if (getTitle() != null ? !getTitle().equals(shot.getTitle()) : shot.getTitle() != null)
            return false;
        if (getDescription() != null ? !getDescription().equals(shot.getDescription()) : shot.getDescription() != null)
            return false;
        if (getHeight() != null ? !getHeight().equals(shot.getHeight()) : shot.getHeight() != null)
            return false;
        if (getWidth() != null ? !getWidth().equals(shot.getWidth()) : shot.getWidth() != null)
            return false;
        return getImages() != null ? getImages().equals(shot.getImages()) : shot.getImages() == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (getId() ^ (getId() >>> 32));
        result = 31 * result + (getTitle() != null ? getTitle().hashCode() : 0);
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        result = 31 * result + (getHeight() != null ? getHeight().hashCode() : 0);
        result = 31 * result + (getWidth() != null ? getWidth().hashCode() : 0);
        result = 31 * result + (getImages() != null ? getImages().hashCode() : 0);
        return result;
    }

    public boolean serialize() {
        boolean result = true;
        mFile = new File(mStorageFile);
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(mFile));
            this.writeObject(oos); // write the class as an 'object'
            oos.flush(); // flush the stream to insure all of the information was written to 'save.bin'
            oos.close();// close the stream

        } catch (FileNotFoundException e) {
            result = false;
            e.printStackTrace();
            Log.e(TAG, String.valueOf(e));
        } catch (IOException e) {
            result = false;
            e.printStackTrace();
            Log.e(TAG, String.valueOf(e));
        }
        return result;
    }

    public boolean deserialize() {
        boolean result = true;
        mFile = new File(mStorageFile);
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(new FileInputStream(mFile));
            this.readObject(ois);
            ois.close();
        } catch (StreamCorruptedException e) {
            result = false;
            e.printStackTrace();
            Log.e(TAG, String.valueOf(e));
        } catch (FileNotFoundException e) {
            result = false;
            e.printStackTrace();
            Log.e(TAG, String.valueOf(e));
        } catch (IOException e) {
            // EOF Exception is handled here too.  No need to print stack.
            result = false;
            Log.e(TAG, String.valueOf(e));
        } catch (ClassNotFoundException e) {
            result = false;
            e.printStackTrace();
            Log.e(TAG, String.valueOf(e));
        }
        return result;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();

        byte[] ff;
        // write id
        ff = String.valueOf(id).getBytes();
        out.writeInt(ff.length);
        out.write(ff);

        // write title
        out.writeInt(title.getBytes().length);
        out.write(title.getBytes());

        // write description
        ff = description.getBytes();
        out.writeInt(ff.length);
        out.write(ff);

        if (shotBitmap != null) {
            shotBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            final byte[] imageByteArray = stream.toByteArray();
            out.writeInt(imageByteArray.length);
            out.write(imageByteArray);
        } else {
            out.writeInt(NO_IMAGE);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {

        int length;
        byte[] bytes;

        // construct the id
        length = in.readInt();
        if (length > 0) {
            bytes = new byte[length];
            in.readFully(bytes);
            id = Integer.valueOf(new String(bytes));
        } else {
            id = 0;
        }

        // construct the title
        length = in.readInt();
        if (length > 0) {
            bytes = new byte[length];
            in.readFully(bytes);
            title = new String(bytes);

        } else {
            title = "";
        }

        // construct the description
        length = in.readInt();
        if (length > 0) {
            bytes = new byte[length];
            in.readFully(bytes);
            description = new String(bytes);
        } else {
            description = "";
        }

        // construct the bitmap
        length = in.readInt();
        if (length > NO_IMAGE+1) {
            try {
                final byte[] imageByteArray = new byte[length];
                in.readFully(imageByteArray);
                shotBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, length);
            } catch (OutOfMemoryError e) {
                shotBitmap = null;
            }
        }
    }
}
