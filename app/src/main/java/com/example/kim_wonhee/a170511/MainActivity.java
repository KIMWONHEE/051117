package com.example.kim_wonhee.a170511;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    Button save_button;
    LinearLayout linearLayout1, linearLayout2;
    EditText editText;
    DatePicker datePicker;
    TextView textView;
    ListView listView;
    ArrayList<File> files;
    ArrayList<String> filename = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    int memo_count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("내맘대로 메모장");

        init();

        //--- 위험 Permission 권한부여
        int permissioninfo = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissioninfo == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "SDCard 쓰기/읽기 권한 있음", Toast.LENGTH_SHORT).show();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(getApplicationContext(), "권한의 필요성 설명", Toast.LENGTH_SHORT).show();
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }

        //--- 외부 메모리 디렉터리 생성
        final String path = getExternalPath();

        File file = new File(path+ "/dairy/");
        file.mkdir();

        //--- 디렉터리 생성 오류 메시지
        String msg = "";
        if (file.isDirectory() == false) {
            msg = "디렉터리 생성 오류";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }

        //--- 외부 메모리 파일 목록
        File[] files = new File(path + "/diary/").listFiles();
        memo_count = files.length;

        for (int i = 0; i < memo_count; i++)
            filename.add(files[i].getName());

        textView.setText("등록된 메모 개수 : " + memo_count);

        //--- listView
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, filename);
        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                dlg.setTitle("삭제확인");
                dlg.setIcon(R.drawable.memo);
                dlg.setMessage("삭제하시겠습니까? ");
                dlg.setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                memo_count--;
                                textView.setText("등록된 메모 개수 : " + memo_count);
                                File file = new File(path + "/diary" + filename.get(position));
                                file.delete();
                                filename.remove(position);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(getApplicationContext(), "선택한 메모가 삭제되었습니다.", Toast.LENGTH_SHORT).show();

                            }
                        });
                dlg.setNegativeButton("취소", null);
                dlg.show();
                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                save_button.setText("수정");
                linearLayout1.setVisibility(View.INVISIBLE);
                linearLayout2.setVisibility(View.VISIBLE);
                String name = filename.get(position);

                int year = Integer.parseInt(name.substring(0, 2)) + 2000;
                int month = Integer.parseInt(name.substring(3, 5)) - 1;
                int day = Integer.parseInt(name.substring(6, 8));

                //--- 외부 메모리(SD Card) 파일 읽기
                try {
                    String path = getExternalPath() + "/diary/";
                    BufferedReader br = new BufferedReader(new FileReader(path + name));
                    String readStr = "";
                    String str = null;
                    while ((str = br.readLine()) != null)
                        readStr += str + "\n";
                    br.close();
                    editText.setText(readStr);
                    datePicker.updateDate(year, month, day);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    void init() {
        save_button = (Button) findViewById(R.id.btnsave);
        linearLayout1 = (LinearLayout) findViewById(R.id.linear1);
        linearLayout2 = (LinearLayout) findViewById(R.id.linear2);
        editText = (EditText) findViewById(R.id.editText);
        datePicker = (DatePicker) findViewById(R.id.datePicker);
        listView = (ListView) findViewById(R.id.listview);
        textView = (TextView) findViewById(R.id.tvCount);
    }

    //--- 외부 메모리(SD Card) 파일 처리
    public String getExternalPath(){
        String sdPath = "";
        String ext = Environment.getExternalStorageState();
        if(ext.equals(Environment.MEDIA_MOUNTED)) {
            sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        } else
            sdPath = getFilesDir() + "";
        return sdPath;
    }

    //--- 위험 Permission 권한 부여
    //@Override
//    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        String str = null;
//
//        if (requestCode == 100) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
//                str = "SD Card 쓰기권한 승인";
//            else
//                str = "SD Card 쓰기권한 거부";
//            Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
//        }
//    }

    public void onClick(View v) {
        //--- file_name
        int year = datePicker.getYear() - 2000;
        int month = datePicker.getMonth() + 1;
        int day = datePicker.getDayOfMonth();

        String file_name = "";

        if (month < 10)
            file_name = year + "-0" + month + "-" + day + ".memo";
        else
            file_name = year + "-" + month + "-" + day + ".memo";

        //--- Button
        if (v.getId() == R.id.btn1) {
            linearLayout1.setVisibility(View.INVISIBLE);
            linearLayout2.setVisibility(View.VISIBLE);
        } else if (v.getId() == R.id.btnsave) {
            String what = save_button.getText().toString();
            String path = getExternalPath() + "/diary/";
            if (what.equals("수정")) {
                // 수정하기
                try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(path + file_name, false));
                    bw.write(editText.getText().toString());
                    bw.close();
                    Toast.makeText(this, "수정되었습니다. ", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (what.equals("저장")) {
                // 저장하기
                //--- 외부 메모리(SD Card) 파일 쓰기
                try {
                   // String path = getExternalPath() + "/diary/";
                    BufferedWriter bw = new BufferedWriter(new FileWriter(path + file_name, false));
                    bw.write(editText.getText().toString());
                    bw.close();
                    filename.add(file_name);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "저장되었습니다. ", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            linearLayout1.setVisibility(View.VISIBLE);
            linearLayout2.setVisibility(View.INVISIBLE);
        } else if (v.getId() == R.id.btncancel) {
            linearLayout1.setVisibility(View.VISIBLE);
            linearLayout2.setVisibility(View.INVISIBLE);
            //--- editText 초기화
            editText.setText(null);
            //--- datePicker 초기화
            Calendar cal= Calendar.getInstance();
            int now_year=cal.get(Calendar.YEAR);
            int now_month=cal.get(Calendar.MONTH);
            int now_day=cal.get(Calendar.DAY_OF_MONTH);
            datePicker.updateDate(now_year, now_month, now_day);
        }

    }


}
