package cn.wthee.note.Fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.wthee.note.MainActivity;
import cn.wthee.note.MyNote.NoteAdapter;
import cn.wthee.note.R;

public class FragmentRec extends Fragment {

    protected boolean isCreated = false;
    private View mView;
    private static MyRecycler myRecycler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isCreated = true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_rec,null);
        myRecycler = new MyRecycler();
        myRecycler.initRecyclerView(2,R.id.list_rec,"0",mView);
        return mView;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isCreated) {
            return;
        }
        MainActivity.isSelectAll = false;
        MainActivity.select.setText("全选");
        myRecycler.initRecyclerView(2,R.id.list_rec,"0",mView);
    }

    public static NoteAdapter getAdapter(){
        return myRecycler.getAdapter();
    }
}
