package com.linchaolong.apktoolplus.core.packagetool;

import com.android.manifmerger.*;
import com.android.utils.StdLogger;
import com.google.common.base.Optional;
import com.linchaolong.apktoolplus.utils.FileHelper;
import com.linchaolong.apktoolplus.utils.Logger;
import com.linchaolong.apktoolplus.utils.StringUtils;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Created by linchaolong on 2020/10/28.
 */
public class ManifestCombiner {

    private File mainFile;
    private File[] libFiles;
    private File output;

    private String applicationId;
    private String applicationName;
    private boolean overrideApplication;
    private String icon;
    private String label;
    private String versionCode;
    private String versionName;
    private Map<String, String> metaDataMap = new LinkedHashMap<>();
    private Map<String, String> placeHolderValues = new LinkedHashMap<>();
    private File smaliDir;
    private SplashSetting splashSetting;

    /**
     * 创建一个 AndroidManifest.xml Merger
     *
     * @param mainFile manifest 文件
     * @param libFiles 合并的 manifest 文件列表
     * @param output   合并后的 manifest 文件
     */
    public ManifestCombiner(File mainFile, File[] libFiles, File output) {
        this.mainFile = mainFile;
        this.libFiles = libFiles;
        this.output = output;
        init();
    }

    protected boolean checkFile() {
        if (!FileHelper.exists(mainFile) || libFiles == null || libFiles.length == 0 || output == null) {
            return false;
        }
        return true;
    }

    /**
     * 处理占位符
     */
    private void init() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder mainBuilder = factory.newDocumentBuilder();
            Document mainDoc = mainBuilder.parse(mainFile);

            Node manifest = mainDoc.getElementsByTagName("manifest").item(0);

            applicationId = manifest.getAttributes().getNamedItem("package").getNodeValue();

            fixFileProviderConflict();

        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置包名
     *
     * @param applicationId 包名
     */
    public ManifestCombiner setApplicationId(String applicationId) {
        if (!StringUtils.isEmpty(applicationId)) {

            // 自动修改 authorities 防止安装冲突
            new FileConfigTool(mainFile).setParam("android:authorities=\"" + this.applicationId, "android:authorities=\"" + applicationId).save();

            this.applicationId = applicationId;

            fixFileProviderConflict();
        }
        return this;
    }

