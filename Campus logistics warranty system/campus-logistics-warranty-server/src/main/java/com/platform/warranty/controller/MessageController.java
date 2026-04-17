package com.platform.warranty.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.warranty.domain.dto.MessageQueryDTO;
import com.platform.warranty.domain.entity.Message;
import com.platform.warranty.domain.entity.User;
import com.platform.warranty.service.IMessageService;
import com.platform.warranty.service.IUserService;
import com.platform.warranty.utils.AjaxResult;
import com.platform.warranty.utils.CommonUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户留言表 前端控制器
 * </p>
 *
 * @author baomidou
 * @since 2025-05-13
 */
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {
private final IMessageService messageService;
private final IUserService userService;
    @GetMapping("/list")
    private AjaxResult<Page<Message>> list(MessageQueryDTO messageQueryDTO, HttpServletRequest request){
        Page<Message> page = new Page<>(messageQueryDTO.getPageNum(), messageQueryDTO.getPageSize());
        Long userId1 = CommonUtils.getUserId(request);
        User user = userService.getById(userId1);
        if (user.getRole().equalsIgnoreCase("admin")) {
            messageService.lambdaQuery()
                .like(StringUtils.isNotBlank(messageQueryDTO.getKeyword()), Message::getReply, messageQueryDTO.getKeyword())
                .or()
                .like(StringUtils.isNotBlank(messageQueryDTO.getKeyword()), Message::getContent, messageQueryDTO.getKeyword())
                .page(page);
        page.getRecords().forEach(item->{
            Long userId = item.getUserId();
            User byId = userService.getById(userId);
            if (byId != null){
                item.setUserName(byId.getName());
            }
        });
        return AjaxResult.success(page);
        }
        messageService.lambdaQuery()
                .eq(Message::getUserId, userId1)
                .page(page);
        return AjaxResult.success(page);

    }
    @GetMapping("/getById/{id}")
    private AjaxResult<Message> getById(@PathVariable Long id){
        return AjaxResult.success(messageService.getById(id));
    }
    @GetMapping("/deleteById/{id}")
    private AjaxResult<Void> deleteById(@PathVariable Long id){
        messageService.removeById(id);
        return AjaxResult.success();
    }
    @PostMapping("/save")
    private AjaxResult<Void> save(@RequestBody Message message, HttpServletRequest request){
        Long userId = CommonUtils.getUserId(request);
        Long id = message.getId();
        if (id == null){
            message.setUserId(userId);
        }
        messageService.saveOrUpdate(message);
        return AjaxResult.success();
    }
}
