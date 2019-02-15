package cn.wthee.note;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import cn.wthee.note.Fragment.FragmentNote;
import cn.wthee.note.Fragment.FragmentRec;
import cn.wthee.note.Fragment.ViewPagerFragmentAdapter;
import cn.wthee.note.MyNote.EditNote;
import cn.wthee.note.MyNote.Note;
import cn.wthee.note.MyNote.NoteAdapter;
import cn.wthee.note.Words.SetWords;
import cn.wthee.note.Words.Word;

import static org.litepal.LitePalApplication.getContext;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,View.OnClickListener {

    private FloatingActionButton fabAdd;
    private boolean isAdd = false;
    private RelativeLayout rlAddBill;
    private int[] llId = new int[]{R.id.ll01,R.id.ll02,R.id.ll03,R.id.ll04};
    private LinearLayout[] ll = new LinearLayout[llId.length];
    private int[] fabId = new int[]{R.id.miniFab01,R.id.miniFab02,R.id.miniFab03,R.id.miniFab04,};
    private FloatingActionButton[] fab = new FloatingActionButton[fabId.length];
    private AnimatorSet addBillTranslate1;
    private AnimatorSet addBillTranslate2;
    private AnimatorSet addBillTranslate3;
    private AnimatorSet addBillTranslate4;

    public static ViewPagerFragmentAdapter mViewPagerFragmentAdapter;
    private FragmentManager mFragmentManager;
    private Fragment fragmentNote;
    private Fragment fragmentRec;
    private ViewPager container;
    private List<Fragment> mFragmentList = new ArrayList<Fragment>();
    public static List<String> mTitleList = new ArrayList<String>();

    private Toolbar toolbar;
    private TabLayout tabLayout;


    private Handler handler;
    public static TextView select;
    public static boolean isSelectAll = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler();
        //申请权限
        String[] permissions = new String[]{
                Manifest.permission.WRITE_CALENDAR,
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO};
        List<String> mPermissions = new ArrayList<>();
        for(String string : permissions){
            if (ContextCompat.checkSelfPermission(MainActivity.this,string) != PackageManager.PERMISSION_GRANTED) {
                mPermissions.add(string);
            }
        }
        if(mPermissions.size()>0){
            ActivityCompat.requestPermissions(this,permissions,1);
        }else{
            initView();
            initNote();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        //新建返回后，更新viewPager
        initNote();
    }

    //初始化控件
    private void initView(){
        //toolbar一句
        final TextView textView = (TextView)findViewById(R.id.dailyWord);
        final TextView textView1 = (TextView)findViewById(R.id.dailyWord_data);
        select = (TextView) findViewById(R.id.select);
        new SetWords().setMyWords(new SetWords.GetText() {
            @Override
            public void onSuccess(final Word word) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("       "+word.getNote());
                        textView1.setText(word.getDateline());
                    }
                });
            }
            @Override
            public void onFailure() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("       "+"希望是附丽于存在的，有存在，便有希望，有希望，便是光明。");
                        textView1.setText("——鲁迅");
                    }
                });
            }
        });
        //toolbar
        setSupportActionBar(toolbar);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        //Nav
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //tab与viewpager绑定、初始化Fragment
        tabLayout = (TabLayout)findViewById(R.id.tablayout);
        container = (ViewPager)findViewById(R.id.container);
        fragmentNote = new FragmentNote();
        fragmentRec = new FragmentRec();
        mFragmentList.add(fragmentNote);
        mFragmentList.add(fragmentRec);
        mFragmentManager = getSupportFragmentManager();
        mTitleList.add("我的便签 "+LitePal.where("notDel = ?", "1").count("note"));
        mTitleList.add("回收站 "+LitePal.where("notDel = ?", "0").count("note"));
        mViewPagerFragmentAdapter = new ViewPagerFragmentAdapter(mFragmentManager, mFragmentList,mTitleList);
        //fab按钮
        fabAdd = (FloatingActionButton)findViewById(R.id.fab);
        rlAddBill = (RelativeLayout)findViewById(R.id.rlAddBill);
        for (int i = 0; i < llId.length;i++){
            ll[i] = (LinearLayout)findViewById(llId[i]);
        }
        for (int i = 0;i < fabId.length; i++){
            fab[i] = (FloatingActionButton)findViewById(fabId[i]);
        }
        fabAdd.setOnClickListener(this);
        for (int i = 0;i < fabId.length; i++){
            fab[i].setOnClickListener(this);
        }
        //动画默认值
        addBillTranslate1 = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(),R.animator.add_bill_anim);
        addBillTranslate2 = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(),R.animator.add_bill_anim);
        addBillTranslate3 = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(),R.animator.add_bill_anim);
        addBillTranslate4 = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(),R.animator.add_bill_anim);
    }
    public void initNote(){
        if(container!=null){
            mTitleList.clear();
            mTitleList.add("我的便签 "+LitePal.where("notDel = ?", "1").count("note"));
            mTitleList.add("回收站 "+LitePal.where("notDel = ?", "0").count("note"));
            mViewPagerFragmentAdapter = new ViewPagerFragmentAdapter(mFragmentManager, mFragmentList,mTitleList);
            container.setAdapter(mViewPagerFragmentAdapter);
            container.setCurrentItem(0);
            tabLayout.setupWithViewPager(container,true);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                fabAdd.setImageResource(isAdd ? R.drawable.more:R.drawable.fold);
                isAdd = !isAdd;
                rlAddBill.setVisibility(isAdd ? View.VISIBLE : View.GONE);
                if (isAdd) {
                    addBillTranslate1.setTarget(ll[0]);
                    addBillTranslate1.start();
                    addBillTranslate2.setTarget(ll[1]);
                    addBillTranslate2.setStartDelay(50);
                    addBillTranslate2.start();
                    addBillTranslate3.setTarget(ll[2]);
                    addBillTranslate3.setStartDelay(100);
                    addBillTranslate3.start();
                    addBillTranslate4.setTarget(ll[3]);
                    addBillTranslate4.setStartDelay(150);
                    addBillTranslate4.start();
                }
                break;
            case R.id.miniFab01:
                //添加
                hideFABMenu();
                Intent intent = new Intent(this,EditNote.class);
                intent.putExtra("isEmpty","1");
                intent.putExtra("id",""+"-1");
                startActivity(intent);
                break;
            case R.id.miniFab02:
                //批量删除
                deleteNote();
                hideFABMenu();
                break;
            case R.id.miniFab03:
                //恢复
                recNote();
                hideFABMenu();
                break;
            case R.id.miniFab04:
                //全选
                setSelectAll();
                hideFABMenu();
                break;
            default:
                break;
        }
    }
    //删除
    public void deleteNote(){
        List<Note> list1 = new ArrayList<Note>(),list2 = new ArrayList<Note>();
        if(container.getCurrentItem()==0){
            NoteAdapter adapter1 = FragmentNote.getAdapter();
            list1 = adapter1.getChecklist();
        } else if(container.getCurrentItem()==1){
            NoteAdapter adapter2 = FragmentRec.getAdapter();
            list2 = adapter2.getChecklist();
        }
        if(list1.size()==0&&list2.size()==0){
            Toast.makeText(this,"你还没有选择要删除的内容~",Toast.LENGTH_SHORT).show();
        }else {
            final List<Note> checkedList1 = list1;
            final List<Note> checkedList2 = list2;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提醒");
            builder.setMessage("将所选项全部删除？");
            builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    for(Note n : checkedList1){
                        n.setNotDel(0);
                        n.save();
                    }
                    for(Note n : checkedList2){
                        LitePal.delete(Note.class,n.getId());
                    }
                    initNote();
                }
            });
            builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.create();
            builder.show();
        }

    }
    //恢复
    public void recNote(){
        List<Note> list2 = new ArrayList<Note>();
        if(container.getCurrentItem()==0){
            Toast.makeText(this,"只能在回收站中恢复~",Toast.LENGTH_SHORT).show();
        } else if(container.getCurrentItem()==1){
            NoteAdapter adapter2 = FragmentRec.getAdapter();
            list2 = adapter2.getChecklist();
            if(list2.size()==0){
                Toast.makeText(this,"你还没有选择要删除的内容~",Toast.LENGTH_SHORT).show();

            }else {
                final List<Note> checkedList2 = list2;
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("提醒");
                builder.setMessage("将所选项全部恢复？");
                builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for(Note n : checkedList2){
                            n.setNotDel(1);
                            n.save();
                        }
                        initNote();
                    }
                });
                builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.create();
                builder.show();
            }
        }
    }
    //全选
    public void setSelectAll(){
        List<Note> list1 = new ArrayList<Note>(),list2 = new ArrayList<Note>();
        NoteAdapter adapter1 = FragmentNote.getAdapter();
        NoteAdapter adapter2 = FragmentRec.getAdapter();
        if(isSelectAll==false){
            isSelectAll=true;
            select.setText("取消全选");
            if(container.getCurrentItem()==0){
                adapter1.setChecklist(adapter1.getNoteList());
            } else if(container.getCurrentItem()==1){
                adapter2.setChecklist(adapter2.getNoteList());
            }
        }
        else {
            isSelectAll=false;
            select.setText("全选");
            if(container.getCurrentItem()==0){
                adapter1.setChecklist(list1);
            } else if(container.getCurrentItem()==1){
                adapter2.setChecklist(list2);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                //有权限没有通过
                boolean hasPermissionDismiss = false;
                for (int i=0;i<grantResults.length;i++){
                    if (grantResults[i]==-1){
                        hasPermissionDismiss=true;
                        break;
                    }
                }
                if (!hasPermissionDismiss) {
                    initView();
                    initNote();
                } else {
                    Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_manage) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void hideFABMenu(){
        rlAddBill.setVisibility(View.GONE);
        fabAdd.setImageResource(R.drawable.more);
        isAdd = false;
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


}
