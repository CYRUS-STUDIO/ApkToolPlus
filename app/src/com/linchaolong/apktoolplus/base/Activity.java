package com.linchaolong.apktoolplus.base;

import javafx.application.Platform;
import javafx.scene.Node;
import com.linchaolong.apktoolplus.core.Global;

/**
 * Created by linchaolong on 2016/3/25.
 */
public class Activity {

    private Node mRootView;

    protected void initRootView(Node node){
        Node parent = node.getParent();
        Node notNullParent = node;
        while(parent != null){
            notNullParent = parent;
            parent = parent.getParent();
        }
        mRootView = notNullParent;
    }

    public Node getRootView(){
        return mRootView;
    }

    public final void runOnUiThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    public final void showToast(final String msg){
        Global.toast(msg);
    }

}
