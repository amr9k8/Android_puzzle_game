package dragosholban.com.androidpuzzlegame;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Set;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private AssetManager am;
    private String[] files;
    private String [] unlocked_images;

    public ImageAdapter(Context c,String [] unlocked_images) {
        mContext = c;
        this.unlocked_images = unlocked_images;
        am = mContext.getAssets();
        try {
            files  = am.list("img");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getCount() {
        return files.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.grid_element, null);
        }

        final ImageView imageView = convertView.findViewById(R.id.gridImageview);
        TextView img_statues = convertView.findViewById(R.id.btnedit);
        imageView.setImageBitmap(null);
        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String getText = (String) img_statues.getText();

                if(getText.equals("Unlock")){

                    AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                    alertDialog.setTitle("Are you sure you want to unlock this puzzle with  1000  ? \n");

                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    SharedPreferences sharedPrefPoints= mContext.getSharedPreferences("Points", 0);
                                    Long number = sharedPrefPoints.getLong("rewards", 0);
                                    Toast.makeText(mContext, Long.toString(number), Toast.LENGTH_SHORT).show();
                                    SharedPreferences.Editor  editor1= sharedPrefPoints.edit();
                                    editor1.putLong("rewards",number-1000);
                                    editor1.commit();
                                    SharedPreferences sharedPrefPuzzle = mContext.getSharedPreferences("Unlocked",Context.MODE_PRIVATE);
                                    Set<String> new_st = sharedPrefPuzzle.getStringSet("unlocked_images",null);
                                    new_st.add(files[position % files.length]);
                                    SharedPreferences.Editor  editorr= sharedPrefPuzzle.edit();
                                    editorr.clear();
                                    editorr.putStringSet("unlocked_images",new_st);
                                    editorr.commit();
                                    //saved_images.
                                    dialog.dismiss();
                                    Intent intent = new Intent(mContext.getApplicationContext(), MainActivity.class);
                                    mContext.startActivity(intent);

                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(mContext, "No is pressed", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }else{
                    Intent intent = new Intent(mContext.getApplicationContext(), LevelSelectionActivity.class);
                    intent.putExtra("assetName", files[position % files.length]);
                    mContext.startActivity(intent);
                }
            }});

            // run image related code after the view was laid out
        imageView.post(new Runnable() {
            @Override
            public void run() {
                new AsyncTask<Void, Void, Void>() {
                    private Bitmap bitmap;
                    @Override
                    protected Void doInBackground(Void... voids) {
                        bitmap = getPicFromAsset(imageView, files[position]);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        imageView.setImageBitmap(bitmap);
                        //Toast.makeText(mContext, files[position], Toast.LENGTH_SHORT).show();
                        if (Arrays.asList(unlocked_images).contains(files[position]))
                        {   img_statues.setText("Play");
                            img_statues.setTextColor(mContext.getResources().getColor(R.color.white));
                            img_statues.setBackgroundColor(mContext.getResources().getColor(R.color.purple_500));
                        }
                        else
                        {
                            img_statues.setText("Unlock");
                            img_statues.setTextColor(mContext.getResources().getColor(R.color.Tomato));
                            img_statues.setBackgroundColor(mContext.getResources().getColor(R.color.black));
                        }
//
//                        if(files[position].equals("A11.jpg") ||files[position].equals("A12.jpg") )
//                        editbtn.setText("Unlock");


                    }
                }.execute();
            }
        });

        return convertView;
    }

    private Bitmap getPicFromAsset(ImageView imageView, String assetName) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        if(targetW == 0 || targetH == 0) {
            // view has no dimensions set
            return null;
        }

        try {
            InputStream is = am.open("img/" + assetName);
            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, new Rect(-1, -1, -1, -1), bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

            is.reset();

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            return BitmapFactory.decodeStream(is, new Rect(-1, -1, -1, -1), bmOptions);
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }
}