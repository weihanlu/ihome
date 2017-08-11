package com.qhiehome.ihome.network.model.update;

import java.util.List;

public class CheckUpdateResponse {


    /**
     * code : 0
     * message :
     * data : {"appKey":"2ecfc4108f814c51dabbb1b21c80fe90","userKey":"ce208a1437998ef0a8e5f60b7dc4aac1","appType":"2","appIsLastest":"1","appFileSize":"20481349","appName":"Ihome","appVersion":"1.0","appVersionNo":"1","appBuildVersion":"4","appIdentifier":"com.qhiehome.ihome","appIcon":"beb4a34dd417af11fe88143a880fe62c","appDescription":"ihome: 共享停车类app","appUpdateDescription":"","appScreenshots":"471ad241698499d4a56148c486dcd9f1,2ff468a77602bb86669058612e3bd508","appShortcutUrl":"aAkC","appCreated":"2017-08-09 18:44:25","appUpdated":"2017-08-10 18:10:42","appQRCodeURL":"http://www.pgyer.com/app/qrcodeHistory/cfcb599100d98d3bd08e04eacb6308f584e935060776f1bec9bb7ad0ecc25e17","appFollowed":"0","otherapps":[{"appKey":"f0d614e0acb4485340b2235e76608c0c","userKey":"ce208a1437998ef0a8e5f60b7dc4aac1","appName":"Ihome","appVersion":"1.0","appBuildVersion":"3","appIdentifier":"com.qhiehome.ihome","appCreated":"2天前","appUpdateDescription":""},{"appKey":"c2422dc6908e90bd154e5af298f55854","userKey":"ce208a1437998ef0a8e5f60b7dc4aac1","appName":"Ihome","appVersion":"1.0","appBuildVersion":"2","appIdentifier":"com.qhiehome.ihome","appCreated":"2017-08-07","appUpdateDescription":""},{"appKey":"f3b6f17da9bd2b85a52520bb26017472","userKey":"ce208a1437998ef0a8e5f60b7dc4aac1","appName":"Ihome","appVersion":"1.0","appBuildVersion":"1","appIdentifier":"com.qhiehome.ihome","appCreated":"2017-08-07","appUpdateDescription":"versionCode 1versionName 1.0"}],"otherAppsCount":"3","comments":[],"commentsCount":"0"}
     */

    private String code;
    private String message;
    private DataBean data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * appKey : 2ecfc4108f814c51dabbb1b21c80fe90
         * userKey : ce208a1437998ef0a8e5f60b7dc4aac1
         * appType : 2
         * appIsLastest : 1
         * appFileSize : 20481349
         * appName : Ihome
         * appVersion : 1.0
         * appVersionNo : 1
         * appBuildVersion : 4
         * appIdentifier : com.qhiehome.ihome
         * appIcon : beb4a34dd417af11fe88143a880fe62c
         * appDescription : ihome: 共享停车类app
         * appUpdateDescription :
         * appScreenshots : 471ad241698499d4a56148c486dcd9f1,2ff468a77602bb86669058612e3bd508
         * appShortcutUrl : aAkC
         * appCreated : 2017-08-09 18:44:25
         * appUpdated : 2017-08-10 18:10:42
         * appQRCodeURL : http://www.pgyer.com/app/qrcodeHistory/cfcb599100d98d3bd08e04eacb6308f584e935060776f1bec9bb7ad0ecc25e17
         * appFollowed : 0
         * otherapps : [{"appKey":"f0d614e0acb4485340b2235e76608c0c","userKey":"ce208a1437998ef0a8e5f60b7dc4aac1","appName":"Ihome","appVersion":"1.0","appBuildVersion":"3","appIdentifier":"com.qhiehome.ihome","appCreated":"2天前","appUpdateDescription":""},{"appKey":"c2422dc6908e90bd154e5af298f55854","userKey":"ce208a1437998ef0a8e5f60b7dc4aac1","appName":"Ihome","appVersion":"1.0","appBuildVersion":"2","appIdentifier":"com.qhiehome.ihome","appCreated":"2017-08-07","appUpdateDescription":""},{"appKey":"f3b6f17da9bd2b85a52520bb26017472","userKey":"ce208a1437998ef0a8e5f60b7dc4aac1","appName":"Ihome","appVersion":"1.0","appBuildVersion":"1","appIdentifier":"com.qhiehome.ihome","appCreated":"2017-08-07","appUpdateDescription":"versionCode 1versionName 1.0"}]
         * otherAppsCount : 3
         * comments : []
         * commentsCount : 0
         */

        private String appKey;
        private String userKey;
        private String appType;
        private String appIsLastest;
        private String appFileSize;
        private String appName;
        private String appVersion;
        private String appVersionNo;
        private String appBuildVersion;
        private String appIdentifier;
        private String appIcon;
        private String appDescription;
        private String appUpdateDescription;
        private String appScreenshots;
        private String appShortcutUrl;
        private String appCreated;
        private String appUpdated;
        private String appQRCodeURL;
        private String appFollowed;
        private String otherAppsCount;
        private String commentsCount;
        private List<OtherappsBean> otherapps;
        private List<?> comments;

        public String getAppKey() {
            return appKey;
        }

        public void setAppKey(String appKey) {
            this.appKey = appKey;
        }

        public String getUserKey() {
            return userKey;
        }

