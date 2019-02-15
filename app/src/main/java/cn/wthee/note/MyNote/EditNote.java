package cn.wthee.note.MyNote;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import cn.wthee.note.R;

public class EditNote extends AppCompatActivity implements View.OnClickListener{


    private final int PICK_PHOTO = 3;
    private static String CALENDAR_EVENT_URL = "content://com.android.calendar/events";
    private EditText title;
    private EditText content;
    private TextView date;
    private Button addImg;
    private ImageView imageView;
    private Bundle bundle;
    private List<Note> notes;
    private String isEmpty, picPath,audioPath, eventDate;
    private boolean isEvent=false;
    private int imgWidth,imgHeight;
    private Button btn_control;
    private Button playAudio;
    private MediaRecorder mr = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_edit);
        setToolBar();
        initView();
        bundle =getIntent().getExtras();
        if(bundle!=null){
            isEmpty = bundle.getString("isEmpty");
        }
        //存在数据，就加载到布局
        if(isEmpty.equals("0")){
            String id = bundle.getString("id");
            notes = LitePal.where("id = ?",id).find(Note.class);
            title.setText(notes.get(0).getTitle());
            content.setText(notes.get(0).getContent());
            picPath = notes.get(0).getPicPath();
            audioPath = notes.get(0).getAudioPath();
            imgWidth = notes.get(0).getPicWidth();
            imgHeight = notes.get(0).getPicHeight();
            isEvent = notes.get(0).isEvent();
            eventDate = notes.get(0).getEventDate();
            Glide.with(this).load(picPath).into(imageView);
            date.setText(notes.get(0).getDate());
            imageView.setVisibility(View.VISIBLE);
            //如果有录音，显示播放按钮
            if(audioPath!=null){
                playAudio.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initView(){
        title = (EditText)findViewById(R.id.show_title);
        content = (EditText)findViewById(R.id.show_content);
        date = (TextView)findViewById(R.id.show_date);
        addImg = (Button)findViewById(R.id.addImg);
        imageView = (ImageView)findViewById(R.id.img);
        addImg.setOnClickListener(this);

        btn_control = (Button) findViewById(R.id.addVioce);
        btn_control.setOnClickListener(this);

        playAudio = (Button)findViewById(R.id.playAudio);
        playAudio.setOnClickListener(this);

        imageView.setLongClickable(true);
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditNote.this);
                builder.setMessage("删除图片？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        picPath = null;
                        imageView.setVisibility(View.GONE);
                        saveNote();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
                return false;
            }
        });
    }

    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_edit);
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.addImg:
                pickPhotoFromAlbum(v);
                break;
            case R.id.addVioce:
                startRecord();
                break;
            case R.id.playAudio:
                try {
                    startPlayAudio();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    //录制
    private void startRecord(){
        if(mr == null){
            File dir = new File(Environment.getExternalStorageDirectory(),"sounds");
            if(!dir.exists()){
                dir.mkdirs();
            }
            String fileName = System.currentTimeMillis()+".amr";
            final String path = dir + "/"+fileName;
            final File soundFile = new File(dir,fileName);
            if(!soundFile.exists()){
                try {
                    soundFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mr = new MediaRecorder();
            mr.setAudioSource(MediaRecorder.AudioSource.MIC);  //音频输入源
            mr.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);   //设置输出格式
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);   //设置编码格式
            mr.setOutputFile(soundFile.getAbsolutePath());
            try {
                mr.prepare();
                mr.start();  //开始录制
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("正在录制声音......");
                builder.setCancelable(false);
                builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mr.stop();
                        mr.release();
                        soundFile.delete();
                        mr = null;
                        playAudio.setVisibility(View.GONE);
                        Toast.makeText(EditNote.this, "已取消录音", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("完成", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mr.stop();
                        mr.release();
                        mr = null;
                        audioPath = path;
                        saveNote();
                        playAudio.setVisibility(View.VISIBLE);
                        Toast.makeText(EditNote.this, "录音保存至"+audioPath, Toast.LENGTH_LONG).show();
                    }
                });
                builder.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //播放录音
    private void startPlayAudio() throws IOException {
        final MediaPlayer mp = new MediaPlayer();
        mp.setDataSource(audioPath);
        mp.prepare();
        mp.start();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("正在播放录音......");
        builder.setCancelable(false);
        builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mp.stop();
                mp.release();
                Toast.makeText(EditNote.this, "已停止播放录音", Toast.LENGTH_LONG).show();
            }
        });
        builder.show();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                saveNote();
                finish();
                break;
            case R.id.action_save:
                saveNote();
                break;
            case R.id.action_reminder:
                //region设置事件提醒
                final Context context =this;
                final GetNowDate gnd = new GetNowDate();
                new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {
                        new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                //创建提醒
                                Calendar beginTime = Calendar.getInstance();//开始时间
                                beginTime.set(year,month,dayOfMonth,hourOfDay,minute);//选择的时间
                                String calId = "";
                                Cursor userCursor = getContentResolver().query(Uri.parse(CALENDAR_EVENT_URL), null, null, null, null);
                                if (userCursor.getCount() > 0) {
                                    userCursor.moveToFirst();
                                    calId = userCursor.getString(userCursor.getColumnIndex("_id"));
                                }
                                ContentValues event = new ContentValues();
                                event.put("title", title.getText().toString());
                                event.put("description", content.getText().toString());
                                event.put("calendar_id", calId);
                                event.put("dtstart", beginTime.getTimeInMillis());
                                event.put("dtend", beginTime.getTimeInMillis()+ 10 * 60 * 1000);//十分钟
                                event.put("hasAlarm", 1);
                                event.put("eventTimezone", TimeZone.getDefault().getID().toString());
                                Uri newEvent = EditNote.this.getContentResolver().insert(Uri.parse(CALENDAR_EVENT_URL), event);

                                long eventId = Long.parseLong(newEvent.getLastPathSegment());
                                ContentValues values = new ContentValues();
                                values.put(CalendarContract.Reminders.MINUTES, 0);
                                values.put(CalendarContract.Reminders.EVENT_ID, eventId);
                                values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
                                ContentResolver cr1 = context.getContentResolver(); // 为刚才新添加的event添加reminder
                                Uri uri = cr1.insert(CalendarContract.Reminders.CONTENT_URI, values);

                                if (uri == null) {
                                    Toast.makeText(EditNote.this, "创建提醒失败", Toast.LENGTH_LONG).show();
                                }else{
                                    //添加提醒，先保存便签内容
                                    isEvent=true;
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd日 HH:mm");
                                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
                                    eventDate = dateFormat.format(new Date(event.getAsLong("dtstart")));
                                    saveNote();
                                    Toast.makeText(EditNote.this, "已创建提醒", Toast.LENGTH_LONG).show();
                                }
                            }
                        },gnd.getHour(),gnd.getMinute(),true).show();
                    }
                },gnd.getYear(),gnd.getMonth(),gnd.getDay()).show();
                //endregion
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void pickPhotoFromAlbum(View view){
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");//设置需要从系统选择的内容：图片出位置
        startActivityForResult(intent, this.PICK_PHOTO);//开始选择
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "获取图片出现错误", Toast.LENGTH_SHORT).show();
        }
        else{
            switch(requestCode) {
                case PICK_PHOTO:
                    imageView.setVisibility(View.VISIBLE);
                    Uri imgUri = data.getData();
                    String [] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(imgUri, filePathColumn, null,
                            null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    picPath = cursor.getString(columnIndex);
                    Bitmap bitmap = BitmapFactory.decodeFile(picPath);
                    imgWidth = bitmap.getWidth();
                    imgHeight = bitmap.getHeight();
                    imageView.setImageBitmap(bitmap);
                    break;
                default:
                    break;
            }
        }
    }
    public int saveNote(){
        if(isEmpty.equals("1")){
            if(newNote()){
                isEmpty="0";
                return LitePal.findLast(Note.class).getId();//当前新建编辑的便签
            }
        }else {
            updateNote();
            return Integer.parseInt(bundle.getString("id"));
        }
        return -1;
    }
    public void updateNote() {
        //更新便签
        LitePal.getDatabase();
        ContentValues values = new ContentValues();
        values.put("title",title.getText().toString());
        values.put("content",content.getText().toString());
        values.put("picPath", picPath);
        values.put("audioPath",audioPath);
        values.put("picWidth",imgWidth);
        values.put("picHeight",imgHeight);
        values.put("isEvent",isEvent);
        values.put("date",new GetNowDate().getNowDate());
        values.put("eventDate",eventDate);
        String str = bundle.getString("id");
        if(!str.equals("-1")){
            LitePal.update(Note.class,values,Long.valueOf(str));
        }else {
            //说明是新建的便签，但多次点击保存
            Note note = LitePal.findLast(Note.class);
            LitePal.update(Note.class,values,note.getId());
        }
    }
    public boolean newNote() {
        //新建便签，return 创建成功true
        LitePal.getDatabase();
        Note note = new Note();
        String t = title.getText().toString();
        String c = content.getText().toString();
        //标题，正文，图片不全为空
        if(!t.equals("")||!c.equals("")|| picPath !=null){
            note.setTitle(t);
            note.setContent(c);
            note.setPicPath(picPath);
            note.setAudioPath(audioPath);
            note.setPicWidth(imgWidth);
            note.setPicHeight(imgHeight);
            note.setEvent(isEvent);
            note.setDate(new GetNowDate().getNowDate());
            note.setEventDate(eventDate);
            Boolean result=note.save();
            if(!result){
                Toast.makeText(this,"保存失败",Toast.LENGTH_SHORT).show();
                return false;
            }else {
                Toast.makeText(this,"保存成功",Toast.LENGTH_SHORT).show();
                return true;
            }
        }else{
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit, menu);
        return true;
    }

    class GetNowDate{
        Calendar calendar;
        int year;
        int month ;
        int day;
        int hour;
        int minute;

        public GetNowDate() {
            calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        }

        public String getNowDate(){
            return year+"年"
                    +String.format("%02d", month+1)+"月"
                    +String.format("%02d", day)+"日"
                    +" "+String.format("%02d", hour)+":"
                    +String.format("%02d", minute);
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public int getMonth() {
            return month;
        }

        public void setMonth(int month) {
            this.month = month;
        }

        public int getDay() {
            return day;
        }

        public void setDay(int day) {
            this.day = day;
        }

        public int getHour() {
            return hour;
        }

        public void setHour(int hour) {
            this.hour = hour;
        }

        public int getMinute() {
            return minute;
        }

        public void setMinute(int minute) {
            this.minute = minute;
        }


    }

}
