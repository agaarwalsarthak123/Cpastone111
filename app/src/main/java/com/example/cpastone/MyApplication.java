package com.example.cpastone;

import static com.example.cpastone.Constants.MAX_BYTES_PDF;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.cpastone.adapters.AdapterPdfAdmin;
import com.example.cpastone.models.ModelPdf;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.zip.DataFormatException;

public class MyApplication extends Application {
    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    public void onCreate() {
        super.onCreate();
    }

    // static method to convert timestamp to proper format
    public static final String formatTimestamp(String timestamp) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));

        String date = DateFormat.format("MM-dd-yyyy", cal).toString();

      return date;
    }

    public static void deleteBook(Context context, String bookId, String bookUrl, String bookTitle) {

        String TAG = "DELETE_BOOK_TAG";

        Log.d(TAG, "deleteBook: Deleting...");
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Deleting "+bookTitle+".....");
        progressDialog.show();

        Log.d(TAG, "deleteBook: Deleting from storage...");
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Deleted from Storage");
                        Log.d(TAG, "onSuccess: Now deleting info from db");
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: Deleted from db too");
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Book Deleted Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure to delete from db due to "+e.getMessage());
                                        progressDialog.dismiss();
                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to delete from storage due to "+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void loadPdfSize(String pdfUrl, String pdfTitle, TextView sizeTv) {
        String TAG = "PDF_SIZE_TAG";
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        double bytes = storageMetadata.getSizeBytes();
                        Log.d(TAG, "onSuccess: "+pdfTitle+" "+bytes);

                        double kb = bytes/1024;
                        double mb = kb/1024;

                        if(mb >= 1){
                            sizeTv.setText(String.format("%.2f", mb)+"MB");
                        }
                        else if(kb >= 1) {
                            sizeTv.setText(String.format("%.2f", kb)+"KB");
                        }
                        else {
                            sizeTv.setText(String.format("%.2f", bytes)+"bytes");

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: "+e.getMessage());
                    }
                });

    }

    public static void loadPdfFromUrlSinglePage(String pdfUrl, String pdfTitle) {
        String TAG = "PDF_LOAD_SINGLE_TAG";
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {

                        Log.d(TAG, "onSuccess: "+pdfTitle+" successfully got the title" );


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed getting file from url due to "+e.getMessage());

                    }
                });
    }

    public static void loadCategory(String categoryId, TextView categoryTv) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category = ""+snapshot.child("category").getValue();
                        categoryTv.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    public static void  incrementBookViewCount(String bookId) {
        // 1. Get book view count
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        if(viewsCount.equals("") || viewsCount.equals("null")) {
                            viewsCount = "0";
                        }
                        // 2. increment views count
                        long newViewsCount = Long.parseLong(viewsCount) +1;
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("viewsCount", newViewsCount);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId)
                                .updateChildren(hashMap);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public  static void downloadBook(Context context, String bookId, String bookTitle, String bookUrl) {
        Log.d(TAG_DOWNLOAD, "downloadBook: downloading book...");
        String nameWithExtension = bookTitle + ".pdf";
        Log.d(TAG_DOWNLOAD, "downloadBook: Name: "+nameWithExtension);

        // progress dialog
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Downloading "+ nameWithExtension +"...");
        progressDialog.show();

        // downloading from firebase
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG_DOWNLOAD, "onSuccess: Book Downloaded");
                        Log.d(TAG_DOWNLOAD, "onSuccess: Saving book...");
                        saveDownloadedBook(context, progressDialog, bytes, nameWithExtension, bookId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG_DOWNLOAD, "onFailure: Failed to download due to"+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, "Failed to download due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private static void saveDownloadedBook(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtension, String bookId) {
        Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Saving downloaded book");
        try {
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadsFolder.mkdirs();

            String filePath = downloadsFolder.getPath() + "/" + nameWithExtension;

            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.write(bytes);
            out.close();

            Toast.makeText(context, "Saved to download Folder", Toast.LENGTH_SHORT).show();
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Saved to download folder");
            progressDialog.dismiss();

            incrementBookDownloadCount(bookId);
        }
        catch (Exception e) {
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Failed saving to Download Folder due to "+e.getMessage());
            Toast.makeText(context, "Failed saving to download folder due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();

        }
    }

    private static void incrementBookDownloadCount(String bookId) {
        Log.d(TAG_DOWNLOAD, "incrementBookDownloadCount: Incrementign Book Download Count");

        // Step 1: Get previous download count
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                        Log.d(TAG_DOWNLOAD, "onDataChange: Downloads COunt: "+downloadsCount);

                        if(downloadsCount.equals("") || downloadsCount.equals("null")) {
                            downloadsCount = "0";
                        }

                        // convert to long and increment 1
                        long newDownloadsCount = Long.parseLong(downloadsCount) +1;
                        Log.d(TAG_DOWNLOAD, "onDataChange: New Download Count");

                        // setup data to update
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("downloadsCount", newDownloadsCount);

                        // 2. Update new incremented downloads counts to db
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId).updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG_DOWNLOAD, "onSuccess: Download Count Updated...");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG_DOWNLOAD, "onFailure: Failed to update Downloads count due to "+e.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void loadDonerSize(String donerUrl, String donerTitle, TextView donerTv) {
        String TAG = "DONER_SIZE_TAG";
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(donerUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        double bytes = storageMetadata.getSizeBytes();
                        Log.d(TAG, "onSuccess: "+donerTitle+" "+bytes);

                        double kb = bytes/1024;
                        double mb = kb/1024;

                        if(mb >= 1){
                            donerTv.setText(String.format("%.2f", mb)+"MB");
                        }
                        else if(kb >= 1) {
                            donerTv.setText(String.format("%.2f", kb)+"KB");
                        }
                        else {
                            donerTv.setText(String.format("%.2f", bytes)+"bytes");

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: "+e.getMessage());
                    }
                });

    }

    public static void loadDonerFromUrlSinglePage(String donerUrl, String donerTitle) {
        String TAG = "DONER_LOAD_SINGLE_TAG";
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(donerUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {

                        Log.d(TAG, "onSuccess: "+donerTitle+" successfully got the title" );


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed getting file from url due to "+e.getMessage());

                    }
                });



    }

    public static void loadBook(String bookId, TextView bookTv) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Doner");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String book = ""+snapshot.child("book").getValue();
                        bookTv.setText(book);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    public static void  incrementDonerViewCount(String bookId) {
        // 1. Get book view count
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Details");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        if(viewsCount.equals("") || viewsCount.equals("null")) {
                            viewsCount = "0";
                        }
                        // 2. increment views count
                        long newViewsCount = Long.parseLong(viewsCount) +1;
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("viewsCount", newViewsCount);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Details");
                        reference.child(bookId)
                                .updateChildren(hashMap);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public  static void downloadPicture(Context context, String bookId, String name, String url) {
        Log.d(TAG_DOWNLOAD, "downloadPicture: downloading picture...");
        String nameWithExtension = name + ".jpeg";
        Log.d(TAG_DOWNLOAD, "downloadBook: Name: "+nameWithExtension);

        // progress dialog
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Downloading "+ nameWithExtension +"...");
        progressDialog.show();

        // downloading from firebase
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(url);
        storageReference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG_DOWNLOAD, "onSuccess: Book Downloaded");
                        Log.d(TAG_DOWNLOAD, "onSuccess: Saving book...");
                        saveDownloadPicture(context, progressDialog, bytes, nameWithExtension, bookId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG_DOWNLOAD, "onFailure: Failed to download due to"+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, "Failed to download due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private static void saveDownloadPicture(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtension, String bookId) {
        Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Saving downloaded book");
        try {
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadsFolder.mkdirs();

            String filePath = downloadsFolder.getPath() + "/" + nameWithExtension;

            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.write(bytes);
            out.close();

            Toast.makeText(context, "Saved to download Folder", Toast.LENGTH_SHORT).show();
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Saved to download folder");
            progressDialog.dismiss();

          //  incrementPictureDownloadCount(bookId);
        }
        catch (Exception e) {
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Failed saving to Download Folder due to "+e.getMessage());
            Toast.makeText(context, "Failed saving to download folder due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();

        }
    }

    public static void addToFavourite(Context context, String bookId) {
        // we can add only if user is logged in
        // 1. Check if user is logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null) {
            // not logged in
            Toast.makeText(context, "You are not logged in", Toast.LENGTH_SHORT).show();
        }
        else {
            long timestamp = System.currentTimeMillis();

            // setting up data to add to firebase db of current user ofr favourite book
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("bookId", ""+bookId);
            hashMap.put("timestamp", ""+timestamp);

            // save to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
            ref.child(firebaseAuth.getUid()).child("Favourites").child(bookId)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Adding to your Favourites list...", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed to add favourite due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public static void removeFromFavourite(Context context, String bookId) {
        // remove favourite item from the list if user is logged in
        //  1. Checking if user is logged in
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    if (firebaseAuth.getCurrentUser() == null) {
        // not logged in, cannot remove from favourite
        Toast.makeText(context, "You are not logged in. PLease log in", Toast.LENGTH_SHORT).show();
    }
    else {
//        remove from db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
        ref.child(firebaseAuth.getUid()).child("Favourites").child(bookId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Removed the book from favourite list.....", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to remove from favourite due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}





}