    /**
     * 解决 android.support.v4.content.FileProvider 组件冲突问题
     */
    protected void fixFileProviderConflict() {
        try {
            String fileProvider = "android.support.v4.content.FileProvider";

            String mainContent = FileUtils.readFileToString(mainFile);

            if (mainContent.contains(fileProvider)) {

                if (libFiles != null && libFiles.length > 0) {

                    for (File libFile : libFiles) {

                        String libContent = FileUtils.readFileToString(libFile);

                        if (libContent.contains(fileProvider)) {

                            Logger.error("find file provider conflict");

                            removeFileProvider(mainFile);

                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 把 android.support.v4.content.FileProvider 从 AndroidManifest.xml 文件中移除
     */
    protected void removeFileProvider(File manifestFile) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(manifestFile);

            NodeList providerList = document.getElementsByTagName("provider");

            for (int i = 0; i < providerList.getLength(); i++) {

                Node node = providerList.item(i);

                String name = node.getAttributes().getNamedItem("android:name").getNodeValue();

                if (StringUtils.isEquals(name, "android.support.v4.content.FileProvider")) {

                    Node applicationNode = document.getElementsByTagName("application").item(0);

                    applicationNode.removeChild(node);

                    writeDocument(document, manifestFile);

                    Logger.error("fixed file provider conflict");

                    break;
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }

    /**
     * Convert org.w3c.dom.Document to File file
     */
    protected void writeDocument(Document doc, File output) {
        try {
            // write the content into xml file
            DOMSource source = new DOMSource(doc);
            FileWriter writer = new FileWriter(output);

            StreamResult result = new StreamResult(writer);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置icon
     *
     * @param icon
     */
    public ManifestCombiner setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    /**
     * 设置游戏名
     *
     * @param label
     * @return
     */
    public ManifestCombiner setLabel(String label) {
        this.label = label;
        return this;
    }

    /**
     * 设置 application name
     *
     * @param applicationName
     * @return
     */
    public ManifestCombiner setApplicationName(String applicationName, boolean overrideApplication) {
        this.applicationName = applicationName;
        this.overrideApplication = overrideApplication;
        return this;
    }

    public ManifestCombiner setApplicationName(String applicationName) {
        return setApplicationName(applicationName, false);
    }

    /**
     * 设置 versionCode
     *
     * @param versionCode
     * @return
     */
    public ManifestCombiner setVersionCode(String versionCode) {
        this.versionCode = versionCode;
        return this;
    }

    /**
     * 设置 versionName
     *
     * @param versionName
     * @return
     */
    public ManifestCombiner setVersionName(String versionName) {
        this.versionName = versionName;
        return this;
    }

    /**
     * 设置 mata-data
     *
     * @param key
     * @param value
     */
    public ManifestCombiner setMetadata(String key, String value) {
        metaDataMap.put(key, value);
        return this;
    }

    /**
     * 设置 mata-data
     *
     * @param metadata
     */
    public ManifestCombiner setMetadata(Map<String, String> metadata) {
        metaDataMap.putAll(metadata);
        return this;
    }

    /**
     * 设置 PlaceHolderValue
     *
     * @param key
     * @param value
     */
    public ManifestCombiner setPlaceHolderValue(String key, String value) {
        placeHolderValues.put(key, value);
        return this;
    }

    /**
     * 设置 PlaceHolderValues
     *
     * @param metadata
     */
    public ManifestCombiner setPlaceHolderValues(Map<String, String> metadata) {
        placeHolderValues.putAll(metadata);
        return this;
    }

    /**
     * 添加 meta-data
     *
     * @param xmlDocument
     */
    private void addMetadata(XmlDocument xmlDocument) {
        if (metaDataMap.isEmpty()) {
            return;
        }
        Document xml = xmlDocument.getXml();

        Node application = xml.getElementsByTagName("application").item(0);

        for (Map.Entry<String, String> entry : metaDataMap.entrySet()) {

            Element metaData = xml.createElement("meta-data");

            metaData.setAttribute("android:name", entry.getKey());
            metaData.setAttribute("android:value", entry.getValue());

            application.appendChild(metaData);
        }
    }

    /**
     * smali目录
     *
     * @param smaliDir
     * @return
     */
    public ManifestCombiner setSmaliDir(File smaliDir) {
        this.smaliDir = smaliDir;
        return this;
    }

    /**
     * 闪屏设置
     *
     * @param splashSetting
     */
    public void setSplashSetting(SplashSetting splashSetting) {
        this.splashSetting = splashSetting;
    }

    /**
     * 合并 Manifest
     *
     * @return 是否合并成功
     */
    public boolean combine() {
        if (!checkFile()) {
            return false;
        }

        try {

            ManifestMerger2.Invoker merger = ManifestMerger2.newMerger(mainFile, new StdLogger(StdLogger.Level.VERBOSE), ManifestMerger2.MergeType.APPLICATION)
                    .addLibraryManifests(libFiles)
//                    .setPlaceHolderValue(PlaceholderHandler.APPLICATION_ID, applicationId)
                    .setOverride(ManifestMerger2.SystemProperty.PACKAGE, applicationId)
                    .withFeatures(ManifestMerger2.Invoker.Feature.REMOVE_TOOLS_DECLARATIONS);

            if (!StringUtils.isEmpty(versionCode)) {
                merger.setOverride(ManifestMerger2.SystemProperty.VERSION_CODE, versionCode);
            }

            if (!StringUtils.isEmpty(versionName)) {
                merger.setOverride(ManifestMerger2.SystemProperty.VERSION_NAME, versionName);
            }

            if (placeHolderValues != null && !placeHolderValues.isEmpty()) {
                merger.setPlaceHolderValues(placeHolderValues);
            }

            Logger.print("ManifestCombiner applicationId=" + applicationId);

            MergingReport mergingReport = merger.merge();

            Optional<XmlDocument> mergedDocument = mergingReport.getMergedDocument();

            if (mergedDocument.isPresent()) {

                XmlDocument xmlDocument = mergedDocument.get();

                if (!StringUtils.isEmpty(icon)) {
                    xmlDocument.getXml().getElementsByTagName("application").item(0).getAttributes().getNamedItem("android:icon").setNodeValue(icon);
                }

                if (!StringUtils.isEmpty(label)) {
                    xmlDocument.getXml().getElementsByTagName("application").item(0).getAttributes().getNamedItem("android:label").setNodeValue(label);
                }

                if (!StringUtils.isEmpty(applicationName)) {
                    Node applicationNode = xmlDocument.getXml().getElementsByTagName("application").item(0);
                    Node applicationNameNode = applicationNode.getAttributes().getNamedItem("android:name");

                    // https://blog.csdn.net/qq_35559358/article/details/107234249
                    if (applicationNode.getAttributes().getNamedItem("android:extractNativeLibs") != null) {
                        applicationNode.getAttributes().removeNamedItem("android:extractNativeLibs");
                    }

                    if (applicationNameNode == null || overrideApplication) {
                        ((Element) applicationNode).setAttributeNS("http://schemas.android.com/apk/res/android", "android:name", applicationName);
                    } else {
                        String originalApplicationName = applicationNameNode.getNodeValue();

                        if (!originalApplicationName.equals(applicationName)) {
                            if (smaliDir != null && smaliDir.exists()) {
                                SmaliTool smaliTool = new SmaliTool(new File(smaliDir, originalApplicationName.replace(".", "/") + ".smali"));
                                smaliTool.setSuper(applicationName);
                                smaliTool.save();
                            } else {
                                Logger.error("try to change " + originalApplicationName + " super class fail, cause smaliDir=" + smaliDir + " is null or not exists.");
                            }
                        } else {
                            Logger.error(originalApplicationName + " equals applicationName");
                        }
                    }
                }

                // 闪屏设置
                if (splashSetting != null) {
                    String mainActivity = setSplashActivity(xmlDocument, splashSetting);
                    setMetadata(splashSetting.mainActivityMetaKey, mainActivity);
                }

                addMetadata(xmlDocument);

                String prettyPrint = xmlDocument.prettyPrint();

                FileUtils.write(output, prettyPrint);

                Logger.print("Manifest merge success");

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Logger.error("Manifest merge fail");

        return false;
    }

    private String setSplashActivity(XmlDocument xmlDocument, SplashSetting splashSetting) {

        NodeList activityList = xmlDocument.getXml().getElementsByTagName("activity");

        Logger.print(activityList.toString());

        Node applicationNode = xmlDocument.getXml().getElementsByTagName("application").item(0);

        for (int i = 0; i < activityList.getLength(); i++) {

            Node activityItem = activityList.item(i);

            NodeList activityChildNodes = activityItem.getChildNodes();

            for (int j = 0; j < activityChildNodes.getLength(); j++) {

                if (StringUtils.isEquals("intent-filter", activityChildNodes.item(j).getNodeName())) {

                    NodeList intentFilterChildNodes = activityChildNodes.item(j).getChildNodes();

                    for (int k = 0; k < intentFilterChildNodes.getLength(); j++) {

                        if (StringUtils.isEquals("action", intentFilterChildNodes.item(j).getNodeName())) {

                            if (StringUtils.isEquals("android.intent.action.MAIN", intentFilterChildNodes.item(j).getAttributes().getNamedItem("android:name").getNodeValue())) {

                                // add splash activity config
                                Node splashNode = activityItem.cloneNode(true);

                                Node nameItem = splashNode.getAttributes().getNamedItem("android:name");

                                String mainActivity = nameItem.getNodeValue();

                                Logger.print("MainActivity=" + mainActivity);

                                nameItem.setNodeValue(splashSetting.splashActivity);

                                applicationNode.appendChild(splashNode);

                                // remove intent-filter from main activity
                                activityItem.removeChild(activityChildNodes.item(j));

                                // launchMode
                                if (!StringUtils.isEmpty(splashSetting.launchMode)) {
                                    Node launchModeItem = splashNode.getAttributes().getNamedItem("android:launchMode");
                                    if (launchModeItem != null) {
                                        launchModeItem.setNodeValue(splashSetting.launchMode);
                                    } else {
                                        ((Element) splashNode).setAttributeNS("http://schemas.android.com/apk/res/android", "android:launchMode", splashSetting.launchMode);
                                    }
                                }

                                return mainActivity;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static class SplashSetting {

        public String splashActivity;
        public String mainActivityMetaKey;
        // android:launchMode="0" = android:launchMode="standard"
        // android:launchMode="1" = android:launchMode="singleTop"
        // android:launchMode="2" = android:launchMode="singleTask"
        // android:launchMode="3" = android:launchMode="singleInstance"
        public String launchMode;

        public SplashSetting(String splashActivity, String mainActivityMetaKey) {
            this.splashActivity = splashActivity;
            this.mainActivityMetaKey = mainActivityMetaKey;
        }

        public SplashSetting(String splashActivity, String mainActivityMetaKey, String launchMode) {
            this.splashActivity = splashActivity;
            this.mainActivityMetaKey = mainActivityMetaKey;
            this.launchMode = launchMode;
        }
    }

}
