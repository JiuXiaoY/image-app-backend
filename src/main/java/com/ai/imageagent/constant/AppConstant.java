package com.ai.imageagent.constant;

public interface AppConstant {

    /**
     * 应用生成目录
     */
    String CODE_OUTPUT_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 应用部署目录
     */
    String CODE_DEPLOY_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_deploy";

    /**
     * 应用部署域名
     */
    String CODE_DEPLOY_HOST = "http://localhost:8069/";

    /**
     * 聊天历史备份目录
     */
    String CHAT_HISTORY_BACKUP_DIR = System.getProperty("user.dir") + "/tmp/app_chat_history_backup";

    String CHROME_PATH_FOR_WINDOWS = "C:\\Users\\86187\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe";

    String CHROME_PATH_FOR_LINUX = "/usr/bin/google-chrome";

}
