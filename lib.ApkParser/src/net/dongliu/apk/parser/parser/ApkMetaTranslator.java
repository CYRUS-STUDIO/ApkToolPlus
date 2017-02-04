package net.dongliu.apk.parser.parser;

import net.dongliu.apk.parser.bean.ApkMeta;
import net.dongliu.apk.parser.bean.GlEsVersion;
import net.dongliu.apk.parser.bean.Permission;
import net.dongliu.apk.parser.bean.UseFeature;
import net.dongliu.apk.parser.struct.xml.*;

/**
 * trans binary xml to text
 *
 * @author Liu Dong im@dongliu.net
 */
public class ApkMetaTranslator implements XmlStreamer {
    private String[] tagStack = new String[100];
    private int depth = 0;
    private ApkMeta apkMeta = new ApkMeta();

    @Override
    public void onStartTag(XmlNodeStartTag xmlNodeStartTag) {
        Attributes attributes = xmlNodeStartTag.getAttributes();
        switch (xmlNodeStartTag.getName()) {
            case "application":
                apkMeta.setLabel(attributes.get("label"));
                apkMeta.setIcon(attributes.get("icon"));
                break;
            case "manifest":
                apkMeta.setPackageName(attributes.get("package"));
                apkMeta.setVersionName(attributes.get("versionName"));
                apkMeta.setVersionCode(attributes.getLong("versionCode"));
                String installLocation = attributes.get("installLocation");
                if (installLocation != null) {
                    apkMeta.setInstallLocation(installLocation);
                }
                break;
            case "uses-sdk":
                apkMeta.setMinSdkVersion(attributes.get("minSdkVersion"));
                apkMeta.setTargetSdkVersion(attributes.get("targetSdkVersion"));
                apkMeta.setMaxSdkVersion(attributes.get("maxSdkVersion"));
                break;
            case "supports-screens":
                apkMeta.setAnyDensity(attributes.getBoolean("anyDensity", false));
                apkMeta.setSmallScreens(attributes.getBoolean("smallScreens", false));
                apkMeta.setNormalScreens(attributes.getBoolean("normalScreens", false));
                apkMeta.setLargeScreens(attributes.getBoolean("largeScreens", false));
                break;
            case "uses-feature":
                String name = attributes.get("name");
                boolean required = attributes.getBoolean("required", false);
                if (name != null) {
                    UseFeature useFeature = new UseFeature();
                    useFeature.setName(name);
                    useFeature.setRequired(required);
                    apkMeta.addUseFeatures(useFeature);
                } else {
                    Integer gl = attributes.getInt("glEsVersion");
                    if (gl != null) {
                        int v = gl;
                        GlEsVersion glEsVersion = new GlEsVersion();
                        glEsVersion.setMajor(v >> 16);
                        glEsVersion.setMinor(v & 0xffff);
                        glEsVersion.setRequired(required);
                        apkMeta.setGlEsVersion(glEsVersion);
                    }
                }
                break;
            case "uses-permission":
                apkMeta.addUsesPermission(attributes.get("name"));
                break;
            case "permission":
                Permission permission = new Permission();
                permission.setName(attributes.get("name"));
                permission.setLabel(attributes.get("label"));
                permission.setIcon(attributes.get("icon"));
                permission.setGroup(attributes.get("group"));
                permission.setDescription(attributes.get("description"));
                String protectionLevel = attributes.get("android:protectionLevel");
                if (protectionLevel != null) {
                    permission.setProtectionLevel(protectionLevel);
                }
                apkMeta.addPermission(permission);
                break;
        }
        tagStack[depth++] = xmlNodeStartTag.getName();
    }

    @Override
    public void onEndTag(XmlNodeEndTag xmlNodeEndTag) {
        depth--;
    }

    @Override
    public void onCData(XmlCData xmlCData) {

    }

    @Override
    public void onNamespaceStart(XmlNamespaceStartTag tag) {

    }

    @Override
    public void onNamespaceEnd(XmlNamespaceEndTag tag) {

    }

    public ApkMeta getApkMeta() {
        return apkMeta;
    }

    private boolean matchTagPath(String... tags) {
        // the root should always be "manifest"
        if (depth != tags.length + 1) {
            return false;
        }
        for (int i = 1; i < depth; i++) {
            if (!tagStack[i].equals(tags[i - 1])) {
                return false;
            }
        }
        return true;
    }

    private boolean matchLastTag(String tag) {
        // the root should always be "manifest"
        return tagStack[depth - 1].endsWith(tag);
    }
}
