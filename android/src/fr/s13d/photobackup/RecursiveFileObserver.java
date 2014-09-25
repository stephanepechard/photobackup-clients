package fr.s13d.photobackup;

import android.os.FileObserver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class RecursiveFileObserver extends FileObserver {

    List<SingleFileObserver> mObservers;
    String mPath;
    int mMask;


    public RecursiveFileObserver(String path) {
        this(path, ALL_EVENTS);
    }


    public RecursiveFileObserver(String path, int mask) {
        super(path, mask);
        mPath = path;
        mMask = mask;
    }


    @Override
    public void startWatching() {
        if (mObservers != null) {
            return;
        }
        mObservers = new ArrayList<SingleFileObserver>();
        Stack<String> stack = new Stack<String>();
        stack.push(mPath);

        while (!stack.empty()) {
            String parent = stack.pop();
            mObservers.add(new SingleFileObserver(parent, mMask));
            File path = new File(parent);
            File[] files = path.listFiles();
            if (files == null) {
                continue;
            }
            for (File file : files) {
                if (file.isDirectory() && !file.getName().equals(".")
                        && !file.getName().equals("..")) {
                    stack.push(file.getPath());
                }
            }
        }

        for (SingleFileObserver observer : mObservers) {
            observer.startWatching();
        }
    }


    @Override
    public void stopWatching() {
        if (mObservers == null) {
            return;
        }

        for (SingleFileObserver observer : mObservers) {
            observer.stopWatching();
        }

        mObservers.clear();
        mObservers = null;
    }


    @Override
    public void onEvent(int event, String path) {}


    private class SingleFileObserver extends FileObserver {
        private String mPath;

        public SingleFileObserver(String path, int mask) {
            super(path, mask);
            mPath = path;
        }

        @Override
        public void onEvent(int event, String path) {
            String newPath = mPath + "/" + path;
            RecursiveFileObserver.this.onEvent(event, newPath);
        }

    }
}
