package ru.remixer_dec.cache_extractor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import ru.remixer_dec.cache_extractor.databinding.FragmentFirstBinding;

import static android.os.Build.VERSION.SDK_INT;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private ArrayList<String> obbFiles;
    private ObbMounter obbMounter;
    private String outputPath;
    public static TextView status;
    public static ProgressBar pbar;
    public static Activity activity;
    public static CheckBox compatibility;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = getActivity();
        obbMounter = new ObbMounter();
        status = activity.findViewById(R.id.statusText);
        pbar = activity.findViewById(R.id.progressBar);
        compatibility = activity.findViewById(R.id.altCopy);
        //Android/obb access in Android 11
        if (SDK_INT >= Build.VERSION_CODES.R) {
            activity.findViewById(R.id.androidEleven).setVisibility(View.VISIBLE);
            binding.androidEleven.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:ru.remixer_dec.cache_extractor")));
                }
            });
        }

        if (new File("/vmos.prop").exists() || new File("/guestOSInfo").exists()) {
            activity.findViewById(R.id.noVMOS).setVisibility(View.VISIBLE);
        }

        File localObbDir = new File(Environment.getExternalStorageDirectory() + "/" +
                "Android/obb/ru.remixer_dec.cache_extractor");
        if (!localObbDir.exists()) {
            localObbDir.mkdirs();
        }

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, 2);
            }
        });
        binding.buttonUnpacked.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, 3);
            }
        });
        binding.buttonUnpack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                if (pbar.getVisibility() == View.VISIBLE) return;
                //check if obb was already moved
                if (new File(obbFiles.get(0)).exists()) {
                    ObbPackageInfoReplacer.replaceDataInMultipleOBBs(obbFiles);
                }
                obbMounter.mountMultipleOBBs(obbFiles);
            }
        });

        obbMounter.init(getContext(),new OnAllMountedListener(){
            @Override
            public void onError(int errors, int errorCode) {
                status.setText("Возникли ошибки (" + errors + ") Код ошибки: " + errorCode);
            }

            @Override
            public void onSuccess(ArrayList<String> rawPaths) {
                status.setText("Успешно смонтировано: " + rawPaths.size());
                MountedObbUnpacker.unpackMultiple(rawPaths, obbMounter, outputPath);
            }
        });

        binding.obbFolderPath.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                obbFolderPathChanged(editable.toString());
            }
        });

        binding.unpackedFolderPath.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                unpackedFolderPathChanged(editable.toString());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void obbFolderPathChanged(String path){
        if (!new File(path).exists()) {
            return;
        }
        File[] files = new File(path).listFiles();
        obbFiles = new ArrayList<>();
        for (int i=0, l=files.length; i <l; i++) {
            if (files[i].getAbsolutePath().endsWith(".obb")) {
                obbFiles.add(files[i].getAbsolutePath());
            }
        }
        ((TextView)getActivity().findViewById(R.id.obbFoundText)).setText("Обнаружено obb-файлов: " + obbFiles.size());
    }

    private void unpackedFolderPathChanged(String path) {
        outputPath = path;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (resultCode == 0) {
            return;
        }
        if (requestCode == 2) {
            String path = PathHelper.getPathFromIntentUri(this.getContext(), data.getData());
            ((EditText)getActivity().findViewById(R.id.obbFolderPath)).setText(path);
        } else if (requestCode == 3) {
            String path = PathHelper.getPathFromIntentUri(this.getContext(), data.getData());
            ((EditText)getActivity().findViewById(R.id.unpackedFolderPath)).setText(path);
        }
    }

}