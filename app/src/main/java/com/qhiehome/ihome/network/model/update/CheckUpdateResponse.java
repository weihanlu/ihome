package com.qhiehome.ihome.network.model.update;

import java.util.List;

public class CheckUpdateResponse {

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
        private List<ListBean> list;

        public List<ListBean> getList() {
            return list;
        }

        public void setList(List<ListBean> list) {
            this.list = list;
        }

        public static class ListBean {
            /**
             * appKey : 74e19df2c45267a53b23c065b3b3eaf7
             * appType : 2
             * appFileSize : 20503213
             * appName : Ihome
             * appVersion : 1.0
             * appVersionNo : 1
             * appBuildVersion : 5
             * appIdentifier : com.qhiehome.ihome
             * appIcon : 18a0f5ef134bbe32b047c568add54928
             * appDescription : ihome: 共享停车类app
             * appUpdateDescription :
             * appScreenshots : 471ad241698499d4a56148c486dcd9f1,2ff468a77602bb86669058612e3bd508
             * appCreated : 2017-08-11 23:03:20
             */

            private String appKey;
            private String appType;
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
            private String appCreated;

            public String getAppKey() {
                return appKey;
            }

            public void setAppKey(String appKey) {
                this.appKey = appKey;
            }

            public String getAppType() {
                return appType;
            }

            public void setAppType(String appType) {
                this.appType = appType;
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

            public String getAppCreated() {
                return appCreated;
            }

            public void setAppCreated(String appCreated) {
                this.appCreated = appCreated;
            }
        }
    }
}
