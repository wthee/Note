package cn.wthee.note.MyNote;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import cn.wthee.note.MainActivity;
import cn.wthee.note.R;


public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {

    private Context context;
    private List<Note> noteList;
    private List<Note> checklist;

    public void setChecklist(List<Note> checklist) {
        this.checklist = checklist;
        notifyDataSetChanged();
    }
    public List<Note> getChecklist() {
        return checklist;
    }
    public List<Note> getNoteList(){return noteList;}
    static class ViewHolder extends RecyclerView.ViewHolder{
        View noteView;
        TextView item_title;
        TextView item_content;
        TextView item_date;
        ImageView item_img;
        Button btn_rec;
        Button btn_del;
        ImageView event;
        TextView eventTime;
        public ViewHolder(View view){
            super(view);
            this.noteView = view;
            item_title = (TextView)view.findViewById(R.id.item_title);
            item_content = (TextView)view.findViewById(R.id.item_content);
            item_date = (TextView)view.findViewById(R.id.item_date);
            item_img = (ImageView) view.findViewById(R.id.item_img);
            btn_rec = (Button)view.findViewById(R.id.btn_rec);
            btn_del = (Button)view.findViewById(R.id.btn_del);
            event = (ImageView)view.findViewById(R.id.isEvent);
            eventTime = (TextView)view.findViewById(R.id.eventTime);
        }
    }

    public NoteAdapter(Context context, List<Note> newsList) {
        this.context =context;
        noteList = newsList;
        checklist = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item,parent,false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Note note = noteList.get(position);
        //如果没有标题或正文，就隐藏
        if(note.getTitle().trim().equals("")){
            holder.item_title.setVisibility(View.GONE);
        }else{
            holder.item_title.setVisibility(View.VISIBLE);
            holder.item_title.setText(note.getTitle());
        }
        if(note.getContent().trim().equals("")){
            holder.item_content.setVisibility(View.GONE);
        }else{
            holder.item_content.setVisibility(View.VISIBLE);
            holder.item_content.setText(note.getContent());
        }
        //如果设置提醒，显示闹钟图标&时间
        if(note.isEvent()){
            holder.event.setVisibility(View.VISIBLE);
            holder.eventTime.setText(note.getEventDate());
            holder.eventTime.setVisibility(View.VISIBLE);
        }else {
            holder.event.setVisibility(View.GONE);
            holder.eventTime.setVisibility(View.GONE);
        }
        if(note.getPicPath()!=null){
            //调整imageview大小，使其宽带合适，高度按比例调整
            holder.item_img.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams params = holder.item_img.getLayoutParams();
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            params.width = metrics.widthPixels / 2;
            double scale = params.width * 1.0/note.getPicWidth();
            params.height = (int)(note.getPicHeight()*scale);
            holder.item_img.setLayoutParams(params);
            //使用Glide加载图片
            Glide.with(context)
                    .load(note.getPicPath())
                    .into(holder.item_img);
        }else {
            holder.item_img.setVisibility(View.GONE);
        }
        holder.item_date.setText(note.getDate());


        holder.noteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,EditNote.class);
                intent.putExtra("isEmpty","0");
                intent.putExtra("id",""+note.getId());
                context.startActivity(intent);
            }
        });
        //region 恢复按钮
        holder.btn_rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(note.getNotDel()==0){
                    //从回收站恢复
                    note.setNotDel(1);
                    notifyItemRemoved(position);
                    noteList.remove(position);
                    note.save();
                    notifyDataSetChanged();
                    updateTab();
                }
            }
        });
        //endregion

        //region 删除按钮
        holder.btn_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(note.getNotDel()==1){
                    //放进回收站
                    notifyItemRemoved(position);
                    noteList.remove(position);
                    note.setNotDel(0);
                    note.save();
                    holder.btn_del.setVisibility(View.GONE);
                    notifyDataSetChanged();
                    updateTab();
                }else if(note.getNotDel()==0){
                    //从回收站删除，并删除数据库中的内容
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("提醒");
                    builder.setMessage("是否永久删除？");
                    builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LitePal.delete(Note.class,note.getId());
                            notifyItemRemoved(position);
                            noteList.remove(position);
                            notifyDataSetChanged();
                            updateTab();
                        }
                    });
                    builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            holder.btn_del.setVisibility(View.VISIBLE);
                        }
                    });
                    builder.create();
                    builder.show();
                }
            }
        });
        //endregion
        //region 长按事件
        holder.noteView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if( holder.btn_del.getVisibility()==View.VISIBLE){
                    if(note.getNotDel()==0)
                        holder.btn_rec.setVisibility(View.GONE);
                    holder.btn_del.setVisibility(View.GONE);
                }else{
                    if(note.getNotDel()==0)
                        holder.btn_rec.setVisibility(View.VISIBLE);
                    holder.btn_del.setVisibility(View.VISIBLE);
                }
                if(checklist.contains(note)){
                    checklist.remove(note);
                }else{
                    checklist.add(note);
                }
                return true;
            }
        });
        //endregion
        if (checklist.contains(note)){
            if(note.getNotDel()==0)
                holder.btn_rec.setVisibility(View.VISIBLE);
            holder.btn_del.setVisibility(View.VISIBLE);
        }else {
            if(note.getNotDel()==0)
                holder.btn_rec.setVisibility(View.GONE);
            holder.btn_del.setVisibility(View.GONE);
        }
    }
    public void updateTab(){
        MainActivity.mTitleList.clear();
        MainActivity.mTitleList.add("我的便签 "+LitePal.where("notDel = ?", "1").count("note"));
        MainActivity.mTitleList.add("回收站 "+LitePal.where("notDel = ?", "0").count("note"));
        MainActivity.mViewPagerFragmentAdapter.notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return noteList.size();
    }

}
