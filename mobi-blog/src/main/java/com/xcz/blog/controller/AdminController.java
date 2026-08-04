package com.xcz.blog.controller;

import com.xcz.blog.domain.dto.UpdateUserRoleDTO;
import com.xcz.blog.domain.dto.UpdateUserStatusDTO;
import com.xcz.blog.domain.mongo.Article;
import com.xcz.blog.domain.mongo.Comment;
import com.xcz.blog.domain.vo.AdminLogVO;
import com.xcz.blog.domain.vo.AdminStatsVO;
import com.xcz.blog.domain.vo.AdminUserVO;
import com.xcz.blog.service.AdminService;
import com.xcz.commons.core.domain.ResponseEntity;
import com.xcz.commons.core.utils.response.ResponseEntityUtils;
import com.xcz.commons.security.utils.SecurityUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理后台接口
 */
@RestController
@RequestMapping("/blog/admin")
@PreAuthorize("@ss.hasAnyRole('MASTER,ADMIN')")
public class AdminController {

    @Resource
    private AdminService adminService;

    /**
     * 获取管理后台统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsVO> getStats() {
        return ResponseEntityUtils.ok(adminService.getStats());
    }

    /**
     * 分页查询文章列表
     */
    @GetMapping("/articles")
    public ResponseEntity<Page<Article>> listArticles(@RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize,
                                                        @RequestParam(required = false) String keyword) {
        return ResponseEntityUtils.ok(adminService.listArticles(pageNum, pageSize, keyword));
    }

    /**
     * 分页查询评论列表
     */
    @GetMapping("/comments")
    public ResponseEntity<Page<Comment>> listComments(@RequestParam(defaultValue = "1") int pageNum,
                                                      @RequestParam(defaultValue = "10") int pageSize,
                                                      @RequestParam(required = false) String keyword) {
        return ResponseEntityUtils.ok(adminService.listComments(pageNum, pageSize, keyword));
    }

    /**
     * 分页查询用户列表
     */
    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserVO>> listUsers(@RequestParam(defaultValue = "1") int pageNum,
                                                       @RequestParam(defaultValue = "10") int pageSize,
                                                       @RequestParam(required = false) String keyword) {
        return ResponseEntityUtils.ok(adminService.listUsers(pageNum, pageSize, keyword));
    }

    /**
     * 修改用户角色（仅 MASTER）
     */
    @PutMapping("/users/{userId}/role")
    @PreAuthorize("@ss.hasRole('MASTER')")
    public ResponseEntity<Void> updateUserRole(@PathVariable Long userId,
                                               @Valid @RequestBody UpdateUserRoleDTO dto) {
        adminService.updateUserRole(SecurityUtils.getUserId(), userId, dto);
        return ResponseEntityUtils.ok();
    }

    /**
     * 修改用户状态（ADMIN / MASTER）
     */
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<Void> updateUserStatus(@PathVariable Long userId,
                                                 @Valid @RequestBody UpdateUserStatusDTO dto) {
        adminService.updateUserStatus(SecurityUtils.getUserId(), userId, dto);
        return ResponseEntityUtils.ok();
    }

    /**
     * 分页查询操作日志，操作人昵称与角色动态关联
     */
    @GetMapping("/logs")
    public ResponseEntity<Page<AdminLogVO>> listLogs(@RequestParam(defaultValue = "1") int pageNum,
                                                     @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntityUtils.ok(adminService.listLogs(pageNum, pageSize));
    }
}
