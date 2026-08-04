package com.xcz.blog.service;

import com.xcz.blog.domain.dto.UpdateUserRoleDTO;
import com.xcz.blog.domain.dto.UpdateUserStatusDTO;
import com.xcz.blog.domain.mongo.Article;
import com.xcz.blog.domain.mongo.Comment;
import com.xcz.blog.domain.vo.AdminLogVO;
import com.xcz.blog.domain.vo.AdminStatsVO;
import com.xcz.blog.domain.vo.AdminUserVO;
import org.springframework.data.domain.Page;

/**
 * 管理后台服务
 */
public interface AdminService {

    /**
     * 获取管理后台统计数据
     *
     * @return 用户、文章、评论及管理员数量
     */
    AdminStatsVO getStats();

    /**
     * 分页查询文章列表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param keyword  标题或摘要关键词，可为空
     * @return 文章分页结果
     */
    Page<Article> listArticles(int pageNum, int pageSize, String keyword);

    /**
     * 分页查询评论列表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param keyword  评论内容关键词，可为空
     * @return 评论分页结果
     */
    Page<Comment> listComments(int pageNum, int pageSize, String keyword);

    /**
     * 分页查询用户列表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param keyword  昵称或邮箱关键词，可为空
     * @return 用户分页结果
     */
    Page<AdminUserVO> listUsers(int pageNum, int pageSize, String keyword);

    /**
     * 修改用户角色（仅 MASTER 可调用）
     *
     * @param operatorId 当前操作人 ID
     * @param userId     目标用户 ID
     * @param dto        新角色信息
     */
    void updateUserRole(Long operatorId, Long userId, UpdateUserRoleDTO dto);

    /**
     * 修改用户状态（ADMIN / MASTER 可调用）
     *
     * @param operatorId 当前操作人 ID
     * @param userId     目标用户 ID
     * @param dto        新状态信息
     */
    void updateUserStatus(Long operatorId, Long userId, UpdateUserStatusDTO dto);

    /**
     * 分页查询操作日志，操作人昵称与角色动态关联用户表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 日志分页结果
     */
    Page<AdminLogVO> listLogs(int pageNum, int pageSize);
}
