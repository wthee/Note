package cn.wthee.note.MyNote;

import org.litepal.crud.LitePalSupport;

public class Note extends LitePalSupport {
    private int id ;
    private String title;
    private String content;
    private String picPath = null;
    private int picWidth;
    private int picHeight;
    private String audioPath = null;
    private String date;
    private int notDel = 1;//1：未删除；0：删除，并存到回收站；-1：彻底删除
    private boolean isEvent = false;
    private String eventDate;

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public boolean isEvent() {
        return isEvent;
    }

    public void setEvent(boolean event) {
        isEvent = event;
    }

    public int getPicWidth() {
        return picWidth;
    }
    public void setPicWidth(int picWidth) {
        this.picWidth = picWidth;
    }

    public int getPicHeight() {
        return picHeight;
    }

    public void setPicHeight(int picHeight) {
        this.picHeight = picHeight;
    }

    public int getNotDel() {
        return notDel;
    }

    public void setNotDel(int notDel) {
        this.notDel = notDel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
