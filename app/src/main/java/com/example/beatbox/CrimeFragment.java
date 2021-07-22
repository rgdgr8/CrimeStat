package com.example.beatbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CrimeFragment extends Fragment {
    public static final String ARG_CRIME_ID = "crime_id";
    public static final String RESULT_DATE = "result_date";
    private static final String TAG = "CrimeFrag";

    private Crime mCrime;
    private File mPhotoFile;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mDelButton;
    private Button mReportButton;
    private Button mDelImageBtn;
    private CheckBox mSolvedCheckBox;

    private ImageView mCrimeImage;
    private int crimeImgHeight;
    private int crimeImgWidth;

    private ActivityResultLauncher<Intent> launcher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UUID id = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(id);
        Log.d(TAG, "onCreate: crime = " + mCrime);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);

        /* launcher must be created before the fragment views are created or even before the fragement is created
        i.e on onAttach(), or onCreate() not after than*/

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Uri uri = FileProvider.getUriForFile(getActivity(),
                                    "com.example.beatbox.fileprovider", mPhotoFile);
                            getActivity().revokeUriPermission(uri,
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            updatePhotoView();
                        }
                    }
                });
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mCrimeImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher_foreground, null));
            mDelImageBtn.setText("Add Image");
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(
                    mPhotoFile.getPath(), crimeImgWidth, crimeImgHeight);
            mCrimeImage.setImageBitmap(bitmap);
            mDelImageBtn.setText("Remove Image");
        }
    }

    public static CrimeFragment newInstance(UUID id) {
        Bundle b = new Bundle();
        b.putSerializable(ARG_CRIME_ID, id);
        CrimeFragment cf = new CrimeFragment();
        cf.setArguments(b);
        return cf;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.crime_fragment, container, false);

        mTitleField = v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());

        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
                // This space intentionally left blank
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // This one too
            }
        });

        mDateButton = (Button) v.findViewById(R.id.crime_date);
        mDateButton.setText(mCrime.getDate().toString());
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment dpf = DatePickerFragment.newInstance(mCrime.getDate());
                //dpf.setTargetFragment();
                FragmentManager fm = getParentFragmentManager();
                fm.setFragmentResultListener(RESULT_DATE, dpf, new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        if (requestKey.equals(RESULT_DATE)) {
                            Date dt = (Date) result.getSerializable(DatePickerFragment.ARG_DATE);
                            mCrime.setDate(dt);
                            mDateButton.setText(dt.toString());
                        }
                    }
                });
                dpf.show(fm, "DateDialog");//show dialog
            }
        });

        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });

        mDelButton = v.findViewById(R.id.del_btn);
        mDelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CrimeLab.get(getActivity()).deleteCrime(mCrime);
                mCrime = null;
                getActivity().finish();
            }
        });

        mReportButton = v.findViewById(R.id.report_btn);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, mCrime.toString());
                startActivity(i);
            }
        });

        mCrimeImage = v.findViewById(R.id.crime_img);
        mCrimeImage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d("onGlobalLayout", "kkkk");
                mCrimeImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                crimeImgWidth = mCrimeImage.getWidth();
                crimeImgHeight = mCrimeImage.getHeight();
                updatePhotoView();
            }
        });
        mCrimeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPhotoFile == null || !mPhotoFile.exists()) {
                    Intent captureImageIntent = new Intent();
                    captureImageIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    Uri uri = FileProvider.getUriForFile(getActivity(), "com.example.beatbox.fileprovider", mPhotoFile);
                    captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                    List<ResolveInfo> cameraActivities = getActivity().getPackageManager()
                            .queryIntentActivities(captureImageIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo act : cameraActivities) {
                        getActivity().grantUriPermission(act.activityInfo.packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }

                    launcher.launch(captureImageIntent);

                /*try {
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    } else {
                        startMediaIntent();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                } else {
                    Bitmap bitmap = BitmapFactory.decodeFile(mPhotoFile.getPath());
                    View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.show_bigger_image, null);
                    ImageView img = dialogView.findViewById(R.id.dialog_img);
                    img.setImageBitmap(bitmap);
                    new AlertDialog.Builder(getActivity())
                            .setView(dialogView)
                            .setNeutralButton("Done", null)
                            .create().show();
                }
            }
        });

        mDelImageBtn = v.findViewById(R.id.del_img);
        mDelImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPhotoFile == null || !mPhotoFile.exists()) {
                    mCrimeImage.performClick();
                    return;
                }

                mPhotoFile.delete();
                updatePhotoView();
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, "onCreateView: crime = "+mCrime+"  user = "+user.getUid());
        if (user == null || !user.getUid().equals(mCrime.getUserId())) {
            mCrimeImage.setEnabled(false);

            mDelImageBtn.setText("Image");
            mDelImageBtn.setEnabled(false);

            mTitleField.setEnabled(false);

            mDateButton.setEnabled(false);

            mSolvedCheckBox.setEnabled(false);

            mDelButton.setVisibility(View.INVISIBLE);
            mDelButton.setEnabled(false);
        }

        return v;
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startMediaIntent();
            }
        }
    }

    public void startMediaIntent(){
        Intent captureImageIntent = new Intent();
        captureImageIntent.setAction(Intent.ACTION_PICK);
        captureImageIntent.setType("image/*");
        //captureImageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        launcher.launch(captureImageIntent);
    }

    public String getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getActivity().managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        // this is our fallback here
        return uri.getPath();
    }*/

    @Override
    public void onStop() {
        super.onStop();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (mCrime != null && user != null) {
            CrimeLab.get(getActivity())
                    .updateCrime(mCrime);
        }
    }
}
