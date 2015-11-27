package com.example.com.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    // 변수선언
    private int cYear, cMonth, cDay;
    TextView tvDate;
    Button btnSave;
    EditText edtDiary;
    String fileName;
    String strSDpath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDate = (TextView) findViewById(R.id.tvDate);
        btnSave = (Button) findViewById(R.id.btnSave);
        edtDiary = (EditText) findViewById(R.id.edtDiary);

        //SD카드의 절대 경로를 돌려줌
        strSDpath = Environment.getExternalStorageDirectory().getAbsolutePath();

        // SD카드 아래에 mydiary 폴더를 생성하기 위한 File형의 변수 선언
        final File mydiary = new File(strSDpath + "/mydiary");

        // 앱 실행 시 mydiary 폴더가 없다면 생성
        mydiary.mkdir();

        //TextView 터치시 DatePicker위젯을 가지고 있는 다이얼로그를 나타냄
        tvDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Dialog datepicker = new DatePickerDialog(MainActivity.this, callBack, cYear, cMonth, cDay);
                        datepicker.show();
                        break;
                }
                return false;
            }
        });

        //현재 날짜 구하기
        Calendar cal = Calendar.getInstance();
        cYear = cal.get(Calendar.YEAR);
        cMonth = cal.get(Calendar.MONTH);
        cDay = cal.get(Calendar.DAY_OF_MONTH);
        String today = Integer.toString(cYear) + "년 " + Integer.toString(cMonth + 1) + "월 " +
                Integer.toString(cDay) + "일";

        //액티비티 위에 인식한 날짜를 띄우는 메서드 호출
        updateDisplay();

        //오늘의 일기가 있으면 앱실행시 EditText에 띄우는 메서드 호출
        showDiary();

        //TextView에 오늘날짜 표시
        tvDate.setText(today);

        //저장버튼 클릭시 이벤트
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(strSDpath+"/mydiary/"+fileName);
                try {
                    FileOutputStream outFs = new FileOutputStream(file);
                    //FileOutputStream outFs = openFileOutput(fileName, Context.MODE_WORLD_WRITEABLE);
                    String str = edtDiary.getText().toString();
                    outFs.write(str.getBytes());
                    outFs.close();
                    Toast.makeText(getApplicationContext(), fileName + " 이 저장됨", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                }
            }
        });
    }

    // 액티비티 위에 인식한 날짜를 띄우는 메서드
    private void updateDisplay() {
        tvDate.setText(new StringBuilder().append(cYear).append("년 ").append(cMonth + 1).append("월 ").append(cDay).append("일"));
    }

    //날짜변경시 이벤트처리
    DatePickerDialog.OnDateSetListener callBack = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            cYear = year;
            cMonth = monthOfYear;
            cDay = dayOfMonth;
            updateDisplay();
            fileName = Integer.toString(year) + "_" + Integer.toString(monthOfYear + 1) + "_" +
                    Integer.toString(dayOfMonth) + ".txt";
            String str = readDiary(fileName);
            edtDiary.setText(str);
            btnSave.setEnabled(true);
        }
    };

    // 오늘의 일기가 있으면 앱실행시 EditText에 띄움
    private void showDiary() {
        fileName = Integer.toString(cYear) + "_" + Integer.toString(cMonth + 1) + "_" +
                Integer.toString(cDay) + ".txt";
        String str = readDiary(fileName);
        edtDiary.setText(str);
        btnSave.setEnabled(true);
    }

    // 다이어리를 읽음
    String readDiary(String fName) {
        String diaryStr = null;
        try {
            FileInputStream fis = new FileInputStream(strSDpath + "/mydiary/" + fName);
            byte[] data = new byte[fis.available()];
            while(fis.read(data) != -1) {;}
            fis.close();
            edtDiary.setText(new String(data));
            diaryStr = (new String(data)).trim();
            btnSave.setText("수정");
        } catch (IOException e) {
            btnSave.setText("저장");
        }
        return diaryStr;
    }

    //옵션메뉴 생성
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final int readAgain = 0;
        final int deleteDiary = 1;
        final int big = 3;
        final int nomal = 4;
        final int small = 5;
        //getMenuInflater().inflate(R.menu.menu_main, menu); //menu xml로 만들 때

        // Menu 추가
        menu.add(0, readAgain, Menu.NONE, "다시 읽기");
        menu.add(0, deleteDiary, Menu.NONE, "일기 삭제");

        // Menu에 SubMenu 추가
        SubMenu subMenu = menu.addSubMenu("글씨 크기");

        subMenu.add(1, big, Menu.NONE, "크게");
        subMenu.add(1, nomal, Menu.NONE, "보통");
        subMenu.add(1, small, Menu.NONE, "작게");

        return true;
    }

    //옵션 메뉴 클릭시 이벤트처리
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == 0) {
            readDiary( strSDpath + "/mydiary/" + fileName);
            Toast.makeText(getApplicationContext(), fileName + "을 다시 불러왔습니다.",Toast.LENGTH_LONG).show();
        } else if (id == 1) {
            openOptionsDialog();
        } else {
            if (id == 3) {
                edtDiary.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
            } else if (id == 4) {
                edtDiary.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            } else if (id == 5) {
                edtDiary.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 5);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    // 옵션메뉴 중 '일기 삭제'를 선택했을 때 이벤트 처리
    private void openOptionsDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.deleteDiary)
                .setMessage(tvDate.getText().toString() + " 일기를 삭제하시겠습니까?")
                .setNegativeButton("No", null)
                .setPositiveButton(R.string.str_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialoginterface, int i) {
                                new File( strSDpath + "/mydiary/" + fileName).delete();
                                edtDiary.setText("");
                                btnSave.setText("저장");
                            }
                        }).show();
    }
}
