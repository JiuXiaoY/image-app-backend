package com.ai.imageagent.service;

import com.ai.imageagent.domain.dto.UserDto;
import com.ai.imageagent.domain.vo.LoginUserVo;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.ai.imageagent.domain.entity.User;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户 服务层。
 *
 * @author chenqj
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVo userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取加密密码
     *
     * @param userPassword 用户密码
     * @return 加密密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVo getLoginUserVo(User user);

    /**
     * 获取查询条件
     *
     * @param dto 接受对象
     * @return 封装好查询条件
     */
    QueryWrapper getQueryWrapper(UserDto dto);
}
