package com.linchaolong.apktoolplus.utils;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.linchaolong.apktoolplus.Config;
import com.linchaolong.apktoolplus.ui.Loading;
import com.linchaolong.apktoolplus.utils.javafx.Delta;
import sun.reflect.misc.MethodUtil;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * UI相关工具类
 *
 * Created by linchaolong on 2015/8/28.
 */
public class UIHelper {

    public static final String TAG = UIHelper.class.getSimpleName();


    /**
     * 监听输入并自动保存输入数据
     *
     * @param textField
     * @param key
     */
    public static void listenerInputAndSave(TextField textField, String key, boolean isEncrypt){
        // 监听输入
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if(isEncrypt){
                    newValue = Base64Tool.encode(newValue.trim());
                }
                Config.set(key, newValue.trim());
            }
        });
    }

    /**
     * 监听输入并自动保存输入数据
     *
     * @param textField
     * @param key
     */
    public static void listenerInputAndSave(TextField textField, String key){
        listenerInputAndSave(textField,key,false);
    }

    /**
     * 恢復数据显示
     *  @param textField
     * @param key
     */
    public static String review(TextField textField, String key){
        String value = Config.get(key, null);
        if(!StringUtils.isEmpty(value)){
            textField.setText(value);
        }
        return value;
    }

    /**
     * 设置无边界、无标题样式
     *
     * @param primaryStage
     */
    public static void setNoBroder (Stage primaryStage){
        primaryStage.initStyle(StageStyle.UNDECORATED);
    }

    /**
     * 设置背景图片
     *
     * @param imgPath
     */
    public static void setBackgroudImage(Parent parent, String imgPath){
        String image = ClassHelper.getResourceAsURL(imgPath).toExternalForm();
        if (image != null){
            parent.setStyle("-fx-background-image: url('" + image + "'); " +
                    "-fx-background-position: center center; " +
                    "-fx-background-repeat: no-repeat;");
        }else{
            Debug.w( "'" + imgPath + "' image not found in classpath.");
        }
    }

    /**
     * 设置背景
     *
     * @param pane      pane
     * @param color     颜色
     * @param radii     圆角半径
     * @param insets    矩形区域
     */
    public static void setBackground(Pane pane, Color color, CornerRadii radii, Insets insets){
        pane.setBackground(new Background(new BackgroundFill(color, radii, insets)));
    }

    /**
     * 设置空背景
     *
     * 已过时，请使用pane.setBackground(Background.EMPTY);
     *
     * @param pane
     */
    @Deprecated
    public static void setEmptyBackground(Pane pane){
        setBackground(pane, Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY);
    }

    /**
     * 创建一个有光晕效果的矩形光圈
     *
     * @param shadowSize 光晕大小
     * @param r          0-255
     * @param g          0-255
     * @param b          0-255
     * @param a          0-1
     * @return
     */
    // Create a shadow effect as a halo around the pane and not within
    // the pane's content area.
    public static Pane createShadowPane(int shadowSize, int r, int g, int b, float a) {

        StringBuilder rgbaBuider = new StringBuilder();
        rgbaBuider.append(r).append(",")
                .append(g).append(",")
                .append(b).append(",")
                .append(a);

        Pane shadowPane = new Pane();
        // a "real" app would do this in a CSS stylesheet.
        shadowPane.setStyle(
                "-fx-background-color: white;" +
                        "-fx-effect: dropshadow(gaussian, rgba(" + rgbaBuider.toString() + "), " + shadowSize + ", 0, 0, 0);" +
                        "-fx-background-insets: " + shadowSize + ";"
        );

        Rectangle innerRect = new Rectangle();
        Rectangle outerRect = new Rectangle();

        shadowPane.layoutBoundsProperty().addListener(
                (observable, oldBounds, newBounds) -> {
                    innerRect.relocate(
                            newBounds.getMinX() + shadowSize,
                            newBounds.getMinY() + shadowSize
                    );
                    innerRect.setWidth(newBounds.getWidth() - shadowSize * 2);
                    innerRect.setHeight(newBounds.getHeight() - shadowSize * 2);

                    outerRect.setWidth(newBounds.getWidth());
                    outerRect.setHeight(newBounds.getHeight());

                    Shape clip = Shape.subtract(outerRect, innerRect);
                    shadowPane.setClip(clip);
                }
        );

        return shadowPane;
    }


    /**
     * 把一个Node导出为png图片
     *
     * @param node      节点
     * @param saveFile  图片文件
     * @return  是否导出成功
     */
    public static boolean node2Png(Node node, File saveFile) {
        SnapshotParameters parameters = new SnapshotParameters();
        // 背景透明
        parameters.setFill(Color.TRANSPARENT);
        WritableImage image = node.snapshot(parameters, null);
        //probably use a file chooser here（原来这里是使用一个文件选择器）
        try {
            return ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", saveFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 把一个Node导出为png图片
     *
     * @param node      节点
     * @param saveFile  图片文件
     * @param width     宽
     * @param height    高
     * @return  是否导出成功
     */
    public static boolean node2Png(Node node, File saveFile, double width, double height) {
        SnapshotParameters parameters = new SnapshotParameters();
        // 背景透明
        parameters.setFill(Color.TRANSPARENT);
        WritableImage image = node.snapshot(parameters, null);

        // 重置图片大小
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        WritableImage exportImage = imageView.snapshot(parameters, null);

        try {
            return ImageIO.write(SwingFXUtils.fromFXImage(exportImage, null), "png", saveFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 反射注入字段值
     *
     * @param parent
     * @param controller
     */
    public static void injectFields(Parent parent, Object controller){
        Class<?> controllerClass = controller.getClass();
        // injectFields
        for(Node node : parent.getChildrenUnmodifiable()){
            if(node.getId() != null){
                Reflect.setFieldValue(controllerClass,controller,node.getId(),node);
            }
            if(node instanceof Parent){
                injectFields((Parent) node, controller);
            }
        }
    }

    /**
     *  强制关联Controller，适用于fxml需要动态关联指定Controller对象的场合，javafx默认关联方式不能解决该问题。
     *
     * @param parent
     * @param controller
     */
    public static void setController(Parent parent, Object controller) {
        try {
            // injectFields
            injectFields(parent,controller);

            if (controller instanceof Initializable) {
                ((Initializable)controller).initialize(null, null);
            }else{
                // Initialize the controller
                Method initializeMethod = controller.getClass().getDeclaredMethod(FXMLLoader.INITIALIZE_METHOD_NAME);
                if (initializeMethod != null) {
                    MethodUtil.invoke(initializeMethod, controller, new Object[]{});
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }


    }

    /**
     * * 设置窗口icon
     *
     * @param stage
     * @param url   icon的url
     */
    public static void setWindowIcon(Stage stage, URL url){
        if (url == null){
            Debug.w("setWindowIcon green_icon url is null");
            return ;
        }
        stage.getIcons().add(new Image(url.toString()));
    }

    /**
     * 设置窗口icon
     *
     * @param stage
     * @param iconPath  icon路径
     */
    public static void setWindowIcon(Stage stage, String iconPath){
        if (iconPath == null){
            Debug.w("setWindowIcon iconPath is null");
            return ;
        }

        File icon = new File(iconPath);
        if (!icon.exists()){
            Debug.w("setWindowIcon green_icon is not exist : " + iconPath);
            return ;
        }

        try {
            setWindowIcon(stage, icon.toURI().toURL().toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 全屏显示
     *
     * @param stage
     */
    public static void fullScreen(Stage stage){
        if (stage.isFullScreen()) {
            stage.setFullScreen(false);
        }else{
            stage.setFullScreen(true);
        }
    }

    /**
     * 最大化
     *
     * @param stage
     */
    public static void maximized(Stage stage){
        stage.setMaximized(!stage.isMaximized());
    }

    /**
     * 打开一个新窗口
     *
     * @param fxmlUrl       fxml文件的url
     * @param isShowTitle   是否显示title
     * @return  Stage，如果出现异常返回null
     */
    public static Stage newWindow(URL fxmlUrl, boolean isShowTitle){
        try {
            Stage stage = new Stage();
            if (!isShowTitle){
                setNoBroder(stage);
            }
            // 背景透明
            stage.initStyle(StageStyle.TRANSPARENT);
            Parent layout = FXMLLoader.load(fxmlUrl);

            Scene scene = new Scene(layout, Color.TRANSPARENT);
            stage.setScene(scene);

            // 在屏幕中间
            stage.centerOnScreen();

            return stage;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 注册拖拽事件
     *
     * @param stage
     * @param root
     */
    public static void registerDragEvent(Stage stage, Node root){
        // allow the clock background to be used to drag the clock around.
        final Delta dragDelta = new Delta();
        root.setOnMousePressed(mouseEvent -> {
            // record a delta distance for the drag and drop operation.
            dragDelta.x = stage.getX() - mouseEvent.getScreenX();
            dragDelta.y = stage.getY() - mouseEvent.getScreenY();
        });
        root.setOnMouseDragged(mouseEvent -> {
            stage.setX(mouseEvent.getScreenX() + dragDelta.x);
            stage.setY(mouseEvent.getScreenY() + dragDelta.y);
        });
    }

    /**
     * 注册拖拽事件
     *
     * @param dialog
     * @param root
     */
    public static void registerDragEvent(Dialog dialog, Node root){
        // allow the clock background to be used to drag the clock around.
        final Delta dragDelta = new Delta();
        root.setOnMousePressed(mouseEvent -> {
            // record a delta distance for the drag and drop operation.
            dragDelta.x = dialog.getX() - mouseEvent.getScreenX();
            dragDelta.y = dialog.getY() - mouseEvent.getScreenY();
        });
        root.setOnMouseDragged(mouseEvent -> {
            dialog.setX(mouseEvent.getScreenX() + dragDelta.x);
            dialog.setY(mouseEvent.getScreenY() + dragDelta.y);
        });
    }

    /**
     * 显示loading
     *
     * @param node
     * @param msg       提示信息
     * @param timeout   超时时间（毫秒）
     * @return  Loading
     */
    public static Loading showLoading(Pane node, String msg, long timeout){
        Loading loading = new Loading(Config.DEFAULT_LOADING_IMAGE);
        loading.setMessage(msg);
        node.getChildren().add(loading);
        loading.lauchTimeoutTimer(timeout);
        return loading;
    }

    /**
     * 移除loading
     *
     * @param node
     * @param loading
     */
    public static void hideLoading(Pane node, Loading loading){
        if(node != null && loading != null){
            Platform.runLater(() -> {
                //if you change the UI, do it here !
                node.getChildren().remove(loading);
            });
        }
    }

}
