package com.linchaolong.apktoolplus.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * Created by linchaolong on 2016/3/25.
 */
public class UIStack {

    /**
     * ui stack
     **/
    private LinkedList<Parent> uiStack = new LinkedList<>();
    /**
     * ui cache
     **/
    private Map<URL, Parent> uiCache = new HashMap<>();

    private AnchorPane mContainer;

    public UIStack(AnchorPane container) {
        mContainer = container;
    }

    /**
     * 把一个ui加入到ui stack
     *
     * @param fxmlUrl ui路径
     * @param isCache 是否缓存该ui
     */
    public void push(URL fxmlUrl, boolean isCache) {
        try {
            Parent pane = uiCache.get(fxmlUrl);
            // 第一次加载该ui
            if (pane == null) {
                StackPane stackPane = new StackPane();
                Parent node = FXMLLoader.load(fxmlUrl);
                stackPane.getChildren().add(node);
                pane = stackPane;
                if (isCache) {
                    uiCache.put(fxmlUrl, pane);
                }
            }
            // 隐藏上一个ui
            Parent last = uiStack.peekLast();
            if (last != null) {
                last.setVisible(false);
            }

            // 添加到ui stack
            mContainer.getChildren().add(pane);
            uiStack.add(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从ui stack中弹出一个ui
     */
    public void pop() {
        // 移除栈顶ui
        Parent last = uiStack.pollLast();
        if (last != null) {
            mContainer.getChildren().remove(last);
            // 显示上一个ui
            last = uiStack.peekLast();
            if (last != null) {
                last.setVisible(true);
            }
        }
    }

    /**
     * 返回UIStack的size
     *
     * @return
     */
    public int size(){
        return uiStack.size();
    }

}