        public void setUserKey(String userKey) {
            this.userKey = userKey;
        }

        public String getAppType() {
            return appType;
        }

        public void setAppType(String appType) {
            this.appType = appType;
        }

        public String getAppIsLastest() {
            return appIsLastest;
        }

        public void setAppIsLastest(String appIsLastest) {
            this.appIsLastest = appIsLastest;
        }

        public String getAppFileSize() {
            return appFileSize;
        }

        public void setAppFileSize(String appFileSize) {
            this.appFileSize = appFileSize;
        }

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getAppVersion() {
            return appVersion;
        }

        public void setAppVersion(String appVersion) {
            this.appVersion = appVersion;
        }

        public String getAppVersionNo() {
            return appVersionNo;
        }

        public void setAppVersionNo(String appVersionNo) {
            this.appVersionNo = appVersionNo;
        }

        public String getAppBuildVersion() {
            return appBuildVersion;
        }

        public void setAppBuildVersion(String appBuildVersion) {
            this.appBuildVersion = appBuildVersion;
        }

        public String getAppIdentifier() {
            return appIdentifier;
        }

        public void setAppIdentifier(String appIdentifier) {
            this.appIdentifier = appIdentifier;
        }

        public String getAppIcon() {
            return appIcon;
        }

        public void setAppIcon(String appIcon) {
            this.appIcon = appIcon;
        }

        public String getAppDescription() {
            return appDescription;
        }

        public void setAppDescription(String appDescription) {
            this.appDescription = appDescription;
        }

        public String getAppUpdateDescription() {
            return appUpdateDescription;
        }

        public void setAppUpdateDescription(String appUpdateDescription) {
            this.appUpdateDescription = appUpdateDescription;
        }

        public String getAppScreenshots() {
            return appScreenshots;
        }

        public void setAppScreenshots(String appScreenshots) {
            this.appScreenshots = appScreenshots;
        }

        public String getAppShortcutUrl() {
            return appShortcutUrl;
        }

        public void setAppShortcutUrl(String appShortcutUrl) {
            this.appShortcutUrl = appShortcutUrl;
        }

        public String getAppCreated() {
            return appCreated;
        }

        public void setAppCreated(String appCreated) {
            this.appCreated = appCreated;
        }

        public String getAppUpdated() {
            return appUpdated;
        }

        public void setAppUpdated(String appUpdated) {
            this.appUpdated = appUpdated;
        }

        public String getAppQRCodeURL() {
            return appQRCodeURL;
        }

        public void setAppQRCodeURL(String appQRCodeURL) {
            this.appQRCodeURL = appQRCodeURL;
        }

        public String getAppFollowed() {
            return appFollowed;
        }

        public void setAppFollowed(String appFollowed) {
            this.appFollowed = appFollowed;
        }

        public String getOtherAppsCount() {
            return otherAppsCount;
        }

        public void setOtherAppsCount(String otherAppsCount) {
            this.otherAppsCount = otherAppsCount;
        }

        public String getCommentsCount() {
            return commentsCount;
        }

        public void setCommentsCount(String commentsCount) {
            this.commentsCount = commentsCount;
        }

        public List<OtherappsBean> getOtherapps() {
            return otherapps;
        }

        public void setOtherapps(List<OtherappsBean> otherapps) {
            this.otherapps = otherapps;
        }

        public List<?> getComments() {
            return comments;
        }

        public void setComments(List<?> comments) {
            this.comments = comments;
        }

        public static class OtherappsBean {
            /**
             * appKey : f0d614e0acb4485340b2235e76608c0c
             * userKey : ce208a1437998ef0a8e5f60b7dc4aac1
             * appName : Ihome
             * appVersion : 1.0
             * appBuildVersion : 3
             * appIdentifier : com.qhiehome.ihome
             * appCreated : 2天前
             * appUpdateDescription :
             */

            private String appKey;
            private String userKey;
            private String appName;
            private String appVersion;
            private String appBuildVersion;
            private String appIdentifier;
            private String appCreated;
            private String appUpdateDescription;

            public String getAppKey() {
                return appKey;
            }

            public void setAppKey(String appKey) {
                this.appKey = appKey;
            }

            public String getUserKey() {
                return userKey;
            }

            public void setUserKey(String userKey) {
                this.userKey = userKey;
            }

            public String getAppName() {
                return appName;
            }

            public void setAppName(String appName) {
                this.appName = appName;
            }

            public String getAppVersion() {
                return appVersion;
            }

            public void setAppVersion(String appVersion) {
                this.appVersion = appVersion;
            }

            public String getAppBuildVersion() {
                return appBuildVersion;
            }

            public void setAppBuildVersion(String appBuildVersion) {
                this.appBuildVersion = appBuildVersion;
            }

            public String getAppIdentifier() {
                return appIdentifier;
            }

            public void setAppIdentifier(String appIdentifier) {
                this.appIdentifier = appIdentifier;
            }

            public String getAppCreated() {
                return appCreated;
            }

            public void setAppCreated(String appCreated) {
                this.appCreated = appCreated;
            }

            public String getAppUpdateDescription() {
                return appUpdateDescription;
            }

            public void setAppUpdateDescription(String appUpdateDescription) {
                this.appUpdateDescription = appUpdateDescription;
            }
        }
    }
}
