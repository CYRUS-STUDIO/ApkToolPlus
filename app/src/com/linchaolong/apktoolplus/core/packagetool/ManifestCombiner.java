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
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
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
    private String icon;
    private Map<String, String> metaDataMap = new LinkedHashMap<>();

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

        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置包名
     *
     * @param applicationId 包名
     * @return
     */
    public ManifestCombiner setApplicationId(String applicationId) {
        this.applicationId = applicationId;
        return this;
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
     * 添加 meta-data
     *
     * @param xmlDocument
     */
    private void addMetadata(XmlDocument xmlDocument){
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

            Logger.print("ManifestCombiner applicationId=" + applicationId);

            MergingReport mergingReport = merger.merge();

            Optional<XmlDocument> mergedDocument = mergingReport.getMergedDocument();

            if (mergedDocument.isPresent()) {

                XmlDocument xmlDocument = mergedDocument.get();

                if (!StringUtils.isEmpty(icon)) {
                    xmlDocument.getXml().getElementsByTagName("application").item(0).getAttributes().getNamedItem("android:icon").setNodeValue(icon);
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

        Logger.print("Manifest merge fail");

        return false;
    }


}
