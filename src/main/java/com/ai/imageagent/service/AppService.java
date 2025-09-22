package com.ai.imageagent.service;

import com.ai.imageagent.domain.dto.AppDto;
import com.ai.imageagent.domain.entity.App;
import com.ai.imageagent.domain.entity.User;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import reactor.core.publisher.Flux;

public interface AppService extends IService<App> {

    QueryWrapper buildUserQuery(AppDto dto);

    QueryWrapper buildAdminQuery(AppDto dto);

    Page<App> pageByDto(AppDto dto, boolean isAdmin);

    Flux<String> chatToGenCode(Long appId, String userMessage, User loginUser, boolean agent);

    // 部署应用
    String deployApp(Long appId, User loginUser);

    void generateAppScreenshotAsync(Long appId, String appUrl);
}


