package com.linchaolong.apktoolplus.core.xml;

import com.android.manifmerger.*;
import com.android.utils.StdLogger;
import com.google.common.base.Optional;
import com.linchaolong.apktoolplus.utils.FileHelper;
import com.linchaolong.apktoolplus.utils.Logger;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;


/**
 * Created by linchaolong on 2020/10/28.
 */
public class ManifestCombiner {

    private File mainFile;
    private File[] libFiles;
    private File output;

    private String applicationId;

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
    }

    protected boolean checkFile() {
        if (!FileHelper.exists(mainFile) || libFiles == null || libFiles.length == 0 || output == null) {
            return false;
        }
        return true;
    }

    /**
     * 合并 Manifest
     *
     * @return 是否合并成功
     */
    @Deprecated
    private boolean combineOld() {
        if (!checkFile()) {
            return false;
        }

        ManifestMerger mm = new ManifestMerger(MergerLog.wrapSdkLog(new StdLogger(StdLogger.Level.VERBOSE)), null);
        return mm.process(output, mainFile, libFiles, null, null);
    }


    /**
     * 处理占位符
     */
    private void handlePlaceHolder() {
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
     * 合并 Manifest
     *
     * @return 是否合并成功
     */
    public boolean combine() {
        if (!checkFile()) {
            return false;
        }

        try {
            handlePlaceHolder();

            ManifestMerger2.Invoker merger = ManifestMerger2.newMerger(mainFile, new StdLogger(StdLogger.Level.VERBOSE), ManifestMerger2.MergeType.APPLICATION)
                    .addLibraryManifests(libFiles)
                    .setPlaceHolderValue(PlaceholderHandler.APPLICATION_ID, applicationId)
                    .withFeatures(ManifestMerger2.Invoker.Feature.REMOVE_TOOLS_DECLARATIONS);

            Logger.print("ManifestCombiner applicationId=" + applicationId);

            MergingReport mergingReport = merger.merge();

            Optional<XmlDocument> mergedDocument = mergingReport.getMergedDocument();

            XmlDocument xmlDocument = mergedDocument.get();

            String prettyPrint = xmlDocument.prettyPrint();

            FileUtils.write(output, prettyPrint);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


}
