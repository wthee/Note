package cn.wthee.note.Fragment;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import org.litepal.LitePal;


import cn.wthee.note.MyNote.Note;
import cn.wthee.note.MyNote.NoteAdapter;

public class MyRecycler  {

    private NoteAdapter adapter;
    private RecyclerView recyclerView;
    private StaggeredGridLayoutManager layoutManager;


    public void initRecyclerView(int spanCount, int id, String condition, View mView){
        recyclerView = (RecyclerView)mView.findViewById(id);
        layoutManager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new NoteAdapter(mView.getContext(),LitePal.where("notDel = ?",condition).find(Note.class));
        recyclerView.setAdapter(adapter);
    }

    public NoteAdapter getAdapter(){
        return adapter;
    }
}
