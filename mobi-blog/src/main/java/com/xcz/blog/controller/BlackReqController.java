package com.xcz.blog.controller;

import com.xcz.blog.domain.BlackContent;
import com.xcz.blog.domain.dto.BlackContentDTO;
import com.xcz.blog.service.BlackContentService;
import com.xcz.commons.core.domain.ResponseEntity;
import com.xcz.commons.core.utils.response.ResponseEntityUtils;
import com.xcz.commons.security.utils.SecurityUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 黑名单配置接口（管理后台）。
 * <p>
 * 用于维护 IP、用户、违禁关键词等拦截规则，仅 ADMIN / MASTER 可访问。
 */
@RestController
@RequestMapping("/blog/black")
@PreAuthorize("@ss.hasAnyRole('MASTER,ADMIN')")
public class BlackReqController {

    @Resource
    private BlackContentService blackContentService;

    /**
     * 新增黑名单规则。
     *
     * @param dto IP / 用户 ID / 关键词配置，至少填一项
     * @return 新规则 ID
     */
    @PreAuthorize("@ss.hasAnyRole('MASTER,ADMIN')")
    @PostMapping
    public ResponseEntity<Long> addBlackContent(@Valid @RequestBody BlackContentDTO dto) {
        return ResponseEntityUtils.ok(blackContentService.addBlackContent(SecurityUtils.getUserId(), dto));
    }

    /**
     * 删除黑名单规则。
     *
     * @param id 规则 ID
     */
    @PreAuthorize("@ss.hasAnyRole('MASTER,ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeBlackContent(@PathVariable Long id) {
        blackContentService.removeBlackContent(SecurityUtils.getUserId(), id);
        return ResponseEntityUtils.ok();
    }

    /**
     * 分页查询黑名单列表。
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 10
     * @return 黑名单分页结果
     */
    @PreAuthorize("@ss.hasAnyRole('MASTER,ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<Page<BlackContent>> listBlackContents(@RequestParam(defaultValue = "1") int pageNum,
                                                                @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntityUtils.ok(blackContentService.listBlackContents(pageNum, pageSize));
    }
}
